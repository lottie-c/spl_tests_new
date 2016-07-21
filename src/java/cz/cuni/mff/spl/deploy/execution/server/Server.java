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
package cz.cuni.mff.spl.deploy.execution.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.ZipUtils;

/**
 * This class executes a batch of measurements one by one.
 * 
 * Server keeps a directory structure like this:
 * 
 * base directory
 * - server directory (server id)
 * - - job #1 directory (job id)
 * - - job #2 directory (job id)
 * - server directory (other server id)
 * - - job #1 directory (job id)
 * - - job #2 directory (job id)
 * - - ... etc
 * 
 * Multiple servers may be present, not running, in the base directory.
 * 
 * Server waits until file indicating start is created by the client in the base
 * directory. By that time, sampling code and configuration should be prepared
 * in the server directory.
 * 
 * Server creates directory for each job, unzips its code and executes its
 * command. Server waits for the job to finish or kills the job if it exceeds
 * timeout.
 * 
 * Job's status is marked also with files. Job's standard output and error is
 * stored in the job's directory. If anything happens while preparing the job
 * for execution details are written into job's log file.
 * 
 * If anything happens to the server itself it written in the base directory to
 * server's logs.
 * 
 * @author Frantisek Haas
 * 
 */
public class Server {

    /**
     * <p>
     * Sleep interval in seconds while waiting for file indicating start.
     */
    private final static int SLEEP_SECONDS      = 1;

    /**
     * <p>
     * Sleep interval in seconds while waiting for job to finish or reach
     * timeout.
     */
    private final static int MAIN_SLEEP_SECONDS = 3;

    /**
     * <p>
     * Sleep interval while waiting for job to finish or reach timeout. Also the
     * interval the file indicating server's run is touched.
     * 
     * @see Server#runningFileName
     */
    public final static int  MAIN_SLEEP         = MAIN_SLEEP_SECONDS * 1000;

    @SuppressWarnings("unused")
    private final String     identification;
    @SuppressWarnings("unused")
    private final File       baseDirectory;
    private final File       serverDirectory;

    public Server(String identification) {
        this.identification = identification;
        baseDirectory = new File("");
        serverDirectory = new File(identification);
    }

    /**
     * <p>
     * File indicating this server's data for execution are prepared and it
     * should start executing.
     * 
     * <p>
     * Location : server directory.
     * 
     * <p>
     * File client should create after has prepared all data. File server should
     * wait for before working with any data.
     * 
     */
    public static String       startBatchFileName = "start";

    /**
     * <p>
     * File indicating this server's should stop.
     * 
     * <p>
     * Location : server directory.
     * 
     * <p>
     * File client should create if he wants to stop the server. File server
     * should check of existence periodically.
     * 
     */
    public final static String stopBatchFileName  = "stop";

    /**
     * <p>
     * File indicating this server's is still running. In regular intervals
     * touches the time-stamp. If server is not running anymore time-stamp
     * stands still
     * 
     * <p>
     * Location : server directory.
     * 
     * <p>
     * Server creates the file. Client should check if time-stamp is still or
     * changing.
     * 
     * @see Server#MAIN_SLEEP
     * 
     */
    public static String       runningFileName    = "running";

    /**
     * <p>
     * File indicating server has finished all measurements and is exiting.
     * 
     * <p>
     * Location : server directory.
     * 
     * <p>
     * File server creates after has finished all work. File client should check
     * if waiting for server to finish.
     * 
     */
    public final static String finishedFileName   = "finished";

    /**
     * <p>
     * File containing server's output.
     * 
     * <p>
     * Location : server directory.
     * 
     */
    public final static String outFileName        = "out";

    /**
     * <p>
     * File containing server's error.
     * 
     * <p>
     * Location : server directory.
     * 
     */
    public final static String errFileName        = "err";

    /**
     * <p>
     * File containing measurements configuration.
     * 
     * <p>
     * Location : server directory.
     * 
     */
    public final static String dataBatchFileName  = "data";

    /**
     * <p>
     * File indicating job has been started.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String startJobFileName   = "job.start";

    /**
     * <p>
     * File indicating job has finished successfully.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String successJobFileName = "success";

    /**
     * <p>
     * File indicating job has been killed due to timeout exceed.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String timeoutJobFileName = "timeout";

    /**
     * <p>
     * File indication job has not finished successfully probably indicating
     * error exit status.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String errorJobFileName   = "error";

    /**
     * <p>
     * File containing server's log of this job execution. Valid after job has
     * finished.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String logJobFileName     = "log";

    /**
     * <p>
     * File containing job's standard output. Valid after has finished.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String outJobFileName     = "out";

    /**
     * <p>
     * File containing job's standard error. Valid after has finished.
     * 
     * <p>
     * Location : job directory.
     */
    public final static String errJobFileName     = "err";

