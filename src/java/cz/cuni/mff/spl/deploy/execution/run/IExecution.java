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

import java.io.InputStream;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.exception.DeployException;

/**
 * Interface for execution batch of samplers.
 * 
 * @author Frantisek Haas
 * 
 */
public interface IExecution extends AutoCloseable {

    /**
     * Starts the server and measuring.
     * 
     * @throws Exception
     */
    public void start()
            throws DeployException;

    /**
     * Stops the server.
     * 
     * @throws DeployException
     */
    public void stop()
            throws DeployException;

    /**
     * Might not stop the server or measurements but releases all client
     * resources required for communication with the server.
     * 
     */
    @Override
    public void close();

    /**
     * Checks if server is still running.
     * 
     * @return
     *         True if server is not running anymore. False otherwise.
     * @throws DeployException
     */
    public boolean isRunning()
            throws DeployException;

    /**
     * Checks if server is stopped and finished measuring.
     * 
     * @return
     *         True if server is not running anymore. False otherwise.
     * @throws DeployException
     */
    public boolean isFinished()
            throws DeployException;

    /**
     * In case server has finished status of all jobs is checked.
     * 
     * @return
     *         True if all jobs has finished successfully. False otherwise.
     * @throws DeployException
     */
    public boolean isSuccessful()
            throws DeployException;

    /**
     * Waits for server and measurements to finish.
     * 
     * @throws DeployException
     */
    public void waitForFinished()
            throws DeployException;

    /**
     * Gets server trace information.
     * 
     * @return
     * @throws DeployException
     */
    public ServerTrace getServerTrace()
            throws DeployException;

    /**
     * Gets measurement run trace information.
     * 
     * @param sampleIdentification
     * @return
     * @throws DeployException
     */
    public Trace getTrace(SampleIdentification sampleIdentification)
            throws DeployException;

    /**
     * Gets measurement run status.
     * 
     * @param sampleIdentification
     * @return
     * @throws DeployException
     */
    public InputStream getResult(SampleIdentification sampleIdentification)
            throws DeployException;

    /**
     * Class holds server trace information.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class ServerTrace {

        private final boolean running;
        private final boolean finished;
        private final String  out;
        private final String  err;

        public ServerTrace(boolean running, boolean finished, String out, String err) {
            this.running = running;
            this.finished = finished;
            this.out = out;
            this.err = err;
        }

        /**
         * @return
         *         True if server is still running.
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * @return
         *         True if server has marked its finish.
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * @return
         *         Server's standard output.
         */
        public String getOut() {
            return out;
        }

        /**
         * @return
         *         Server's standard error.
         */
        public String getErr() {
            return err;
        }
    }

    /**
     * Class holds trace information of a single run of measurement code.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class Trace {

        public enum Status {
            Started, Successful, Error, Timeout, NotStarted
        }

        private final Status status;
        private final String out;
        private final String err;
        private final String log;

        public Trace(Status status, String out, String err, String log) {
            this.status = status;
            this.out = out;
            this.err = err;
            this.log = log;
        }

        /**
         * @return
         *         Program standard output.
         */
        public String getOut() {
            return out;
        }

        /**
         * @return
         *         Program standard error.
         */
        public String getErr() {
            return err;
        }

        /**
         * @return
         *         Server log from program execution.
         */
        public String getLog() {
            return log;
        }

        /**
         * @return
         *         Program exit status.
         */
        public Status getStatus() {
            return status;
        }
    }
}
