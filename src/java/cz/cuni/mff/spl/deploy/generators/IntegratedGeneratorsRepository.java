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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import cz.cuni.mff.spl.deploy.build.vcs.IRepository;
import cz.cuni.mff.spl.deploy.build.vcs.RepositoryFactory;
import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.PackUtils;

/**
 * <p>
 * This class is used in {@link Builder} infrastructure to obtain integrated
 * generators when they're needed. This enables easy integration to the
 * {@link Builder} though integrated generators do not require special handling.
 * 
 * <p>
 * This {@link IRepository} implementation is not to be used directly by users
 * via declaring its {@link RepositoryFactory.RepositoryType} in XML
 * configuration.
 * 
 * @author Frantisek Haas
 * 
 */
public class IntegratedGeneratorsRepository extends IRepository {

    public static final String SPL_GENERATORS_JAR = "spl-generators.jar";

    public IntegratedGeneratorsRepository(File cache) {
    }

    private static File findJar(Class<?> clazz)
            throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource(clazz.getCanonicalName().replace('.', '/') + ".class");

        final String protocol = "jar:file:";
        if (!url.toString().startsWith(protocol)) {
            throw new IOException("Can find only jars as files.");
        }

        String path = url.getFile().split("!")[0];

        File file;
        try {
            file = new File(new URI(path));
        } catch (URISyntaxException e) {
            throw new IOException("Can find only jars as files.");
        }

        return file;
    }

    @Override
    public String checkout(String what, File where) throws VcsCheckoutException {
        checkWhere(where);

        PackUtils pack = new PackUtils();

        try {
            pack.addAllInDirectoryRecursively(spl.Generators.class.getPackage().getName().replace('.', '/') + "/");
            pack.write(new File(where, SPL_GENERATORS_JAR));

        } catch (Exception e) {
            throw new VcsCheckoutException("Failed to initialize integrated generators.", e);
        }

        try {
            File jar = findJar(org.apache.commons.math3.random.RandomDataGenerator.class);
            FileUtils.copy(jar, new File(where, jar.getName()));
        } catch (IOException e) {
            throw new VcsCheckoutException("Failed to initialize integrated generators.", e);
        }

        return RepositoryFactory.RepositoryType.SplGenerators.toString();
    }
}
