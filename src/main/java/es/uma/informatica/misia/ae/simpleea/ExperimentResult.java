package es.uma.informatica.misia.ae.simpleea;

public class ExperimentResult {
	double[] bestParameters;
	Individual solution;
	
	public ExperimentResult(double[] bestParameters, Individual solution) {
		this.bestParameters = bestParameters;
		this.solution = solution;
	}
	
	public double[] getBestParameters() {
		return this.bestParameters;
	}
	
	public Individual getSolution() {
		return this.solution;
	}
}
