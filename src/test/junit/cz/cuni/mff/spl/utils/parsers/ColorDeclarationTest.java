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
package cz.cuni.mff.spl.utils.parsers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.awt.Color;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Tests parser on graph declaration.
 * 
 * @author Martin Lacina
 * 
 */
public class ColorDeclarationTest {

    private static final SplLog LOG = SplLogger.getLogger(ColorDeclarationTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    /**
     * Runs test on method alias.
     * 
     * @param index
     *            Index to {@link #methodAliasesTests}.
     * @throws ParseException
     */
    private Color parseColorDefinition(String str) throws ParseException {
        LOG.info(str);
        Color definition = ColorParser.ParseColorDefinition(str);
        return definition;
    }

    private void validate(int red, int green, int blue, Color parsed) {
        assertEquals(red, parsed.getRed());
        assertEquals(green, parsed.getGreen());
        assertEquals(blue, parsed.getBlue());
    }

    private void testColorDefinition(int red, int green, int blue) throws ParseException {
        String testValue = String.format("rgb(%d, %d, %s)", red, green, blue);
        Color color = parseColorDefinition(testValue);
        validate(red, green, blue, color);
    }

    private void testColorDefinition(String testColorName) throws ParseException {
        Color color = parseColorDefinition(testColorName);
        Assert.assertNotNull(color);
    }

    @Test
    public void parseRGB() throws ParseException {
        testColorDefinition(10, 20, 30);
    }

    @Test
    public void testColorInstances() throws ParseException {
        String[] colors = new String[] {
                "red", "orange", "yellow", "green", "cyan", "blue", "violet", "magenta", "white", "gray", "black"
        };

        for (String c : colors) {
            testColorDefinition(c);
        }
    }

    @Test
    public void parseBadRGB() throws ParseException {
        try {
            testColorDefinition(-10, 2000, 30);
        } catch (ParseException | TokenMgrError e) {
            return;
        }
        fail();
    }

    @Test
    public void parseBadDeclaration() {
        String testValue = "no rgb declaration";
        try {
            parseColorDefinition(testValue);
        } catch (ParseException | TokenMgrError e) {
            return;
        }
        fail();
    }

    @Test
    public void parseEmptyDeclaration() {
        String testValue = "";
        try {
            parseColorDefinition(testValue);
        } catch (ParseException e) {
            return;
        }
        fail();
    }

    @Test
    public void parseUnknownColorDeclaration() {
        String testValue = "alfa beta gama delta";
        try {
            parseColorDefinition(testValue);
        } catch (ParseException | TokenMgrError e) {
            return;
        }
        fail();
    }

    @Test
    public void testByteValueRule() throws ParseException {
        for (int b = 0; b < 256; ++b) {
            assertEquals(Integer.toString(b), b, new ColorParser(Integer.toString(b)).byteValue());
        }
    }
}
