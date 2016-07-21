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

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import cz.cuni.mff.spl.deploy.build.exception.CompileException;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class provides simple static API for java source code compilation.
 * 
 * @author Frantisek Haas
 * 
 */
public class Compiler {

    private static final SplLog logger = SplLogger.getLogger(Compiler.class);

    /**
     * <p>
     * Checks if Java compiler is present.
     * 
     * @return
     *         <p>
     *         True if compiler is found. False otherwise.
     */
    public static boolean isCompilerPresent() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return (compiler != null);
    }

    /**
     * Obtains java compiler for compiling source code.
     * 
     * @return
     *         System java compiler.
     * @throws CompileException
     *             If system java compiler is not present, probably not running
     *             on JDK but JRE.
     */
    private static JavaCompiler getCompiler()
            throws CompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompileException("Could not obtain system java compiler, probably running on JRE not JDK.");
        } else {
            return compiler;
        }
    }

    /**
     * Prepares options for compiling. Namely converts class paths to use-able
     * data structure.
     * 
     * @return
     */
    private static Iterable<String> getOptions(Iterable<String> classPaths) {
        StringBuilder classPathBuilder = new StringBuilder();
        for (String classPath : classPaths) {
            classPathBuilder.append(File.pathSeparator + classPath);
        }

        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList("-cp", classPathBuilder.toString()));
        return options;
    }

    /**
     * Prepares data structure containing compilation unit.
     * 
     * @return
     * @throws CompileException
     */
    private static Iterable<? extends JavaFileObject> getCompilationUnits(JavaCompiler compiler, Iterable<String> sourcePaths) {
        Locale defaultLocale = null;
        Charset defaultCharset = null;
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, defaultLocale, defaultCharset);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(sourcePaths);

        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            logger.error(d.toString());
        }

        return compilationUnits;
    }

    /**
     * Compiles java source located on source paths and uses class paths for
     * dependencies.
     * 
     * @param sourcePaths
     * @param classPaths
     * @throws CompileException
     */
    public static void call(List<String> sourcePaths, List<String> classPaths)
            throws CompileException {

        JavaCompiler compiler = getCompiler();

        Locale defaultLocale = null;
        Charset defaultCharset = null;
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, defaultLocale, defaultCharset);

        Iterable<String> options = getOptions(classPaths);
        Iterable<? extends JavaFileObject> compilationUnits = getCompilationUnits(compiler, sourcePaths);

        Writer defaultToStdErr = null;
        Iterable<String> noAnnotationClasses = null;
        JavaCompiler.CompilationTask task = compiler.getTask(defaultToStdErr, fileManager, diagnostics, options, noAnnotationClasses, compilationUnits);

        if (!task.call()) {
            for (String sourcePath : sourcePaths) {
                logger.error("Source path: %s.", sourcePath);
            }
            for (String classPath : classPaths) {
                logger.error("Class path: %s.", classPath);
            }
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                logger.error("Diagnostics:\n%s", d.toString());
            }
            throw new CompileException("Failed to compile source code.");
        }
    }
}
