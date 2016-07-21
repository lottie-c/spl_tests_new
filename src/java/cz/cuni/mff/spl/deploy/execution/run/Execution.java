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
package cz.cuni.mff.spl.deploy.execution.run;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.build.Sampler;
import cz.cuni.mff.spl.deploy.exception.DeployException;
import cz.cuni.mff.spl.deploy.execution.run.exception.ExecutionServerAlreadyRunning;
import cz.cuni.mff.spl.deploy.execution.server.Server;
import cz.cuni.mff.spl.deploy.execution.server.ServerMain;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.PackUtils;
import cz.cuni.mff.spl.utils.StreamUtils;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.ZipUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * This is abstract class which has implemented the logic of execution on local
 * and remote machines.
 * 
 * The dependent code for file manipulation and command execution is left for
 * further implementation in inheriting classes.
 * 
 * @author Frantisek Haas
 * 
 */
public abstract class Execution implements IExecution {

    protected static final SplLog logger                   = SplLogger.getLogger("Execution");

    private static final int      JOB_WAIT_SLEEP           = 10000;
    private static final int      SERVER_WAIT_SLEEP        = 10000;
    private static final String   SERVER_COMMAND_ARGUMENTS = " -server -jar";

    private String serverCommand() {
        return config.getDeploymentConfig().getJavaPath() + SERVER_COMMAND_ARGUMENTS;
    }

    /** Server identification. */
    protected final String                                sid;
    /** Info object to process. */
    protected final Info                                  info;
    /** Configuration. */
    protected final ConfigurationBundle                   config;
    /** Samplers to run. */
    protected final Iterable<Sampler>                     samplers;
    /**
     * Mapping of samplers to identifications. This makes checking of running /
     * finished samplers easier. It's not necessary to check all done files but
     * only the one of upcoming sampler.
     */
    protected final HashMap<SampleIdentification, String> idMapping;

    protected Execution(Info info, Iterable<Sampler> samplers, ConfigurationBundle config) {
        this.sid = UniqueIdProvider.newId();
        this.info = info;
        this.samplers = samplers;
        this.config = config;
        this.idMapping = new HashMap<>();
    }

    /**
     * Creates base directory.
     * 
     * @throws DeployException
     */
    protected abstract void createBaseDirectory()
            throws DeployException;

    /**
     * Creates server specified directory.
     * 
     * @throws DeployException
     */
    protected abstract void createServerDirectory()
            throws DeployException;

    /**
     * Executes command in the base directory and returns process output
     * stream.
     * 
     * @param command
     *            The command to execute.
     * @return
     *         {@link InputStream} to read command result.
     * 
     * @throws DeployException
     */
    protected abstract InputStream executeCommand(String command)
            throws DeployException;

    /**
     * Returns output stream to file located in the base directory and specified
     * with file name.
     * 
     * @param fileName
     *            The file to create.
     * @return
     *         The {@link OutputStream} to write file's data to.
     * 
     * @throws DeployException
     */
    protected abstract OutputStream baseFileOutputStream(String fileName)
            throws DeployException;

    /**
     * Returns output stream to file located in the server's specific directory
     * specified with file name.
     * 
     * @param fileName
     *            The file to create.
     * @return
     *         The {@link OutputStream} to write file's data to.
     * 
     * @throws DeployException
     */
    protected abstract OutputStream serverFileOutputStream(String fileName)
            throws DeployException;

