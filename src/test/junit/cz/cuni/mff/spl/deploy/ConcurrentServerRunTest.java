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
package cz.cuni.mff.spl.deploy;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.deploy.build.Builder;
import cz.cuni.mff.spl.deploy.build.Sampler;
import cz.cuni.mff.spl.deploy.execution.run.IExecution;
import cz.cuni.mff.spl.deploy.execution.run.LocalExecution;
import cz.cuni.mff.spl.deploy.store.LocalStore;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class ConcurrentServerRunTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final File     xml    = new File("src/test/projects/test-basic/spl.xml");

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void test()
            throws Exception {

        LocalStore store = new LocalStore(folder.newFolder("store"));

        Builder builder = new Builder(store, new Machine("test", "test"), xml, null, Utils.createTestConfig());
        builder.call();

        List<Sampler> samplers = builder.getSamplers();
        Info info = builder.getInfo();

        if (samplers.isEmpty()) {
            fail("No sampling code built.");
        }

        File executionDirectory = store.createTemporaryDirectory("execution");
        try (
                IExecution e1 = new LocalExecution(info, samplers, executionDirectory, Utils.createTestConfig());
                IExecution e2 = new LocalExecution(info, samplers, executionDirectory, Utils.createTestConfig());
                IExecution e3 = new LocalExecution(info, samplers, executionDirectory, Utils.createTestConfig());
                IExecution e4 = new LocalExecution(info, samplers, executionDirectory, Utils.createTestConfig());
                IExecution e5 = new LocalExecution(info, samplers, executionDirectory, Utils.createTestConfig())) {

            e1.start();
            e2.start();
            e3.start();
            e4.start();
            e5.start();

            e1.waitForFinished();
            e2.waitForFinished();
            e3.waitForFinished();
            e4.waitForFinished();
            e5.waitForFinished();

            if (!e1.isSuccessful() || !e2.isSuccessful() || !e3.isSuccessful() || !e4.isSuccessful() || !e5.isSuccessful()) {
                fail("Execution server 1 failed to finish successfully.");
            }
        }
    }
}
