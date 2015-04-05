package com.marvinmiaowang.ml;

public class Instance {
	private String label;
	private double[] features;

	public Instance(String label, double[] features) {
		this.label = label;
		this.features = features;
	}

	public String getLabel() {
		return label;
	}

	public double[] getFeatures() {
		return features;
	}
}
