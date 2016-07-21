package main;

public class Method {

    public static void sortDouble(double[] a) {
        java.util.Arrays.sort(a);
    }

    public static void sortDoubleArray(Double[] a) {
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        java.util.Arrays.sort(b);
    }

    public static void sortDoubleList(java.util.List<Double> a) {
        double[] b = new double[a.size()];
        for (int i = 0; i < a.size(); i++) {
            b[i] = a.get(i);
        }
        java.util.Arrays.sort(b);
    }

    public static void sortInteger(int[] a) {
        java.util.Arrays.sort(a);
    }

    public static void sortIntegerArray(Integer[] a) {
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        java.util.Arrays.sort(b);
    }

    public static void sortIntegerList(java.util.List<Integer> a) {
        int[] b = new int[a.size()];
        for (int i = 0; i < a.size(); i++) {
            b[i] = a.get(i);
        }
        java.util.Arrays.sort(b);
    }

    public static void sortLong(long[] a) {
        java.util.Arrays.sort(a);
    }

    public static void sortLongArray(Long[] a) {
        long[] b = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        java.util.Arrays.sort(b);
    }

    public static void sortLongList(java.util.List<Long> a) {
        long[] b = new long[a.size()];
        for (int i = 0; i < a.size(); i++) {
            b[i] = a.get(i);
        }
        java.util.Arrays.sort(b);
    }
}
