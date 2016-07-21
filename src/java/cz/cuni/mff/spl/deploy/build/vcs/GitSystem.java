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
package cz.cuni.mff.spl.deploy.build.vcs;

import java.io.File;
import java.io.IOException;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.deploy.build.ShellCommand;
import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.deploy.execution.server.Job;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class contains methods to access public git repositories using git
 * installed on the path in the system.
 * 
 * @author Frantisek Haas
 * 
 */
public class GitSystem {

    private static final SplLog logger = SplLogger.getLogger(GitSystem.class);

    /** How long between checks whether command has finished. */
    private static int          SLEEP  = 100;

    /**
     * <p>
     * Checks if there's installed a git somewhere on the class path.
     * 
     * @return
     *         True if git is installed. False otherwise.
     */
    public static boolean isPresent() {
        try {
            ShellCommand command = new ShellCommand(true, "git");
            Job job = new Job(command.getCommand(), new File("."));
            job.execute();
            job.getInput().close();

            while (job.isRunning()) {
                try {
                    Thread.sleep(SLEEP);

                } catch (InterruptedException e) {
                    job.destroy();
                    Thread.currentThread().interrupt();
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    Thread.interrupted();
                }
            }

            if (job.getExitStatus() == 1 && job.getOutput().startsWith("usage: git")) {
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * <p>
     * Clones repository from url into specified directory.
     * 
     * @param url
     *            Url of the repository.
     * @param where
     *            Where to clone the repository.
     * @throws VcsCheckoutException
     */
    public static void clone(String url, File where)
            throws VcsCheckoutException {
        try {
            File dir = where.getAbsoluteFile().getParentFile();

            ShellCommand command = new ShellCommand(true, "git", "clone", url, where.getName());
            Job job = new Job(command.getCommand(), dir);
            job.execute();
            job.getInput().close();

            logger.trace("Cloning ...");

            while (job.isRunning()) {
                try {
                    Thread.sleep(SLEEP);

                } catch (InterruptedException e) {
                    job.destroy();
                    Thread.currentThread().interrupt();
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    Thread.interrupted();
                }
            }

            if (job.getOutput().length() > 0) {
                logger.trace("System git output:\n%s", job.getOutput());
            }

            if (job.getError().length() > 0) {
                logger.trace("System git error:\n%s", job.getError());
            }

            if (!job.isFinished()) {
                throw new VcsCheckoutException("Failed to clone the repository.");
            }

        } catch (IOException e) {
            throw new VcsCheckoutException("Failed to clone the repository.", e);
        }
    }

    /**
     * <p>
     * Calls git in the system to checkout specified revision {@code what} in
     * the repository located in {@code where}.
     * 
     * @param what
     *            Revision to checkout.
     * @param where
     *            Location of cloned repository.
     * @throws VcsCheckoutException
     */
    public static String checkout(String what, File where)
            throws VcsCheckoutException {
        try {
            ShellCommand command = new ShellCommand(true, "git", "checkout", "--force", what);
            Job job = new Job(command.getCommand(), where);
            job.execute();
            job.getInput().close();

            logger.trace("Checking out ...");

            while (job.isRunning()) {
                try {
                    Thread.sleep(SLEEP);

                } catch (InterruptedException e) {
                    job.destroy();
                    Thread.currentThread().interrupt();
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    Thread.interrupted();
                }
            }

            if (job.getOutput().length() > 0) {
                logger.trace("System git output:\n%s", job.getOutput());
            }

            if (job.getError().length() > 0) {
                logger.trace("System git error:\n%s", job.getError());
            }

            if (!job.isFinished()) {
                throw new VcsCheckoutException("Failed to checkout the revision.");
            }

        } catch (IOException e) {
            throw new VcsCheckoutException("Failed to checkout the revision.", e);
        }

        try {
            ShellCommand command = new ShellCommand(true, "git", "rev-parse", what);
            Job job = new Job(command.getCommand(), where);
            job.execute();
            job.getInput().close();

            while (job.isRunning()) {
                try {
                    Thread.sleep(SLEEP);

                } catch (InterruptedException e) {
                    job.destroy();
                    Thread.currentThread().interrupt();
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    Thread.interrupted();
                }
            }

            if (job.getError().length() > 0) {
                logger.trace("System git error:\n%s", job.getError());
            }

            if (!job.isFinished()) {
                throw new VcsCheckoutException("Failed to retrieve revison's hash.");
            }

            return job.getOutput().trim();

        } catch (IOException e) {
            throw new VcsCheckoutException("Failed to retrieve revison's hash.", e);
        }
    }
}
