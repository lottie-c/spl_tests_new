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
package cz.cuni.mff.spl.deploy.build.vcs;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.cuni.mff.spl.configuration.SplAccessConfiguration;
import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsParseException;
import cz.cuni.mff.spl.deploy.generators.IntegratedGeneratorsRepository;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;

/**
 * <p>
 * This class servers for creating {@link IRepository} object based on their
 * {@link RepositoryType} and {@link Repository#getUrl()}. Additional
 * information might be loaded from INI configuration.
 * 
 * <p>
 * To add support for new versioning system:
 * 
 * <ul>
 * 
 * <li>Implement {@link IRepository}.</li>
 * 
 * <li>
 * <p>
 * Add its type to {@link RepositoryType} and into
 * {@link #parse(String, String, Map, File, File, InteractiveInterface)}
 * function's code.</li>
 * 
 * <li>
 * <p>
 * If further information such as login credentials are needed edit
 * {@link SplAccessConfiguration}</li>
 * 
 * </ul>
 * 
 * @author Frantisek Haas
 * 
 */
public class RepositoryFactory {

    /**
     * <p>
     * Creates IRepository instance based on its type encoded in string.
     * Repository will be using {@link Repository#getUrl()} and {@link Map
     * <String, String>} of values loaded from INI configuration to connect to
     * the repository if such are needed.
     * 
     * <p>
     * In case additional information from SPL XML might be needed for some
     * IRepository implementations its path is present.
     * 
     * @param type
     *            Repository type.
     * @param url
     *            Repository URL.
     * @param values
     *            Access information to the repository.
     * @param xml
     *            SPL XML for additional information.
     * @param cache
     *            Directory {@link IRepository} can use as a cache.
     * @return
     * @throws VcsParseException
     */
    public static IRepository parse(String type, String url, Map<String, String> values, File xml, File cache, InteractiveInterface interactive,
            File localStoreRootDirectory)
            throws VcsParseException {

        RepositoryType repositoryType = RepositoryType.fromString(type);
        if (repositoryType == null) {
            throw new VcsParseException(String.format("Repository type unknown [%s].", type));
        }

        switch (repositoryType) {
            case SplGenerators:
                return new IntegratedGeneratorsRepository(cache);
            case Git:
                return new Git(url, values, interactive, cache, localStoreRootDirectory);
            case Subversion:
                return new Subversion(url, values, interactive);
            case Source:
                return new Source(new File(url), localStoreRootDirectory);
            case SourceRelative:
                return new Source(new File(xml.getAbsoluteFile().getParentFile(), url), localStoreRootDirectory);
            default:
                throw new VcsParseException(String.format("Repository type not supported [%s].", repositoryType));
        }
    }

    /**
     * Supported repository types.
     * 
     * @author Frantisek Haas
     * 
     */
    public static enum RepositoryType {

        /**
         * <p>
         * SplGenerators is integrated in the framework and should not be listed
         * in the {@link #getTypes()} because it's not to be created by the
         * user. It's handled only internally though it can appear in the xml
         * file.
         */
        SplGenerators,
        Git,
        Subversion,
        Source,
        SourceRelative;

        /** The short name for SVN as {@link Subversion} name is longer. */
        private static final String                SVN_SHORT_NAME = "svn";

        private static Map<RepositoryType, String> description;

        static {
            description = new HashMap<>();
            description.put(Git,
                    "Git versioning system.");
            description.put(Subversion,
                    "Subversion versioning system.");
            description.put(Source,
                    "Local source folder.");
            description.put(SourceRelative,
                    "Local source folder located relative to spl.xml.");
        }

        /**
         * Lists all supported VCS systems.
         * 
         * @return
         */
        public static Iterable<RepositoryType> getTypes() {
            LinkedList<RepositoryType> types = new LinkedList<>();
            types.add(Git);
            types.add(Subversion);
            types.add(Source);
            types.add(SourceRelative);
            return types;
        }

        /**
         * Parses {@link RepositoryType} from {@link String} representation.
         * 
         * @param type
         * @return
         */
        public static RepositoryType fromString(String type) {
            if (type != null) {
                for (RepositoryType rt : RepositoryType.values()) {
                    if (type.equalsIgnoreCase(rt.name())) {
                        return rt;
                    }
                }
                if (SVN_SHORT_NAME.equalsIgnoreCase(type)) {
                    return RepositoryType.Subversion;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name();
        }

        /**
         * Gets versioning system description.
         * 
         * @return
         *         The versioning system description.
         */
        public String getDescription() {
            if (description.containsKey(this)) {
                return description.get(this);
            } else {
                return "No description present.";
            }
        }

        /**
         * Gets the description for repository specified as string.
         * 
         * @param repositoryType
         *            The repository type.
         * @return
         *         The description.
         * 
         * @see #fromString(String)
         * @see #getDescription(RepositoryType)
         */
        public static String getDescription(String repositoryType) {
            RepositoryType typed = fromString(repositoryType);
            if (typed != null) {
                return typed.getDescription();
            } else {
                return "No description present.";
            }
        }

    }
}
