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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.Sampler;
import cz.cuni.mff.spl.deploy.exception.DeployException;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.SystemUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * This class implements abstract functions for local execution.
 * 
 * @author Frantisek Haas
 * 
 */
public class LocalExecution extends Execution implements IExecution {

    /** The logger. */
    protected static final SplLog logger = SplLogger.getLogger(LocalExecution.class);

    private final File            basePath;
    private final File            serverPath;

    public LocalExecution(Info info, Iterable<Sampler> samplers, File path, ConfigurationBundle config) {
        super(info, samplers, config);
        basePath = path;
        serverPath = new File(path, sid);

        logger.trace("Initialized local execution in path [%s]", path.getAbsolutePath());
    }

    @Override
    protected void createBaseDirectory()
            throws DeployException {
        try {
            FileUtils.createDirectory(basePath);
        } catch (IOException e) {
            throw new DeployException("Failed to create base directory.", e);
        }
    }

    @Override
    protected void createServerDirectory()
            throws DeployException {
        try {
            FileUtils.createDirectory(serverPath);
        } catch (IOException e) {
            throw new DeployException("Failed to create server directory.", e);
        }
    }

    @Override
    protected InputStream executeCommand(String command)
            throws DeployException {
        try {
            Process process = Runtime.getRuntime().exec(
                    command,
                    SystemUtils.getDefaultEnvironment(),
                    basePath);
            return process.getInputStream();
        } catch (IOException e) {
            throw new DeployException("Failed to execute command.", e);
        }
    }

    @Override
    public void close() {
        // no resources to release
    }

    private OutputStream getFileOutputStream(File base, String fileName, String fileKind) throws DeployException {
        File file = new File(base, fileName);
        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new DeployException(String.format("Failed to read %s file [%s].",
                    fileKind, file), e);
        }
    }

    @Override
    protected OutputStream baseFileOutputStream(String fileName)
            throws DeployException {
        return getFileOutputStream(basePath, fileName, "base");
    }

    @Override
    protected OutputStream serverFileOutputStream(String fileName)
            throws DeployException {
        return getFileOutputStream(serverPath, fileName, "server");
    }

    @Override
    protected OutputStream jobFileOutputStream(String jobId, String fileName)
            throws DeployException {
        return getFileOutputStream(new File(serverPath, jobId), fileName, "job");
    }

    private InputStream getFileInputStream(File base, String fileName, String fileKind) throws DeployException {
        File file = new File(base, fileName);
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new DeployException(String.format("Failed to read %s file [%s].",
                    fileKind, file), e);
        }
    }

    @Override
    protected InputStream baseFileInputStream(String fileName)
            throws DeployException {
        return getFileInputStream(basePath, fileName, "base");
    }

    @Override
    protected InputStream serverFileInputStream(String fileName)
            throws DeployException {
        return getFileInputStream(serverPath, fileName, "server");
    }

    @Override
    protected InputStream jobFileInputStream(String jobId, String fileName)
            throws DeployException {
        return getFileInputStream(new File(serverPath, jobId), fileName, "job");
    }

    @Override
    protected void createBaseFile(String fileName)
            throws DeployException {
        try {
            File file = new File(basePath, fileName);
            file.createNewFile();
        } catch (IOException e) {
            throw new DeployException("Failed to create base file.", e);
        }
    }

    @Override
    protected void createServerFile(String fileName)
            throws DeployException {
        try {
            File file = new File(serverPath, fileName);
            file.createNewFile();
        } catch (IOException e) {
            throw new DeployException("Failed to create server file.", e);
        }
    }

    @Override
    protected void createJobFile(String jobId, String fileName)
            throws DeployException {
        try {
            File file = new File(new File(serverPath, jobId), fileName);
            file.createNewFile();
        } catch (IOException e) {
            throw new DeployException("Failed to create job file.", e);
        }
    }

    @Override
    protected boolean existsBaseFile(String fileName)
            throws DeployException {
        File file = new File(basePath, fileName);
        return file.exists();
    }

    @Override
    protected boolean existsServerFile(String fileName)
            throws DeployException {
        File file = new File(serverPath, fileName);
        return file.exists();
    }

    @Override
    protected boolean existsJobFile(String jobId, String fileName)
            throws DeployException {
        File file = new File(new File(serverPath, jobId), fileName);
        return file.exists();
    }
}
