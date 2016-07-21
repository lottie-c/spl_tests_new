/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.evaluator.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.jfree.data.xy.XYSeries;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.Run;
import cz.cuni.mff.spl.evaluator.graphs.GraphUtils;
import cz.cuni.mff.spl.utils.ConvertUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * Simple interface to call R scripts.
 * <p>
 * {@code Rscript} executable is expected to be either in PATH environment
 * property or specified in SPL evaluation configuration.
 * 
 * @author Martin Lacina
 * 
 * @see http://www.r-project.org/
 */
public class RProjectCaller {

    /** The logger. */
    private static final SplLog logger            = SplLogger.getLogger(RProjectCaller.class);

    /** The Rsript executable command. */
    private final String        R_runtime_command;

    /** The project R script processor. */
    public static final String  R_RUNTIME_DEFAULT = "Rscript";

    /** The working directory. */
    private final File          workingDirectory;

    /** The r script file. */
    private File                R_SCRIPT_FILE;

    /** The r script x values. */
    private File                R_SCRIPT_XVALUES;

    /** The r script y values file. */
    private File                R_SCRIPT_YVALUES;

    /** The file number. */
    private static volatile int fileNumber        = 0;

    /**
     * Instantiates a new R project caller.
     * 
     * @param rScriptCommand
     *            The Rscript command.
     * @param workingDirectory
     *            The working directory.
     */
    public RProjectCaller(String rScriptCommand, File workingDirectory) {
        this.R_runtime_command = rScriptCommand;
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the density series.
     * 
     * @param data
     *            The data.
     * @param seriesName
     *            The series name.
     * @return The density series.
     */
    public synchronized XYSeries getDensitySeries(double[] data, String seriesName) {
        try {
            int runFileNumber = ++fileNumber;
            R_SCRIPT_FILE = new File(workingDirectory, String.format("spl-rscript-%d.r", runFileNumber));
            R_SCRIPT_XVALUES = new File(workingDirectory, String.format("spl-rscript-x-%d.txt", runFileNumber));
            R_SCRIPT_YVALUES = new File(workingDirectory, String.format("spl-rscript-y-%d.txt", runFileNumber));

            R_SCRIPT_FILE.createNewFile();
            R_SCRIPT_XVALUES.createNewFile();
            R_SCRIPT_YVALUES.createNewFile();
        } catch (IOException e) {
            logger.error(e, "Unable to create temporary file.");
            throw new IllegalStateException("Unable to create temporary file.", e);
        }
        return this.getDensitySeriesImpl(data, seriesName);
    }

    /**
     * Gets the density series.
     * 
     * @param data
     *            The data.
     * @param seriesName
     *            The series name.
     * @return The density series. When error in obtaining series occurs, than
     *         series with no points is returned.
     */
    private XYSeries getDensitySeriesImpl(double[] data, String seriesName) {
        try {
            prepareDensityScriptFile(data);
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            runProcess();
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            return loadResult(seriesName);
        } catch (IOException e) {
            logger.error(e, "Unable to compute density function.");
        } catch (InterruptedException e) {
            logger.error(e, "Unable to compute density function.");
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            // if executoin was not aborted, clear the interrupted flag
            Thread.interrupted();
        }
        return new XYSeries(seriesName);
    }

    /**
     * Run process.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             Thrown when execution was aborted when waiting for Rscript
     *             process to compute density, see
     *             {@link Run#runForInvocation(File, File, File, String, cz.cuni.mff.spl.utils.interactive.InteractiveInterface)}
     *             .
     */
    private void runProcess() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(R_runtime_command, R_SCRIPT_FILE.getAbsolutePath());

        logger.trace("Processing call to RSCRIPT...");
        logger.trace(processBuilder.toString());

        Process p = processBuilder.start();
        // we need to be able to abort execution when invoked remotely
        p.waitFor();

        String line;
        try (
                BufferedReader bri = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(
                        p.getErrorStream()))) {
            while ((line = bri.readLine()) != null) {
                logger.trace("Rscript out: " + line);
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
            while ((line = bre.readLine()) != null) {
                logger.error("Rscript err: " + line);
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }
        logger.trace("Done.");
    }

    /**
     * Load result.
     * 
     * @param seriesName
     *            The series name.
     * @return the xY series
     */
    private XYSeries loadResult(String seriesName) {

        double[] xValues = loadFile(R_SCRIPT_XVALUES);

        double[] yValues = loadFile(R_SCRIPT_YVALUES);

        return GraphUtils.createSeriesOfPoints(xValues, yValues, seriesName);
    }

    /**
     * Load file.
     * 
     * @param file
     *            The filename.
     * @return the double[]
     */
    private static double[] loadFile(File file) {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            StringTokenizer tokenizer;

            List<Double> data = new LinkedList<>();

            while ((line = reader.readLine()) != null) {
                tokenizer = new StringTokenizer(line, " ");
                while (tokenizer.hasMoreElements()) {
                    String nextToken = tokenizer.nextToken();
                    data.add(Double.parseDouble(nextToken));
                }
            }
            return ConvertUtils.convertDoublesToArray(data, new double[data.size()]);
        } catch (IOException | NumberFormatException e) {
            logger.error("Unable to read measured density function data from file [%s]", file.getPath());
        }

        return new double[0];
    }

    /**
     * Prepares density script file.
     * 
     * @param data
     *            The data.
     */
    private void prepareDensityScriptFile(double[] data) {

        if (data.length < 2) {
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(R_SCRIPT_FILE));

            writer.println("# SPL Evaluator generated temporary file");

            writer.println("density.adjust <- 1");

            writer.println("data <- as.double(c(");

            for (int i = 0; i < data.length; ++i) {
                if (i != 0) {
                    writer.print(',');
                }
                writer.print(data[i]);
            }

            writer.println();
            writer.println("))");

            writer.println("dst <- density(data, adjust=density.adjust)");

            writer.printf("write(dst$x, file=\"%s\")\n",
                    R_SCRIPT_XVALUES.getAbsolutePath().replace("\\", "\\\\"));
            writer.printf("write(dst$y, file=\"%s\")\n",
                    R_SCRIPT_YVALUES.getAbsolutePath().replace("\\", "\\\\"));

            writer.close();

        } catch (IOException e) {
            logger.error(e, "Unable to prepare density function R script to file [%s].", R_SCRIPT_FILE);
            e.printStackTrace();
        }

    }
}
