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
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.Builder;
import cz.cuni.mff.spl.deploy.execution.run.Execution;
import cz.cuni.mff.spl.deploy.execution.run.LocalExecution;
import cz.cuni.mff.spl.deploy.store.LocalStore;

/**
 * 
 * @author Frantisek Haas
 * 
 */
@RunWith(Parameterized.class)
public class ProjectsTest {

    private static final File      projects = new File("src/test/projects");
    private static final String    prefix   = "test-";
    private static final String    xml      = "spl.xml";

    private final Callable<Object> test;

    public ProjectsTest(Callable<Object> test) {
        this.test = test;
    }

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void test()
            throws Exception {
        test.call();
    }

    @Parameters
    public static Collection<Object[]> getProjects()
            throws IOException {

        if (!projects.exists()) {
            fail("Project folders not found.");
        }

        LinkedList<Object[]> tests = new LinkedList<>();

        for (File f : projects.listFiles()) {
            if (f.getName().startsWith(prefix)) {
                tests.add(new Object[] { new MyTest(new File(f, xml)) });
            }
        }

        return tests;
    }

    public static class MyTest implements Callable<Void> {

        @Rule
        private final TemporaryFolder folder = new TemporaryFolder();

        private final File            xml;
        private final String          id;

        public MyTest(File xml) {
            this.xml = xml;
            this.id = xml.getAbsoluteFile().getParentFile().getName().toUpperCase() + ": ";
        }

        private void run()
                throws Exception {
            File tmp = folder.newFolder();
            while (!tmp.exists()) {
                Thread.sleep(100);
            }

            LocalStore localStore = new LocalStore(tmp);
            ConfigurationBundle config = Utils.createTestConfig();

            Builder builder = new Builder(localStore, new Machine("test", "test"), xml, null, config);
            builder.call();

            if (builder.getInfo().getMeasurements().isEmpty()) {
                fail(String.format(id + "No measurements scanned for [%s].", xml.getPath()));
            }

            if (builder.getSamplers().isEmpty()) {
                fail(String.format(id + "No sampling code built for [%s].", xml.getPath()));
            }
            if (builder.getSamplers().size() != builder.getInfo().getMeasurements().size()) {
                fail(String.format(id + "Not enough sampling code built for [%s].", xml.getPath()));
            }

            File executionDirectory = localStore.createTemporaryDirectory("execution");
            Execution execution = new LocalExecution(builder.getInfo(), builder.getSamplers(), executionDirectory, config);
            execution.start();
            execution.waitForFinished();

            if (!execution.isSuccessful()) {
                fail(String.format(id + "All sampling code failed to get measured successfully.", xml.getPath()));
            }
        }

        @Override
        public Void call() {

            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException(id + "Unexpected exception.", e);
            }

            return null;
        }
    }
}
