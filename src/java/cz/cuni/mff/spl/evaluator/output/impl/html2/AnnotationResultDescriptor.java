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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.util.ArrayList;

import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The annotation evaluation result descriptor for XSLT transformation.
 * 
 * @author Martin Lacina
 */
public class AnnotationResultDescriptor extends OutputResultDescriptor {

    /** The annotation evaluation result. */
    private final AnnotationEvaluationResult annotationEvaluationResult;

    /**
     * Gets the annotation evaluation result.
     * 
     * @return The annotation evaluation result.
     */
    public AnnotationEvaluationResult getAnnotationEvaluationResult() {
        return annotationEvaluationResult;
    }

    /** The annotation validation flags. */
    private final AnnotationValidationFlags annotationValidationFlags = new AnnotationValidationFlags();

    /**
     * Gets the annotation validation flags.
     * 
     * @return The annotation validation flags.
     */
    public AnnotationValidationFlags getAnnotationValidationFlags() {
        return annotationValidationFlags;
    }

    /**
     * Instantiates a new annotation result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param annotationEvaluationResult
     *            The annotation evaluation result.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public AnnotationResultDescriptor(Info info, ConfigurationBundle configuration, AnnotationEvaluationResult annotationEvaluationResult,
            StatisticValueChecker checker, BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks, AnnotationValidationFlags globalAliasesSummary) {
        super(info, configuration, outputLinks, globalAliasesSummary);

        this.annotationEvaluationResult = annotationEvaluationResult;

        this.annotationValidationFlags.setFlags(annotationEvaluationResult);
    }

    /**
     * The annotation validation flags.
     * 
     * @author Martin Lacina
     */
    public static class AnnotationValidationFlags {

        /** The number of formulas in annotation. */
        int formulas;

        /**
         * Gets the number of formulas in annotation.
         * 
         * @return The formulas.
         */
        public int getFormulas() {
            return formulas;
        }

        /** The number of formulas witch were not parsed. */
        int notParsed;

        /**
         * Gets the number of formulas witch were not parsed.
         * 
         * @return The number of formulas witch were not parsed.
         */
        public int getNotParsed() {
            return notParsed;
        }

        /** The number of formulas with satisfied evaluation result. */
        int satisfied;

        /**
         * Gets the number of formulas with satisfied evaluation result.
         * 
         * @return The number of formulas with satisfied evaluation result.
         */
        public int getSatisfied() {
            return satisfied;
        }

        /** The number of formulas with failed evaluation result. */
        int failed;

        /**
         * Gets the number of formulas with failed evaluation result.
         * 
         * @return The number of formulas with failed evaluation result.
         */
        public int getFailed() {
            return failed;
        }

        /** The number of formulas with unknown evaluation result. */
        int unknown;

        /**
         * Gets the unknown.
         * 
         * @return The unknown.
         */
        public int getUnknown() {
            return unknown;
        }

        /**
         * Gets the method aliases.
         * 
         * @return The method aliases.
         */
        public int getMethodAliases() {
            return methodAliasesOk + methodAliasesWarnings + methodAliasesErrors;
        }

        /** The method aliases count. */
        int methodAliasesWarnings;

        /**
         * Gets the method aliases warning.
         * 
         * @return The method aliases warning.
         */
        public int getMethodAliasesWarnings() {
            return methodAliasesWarnings;
        }

        /** The method aliases OK count. */
        int methodAliasesOk;

        /**
         * Gets the method aliases ok.
         * 
         * @return The method aliases ok.
         */
        public int getMethodAliasesOk() {
            return methodAliasesOk;
        }

        /** The method aliases errors count. */
        int methodAliasesErrors;

        /**
         * Gets the method aliases errors.
         * 
         * @return The method aliases errors.
         */
        public int getMethodAliasesErrors() {
            return methodAliasesErrors;
        }

        /**
         * Gets the generator aliases.
         * 
         * @return The generator aliases.
         */
        public int getGeneratorAliases() {
            return generatorAliasesOk + generatorAliasesWarnings + generatorAliasesErrors;
        }

        /** The generator aliases warnings count. */
        int generatorAliasesWarnings;

        /**
         * Gets the generator aliases warning.
         * 
         * @return The generator aliases warning.
         */
        public int getGeneratorAliasesWarnings() {
            return generatorAliasesWarnings;
        }

        /** The generator aliases OK count. */
        int generatorAliasesOk;

        /**
         * Gets the generator aliases ok.
         * 
         * @return The generator aliases ok.
         */
        public int getGeneratorAliasesOk() {
            return generatorAliasesOk;
        }

        /** The generator aliases errors count. */
        int generatorAliasesErrors;

        /**
         * Gets the generator aliases errors.
         * 
         * @return The generator aliases errors.
         */
        public int getGeneratorAliasesErrors() {
            return generatorAliasesErrors;
        }

        /**
         * Sets the flags.
         * 
         * @param annotationEvaluationResult
         *            The annotation evaluation result.
         */
        public void setFlags(AnnotationEvaluationResult annotationEvaluationResult) {
            this.formulas = annotationEvaluationResult.getAnnotationLocation().getFormulas().size();

            this.notParsed = formulas - annotationEvaluationResult.getFormulaEvaluationResults().size();
            this.satisfied = 0;
            this.failed = 0;

            for (FormulaEvaluationResult formula : annotationEvaluationResult.getFormulaEvaluationResults()) {
                switch (formula.getStatisticalResult()) {
                    case OK:
                        ++this.satisfied;
                        break;
                    case FAILED:
                        ++this.failed;
                        break;
                    case NOT_COMPUTED:
                        ++this.unknown;
                        break;
                    default:
                        assert (false);
                        throw new IllegalStateException("Unexpected statistical result " + formula.getStatisticalResult().toString());
                }
            }

            this.methodAliasesOk = 0;
            this.methodAliasesWarnings = 0;
            this.methodAliasesErrors = 0;
            for (MethodAliasDeclaration m : annotationEvaluationResult.getAnnotationLocation().getMethodAliases()) {
                addMethodAliasDeclaration(m);
            }

            this.generatorAliasesOk = 0;
            this.generatorAliasesWarnings = 0;
            this.generatorAliasesErrors = 0;
            for (GeneratorAliasDeclaration g : annotationEvaluationResult.getAnnotationLocation().getGeneratorAliases()) {
                addGeneratorAliasDeclaration(g);
            }
        }

        /**
         * Adds the method alias declaration.
         * 
         * @param methodAliasDeclaration
         *            The method alias declaration.
         */
        void addMethodAliasDeclaration(MethodAliasDeclaration methodAliasDeclaration) {
            if (methodAliasDeclaration.hasParserErrors()) {
                ++this.methodAliasesErrors;
            } else if (methodAliasDeclaration.hasParserWarnings()) {
                ++this.methodAliasesWarnings;
            } else {
                ++this.methodAliasesOk;
            }
        }

        /**
         * Adds the generator alias declaration.
         * 
         * @param generatorAliasDeclaration
         *            The generator alias declaration.
         */
        void addGeneratorAliasDeclaration(GeneratorAliasDeclaration generatorAliasDeclaration) {
            if (generatorAliasDeclaration.hasParserErrors()) {
                ++this.generatorAliasesErrors;
            } else if (generatorAliasDeclaration.hasParserWarnings()) {
                ++this.generatorAliasesWarnings;
            } else {
                ++this.generatorAliasesOk;
            }
        }

    }
}
