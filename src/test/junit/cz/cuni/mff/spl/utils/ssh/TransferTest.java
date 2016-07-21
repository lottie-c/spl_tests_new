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

import static org.junit.Assert.fail;

import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class TransferTest {

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void test()
            throws Exception {
        SshDetails details = null;

        details = Utils.createSshDetails(Utils.loadValuesAndSetInput("key.properties.sshtest"));

        UnixFile file = new UnixFile("/tmp", UUID.randomUUID().toString());

        try (AutoSession session = new AutoSession(details)) {
            if (SshUtils.fileExists(session.getChannel(), file.getPath())) {
                fail(String.format("File [%s] already exists on host machine.", file.getPath()));
            }

            SshUtils.createFile(session.getChannel(), file.getPath());

            if (!SshUtils.fileExists(session.getChannel(), file.getPath())) {
                fail(String.format("File was not created [%s] on host machine.", file.getPath()));
            }

            try (SshOutputStream out = new SshOutputStream(session.getChannel(), file.getPath())) {
                out.write(new byte[] { 0, 42, 0, 24 });
            }

            try (SshInputStream in = new SshInputStream(session.getChannel(), file.getPath())) {
                if (in.read() != 0) {
                    fail("File content corrupted.");
                }
                if (in.read() != 42) {
                    fail("File content corrupted.");
                }
                if (in.read() != 0) {
                    fail("File content corrupted.");
                }
                if (in.read() != 24) {
                    fail("File content corrupted.");
                }
            }

            SshUtils.removeFile(session.getChannel(), file.getPath());

            if (SshUtils.fileExists(session.getChannel(), file.getPath())) {
                fail(String.format("File [%s] was not removed from host machine.", file.getPath()));
            }

            if (!session.getChannel().isConnected() || session.getChannel().isClosed() || !session.getSession().isConnected()) {
                fail("Session fail.");
            }
        }
    }

    public static class AutoSession implements AutoCloseable {

        private final Session     session;
        private final ChannelSftp channel;

        public AutoSession(SshDetails details)
                throws Exception {
            session = SshUtils.createSession(details);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        }

        public Session getSession() {
            return session;
        }

        public ChannelSftp getChannel() {
            return channel;
        }

        @Override
        public void close() {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
