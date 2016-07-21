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
package cz.cuni.mff.spl.deploy.store;

import java.io.InputStream;
import java.io.OutputStream;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public interface IStore extends IStoreReadonly {

    /**
     * Saves measurement from the stream into the store. There is a special
     * requirement on measurement files. They must contain their full
     * identification in the commentary on the first line. Commentary line
     * starts with character '#'.
     * 
     * @param measurement
     * @param identification
     * @throws StoreException
     */
    public void saveMeasurement(InputStream measurement, SampleIdentification identification)
            throws StoreException;

    /**
     * Creates evaluation directory and returns object to control it.
     * 
     * @param prefix
     * @return
     */
    public IStoreDirectory createEvaluationDirectory(String prefix)
            throws StoreException;

    /**
     * Interface to access store directory object.
     * 
     * <p>
     * {@link IStore} interface supports one level of directories. Therefore a
     * separate directory may be created per run (e.g. evaluation process) of
     * SPL framework.
     * </p>
     * 
     * @author Frantisek Haas
     * 
     */
    public interface IStoreDirectory extends IStoreReadonlyDirectory {

        /**
         * Creates file with specified name in the directory.
         * 
         * @param name
         *            Full name of the file to be created.
         * @return
         *         Object to access the file.
         * 
         * @throws StoreException
         */
        public IStoreFile createFile(String name)
                throws StoreException;

        /**
         * Creates unique file with specified prefix and suffix in the
         * directory.
         * 
         * @param prefix
         *            Mandatory prefix of the file.
         * @param suffix
         *            Mandatory suffix of the file.
         * @return
         *         Object to access the file.
         * 
         * @throws StoreException
         */
        public IStoreFile createUniqueFile(String prefix, String suffix)
                throws StoreException;

        /**
         * Interface to access store file object.
         * 
         * @author Frantisek Haas
         * 
         */
        public interface IStoreFile extends IStoreReadonlyFile {

            /**
             * Gets the output stream to save content to.
             * 
             * @return The output stream.
             * @throws StoreException
             *             The store exception.
             */
            public OutputStream getOutputStream()
                    throws StoreException;

            /**
             * Deletes file.
             * 
             * <p>
             * Note that this operation may not be supported and
             * {@link StoreException} will be thrown.
             * 
             * @return True if and only if the file; false otherwise.
             * @throws StoreException
             *             The store exception.
             */
            public boolean delete()
                    throws StoreException;
        }
    }
}
