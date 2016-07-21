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
package cz.cuni.mff.spl.annotation;

/**
 * Logic operator connecting two comparisons.
 * 
 * @author Frantisek Haas
 * @author Jaroslav Kotrc
 * @author Jiri Daniel
 * @author Martin Lacina
 */
public enum Operator {

    AND {
        @Override
        public String toString() {
            return "AND";
        }

        @Override
        public boolean evaluate(boolean leftOperand, boolean rightOperand) {
            return leftOperand && rightOperand;
        }

    },
    OR {
        @Override
        public String toString() {
            return "OR";
        }

        @Override
        public boolean evaluate(boolean leftOperand, boolean rightOperand) {
            return leftOperand || rightOperand;
        }

    },
    IMPL {
        @Override
        public String toString() {
            return "IMPL";
        }

        @Override
        public String getStringForOutput() {
            return "===>";
        }

        @Override
        public boolean evaluate(boolean leftOperand, boolean rightOperand) {
            return !(!leftOperand && rightOperand);
        }

    };

    /**
     * Evaluates operator based on operands.
     * 
     * @param leftOperand
     *            The left operand.
     * @param rightOperand
     *            The right operand.
     * @return Truth value of operator based on provided operands.
     */
    public abstract boolean evaluate(boolean leftOperand, boolean rightOperand);

    /**
     * Gets string to output for humans.
     * 
     * @return The human readable string.
     */
    public String getStringForOutput() {
        return toString();
    }

}
