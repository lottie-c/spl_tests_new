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
package cz.cuni.mff.spl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import cz.cuni.mff.spl.utils.interactive.InteractiveConsole;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.interactive.InteractiveSwingGui;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Command line interface to SPL framework.
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 * 
 */
public class Main {

    /** The logger. */
    private static final SplLog logger = SplLogger.getLogger(Main.class);

    private static class InteractivityConverter implements IStringConverter<InteractiveInterface> {
        @Override
        public InteractiveInterface convert(String value) {
            if (value == null) {
                return null;
            }

            if (value.equals("console")) {
                return new InteractiveConsole();
            } else if (value.equals("gui")) {
                return new InteractiveSwingGui();
            }

            return null;
        }
    }

    @Parameters(separators = "= ")
    private static class RunConfig {
        @Parameter
        private final List<String>  xmlConfig     = new LinkedList<String>();

        @Parameter(names = { "-c", "--config" }, converter = FileConverter.class, description = "Local INI configuration")
        public File                 iniConfig     = null;

        @Parameter(names = { "-w", "--work-dir" }, converter = FileConverter.class, description = "Working directory")
        public File                 workDir       = new File(".spl");

        @Parameter(names = { "-m", "--machine" }, description = "Machine where to measure")
        public String               machine       = null;

        @Parameter(names = { "-i", "--interactivity" }, converter = InteractivityConverter.class, description = "Interactive behavior")
        public InteractiveInterface interactivity = null;

        @Parameter(names = { "-v", "--verbose" }, description = "Verbose output + log to file")
        public boolean              verbose       = false;

        public void check(JCommander parser) {
            if (xmlConfig.size() != 1) {
                throw new ParameterException("Single XML configuration file required.");
            }
        }

        public File getXml() {
            return new File(xmlConfig.get(0));
        }

        public void updateLogging() {
            if (verbose) {
                SplLogger.reloadConfiguration(this.getClass().getClassLoader().getResourceAsStream("log4j.verbose"));
            }
        }
    }

    /**
     * The main method.
     * 
     * @param args
     *            The arguments.
     * @throws Exception
     *             The exception.
     */
    public static void main(String[] args)
            throws Exception {

        RunConfig config = new RunConfig();
        JCommander parser = new JCommander(config);
        try {
            parser.parse(args);
            config.updateLogging();
            config.check(parser);
        } catch (ParameterException e) {
            System.err.printf("Error: %s\n", e.getMessage());
            parser.usage();
            return;
        }

        try {
            Run.run(config.getXml(), config.workDir, config.iniConfig,
                    config.machine, config.interactivity);
        } catch (Throwable e) {
            logger.fatal(e, "Fatal error: %s", e.getMessage());
        }
    }
}
