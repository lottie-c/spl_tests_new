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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Repository.RevisionNotFoundException;
import cz.cuni.mff.spl.deploy.generators.IntegratedGeneratorsFactory;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.ReturningSet;

/**
 * Storing information gathered by SPL scanner from project configuration and
 * annotations of methods.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Info implements Cloneable {

    /** The projects. */
    private final Set<Project>                   projects;

    /** The project with alias {@link ParserContext#THIS_PROJECT}. */
    private Project                              thisProject;
    /** The project with alias "spl-integrated". */
    private Project                              splIntegratedProject;

    /** The generators. */
    private final Set<Generator>                 generators;

    /** The methods. */
    private final Set<Method>                    methods;

    /** The measurements. */
    private final Set<Measurement>               measurements;

    /** The annotation generator aliases. */
    private final Set<GeneratorAliasDeclaration> globalGenerators        = new LinkedHashSet<>();

    /** The annotation method aliases. */
    private final Set<MethodAliasDeclaration>    globalMethods           = new LinkedHashSet<>();

    private final Set<GeneratorAliasDeclaration> splIntegratedGenerators = new LinkedHashSet<>();

    /** The formulas. */
    private final Set<AnnotationLocation>        annotationLocations;

    /** Declared parameters for lambda functions. */
    private final Map<String, Double>            parameters;

    /**
     * Instantiates a new info.
     */
    public Info() {
        this.projects = new LinkedHashSet<>();
        this.generators = new LinkedHashSet<>();
        this.methods = new LinkedHashSet<>();
        this.measurements = new LinkedHashSet<>();
        this.annotationLocations = new LinkedHashSet<>();
        this.parameters = new LinkedHashMap<>();
    }

    /**
     * Sets the machine.
     * 
     * @param machine
     *            The new machine.
     */
    public void setMachine(Machine machine) {
        for (Measurement m : getMeasurements()) {
            m.setMachine(machine);
        }
    }

    /**
     * Gets the projects.
     * 
     * @return the projects
     */
    public Set<Project> getProjects() {
        return projects;
    }

    /**
     * Add parameter with given name and value.
     * 
     * @param name
     *            parameter name
     * @param value
     *            parameter value
     */
    public void addParameter(String name, Double value) {
        parameters.put(name, value);
    }

    /**
     * Gets the parameters.
     * 
     * @return the parameters
     */
    public Map<String, Double> getParameters() {
        return parameters;
    }

    /**
     * Adds the project.
     * 
     * @param project
     *            the project
     */
    public void addProject(Project project) {
        if (this.projects.add(project)) {
            if (ParserContext.THIS_PROJECT.equals(project.getAlias())) {
                this.thisProject = project;
            } else if (ParserContext.SPL_INTEGRATED_PROJECT.equals(project.getAlias())) {
                this.splIntegratedProject = project;
            }
        }
    }

    /**
     * Gets the project with alias {@link ParserContext#THIS_PROJECT}.
     * Returned value is never {@code null}.
     * 
     * @return The project with alias {@link ParserContext#THIS_PROJECT}.
     * @throws ProjectNotFoundException
     *             when no project with alias {@link ParserContext#THIS_PROJECT}
     *             is present.
     */
    public Project getThisProject() throws ProjectNotFoundException {
        if (thisProject == null) {
            throw new ProjectNotFoundException(ParserContext.THIS_PROJECT);
        }
        return thisProject;
    }

    /**
     * Gets the project with alias {@link ParserContext#SPL_INTEGRATED_PROJECT}.
     * <p>
     * If project is not in info, it shoud be created.
     * <p>
     * Returned can never be {@code null}.
     * 
     * @return The project with alias
     *         {@link ParserContext#SPL_INTEGRATED_PROJECT}.
     */
    public Project getSplIntegratedProject() {
        if (splIntegratedProject == null) {
            splIntegratedProject = IntegratedGeneratorsFactory.createProject();
            this.projects.add(splIntegratedProject);
        }
        return splIntegratedProject;
    }

    /**
     * The exception which is to be thrown when project was not found.
     */
    public static class ProjectNotFoundException extends Exception {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 5099830833799000744L;

        /**
         * Instantiates a new project not found exception.
         * 
         * @param projectName
         *            The revision name.
         */
        public ProjectNotFoundException(String projectName) {
            super("Project '" + projectName + "' not found.");
        }
    }

    /**
     * Gets the generators.
     * 
     * @return the generators
     */
    public Set<Generator> getGenerators() {
        return generators;
    }

    /**
     * Adds the generator.
     * 
     * @param generator
     *            the generator
     */
    public void addGenerator(Generator generator) {
        this.generators.add(generator);
    }

    /**
     * Gets the methods.
     * 
     * @return the methods
     */
    public Set<Method> getMethods() {
        return methods;
    }

    /**
     * Adds the method.
     * 
     * @param method
     *            the method
     */
    public void addMethod(Method method) {
        this.methods.add(method);
    }

    /**
     * Gets the measurements.
     * 
     * @return the measurements
     */
    public Set<Measurement> getMeasurements() {
        return measurements;
    }

    /**
     * Adds the measurement.
     * 
     * @param measurement
     *            the measurement
     */
    public void addMeasurement(Measurement measurement) {
        if (measurement.getGenerator() == null) {
            System.err.println("Discarding measurement without generator.");
        } else if (measurement.getMethod() == null) {
            System.err.println("Discarding measurement without method.");
        } else {
            this.measurements.add(measurement);
        }
    }

    /**
     * Gets the SPL integrated generator aliases.
     * 
     * @return The SPL integrated generator aliases.
     */
    public Set<GeneratorAliasDeclaration> getSplIntegratedGeneratorAliases() {
        return splIntegratedGenerators;
    }

    /**
     * Adds the SPL integrated generator alias.
     * <p>
     * For XML conversion only.
     * 
     * @param generator
     *            The SPL integrated generator alias.
     */
    @Deprecated
    public void addSplIntegratedGeneratorAlias(GeneratorAliasDeclaration generator) {
        this.splIntegratedGenerators.add(generator);
    }

    /**
     * Gets the global generator aliases.
     * 
     * @return The global generator aliases.
     */
    public Set<GeneratorAliasDeclaration> getGlobalGeneratorAliases() {
        return globalGenerators;
    }

    /**
     * Adds the global generator alias.
     * 
     * @param generator
     *            The generator alias.
     */
    public void addGlobalGeneratorAlias(GeneratorAliasDeclaration generator) {
        this.globalGenerators.add(generator);
    }

    /**
     * Gets the global method aliases.
     * 
     * @return The global method aliases.
     */
    public Set<MethodAliasDeclaration> getGlobalMethodAliases() {
        return globalMethods;
    }

    /**
     * Adds the global method alias.
     * 
     * @param method
     *            The method alias.
     */
    public void addGlobalMethodAlias(MethodAliasDeclaration method) {
        this.globalMethods.add(method);
    }

    /**
     * Gets the annotation locations.
     * 
     * @return The annotation locations.
     */
    public Set<AnnotationLocation> getAnnotationLocations() {
        return annotationLocations;
    }

    /**
     * Adds the annotation location.
     * 
     * @param annotationLocation
     *            The annotation location.
     */
    public void addAnnotationLocation(AnnotationLocation annotationLocation) {
        this.annotationLocations.add(annotationLocation);
    }

    /**
     * Generates a new parser context that will contain all information about
     * projects and aliases.
     * 
     * @return New parser context instance.
     */
    public ParserContext getParserContext() {

        initializeIntegratedGeneratorsLibrary();

        ParserContext res = new ParserContext();
        for (Project project : this.projects) {
            res.addProject(project.getAlias(), project);
        }

        // add generator objects
        for (Generator generator : this.generators) {
            res.addGenerator(generator);
        }

        // add method objects
        for (Method method : this.methods) {
            res.addMethod(method);
        }

        // add global defined aliases mapping

        res.addGeneratorAliases(getGlobalGeneratorAliases());

        res.addMethodAliases(getGlobalMethodAliases());

        res.putAllParameters(parameters);

        res.addGeneratorAliases(getSplIntegratedGeneratorAliases());

        return res;
    }

    /**
     * Initializes integrated generators.
     * <p>
     * Preferred way is to add declarations directly to this method.
     * <p>
     * In implementation add generators like this:
     * 
     * <code>
     * <br /> // get returning set for generators
     * <br /> ReturningSet<Generator> uniqueGenerators = new ReturningSet<>();
     * <br /> uniqueGenerators.addAll(getGenerators());
     * <br /> 
     * <br /> Set<Problem> noProblems = Collections.emptySet();
     * <br />
     * <br /> Generator generator = new Generator(mainRevision, "cz.cuni.mff.spl.generators.Gen1", "abc", new GeneratorMethod("method", "def"));
     * <br /> // make sure we have unique one
     * <br /> generator = uniqueGenerators.returningAdd(generator);
     * <br /> 
     * <br /> GeneratorAliasDeclaration declaration = new GeneratorAliasDeclaration("SPL_Gen1", generator,
     * <br /> "SPL_Gen1=cz.cuni.mff.spl.generators.Gen1('abc')#method('def')", noProblems, noProblems);
     * <br /> 
     * <br /> // if generator already existed, it was returned from returning set
     * <br /> getGenerators().add(generator.getGenerator());
     * <br /> // if declaration was parsed from XML, than it is not added again as
     * <br /> // collection is set
     * <br /> getSplIntegratedGeneratorAliases().add(generator);
     * </code>
     * 
     */
    public void initializeIntegratedGeneratorsLibrary() {

        Project splIntegratedProject = getSplIntegratedProject();

        Revision mainRevision;
        try {
            mainRevision = splIntegratedProject.getRepository().getHeadRevision();
        } catch (RevisionNotFoundException e) {
            e.printStackTrace();
            // project is weird, what to do?
            // HEAD revision in integrated project?
            return;
        }
        // get returning set for generators
        ReturningSet<Generator> uniqueGenerators = new ReturningSet<>();
        uniqueGenerators.addAll(getGenerators());

        // list of generators to add
        Collection<GeneratorAliasDeclaration> newSplIntegratedGenerators = new ArrayList<>();

        IntegratedGeneratorsFactory.createGenerators(mainRevision, uniqueGenerators, newSplIntegratedGenerators);

        for (GeneratorAliasDeclaration generator : newSplIntegratedGenerators) {
            // if generator already existed, it was returned from returning set
            this.generators.add(generator.getGenerator());
            // if declaration was parsed from XML, than it is not added again as
            // collection is set
            this.splIntegratedGenerators.add(generator);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationLocations == null) ? 0 : annotationLocations.hashCode());
        result = prime * result + ((generators == null) ? 0 : generators.hashCode());
        result = prime * result + ((globalGenerators == null) ? 0 : globalGenerators.hashCode());
        result = prime * result + ((globalMethods == null) ? 0 : globalMethods.hashCode());
        result = prime * result + ((measurements == null) ? 0 : measurements.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((projects == null) ? 0 : projects.hashCode());
        result = prime * result + ((splIntegratedGenerators == null) ? 0 : splIntegratedGenerators.hashCode());
        result = prime * result + ((splIntegratedProject == null) ? 0 : splIntegratedProject.hashCode());
        result = prime * result + ((thisProject == null) ? 0 : thisProject.hashCode());
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
        Info other = (Info) obj;

        return EqualsUtils.safeEquals(this.projects, other.projects)
                && EqualsUtils.safeEquals(this.thisProject, other.thisProject)
                && EqualsUtils.safeEquals(this.splIntegratedProject, other.splIntegratedProject)
                && EqualsUtils.safeEquals(this.generators, other.generators)
                && EqualsUtils.safeEquals(this.methods, other.methods)
                && EqualsUtils.safeEquals(this.measurements, other.measurements)
                && EqualsUtils.safeEquals(this.globalGenerators, other.globalGenerators)
                && EqualsUtils.safeEquals(this.globalMethods, other.globalMethods)
                && EqualsUtils.safeEquals(this.splIntegratedGenerators, other.splIntegratedGenerators)
                && EqualsUtils.safeEquals(this.annotationLocations, other.annotationLocations)
                && EqualsUtils.safeEquals(this.parameters, other.parameters);
    }

    @Override
    public String toString() {
        return String
                .format("projects: %s\nmethods: %s\ngenerators: %s\nannotations: %s\nmeasurements: %s\nparameters: %s", projects, methods, generators,
                        annotationLocations,
                        measurements, parameters);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return getClone();
    }

    public Info getClone() {
        Info info = new Info();

        info.projects.addAll(this.projects);
        info.thisProject = this.thisProject;
        info.splIntegratedProject = this.splIntegratedProject;
        info.generators.addAll(this.generators);
        info.methods.addAll(this.methods);
        info.measurements.addAll(this.measurements);
        info.globalGenerators.addAll(this.globalGenerators);
        info.globalMethods.addAll(this.globalMethods);
        info.splIntegratedGenerators.addAll(this.splIntegratedGenerators);
        info.annotationLocations.addAll(this.annotationLocations);
        info.parameters.putAll(parameters);

        assert (this.equals(info) && info.equals(this));

        return info;
    }
}
