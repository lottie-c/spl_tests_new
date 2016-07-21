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
package cz.cuni.mff.spl.deploy.build.vcs;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cz.cuni.mff.spl.test.SkipOnMissingPropertyRule;

/**
 * The tests here are merged from SvnAccess*Test classes.
 * 
 * @author Vojtěch Horký
 * 
 */
@RunWith(Parameterized.class)
public class SvnAccessAndTrustTest {

    @Rule
    public SkipOnMissingPropertyRule skipTests = new SkipOnMissingPropertyRule();

    @Parameters
    public static Collection<Object[]> getProperties() {
        return Arrays.asList(new Object[][] {
                { true, "Access repository with key protected by a passphrase", "git-access-key-passphrase.properties.vcstest" },
                { true, "Access repository with key (no passphrase)", "git-access-key.properties.vcstest" },
                { true, "Access repository over SSH with password", "git-access-password-ssh.properties.vcstest" },
                { true, "Access repository over HTTPS with password", "git-access-password-https.properties.vcstest" },
                { true, "Access public repository", "git-access-public.properties.vcstest" },

                { true, "Trust Git server implicitly", "git-trust-all.properties.vcstest" },
                { false, "Do not trust any Git server implicitly", "git-trust-all-fail.properties.vcstest" },
                { true, "Trust Git server when fingerprint is provided", "git-trust-fingerprint.properties.vcstest" },
                { false, "Wrong fingerprint of a Git server", "git-trust-fingerprint-fail.properties.vcstest" },
                { true, "Trust Git server through known_hosts file", "git-trust-knownhosts.properties.vcstest" },
        });
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final String   properties;
    private final boolean  successful;

    public SvnAccessAndTrustTest(boolean expectSuccess, String description, String propertyFile) {
        properties = propertyFile;
        successful = expectSuccess;
    }

    @Test
    public void accessWorks() throws Exception {
        if (successful) {
            SvnUtils.testHead(folder.newFolder(), properties);
        } else {
            SvnUtils.testHeadFail(folder.newFolder(), properties);
        }
    }
}
