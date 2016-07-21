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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsException;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.interactive.InteractiveSilentConsole;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class GitUtils {

    /** The logger. */
    private static final SplLog logger = SplLogger.getLogger(GitUtils.class);

    public static void testHead(File tmp, String properties)
            throws Exception {
        Git git = prepareGit(tmp, properties);

        git.checkout("master", tmp);

        File check = new File(tmp, ".git");
        assertTrue(check.exists());
    }

    public static void testHeadFail(File tmp, String properties)
            throws Exception {
        Git git = prepareGit(tmp, properties);

        try {
            git.checkout("master", tmp);
            fail("Should have failed due to host verification.");
        } catch (VcsException e) {
            // ok - must fail
            logger.trace(e, "Successfully failed due to [%s].", e.getMessage());
        }
    }

    private static Git prepareGit(File tmp, String properties) throws Exception {
        LogManager.getRootLogger().setLevel(Level.FATAL);

        InteractiveInterface console = new InteractiveSilentConsole();

        Map<String, String> values = Utils.loadValuesAndSetInput(properties);
        return new Git(values.get("url"), values, console, null);
    }
}
