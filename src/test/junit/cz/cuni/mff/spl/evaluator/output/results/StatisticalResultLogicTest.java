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
package cz.cuni.mff.spl.evaluator.output.results;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Just validation of three value logic in {@link StatisticalResult}.
 * 
 * @author Martin Lacina
 * 
 */
public class StatisticalResultLogicTest {

    private final SplLog logger = SplLogger.getLogger(StatisticalResultLogicTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void testLogicalOperationsWithStatisticalResult() {

        for (Operator operator : Operator.values()) {
            for (StatisticalResult leftOperand : StatisticalResult.values()) {
                for (StatisticalResult rightOperand : StatisticalResult.values()) {
                    StatisticalResult result = StatisticalResult.combine(operator, leftOperand, rightOperand);
                    logger.trace(String.format("[%12s] %5s [%12s] = [%12s]", leftOperand, operator, rightOperand, result));
                }
            }
        }
    }

    @Test
    public void testLogicalOperationsWithStatisticalResult2() {
        for (StatisticalResult leftOperand : StatisticalResult.values()) {
            for (StatisticalResult rightOperand : StatisticalResult.values()) {
                for (Operator operator : Operator.values()) {
                    if (!operator.equals(Operator.IMPL)) {
                        StatisticalResult result1 = StatisticalResult.combine(operator, leftOperand, rightOperand);
                        StatisticalResult result2 = StatisticalResult.combine(operator, rightOperand, leftOperand);
                        if (!result1.equals(result2)) {
                            logger.trace(String.format("[%12s] %5s [%12s] = [%12s] != [%12s] = [%12s] %5s [%12s] ",
                                    leftOperand, operator, rightOperand, result1,
                                    result2, rightOperand, operator, leftOperand));
                            throw new IllegalStateException();
                        }

                    }
                }
            }
        }
    }

}
