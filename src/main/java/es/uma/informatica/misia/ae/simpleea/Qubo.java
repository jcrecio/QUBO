package es.uma.informatica.misia.ae.simpleea;

import java.util.Random;

public class Qubo implements Problem {

	double matrix[][];
	int n;
	
	public Qubo(int n, int seed) {
		this.n = n;
		this.matrix = new double[n][n];
		
		Random random = new Random(seed);
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				this.matrix[i][j] = random.nextDouble();
			}
		}
	}
	
	@Override
	public double evaluate(Individual individual) {
		BinaryString binaryString = (BinaryString)individual;
		byte[] chromosome = binaryString.getChromosome();
		
		double fitness = 0;
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {				
				fitness += matrix[i][j] * chromosome[i] * chromosome[j];
			}
		}
		return fitness;
	}

	@Override
	public Individual generateRandomIndividual(Random rnd) {
		return new BinaryString(n,rnd);
	}
	
	public void print() {
		for(int i = 0; i < this.n; i++) {
			for(int j = 0; j < this.n; j++) {
				System.out.print(this.matrix[i][j] + " ");
			}
			System.out.println();
		}
	}

}
