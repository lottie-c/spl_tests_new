package main;

import java.util.ArrayList;
import java.util.Random;

public class Generator {    
    Random rnd = new Random();
    int calls;
    int args;
    
    public Generator(String calls_args) throws Exception {        
        System.out.println("Generator()");      
        String split[] = calls_args.split(";");        
        calls = Integer.valueOf(split[0]); 
        args  = Integer.valueOf(split[1]); 
    }
    
    public ArrayList<Object[]> generate() {
        System.out.println("generate()");

        ArrayList<Object[]> allCalls = new ArrayList<>();
        
        for (int i = 0; i < calls; i++) {
            
            int[] argument = new int[args];
            for (int j = 0; j < args; j++) {
                argument[i] = rnd.nextInt();
            }
            
            Object[] callArguments = new Object[] {argument};
            allCalls.add(callArguments);
        }
        
        return allCalls;
    }
}
