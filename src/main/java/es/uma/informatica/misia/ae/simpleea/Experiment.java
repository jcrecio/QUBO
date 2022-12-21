package es.uma.informatica.misia.ae.simpleea;

public class Experiment {
	double lowerBound;
	double upperBound;
	double increment;
	int indexArgToVariate;
	long seed;
	
	public Experiment(double lowerBound, double upperBound, double increment,
			int indexArgToVariate, long seed) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.increment = increment;
		this.indexArgToVariate = indexArgToVariate;
		this.seed = seed;
	}
	
	public double getLowerBound() {
		return lowerBound;
	}
	public double getUpperBound() {
		return upperBound;
	}
	public double getIncrement() {
		return increment;
	}
	public int getIndexArgToVariate() {
		return indexArgToVariate;
	}
	public long getSeed() {
		return seed;
	}
}
