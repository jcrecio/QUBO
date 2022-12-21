package es.uma.informatica.misia.ae.simpleea;

import java.util.HashMap;
import java.util.Map;

public class Experiment {
	private static boolean enableLogs = true;

	double lowerBound;
	double upperBound;
	double increment;
	int indexArgToVariate;
	long seed;
	String parameterName;
	String fixedParameter;
	double fixedParameterValue;
	
	Individual bestSolution;
	
	public Experiment(String parameterName, String fixedParameter, double lowerBound, double upperBound, double increment, 
			int indexArgToVariate, double fixedParameterValue, long seed) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.increment = increment;
		this.indexArgToVariate = indexArgToVariate;
		this.seed = seed;
		this.parameterName = parameterName;
		this.fixedParameter = fixedParameter;
		this.fixedParameterValue = fixedParameterValue;
	}
	
	public Individual GetBestSolution() {
		return this.bestSolution;
	}
	
	public double Run(Problem problem, double[] arguments) {
		if (enableLogs) {
			System.out.println();
			System.out.println("****************************************************************");
			System.out.println("Ejecutando experimento para parámetro " + this.parameterName);
			System.out.println("****************************************************************");
			System.out.println();
		}
		
		double bestParameter = 0;
		double bestFitness = 0;
		
		for (double i = this.lowerBound; i < this.upperBound; i += this.increment) {
			arguments[this.indexArgToVariate] = i;
			
			Map<String, Double> parameters = this.mapParameters(arguments, this.seed);
			EvolutionaryAlgorithm evolutionaryAlgorithm = new EvolutionaryAlgorithm(parameters, problem);
			
			Individual localBestSolution = evolutionaryAlgorithm.run();
			
			if (localBestSolution.fitness > bestFitness) {
				bestFitness = localBestSolution.fitness;
				bestParameter = i;
				
				this.bestSolution = localBestSolution;
			}
			
			if (enableLogs) {
				System.out.println("Mejor solución al probar '" + this.parameterName +
						"' con valor = " + i + " y parámetro '" 
						+ this.fixedParameter + "' fijo con valor " + this.fixedParameterValue + ":");
				System.out.println(localBestSolution);
				System.out.println();
				System.out.println("Mejor parámetro encontrado:" + bestParameter);
				System.out.println("Mejor fitness encontrado:" + bestFitness);
				System.out.println();
				System.out.println("----------------------------------------------------------------");
			}
		}
		
		return bestParameter;
	}
	
	private Map<String, Double> mapParameters(double[] args, long randomSeed){
		Map<String, Double> parameters = new HashMap<>();
		parameters.put(EvolutionaryAlgorithm.POPULATION_SIZE_PARAM, args[0]);
		parameters.put(EvolutionaryAlgorithm.MAX_FUNCTION_EVALUATIONS_PARAM, args[1]);
		parameters.put(BitFlipMutation.BIT_FLIP_PROBABILITY_PARAM, args[2]);
		parameters.put(EvolutionaryAlgorithm.RANDOM_SEED_PARAM, (double)randomSeed);
		return parameters;
	}
}