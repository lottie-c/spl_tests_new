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
package cz.cuni.mff.spl.deploy.generators;

import java.util.Collection;
import java.util.LinkedList;

import cz.cuni.mff.spl.SPL;
import cz.cuni.mff.spl.annotation.Build;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.deploy.build.ClassPathExpander;
import cz.cuni.mff.spl.deploy.build.vcs.RepositoryFactory;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.utils.ReturningSet;

/**
 * <p>
 * This class is a factory for integrated generators support. Integrated
 * generators are implemented as a special built-in project that is accessbile
 * to all SPL projects.
 * 
 * <p>
 * SPL integrated project with generators may be either loaded from XML or if is
 * not present is added at run-time to the Info object.
 * 
 * @author Frantisek Haas
 * 
 */
public class IntegratedGeneratorsFactory {

    /**
     * <p>
     * Creates {@link Project} which will contain integrated {@link Generator}s
     * when filled with
     * {@link #createGenerators(Revision, ReturningSet, Collection)}.
     * {@link Generator}s from this {@link Project} might be referenced directly
     * in {@link SPL} annotations without explicit declaration.
     * 
     * @return
     */
    public static Project createProject() {
        Project project = new Project();

        project.setAlias(ParserContext.SPL_INTEGRATED_PROJECT);

        {
            Build build = new Build();
            build.setCommand(null);
            project.setBuild(build);
        }

        {
            LinkedList<String> classPaths = new LinkedList<>();
            classPaths.add(ClassPathExpander.EXPAND_JAR_DIRECTORY);
            project.setClasspaths(classPaths);
        }

        {
            Repository repository = new Repository();
            repository.setProject(project);
            repository.setType(RepositoryFactory.RepositoryType.SplGenerators.toString());
            repository.setUrl(null);

            Revision revision = new Revision();
            revision.setRepository(repository);
            revision.setAlias(ParserContext.HEAD_REVISION);
            revision.setValue(null);

            repository.addRevision(revision);
            project.setRepository(repository);
        }

        return project;
    }

    /**
     * <p>
     * Initializes integrated {@link Generator}s located in
     * {@link spl}.
     * 
     * @param mainRevision
     * @param uniqueGenerators
     * @param integratedGenerators
     */
    public static void createGenerators(Revision mainRevision, ReturningSet<Generator> uniqueGenerators,
            Collection<GeneratorAliasDeclaration> integratedGenerators) {

        /*
        String aliasPrefix = "SPL_";

        Set<Problem> noProblems = Collections.emptySet();

        {
            Generator generator = new Generator(mainRevision, TestGenerator.class.getCanonicalName(), null, null);
            generator = uniqueGenerators.returningAdd(generator);

            GeneratorAliasDeclaration declaration = new GeneratorAliasDeclaration(aliasPrefix + TestGenerator.class.getSimpleName(), generator,
                    aliasPrefix + TestGenerator.class.getSimpleName() + "=" + TestGenerator.class.getCanonicalName() + "()", noProblems, noProblems);

            integratedGenerators.add(declaration);
        }

        {
            Generator generator = new Generator(mainRevision, LibTestGenerator.class.getCanonicalName(), null, null);
            generator = uniqueGenerators.returningAdd(generator);

            GeneratorAliasDeclaration declaration = new GeneratorAliasDeclaration(aliasPrefix + LibTestGenerator.class.getSimpleName(), generator,
                    aliasPrefix + LibTestGenerator.class.getSimpleName() + "=" + LibTestGenerator.class.getCanonicalName() + "()", noProblems, noProblems);

            integratedGenerators.add(declaration);
        }        
        */
    }
}
