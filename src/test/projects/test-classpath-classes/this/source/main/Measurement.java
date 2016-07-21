package main;

import cz.cuni.mff.spl.SPL;

public class Measurement {

    @SPL(
            generators = {
                    "generator="
                            + "THIS@HEAD:"
                            + "main."
                            + "Generator()"
            },
            methods = {
                    "sort1="
                            + "THIS@HEAD:"
                            + "main."
                            + "Method#sort1",
                    "sort2="
                            + "THIS@HEAD:"
                            + "main."
                            + "Method#sort2"
            },
            formula = {
                    "sort1[generator] = sort2[generator]"
            })
    
    public static void main(String args[]) {

    }
}
