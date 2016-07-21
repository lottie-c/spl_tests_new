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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** Simple processor of command-line options. */
public class Opts {
    private class Argument {
        String  argument;
        boolean processed;

        Argument(String a) {
            argument = a;
            processed = false;
        }

        void setProcessed() {
            processed = true;
        }

        boolean unprocessed() {
            return !processed;
        }
    }

    private final List<Option>   options;
    private final List<Argument> arguments;

    /**
     * Create processor from actual command-line parameters.
     * 
     * @param args
     *            Parameters as passed to main().
     */
    public Opts(String[] args) {
        arguments = new LinkedList<>();
        for (String s : args) {
            arguments.add(new Argument(s));
        }
        options = new LinkedList<>();
    }

    /**
     * Add recognized option.
     * 
     * @param name
     *            Option name (e.g. <code>--dir</code>).
     */
    public void addOption(String name) {
        String names[] = new String[1];
        names[0] = name;
        addOption(names);
    }

    /**
     * Add recognized option (with aliases).
     * 
     * @param names
     *            Option names.
     */
    public void addOption(String[] names) {
        options.add(new Option(names));
    }

    /**
     * Tells whether given option is present on the command-line.
     * 
     * @param name
     *            Option name.
     * @return Whether this option (or its alias) is present in the arguments.
     */
    public boolean isPresent(String name) {
        Option opt = findOption(name);
        if (opt == null) {
            return false;
        }

        for (Argument arg : arguments) {
            if (opt.matches(arg.argument)) {
                arg.setProcessed();
                return true;
            }
        }

        return false;
    }

    /**
     * Get value for given option.
     * 
     * @param opt
     *            Option name (or alias).
     * @return Option value or null when not present.
     */
    public String getValue(String opt) {
        ListIterator<Argument> pos = findInArguments(opt);
        if (!pos.hasNext()) {
            return null;
        }
        Argument arg = pos.next();
        arg.setProcessed();

        if (!pos.hasNext()) {
            return null;
        }
        arg = pos.next();
        arg.setProcessed();
        return arg.argument;
    }

    /**
     * Get value for given option or default when not present.
     * 
     * @param opt
     *            Option name (alias).
     * @param defaults
     *            Default value when option not present.
     * @return Always a non-null value (unless @p defaults is null).
     */
    public String getValue(String opt, String defaults) {
        String val = getValue(opt);
        if (val == null) {
            return defaults;
        } else {
            return val;
        }
    }

    /**
     * Get list of remaining arguments (usually filenames).
     * 
     * @return List of arguments no one asked for via getValue() or isPresent().
     */
    public List<String> getRemainingArguments() {
        List<String> remainings = new ArrayList<>();
        for (Argument arg : arguments) {
            if (arg.unprocessed()) {
                remainings.add(arg.argument);
            }
        }

        return remainings;
    }

    protected ListIterator<Argument> findInArguments(String optionName) {
        Option opt = findOption(optionName);
        ListIterator<Argument> it = arguments.listIterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (opt == null) {
                continue;
            }

            if (opt.matches(arg.argument)) {
                it.previous();
                return it;
            }
        }

        return it;
    }

    protected Option findOption(String name) {
        for (Option opt : options) {
            if (opt.matches(name)) {
                return opt;
            }
        }
        return null;
    }
}

enum ArgumentKind {
    NO_ARGUMENT,
    MANDATORY_ARGUMENT
};

class Option {
    String[] names;

    Option(String[] n) {
        names = n;
    }

    boolean matches(String opt) {
        for (String n : names) {
            if (n.equals(opt)) {
                return true;
            }
        }
        return false;
    }

}
