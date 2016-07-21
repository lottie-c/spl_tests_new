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
package cz.cuni.mff.spl.deploy.build;

import java.io.File;
import java.io.IOException;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Build;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.execution.server.Job;
import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;

/**
 * <p>
 * This class provides tools for building checked out revision's code. It
 * executes specified project's build command and waits for its completion.
 * 
 * @author Frantisek Haas
 * 
 */
public class BuilderUtils {

    private static final SplLog logger = Builder.logger;

    /** How long to wait between checks if build process has finished. */
    private static final int    SLEEP  = 100;

    /**
     * Call specified build command in platform specific shell and waits for
     * completion.
     * 
     * @param revision
     *            The revision to build.
     * @param directory
     *            Where to execute build command.
     * @param config
     *            Various configuration.
     * @throws BuildException
     */
    public static void buildRevision(Revision revision, File directory, ConfigurationBundle config)
            throws BuildException {

        Build build = revision.getProject().getBuild();

        if (build == null || build.getCommand() == null || build.getCommand().isEmpty()) {
            logger.debug("Skipping build as no build command is set");
            return;
        }

        ShellCommand shellCommand = new ShellCommand(config.getDeploymentConfig().getUseSystemShell(), build.getCommand());
        Job job = new Job(shellCommand.getCommand(), directory);

        try {
            job.execute();

            logger.trace("Executing ...");

            while (job.isRunning()) {
                job.getOutput();
                job.getError();

                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    job.destroy();
                    Thread.currentThread().interrupt();
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    // if execution was not aborted, clear the interrupted flag
                    Thread.interrupted();
                }
            }

            if (!job.isFinished()) {
                String msg = String.format("Build command failed [%s].", build.getCommand());
                logger.fatal(msg);
                /* Trim the output to save some space. */
                logger.fatal("Build command standard output stream:\n%s", job.getOutput().trim());
                logger.fatal("Build command standard error output stream: \n%s", job.getError().trim());
                throw new BuildException(msg);
            } else {
                logger.debug("Executed build command [%s].", StringUtils.arrayToString(shellCommand.getCommand(), " "));
                // logger.info("Executed it in the     [%s].",
                // directory.getPath());
            }

        } catch (IOException e) {
            throw new BuildException("Reading output of failed custom build command process failed.", e);
        }
    }
}
