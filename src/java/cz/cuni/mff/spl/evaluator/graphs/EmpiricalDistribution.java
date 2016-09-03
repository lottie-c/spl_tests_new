package cz.cuni.mff.spl.evaluator.graphs;

import java.util.Arrays;
import java.util.ArrayList;

public class EmpiricalDistribution{

	public EmpiricalDistribution(){
	}

	
	public ArrayList<ArrayList<Double>> load(double[] a){

		if ((a == null)||(a.length == 0)){
			ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();
			ArrayList<Double> firstEntry = new ArrayList<Double>();
			firstEntry.add(0D);
			firstEntry.add(0D);
			output.add(firstEntry);
			return output;
		}

		int n = a.length;
		Arrays.sort(a);
		int min = (int)a[0];
		int max = (int)a[n-1];
		
		ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();


		double inc = 1/(double)n;
		
		double current = 0D; 
		// the EDF must start a 0 on the x axis before the first data point
		ArrayList<Double> firstEntry = new ArrayList<Double>();
		firstEntry.add(0D);
		firstEntry.add(Double.valueOf(min) - 1);
		output.add(firstEntry);

		
		//for each data point, increase the step by 1/n
		for (int i = 0; i < a.length - 1; i++){
			//if no data points in bin, line is flat, no step
			if(a[i+1] == a[i]){
				current += inc;
			}else{
				current += inc;
				ArrayList<Double> entry = new ArrayList<Double>();
				entry.add(current);
				entry.add(Double.valueOf(a[i]));
				output.add(entry);
			}

			if(i == a.length - 2){
				current += inc;
				ArrayList<Double> entry = new ArrayList<Double>();
				entry.add(current);
				entry.add(Double.valueOf(a[i+1]));
				output.add(entry);
			}
		}

		return output;
	}



}