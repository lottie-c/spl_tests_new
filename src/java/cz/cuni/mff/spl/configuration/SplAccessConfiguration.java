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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.annotation.Project;

/**
 * <p>
 * Configuration for accessing remote hosts via SSH for remote execution or
 * connection details to private repositories.
 * </p>
 * 
 * @author Frantisek Haas
 * 
 */
public class SplAccessConfiguration {

    /** Further information for project's repositories. */
    private final Map<String, Map<String, String>> vcsMap;

    /** Configuration for SSH machine execution. */
    private final Map<String, Map<String, String>> sshMap;

    /**
     * @return
     *         <p>
     *         Returns default empty configuration.
     */
    public static SplAccessConfiguration createDefaultConfiguration() {
        return new SplAccessConfiguration();
    }

    /**
     * @return
     *         <p>
     *         Returns factories to all sections this configuration contains.
     */
    public static List<ISectionFactory> getSectionFactories() {
        LinkedList<ISectionFactory> list = new LinkedList<>();
        list.add(new SplAccessMachineSectionFactory());
        list.add(new SplAccessGitSectionFactory());
        list.add(new SplAccessSubversionSectionFactory());

        return list;
    }

    /**
     * <p>
     * Creates empty configuration.
     */
    private SplAccessConfiguration() {
        vcsMap = new HashMap<>();
        sshMap = new HashMap<>();
    }

    /**
     * <p>
     * Initializes configuration with specified values.
     * 
     * @param vcsMap
     *            <p>
     *            Access information to repositories.
     * @param sshMap
     *            <p>
     *            Access information to SSH machines.
     */
    public SplAccessConfiguration(Map<String, Map<String, String>> vcsMap, Map<String, Map<String, String>> sshMap) {
        this.vcsMap = vcsMap;
        this.sshMap = sshMap;
    }

    /**
     * <p>
     * Checks whether configuration contains configuration to a repository of
     * specified projects.
     * 
     * @param vcsName
     *            <p>
     *            The name of the {@link Project} to check vcs configuration
     *            for.
     * @return
     *         True if configuration is found. False otherwise.
     */
    public boolean containsVcsValues(String vcsName) {
        return vcsMap.containsKey(vcsName);
    }

    /**
     * @param vcsName
     *            <p>
     *            The name of the {@link Project} to get vcs configuration for.
     * @return
     *         <p>
     *         Returns configuration to a repository from the {@link Project} of
     *         specified name. If no such is find for the name, empty map is
     *         returned.
     */
    public Map<String, String> getVcsValues(String vcsName) {
        if (vcsMap.containsKey(vcsName)) {
            return vcsMap.get(vcsName);
        } else {
            return new HashMap<String, String>();
        }
    }

    /**
     * @return
     *         <p>
     *         Returns name of all {@link Project}s which contain further
     *         configuration for their repositories.
     */
    public String[] getVcsNames() {
        return vcsMap.keySet().toArray(new String[vcsMap.size()]);
    }

    /**
     * <p>
     * Checks if there's a configuration for a machine with specified name.
     * 
     * @param sshName
     *            <p>
     *            The machine name to check for.
     * @return
     *         <p>
     *         True if such exists. False otherwise.
     */
    public boolean containsSshValues(String sshName) {
        return sshMap.containsKey(sshName);
    }

    /**
     * <p>
     * The name of the machine to get configuration for.
     * 
     * @param sshName
     *            <p>
     *            The machine name to get configuration for.
     * @return
     *         <p>
     *         Returns the machine's configuration. If there's no such for the
     *         name returns empty map.
     */
    public Map<String, String> getSshValues(String sshName) {
        if (sshMap.containsKey(sshName)) {
            return sshMap.get(sshName);
        } else {
            return new HashMap<String, String>();
        }
    }

    /**
     * @return
     *         <p>
     *         Returns list of all machine names that are present in the
     *         configuration.
     */
    public String[] getSshNames() {
        return sshMap.keySet().toArray(new String[sshMap.size()]);
    }

    /**
     * <p>
     * This class contains description of an INI section containing access to
     * remote machine via SSH.
     * 
     * <p>
     * Example of such configuration:
     * 
     * <code>
     * <br /> [access.machine.pc1]
     * <br /> url=example.com
     * <br /> path=spl/execution
     * <br /> username=usr
     * <br /> keyPath=/home/usr/.ssh/pc1_rsa
     * <br /> trustAll=true
     * </code>
     * 
     * @author Frantisek Haas
     * @author Jaroslav Kotrc
     * 
     */
    public static class SplAccessMachineSectionFactory implements ISectionFactory {

        private static final Map<String, EntryInformation> descriptions = new LinkedHashMap<String, EntryInformation>();
        public static final String                         PREFIX       = "access.machine.";

        static {
            descriptions.put("path", new EntryInformation("path", "", "Path on remote machine where to deploy measurement."));
            descriptions.put("username", new EntryInformation("username", "", "Username to login with."));
            descriptions.put("keyPath", new EntryInformation("keyPath", "", "Local path to ssh private key."));
            descriptions.put("trustAll", new EntryInformation("trustAll", "false", "Whether to trust all hosts by default or not (true/false)."));
            descriptions.put("fingerprint", new EntryInformation("fingerprint", "", "Fingerprint of remote host key."));
            descriptions.put("knownHostsPath", new EntryInformation("knownHostsPath", "", "Local path to known_hosts file."));
        }

