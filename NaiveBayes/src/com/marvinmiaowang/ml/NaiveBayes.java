package com.marvinmiaowang.ml;

import java.util.ArrayList;

public class NaiveBayes {

	private int numberOfFeatures;

	private double[][] means;
	private double[][] variances;
	private int[] counts;
	private int total_count;	
	
	private ArrayList<String> labelTypes;

	public NaiveBayes(int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
		this.labelTypes = new ArrayList<String>();
	}

	public void Train(Instance[] instances) {
		for (Instance instance : instances) {
			if (!labelTypes.contains(instance.getLabel())) {
				labelTypes.add(instance.getLabel());
			}
		}

		this.total_count = instances.length;
		this.means = new double[labelTypes.size()][numberOfFeatures];
		this.variances = new double[labelTypes.size()][numberOfFeatures];
		this.counts = new int[labelTypes.size()];
				
		for (Instance instance : instances) {
			int labelId = labelTypes.indexOf(instance.getLabel());
			counts[labelId]++;

			for (int i = 0; i < this.numberOfFeatures; i++) {
				this.means[labelId][i] += instance.getFeatures()[i];
			}
		}

		for (int labelId = 0; labelId < labelTypes.size(); labelId++) {
			for (int i = 0; i < this.numberOfFeatures; i++) {
				this.means[labelId][i] /= counts[labelId];
			}
		}

		for (Instance instance : instances) {
			int labelId = labelTypes.indexOf(instance.getLabel());

			for (int i = 0; i < this.numberOfFeatures; i++) {

				this.variances[labelId][i] += Math.pow(
						instance.getFeatures()[i] - this.means[labelId][i], 2);
			}
		}

		for (int labelId = 0; labelId < labelTypes.size(); labelId++) {
			for (int i = 0; i < this.numberOfFeatures; i++) {
				this.variances[labelId][i] /= (counts[labelId] - 1);
			}
		}
	}

	public String classify(double[] features){
		
		double max = 0.0;
		int maxLabelId = 0;
		
		for(int labelId = 0; labelId < labelTypes.size(); labelId++){
			double s = 1.0 * this.counts[labelId] / this.total_count;
			
			for (int i = 0; i < this.numberOfFeatures; i++) {
				s *= Helper.NormalDist(features[i], this.means[labelId][i], this.variances[labelId][i]);
			}
			
			if (s > max){
				maxLabelId = labelId;
				max = s;
			}
		}
		
		return this.labelTypes.get(maxLabelId);
	}
}
