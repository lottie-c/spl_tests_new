=== Introduction ===

Stochastic Performance Logic (SPL) tools serve for capturing performance
assumptions.

With SPL, it is possible to annotate Java functions with assumptions stating,
for example, that the annotated function is at most three times slower than
array copying. The assumption is then checked at build time in a similar way as
standard unit testing. In other words, SPL is about introducing performance
assert().

The advantage of SPL in comparison with other similar solutions includes
following. SPL formulas uses well-defined logic and statistically sound testing
(such as t-test). Next, SPL is aimed at automatic evaluation. And finally, SPL
formulas uses relative comparisons (opposed to comparing against fixed time) to
provide better portability.



=== About tools ===

SPL Tools project consists of a command line utility, an Eclipse plugin and a
Hudson plugin. These tools are stored in separate repositories. The basic
functionality is placed in the main _this_ repository and is used by both
Eclipse and Hudson plug-in as a library.

This repository URL
git://git.code.sf.net/p/spl-tools/code
The Eclipse plugin URL
git://git.code.sf.net/p/spl-tools/eclipseplugin
The Hudson plugin URL
git://git.code.sf.net/p/spl-tools/hudson

Project development documentation and user manual URL
git://git.code.sf.net/p/spl-tools/doc



=== Requirements ===

To compile and run the code Java Development Kit 1.7 and Ant 1.8 or higher is
required. The code is built using ant targets.



=== Build ===

ant             - builds the code
ant dist        - builds packed jar distribution
ant zip-dist    - builds and packs jar and dependencies to zip file
ant doc         - builds JavaDoc documentation
ant test-junit  - builds and runs unit tests
ant case-study  - downloads actual case study files and runs it



=== Filesystem structure ===

(d) build             (generated files)
(d)  |- dist          (completely packed project)
(d) lib               (folder for SPL library JAR files) 
(d) src               (various source files)
(d)  |- examples      (XML and INI examples)
(d)  |- java          (main Java source files) 
(d)  |- script        (shell wrappers)
(d)  |- test          (test files)
(d)    |- junit       (unit tests) 
(d)    |- projects    (integration tests, performed by dynamic junit tests) 
(d)  |- uml           (documentation diagrams) 
(d)  |- xslt          (xml to html evaluator transformation files) 
(d) tools             (various tools) 
(-) .classpath        (Project Java class path configuration file) 
(-) .gitignore        (GIT ignore file) 
(-) .project          (Eclipse file with project configuration) 
(-) LICENSE.txt       (SPL license file) 
(-) SPLcodetemplates  (Eclipse file) 
(-) SPLformater       (Eclipse file with formatting options) 
(-) SPLsaveactions    (Eclipse file)



=== Run ===

To run the application execute in build/dist directory a) java -jar SPL.jar
(on all platforms) b) spl.bat c) spl.sh 



=== Examples ===

Some examples may be found in the src/test/projects directory.

And more example projects on following URLs
git://git.code.sf.net/p/spl-tools/testingrepository1
git://git.code.sf.net/p/spl-tools/testingrepository2
svn://svn.code.sf.net/p/spl-tools/testingrepository3/

