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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;

/**
 * Convert from/into XML string into/from object
 * 
 * Uses mappings from mapping.xml file.
 * 
 * @author Jiri Daniel
 * @author Martin Lacina
 */
public final class XmlConversion {

    /** The singleton XML conversion implementation instance. */
    private static final XmlConversion INSTANCE;
    static {
        try {
            INSTANCE = new XmlConversion();
        } catch (ConversionException e) {
            throw new RuntimeException(e);
        }
    }

    /** The mapping context. */
    private final XMLContext           context;

    /**
     * Gets the unmarshaller.
     * 
     * @return The unmarshaller.
     */
    private Unmarshaller getUnmarshaller() {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setValidation(false);
        return unmarshaller;
    }

    /**
     * Gets the marshaller.
     * 
     * @return The marshaller.
     */
    private Marshaller getMarshaller() {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setValidation(false);
        marshaller.setSuppressXSIType(true);
        return marshaller;
    }

    /**
     * Constructor of conversion object
     * 
     * Loads mappings from the ./mapping.xml file.
     * 
     * @throws ConversionException
     *             Thrown when mapping file contains errors or when it is
     *             inconsistent with class structure.
     */
    private XmlConversion() throws ConversionException {
        URL url = XmlConversion.class.getResource("mapping.xml");
        try {
            context = new XMLContext();
            Mapping mapping = context.createMapping();
            mapping.loadMapping(url);
            context.addMapping(mapping);
        } catch (IOException e) {
            throw new ConversionException(e);
        } catch (MappingException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Converts a class using provided reader.
     * 
     * @param strXML
     *            XML file content
     * @return Unmarshalled object.
     * @throws ConversionException
     */
    private synchronized Object convertClassFromXml(Reader reader) throws ConversionException {

        Object o;
        try {
            o = getUnmarshaller().unmarshal(reader);
        } catch (ValidationException e) {
            throw new ConversionException(e);
        } catch (MarshalException e) {
            throw new ConversionException(e);
        }
        return o;
    }

    /**
     * Convert a class to XML string written to writer.
     * 
     * @param o
     *            Object to marshall.
     * @return Marshalled XML string
     * @throws ConversionException
     */
    private synchronized void convertClassToXml(Object o, Writer writer) throws ConversionException {
        try {
            Marshaller marshaller = getMarshaller();
            marshaller.setWriter(writer);
            marshaller.marshal(o);
        } catch (ValidationException e) {
            throw new ConversionException(e);
        } catch (MarshalException e) {
            throw new ConversionException(e);
        } catch (IOException e) {
            throw new ConversionException(e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
    }

    /**
     * Converts a class to XML string written to writer.
     * 
     * @param o
     *            Object to marshall.
     * @return Marshalled XML string.
     * @throws ConversionException
     *             Thrown when error occurs during conversion.
     */
    public static String ConvertClassToXml(Object o) throws ConversionException {
        StringWriter writer = new StringWriter();
        INSTANCE.convertClassToXml(o, writer);
        return writer.toString();
    }

    /**
     * Converts a class to XML string written to writer.
     * 
     * @param o
     *            Object to marshall.
     * @param outputStream
     *            The output stream to use.
     * @throws ConversionException
     *             Thrown when error occurs during conversion or output stream
     *             usage.
     */
    public static void ConvertClassToXml(Object o, OutputStream outputStream) throws ConversionException {
        INSTANCE.convertClassToXml(o, new BufferedWriter(new OutputStreamWriter(outputStream)));
    }

    /**
     * Converts a class to XML string written to writer.
     * 
     * @param o
     *            Object to marshall.
     * @param outputWriter
     *            The output writer to use.
     * @throws ConversionException
     *             Thrown when error occurs during conversion or writer usage.
     */
    public static void ConvertClassToXml(Object o, Writer outputWriter) throws ConversionException {
        INSTANCE.convertClassToXml(o, outputWriter);
    }

    /**
     * Converts a class from string.
     * 
     * @param xmlString
     *            XML file content
     * @return Unmarshalled object.
     * @throws ConversionException
     *             Thrown when input string contains error.
     */
    public static Object ConvertClassFromXml(String xmlString) throws ConversionException {
        return INSTANCE.convertClassFromXml(new StringReader(xmlString));
    }

    /**
     * Converts a class from provided input stream.
     * 
     * @param xmlInputStrem
     *            Input stream with XML content.
     * @return Unmarshalled object.
     * @throws ConversionException
     *             Thrown when string in input stream contains error or when I/O
     *             exception occurs
     *             during input stream usage.
     */
    public static Object ConvertClassFromXml(InputStream xmlInputStrem) throws ConversionException {
        return INSTANCE.convertClassFromXml(new BufferedReader(new InputStreamReader(xmlInputStrem)));
    }

    /**
     * Converts a class from provided input reader.
     * 
     * @param xmlReader
     *            Reader with XML content.
     * @return Unmarshalled object.
     * @throws ConversionException
     *             Thrown when string provided by reader contains error or when
     *             I/O exception occurs during input reader usage.
     */
    public static Object ConvertClassFromXml(Reader xmlReader) throws ConversionException {
        return INSTANCE.convertClassFromXml(xmlReader);
    }
}
