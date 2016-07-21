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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class implements access to local files as if it was a version control
 * system.
 * 
 * <p>
 * On checkout identification is made of directory canonical name and actual
 * time. This is because every check out from a directory is unique because
 * changes might have been made.
 * 
 * @author Frantisek Haas
 * 
 */
public class Source extends IRepository {

    private static final SplLog logger = SplLogger.getLogger(Source.class);

    /** Source folder/ */
    private final File          source;

    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File          localStoreRootDirectory;

    public Source(File source, File localStoreRootDirectory) {
        this.source = source;
        this.localStoreRootDirectory = localStoreRootDirectory;
    }

    /**
     * @param what
     *            Ignored.
     * 
     * @return
     *         Canonical directory path with actual time.
     * 
     * @throws VcsCheckoutException
     *             If checkout fails for some reason.
     */
    @Override
    public String checkout(String what, File where)
            throws VcsCheckoutException {

        checkWhere(where);

        try {
            FileUtils.copyDirectory(source, where, localStoreRootDirectory);
        } catch (IOException e) {
            throw new VcsCheckoutException("Failed to check out source.", e);
        }

        String path = where.getAbsolutePath();

        try {
            path = where.getCanonicalPath();
        } catch (IOException e) {
            logger.error(e, "Failed to resolve source canonical path, using absolute instead.");
        }

        DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date now = Calendar.getInstance().getTime();
        String time = formatter.format(now);

        // replace \\ for / so it's safely escaped for writing as a string
        // to java source
        return String.format("%s-%s", path, time).replace('\\', '/');

    }
}
