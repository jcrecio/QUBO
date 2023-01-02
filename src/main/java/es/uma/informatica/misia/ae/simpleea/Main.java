package es.uma.informatica.misia.ae.simpleea;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.github.sh0nk.matplotlib4j.NumpyUtils;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class Main {

	/**
	 * Execution parameters
	 */
	private static final int LOWER_BOUND_POPULATION = 5;
	private static final int UPPER_BOUND_POPULATION = 50;
	private static final int INCREMENT_POPULATION = 1;

	private static final double LOWER_BOUND_MUTATION = 0.015;
	private static final double UPPER_BOUND_MUTATION = 0.02;
	private static final double INCREMENT_MUTATION = 0.001;

	
	private static final int NUMBER_ITERATIONS = 30;
	private static final int QUBO_INSTANCES = 25;
	
	private static final boolean saveResults = true;
	
	public static void main (String args []) {
		if (args.length < 2) {
			help();
			return;
		}
		if (args[0].equals("plot")) {
			plotFile(args[1]);
			return;
		}

		if (args.length < 6) {
			help();
			return;
		}
		
		double[] avgPopulations = new double[QUBO_INSTANCES];
		double[] avgMutations = new double[QUBO_INSTANCES];
		double[] avgFitness = new double[QUBO_INSTANCES];
		double[] bestPopulations = new double[QUBO_INSTANCES];
		double[] bestMutations = new double[QUBO_INSTANCES];
				
		Individual[] bestSolutions = new Individual[QUBO_INSTANCES];
		
		int n = Integer.parseInt(args[4]);
		int parameter = Integer.parseInt(args[5]);
		
		// QUBO instances
		for (int quboIndex = 0; quboIndex < QUBO_INSTANCES; quboIndex++) {
			long quboSeed = System.currentTimeMillis() * quboIndex;
			Qubo problem = new Qubo(n, quboSeed);

			double accumulatedMutationRate = 0, standardDeviationMutationRate = 0;
			double accumulatedPopulation = 0, standardDeviationPopulation = 0;
			double accumulatedFitness = 0, standardDeviationFitness = 0;
			
			double[] historyMutation = new double[NUMBER_ITERATIONS];
			double[] historyPopulation = new double[NUMBER_ITERATIONS];
			double[] historyFitness = new double[NUMBER_ITERATIONS];
			
			bestSolutions[quboIndex] = new Individual();
			
			// Number of iterations for each instance
			for (int executionSeed = 0; executionSeed < NUMBER_ITERATIONS; executionSeed++) {
				double[] arguments = resetArguments(args);

				ExperimentResult result = runExperiment(problem, arguments, executionSeed, parameter);
				double[] parametersFromExperiment = result.getBestParameters();
				
				historyMutation[executionSeed] = parametersFromExperiment[0];
				historyPopulation[executionSeed] = parametersFromExperiment[1];
				historyFitness[executionSeed] = result.getSolution().getFitness();
				if (bestSolutions[quboIndex].getFitness() < result.getSolution().getFitness()) {
					bestSolutions[quboIndex] = result.getSolution();
					bestPopulations[quboIndex] = parametersFromExperiment[1];
					bestMutations[quboIndex] = parametersFromExperiment[0];
				}

				accumulatedMutationRate += parametersFromExperiment[0];
				accumulatedPopulation += parametersFromExperiment[1];
				accumulatedFitness += result.getSolution().getFitness();
			}
			
			avgMutations[quboIndex] = accumulatedMutationRate / NUMBER_ITERATIONS;
			avgPopulations[quboIndex] = accumulatedPopulation / NUMBER_ITERATIONS;
			avgFitness[quboIndex] = accumulatedFitness / NUMBER_ITERATIONS;
			
			for (int i = 0; i < NUMBER_ITERATIONS; i++) {
				standardDeviationMutationRate += Math.pow(historyMutation[i] - avgMutations[quboIndex], 2);
				standardDeviationPopulation += Math.pow(historyPopulation[i] - avgPopulations[quboIndex], 2);
				standardDeviationFitness += Math.pow(historyFitness[i] - avgFitness[quboIndex], 2);
			}
			
			standardDeviationMutationRate = Math.sqrt(standardDeviationMutationRate / NUMBER_ITERATIONS);
			standardDeviationPopulation = Math.sqrt(standardDeviationPopulation / NUMBER_ITERATIONS);
			standardDeviationFitness = Math.sqrt(standardDeviationFitness / NUMBER_ITERATIONS);
					
			System.out.println("Instance QUBO[" + quboIndex + "] after " + NUMBER_ITERATIONS +  " executions.");
			System.out.println("....Varying " + (parameter == 0 ? "mutation " : "population "));
			System.out.println("Average fitness = " + avgFitness[quboIndex]);
			System.out.println("Average optimal mutation rate = " + avgMutations[quboIndex]);
			System.out.println("Average optimal population size = " + avgPopulations[quboIndex]);

			System.out.println("Standard deviation fitness = " + standardDeviationFitness);
			System.out.println("Standard deviation mutation rate = " + standardDeviationMutationRate);
			System.out.println("Standard deviation population size = " + standardDeviationPopulation);
			
			System.out.println("Best optimal population size = " + bestPopulations[quboIndex]);
			System.out.println("Best optimal mutation rate = " + bestMutations[quboIndex]);
			System.out.println("Best Solution: " + bestSolutions[quboIndex]);
			System.out.println();
		}
		
		if (saveResults) {
			InstanceExecutionData session = new InstanceExecutionData(avgPopulations, avgMutations, bestSolutions);
			JSONSerializer serializer = new JSONSerializer().prettyPrint(true); 
		    String jsonString = serializer.deepSerialize(session);
		    System.out.println(jsonString);
		    saveSession("quboExperiment",jsonString);
		}
	    
		plot(avgFitness, QUBO_INSTANCES, "Avg Fitness","#66DD66", "Fitness on all the instances");
		plot(avgPopulations, QUBO_INSTANCES, "Avg Population", "#66DD66", "Populations on all the instances");
		plot(avgMutations, QUBO_INSTANCES, "Avg Mutation","#66DD66", "Mutations on all the instances");
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
				new Experiment("mutation", "population", LOWER_BOUND_MUTATION, UPPER_BOUND_MUTATION, INCREMENT_MUTATION, 2, arguments[0], seed);
		return experimentMutation.Run(problem, arguments);
	}
	
	/*
	 * Search Best population with fixed mutation rate
	 * Vary population from 100, 150, 200, 250... 20000
	 */
	private static ExperimentResult runPopulationExperiment(Problem problem, double[] arguments, long seed) {
		// arguments[2] is fixed mutation
		Experiment experimentPopulation = 
				new Experiment("population", "mutation", LOWER_BOUND_POPULATION, UPPER_BOUND_POPULATION, INCREMENT_POPULATION, 0, arguments[2], seed);
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
			int numInstances,
			String avgLabel,
			String avgColor,
			String title) {
		
		Plot plt = Plot.create();
		
		_plot(plt, avgBestValues, numInstances, avgLabel, avgColor);
		
		plt.legend().loc("upper right");
		plt.title(title);
		try {
			plt.show();
		} catch (IOException | PythonExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private static void plotFile(String file) {
		try {
		  String fileName = file;
	      FileInputStream input = new FileInputStream(fileName);
	      byte[] raw = input.readAllBytes();
	      String content = new String(raw, StandardCharsets.UTF_8);
	      input.close();
	      
	      JSONDeserializer<InstanceExecutionData> deserializer = new JSONDeserializer<InstanceExecutionData>(); 
	      InstanceExecutionData session = deserializer.deserialize(content);
	      
	      plot(session.getGraphPopulation(), QUBO_INSTANCES, "Avg Population", 
					  "#66DD66", "Populations on all the instances");
	      plot(session.getGraphMutation(), QUBO_INSTANCES, "Avg Mutation", 
					 "#66DD66", "Mutations on all the instances");
		} catch (IOException e) {
		      e.printStackTrace();
		}
		catch (Exception e) {
		      e.printStackTrace();
		}
		return;
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
