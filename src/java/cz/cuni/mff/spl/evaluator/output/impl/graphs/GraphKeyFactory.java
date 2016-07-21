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

package cz.cuni.mff.spl.evaluator.output.impl.graphs;

import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;

/**
 * A factory for creating GraphKey objects.
 * 
 * @author Martin Lacina
 */
public class GraphKeyFactory {

    /**
     * Creates a new GraphKey object.
     * 
     * @param relatedTo
     *            The related to object.
     * @param graphType
     *            The graph definition.
     * @return the object
     */
    public static Object createGraphKey(Object relatedTo, GraphDefinition graphType) {
        return new GraphKeyImpl(relatedTo, graphType);
    }

    /**
     * The Class GraphKeyImpl.
     */
    private static class GraphKeyImpl {

        /** The related to object. */
        private final Object relatedTo;

        /** The graph type. */
        private final Object graphType;

        /**
         * Instantiates a new graph key implementation instance.
         * 
         * @param relatedTo
         *            The related to object.
         * @param graphType
         *            The graph type.
         */
        public GraphKeyImpl(Object relatedTo, Object graphType) {
            if (relatedTo == null) {
                throw new IllegalArgumentException("Argument 'relatedTo' can not be null.");
            }
            if (graphType == null) {
                throw new IllegalArgumentException("Argument 'graphType' can not be null.");
            }

            this.relatedTo = relatedTo;
            this.graphType = graphType;
        }

        @Override
        public int hashCode() {
            return this.relatedTo.hashCode() + this.graphType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GraphKeyImpl) {
                GraphKeyImpl second = (GraphKeyImpl) obj;
                return this.graphType.equals(second.graphType) && this.relatedTo.equals(second.relatedTo);
            } else {
                return false;
            }
        }
    }
}
