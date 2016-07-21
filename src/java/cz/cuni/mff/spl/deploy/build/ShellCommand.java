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
package cz.cuni.mff.spl.deploy.build;

import java.util.LinkedList;

/**
 * Creates current platform specific shell command.
 * 
 * <br>
 * 
 * In case 'command' passed to {@link Runtime#exec(String)} or
 * {@link ProcessBuilder#command(String...)} is not a binary file on the path
 * the execution will fail. For example if 'ant' is called on windows no
 * executable is found. The reason is there is no executable named 'ant',
 * there's just 'ant.bat' which can be executed only indirectly via cmd shell.
 * The same happens when shell commands are called such as 'echo', 'ls', etc.
 * 
 * @author Frantisek Haas
 * 
 */
public class ShellCommand {

    private final LinkedList<String> commands = new LinkedList<>();

    /**
     * Obtains current platform this JVM is running on and creates platform's
     * specific command.
     * 
     * @param config
     * @param command
     *            System shell free or shell dependent command.
     */
    public ShellCommand(boolean useSystemShell, String... command) {

        if (useSystemShell) {
            switch (Platform.getCurrent()) {
                case Windows:
                    commands.add("cmd");
                    commands.add("/c");
                    break;

                case Linux:
                    commands.add("/bin/sh");
                    commands.add("-c");
                    break;

                default:
                    // hope for the best
                    commands.add("sh");
                    commands.add("-c");
                    break;
            }
        }

        for (String c : command) {
            commands.add(c);
        }
    }

    /**
     * @return
     *         Platform specific command derived from the original command.
     */
    public String[] getCommand() {
        return commands.toArray(new String[commands.size()]);
    }

    public enum Platform {

        Windows, Linux, Unknown;

        public static Platform getCurrent() {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.indexOf("win") >= 0) {
                return Platform.Windows;
            } else if (osName.indexOf("linux") >= 0) {
                return Platform.Linux;
            } else {
                return Platform.Unknown;
            }
        }
    }
}
