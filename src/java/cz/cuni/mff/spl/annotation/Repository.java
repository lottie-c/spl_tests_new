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

import java.util.LinkedHashMap;
import java.util.Map;

import cz.cuni.mff.spl.deploy.build.Builder;
import cz.cuni.mff.spl.deploy.build.vcs.IRepository;
import cz.cuni.mff.spl.deploy.build.vcs.RepositoryFactory;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Representing repository of single project.
 * 
 * Contains information on how to access repository.
 * 
 * Contains map of revisions - every revision is mapped with its alias.
 * 
 * The repository type is defined as String value.
 * Supported values are defined in {@link Builder}.
 * 
 * @author Martin Lacina
 * @author Jiri Daniel
 */
public class Repository {

    /**
     * <p>
     * Type of repository based on implemented system. Supported types are
     * listed in {@link RepositoryFactory.RepositoryType}.
     * </p>
     */
    private String                      type;

    /**
     * <p>
     * URL of the repository. Must be a valid URL corresponding to the
     * repository type specified.
     * </p>
     */
    private String                      url;

    /** The project of repository. */
    private Project                     project;

    /** The revisions. */
    private final Map<String, Revision> revisions = new LinkedHashMap<>();

    /**
     * Name of section in configuration INI specifying details about this
     * instance for {@link IRepository}.
     * 
     * @return Name of ini section.
     */
    public String getRepositoryConfigurationSectionName() {
        return String.format("access.%s.%s", type != null ? type.toLowerCase() : "", getProject().getAlias());
    }

    public Repository() {

    }

    public Repository(String type, String url) {
        this.type = type;
        this.url = url;
    }

    /**
     * Gets the repository type.
     * 
     * @return The repository type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the repository type.
     * 
     * @param repositoryType
     *            The new repository type.
     */
    public void setType(String repositoryType) {
        this.type = repositoryType;
    }

    /**
     * Gets the project.
     * 
     * @return The project.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project.
     * 
     * @param project
     *            The new project.
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Gets the repository URL.
     * 
     * @return The repository URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the repository URL.
     * 
     * @param project
     *            The repository URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the revision with alias {@code HEAD}.
     * 
     * @return The head revision.
     * @throws RevisionNotFoundException
     *             The revision not found exception.
     */
    public Revision getHeadRevision() throws RevisionNotFoundException {
        return getRevision(ParserContext.HEAD_REVISION);
    }

    /**
     * Adds the revision.
     * 
     * @param revision
     *            The revision.
     */
    public void addRevision(Revision revision) {
        if (revision == null) {
            throw new IllegalStateException("Revision can't be null.");
        } else if (revision.getRepository() == null) {
            revision.setRepository(this);
            this.revisions.put(revision.getAlias(), revision);
        } else {
            if (revision.getRepository() == this) {
                this.revisions.put(revision.getAlias(), revision);
            } else {
                throw new IllegalStateException("Added revision has already been assigned to another repository.");
            }
        }
    }

    /**
     * Gets the revision with specified alias.
     * 
     * @param revisionAlias
     *            The revision alias.
     * @return The revision.
     * @throws RevisionNotFoundException
     *             The revision not found exception.
     */
    public Revision getRevision(String revisionAlias) throws RevisionNotFoundException {
        Revision revision = revisions.get(revisionAlias);
        if (revision == null) {
            throw new RevisionNotFoundException(revisionAlias);
        }
        return revision;
    }

    /**
     * Contains revision.
     * 
     * @param revisionName
     *            The revision name.
     * @return True, if revision with specified name is present.
     */
    public boolean containsRevision(String revisionName) {
        return revisions.containsKey(revisionName);
    }

    /**
     * Gets the revisions list.
     * 
     * @return The revisions list.
     */
    public Map<String, Revision> getRevisions() {
        return revisions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((project == null) ? 0 : project.getAlias().hashCode());

        result = prime * result + ((revisions == null) ? 0 : revisions.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Repository other = (Repository) obj;
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        } else if (!project.getAlias().equals(other.project.getAlias())) {
            return false;
        }
        return EqualsUtils.safeEquals(this.type, other.type)
                && EqualsUtils.safeEquals(this.revisions, other.revisions);
    }

    // generated by eclipse
    @Override
    public String toString() {
        return "Repository [type=" + type + ", revisions=" + revisions + "]";
    }

    /**
     * The exception which is to be thrown when revision was not found.
     */
    public static class RevisionNotFoundException extends Exception {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 3727091735663068750L;

        /**
         * Instantiates a new revision not found exception.
         * 
         * @param revisionName
         *            The revision name.
         */
        public RevisionNotFoundException(String revisionName) {
            super(revisionName);
        }
    }

}
