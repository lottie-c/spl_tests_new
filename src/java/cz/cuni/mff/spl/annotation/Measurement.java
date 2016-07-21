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

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Represents single measurement of method.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Measurement extends AbstractXmlTransformationReference implements AnnotationToString {

    /** The method. */
    private Method                 method;

    /** The generator. */
    private Generator              generator;

    /** The machine. */
    private Machine                machine;

    /** The variables for generator. */
    private Variable               variable;

    /** The measurement state. Has no effect on hashcode and equals. */
    private final MeasurementState measurementState = new MeasurementState();

    public Measurement() {
    }

    public Measurement(Method method, Generator generator, Machine machine) {
        this.method = method;
        this.generator = generator;
        this.machine = machine;
    }

    public Measurement(Method method, Generator generator, ParserContext context) {
        this(method, generator, new ParserVariable(), context);
    }

    public Measurement(Method method, Generator generator, ParserVariable variable, ParserContext context) {
        this.method = method;
        this.generator = generator;
        this.machine = context.getMachine();
        if (variable == null) {
            variable = new ParserVariable();
        }
        this.variable = variable;
    }

    /**
     * Adding variable into list. This can be done only before transforming
     * into final structure.
     * 
     * @param variable
     *            Variable to add into list of variables.
     */
    public void addVariable(String variable) {
        if (this.variable == null) {
            this.variable = new ParserVariable();
        }
        ((ParserVariable) this.variable).addVariable(variable);
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public MeasurementState getMeasurementState() {
        return measurementState;
    }

    public void setMeasurementState(MeasurementState measurementState) {
        this.measurementState.setOk(measurementState.isOk());
        this.measurementState.setLastPhase(measurementState.getLastPhase());
        this.measurementState.setMessage(measurementState.getMessage());
    }

    /** ID has no influence to hash code. */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((generator == null) ? 0 : generator.hashCode());
        // lets omit machine in hash code - we use (Linked)HashSets and they
        // don't like when hash of stored item is changed - equality gets
        // broken.
        // result = prime * result + ((machine == null) ? 0 :
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((variable == null) ? 0 : variable.hashCode());
        return result;
    }

    /** ID has no influence to equality. */
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
        Measurement other = (Measurement) obj;
        return EqualsUtils.safeEquals(this.machine, other.machine)
                && EqualsUtils.safeEquals(this.generator, other.generator)
                && EqualsUtils.safeEquals(this.method, other.method)
                && EqualsUtils.safeEquals(this.variable, other.variable);
    }

    /**
     * Expanding variables created by parser with concrete values used in
     * measurement and returning expanded measurement. If the measurement
     * already exists in the context, then it returns reference to existing one
     * instead of creating new one. Also add method and generator used for
     * measurement and measurement itself to parser context if it is not present
     * in the context.
     * 
     * @param context
     *            Parser context for storing measurements.
     * @param valuesArr
     *            Array of concrete variable values
     * @param position
     *            Mapping variable name to index of variable value in
     *            valuesArr
     * @return Expanded measurement
     */
    public Measurement expand(ParserContext context, int[] valuesArr, Map<String, Integer> position) {
        Measurement measurement;
        measurement = new Measurement(context.addMethod(method), context.addGenerator(generator), machine);
        measurement.variable = new ExpandedVariable(context, valuesArr, position, (ParserVariable) variable);
        return context.addMeasurement(measurement);
    }

    @Override
    public String toString() {
        return getDeclarationString();
    }

    @Override
    public String getDeclarationString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(method.getDeclarationString());
        buffer.append("[");
        buffer.append(generator.getDeclarationString());
        buffer.append("]");
        buffer.append(variable.getDeclarationString());
        return buffer.toString();
    }

    @Override
    public String getIdentificationString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(Integer.toHexString(this.hashCode()));
        buffer.append("|");
        if (machine != null) {
            buffer.append(machine.getIdentificationString());
        }
        buffer.append("|");
        buffer.append(method.getIdentificationString());
        buffer.append("[");
        buffer.append(generator.getIdentificationString());
        buffer.append("]");
        buffer.append(variable.getIdentificationString());
        return buffer.toString();
    }
}
