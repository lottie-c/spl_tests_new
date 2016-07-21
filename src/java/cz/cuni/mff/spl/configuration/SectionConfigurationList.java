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
package cz.cuni.mff.spl.configuration;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * This enum is to list all section factories (validators) so INI editor in
 * Eclipse does not have to hard-coded.
 * 
 * <p>
 * The purpose of factories is so when one has the name of an INI section in the
 * file he can iterate over all known {@link ISectionFactory}ies and call
 * {@link ISectionFactory#createFromSectionName(String, java.util.Map)}. If this
 * method returns non-null object he has found the factory containing the
 * description and the validator for the section.
 * 
 * @see ISectionFactory
 * 
 * @author Frantisek Haas
 * 
 */
public enum SectionConfigurationList {

    ACCESS, DEPLOYMENT, EVALUATOR;

    public List<ISectionFactory> getSectionFactory() {
        switch (this) {
            case ACCESS:
                return SplAccessConfiguration.getSectionFactories();
            case DEPLOYMENT:
                return SplDeploymentConfiguration.getSectionFactories();
            case EVALUATOR:
                return SplEvaluatorConfiguration.getSectionFactories();
            default:
                return new LinkedList<ISectionFactory>();
        }
    }
}
