/**
 * This package contains tools for scanning classes for SPL annotations. 
 * 
 * Use the scanner either from command line or run the scanner from java code.
 * 
 * It supports the possibility to scan classes by patterns. These patterns are
 * joined in an OR condition. If no pattern is given, '*' is used (all classes
 * are searched.). There are four kinds of patterns:
 * <ul>
 * <li>'*' - everything</li>
 * <li>'cz.cuni.mff.*' - all classes in package cz.cuni.mff but not the classes
 * in the sub packages</li>
 * <li>'cz.cuni.mff.**' - all classes in package cz.cuni.mff and also the
 * classes in the sub packages</li>
 * <li>'cz.cuni.mff.Class' - just the one class</li>
 * </ul>
 * 
 * The Scanner expects, that all the scanned classes are built on the class path
 * either as .class files in a directory tree or in a .jar file.
 * But you can give the scanner a URLClassLoader.
 * There have also the SPL.jar file and its dependencies to be on the class path.
 * You can use Utils.addClassPathItems to get the ClassLoader.
 */
package cz.cuni.mff.spl.scanner;