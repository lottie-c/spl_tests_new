package main;

import cz.cuni.mff.spl.SPL;

public class MeasurementLong {

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.LongUniform('-10;10')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortLong"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)"
            })
    public static void longArrayPrimitiveDefault(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.LongUniform('-10;10;arrayPrimitive')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortLong"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)"
            })
    public static void longArrayPrimitive(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.LongUniform('-10;10;array')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortLongArray"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)"
            })
    public static void longArray(String args[]) {

    }

    @SPL(
            generators = {
                    "gen1="
                            + "SPL:spl.LongUniform('-10;10;list')#generate()"
            },
            methods = {
                    "mth="
                            + "THIS:main.Method#sortLongList"
            },
            formula = {
                    "for (i{1} j{10}) mth[gen1](i,j) = mth[gen1](i,j)"
            })
    public static void longList(String args[]) {

    }
}

