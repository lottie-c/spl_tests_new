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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.cuni.mff.spl.utils.interactive.InteractiveConsole;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class GitCheckoutSystemCacheTest {

    Git                    git;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void init()
            throws Exception {
        LogManager.getRootLogger().setLevel(Level.FATAL);

        Assume.assumeTrue(GitSystem.isPresent());

        InteractiveConsole console = new InteractiveConsole();
        Map<String, String> values = Utils.loadValuesAndSetInput("git-access-public.properties.vcstest");
        git = new Git(values.get("url"), values, console, folder.newFolder(), null);

        Assume.assumeNotNull(git);

        git.setUseSystemGit();

    }

    @Test
    public void testHead()
            throws Exception {
        File tmp = folder.newFolder();
        String id = git.checkout("HEAD", tmp);
        File test = new File(tmp, "head");

        assertEquals(id, "22b3614cdd9c58e9ab80d814ec8f92fea37e4467");
        assertTrue(test.exists());
    }

    @Test
    public void testBranch()
            throws Exception {
        File tmp = folder.newFolder();
        String id = git.checkout("origin/testing", tmp);
        File test = new File(tmp, "branch");

        assertEquals(id, "4f9389520fcab06b7e729e8d0d5295755d83db88");
        assertTrue(test.exists());
    }

    @Test
    public void testTag()
            throws Exception {
        File tmp = folder.newFolder();
        String id = git.checkout("1.0", tmp);
        File test = new File(tmp, "tag");

        assertEquals(id, "bbb84c23b05383daeb1f332c2b93e500d4108c11");
        assertTrue(test.exists());
    }

    @Test
    public void testRevision()
            throws Exception {
        File tmp = folder.newFolder();
        String id = git.checkout("12b81d", tmp);
        File test = new File(tmp, "revision");

        assertEquals(id, "12b81d2a2f04cec9eae1c3ebbd7fc0ff9a7c125d");
        assertTrue(test.exists());
    }
}
