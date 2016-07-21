/**
 * 
 */
package cz.cuni.mff.spl.utils;

import java.util.Iterator;
import java.util.List;

/**
 * @author Martin Lacina
 * 
 */
public class ConvertUtils {

    /**
     * Converts doubles to array.
     * <p>
     * Data are stored to provided buffer it its length fits, if it does not
     * fit, than new array is allocated with matching length.
     * 
     * @param doubles
     *            The list with doubles.
     * @param buffer
     *            The buffer to be used for data, it its length fits.
     * @return the double[]
     */
    public static double[] convertDoublesToArray(List<Double> doubles, double[] buffer) {
        double[] result;
        if (buffer.length == doubles.size()) {
            result = buffer;
        } else {
            result = new double[doubles.size()];
        }
        Iterator<Double> iterator = doubles.iterator();
        for (int i = 0; i < result.length; i++) {
            result[i] = iterator.next().doubleValue();
        }
        return result;
    }

}
