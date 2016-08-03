package cz.cuni.mff.spl.evaluator.graphs;

import java.util.Arrays;
import java.util.ArrayList;

public class EmpiricalDistributionNew{
	public static final int  DEFAULT_BIN_COUNT = 1000;
	private final int binCount; 
	 
	public EmpiricalDistributionNew(int binCount){
		this.binCount = binCount;
	}

	/*public EmpiricalDistributionNew(){
		EmpiricalDistributionNew(DEFAULT_BIN_COUNT);
	}*/

	
	/*public double[][] load(double[] a){

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
			output[i][1] = (i - 1)*binSize + min;
		}


		return output;
	}*/


		public ArrayList<ArrayList<Double>> load(double[] a){

			int n = a.length;

			Arrays.sort(a);
			int min = (int)a[0];
			int max = (int)a[n-1];
			int range = max - min;
			double binSize = range/(double)binCount;
			ArrayList<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();

			Double[] counts = new Double[binCount+1];
			double inc = 1/(double)n;
			
			for (int i = 0; i < counts.length; i++){
				counts[i] = 0D;
			}

			for (int i = 0; i < n; i++){
				counts[(int)((a[i] - min)/binSize)]++;
			}
			


			double current = 0D; 


			ArrayList<Double> firstEntry = new ArrayList<Double>();
			firstEntry.add(0D);
			firstEntry.add(min - binSize);
			output.add(firstEntry);

			for (int i = 0; i < counts.length; i++){
				
				if (counts[i] == 0){
					ArrayList<Double> entry = new ArrayList<Double>();
					entry.add(current);
					entry.add(Double.valueOf(i*binSize + min));
					output.add(entry);
				}else{
					ArrayList<Double> entry = new ArrayList<Double>();
					entry.add(current);
					entry.add(Double.valueOf(i*binSize + min));
					output.add(entry);
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