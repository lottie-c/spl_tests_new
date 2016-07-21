package main;

import cz.cuni.mff.spl.SPL;

public class MeasurementDouble {

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.DoubleExponential('10.0')#generate()",
                    "gen2="
                            + "SPL:spl.DoubleGaussian('10.0;10.0')#generate()",
                    "gen3="
                            + "SPL:spl.DoubleUniform('-10.0;10.0')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortDouble"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)",
                    "for (i{1} j{10}) mth[gen3](i,j) = mth[gen3](i,j)"
            })
    public static void doubleArrayPrimitiveDefault(String args[]) {

    }
    

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.DoubleExponential('10.0;arrayPrimitive')#generate()",
                    "gen2="
                            + "SPL:spl.DoubleGaussian('10.0;10.0;arrayPrimitive')#generate()",
                    "gen3="
                            + "SPL:spl.DoubleUniform('-10.0;10.0;arrayPrimitive')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortDouble"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)",
                    "for (i{1} j{10}) mth[gen3](i,j) = mth[gen3](i,j)"
            })
    public static void doubleArrayPrimitive(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.DoubleExponential('10.0;array')#generate()",
                    "gen2="
                            + "SPL:spl.DoubleGaussian('10.0;10.0;array')#generate()",
                    "gen3="
                            + "SPL:spl.DoubleUniform('-10.0;10.0;array')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortDoubleArray"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)",
                    "for (i{1} j{10}) mth[gen3](i,j) = mth[gen3](i,j)"
            })
    public static void doubleArray(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.DoubleExponential('10.0;list')#generate()",
                    "gen2="
                            + "SPL:spl.DoubleGaussian('10.0;10.0;list')#generate()",
                    "gen3="
                            + "SPL:spl.DoubleUniform('-10.0;10.0;list')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortDoubleList"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)",
                    "for (i{1} j{10}) mth[gen2](i,j) = mth[gen2](i,j)",
                    "for (i{1} j{10}) mth[gen3](i,j) = mth[gen3](i,j)"
            })
    public static void doubleList(String args[]) {

    }
}

