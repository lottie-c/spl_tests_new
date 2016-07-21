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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition.DataClipType;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition.GraphType;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Tests parser on graph declaration.
 * 
 * @author LacinaM
 * 
 */
public class GraphDeclarationTest {

    private static final SplLog LOG = SplLogger.getLogger(GraphDeclarationTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    private GraphDefinition testParseGraphDefinition(String str) throws ParseException {
        LOG.info(str);
        GraphDefinition definition = GraphDefinitionParser.ParseGraphDefinition(str);
        Assert.assertNotNull(definition);
        return definition;
    }

    @Test
    public void parse3SigmaClippedHistogram() throws ParseException {
        String testValue = "Histogram(Sigma, 3, 2)";
        GraphDefinition d = testParseGraphDefinition(testValue);
        assertNotNull(d);
        assertEquals(d.getBasicGraphType(), GraphType.Histogram);
        assertEquals(d.getDataClipType(), DataClipType.Sigma);
        assertEquals(d.getDataClipParameters().size(), 2);
        assertEquals(d.getSigmaMultiplier(), new Double(3));
        assertEquals(d.getSigmaMaxIteration(), 2);
    }

    @Test
    public void parseQuantileClippedTimeDiagram() throws ParseException {
        String testValue = "TimeDiagram(Quantile, 10, 20)";
        GraphDefinition d = testParseGraphDefinition(testValue);
        assertNotNull(d);
        assertEquals(d.getBasicGraphType(), GraphType.TimeDiagram);
        assertEquals(d.getDataClipType(), DataClipType.Quantile);
        assertEquals(d.getDataClipParameters().size(), 2);
        assertEquals(d.getQuantileLowerClip(), Double.valueOf(10));
        assertEquals(d.getQuantileUpperClip(), Double.valueOf(20));
    }

    @Test
    public void parseNotClippedDensityComparison() throws ParseException {
        String testValue = "DensityComparison(None)";
        GraphDefinition d = testParseGraphDefinition(testValue);
        assertNotNull(d);
        assertEquals(d.getBasicGraphType(), GraphType.DensityComparison);
        assertEquals(d.getDataClipType(), DataClipType.None);
        assertEquals(d.getDataClipParameters().size(), 0);
    }

    @Test
    public void parseBadDeclaration() {
        String testValue = "Density Comparison(None)";
        try {
            testParseGraphDefinition(testValue);
        } catch (ParseException e) {
            return;
        }
        fail();
    }

    @Test
    public void parseEmptyDeclaration() {
        String testValue = "";
        try {
            testParseGraphDefinition(testValue);
        } catch (ParseException e) {
            return;
        }
        fail();
    }
}
