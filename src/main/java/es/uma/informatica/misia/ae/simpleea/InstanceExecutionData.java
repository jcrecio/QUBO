package es.uma.informatica.misia.ae.simpleea;

public class InstanceExecutionData {
	double[] graphPopulation;
	double[] graphMutation;
	Individual[] solutions;
	double[] lowerBestPopulation;
	double[] lowerBestMutation;
	
	public InstanceExecutionData() {
		
	}
	public InstanceExecutionData(double[] graphPopulation, double[] graphMutation, 
			Individual[] solutions) {
		this.graphMutation = graphMutation;
		this.graphPopulation = graphPopulation;
		this.solutions = solutions;
	}
	
	public double[] getGraphPopulation() {
		return this.graphPopulation;
	}
	public void setGraphPopulation(double[] graphPopulation) {
		this.graphPopulation = graphPopulation;
	}
	public double[] getGraphMutation() {
		return this.graphMutation;
	}
	public void setGraphMutation(double[] graphMutation) {
		this.graphMutation = graphMutation;
	}
	public Individual[] getSolutions() {
		return this.solutions;
	}
	public void setGraphMutation(Individual[] solutions) {
		this.solutions = solutions;
	}
}
