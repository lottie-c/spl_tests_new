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
package cz.cuni.mff.spl.formula;

import cz.cuni.mff.spl.formula.parser.ParseException;
import cz.cuni.mff.spl.formula.parser.TokenMgrError;

/**
 * Exception thrown by formula parser. Class can not be placed in package
 * cz.cuni.mff.spl.formula.parser because it is cleaned automatically by ant.
 * 
 * @author Jaroslav Kotrc
 * 
 */
public class ParserException extends ParseException {

    private static final long serialVersionUID = -6029272989327611238L;

    /** Constructs a new exception with {@code null} as its detail message. */
    public ParserException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            the detail message set to the exception
     */
    public ParserException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause
     * 
     * @param cause
     *            the exception cause
     */
    public ParserException(Throwable cause) {
        super(cause.getMessage());
        initCause(cause);
    }

    /**
     * Constructs a new exception with the specified cause
     * 
     * @param cause
     *            the exception cause
     */
    public ParserException(TokenMgrError cause) {
        super(cause.getMessage());
        initCause(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message set to the exception
     * @param cause
     *            the exception cause
     */
    public ParserException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
