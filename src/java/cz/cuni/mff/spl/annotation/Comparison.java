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

import java.util.Map;

import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Class representing comparison of two measurements storing also comparing sign
 * and multipliers of the measurements.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Comparison extends Formula {

    private Measurement leftMeasurement;
    private Lambda      leftLambda;
    private Sign        sign;
    private Measurement rightMeasurement;
    private Lambda      rightLambda;

    /** The interval for {@link Sign.EQI}. */
    private Double      interval;

    @Override
    public Formula expand(ParserContext context, int[] valuesArr, Map<String, Integer> position) {
        Lambda newLeftLambda = null;
        if (leftLambda != null) {
            newLeftLambda = leftLambda.expand(context, valuesArr, position);
        }
        Lambda newRightLambda = null;
        if (rightLambda != null) {
            newRightLambda = rightLambda.expand(context, valuesArr, position);
        }
        return new Comparison(leftMeasurement.expand(context, valuesArr, position), newLeftLambda, sign,
                rightMeasurement.expand(context, valuesArr, position), newRightLambda, interval);
    }

    public Comparison() {

    }

    public Comparison(Measurement leftMeasurement, Lambda leftLambda, Sign sign, Measurement rightMeasurement, Lambda rightLambda) {
        this.leftMeasurement = leftMeasurement;
        this.leftLambda = leftLambda;
        this.sign = sign;
        this.rightMeasurement = rightMeasurement;
        this.rightLambda = rightLambda;
    }

    public Comparison(Measurement leftMeasurement, Lambda leftLambda, Sign sign, Measurement rightMeasurement, Lambda rightLambda, Double interval) {
        this.leftMeasurement = leftMeasurement;
        this.leftLambda = leftLambda;
        this.sign = sign;
        this.rightMeasurement = rightMeasurement;
        this.rightLambda = rightLambda;
        this.interval = interval;
    }

    public Measurement getLeftMeasurement() {
        return leftMeasurement;
    }

    public void setLeftMeasurement(Measurement leftMeasurement) {
        this.leftMeasurement = leftMeasurement;
    }

    public Lambda getLeftLambda() {
        return leftLambda;
    }

    public void setLeftLambda(Lambda leftLambda) {
        this.leftLambda = leftLambda;
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public Measurement getRightMeasurement() {
        return rightMeasurement;
    }

    public void setRightMeasurement(Measurement rightMeasurement) {
        this.rightMeasurement = rightMeasurement;
    }

    public Lambda getRightLambda() {
        return rightLambda;
    }

    public void setRightLambda(Lambda rightLambda) {
        this.rightLambda = rightLambda;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftMeasurement == null) ? 0 : leftMeasurement.hashCode());
        result = prime * result + ((leftLambda == null) ? 0 : leftLambda.hashCode());
        result = prime * result + ((rightMeasurement == null) ? 0 : rightMeasurement.hashCode());
        result = prime * result + ((rightLambda == null) ? 0 : rightLambda.hashCode());
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());

        // hashCode for ENUM is NOT stable between JVM instances
        result = prime * result + ((sign == null) ? 0 : sign.ordinal());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Comparison other = (Comparison) obj;
        return EqualsUtils.safeEquals(this.sign, other.sign)
                && EqualsUtils.safeEquals(this.leftMeasurement, other.leftMeasurement)
                && EqualsUtils.safeEquals(this.leftLambda, other.leftLambda)
                && EqualsUtils.safeEquals(this.rightMeasurement, other.rightMeasurement)
                && EqualsUtils.safeEquals(this.rightLambda, other.rightLambda);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s, %s) %s", leftMeasurement, sign, leftLambda, rightLambda, rightMeasurement);
    }
}
