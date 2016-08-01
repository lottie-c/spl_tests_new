package cz.cuni.mff.spl.evaluator.graphs;

import java.util.Arrays;

public class EmpiricalDistribution{
	int DEFAULT_BIN_SIZE = 1;
	int binSize = DEFAULT_BIN_SIZE; 
	 

	public EmpiricalDistribution(){

	}


	public double[] load(double[] a){

		int n = a.length;


		Arrays.sort(a);
		int min = (int)a[0];
		int max = (int)a[n-1];
		double[] output = new double[(max - min + 1)/binSize + 1];
		double inc = 1/(double)n;

		output[0] = 0;
		
		for (int i = 0; i < n; i++){
			output[(((int)a[i] - min)/binSize) + 1]++;
		}

		double current = 0; 
		for (int i = 0; i < output.length; i++){
			if(output[i] == 0){
				output[i] = current;
			}else{
				current += output[i]*inc;
				output[i] = current;
				
			}
		}
		return output;
	}

}