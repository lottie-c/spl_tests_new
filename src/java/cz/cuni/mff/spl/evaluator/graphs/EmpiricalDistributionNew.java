package cz.cuni.mff.spl.evaluator.graphs;

import java.util.Arrays;

public class EmpiricalDistributionNew{
	public static final int  DEFAULT_BIN_COUNT = 1000;
	private final int binCount; 
	 
	public EmpiricalDistributionNew(int binCount){
		this.binCount = binCount;
	}

	/*public EmpiricalDistributionNew(){
		EmpiricalDistributionNew(DEFAULT_BIN_COUNT);
	}*/

	
	public double[][] load(double[] a){

		int n = a.length;

		Arrays.sort(a);
		int min = (int)a[0];
		int max = (int)a[n-1];
		int range = max - min;
		int binSize = range/binCount;
		double[][] output = new double[binCount + 3][2];
		double inc = 1/(double)n;

		output[0][0] = 0;
		
		for (int i = 0; i < n; i++){
			output[(((int)a[i] - min)/binSize) + 1][0]++;
		}

		double current = 0; 

		for (int i = 0; i < output.length; i++){
			if(output[i][0] == 0){
				output[i][0] = current;
			}else{
				current += output[i][0]*inc;
				output[i][0] = current;
				
			}
			output[i][1] = (i - 1)*binSize;
		}


		return output;
	}

}