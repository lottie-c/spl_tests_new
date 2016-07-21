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

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Implementation per section type.
 * 
 * <p>
 * Git, Subversion, Execution machine - all these would have their own
 * implementation listed by SplAccessConfiguration.
 * 
 * @author Frantisek Haas
 * @author Jaroslav Kotrc
 * 
 */
public interface ISectionFactory {

    /**
     * Gets prefix of the section name for which is this factory. Prefix can be
     * whole name or just first part of the name.
     * 
     * @return prefix of the section name
     */
    public String getSectionPrefix();

    /**
     * Gets general description of the section.
     * 
     * @return section description
     */
    public String getDescription();

    /**
     * Gets description of keys in the section.
     * 
     * @return entries description
     */
    public Map<String, EntryInformation> getEntriesDescription();

    /**
     * Creates ISection from already specified section name.
     * 
     * <p>
     * For example [access.git] does not contain valid details [access.git.THIS]
     * however does. The repository name section is missing.
     * </p>
     * 
     * <p>
     * [access.svn.THAT] won't be for example parsed by this ISectionFactory but
     * by ISectionFactory implemented for Subversion.
     * </p>
     * 
     * @return
     */
    public ISection createFromSectionName(String sectionName, Map<String, String> values);

    public interface ISection {

        public ISectionFactory getFactory();

        /**
         * Complete or incomplete ISection name to show in the table.
         * 
         * @return
         */
        public String getName();

        /**
         * Checks if ISection with supplied details is valid or not.
         * 
         * @return
         */
        public boolean isValid();

        /**
         * Returns troubles in validation.
         * 
         * @return
         */
        public List<String> getErrors();
    }
}
