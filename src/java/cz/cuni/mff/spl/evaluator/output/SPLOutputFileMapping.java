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
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;

/**
 * The Interface OutputFileMapping.
 * 
 * @author Martin Lacina
 */
public interface SPLOutputFileMapping extends BasicOutputFileMapping {

    /**
     * Gets the measurement output file.
     * For specified measurement returns same file in every call.
     * 
     * @param measurement
     *            The measurement.
     * @return The measurement output file.
     * @throws StoreException
     *             The store exception.
     */
    IStoreFile getMeasurementOutputFile(Measurement measurement) throws StoreException;

    /**
     * Gets the comparison output file.
     * For specified comparison returns same file in every call.
     * 
     * @param comparison
     *            The comparison.
     * @return The comparison output file.
     * @throws StoreException
     *             The store exception.
     */
    IStoreFile getComparisonOutputFile(Comparison comparison) throws StoreException;

    /**
     * Gets the formula output file.
     * For specified formula returns same file in every call.
     * 
     * @param formulaEvaluationiResult
     *            The formula.
     * @return The formula output file.
     * @throws StoreException
     *             The store exception.
     */
    IStoreFile getFormulaOutputFile(FormulaEvaluationResult formulaEvaluationiResult) throws StoreException;

    /**
     * Gets the annotation output file.
     * For specified annotation returns same file in every call.
     * 
     * @param annotationEvaluationResult
     *            The annotation evaluation result.
     * @return The annotation output file.
     * @throws StoreException
     *             The store exception.
     */
    IStoreFile getAnnotationOutputFile(AnnotationEvaluationResult annotationEvaluationResult) throws StoreException;

}
