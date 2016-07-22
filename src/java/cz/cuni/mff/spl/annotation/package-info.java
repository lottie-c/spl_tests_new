/**
 * Provides objects for representing SPL formula. Objects as generators, 
 * methods, formulas and their parts are generated by parser 
 * {@link cz.cuni.mff.spl.formula.parser.Parser} from string entry of the
 * formula. Object info and its parts as machine, project and revision
 * are transformed from XML configuration of the project.
 * <p>
 * Objects are not in final state for usage after running the parser. 
 * To expand parameters of the formula it is needed to use 
 * {@link cz.cuni.mff.spl.formula.expander.Expander} on the formula object created by parser.
 */
package cz.cuni.mff.spl.annotation;