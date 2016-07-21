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
package cz.cuni.mff.spl.utils.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.utils.ssh.exception.AuthenticationException;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;
import cz.cuni.mff.spl.utils.ssh.exception.UnknownHostException;

/**
 * This class operates secure shell protocol tunnel. Such tunnel can be for
 * instance used to tunnel java sockets or other sorts of communication.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshTunnel {

    private Session          session;
    private final SshDetails details;
    private final int        remotePort;
    private int              localPort;

    public SshTunnel(SshDetails details, int remotePort) {
        this.details = details;
        this.remotePort = remotePort;
        this.localPort = -1;
    }

    /**
     * Initializes and connects the tunnel to the specified host. In case of any
     * trouble exception is thrown.
     * 
     * @throws SshException
     */
    public void open()
            throws SshException {
        final int any_port = 0;
        final String local_host = "localhost";

        try {
            session = SshUtils.createSession(details);
            localPort = session.setPortForwardingL(any_port, local_host, remotePort);

        } catch (JSchException e) {
            close();

            if (e.getCause() instanceof java.net.UnknownHostException) {
                throw new UnknownHostException(e.getMessage());

            } else if (e.getMessage().equals("Auth fail")) {
                throw new AuthenticationException(e.getMessage());

            } else {
                throw new SshException(e.getMessage());
            }
        }
    }

    /**
     * This function disconnects the tunnel if still connected and does the
     * clean up.
     * 
     */
    public void close() {
        if (session != null) {
            session.disconnect();
        }
        session = null;
        localPort = -1;
    }

    /**
     * Determine whether tunnel was initialized and connected.
     * 
     * IMPORTANT: May not be absolutely reliable in case of connection
     * problems after the tunnel itself was successfully initialized and
     * the connection lost moments after. Without any further traffic in a
     * such way disconnected tunnel. This method could return false positive
     * state.
     * 
     * @return
     */
    public boolean isConnected() {
        return (session != null && session.isConnected());
    }

    /**
     * Returns local port this tunnel is bind to. Which is important so for
     * instance java socket can connect to this local port and be tunneled to
     * the remote machine via tunnel.
     * 
     * @return
     *         Tunnel's local port number.
     */
    public int getPort() {
        return localPort;
    }
}