    /**
     * Returns output stream to file located in the job's specific directory
     * specified with file name.
     * 
     * @param jobId
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract OutputStream jobFileOutputStream(String jobId, String fileName)
            throws DeployException;

    /**
     * Returns input stream of file located in the base directory and specified
     * with file name.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract InputStream baseFileInputStream(String fileName)
            throws DeployException;

    /**
     * Returns input stream of file located in the server's specific directory
     * specified with file name.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract InputStream serverFileInputStream(String fileName)
            throws DeployException;

    /**
     * Returns input stream of file located in the job's specific directory
     * specified with file name.
     * 
     * @param jobId
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract InputStream jobFileInputStream(String jobId, String fileName)
            throws DeployException;

    /**
     * Creates file located in the base directory and specified with file name.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract void createBaseFile(String fileName)
            throws DeployException;

    /**
     * Creates file located in the server's specific directory and specified
     * with file name.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract void createServerFile(String fileName)
            throws DeployException;

    /**
     * Creates file located in the job's specific directory and specified with
     * file name.
     * 
     * @param jobId
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract void createJobFile(String jobId, String fileName)
            throws DeployException;

    /**
     * Checks if file with specified file name exists in the base directory.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract boolean existsBaseFile(String fileName)
            throws DeployException;

    /**
     * Checks if file with specified file name exists in the server directory.
     * 
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract boolean existsServerFile(String fileName)
            throws DeployException;

    /**
     * Checks if file with specified file name exists in the specified job's
     * directory.
     * 
     * @param jobId
     * @param fileName
     * @return
     * @throws DeployException
     */
    protected abstract boolean existsJobFile(String jobId, String fileName)
            throws DeployException;

    /**
     * Creates server binary.
     * 
     * @return
     * @throws IOException
     */
    private PackUtils createServer()
            throws IOException {
        PackUtils p = new PackUtils()
                .setMainClass(ServerMain.class.getCanonicalName())
                .addAllInDirectory(ServerMain.class.getPackage().getName().replace('.', '/') + "/")
                .addAllInDirectoryRecursively(ZipUtils.class.getPackage().getName().replace('.', '/') + "/")
                .addResource(org.apache.xerces.impl.dv.util.Base64.class.getName().replace('.', '/') + ".class");

        return p;
    }

    /**
     * Creates server directory if not present. Creates server executable and
     * transfers it to the specified location.
     * 
     * @param serverName
     *            Name of server's executable binary file.
     * 
     * @throws DeployException
     */
    private void copyServer(String serverName)
            throws DeployException {
        try {
            createBaseDirectory();
            logger.debug("Created base directory.");
            createServerDirectory();
            logger.debug("Created server directory.");
        } catch (Exception e) {
            throw new DeployException("Failed to create specified directory for the server.", e);
        }

        try (OutputStream stream = baseFileOutputStream(serverName)) {
            createServer().write(stream);
            logger.info("Transferred server binary.");
        } catch (Exception e) {
            throw new DeployException("Failed to copy the server binary specified directory.", e);
        }
    }

    /**
     * Starts the server binary in the earlier specified directory using the
     * passed command. Retrieves and checks server status.
     * 
     * @param serverCommand
     *            The command to run the server. Directory information free.
     * @throws DeployException
     */
    private void startServer(String serverCommand)
            throws DeployException {
        try (InputStream stream = executeCommand(serverCommand)) {
            logger.info("Started server. Waiting for status");

            int status = stream.read();
            if (status == ServerMain.START_STATUS_SUCCESS) {
                logger.info("Success status received.");

            } else if (status == ServerMain.START_STATUS_FAILURE) {
                logger.info("Failure status received. Another server already running.");
                throw new ExecutionServerAlreadyRunning("Server failed to acquire the lock and shuts down.");

            } else {
                logger.fatal("Unknown status received.");
                throw new DeployException(String.format("Unknown status code from the server [%s].", status));
            }

        } catch (IOException e) {
            throw new DeployException("Failed to read data from the server.", e);
        }
    }

    /**
     * Starts the server based on directory and identification information.
     * 
     * @throws DeployException
     */
    private void runServer()
            throws DeployException {
        logger.info("Starting server identified as [%s].", sid);

        final String serverName = String.format("%s.jar", sid);
        final String serverCommand = String.format("%s %s %s", serverCommand(), serverName, sid);

        copyServer(serverName);

        /**
         * <p>
         * Start the server and if another server is running in the location
         * wait till it's finished and start our's.
         */
        while (true) {
            try {
                startServer(serverCommand);
                break;
            } catch (ExecutionServerAlreadyRunning e) {
            }

            try {
                Thread.sleep(SERVER_WAIT_SLEEP);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                InvokedExecutionConfiguration.checkIfExecutionAborted();
                // if execution was not aborted, clear the interrupted
                // flag
                Thread.interrupted();
            }
        }
    }

