package com.marvinmiaowang.ml;

import java.util.ArrayList;

public class SimpleTrainer {

	private int numberOfFeatures;

	private double[] means;
	private double[] variances;
	private int total_count;	
	
	public SimpleTrainer(int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
	}

	public void Train(double[][] instances) {
		this.total_count = instances.length;
		this.means = new double[numberOfFeatures];
		this.variances = new double[numberOfFeatures];
				
		for (double[] instance : instances) {
			for (int i = 0; i < this.numberOfFeatures; i++) {
				this.means[i] += instance[i];
			}
		}

		for (int i = 0; i < this.numberOfFeatures; i++) {
			this.means[i] /= total_count;
		}

		for (double[] instance : instances) {
			for (int i = 0; i < this.numberOfFeatures; i++) {

				this.variances[i] += Math.pow(instance[i] - this.means[i], 2);
			}
		}

		for (int i = 0; i < this.numberOfFeatures; i++) {
			this.variances[i] /= (total_count - 1);
		}
	}

	public double test(double[] features){

		double s = 1.0 * this.counts[labelId] / this.total_count;
		
		for (int i = 0; i < this.numberOfFeatures; i++) {
			s *= Helper.NormalDist(features[i], this.means[labelId][i], this.variances[labelId][i]);
		}
		
		if (s > max){
			maxLabelId = labelId;
			max = s;
		}
	}	
	
}
