package cz.cuni.mff.spl.evaluator.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;


public class KSFlagTest{
	// length = 100, samples taken from a N(0,1) distribution using R
	private double[] x = {	0.9843459, -1.958506, -0.1800928, -2.730587, -0.4714388,
						0.5110352, 0.6103448, -0.3367046, -0.7985175, -0.06690821,
						0.047275, 1.748009, -1.296815, -0.583574, 0.4612437,
						0.8849676, 0.6539612, 0.4322807, -1.694291, -0.8639357,
						1.150202, -0.656564, 0.3563814, 0.06188491, -0.9006197,
						-1.827616, -1.963149, 0.1755631, -0.5924122, -0.8034386,
						-0.9578047, -0.3785191, -0.5104308, 2.695843, -0.1072021,
						-0.4953495, -1.519183, 0.3850391, -0.07993733, 0.5207623,
						0.7449531, -0.2691441, -0.2283111, 0.9008536, -1.687588,
						0.7003996, 1.09112, 2.009403, -0.6134253, 0.1681507,
						-0.8558765, -0.7882558, -0.5936598, 0.1161674, -0.7525728,
						2.11983, 1.504454, -0.4420895, -0.3551397, 0.1549474,
						0.4008008, -1.475788, 0.1528629, 0.9642619, 2.224475,
						-0.488233, -0.1134522, 1.041413, -0.9720026, -0.09203689,
						1.687021, -0.3523008, 0.3214575, -0.06735462, 0.927691,
						0.456387, 0.607188, 0.1507881, -0.9789458, -1.355592,
						0.2163546, -0.8339699, -1.090991, -0.1615419, 0.6474461,
						-0.2566488, -1.817782, -1.050995, -1.519477, -0.6434857,
						-0.4184541, -0.05796555, 0.3514677, 0.8135559, -0.713275,
						-0.2701461, -0.686365, 0.8174561, 1.355658, 0.007721954};
	// length = 100, samples taken from a N(3,1) distribution using R
	private double[] y = {	1.794472, 2.983179, 4.514507, 2.991349, 3.032173,
						3.421548, 4.381188, 2.808052, 2.515681, 3.449358,
						3.157277, 3.444509, 1.920292, 3.116253, 2.738057,
						4.094811, 3.427907, 2.4446, 3.757353, 4.046024,
						3.873948, 1.945214, 2.13423, 2.125992, 2.986424,
						2.052631, 3.701966, 4.96011, 2.34393, 3.527641,
						3.032931, 1.532799, 3.847601, 4.431537, 2.213131,
						3.151736, 3.86103, 1.11692, 2.407346, 2.340258,
						3.300329, 3.235954, 2.83713, 3.366517, 2.399033,
						1.757898, 2.50433, 2.388816, 4.226333, 4.196222,
						3.78679, 1.562194, 3.128802, 2.809441, 4.778584,
						3.003491, 4.701443, 3.447213, 3.416038, 0.8522436,
						2.858666, 3.492934, 4.09653, 3.932457, 2.193282,
						1.958251, 5.496294, 4.358864, 4.149759, 2.34925,
						1.649902, 3.985437, 2.132602, 0.5103519, 1.312693,
						4.047, 2.46409, 2.661703, 3.22195, 4.697262,
						2.089505, 2.623525, 0.6697467, 3.044724, 2.389106,
						2.709946, 3.444982, 1.283212, 4.242514, 2.657452,
						2.776125, 4.976497, 2.931005, 2.642703, 5.199425,
						2.064087, 2.842258, 3.243002, 3.976528, 3.61147};

	
	/*want to test
		same dist gives  p-value 1
		KSFlag gives same p_value as ks test*/
	
	@Test
	public void testKSFlag(){	
		KolmogorovSmirnovTestFlag TEST_WITH_FLAG = new KolmogorovSmirnovTestFlag();
		KolmogorovSmirnovTest TEST = new KolmogorovSmirnovTest();
		double flagPValue = TEST_WITH_FLAG.kolmogorovSmirnovTestFlag(x,y);
		double pValue = TEST.kolmogorovSmirnovTest(x,y);
		assertEquals("KS test with flag should return same as normal KS test",flagPValue, pValue, 0.00001);
	}

	@Test
	public void testSameInput(){
		KolmogorovSmirnovTestFlag TEST_WITH_FLAG = new KolmogorovSmirnovTestFlag();
		double pValue = TEST_WITH_FLAG.kolmogorovSmirnovTestFlag(x,x);
		assertEquals("When comparing the same sample, should return 1", 1.0, pValue, 0.00001);
	}

	@Test
	public void testDifferentInput(){
		KolmogorovSmirnovTestFlag TEST_WITH_FLAG = new KolmogorovSmirnovTestFlag();
		double pValue = TEST_WITH_FLAG.kolmogorovSmirnovTestFlag(x,y);
		assertFalse(pValue == 1);
	}

	@Test 
	public void testNegFlagSet(){
		KolmogorovSmirnovTestFlag TEST_WITH_FLAG = new KolmogorovSmirnovTestFlag();
		double pValue = TEST_WITH_FLAG.kolmogorovSmirnovTestFlag(y,x);
		int negFlag = TEST_WITH_FLAG.getNegFlag();
		assertEquals("NegFlag should be set to 1 when L > R", 1, negFlag);
	}

	@Test 
	public void testNegFlagUnset(){
		KolmogorovSmirnovTestFlag TEST_WITH_FLAG = new KolmogorovSmirnovTestFlag();
		double pValue = TEST_WITH_FLAG.kolmogorovSmirnovTestFlag(x,y);
		int negFlag = TEST_WITH_FLAG.getNegFlag();
		assertEquals("NegFlag should be set to 0 when L < R", 0, negFlag);
	}


}