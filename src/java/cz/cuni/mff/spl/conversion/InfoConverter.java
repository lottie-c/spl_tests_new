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
package cz.cuni.mff.spl.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import cz.cuni.mff.spl.annotation.Info;

/**
 * Support class for common XML conversion manipulation with {@link Info} class
 * instances.
 * <p>
 * This class initializes integrated generator library on loaded info instances.
 * 
 * @author Martin Lacina
 * 
 */
public abstract class InfoConverter {

    /**
     * Saves SPL context information to file as XML.
     * 
     * @param info
     *            The SPL context information.
     * @param file
     *            The file.
     * @throws ConversionException
     *             The conversion exception is thrown when error saving
     *             info to file occurs (either conversion error, or I/O error).
     */
    public static void saveInfoToFile(Info info, File file) throws ConversionException {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            XmlConversion.ConvertClassToXml(info, new FileOutputStream(file));
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Saves SPL context information writer as XML.
     * 
     * @param info
     *            The SPL context information.
     * @param writer
     *            The writer.
     * @throws ConversionException
     *             The conversion exception is thrown when error saving
     *             info to writer occurs (either conversion error, or I/O
     *             error).
     */
    public static void saveInfoToWriter(Info info, Writer writer) throws ConversionException {
        XmlConversion.ConvertClassToXml(info, writer);
    }

    /**
     * Converts SPL context information to XML.
     * 
     * @param info
     *            The SPL context information.
     * @return The string.
     * @throws ConversionException
     *             The conversion exception is thrown when conversion error
     *             occurs.
     */
    public static String convertInfoToXml(Info info) throws ConversionException {
        return XmlConversion.ConvertClassToXml(info);
    }

    /**
     * Loads SPL context information from file.
     * 
     * @param file
     *            The file.
     * @return The SPL context information.
     * @throws ConversionException
     *             The conversion exception is thrown when error loading
     *             info from file occurs (either conversion error, or I/O
     *             error).
     */
    public static Info loadInfoFromFile(File file) throws ConversionException {
        try {
            Info info = (Info) XmlConversion.ConvertClassFromXml(new FileInputStream(file));
            info.initializeIntegratedGeneratorsLibrary();
            return info;
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Loads SPL context information from reader.
     * 
     * @param reader
     *            The file.
     * @return The SPL context information.
     * @throws ConversionException
     *             The conversion exception is thrown when error loading
     *             info from reader occurs (either conversion error, or I/O
     *             error).
     */
    public static Info loadInfoFromReader(Reader reader) throws ConversionException {
        Info info = (Info) XmlConversion.ConvertClassFromXml(reader);
        info.initializeIntegratedGeneratorsLibrary();
        return info;
    }

    /**
     * Loads SPL context information from string.
     * 
     * @param infoxml
     *            The SPL context information XML.
     * @return The SPL context information.
     * @throws ConversionException
     *             The conversion exception is thrown when conversion error in
     *             string occurs.
     */
    public static Info loadInfoFromString(String infoxml) throws ConversionException {
        Info info = (Info) XmlConversion.ConvertClassFromXml(infoxml);
        info.initializeIntegratedGeneratorsLibrary();
        return info;
    }

}
