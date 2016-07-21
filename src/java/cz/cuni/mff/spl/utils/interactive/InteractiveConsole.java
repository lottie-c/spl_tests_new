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
package cz.cuni.mff.spl.utils.interactive;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class InteractiveConsole implements InteractiveInterface {

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public String getString(String prompt) {
        System.out.println("=====Prompt=====");

        String input = null;

        if (System.console() != null) {
            input = System.console().readLine(prompt);
        } else {
            System.out.println(prompt);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                input = in.readLine();
            } catch (Exception e) {
                input = null;
            }
        }

        return input;
    }

    @Override
    public String getMaskedString(String prompt) {
        System.out.println("=====Prompt=====");

        String input = null;

        if (System.console() != null) {
            char[] password = System.console().readPassword(prompt);
            if (password != null) {
                input = new String(password);
            } else {
                input = null;
            }
        } else {
            System.out.println(prompt);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                input = in.readLine();
            } catch (Exception e) {
                input = null;
            }
        }

        return input;
    }

    @Override
    public Boolean getBoolean(String prompt) {
        System.out.println("=====Prompt=====");

        String input = null;

        if (System.console() != null) {
            input = System.console().readLine(prompt);

        } else {
            System.out.println(prompt);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                input = in.readLine();

            } catch (Exception e) {
                input = null;
            }
        }

        if (input != null) {
            return Boolean.valueOf(input);
        } else {
            return null;
        }
    }
}
