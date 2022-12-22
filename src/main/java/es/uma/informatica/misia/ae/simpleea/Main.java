package es.uma.informatica.misia.ae.simpleea;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.github.sh0nk.matplotlib4j.NumpyUtils;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import flexjson.JSONSerializer;

public class Main {

	private static final int ITERATIONS = 1;
	private static final int INSTANCES = 100;
	
	private static final boolean saveResults = true;
	
	public static void main (String args []) {
		if (args.length < 2) {
			help();
			return;
		}
		if (args[0].equals("plot")) {
			
		}

		if (args.length < 6) {
			System.err.println("Run experiment: Invalid number of arguments");
			System.err.println("Arguments: $ run <initial population size> <function evaluations> <initial bitflip probability> <problem size> <initial parameter>");
			System.err.println("\nArguments details:");
			System.err.println("<initial population size>: fixed population if starts varying mutation");
			System.err.println("<function evaluations>: number of functions evaluations");
			System.err.println("<initial bitflip probability>: fixed probability of mutation if starts varying population");
			System.err.println("<problem size>: size of the problem");
			System.err.println("<parameter>: 0 = mutation OR 1 = population: Decides which parameter is optimized first");
			return;
		}
		
		double[] avgPopulations = new double[INSTANCES];
		double[] avgMutations = new double[INSTANCES];
		double[] bestPopulations = new double[INSTANCES];
		double[] lowerBestPopulations = new double[INSTANCES];
		double[] bestMutations = new double[INSTANCES];
		double[] lowerBestMutations = new double[INSTANCES];
				
		Individual[] bestSolutions = new Individual[INSTANCES];
		Individual[] lowerBestSolutions = new Individual[INSTANCES];
		
		int n = Integer.parseInt(args[4]);
		int parameter = Integer.parseInt(args[5]);
		
		// QUBO instances
		for (int quboIndex = 0; quboIndex < INSTANCES; quboIndex++) {
			long quboSeed = System.currentTimeMillis() * quboIndex;
			Qubo problem = new Qubo(n, quboSeed);

			double accumulatedMutationRate = 0, standardDeviationMutationRate = 0;
			double accumulatedPopulation = 0, standardDeviationPopulation = 0;
			
			double[] historyMutation = new double[ITERATIONS];
			double[] historyPopulation = new double[ITERATIONS];
			
			bestSolutions[quboIndex] = new Individual();
			lowerBestSolutions[quboIndex] = new Individual();
			
			// X executions for each instance
			for (int executionSeed = 0; executionSeed < ITERATIONS; executionSeed++) {
				double[] arguments = resetArguments(args);

				
				ExperimentResult result = runExperiment(problem, arguments, executionSeed, parameter);
				double[] parametersFromExperiment = result.getBestParameters();
				
				historyMutation[executionSeed] = parametersFromExperiment[0];
				historyPopulation[executionSeed] = parametersFromExperiment[1];
				if (bestSolutions[quboIndex].getFitness() < result.getSolution().getFitness()) {
					bestSolutions[quboIndex] = result.getSolution();
					bestPopulations[quboIndex] = parametersFromExperiment[1];
					bestMutations[quboIndex] = parametersFromExperiment[0];
				}
				if (result.getSolution().getFitness() < lowerBestSolutions[quboIndex].getFitness()) {
					lowerBestSolutions[quboIndex] = result.getSolution();
					lowerBestPopulations[quboIndex] = parametersFromExperiment[1];
					lowerBestMutations[quboIndex] = parametersFromExperiment[0];
				}

				accumulatedMutationRate += parametersFromExperiment[0];
				accumulatedPopulation += parametersFromExperiment[1];
			}
			
			avgMutations[quboIndex] = accumulatedMutationRate / ITERATIONS;
			avgPopulations[quboIndex] = accumulatedPopulation / ITERATIONS;
			
			for (int i = 0; i < ITERATIONS; i++) {
				standardDeviationMutationRate += Math.pow(historyMutation[i] - avgMutations[quboIndex], 2);
				standardDeviationPopulation += Math.pow(historyPopulation[i] - avgPopulations[quboIndex], 2);
			}
			
			standardDeviationMutationRate = Math.sqrt(standardDeviationMutationRate / ITERATIONS);
			standardDeviationPopulation = Math.sqrt(standardDeviationPopulation / ITERATIONS);
					
			System.out.println("Instance QUBO[" + quboIndex + "] after " + ITERATIONS +  " executions.");
			System.out.println("Average optimal mutation rate = " + avgMutations[quboIndex]);
			System.out.println("Average optimal population size = " + avgPopulations[quboIndex]);
			System.out.println("Standard deviation mutation rate = " + standardDeviationMutationRate);
			System.out.println("Standard deviation population size = " + standardDeviationPopulation);
			System.out.println("Solution: " + bestSolutions[quboIndex]);
			System.out.println();
		}
		
		if (saveResults) {
			InstanceExecutionData session = new InstanceExecutionData(avgPopulations, avgMutations, bestSolutions);
			JSONSerializer serializer = new JSONSerializer().prettyPrint(true); 
		    String jsonString = serializer.deepSerialize(session);
		    System.out.println(jsonString);
		    saveSession("quboExperiment",jsonString);
		}
	    
		plot(avgPopulations, bestPopulations, lowerBestPopulations, INSTANCES, "Avg Population", 
				"Highest population", "Lowest population", "#66DD66", "#0000FF", "#FF0000", "Populations on all the instances");
		plot(avgMutations, bestMutations, lowerBestMutations, INSTANCES, "Avg Mutation", 
				"Highest mutation", "Lowest mutation", "#66DD66", "#0000FF", "#FF0000", "Mutations on all the instances");
	}
	
