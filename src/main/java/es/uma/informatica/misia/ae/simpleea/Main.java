package es.uma.informatica.misia.ae.simpleea;

public class Main {

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
		
		
		// 10 instancias de QUBO
		for (int seed = 0; seed < 10; seed++) {
			Problem problem = new Qubo(n, seed);
			double[] arguments = resetArguments(args);
			double[] results = runExperiment(problem, arguments, seed);
		}
	}
	
	private static double[] runExperiment(Problem problem, double[] arguments, long seed){
		// Obtain the best mutation rate based on a initial fixed population
		// Vary mutation rate from 0.1, 0.2... to 0.9
		Experiment experimentMutation = 
				new Experiment("mutación", "población", 0.1, 0.9, 0.1, 2, arguments[0], seed);
		double bestMutation = experimentMutation.Run(problem, arguments);
		arguments[2] = bestMutation;
		
		// Obtain the best population based on the previous calculated mutation rate
		// Vary population from 100, 150, 200, 250... 20000 
		Experiment experimentPopulation = 
				new Experiment("población", "mutación", 100, 1000, 50, 2, arguments[2], seed);
		double bestPopulation = experimentPopulation.Run(problem, arguments);
		
		return new double[] {bestMutation,bestPopulation};
	}
	
	private static double[] resetArguments(String[] args) {
		double[] arguments = new double[5];
		for(int i = 0; i < 5; i++) {
			if (args.length > i+1) arguments[i] = Double.parseDouble(args[i]);
		}
		return arguments;
	}
}
