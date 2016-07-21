/**
 * 
 */
package cz.cuni.mff.spl.scannertest;

import cz.cuni.mff.spl.SPL;

/**
 * @author Jirka
 * 
 */
public class AnnotatedClass {

    @SPL(

    generators = { "generator=cz.cuni.mff.spl.scanner.AnnotatedClass", },

    methods = { "method=THIS:cz.cuni.mff.spl.scanner.AnnotatedClass#otherMethod" },

    formula = { "for (j {100, 200})" + " SELF[generator](j) <= method[generator](j)" }

    )
    public void annotatedMethod() {

    }

    public void otherMethod() {

    }
}