    /**
     * <p>
     * Waits for start file {@link Server#startBatchFileName(String)}.
     */
    private void waitForStart() {
        File start = new File(serverDirectory, startBatchFileName);
        while (!start.exists()) {
            Server.sleep(SLEEP_SECONDS);
        }
    }

    /**
     * <p>
     * Loads job's configuration from file {@link Server#dataBatchFileName}.
     * 
     * @return
     * @throws IOException
     */
    private Data loadData()
            throws IOException {
        File dataFile = new File(serverDirectory, dataBatchFileName);
        try (FileInputStream dataStream = new FileInputStream(dataFile)) {
            return new Data(dataStream);
        }
    }

    /**
     * <p>
     * Creates directory for execution.
     * 
     * @param directory
     * @throws IOException
     */
    private void prepareJobDirectory(File directory)
            throws IOException {
        if (directory.exists()) {
            throw new IOException(String.format("Job directory already exists [%s].", directory.getAbsolutePath()));
        }

        if (!directory.mkdirs() || !directory.exists() || !directory.isDirectory()) {
            throw new IOException(String.format("Failed to create job directory [%s].", directory.getAbsolutePath()));
        }
    }

    /**
     * <p>
     * Extracts code for execution.
     * 
     * @param directory
     *            Directory to extract code to.
     * @param zip
     *            Code to extract.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void extractJobFiles(File directory, File zip)
            throws FileNotFoundException, IOException {
        ZipUtils.unzip(new FileInputStream(zip), directory);
    }

    /**
     * <p>
     * Creates file indication job's status.
     * 
     * @param jobStatusFile
     *            Job's state.
     * @throws IOException
     */
    private void markJobStatus(File jobStatusFile)
            throws IOException {
        if (!jobStatusFile.createNewFile()) {
            throw new IOException(String.format("Failed to create job state file [%s].", jobStatusFile.getAbsolutePath()));
        }
    }

    /**
     * <p>
     * Checks if file indication server should stop has been created.
     * 
     * @return
     */
    private boolean checkStop() {
        File stop = new File(serverDirectory, stopBatchFileName);
        return stop.exists();
    }

    /**
     * <p>
     * Executes the job and waits for it to finish. In case execution exceeds
     * timeout it gets killed. Marks job status.
     * 
     * @param directory
     *            Directory where code is present and where to execute.
     * @param id
     *            Job's id.
     * @param command
     *            Command to execute.
     * @param timeoutSeconds
     *            Maximum time to wait for job to finish.
     * @throws JobException
     * @throws InterruptedException
     */
    private void executeJob(File directory, String id, String command, int timeoutSeconds)
            throws IOException, InterruptedException {

        markJobStatus(new File(directory, startJobFileName));

        try {
            // start the job
            @SuppressWarnings("deprecation")
            Job job = new Job(command, directory, true);
            job.execute();

            int secondsToTimeout = timeoutSeconds;
            final int secondsSleepLength = MAIN_SLEEP_SECONDS;

            // wait for job to finish or break if it exceeded timeout
            while (job.isRunning()) {
                sleep(secondsSleepLength);
                secondsToTimeout -= secondsSleepLength;

                if (secondsToTimeout <= 0) {
                    break;
                }

                if (checkStop()) {
                    job.destroy();
                    throw new InterruptedException();
                }
            }

            if (job.isRunning()) {
                job.destroy();
                markJobStatus(new File(directory, timeoutJobFileName));
            } else {
                if (job.isFinished()) {
                    markJobStatus(new File(directory, successJobFileName));
                } else {
                    markJobStatus(new File(directory, errorJobFileName));
                }
            }

        } catch (Throwable e) {
            throw e;
        }
    }

    /**
     * <p>
     * Prepares everything for execution and executes.
     * 
     * @param job
     *            The job to execute.
     * 
     * @throws InterruptedException
     */
    private void runJob(Data.Configuration job)
            throws InterruptedException {

        File jobDirectory = new File(serverDirectory, job.getId());
        File jobLog = new File(jobDirectory, logJobFileName);

        try {
            prepareJobDirectory(jobDirectory);
        } catch (IOException e) {
            // this goes to server's log
            e.printStackTrace();
        }

        try (PrintStream jobLogStream = new PrintStream(jobLog)) {
            // all troubles caught here goes to job's log
            try {
                extractJobFiles(jobDirectory, new File(serverDirectory, job.getZip()));

                executeJob(jobDirectory, job.getId(), job.getCommand(), job.getTimeoutSeconds());

            } catch (IOException e) {
                try {
                    markJobStatus(new File(jobDirectory, errorJobFileName));
                    e.printStackTrace(jobLogStream);
                } catch (IOException e1) {
                    IOException custom = new IOException("Failed to mark job error state.", e1);
                    custom.printStackTrace(jobLogStream);
                }
            }
        } catch (FileNotFoundException e) {
            // this goes to server's log
            e.printStackTrace();
        }
    }

