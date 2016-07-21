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
package cz.cuni.mff.spl.evaluator.output;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.FileNameMapper;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;

/**
 * The Class OutputFileMappingImpl.
 * 
 * @author Martin Lacina
 */
public class StoreSplOutputFileMappingImpl extends StoreBasicOutputFileMappingImpl implements SPLOutputFileMapping {

    /** The file extension. */
    private final String fileExtension;

    /** The file prefix. */
    private final String filePrefix;

    /**
     * Instantiates a new file mapping implementation.
     * 
     * @param directory
     *            The directory to create new files in.
     * @param extension
     *            The extension for created files.
     */
    public StoreSplOutputFileMappingImpl(IStoreDirectory directory, String extension) {
        super(directory);
        this.filePrefix = "";
        this.fileExtension = extension != null ? extension : "";
    }

    /**
     * Instantiates a new file mapping implementation.
     * 
     * @param directory
     *            The directory to create new files in.
     * @param prefix
     *            The prefix for created files.
     * @param extension
     *            The extension for created files.
     */
    public StoreSplOutputFileMappingImpl(IStoreDirectory directory, String prefix, String extension) {
        super(directory);
        this.filePrefix = prefix != null ? prefix : "";
        this.fileExtension = extension != null ? extension : "";
    }

    @Override
    public IStoreFile getMeasurementOutputFile(Measurement measurement) throws StoreException {
        String secondPrefix = FileNameMapper.getMeasurementFileNamePrefix(measurement);
        return getOutputFile(measurement, secondPrefix);
    }

    @Override
    public IStoreFile getComparisonOutputFile(Comparison comparison) throws StoreException {
        String secondPrefix = FileNameMapper.getComparisonFileNamePrefix(comparison);
        return getOutputFile(comparison, secondPrefix);
    }

    @Override
    public IStoreFile getFormulaOutputFile(FormulaEvaluationResult formula) throws StoreException {
        String secondPrefix = FileNameMapper.getFormulaFileNamePrefix(formula.getFormulaDeclaration().getFormula());
        return getOutputFile(formula, secondPrefix);
    }

    @Override
    public IStoreFile getAnnotationOutputFile(AnnotationEvaluationResult annotationEvaluationResult) throws StoreException {
        String secondPrefix = FileNameMapper.getAnnotationFileNamePrefix(annotationEvaluationResult.getAnnotationLocation());
        return getOutputFile(annotationEvaluationResult, secondPrefix);
    }

    /**
     * Gets the output file.
     * 
     * @param key
     *            The key.
     * @param secondPrefix
     *            The second prefix of file prefix. When prefix is empty, than
     *            prefix is generated
     *            using {@link FileNameMapper#createPrefixProposal(Object)}.
     * @return The output file.
     * @throws StoreException
     */
    protected IStoreFile getOutputFile(Object key, String secondPrefix) throws StoreException {

        IStoreFile result = super.getIStoreFile(key);

        if (result == null) {

            if (secondPrefix == null) {
                secondPrefix = "";
            }

            String prefix = this.filePrefix + secondPrefix;

            result = super.getOutputFile(key, prefix, fileExtension);
        }

        return result;
    }

}
