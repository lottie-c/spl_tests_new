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

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public interface IStoreReadonly {

    /**
     * <p>
     * The store root helper file allows to find store root for any file inside
     * store
     * </p>
     */
    public static final String STORE_ROOT_HELPER_FILE_NAME = ".spl-store-root";

    /**
     * Checks if measurement with specified identification already exists.
     * 
     * @param identification
     * @return
     */
    public boolean measurementExists(SampleIdentification identification)
            throws StoreException;

    /**
     * Loads measurement from the store and returns it as a stream.
     * 
     * @param identification
     * @return
     * @throws StoreException
     */
    public InputStream loadMeasurement(SampleIdentification identification)
            throws StoreException;

    /**
     * Lists all present evaluation directories.
     * 
     * @return
     */
    public IStoreReadonlyDirectory[] listEvaluationDirectories()
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
    public interface IStoreReadonlyDirectory {

        /**
         * <p>
         * The helper file allows to find evaluation result directory any file
         * inside it.
         */
        public static final String EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME = ".spl-evaluation-result";

        /**
         * Lists all files in the directory.
         * 
         * @return
         *         Array of files in the directory.
         * 
         * @throws StoreException
         */
        public IStoreReadonlyFile[] listFiles()
                throws StoreException;

        /**
         * Gets the file.
         * 
         * @param name
         *            The name.
         * @return
         *         The file.
         * 
         * @throws StoreException
         *             In case file is not present.
         */
        public IStoreReadonlyFile getFile(String name)
                throws StoreException;

        /**
         * Returns full name of the directory without any path information.
         * 
         * @return
         *         File name.
         */
        public String getName();

        /**
         * Interface to access store file object.
         * 
         * @author Frantisek Haas
         * 
         */
        public interface IStoreReadonlyFile {

            /**
             * Returns output stream to retrieve all data from file.
             * 
             * @return
             *         Data stream to read from.
             * 
             * @throws StoreException
             */
            public InputStream getInputStream()
                    throws StoreException;

            /**
             * Returns full name of the file without any path information.
             * 
             * @return
             *         File name.
             */
            public String getName();
        }
    }
}
