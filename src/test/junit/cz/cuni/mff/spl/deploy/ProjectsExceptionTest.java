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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
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
import cz.cuni.mff.spl.deploy.build.Builder;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.utils.Pair;

/**
 * 
 * @author Frantisek Haas
 * 
 */
@RunWith(Parameterized.class)
public class ProjectsExceptionTest {

    private static final File      projects = new File("src/test/projects");
    private static final String    prefix   = "exception-test-";
    private static final String    xml      = "spl.xml";

    private final Callable<Object> test;

    public ProjectsExceptionTest(Callable<Object> test) {
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

        private Pair<String, String> expectedCause()
                throws Exception {
            Properties properties = new Properties();
            try (InputStream in = new FileInputStream(new File(xml.getParentFile(), "cause.properties"))) {
                properties.load(in);
            }

            String type = properties.getProperty("type");
            String message = properties.getProperty("message");

            return new Pair<String, String>(type, message);
        }

        private void run()
                throws Exception {
            File tmp = folder.newFolder();
            while (!tmp.exists()) {
                Thread.sleep(100);
            }

            Pair<String, String> cause = expectedCause();
            String type = cause.getLeft();
            String message = cause.getRight();

            boolean thrown = false;
            try {
                LocalStore store = new LocalStore(tmp);

                Builder builder = new Builder(store, new Machine("test", "test"), xml, null, Utils.createTestConfig());
                builder.call();

            } catch (Throwable e) {
                thrown = true;
                if (!e.getClass().getCanonicalName().equals(type)) {
                    fail(String.format(id + "Expected different type [%s] instead of [%s].", type, e.getClass().getCanonicalName()));
                }
                if (!e.getMessage().contains(message)) {
                    fail(String.format(id + "Expected different message [%s] instead of [%s].", message, e.getMessage()));
                }
            }

            if (!thrown) {
                fail(String.format(id + "Expected exception to be thrown, type [%s], message [%s].", type, message));
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
