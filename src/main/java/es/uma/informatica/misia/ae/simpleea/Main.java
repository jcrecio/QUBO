package es.uma.informatica.misia.ae.simpleea;

public class Main {

	private static final int ITERATIONS = 30;
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
		
		int n = Integer.parseInt(args[3]);
		int parameter = Integer.parseInt(args[4]);
		
		// QUBO instances
		for (int quboSeed = 0; quboSeed < INSTANCES; quboSeed++) {
			Problem problem = new Qubo(n, quboSeed);
			double accumulatedMutationRate = 0, standardDeviationMutationRate = 0;
			double accumulatedPopulation = 0, standardDeviationPopulation = 0;
			
			double[] historyMutation = new double[ITERATIONS];
			double[] historyPopulation = new double[ITERATIONS];
			
			// 30 executions for each instance
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
			
			System.out.println("Instance QUBO[" + quboSeed + "] after 30 executions.");
			System.out.println("Average optimal mutation rate = " + avgMutationRate);
			System.out.println("Average optimal population size = " + avgPopulation);
			System.out.println("Standard deviation mutation rate = " + standardDeviationMutationRate);
			System.out.println("Standard deviation population size = " + standardDeviationPopulation);
		}
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
				new Experiment("population", "mutation", 100, 1000, 50, 2, arguments[2], seed);
		return experimentPopulation.Run(problem, arguments);
	}
	
	private static double[] resetArguments(String[] args) {
		double[] arguments = new double[5];
		for(int i = 0; i < 5; i++) {
			if (args.length > i+1) arguments[i] = Double.parseDouble(args[i]);
		}
		return arguments;
	}
}
