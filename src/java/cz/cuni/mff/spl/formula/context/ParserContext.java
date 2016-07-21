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
package cz.cuni.mff.spl.formula.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository.RevisionNotFoundException;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.utils.ReturningSet;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Context used by parser contains machine where measurements are executed, set
 * of project, generators, methods, measurements and parser errors.
 * <p>
 * Initial context should be created from configuration file and then for every
 * formula parsing new clone of initial context should be used.
 * 
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class ParserContext implements Cloneable {

    /** The logger. */
    private static final SplLog LOG                                              = SplLogger.getLogger(ParserContext.class);

    /** The alias name for most recent revision. */
    public static final String  HEAD_REVISION                                    = "HEAD";

    /** The alias name for annotated project. */
    public static final String  THIS_PROJECT                                     = "THIS";

    /** The alias name for project with SPL integrated aliases declarations. */
    public static final String  SPL_INTEGRATED_PROJECT                           = "SPL";

    /** The alias name for annotated method. */
    public static final String  SELF_METHOD                                      = "SELF";

    /**
     * Machine where measurements are executed.
     * Should be initialized from project configuration before parsing.
     */
    private Machine             machine;

    /**
     * The flag indicating if to issue warnings for generator as class in
     * default package.
     */
    private boolean             issueWarningsForGeneratorAsClassInDefaultPackage = true;

    /**
     * Checks if is issue warnings for generator as class in default package.
     * 
     * @return True, if is issue warnings for generator as class in default
     *         package.
     */
    public boolean isIssueWarningsForGeneratorAsClassInDefaultPackage() {
        return issueWarningsForGeneratorAsClassInDefaultPackage;
    }

    /**
     * Sets the issue warnings for generator as class in default package.
     * 
     * @param issueWarningsForGeneratorAsClassInDefaultPackage
     *            The new issue warnings for generator as class in default
     *            package.
     */
    public void setIssueWarningsForGeneratorAsClassInDefaultPackage(boolean issueWarningsForGeneratorAsClassInDefaultPackage) {
        this.issueWarningsForGeneratorAsClassInDefaultPackage = issueWarningsForGeneratorAsClassInDefaultPackage;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    /**
     * Declared parameters for lambda functions obtained from project
     * configuration.
     */
    private final Map<String, Double> parameters = new LinkedHashMap<String, Double>();

    /**
     * Copies all parameters from given map to this map of parameters.
     * 
     * @param params
     *            parameters to store
     */
    public void putAllParameters(Map<String, Double> params) {
        parameters.putAll(params);
    }

    /**
     * Returns parameter value for given parameter name
     * 
     * @param name
     *            parameter name
     * @return value of parameter for given name
     */
    public Double getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Getter for map of parameters. Returns an unmodifiable view of the map.
     * 
     * @return an unmodifiable view of the map of parameters
     */
    public Map<String, Double> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /** Declared variables in the formula ("for" part of formula). */
    private final Set<String> variables = new LinkedHashSet<String>();

    /**
     * Add variable name to the set of all variables.
     * 
     * @param name
     *            variable name
     */
    public void addVariable(String name) {
        variables.add(name);
    }

    /**
     * Getter for collection of variables declared in formula. Returns an
     * unmodifiable view of the collection.
     * 
     * @return an unmodifiable view of the collection of variables
     */
    public Collection<String> getVariables() {
        return Collections.unmodifiableCollection(variables);
    }

    private final Set<String> usedVariables = new LinkedHashSet<String>();

    /**
     * Adds variable to the set of used variables.
     * 
     * @param name
     *            name of the variable to add
     */
    public void addUsedVariable(String name) {
        usedVariables.add(name);
    }

    /**
     * Go through set of variables and checks that variable is used in formula.
     * For every variable that is not used in formula it reports add warning.
     * 
     */
    public void checkAndReportUnusedVariables() {
        for (String variable : variables) {
            if (!usedVariables.contains(variable)) {
                this.addWarning(new Problem("The value of the variable " + variable + " is not used"));
            }
        }
    }

    /**
     * Mapping name of project to project objects.
     * Projects with revisions should be initialized from configuration file
     * into this map. Project THIS and revision HEAD should be always present
     * before parsing.
     * */
    private final Map<String, Project> projects = new HashMap<String, Project>();

    /**
     * Add project with given name.
     * 
     * @param name
     *            Name of the project
     * @param project
     *            Project for given name
     */
    public void addProject(String name, Project project) {
        projects.put(name, project);
    }

    /**
     * Gets projects available in parser context. Returns an unmodifiable view
     * of the specified map.
     * 
     * @return Projects available in parser context.
     */
    public Map<String, Project> getProjects() {
        return Collections.unmodifiableMap(projects);
    }

    /**
     * For given name of project and name of revision it finds object for
     * revision in specified project.
     * If project is not specified THIS is used.
     * If revision is not specified HEAD is used.
     * 
     * @param projectName
     *            Name of the project
     * @param revisionName
     *            Name o the revision in specified project.
     * @return Revision with given name in the specified project.
     */
    public Revision findRevision(String projectName, String revisionName) {
        if (projectName == null) {
            projectName = THIS_PROJECT;
        }
        if (revisionName == null) {
            revisionName = HEAD_REVISION;
        }
        Project project = projects.get(projectName);
        Revision revision = null;
        if (project == null) {
            addError(new Problem("Cannot find project for name: " + projectName));
        } else {
            try {
                revision = project.getRepository().getRevision(revisionName);
            } catch (RevisionNotFoundException e) {
                addError(new Problem("Cannot find revision for name: " + revisionName + " in project " + projectName));
            }
        }
        return revision;
    }

    /**
     * Mapping name of generator to generator objects. Generators without alias
     * will not be stored in this map.
     */
    private final Map<String, Generator>  generatorMap = new HashMap<String, Generator>();
    /** Set of all generators without duplications. */
    private final ReturningSet<Generator> generators   = new ReturningSet<Generator>();

    /**
     * Getter for set of generators.
     * 
     * @return set of generators
     */
    public ReturningSet<Generator> getGenerators() {
        return generators;
    }

    /**
     * Returns generator for given name. It may be alias for generator or
     * generator from THIS project at HEAD revision without packages before
     * class name. In the second case warning is added to the context.
     * 
     * @param name
     *            Name of the generator which is alias or generator class from
     *            current package.
     * @return Generator for given name.
     */
    public Generator getGenerator(String name) {
        Generator generator = generatorMap.get(name);
        if (generator == null) {
            // assuming that it is static class generator
            generator = Generator.createGenerator(null, null, name, null, null, this);
            if (isIssueWarningsForGeneratorAsClassInDefaultPackage()) {
                addWarning(new Problem("Generator " + name + " is declared as class in default package. Did you mean not yet defined generator alias '" + name
                        + "'?"));
            }
        }
        return generator;
    }

    /**
     * Add generator in the set of generators if it does not exists yet. Do not
     * use for generator with alias, use
     * {@link #addGenerator(String, Generator)} instead. Generator will not be
     * stored in the map for mapping generator names to generator objects.
     * 
     * @param generator
     *            Generator to add into set
     * @return Added generator if it does not exist or existing one if it is
     *         present in the set.
     */
    public Generator addGenerator(Generator generator) {
        return generators.returningAdd(generator);
    }

    /**
     * Add generator with given name. Generator is added to set of all
     * generators if it is not present yet. Either way it adds given alias to
     * map of aliases for generators. If generator has not alias, use
     * {@link #addGenerator(Generator)} instead.
     * 
     * @param name
     *            Name of the generator
     * @param generator
     *            Generator for given name
     * @return Added generator if it does not exist or existing one if it is
     *         present in the context.
     */
    public Generator addGenerator(String name, Generator generator) {
        Generator returnGenerator = generators.returningAdd(generator);
        generatorMap.put(name, returnGenerator);
        return returnGenerator;
    }

    /**
     * Adds all generator aliases from the passed collection that has been
     * parsed successfully to the map of generator aliases and generator objects
     * without duplications.
     * 
     * @param generatorAliases
     *            collection of generator aliases to add
     */
    public void addGeneratorAliases(Collection<GeneratorAliasDeclaration> generatorAliases) {
        for (GeneratorAliasDeclaration genAlias : generatorAliases) {
            if (genAlias.hasDeclarationBeenParsedSuccessfully()) {
                String aliasName = genAlias.getAlias();
                Generator generator = genAlias.getGenerator();
                this.addGenerator(aliasName, generator);
            }
        }
    }

    /**
     * Mapping name of method to method objects. Methods without alias will not
     * be stored in this map. Method SELF should be initialized in the context
     * from project configuration before parsing.
     */
    private final Map<String, Method>  methodMap = new HashMap<String, Method>();
    /** Set of all methods without duplications. */
    private final ReturningSet<Method> methods   = new ReturningSet<Method>();

    /**
     * Getter for set of methods.
     * 
     * @return set of methods
     */
    public ReturningSet<Method> getMethods() {
        return methods;
    }

    /**
     * Returns method for given name. If the method is not present then it
     * reports error.
     * 
     * @param name
     *            Name of the returned method
     * @return Method for given name
     */
    public Method getMethod(String name) {
        Method method = methodMap.get(name);
        if (method == null) {
            addError(new Problem("Cannot find method for alias: " + name));
        }
        return method;
    }

    /**
     * Add method with given alias. Method is added to set of all methods if it
     * is not present yet. Either way it adds given alias to map of aliases for
     * methods. If method has not alias, use {@link #addMethod(Method method)}
     * instead.
     * 
     * @param alias
     *            Alias for given method
     * @param method
     *            Method for given name
     */
    public Method addMethod(String alias, Method method) {
        Method returnMethod = methods.returningAdd(method);
        methodMap.put(alias, returnMethod);
        return returnMethod;
    }

    /**
     * Adds the method aliases mapping.
     * 
     * @param methodAliases
     *            The method aliases declarations.
     */
    public void addMethodAliases(Collection<MethodAliasDeclaration> methodAliases) {
        for (MethodAliasDeclaration genAlias : methodAliases) {
            if (genAlias.hasDeclarationBeenParsedSuccessfully()) {
                String aliasName = genAlias.getAlias();
                Method method = genAlias.getMethod();
                this.addMethod(aliasName, method);
            }
        }
    }

    /**
     * Add method in the set of methods if it does not exists yet. Do not use
     * for method with alias, use {@link #addMethod(String, Method)} instead.
     * Method will not be stored in the map for mapping method names to method
     * objects.
     * 
     * @param method
     *            Method to add into set
     * @return Added method if it does not exist or existing one if it is
     *         present in the set.
     */
    public Method addMethod(Method method) {
        return methods.returningAdd(method);
    }

    /** Set of measurements without duplications. */
    private final ReturningSet<Measurement> measurements = new ReturningSet<Measurement>();

    /**
     * Getter for set of measurements.
     * 
     * @return set of measurements
     */
    public ReturningSet<Measurement> getMeasurements() {
        return measurements;
    }

    /**
     * Add measurement in the set of measurements if it does not exists yet.
     * 
     * @param measurement
     *            Measurement to add into set
     * @return Added measurement if it does not exist or existing one if it is
     *         present in the set.
     * @throws IllegalStateException
     *             The illegal state exception is thrown when measurement has no
     *             generator or no method.
     */
    public Measurement addMeasurement(Measurement measurement) throws IllegalStateException {
        if (measurement.getGenerator() == null) {
            throw new IllegalStateException("Measurement without generator not allowed.");
        } else if (measurement.getMethod() == null) {
            throw new IllegalStateException("Measurement without method not allowed.");
        } else {
            return measurements.returningAdd(measurement);
        }
    }

    /** List of errors reported during formula parsing. */
    private final Set<Problem> errors   = new LinkedHashSet<Problem>();
    /** List of warnings reported during formula parsing. */
    private final Set<Problem> warnings = new LinkedHashSet<Problem>();

    /**
     * Class for reporting problems (error or warnings) during formula parsing.
     */
    public static class Problem {

        /** The problem message text. */
        private String text;

        /**
         * Instantiates a new parser problem.
         * 
         * For XML transformation only.
         */
        @Deprecated
        public Problem() {

        }

        /**
         * Constructs error with given problem message text.
         * 
         * @param text
         *            Text of the problem message error.
         */
        public Problem(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        /**
         * Sets the problem message text.
         * 
         * For XML transformation only.
         * 
         * @param text
         *            The new problem message text.
         */
        @Deprecated
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((text == null) ? 0 : text.hashCode());
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
            Problem other = (Problem) obj;
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return text;
        }

    }

    /**
     * Add error to list of errors.
     * 
     * @param error
     *            Error to add into list.
     */
    public void addError(Problem error) {
        errors.add(error);
    }

    /**
     * Getter for unmodifiable set of errors.
     * 
     * @return unmodifiable set of errors
     */
    public Set<Problem> getErrors() {
        return Collections.unmodifiableSet(errors);
    }

    /**
     * Print all errors in the error list to the log.
     */
    public void printErrors() {
        for (Problem err : errors) {
            LOG.error(err.text);
        }
    }

    /**
     * Add warning to list of warnings.
     * 
     * @param warning
     *            Warning to add into list.
     */
    public void addWarning(Problem warning) {
        warnings.add(warning);
    }

    /**
     * Getter for unmodifiable set of warnings.
     * 
     * @return unmodifiable set of warnings
     */
    public Set<Problem> getWarnings() {
        return Collections.unmodifiableSet(warnings);
    }

    /**
     * Print all warnings in the warning list to the log.
     */
    public void printWarnings() {
        for (Problem warning : warnings) {
            LOG.warn(warning.text);
        }
    }

    /**
     * Deletes all warnings in the context.
     */
    public void clearWarnings() {
        warnings.clear();
    }

    /**
     * Clears the errors.
     */
    public void clearErrors() {
        errors.clear();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return getClone();
    }

    /**
     * Creates typed clone.
     * Differs from {@link #clone()} that {@link CloneNotSupportedException} is
     * not expected to occur.
     * 
     * @return Clone of current instance.
     */
    public ParserContext getClone() {

        ParserContext pc = new ParserContext();

        pc.machine = this.machine;

        pc.projects.putAll(projects);

        pc.generatorMap.putAll(generatorMap);
        pc.generators.addAll(generators);

        pc.methodMap.putAll(methodMap);
        pc.methods.addAll(methods);

        pc.measurements.addAll(measurements);

        pc.errors.addAll(errors);
        pc.warnings.addAll(warnings);

        pc.parameters.putAll(parameters);

        pc.variables.addAll(variables);

        if (pc.hashCode() != this.hashCode() || !pc.equals(this) || !this.equals(pc)) {
            throw new IllegalStateException();
        }

        return pc;
    }

    /**
     * Gets the declared method aliases names. Returned collection is not
     * modifiable.
     * 
     * @return the declared method aliases names
     */
    public Collection<String> getMethodAliasesNames() {
        return Collections.unmodifiableCollection(methodMap.keySet());
    }

    /**
     * Gets names of declared generator aliases. Returned collection is not
     * modifiable.
     * 
     * @return Names of declared generator aliases.
     */
    public Collection<String> getGeneratorAliasesNames() {
        return Collections.unmodifiableCollection(generatorMap.keySet());
    }

    /**
     * Returns <code>true</code> if the passed name of the project is a key in
     * map of projects, <code>false</code> otherwise.
     * 
     * @param projectName
     *            Name of the project
     * @return <code>true</code> if the passed name of the project is a key in
     *         map of projects, <code>false</code> otherwise.
     */
    public boolean containsProject(String projectName) {
        return projects.containsKey(projectName);
    }

    /**
     * Returns <code>true</code> if the project contains given revision,
     * <code>false</code> otherwise.
     * 
     * @param projectName
     *            Name of the project
     * @param revisionName
     *            Name of the revision
     * @return <code>true</code> if the project contains given revision,
     *         <code>false</code> otherwise.
     */
    public boolean containsRevision(String projectName, String revisionName) {
        if ((projectName == null) || projectName.isEmpty()) {
            projectName = THIS_PROJECT;
        }
        if ((revisionName == null) || revisionName.isEmpty()) {
            revisionName = HEAD_REVISION;
        }
        Project project = projects.get(projectName);
        if (project == null) {
            return false;
        } else {
            return project.getRepository().containsRevision(revisionName);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errors == null) ? 0 : errors.hashCode());
        result = prime * result + ((generatorMap == null) ? 0 : generatorMap.hashCode());
        result = prime * result + ((generators == null) ? 0 : generators.hashCode());
        result = prime * result + ((machine == null) ? 0 : machine.hashCode());
        result = prime * result + ((measurements == null) ? 0 : measurements.hashCode());
        result = prime * result + ((methodMap == null) ? 0 : methodMap.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((projects == null) ? 0 : projects.hashCode());
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        result = prime * result + ((warnings == null) ? 0 : warnings.hashCode());
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
        ParserContext other = (ParserContext) obj;
        if (errors == null) {
            if (other.errors != null) {
                return false;
            }
        } else if (!errors.equals(other.errors)) {
            return false;
        }
        if (generatorMap == null) {
            if (other.generatorMap != null) {
                return false;
            }
        } else if (!generatorMap.equals(other.generatorMap)) {
            return false;
        }
        if (generators == null) {
            if (other.generators != null) {
                return false;
            }
        } else if (!generators.equals(other.generators)) {
            return false;
        }
        if (machine == null) {
            if (other.machine != null) {
                return false;
            }
        } else if (!machine.equals(other.machine)) {
            return false;
        }
        if (measurements == null) {
            if (other.measurements != null) {
                return false;
            }
        } else if (!measurements.equals(other.measurements)) {
            return false;
        }
        if (methodMap == null) {
            if (other.methodMap != null) {
                return false;
            }
        } else if (!methodMap.equals(other.methodMap)) {
            return false;
        }
        if (methods == null) {
            if (other.methods != null) {
                return false;
            }
        } else if (!methods.equals(other.methods)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (projects == null) {
            if (other.projects != null) {
                return false;
            }
        } else if (!projects.equals(other.projects)) {
            return false;
        }
        if (variables == null) {
            if (other.variables != null) {
                return false;
            }
        } else if (!variables.equals(other.variables)) {
            return false;
        }
        if (warnings == null) {
            if (other.warnings != null) {
                return false;
            }
        } else if (!warnings.equals(other.warnings)) {
            return false;
        }
        return true;
    }

}
