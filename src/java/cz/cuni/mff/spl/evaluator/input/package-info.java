/**
 * <p>
 * Provides interfaces for providing measurement samples and measurement sample data
 * and classes implementing them.
 * <p>
 * The evaluation uses instances of 
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementSampleProvider} 
 * to get instances of 
 * {@link cz.cuni.mff.spl.evaluator.statistics.MeasurementSample} 
 * for instance of {@link cz.cuni.mff.spl.annotation.Measurement}.
 * <p>
 * {@link cz.cuni.mff.spl.evaluator.input.CachingMeasurementSampleProvider} 
 * is implementation of 
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementSampleProvider} 
 * and uses instance of 
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider}
 * to obtain instance of 
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementSampleDataProvider}
 * for every created
 * instance of {@link cz.cuni.mff.spl.evaluator.statistics.MeasurementSample}.
 * <p>
 * {@link cz.cuni.mff.spl.evaluator.input.StoreMeasurementDataProvider}
 * is implementation of
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider}  
 * which has implementation of 
 * {@link cz.cuni.mff.spl.evaluator.input.MeasurementSampleDataProvider}
 * as private inner class.
 */
package cz.cuni.mff.spl.evaluator.input;