    /**
     * Waits for data and executes everything prepared.
     * 
     * @throws IOException
     */
    public void run() {
        try {
            waitForStart();
            Data data = loadData();

            for (Data.Configuration c : data.getJobs()) {
                runJob(c);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                File done = new File(serverDirectory, finishedFileName);
                done.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * Class representing all jobs configurations. Namely their ids, zip files,
     * commands and timeouts.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class Data {

        private final LinkedList<Configuration> jobs = new LinkedList<>();

        /**
         * <p>
         * Creates data object from stream. In case of format error exception is
         * thrown.
         * 
         * @param source
         *            Stream to be parsed.
         * @throws IOException
         */
        public Data(InputStream source)
                throws IOException {

            try (@SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new InputStreamReader(source))) {
                while (true) {
                    String id = reader.readLine();
                    String zip = reader.readLine();
                    String command = reader.readLine();
                    String timeout = reader.readLine();

                    if (id == null && zip == null && command == null && timeout == null) {
                        break;
                    }

                    if (id == null || zip == null || command == null || timeout == null) {
                        throw new IOException("Part of configuration is missing.");
                    }

                    try {
                        jobs.add(new Configuration(
                                StringUtils.decodeFromBase64(id),
                                StringUtils.decodeFromBase64(zip),
                                StringUtils.decodeFromBase64(command),
                                Integer.parseInt(StringUtils.decodeFromBase64(timeout))));
                    } catch (NumberFormatException e) {
                        throw new IOException("Timeout malformed.", e);
                    }
                }
            }
        }

        /**
         * <p>
         * Creates data object from configuration list.
         * 
         * @param jobs
         *            Object to be used.
         */
        public Data(Iterable<Configuration> jobs) {
            Iterator<Configuration> iterator = jobs.iterator();
            while (iterator.hasNext()) {
                this.jobs.add(iterator.next());
            }
        }

        /**
         * <p>
         * Returns jobs configuration.
         * 
         * @return
         */
        public Iterable<Configuration> getJobs() {
            return jobs;
        }

        /**
         * <p>
         * Saves all data into stream which can be later loaded via stream
         * constructor.
         * 
         * @param destination
         * @throws IOException
         */
        public void save(OutputStream destination)
                throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(destination))) {
                for (Configuration c : jobs) {
                    writer.write(StringUtils.encodeToBase64(c.getId()));
                    writer.newLine();
                    writer.write(StringUtils.encodeToBase64(c.getZip()));
                    writer.newLine();
                    writer.write(StringUtils.encodeToBase64(c.getCommand()));
                    writer.newLine();
                    writer.write(StringUtils.encodeToBase64(String.valueOf(c.getTimeoutSeconds())));
                    writer.newLine();
                }
            }
        }

        /**
         * <p>
         * Job configuration.
         * 
         * @author Frantisek Haas
         * 
         */
        public static class Configuration {

            private final String id;
            private final String zip;
            private final String command;
            private final int    timeoutSeconds;

            public Configuration(String id, String zip, String command, int timeoutSeconds) {
                this.id = id;
                this.zip = zip;
                this.command = command;
                this.timeoutSeconds = timeoutSeconds;
            }

            /**
             * <p>
             * Job clients id.
             * 
             * @return
             */
            public String getId() {
                return id;
            }

            /**
             * <p>
             * Code zip file name.
             * 
             * @return
             */
            public String getZip() {
                return zip;
            }

            /**
             * <p>
             * Command to start the job.
             * 
             * @return
             */
            public String getCommand() {
                return command;
            }

            /**
             * <p>
             * Timeout this job should not exceed.
             * 
             * @return
             */
            public int getTimeoutSeconds() {
                return timeoutSeconds;
            }
        }
    }

    /**
     * <p>
     * Politely wraps {@link Thread#sleep(long)} so it won't throw an exception
     * but keeps the thread's interrupted flag on so functions higher in the
     * call stack have a chance to find out that {@link InterruptedException}
     * occurred.
     * 
     * @param seconds
     */
    public static void sleep(long seconds) {
        final int secondsToMilliseconds = 1000;

        try {
            Thread.sleep(seconds * secondsToMilliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
