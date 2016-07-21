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
package cz.cuni.mff.spl.reflection;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.spl.StringFilter;

/**
 * Filter for classnames using wild cards.
 * 
 * The filtering uses simple pattern matching.
 * When the name ends with <code>.**</code>, the prefix (i.e. everything
 * prior the double asterisk) is taken as a package name and all classes
 * in that package (even in subpackages) are matched.
 * When the name ends with <code>.*</code>, only classes in that package
 * are taken (i.e. no subpackage classes).
 * Otherwise, the name is treated as a fully qualified class name.
 */
public class ClassNamesByPaternFilter implements StringFilter {

    /**
     * Factory to create filter from list of patterns.
     * 
     * @param patterns
     *            Patterns to apply.
     * @return New filter.
     */
    public static ClassNamesByPaternFilter createFromPatternList(String[] patterns) {
        ClassNamesByPaternFilter filter = new ClassNamesByPaternFilter();
        for (String p : patterns) {
            filter.addPattern(p);
        }
        return filter;
    }

    private final List<ClassNamePattern> patterns;

    /** Filter accepting all classes. */
    public ClassNamesByPaternFilter() {
        patterns = new LinkedList<>();
    }

    /**
     * Add search pattern.
     * 
     * @param pat
     *            The pattern.
     */
    public void addPattern(String pat) {
        patterns.add(new ClassNamePattern(pat));
    }

    /** The matching function (interface implementation). */
    @Override
    public boolean match(String s) {
        for (ClassNamePattern p : patterns) {
            if (p.match(s)) {
                return true;
            }
        }
        return false;
    }

    private enum ClassNamePatternKind {
        THIS_PACKAGE,
        ALL_SUBPACKAGES,
        EVERYTHING,
        THIS_CLASS
    };

    private class ClassNamePattern {
        private String               name;
        private ClassNamePatternKind kind;

        public ClassNamePattern(String s) {
            if (s.endsWith(".*")) {
                name = s.substring(0, s.length() - 2);
                kind = ClassNamePatternKind.THIS_PACKAGE;
            } else if (s.endsWith(".**")) {
                name = s.substring(0, s.length() - 3);
                kind = ClassNamePatternKind.ALL_SUBPACKAGES;
            } else if (s.equals("*")) {
                name = null;
                kind = ClassNamePatternKind.EVERYTHING;
            } else {
                name = s;
                kind = ClassNamePatternKind.THIS_CLASS;
            }
        }

        public boolean match(String s) {
            switch (kind) {
                case THIS_CLASS:
                    return name.equals(s);
                case EVERYTHING:
                    return true;
                case THIS_PACKAGE:
                    if (s.startsWith(name + ".")) {
                        return !s.substring(name.length() + 1).contains(".");
                    } else {
                        return false;
                    }
                case ALL_SUBPACKAGES:
                    return s.startsWith(name + ".");
                default:
                    assert (false);
                    return false;
            }
        }
    }

}