        @Override
        public String getDescription() {
            return "Configuration of measurement machine.";
        }

        @Override
        public Map<String, EntryInformation> getEntriesDescription() {
            return descriptions;
        }

        @Override
        public ISection createFromSectionName(String name, Map<String, String> values) {
            if (name.startsWith(PREFIX)) {
                return new SplAccessSection(this, name, values);
            } else {
                return null;
            }
        }

        /**
         * <p>
         * This class serves for validation of access information to remote
         * machine. Currently no validation is supported.
         * 
         * @author Frantisek Haas
         * 
         */
        public static class SplAccessSection implements ISection {

            private final ISectionFactory factory;
            private final String          name;

            public SplAccessSection(ISectionFactory factory, String name, Map<String, String> values) {
                this.factory = factory;
                this.name = name;
            }

            @Override
            public ISectionFactory getFactory() {
                return factory;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public List<String> getErrors() {
                return new ArrayList<String>();
            }
        }

        @Override
        public String getSectionPrefix() {
            return PREFIX;
        }
    }

    /**
     * <p>
     * This class contains description of an INI section containing possible
     * required credentials to access git repositories.
     * 
     * @author Frantisek Haas
     * @author Jaroslav Kotrc
     * 
     */
    public static class SplAccessGitSectionFactory implements ISectionFactory {

        private static final Map<String, EntryInformation> descriptions = new LinkedHashMap<String, EntryInformation>();
        public static final String                         PREFIX       = "access.git.";

        static {
            descriptions.put("username", new EntryInformation("username", "", "Username to login with."));
            descriptions.put("keyPath", new EntryInformation("keyPath", "", "Local path to ssh private key."));
            descriptions.put("trustAll", new EntryInformation("trustAll", "false", "Whether to trust all hosts by default or not (true/false)."));
            descriptions.put("fingerprint", new EntryInformation("fingerprint", "", "Fingerprint of remote host key."));
            descriptions.put("knownHostsPath", new EntryInformation("knownHostsPath", "", "Local path to known_hosts file."));
        }

        @Override
        public String getDescription() {
            return "Configuration of git repository.";
        }

        @Override
        public Map<String, EntryInformation> getEntriesDescription() {
            return descriptions;
        }

        @Override
        public ISection createFromSectionName(String name, Map<String, String> values) {
            if (name.startsWith(PREFIX)) {
                return new SplAccessGitSection(this, name, values);
            } else {
                return null;
            }
        }

        /**
         * <p>
         * This class serves for validation of credentials to git repositories.
         * Currently no validation is supported.
         * 
         * @author Frantisek Haas
         * 
         */
        public static class SplAccessGitSection implements ISection {

            private final ISectionFactory factory;
            private final String          name;

            public SplAccessGitSection(ISectionFactory factory, String name, Map<String, String> values) {
                this.factory = factory;
                this.name = name;
            }

            @Override
            public ISectionFactory getFactory() {
                return factory;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public List<String> getErrors() {
                return new ArrayList<String>();
            }
        }

        @Override
        public String getSectionPrefix() {
            return PREFIX;
        }
    }

    /**
     * <p>
     * This class contains description of an INI section containing possible
     * required credentials to access subversion repositories.
     * 
     * @author Frantisek Haas
     * @author Jaroslav Kotrc
     * 
     */
    public static class SplAccessSubversionSectionFactory implements ISectionFactory {

        private static final Map<String, EntryInformation> descriptions = new LinkedHashMap<String, EntryInformation>();
        public static final String                         PREFIX       = "access.subversion.";

        static {
            descriptions.put("username", new EntryInformation("username", "", "Username to login with."));
            descriptions.put("keyPath", new EntryInformation("keyPath", "", "Local path to ssh private key."));
            descriptions.put("trustAll", new EntryInformation("trustAll", "false", "Whether to trust all hosts by default or not (true/false)."));
            descriptions.put("fingerprint", new EntryInformation("fingerprint", "", "Fingerprint of remote host key."));
            descriptions.put("knownHostsPath", new EntryInformation("knownHostsPath", "", "Local path to known_hosts file."));
        }

        @Override
        public String getDescription() {
            return "Configuration of subversion repository.";
        }

        @Override
        public Map<String, EntryInformation> getEntriesDescription() {
            return descriptions;
        }

        @Override
        public ISection createFromSectionName(String name, Map<String, String> values) {
            if (name.startsWith(PREFIX)) {
                return new SplAccessSubversionSection(this, name, values);
            } else {
                return null;
            }
        }

        /**
         * <p>
         * This class serves for validation of credentials to subversion
         * repositories. Currently no validation is supported.
         * 
         * @author Frantisek Haas
         * 
         */
        public static class SplAccessSubversionSection implements ISection {

            private final ISectionFactory factory;
            private final String          name;

            public SplAccessSubversionSection(ISectionFactory factory, String name, Map<String, String> values) {
                this.factory = factory;
                this.name = name;
            }

            @Override
            public ISectionFactory getFactory() {
                return factory;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public List<String> getErrors() {
                return new ArrayList<String>();
            }
        }

        @Override
        public String getSectionPrefix() {
            return PREFIX;
        }
    }
}