    /**
     * Creates archive file name based on id.
     * 
     * @param name
     *            Id of the job.
     * @return
     */
    private String archiveFileName(String name) {
        return String.format("%s.zip", name);
    }

    /**
     * Returns count of samplers.
     * 
     * Will be much better use different structure to hold sampler but don't
     * want to change api.
     * 
     * @return
     */
    private int samplerCount() {
        Iterator<Sampler> iterator = samplers.iterator();
        int samplerCount = 0;
        while (iterator.hasNext()) {
            iterator.next();
            samplerCount++;
        }

        return samplerCount;
    }

    /**
     * Copies zipped samplers to the server directory.
     * 
     * @throws DeployException
     * 
     */
    private void copyCode()
            throws DeployException {

        try {
            createServerDirectory();
        } catch (Exception e) {
            throw new DeployException("Failed to create server directory.", e);
        }

        logger.info("Transferring sampling code for measurements [%d].", samplerCount());

        Iterator<Sampler> iterator = samplers.iterator();
        int id = 1;
        while (iterator.hasNext()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();

            Sampler sampler = iterator.next();
            String jobId = String.valueOf(id);

            try (
                    InputStream input = sampler.getInputStream();
                    OutputStream output = serverFileOutputStream(archiveFileName(jobId))) {
                FileUtils.copy(input, output);
                logger.debug("Transferred sampling code for [%s].", sampler.getIdentification());
                logger.info("[%" + Utils.magnitude(samplerCount()) + "d/%d].", id, samplerCount());

            } catch (Exception e) {
                throw new DeployException("Failed to transfer code to remote directory.", e);
            }

            id++;
        }
    }

    /**
     * Creates job configuration file. Configuration consists of jobs
     * information. Each job has 'id', 'zip file name', 'command', 'timeout in
     * seconds'.
     * 
     * @return
     * @throws IOException
     */
    public Server.Data createConfiguration()
            throws IOException {
        LinkedList<Server.Data.Configuration> configuration = new LinkedList<>();

        Iterator<Sampler> iterator = samplers.iterator();
        int id = 1;
        while (iterator.hasNext()) {
            Sampler sampler = iterator.next();
            String jobId = String.valueOf(id);

            idMapping.put(sampler.getIdentification(), jobId);

            Server.Data.Configuration c = new Server.Data.Configuration(jobId, archiveFileName(jobId), sampler.getCommand(), config.getDeploymentConfig()
                    .getTimeout());
            configuration.add(c);

            id++;
        }

        return new Server.Data(configuration);
    }

    /**
     * Copies configuration file to the server.
     * 
     * @throws DeployException
     */
    private void copyConfiguration()
            throws DeployException {
        try (OutputStream stream = serverFileOutputStream(Server.dataBatchFileName)) {
            createConfiguration().save(stream);
            logger.info("Transferred sampling configuration.");
        } catch (Exception e) {
            throw new DeployException("Failed to transfer configuration to specified directory.", e);
        }
    }

    /**
     * Creates server specific file the server is waiting for. Until file is
     * created server is just waiting and is not running any measurements.
     * 
     * @throws DeployException
     */
    private void markStart()
            throws DeployException {
        try {
            createServerFile(Server.startBatchFileName);
            logger.info("Created start file for the server.");
        } catch (Exception e) {
            throw new DeployException("Failed to create start file for batch server.", e);
        }
    }

    /**
     * This function starts the server and measuring.
     * 
     * @throws DeployException
     */
    @Override
    public void start()
            throws DeployException {
        runServer();
        copyCode();
        copyConfiguration();
        markStart();
    }

    @Override
    public void stop()
            throws DeployException {
        try {
            createServerFile(Server.stopBatchFileName);
            logger.info("Created stop file for the server.");
        } catch (Exception e) {
            throw new DeployException("Failed to create stop file for batch server.", e);
        }
    }

    /**
     * Checks if server is still running or not.
     * 
     * @return
     *         True in case the server is not running anymore. False otherwise.
     * @throws DeployException
     */
    @Override
    public boolean isRunning()
            throws DeployException {
        return !isFinished();
    }

