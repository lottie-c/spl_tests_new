/**
 * <p>
 * Provides interfaces for evaluation output abstraction and classes implementing them.
 * <p>
 * Interface {@link cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping}
 * allows to map object to {@link cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile}
 * instances and to maintain this mapping during evaluation. This allows to check if some 
 * object (for example measurement sample) was already processed or not.
 * <p>
 * {@link cz.cuni.mff.spl.evaluator.output.SPLOutputFileMapping} is extension of 
 * {@link cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping} with specific methods for 
 * various output objects (i.e. measurement, comparison, formula result or annotation result).
 */
package cz.cuni.mff.spl.evaluator.output;