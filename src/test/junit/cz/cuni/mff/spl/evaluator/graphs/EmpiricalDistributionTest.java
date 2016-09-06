package cz.cuni.mff.spl.evaluator.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EmpiricalDistributionTest{

	private double[] input = {1,2,3,4};

	private ArrayList<ArrayList<Double>> output;

	private ArrayList<ArrayList<Double>>  empty;

	@Before
	public void init(){
		ArrayList<ArrayList<Double>> extra = new ArrayList<ArrayList<Double>>();

		ArrayList<Double> entry1 = new ArrayList<Double>();
		entry1.add(0.0);
		entry1.add(0.0);
		extra.add(entry1);


		ArrayList<Double> entry2 = new ArrayList<Double>();
		entry2.add(0.25);
		entry2.add(1.0);
		extra.add(entry2);

		ArrayList<Double> entry3 = new ArrayList<Double>();
		entry3.add(0.5);
		entry3.add(2.0);
		extra.add(entry3);

		ArrayList<Double> entry4 = new ArrayList<Double>();
		entry4.add(0.75);
		entry4.add(3.0);
		extra.add(entry4);

		ArrayList<Double> entry5 = new ArrayList<Double>();
		entry5.add(1.0);
		entry5.add(4.0);
		extra.add(entry5);


		output = extra;

		ArrayList<ArrayList<Double>> extra2 = new ArrayList<ArrayList<Double>>();
		extra2.add(entry1);

		empty = extra2;

	}

	/*Test the load function for EmpricalDistribution*/
	@Test
	public void testLoad(){
		EmpiricalDistribution dist = new EmpiricalDistribution();
		
		double[] a = null;

		assertEquals(output, dist.load(input));
		assertEquals("null input array should output (0,0)", empty, dist.load(a));

	}
}
