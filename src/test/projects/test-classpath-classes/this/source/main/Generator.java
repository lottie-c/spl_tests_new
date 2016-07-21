package main;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("serial")
public class Generator extends ArrayList<Object[]> {

    public Generator() {
        generate();
    }

    public void generate() {
        int n = 10;	
        Random generator = new Random(0);

        for (int i = 0; i < 10; i++) {
            int[] data = new int[n];
            for (int j = 0; j < n; j++) {
                data[j] = generator.nextInt(n * 2);
            }
            Object[] arg = new Object[1];
            arg[0] = data;
            this.add(arg);
        }
    }
}
