/**
 * All java files in this package are generated from Parser.jj file. Do not change java
 * files, change Parser.jj file instead and run javacc for generation.
 * <p>
 * Provides parser for SPL formula. Parser can be used for parsing whole formula
 * or declaration of alias for generator or method. Parser needs initialized
 * {@link cz.cuni.mff.spl.formula.context.ParserContext} where it is searching projects and
 * revisions during parsing.
 * <p>
 * Parsing produces objects from package {@link cz.cuni.mff.spl.annotation}
 * that represents the formula and its parts.
 * <p>
 * Objects are not in final state for usage after running the parser. 
 * To expand parameters of the formula it is needed to use 
 * {@link cz.cuni.mff.spl.formula.expander.Expander} on the formula object created by parser.    
 */
package cz.cuni.mff.spl.formula.parser;