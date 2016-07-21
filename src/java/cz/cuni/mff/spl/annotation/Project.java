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
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.annotation.Repository.RevisionNotFoundException;
import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.deploy.build.ClassPathExpander;
import cz.cuni.mff.spl.utils.AliasedObject;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Representing one project which consists of project repository, list of
 * revisions and information how to build this project.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Project extends AbstractXmlTransformationReference implements AliasedObject {

    /** The project alias. */
    private String       alias;

    /** The build command. */
    private Build        build;

    /**
     * <p>
     * The class paths to be used for runtime (generator usage or method
     * invocation).
     * </p>
     * 
     * <p>
     * All values are paths relative to project's root directory.
     * <ul>
     * Expected values are following:
     * <li>Path to directory containing class files.</li>
     * <li>Path to jar file.</li>
     * <li>Path to directory with jar files, ending with '*.jar'. This includes
     * all jars in the directory.</li>
     * <li>Path to directory with jar files, ending with '**.jar'. This includes
     * all jars in the directory and all subdirectories.</li>
     * </ul>
     * </p>
     * 
     * @see ClassPathExpander
     * 
     */
    private List<String> classpaths;

    /**
     * <p>
     * The scan patterns if scan should be done on this project.
     * </p>
     * 
     * <p>
     * Expected values are:
     * <table>
     * <tr>
     * <th>Value</th>
     * <th>What is scanned</th>
     * </tr>
     * <tr>
     * <td>*</td>
     * <td>everything</td>
     * </tr>
     * <tr>
     * <td>package.*</td>
     * <td>all classes in the package</td>
     * </tr>
     * <tr>
     * <td>package.**</td>
     * <td>all classes in the package and all sub packages</td>
     * </tr>
     * <tr>
     * <td>package.AClass</td>
     * <td>just selected class</td>
     * </tr>
     * </table>
     * </p>
     */
    private List<String> scanPatterns;

    /** The repository. */
    private Repository   repository;

    /**
     * Instantiates a new project.
     */
    public Project() {
    }

    /**
     * Gets the alias.
     * 
     * @return The alias.
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias.
     * 
     * @param alias
     *            The new alias.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the builds the.
     * 
     * @return The builds the.
     */
    public Build getBuild() {
        return build;
    }

    /**
     * Sets the builds the.
     * 
     * @param build
     *            The new builds the.
     */
    public void setBuild(Build build) {
        this.build = build;
    }

    /**
     * Gets the repository.
     * 
     * @return The repository, never {@code null}.
     */
    public Repository getRepository() {
        if (repository == null) {
            repository = new Repository();
        }
        return repository;
    }

    /**
     * Sets the repository.
     * 
     * @param repository
     *            The new repository.
     */
    public void setRepository(Repository repository) {
        if (repository.getProject() == null) {
            repository.setProject(this);
        } else if (repository.getProject() != this) {
            throw new IllegalStateException("Repository has already been assigned to another project.");
        }
        this.repository = repository;
    }

    /**
     * Gets the revisions.
     * <p>
     * Use {@code getRepository().getRevisions()} instead.
     * 
     * @return The revisions.
     */
    @Deprecated
    public Map<String, Revision> getRevisions() {
        return getRepository().getRevisions();
    }

    /**
     * Gets the class path entries.
     * 
     * @return The class path entries.
     */
    public List<String> getClasspaths() {
        if (classpaths == null) {
            classpaths = new ArrayList<>(1);
        }
        return classpaths;
    }

    /**
     * Sets the class path entries.
     * 
     * @param classpaths
     *            The new class path entries.
     */
    public void setClasspaths(List<String> classpaths) {
        this.classpaths = classpaths;
    }

    /**
     * Gets the scan patterns.
     * 
     * @return The scan patterns.
     */
    public List<String> getScanPatterns() {
        if (scanPatterns == null) {
            scanPatterns = new ArrayList<>(1);
        }
        return scanPatterns;
    }

    /**
     * Sets the scan patterns.
     * 
     * @param scanPatterns
     *            The new scan patterns.
     */
    public void setScanPatterns(List<String> scanPatterns) {
        this.scanPatterns = scanPatterns;
    }

    /**
     * Returns revision for given name.
     * 
     * @param revisionName
     *            Name of the revision
     * @return Revision for given name
     * @throws RevisionNotFoundException
     *             The revision not found exception.
     */
    @Deprecated
    public Revision getRevision(String revisionName) throws RevisionNotFoundException {
        return getRepository().getRevision(revisionName);
    }

    /**
     * Adds the revision to this project. Sets this project as the revision's
     * project and uses the revision's id as key in the internal map
     * 
     * @param revision
     *            revision to add
     */
    @Deprecated
    public void addRevision(Revision revision) {
        this.getRepository().addRevision(revision);
    }

    /**
     * ID has no influence to hashCode.
     * 
     * @return The int.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((build == null) ? 0 : build.hashCode());
        result = prime * result + ((classpaths == null) ? 0 : classpaths.hashCode());
        result = prime * result + ((scanPatterns == null) ? 0 : scanPatterns.hashCode());
        result = prime * result + ((repository == null) ? 0 : repository.hashCode());
        return result;
    }

    /**
     * ID has no influence to equals.
     * 
     * @param obj
     *            The obj.
     * @return True, if successful.
     */
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
        Project other = (Project) obj;
        return EqualsUtils.safeEquals(this.alias, other.alias)
                && EqualsUtils.safeEquals(this.build, other.build)
                && EqualsUtils.safeEquals(this.classpaths, other.classpaths)
                && EqualsUtils.safeEquals(this.scanPatterns, other.scanPatterns)
                && EqualsUtils.safeEquals(this.repository, other.repository);
    }

    @Override
    public String toString() {
        return "Project [alias=" + alias + ", build=" + build + ", classpaths=" + classpaths + ", scanPatterns=" + scanPatterns + ", repository=" + repository
                + "]";
    }

}
