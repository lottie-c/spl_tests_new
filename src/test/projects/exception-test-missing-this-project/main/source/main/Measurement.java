package main;

import cz.cuni.mff.spl.SPL;

public class Measurement {

    @SPL(
            generators = {
                    "generator="
                            + "main@HEAD:"
                            + "main."
                            + "Generator()"
            },
            methods = {
                    "sort1="
                            + "main@HEAD:"
                            + "main."
                            + "Method#sort1",
                    "sort2="
                            + "main@HEAD:"
                            + "main."
                            + "Method#sort2"
            },
            formula = {
                    "sort1[generator] = sort2[generator]"
            })
    
    public static void main(String args[]) {

    }
}
