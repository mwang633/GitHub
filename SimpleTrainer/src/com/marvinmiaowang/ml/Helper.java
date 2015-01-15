package com.marvinmiaowang.ml;

public class Helper {

	public static double NormalDist(double x, double mean, double variance) {
		double fact = Math.sqrt(2.0 * Math.PI * variance);
		double expo = (x - mean) * (x - mean) / (2.0 * variance);
		return Math.exp(-expo) / fact;
	}
}
