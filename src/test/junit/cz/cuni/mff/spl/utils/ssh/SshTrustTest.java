/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * Copyright (c) 2014, Vojtěch Horký
 * Copyright (c) 2014, Charles University
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

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.test.SkipOnMissingPropertyRule;

/**
 * The tests here are merged from Trust*Test classes.
 * 
 * @author Vojtěch Horký
 * 
 */
@RunWith(Parameterized.class)
public class SshTrustTest {

    @Rule
    public SkipOnMissingPropertyRule skipTests = new SkipOnMissingPropertyRule();

    @Parameters
    public static Collection<Object[]> getProperties() {
        return Arrays.asList(new Object[][] {
                { "Trust host when fingerprint is provided", "trust-fingerprint.properties.sshtest" },
                { "Trust host through known_hosts file", "trust-knownhosts.properties.sshtest" },
        });
    }

    private final String properties;
    private Session      session;

    public SshTrustTest(String description, String propertyFile) {
        properties = propertyFile;
    }

    @Before
    public void prepareLogger() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Before
    public void ensureSessionIsNull() {
        session = null;
    }

    @After
    public void closeOpenedSession() {
        if (session != null) {
            session.disconnect();
        }
    }

    @Test
    public void accessWorks()
            throws Exception {
        SshDetails details = Utils.createSshDetails(Utils.loadValuesAndSetInput(properties));

        session = SshUtils.createSession(details);

        Assert.assertTrue("Failed to connect the session.", session.isConnected());
    }
}
