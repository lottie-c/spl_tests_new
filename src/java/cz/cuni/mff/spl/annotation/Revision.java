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

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.utils.AliasedObject;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Representing one revision of certain repository in one project.
 * Contains reference to parent repository and value identifying revision.
 * 
 * Meaning of revision value is defined for every repository type by
 * its implementation.
 * 
 * @author Martin Lacina
 */
public class Revision extends AbstractXmlTransformationReference implements AnnotationToString, AliasedObject {

    /** The revision alias. */
    private String     alias;

    /** The repository. */
    private Repository repository;

    /**
     * The comment, has no effect on {@link #hashCode()} or
     * {@link #equals(Object)}.
     */
    private String     comment;

    /**
     * <p>
     * Identification of revision corresponding to repository type.
     * </p>
     * 
     * <ul>
     * Examples:
     * <li>git - part of hash, hash, branch, tag</li>
     * <li>subversion - revision number, HEAD</li>
     * <li>source - directory path</li>
     * </ul>
     */
    private String     value;

    /**
     * <p>
     * Do not set outside SPL framework code. Internal use only.
     * </p>
     */
    private String     revisionIdentification;

    /**
     * Instantiates a new revision.
     */
    public Revision() {

    }

    /**
     * Instantiates a new revision.
     * 
     * @param alias
     *            The revision alias.
     * @param revisionValue
     *            The revision value.
     */
    public Revision(String alias, String revisionValue) {
        this.alias = alias;
        this.value = revisionValue;
    }

    /**
     * Gets the repository.
     * 
     * @return The repository.
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * sets the repository
     * 
     * @param repository
     *            the new repository
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Gets the name of revision alias.
     * 
     * @return the name of revision alias
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the name of revision alias.
     * 
     * @param alias
     *            the new name of revision alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the project of this revision if the repository object is not null.
     * 
     * Same as calling {@link #getRepository()#getProject()} with repository
     * null checking.
     * 
     * @return the project of this revision
     */
    public Project getProject() {
        Repository repository = getRepository();
        if (repository != null) {
            return repository.getProject();
        }
        return null;
    }

    /**
     * Gets the node identification.
     * 
     * @return The revision identification.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the node identification.
     * 
     * @param revisionIdentification
     *            The new revision identification.
     */
    public void setValue(String revisionIdentification) {
        this.value = revisionIdentification;
    }

    /**
     * Gets the revision identification meant for precise measurement
     * identification.
     * 
     * @return The revision identification.
     */
    public String getRevisionIdentification() {
        return revisionIdentification;
    }

    /**
     * Sets the revision identification meant for precise measurement
     * identification.
     * 
     * @param revisionIdentification
     *            The new revision identification.
     */
    public void setRevisionIdentification(String revisionIdentification) {
        this.revisionIdentification = revisionIdentification;
    }

    /**
     * Gets the comment.
     * 
     * @return The comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment, has no effect on {@link #hashCode()} or
     * {@link #equals(Object)}.
     * 
     * @param comment
     *            The new comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * ID has no influence to hash code.
     * 
     * Repository of this revision affects hash code only with its URL.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((repository == null || repository.getUrl() == null) ? 0 : repository.getUrl().hashCode());
        result = prime * result + ((getProject() == null || getProject().getAlias() == null) ? 0 : getProject().getAlias().hashCode());

        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        // don't add revision identification to hash code, as we use HashSet and
        // LinkedHashSet which does not like when it changes, especially for
        // equals imlementation.
        // result = prime * result + ((revisionIdentification == null) ? 0 :
        // revisionIdentification.hashCode());
        return result;
    }

    /**
     * ID has no influence to equality.
     * 
     * Repository of this revision affects equality only with its URL.
     * 
     * @param obj
     *            the obj
     * @return True, if revisions match.
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
        Revision other = (Revision) obj;

        String thisUrl = null;
        if (repository != null) {
            thisUrl = repository.getUrl();
        }

        String otherUrl = null;
        if (other.repository != null) {
            otherUrl = other.repository.getUrl();
        }

        String thisProjectAlias = null;
        if (getProject() != null) {
            thisProjectAlias = getProject().getAlias();
        }
        String otherProjectAlias = null;
        if (other.getProject() != null) {
            otherProjectAlias = other.getProject().getAlias();
        }
        return EqualsUtils.safeEquals(thisUrl, otherUrl)
                && EqualsUtils.safeEquals(thisProjectAlias, otherProjectAlias)
                && EqualsUtils.safeEquals(this.alias, other.alias)
                && EqualsUtils.safeEquals(this.value, other.value)
                && EqualsUtils.safeEquals(this.revisionIdentification, other.revisionIdentification);
    }

    @Override
    public String toString() {
        return "Revision [alias=" + alias + ", repositoryType=" + repository.getType() + ", repositoryUrl=" + repository.getUrl() + ", comment=" + comment
                + ", value=" + value + ", revisionIdentification=" + revisionIdentification + "]";
    }

    /**
     * Gets the declaration string.
     * 
     * @return The declaration string.
     */
    @Override
    public String getDeclarationString() {
        return String.format("%s@%s", getProject().getAlias(), alias);
    }

    /**
     * Gets the identification string.
     * 
     * @return The identification string.
     */
    @Override
    public String getIdentificationString() {
        if (revisionIdentification != null) {
            return String.format("%s@%s", getProject().getAlias(), revisionIdentification);
        } else {
            return String.format("%s@%s", getProject().getAlias(), value);
        }
    }
}
