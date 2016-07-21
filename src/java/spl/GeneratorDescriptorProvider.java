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
package spl;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.spl.formula.context.ParserContext;

/**
 * <p>
 * This class lists all {@link IGeneratorDescriptor}s for all integrated
 * generators.
 * 
 * @author Frantisek Haas
 * 
 */
public class GeneratorDescriptorProvider {

    /**
     * <p>
     * Lists all the descriptors.
     * 
     * @return
     */
    public static List<IGeneratorDescriptor> getDescriptors() {
        List<IGeneratorDescriptor> descriptors = new LinkedList<>();

        descriptors.add(new GeneratorDescriptorProjectProxy(DoubleUniform.getDescriptor()));
        descriptors.add(new GeneratorDescriptorProjectProxy(DoubleExponential.getDescriptor()));
        descriptors.add(new GeneratorDescriptorProjectProxy(DoubleGaussian.getDescriptor()));

        descriptors.add(new GeneratorDescriptorProjectProxy(IntegerPermutation.getDescriptor()));
        descriptors.add(new GeneratorDescriptorProjectProxy(IntegerUniform.getDescriptor()));

        descriptors.add(new GeneratorDescriptorProjectProxy(LongUniform.getDescriptor()));

        return descriptors;

    }

    /**
     * The IGeneratorDescriptor proxy adding project declaration to proposal.
     * 
     * @author Martin Lacina
     */
    private static class GeneratorDescriptorProjectProxy implements IGeneratorDescriptor {

        /** The descriptor. */
        private final IGeneratorDescriptor descriptor;

        /**
         * Instantiates a new generator descriptor project proxy.
         * 
         * @param descriptor
         *            The descriptor.
         */
        public GeneratorDescriptorProjectProxy(IGeneratorDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String getProposal() {
            return String.format("%s:%s", ParserContext.SPL_INTEGRATED_PROJECT, descriptor.getProposal());
        }

        @Override
        public String getDescription() {
            return descriptor.getDescription();
        }

    }
}
