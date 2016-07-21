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
package cz.cuni.mff.spl.deploy.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * <p>
 * This class is a container for one measurement's sampler code and
 * configuration.
 * 
 * @author Frantisek Haas
 * 
 */
public class Sampler {

    private final SampleIdentification identification;
    private final File                 sampler;
    private final String               command;
    private final String               resultFileName;

    public Sampler(SampleIdentification identification, File sampler, String command, String resultFileName) {
        this.identification = identification;
        this.sampler = sampler;
        this.command = command;
        this.resultFileName = resultFileName;
    }

    /**
     * Sampler full identification.
     * 
     * @return
     */
    public SampleIdentification getIdentification() {
        return identification;
    }

    /**
     * Opens stream to the sampler archive packed in ZIP format.
     * 
     * @return
     * @throws FileNotFoundException
     */
    public InputStream getInputStream()
            throws FileNotFoundException {
        return new FileInputStream(sampler);
    }

    /**
     * Returns command to run the unpacked sampler.
     * 
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * File name of the result file the sampler writes data to.
     * 
     * @return
     */
    public String getResultFileName() {
        return resultFileName;
    }
}
