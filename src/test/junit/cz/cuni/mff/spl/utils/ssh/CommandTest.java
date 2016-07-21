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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class CommandTest {

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void okStatusAndOutputTest()
            throws Exception {
        SshDetails details = null;

        details = Utils.createSshDetails(Utils.loadValuesAndSetInput("key.properties.sshtest"));

        SshCommand sshCommand = new SshCommand(details, "ls");
        sshCommand.execute();

        for (int i = 0; i < 10; i++) {
            if (!sshCommand.isRunning()) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        if (sshCommand.isRunning()) {
            fail("Command running for too long.");
        }

        assertTrue(sshCommand.getExitStatus() == 0);
        assertTrue(sshCommand.getOutput().length() > 0);
        assertTrue(sshCommand.getError().length() == 0);
    }

    @Test
    public void failStatusAndOutputTest()
            throws Exception {
        SshDetails details = null;

        details = Utils.createSshDetails(Utils.loadValuesAndSetInput("key.properties.sshtest"));

        SshCommand sshCommand = new SshCommand(details, "thisisnotavalidcommand");
        sshCommand.execute();

        for (int i = 0; i < 10; i++) {
            if (!sshCommand.isRunning()) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        if (sshCommand.isRunning()) {
            fail("Command running for too long.");
        }

        assertTrue(sshCommand.getExitStatus() != 0);
        assertTrue(sshCommand.getError().length() > 0);
    }
}
