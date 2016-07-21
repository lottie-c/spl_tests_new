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
import java.util.List;
import java.util.TreeMap;

import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The annotation evaluation overview result descriptor for XSLT transformation.
 * 
 * @author Martin Lacina
 */
public class OverviewResultDescriptor extends OutputResultDescriptor {

    /** The packages. */
    private final Root packages = new Root();

    /**
     * Gets the package.
     * 
     * @return The package.
     */
    public Root getPackages() {
        return packages;
    }

    private final AnnotationValidationFlags evaluationSummary = new AnnotationValidationFlags();

    /**
     * Gets the evaluation summary.
     * 
     * @return The evaluation summary.
     */
    public AnnotationValidationFlags getEvaluationSummary() {
        return evaluationSummary;
    }

    /**
     * Adds the values in flags to summary.
     * 
     * @param flags
     *            The flags.
     */
    private void addToSummary(AnnotationValidationFlags flags) {
        evaluationSummary.formulas += flags.formulas;
        evaluationSummary.notParsed += flags.notParsed;
        evaluationSummary.satisfied += flags.satisfied;
        evaluationSummary.unknown += flags.unknown;
        evaluationSummary.failed += flags.failed;

        evaluationSummary.generatorAliasesOk += flags.generatorAliasesOk;
        evaluationSummary.generatorAliasesWarnings += flags.generatorAliasesWarnings;
        evaluationSummary.generatorAliasesErrors += flags.generatorAliasesErrors;
        evaluationSummary.methodAliasesOk += flags.methodAliasesOk;
        evaluationSummary.methodAliasesWarnings += flags.methodAliasesWarnings;
        evaluationSummary.methodAliasesErrors += flags.methodAliasesErrors;
    }

    /**
     * Overview result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param annotationEvaluationResults
     *            The annotation evaluation results.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public OverviewResultDescriptor(Info info, ConfigurationBundle configuration, List<AnnotationEvaluationResult> annotationEvaluationResults,
            StatisticValueChecker checker, BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks, AnnotationValidationFlags globalAliasesSummary) {
        super(info, configuration, outputLinks, globalAliasesSummary);

        for (AnnotationEvaluationResult annotation : annotationEvaluationResults) {
            AnnotationLocation location = annotation.getAnnotationLocation();

            AnnotationValidationFlags validationFlags = new AnnotationValidationFlags();
            validationFlags.setFlags(annotation);

            addToSummary(validationFlags);

            Package p = packages.getItem(location.getPackageName());
            Class c = p.getItem(location.getClassName());
            Method m = c.getItem(location.getMethodName());
            m.getAnnotations().add(new Annotation(validationFlags, annotation));
        }

    }

    /**
     * The Class OverviewNode.
     */
    public static abstract class OverviewNode {

        /** The name. */
        private final String name;

        /**
         * Gets the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Instantiates a new overview node.
         * 
         * @param name
         *            The name.
         */
        public OverviewNode(String name) {
            this.name = name;
        }

    }

    /**
     * The Class ContainerNode.
     * 
     * @param <T>
     *            the generic type
     */
    public static abstract class ContainerNode<T extends OverviewNode> extends OverviewNode {

        /** The classes. */
        protected final TreeMap<String, T> items = new TreeMap<>();

        /**
         * New item.
         * 
         * @param name
         *            The name.
         * @return The t.
         */
        protected abstract T newItem(String name);

        /**
         * Gets the item.
         * 
         * @param name
         *            The name.
         * @return The item.
         */
        T getItem(String name) {
            T item = items.get(name);
            if (item == null)
            {
                item = newItem(name);
                items.put(name, item);
            }
            return item;
        }

        /**
         * Instantiates a new container node.
         * 
         * @param name
         *            The name.
         */
        public ContainerNode(String name) {
            super(name);
        }
    }

    /**
     * The class for Root of hierarchy.
     */
    public static class Root extends ContainerNode<Package> {

        /**
         * Instantiates a new root.
         */
        public Root() {
            super(null);
        }

        /**
         * Gets the packages.
         * 
         * @return The packages.
         */
        public ArrayList<Package> getPackages() {
            return new ArrayList<>(items.values());
        }

        /**
         * New item.
         * 
         * @param name
         *            The name.
         * @return The package.
         */
        @Override
        protected Package newItem(String name) {
            return new Package(name);
        }

    }

    /**
     * The class for Package.
     */
    public static class Package extends ContainerNode<Class> {

        /**
         * Gets the classes.
         * 
         * @return The classes.
         */
        public ArrayList<Class> getClasses() {
            return new ArrayList<>(items.values());
        }

        /**
         * New item.
         * 
         * @param name
         *            The name.
         * @return The class.
         */
        @Override
        protected Class newItem(String name) {
            return new Class(name);
        }

        /**
         * Instantiates a new package.
         * 
         * @param name
         *            The name.
         */
        public Package(String name) {
            super(name);
        }
    }

    /**
     * The class for Class.
     */
    public static class Class extends ContainerNode<Method> {

        /**
         * Gets the methods.
         * 
         * @return the methods
         */
        public ArrayList<Method> getMethods() {
            return new ArrayList<>(items.values());
        }

        /**
         * New item.
         * 
         * @param name
         *            The name.
         * @return The method.
         */
        @Override
        protected Method newItem(String name) {
            return new Method(name);
        }

        /**
         * Instantiates a new class.
         * 
         * @param name
         *            The name.
         */
        public Class(String name) {
            super(name);
        }
    }

    /**
     * The class for Method.
     */
    public static class Method extends OverviewNode {

        /** The annotations. */
        private final ArrayList<Annotation> annotations = new ArrayList<>();

        /**
         * Gets the annotations.
         * 
         * @return The annotations.
         */
        public ArrayList<Annotation> getAnnotations() {
            return annotations;
        }

        /**
         * Instantiates a new method.
         * 
         * @param name
         *            The name.
         */
        public Method(String name) {
            super(name);
        }
    }

    /**
     * The class for Annotation.
     */
    public static class Annotation extends OverviewNode {

        /** The validation flags. */
        public AnnotationValidationFlags validationFlags = new AnnotationValidationFlags();

        /**
         * Gets the validation flags.
         * 
         * @return the validation flags
         */
        public AnnotationValidationFlags getValidationFlags() {
            return validationFlags;
        }

        /** The result. */
        private final AnnotationEvaluationResult result;

        /**
         * Gets the annotation id.
         * 
         * @return The annotation id.
         */
        @SuppressWarnings("deprecation")
        public String getAnnotationId() {
            return result.getId();
        }

        /**
         * Instantiates a new annotation.
         * 
         * @param validationFlags
         *            The validation flags.
         * @param result
         *            The result.
         */
        public Annotation(AnnotationValidationFlags validationFlags, AnnotationEvaluationResult result) {
            super(String.format("%s(%s)", result.getAnnotationLocation().getMethodName(), result.getAnnotationLocation().getArgumentsShort()));
            this.validationFlags = validationFlags;
            this.result = result;
        }
    }

}
