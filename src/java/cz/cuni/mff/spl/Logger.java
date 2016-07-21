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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import cz.cuni.mff.spl.utils.logging.MultiOutputLogger;

/**
 * <p>
 * This class is used to capture SPL framework log into a file using
 * {@link MultiOutputLogger}. The log is permanently stored in the evaluation
 * where Eclipse plug-in can retrieve it.
 * </p>
 * 
 * @author Frantisek Haas
 * 
 */
public class Logger implements Appendable, AutoCloseable {

    private final File        file;
    private final PrintStream out;

    public Logger(File file, boolean doBuffer)
            throws FileNotFoundException {
        this.file = file;
        this.out = new PrintStream(new FileOutputStream(file));
    }

    @Override
    public void close() {
        out.close();
    }

    public InputStream getInputStream()
            throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    public Appendable append(CharSequence csq)
            throws IOException {
        out.println(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end)
            throws IOException {
        out.println(csq.subSequence(start, end));
        return this;
    }

    @Override
    public Appendable append(char c)
            throws IOException {
        out.println(c);
        return this;
    }
}
