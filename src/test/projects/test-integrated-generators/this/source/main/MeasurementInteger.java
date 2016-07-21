package main;

import cz.cuni.mff.spl.SPL;

public class MeasurementInteger {

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.IntegerPermutation#generate()",
                    "gen2="
                            + "SPL:spl.IntegerUniform('-10;10')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:"
                            + "main."
                            + "Method#sortInteger"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)"
            })
    public static void integerArrayPrimitiveDefault(String args[]) {

    }

    @SPL(
           generators = {
                    "gen1="
                            + "SPL:spl.IntegerPermutation('arrayPrimitive')#generate()",
                    "gen2="
                            + "SPL:spl.IntegerUniform('-10;10;arrayPrimitive')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortInteger"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)"
            })
    public static void integerArrayPrimitive(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.IntegerPermutation('array')#generate()",
                    "gen2="
                            + "SPL:spl.IntegerUniform('-10;10;array')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortIntegerArray"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)"
            })
    public static void integerArray(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.IntegerPermutation('list')#generate()",
                    "gen2="
                            + "SPL:spl.IntegerUniform('-10;10;list')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortIntegerList"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)"
            })
    public static void integerList(String args[]) {

    }

    @SPL(
            generators = {
                    "gen=SPL:spl.IntegerUniform('10;50')#generate()"
            },
            methods = {
                    "mth=THIS:main.Method#sortInteger"
            },
            formula = {
                    "mth[gen](10,10) <= (1, 1.5)  java.util.Arrays#sort(int[])[gen](10,10)"
            })
    public static void integerTest(String args[]) {

    }
}

