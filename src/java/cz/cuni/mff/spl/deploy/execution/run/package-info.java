/**
 * <p>
 * This packages contains unified client code for startup and control of local
 * or remote sampler execution.
 * 
 * <p>
 * Classes in this package essentially pack all classes in
 * {@link cz.cuni.mff.spl.deploy.build.execution.server} into a jar file and
 * copy this jar to execution machine. Sampling codes are also copied there.
 * Then this jar is started as a new process and this package provides classes
 * for status and control of this execution. Communication is based on creation
 * and read of files.
 */
package cz.cuni.mff.spl.deploy.execution.run;