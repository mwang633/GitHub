package com.marvinmiaowang.ml;

import java.util.ArrayList;

/**
 *  A simple Native Bayes single label trainer.
 */
public class SimpleTrainer {
	private int numberOfFeatures;

	private double[] means;
	private double[] variances;

    public SimpleTrainer(int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
	}

	public void train(double[][] instances) {
        int total_count = instances.length;
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

	public double test(double[] features) {

        double s;
        s = 1.0;

        for (int i = 0; i < this.numberOfFeatures; i++) {
            s *= MathUtil.NormalDist(features[i], this.means[i], this.variances[i]);
        }

        return s;
    }
}
