package es.uma.informatica.misia.ae.simpleea;

import java.util.HashMap;
import java.util.Map;

public class Main {

	private static boolean enableLogs = true;
	
	public static void main (String args []) {
		
		if (args.length < 4) {
			System.err.println("Invalid number of arguments");
			System.err.println("Arguments: <population size> <function evaluations> <bitflip probability> <problem size> [<random seed>]");
			return;
		}
		
		int n = Integer.parseInt(args[3]);
		long randomSeed = System.currentTimeMillis();
		if (args.length > 4) {
			randomSeed = Long.parseLong(args[4]);
		}
		
		Problem problem = new Qubo(n, randomSeed);
		double[] arguments = new double[5];
		for(int i = 0; i<5; i++) {
			if (args.length > i+1) arguments[i] = Double.parseDouble(args[i]);
		}
		
		// Obtain the best mutation rate based on a initial fixed population
		// Vary mutation rate from 0.1, 0.2... to 0.9
		Experiment experimentMutation = new Experiment(0.1, 0.9, 0.1, 2, randomSeed);
		double resultExperimentMutation = RunExperiment(experimentMutation, problem, arguments);
		arguments[2] = resultExperimentMutation;
		
		// Obtain the best population based on the previous calculated mutation rate
		// Vary population from 100, 150, 200, 250... 20000 
		Experiment experimentPopulation = new Experiment(100, 1000, 50, 2, randomSeed);
		double resultexperimentPopulation = RunExperiment(experimentMutation, problem, arguments);
	}


	private static double RunExperiment(Experiment experiment, Problem problem, double[] arguments) {
		double bestParameter = 0;
		Individual bestCandidate = null;
		double bestFitness = 0;
		
		double lowerBound = experiment.getLowerBound();
		double upperBound = experiment.getUpperBound();
		double increment = experiment.getIncrement();
		int indexToVariate = experiment.getIndexArgToVariate();
		
		for (double i = lowerBound; i < upperBound; i += increment) {
			arguments[indexToVariate] = i;
			
			Map<String, Double> parameters = mapEAParameters(arguments, experiment.getSeed());
			EvolutionaryAlgorithm evolutionaryAlgorithm = new EvolutionaryAlgorithm(parameters, problem);
			
			Individual localBestSolution = evolutionaryAlgorithm.run();
			
			if (localBestSolution.fitness > bestFitness) {
				bestFitness = localBestSolution.fitness;
				bestCandidate = localBestSolution;
				bestParameter = i;
			}
			
			if (enableLogs) {
				System.out.print("Mejor solución al mutar arg[" + indexToVariate + "] con " +i);
				System.out.println(localBestSolution);
				
				System.out.println("Mejor parámetro encontrado:" + bestParameter);
				System.out.println("Mejor fitness encontrado:" + bestFitness);
				System.out.println();
				System.out.println();
			}
			
		}
		
		return bestParameter;
		
	}
	
	private static Map<String, Double> mapEAParameters(double[] args, long randomSeed){
		Map<String, Double> parameters = new HashMap<>();
		parameters.put(EvolutionaryAlgorithm.POPULATION_SIZE_PARAM, args[0]);
		parameters.put(EvolutionaryAlgorithm.MAX_FUNCTION_EVALUATIONS_PARAM, args[1]);
		parameters.put(BitFlipMutation.BIT_FLIP_PROBABILITY_PARAM, args[2]);
		parameters.put(EvolutionaryAlgorithm.RANDOM_SEED_PARAM, (double)randomSeed);
		return parameters;
	}
}
