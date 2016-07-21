package main;

import cz.cuni.mff.spl.SPL;

public class Measurement {

    @SPL(
            generators = {
                    "generator=main.Generator('30;1000')#generate()"
            },
            methods = {
                    "sort1=main.Method#sort1",
                    "sort2=main.Method#sort2"
            },
            formula = {
                    "sort1[generator] < sort2[generator]"
            })    
    void compareSorts() {}
}
