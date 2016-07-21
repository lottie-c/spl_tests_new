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
import java.io.OutputStream;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.Sampler;
import cz.cuni.mff.spl.deploy.exception.DeployException;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.SshCommand;
import cz.cuni.mff.spl.utils.ssh.SshDetails;
import cz.cuni.mff.spl.utils.ssh.SshReconnectingInputStream;
import cz.cuni.mff.spl.utils.ssh.SshReconnectingOutputStream;
import cz.cuni.mff.spl.utils.ssh.SshReconnectingSession;
import cz.cuni.mff.spl.utils.ssh.SshReconnectingUtils;
import cz.cuni.mff.spl.utils.ssh.UnixFile;

/**
 * This class implements abstract functions for execution via SSH (SFTP).
 * 
 * Call of {@link RemoteExecution#close()} is required for release of all
 * resources and stop of inner threads.
 * 
 * In case connection to the remote machine is list while execution is still in
 * progress server keeps running. Under the hood is used JSch for SSH connection
 * and NOTTY stays open on the remote host. If NOTTY would not stay opened some
 * system specific command must be used to keep the server running like NOHUP,
 * TMUX or SCREEN.
 * 
 * In case connection is dropped while waiting for measurements to finish
 * {@link SshReconnectingSession} keeps reconnecting. JSch session itself is not
 * a thread
 * safe library but calling disconnect on already disconnected and invalid
 * session should not cause any runtime errors. Disconnected session is then
 * replaced with a possibly connected session. More support for transfer or
 * command execution repeat should be however added to {@link Execution} to
 * survive connection lost on these events as well.
 * 
 * @author Frantisek Haas
 * 
 */
public class RemoteExecution extends Execution implements IExecution {

    /** The logger. */
    protected static final SplLog        logger = SplLogger.getLogger(RemoteExecution.class);

    private final SshReconnectingSession session;
    private final UnixFile               basePath;
    private final UnixFile               serverPath;

    /**
     * Initializes execution server.
     * 
     * @param info
     *            Project configuration.
     * @param samplers
     *            Samplers to execute.
     * @param details
     *            Connection details to open session with.
     * @param path
     *            Path where to place execution server.
     * @param config
     *            Further configuration.
     * 
     * @throws DeployException
     */
    public RemoteExecution(Info info, Iterable<Sampler> samplers, SshDetails details, String path, ConfigurationBundle config)
            throws DeployException {
        super(info, samplers, config);

        session = new SshReconnectingSession(details);
        session.connect();

        basePath = new UnixFile(path);
        serverPath = new UnixFile(path, sid);

        logger.trace("Initialized remote execution on host [%s] in path [%s]", details.getUrl(), path);
    }

    /**
     * Initializes execution server.
     * 
     * @param info
     *            Project configuration.
     * @param samplers
     *            Samplers to execute.
     * @param session
     *            Already connected session.
     * @param path
     *            Path where to place execution server.
     * @param config
     *            Further configuration.
     * 
     * @throws DeployException
     */
    public RemoteExecution(Info info, Iterable<Sampler> samplers, SshReconnectingSession session, String path, ConfigurationBundle config)
            throws DeployException {
        super(info, samplers, config);

        this.session = session;

        basePath = new UnixFile(path);
        serverPath = new UnixFile(path, sid);

        logger.trace("Initialized remote execution on host [%s] in path [%s]", session.getDetails().getUrl(), path);
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    protected void createBaseDirectory()
            throws DeployException {
        SshReconnectingUtils.createDirectory(session, basePath.getPath());
    }

    @Override
    protected void createServerDirectory()
            throws DeployException {
        SshReconnectingUtils.createDirectory(session, serverPath.getPath());
    }

    @Override
    protected InputStream executeCommand(String command)
            throws DeployException {
        String fullCommand = String.format("cd %s; %s", basePath.getPath(), command);

        @SuppressWarnings("resource")
        SshCommand cmd = new SshCommand(session.getSession(), fullCommand);
        cmd.execute();

        return cmd.getSmartOutputStream();
    }

    @Override
    protected OutputStream baseFileOutputStream(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(basePath, fileName);
        return new SshReconnectingOutputStream(session, file.getPath());
    }

    @Override
    protected OutputStream serverFileOutputStream(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(serverPath, fileName);
        return new SshReconnectingOutputStream(session, file.getPath());
    }

    @Override
    protected OutputStream jobFileOutputStream(String jobId, String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(new UnixFile(serverPath, jobId), fileName);
        return new SshReconnectingOutputStream(session, file.getPath());
    }

    @Override
    protected InputStream baseFileInputStream(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(basePath, fileName);
        return new SshReconnectingInputStream(session, file.getPath());
    }

    @Override
    protected InputStream serverFileInputStream(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(serverPath, fileName);
        return new SshReconnectingInputStream(session, file.getPath());
    }

    @Override
    protected InputStream jobFileInputStream(String jobId, String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(new UnixFile(serverPath, jobId), fileName);
        return new SshReconnectingInputStream(session, file.getPath());
    }

    @Override
    protected void createBaseFile(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(basePath, fileName);
        SshReconnectingUtils.createFile(session, file.getPath());
    }

    @Override
    protected void createServerFile(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(serverPath, fileName);
        SshReconnectingUtils.createFile(session, file.getPath());
    }

    @Override
    protected void createJobFile(String jobId, String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(new UnixFile(serverPath, jobId), fileName);
        SshReconnectingUtils.createFile(session, file.getPath());
    }

    @Override
    protected boolean existsBaseFile(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(basePath, fileName);
        return SshReconnectingUtils.fileExists(session, file.getPath());
    }

    @Override
    protected boolean existsServerFile(String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(serverPath, fileName);
        return SshReconnectingUtils.fileExists(session, file.getPath());
    }

    @Override
    protected boolean existsJobFile(String jobId, String fileName)
            throws DeployException {
        UnixFile file = new UnixFile(new UnixFile(serverPath, jobId), fileName);
        return SshReconnectingUtils.fileExists(session, file.getPath());
    }
}