	private static ExperimentResult runExperiment(Problem problem, double[] arguments, long seed, int parameter){

		if (parameter == 0) {
			ExperimentResult resultWithParameter1 = 
					runMutationExperiment(problem, arguments, seed);
			arguments[2] = (resultWithParameter1.getBestParameters())[0];
			
			ExperimentResult resultWithParameter2 =  
					runPopulationExperiment(problem, arguments, seed);
			
			return new ExperimentResult(new double[] {
					(resultWithParameter1.getBestParameters())[0],
					(resultWithParameter2.getBestParameters())[0]},
					resultWithParameter2.solution);
		}
		
		ExperimentResult resultWithParameter1 = 
				runPopulationExperiment(problem, arguments, seed);
		arguments[0] = (resultWithParameter1.getBestParameters())[0];
		
		ExperimentResult resultWithParameter2 =  
				runMutationExperiment(problem, arguments, seed);
		
		return new ExperimentResult(new double[] {
				(resultWithParameter2.getBestParameters())[0],
				(resultWithParameter1.getBestParameters())[0]},
				resultWithParameter2.solution);
	}
	
	/*
	 * Search Best mutation with fixed population
	 * Vary mutation rate from 0.1, 0.2... to 0.9
	 */
	private static ExperimentResult runMutationExperiment(Problem problem, double[] arguments, long seed) {
		// arguments[0] is fixed population
		Experiment experimentMutation = 
				new Experiment("mutation", "population", 0.1, 0.9, 0.05, 2, arguments[0], seed);
		return experimentMutation.Run(problem, arguments);
	}
	
	/*
	 * Search Best population with fixed mutation rate
	 * Vary population from 100, 150, 200, 250... 20000
	 */
	private static ExperimentResult runPopulationExperiment(Problem problem, double[] arguments, long seed) {
		// arguments[2] is fixed mutation
		Experiment experimentPopulation = 
				new Experiment("population", "mutation", 10, 10000, 100, 0, arguments[2], seed);
		return experimentPopulation.Run(problem, arguments);
	}
	
	private static double[] resetArguments(String[] args) {
		double[] arguments = new double[5];
		for(int i = 0; i < 5; i++) {
			if (args.length > i+1) arguments[i] = Double.parseDouble(args[i+1]);
		}
		return arguments;
	}
	
	private static void _plot(Plot plot, double[] values, int numInstances,
			String label, String color) {
		List<Double> x = NumpyUtils.linspace(1, numInstances, numInstances);
		List<Double> y = x.stream()
				.map(i -> values[(int) (Math.round(i)-1)])
				.collect(Collectors.toList());

		plot.plot().add(x, y, "o").label(label).color(color);
	}
	
	private static void plot(double[] avgBestValues,
			double[] bestValues,
			double[] lowerBestValues,
			int numInstances,
			String avgLabel, String bestLabel, String lowerLabel,
			String avgColor, String bestColor, String lowerColor,
			String title) {
		
		Plot plt = Plot.create();
		
		_plot(plt, bestValues, numInstances, bestLabel, bestColor);
		_plot(plt, lowerBestValues, numInstances, lowerLabel, lowerColor);
		_plot(plt, avgBestValues, numInstances, avgLabel, avgColor);
		
		plt.legend().loc("upper right");
		plt.title(title);
		try {
			plt.show();
		} catch (IOException | PythonExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private static void saveSession(String sessionName, String sessionData) {
		try {
			  String fileName = sessionName + "_" + System.currentTimeMillis() + ".txt";
		      FileWriter myWriter = new FileWriter(fileName);
		      myWriter.write(sessionData);
		      myWriter.close();
		      System.out.println("Successfully saved to the file " + sessionName);
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      System.out.println(e);
		      e.printStackTrace();
		    }
	}
	
	private static void help() {
		System.err.println("Options: 'run' or 'plot'");
		System.err.println();
		System.err.println("Arguments: $ plot <file>");
		System.err.println("\n<file>: path to the file to plot");
		System.err.println();
		
		System.err.println("Arguments: $ run <initial population size> <function evaluations> <initial bitflip probability> <problem size> <initial parameter>");
		System.err.println("\nArguments details:");
		System.err.println("<initial population size>: fixed population if starts varying mutation");
		System.err.println("<function evaluations>: number of functions evaluations");
		System.err.println("<initial bitflip probability>: fixed probability of mutation if starts varying population");
		System.err.println("<problem size>: size of the problem");
		System.err.println("<parameter>: 0 = mutation OR 1 = population: Decides which parameter is optimized first");
		
	}
}
