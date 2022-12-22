package es.uma.informatica.misia.ae.simpleea;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.sh0nk.matplotlib4j.NumpyUtils;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

public class Main {

	private static final int ITERATIONS = 1;
	private static final int INSTANCES = 10;
	
	public static void main (String args []) {
		
		/**
		 * <parameter>: mutation(0) or population(1): Decides which parameter is optimized first
		 */
		if (args.length < 5) {
			System.err.println("Invalid number of arguments");
			System.err.println("Arguments: <population size> <function evaluations> <bitflip probability> <problem size> <parameter>");
			return;
		}
		
		double[] graphPopulation = new double[INSTANCES], graphMutation = new double[INSTANCES];
		
		int n = Integer.parseInt(args[3]);
		int parameter = Integer.parseInt(args[4]);
		
		// QUBO instances
		for (int quboSeed = 0; quboSeed < INSTANCES; quboSeed++) {
			Qubo problem = new Qubo(n, quboSeed);

			double accumulatedMutationRate = 0, standardDeviationMutationRate = 0;
			double accumulatedPopulation = 0, standardDeviationPopulation = 0;
			
			double[] historyMutation = new double[ITERATIONS];
			double[] historyPopulation = new double[ITERATIONS];
			
			// executions for each instance
			for (int executionSeed = 0; executionSeed < ITERATIONS; executionSeed++) {
				double[] arguments = resetArguments(args);
				double[] results = runExperiment(problem, arguments, executionSeed, parameter);
				
				historyMutation[executionSeed] = results[0];
				historyPopulation[executionSeed] = results[1];
				
				accumulatedMutationRate += results[0];
				accumulatedPopulation += results[1];
			}
			
			double avgMutationRate = accumulatedMutationRate / ITERATIONS;
			double avgPopulation = accumulatedPopulation / ITERATIONS;
			
			for (int i = 0; i < ITERATIONS; i++) {
				standardDeviationMutationRate += Math.pow(historyMutation[i] - avgMutationRate, 2);
				standardDeviationPopulation += Math.pow(historyPopulation[i] - avgPopulation, 2);
			}
			
			standardDeviationMutationRate = Math.sqrt(standardDeviationMutationRate / ITERATIONS);
			standardDeviationPopulation = Math.sqrt(standardDeviationPopulation / ITERATIONS);
					
			System.out.println("Instance QUBO[" + quboSeed + "] after " + ITERATIONS +  " executions.");
			System.out.println("Average optimal mutation rate = " + avgMutationRate);
			System.out.println("Average optimal population size = " + avgPopulation);
			System.out.println("Standard deviation mutation rate = " + standardDeviationMutationRate);
			System.out.println("Standard deviation population size = " + standardDeviationPopulation);
		
			graphPopulation[quboSeed] = avgPopulation;
			graphMutation[quboSeed] = avgMutationRate;
		}
		
		plot(graphMutation, graphPopulation);
	}
	
	private static double[] runExperiment(Problem problem, double[] arguments, long seed, int parameter){

		double bestMutation, bestPopulation;
		if (parameter == 0) {
			bestMutation = runMutationExperiment(problem, arguments, seed);
			arguments[2] = bestMutation;
			bestPopulation = runPopulationExperiment(problem, arguments, seed);
		}
		else {
			bestPopulation = runPopulationExperiment(problem, arguments, seed);
			arguments[0] = bestPopulation;
			bestMutation = runMutationExperiment(problem, arguments, seed);
		}
		
		return new double[] {bestMutation, bestPopulation};
	}
	
	private static double runMutationExperiment(Problem problem, double[] arguments, long seed) {
		// Search Best mutation with fixed population
		// Vary mutation rate from 0.1, 0.2... to 0.9
		Experiment experimentMutation = 
				new Experiment("mutation", "population", 0.1, 0.9, 0.1, 2, arguments[0], seed);
		return experimentMutation.Run(problem, arguments);
	}
	
	private static double runPopulationExperiment(Problem problem, double[] arguments, long seed) {
		// Search Best population with fixed mutation rate
		// Vary population from 100, 150, 200, 250... 20000 
		Experiment experimentPopulation = 
				new Experiment("population", "mutation", 10, 1000, 10, 0, arguments[2], seed);
		return experimentPopulation.Run(problem, arguments);
	}
	
	private static double[] resetArguments(String[] args) {
		double[] arguments = new double[5];
		for(int i = 0; i < 5; i++) {
			if (args.length > i+1) arguments[i] = Double.parseDouble(args[i]);
		}
		return arguments;
	}
	
	private static void plot(double[] mutation, double[] population) {
//		List<Double> x = NumpyUtils.linspace(10, 1000, 990);
//		List<Double> y = x.stream()
//				.map(xi -> mutation[(int) (Math.round(xi)-1)])
//				.collect(Collectors.toList());
//
//		Plot plt = Plot.create();
//		plt.plot().add(x, y, "o").label("Mutation").color("#66DD66");
//		plt.legend().loc("upper right");
//		plt.title("scatter");
//		try {
//			plt.show();
//		} catch (IOException | PythonExecutionException e) {
//			e.printStackTrace();
//		}
//		
		List<Double> x = NumpyUtils.linspace(1, 10, 10);
		List<Double> y = x.stream()
				.map(xi -> mutation[(int) (Math.round(xi)-1)])
				.collect(Collectors.toList());

		Plot plt2 = Plot.create();
		plt2.plot().add(x, y, "o").label("Population").color("#6688FF");
		plt2.legend().loc("upper right");
		plt2.title("scatter");
		try {
			plt2.show();
		} catch (IOException | PythonExecutionException e) {
			e.printStackTrace();
		}
	}
}