    /**
     * Checks if server has finished measuring. No indication of success of
     * failure, just finish.
     * 
     * @return
     *         True in case the server is not running anymore. False otherwise.
     * @throws DeployException
     */
    @Override
    public boolean isFinished()
            throws DeployException {
        return existsServerFile(Server.finishedFileName);
    }

    /**
     * Checks if all jobs has finished successfully.
     * 
     * @return
     *         True in case all jobs finished successfully. False otherwise.
     * @throws DeployException
     */
    @Override
    public boolean isSuccessful()
            throws DeployException {
        if (isRunning() || !isFinished()) {
            return false;
        }

        for (Sampler sampler : samplers) {
            if (getStatus(sampler.getIdentification()) != Trace.Status.Successful) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets details about measurement status to {@link Info}.
     * 
     * @param samplerIdentification
     */
    private void setMeasurementStatus(SampleIdentification samplerIdentification, Trace.Status status) {
        for (Measurement m : info.getMeasurements()) {
            SampleIdentification sid = new SampleIdentification(m);

            if (sid.equals(samplerIdentification)) {
                if (m.getMeasurementState().isOk()) {
                    if (status == Trace.Status.Successful) {
                        m.getMeasurementState().setLastPhase(LastPhase.MEASURED);
                    } else {
                        m.getMeasurementState().setOk(false);
                        m.getMeasurementState().setLastPhase(LastPhase.FAILED);
                        m.getMeasurementState().setMessage(String.format("Measurement sampler failed to finished succesfully, ended with: %s.", status));
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Checks job status and logs it. If job has finished returns true.
     * Otherwise returns false.
     * 
     * @param samplerId
     * @param sampler
     * @return
     * @throws DeployException
     */
    private boolean jobCheck(int samplerId, Sampler sampler)
            throws DeployException {
        Trace.Status status = getStatus(sampler.getIdentification());

        if (status != Trace.Status.NotStarted) {

            logger.debug("Status [%s] of measurement [%s].", status, sampler.getIdentification());
            logger.info("[%" + Utils.magnitude(samplerCount()) + "d/%d], result [%s].", samplerId, samplerCount(), status);

            if (status != Trace.Status.Successful) {
                logJobTrace(sampler.getIdentification());
            }

            setMeasurementStatus(sampler.getIdentification(), status);

            return true;
        }

        return false;
    }

    /**
     * <p>
     * Sleep between job checks.
     * 
     * @throws DeployException
     */
    private void jobWaitSleep()
            throws DeployException {
        try {
            Thread.sleep(JOB_WAIT_SLEEP);
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
            InvokedExecutionConfiguration.checkIfExecutionAborted();
        }
    }

    /**
     * This function wait for server and measurements to finish. In fixed
     * intervals logs status of upcoming jobs.
     * 
     * @throws DeployException
     */
    @Override
    public void waitForFinished()
            throws DeployException {
        boolean running = true;
        int samplerId = 1;

        try {
            for (Sampler sampler : samplers) {
                while (true) {
                    if (jobCheck(samplerId, sampler)) {
                        break;
                    }

                    // if server has finished but jobs did not, something wrong
                    // happened
                    if (!running) {
                        throw new DeployException("Server has stopped running but some jobs still didn't finish.");
                    } else if ((false || isFinished()) && running) {
                        running = false;
                    }

                    jobWaitSleep();

                }
                samplerId++;
            }
        } finally {
            logServerTrace();
        }
    }

    /**
     * Tries to retrieve job's trace information and prints it to the logger.
     * 
     * @param sampleIdentification
     */
    private void logJobTrace(SampleIdentification sampleIdentification) {
        try {
            Trace trace = getTrace(sampleIdentification);

            if (trace.getOut() != null && trace.getOut().length() > 0) {
                logger.error("Job Out:\n%s", trace.getOut());
            }
            if (trace.getErr() != null && trace.getErr().length() > 0) {
                logger.error("Job Err:\n%s", trace.getErr());
            }
            if (trace.getLog() != null && trace.getLog().length() > 0) {
                logger.error("Job Log:\n%s", trace.getLog());
            }

        } catch (DeployException e) {
            logger.error("Failed to retrieve job's trace information due to [%s].", e.getMessage());
        }
    }

    /**
     * Tries to retrieve server's trace information and prints it to the logger.
     */
    private void logServerTrace() {
        try {
            ServerTrace trace = getServerTrace();

            logger.trace("Server running [%s].", trace.isRunning());

            if (trace.getOut() != null && trace.getOut().length() > 0) {
                logger.error("Server Out:\n%s", trace.getOut());
            }

            if (trace.getErr() != null && trace.getErr().length() > 0) {
                logger.error("Server Err:\n%s", trace.getErr());
            }

        } catch (DeployException e) {
            logger.error(e, "Failed to get server's trace.");
        }
    }

    /**
     * Returns server's trace information.
     * 
     * @return
     * @throws DeployException
     */
    @Override
    public ServerTrace getServerTrace()
            throws DeployException {

        try (
                InputStream outStream = serverFileInputStream(Server.outFileName);
                InputStream errStream = serverFileInputStream(Server.errFileName)) {

            String out = StreamUtils.readEntireStreamToString(outStream);
            String err = StreamUtils.readEntireStreamToString(errStream);
            return new ServerTrace(isRunning(), isFinished(), out, err);

        } catch (Exception e) {
            throw new DeployException(String.format("Failed to retrieve server's status, output and error due to [%s].", e.getMessage()), e);
        }

    }

    /**
     * Returns trace and status information of a single measurement.
     * 
     * @param sampleIdentification
     *            Measurement identification.
     * @return
     * @throws DeployException
     */
    @Override
    public Trace getTrace(SampleIdentification sid)
            throws DeployException {
        String id = idMapping.get(sid);

        Trace.Status status = Trace.Status.NotStarted;

        if (existsJobFile(id, Server.successJobFileName)) {
            status = Trace.Status.Successful;
        } else if (existsJobFile(id, Server.errorJobFileName)) {
            status = Trace.Status.Error;
        } else if (existsJobFile(id, Server.timeoutJobFileName)) {
            status = Trace.Status.Timeout;
        }

        String out = null;
        String err = null;
        String log = null;

        if (status == Trace.Status.Successful || status == Trace.Status.Error || status == Trace.Status.Timeout) {
            try (
                    InputStream outStream = jobFileInputStream(id, Server.outJobFileName);
                    InputStream errStream = jobFileInputStream(id, Server.errJobFileName);
                    InputStream logStream = jobFileInputStream(id, Server.logJobFileName)) {
                out = StreamUtils.readEntireStreamToString(outStream);
                err = StreamUtils.readEntireStreamToString(errStream);
                log = StreamUtils.readEntireStreamToString(logStream);
            } catch (IOException e) {
                throw new DeployException(String.format("Failed to retrieve trace information due to [%s].", e.getMessage()), e);
            }
        }

        return new Trace(status, out, err, log);
    }

    /**
     * Returns status information of a single measurement.
     * 
     * @param sampleIdentification
     *            Measurement identification.
     * @return
     * @throws DeployException
     */
    public Trace.Status getStatus(SampleIdentification sid)
            throws DeployException {
        String id = idMapping.get(sid);

        Trace.Status status = Trace.Status.NotStarted;

        if (existsJobFile(id, Server.successJobFileName)) {
            status = Trace.Status.Successful;

        } else if (existsJobFile(id, Server.errorJobFileName)) {
            status = Trace.Status.Error;

        } else if (existsJobFile(id, Server.timeoutJobFileName)) {
            status = Trace.Status.Timeout;
        }

        return status;
    }

    /**
     * Returns stream to measured data.
     * 
     * @param sampleIdentification
     * @return
     * @throws DeployException
     */
    @Override
    public InputStream getResult(SampleIdentification sid)
            throws DeployException {
        String id = idMapping.get(sid);
        for (Sampler sampler : samplers) {
            if (sampler.getIdentification().equals(sid)) {
                return jobFileInputStream(id, sampler.getResultFileName());
            }
        }
        throw new DeployException("Sampler not present in this execution context.");
    }
}
