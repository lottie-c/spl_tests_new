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

import cz.cuni.mff.spl.annotation.Operator;

/**
 * The statistical result can be one of:
 * 
 * <ul>
 * <li>{@link #OK}</li>
 * <ul>
 * <li>comparison passed statistical t-test</li>
 * <li>logical operation satisfied</li>
 * </ul>
 * 
 * <li>{@link #FAILED} - comparison failed statistical t-test.</li>
 * <ul>
 * <li>comparison failed statistical t-test</li>
 * <li>logical operation not satisfied</li>
 * </ul>
 * 
 * <li>{@link #NOT_COMPUTED}</li>
 * <ul>
 * <li>comparison was not computed, as at least one sample is missing</li>
 * <li>logical operation can not be decided</li>
 * </ul>
 * </ul>
 * 
 * @author Martin Lacina
 */
public enum StatisticalResult {

    /**
     * Comparison passed statistical t-test.
     */
    OK {
        @Override
        public String toString() {
            return "OK";
        }

        @Override
        public String toPrintableString() {
            return "OK";
        }
    },

    /**
     * Comparison failed statistical t-test.
     */
    FAILED {
        @Override
        public String toString() {
            return "FAILED";
        }

        @Override
        public String toPrintableString() {
            return "FAILED";
        }
    },

    /**
     * Comparison was not computed, as at least one sample is missing.
     */
    NOT_COMPUTED {
        @Override
        public String toString() {
            return "NOT_COMPUTED";
        }

        @Override
        public String toPrintableString() {
            return "NOT COMPUTED";
        }
    };

    /**
     * To printable string.
     * 
     * @return The printable string.
     */
    public abstract String toPrintableString();

    /**
     * Combines two statistical result values using three-value logic with
     * implication definition by <strong>Kleene logic</strong>.
     * <p>
     * Combination values are defined as follow:
     * 
     * <code>
[          OK]   AND [          OK] = [          OK]<br>
[          OK]   AND [      FAILED] = [      FAILED]<br>
[          OK]   AND [NOT_COMPUTED] = [NOT_COMPUTED]<br>
[      FAILED]   AND [          OK] = [      FAILED]<br>
[      FAILED]   AND [      FAILED] = [      FAILED]<br>
[      FAILED]   AND [NOT_COMPUTED] = [      FAILED]<br>
[NOT_COMPUTED]   AND [          OK] = [NOT_COMPUTED]<br>
[NOT_COMPUTED]   AND [      FAILED] = [      FAILED]<br>
[NOT_COMPUTED]   AND [NOT_COMPUTED] = [NOT_COMPUTED]<br>
[          OK]    OR [          OK] = [          OK]<br>
[          OK]    OR [      FAILED] = [          OK]<br>
[          OK]    OR [NOT_COMPUTED] = [          OK]<br>
[      FAILED]    OR [          OK] = [          OK]<br>
[      FAILED]    OR [      FAILED] = [      FAILED]<br>
[      FAILED]    OR [NOT_COMPUTED] = [NOT_COMPUTED]<br>
[NOT_COMPUTED]    OR [          OK] = [          OK]<br>
[NOT_COMPUTED]    OR [      FAILED] = [NOT_COMPUTED]<br>
[NOT_COMPUTED]    OR [NOT_COMPUTED] = [NOT_COMPUTED]<br>
[          OK]  IMPL [          OK] = [          OK]<br>
[          OK]  IMPL [      FAILED] = [      FAILED]<br>
[          OK]  IMPL [NOT_COMPUTED] = [NOT_COMPUTED]<br>
[      FAILED]  IMPL [          OK] = [          OK]<br>
[      FAILED]  IMPL [      FAILED] = [          OK]<br>
[      FAILED]  IMPL [NOT_COMPUTED] = [          OK]<br>
[NOT_COMPUTED]  IMPL [          OK] = [          OK]<br>
[NOT_COMPUTED]  IMPL [      FAILED] = [NOT_COMPUTED]<br>
[NOT_COMPUTED]  IMPL [NOT_COMPUTED] = [NOT_COMPUTED]<br>
     * </code>
     * 
     * @param operator
     *            The operator.
     * @param leftOperand
     *            The left operand.
     * @param rightOperand
     *            The right operand.
     * @return The statistical result.
     * 
     * @see http://en.wikipedia.org/wiki/Three-valued_logic#Kleene_logic
     */
    public static StatisticalResult combine(Operator operator, StatisticalResult leftOperand, StatisticalResult rightOperand) {
        switch (operator) {
            case AND:
                if (leftOperand.equals(StatisticalResult.FAILED) || rightOperand.equals(StatisticalResult.FAILED)) {
                    return StatisticalResult.FAILED;
                } else if (leftOperand.equals(StatisticalResult.NOT_COMPUTED) || rightOperand.equals(StatisticalResult.NOT_COMPUTED)) {
                    return StatisticalResult.NOT_COMPUTED;
                } else {
                    return StatisticalResult.OK;
                }
            case IMPL:
                switch (leftOperand) {
                    case OK:
                        return rightOperand;
                    case FAILED:
                        return OK;
                    case NOT_COMPUTED:
                        if (rightOperand.equals(OK)) {
                            return OK;
                        } else {
                            return NOT_COMPUTED;
                        }
                }
                assert (false);
                throw new IllegalStateException(String.format("Unexpected statistical results case: [%s] %s [%s]", leftOperand, operator, rightOperand));
            case OR:
                if (leftOperand.equals(StatisticalResult.OK) || rightOperand.equals(StatisticalResult.OK)) {
                    return StatisticalResult.OK;
                } else if (leftOperand.equals(StatisticalResult.FAILED) && rightOperand.equals(StatisticalResult.FAILED)) {
                    return StatisticalResult.FAILED;
                } else {
                    return StatisticalResult.NOT_COMPUTED;
                }
            default:
                assert (false);
                throw new IllegalStateException("Unexpected logical operator " + operator.toString());
        }

    }

}
