package cz.cuni.mff.spl.evaluator.graphs;

import java.util.Arrays;
import java.util.ArrayList;

public class EmpiricalDistribution{
	public static final int  DEFAULT_BIN_COUNT = 200;
	private int binCount; 
	 
	public EmpiricalDistribution(int binCount){
		this.binCount = binCount;
	}

	public EmpiricalDistribution(){
		this(DEFAULT_BIN_COUNT);
	}

	/**
	* Function takes a double array of data and outputs an nx2 arrayList
	* containing the y and x coordinates [y,x] of the input's empirical 
	* distribution. 
	*/
	public ArrayList<ArrayList<Double>> load(double[] a){

		int n = a.length;
		Arrays.sort(a);
		int min = (int)a[0];
		int max = (int)a[n-1];
		int range = max - min;
		if (n < 100){
			binCount = range;
		}
		double binSize = range/(double)binCount;
		ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();

		Double[] counts = new Double[binCount+1];
		double inc = 1/(double)n;
		for (int i = 0; i < counts.length; i++){
			counts[i] = 0D;
		}
		//for each data point, add one to the appropriate bin
		for (int i = 0; i < n; i++){
			counts[(int)((a[i] - min)/binSize)]++;
		}
		
		double current = 0D; 
		// the EDF must start a 0 on the x axis before the first data point
		ArrayList<Double> firstEntry = new ArrayList<Double>();
		firstEntry.add(0D);
		firstEntry.add(min - binSize);
		output.add(firstEntry);

		//for each bin increase the step by 1/n * (number of datapoints in bin)
		// need to add the point before and after the step to maintain the step 
		//function, y coordinates - output(i,0), x coordinates - output(i,1)
		for (int i = 0; i < counts.length; i++){
			//if no data points in bin, line is flat, no step
			if (counts[i] == 0){
				ArrayList<Double> entry = new ArrayList<Double>();
				entry.add(current);
				entry.add(Double.valueOf(i*binSize + min));
				output.add(entry);
			}else{
				// add a plot point before the step
				ArrayList<Double> entry = new ArrayList<Double>();
				entry.add(current);
				entry.add(Double.valueOf(i*binSize + min));
				output.add(entry);
				// add a plot point after the step
				current += counts[i]*inc;
				ArrayList<Double> entry2 = new ArrayList<Double>();
				entry2.add(current);
				entry2.add(Double.valueOf(i*binSize + min));
				output.add(entry2);

			}

		}

		return output;
	}



}