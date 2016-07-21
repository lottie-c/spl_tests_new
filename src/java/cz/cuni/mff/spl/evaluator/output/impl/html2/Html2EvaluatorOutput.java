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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.XmlConversion;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.LocalStoreDirectory;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.output.AnnotationPrettyPrinter;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.SPLOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.StoreSplOutputFileMappingImpl;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.EvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.LogicalOperationEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.StoreUtils;
import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * The {@link EvaluatorOutput} implementation for creating HTML output using
 * XSLT template.
 * <p>
 * XSLT template and resources are located in {@value #XSLT_RESOURCE_LOCATION}
 * class path location.
 * <p>
 * All resources that need to be copied to HTML output directory (CSS, images)
 * have to be listed in file {@value #COPY_RESOURCES_LIST} (one file name a
 * line, comment lines start with #).
 * <p>
 * Transformation uses main XSLT template {@value #XSLT_SCRIPT} that servers as
 * entry point for transformation.
 * <p>
 * You need to import {@link Info}, {@link ConfigurationBundle} instances XML in
 * XSLT script with protocol {@value #XSLT_RESOURCE_PROTOCOL} and path
 * {@value #SHARED_INFO} as those instances are converted to XML only once and
 * provided through URI {@code spl://?shared-info.xml}.
 * <p>
 * If you need to import other resources located in
 * {@value #XSLT_RESOURCE_LOCATION}, you need to use protocol
 * {@value #XSLT_RESOURCE_PROTOCOL} and their path inside resource directory in
 * their URI, for example {@code spl://shared-template.xsl}.
 * <p>
 * XML chunks are produces using {@link XmlConversion} and instances of classes
 * derived from {@link OutputResultDescriptor}.
 * <p>
 * 
 * @author Martin Lacina
 * 
 * @see OutputResultDescriptor
 * @see MeasurementResultDescriptor
 * @see FormulaResultDescriptor
 * @see AnnotationResultDescriptor
 * @see OverviewResultDescriptor
 * @see GraphReference
 * @see Link
 * 
 */
public class Html2EvaluatorOutput implements EvaluatorOutput {

    /** The logger. */
    private static final SplLog                          logger                                   = SplLogger.getLogger(Html2EvaluatorOutput.class);

    /** The file name for {@code index.html}. */
    private static final String                          INDEX_PAGE_FILENAME                      = "index.html";

    /** The file name for {@code configuration.html}. */
    private static final String                          CONFIG_PAGE_FILENAME                     = "configuration.html";

    /** The file name for {@code measurements-with-problems.html}. */
    private static final String                          MEASUREMENTS_WITH_PROBLEMS_PAGE_FILENAME = "measurements-with-problems.html";

    /** The file name for {@code suspicious-measurements.html}. */
    private static final String                          SUSPICIOUS_MEASUREMENTS_PAGE_FILENAME    = "suspicious-measurements.html";

    /** The file extension for HTML files. */
    private static final String                          HTML_EXTENSION                           = ".html";

    /** The Constant XSLT_RESOURCE_LOCATION. */
    private static final String                          XSLT_RESOURCE_LOCATION                   = "/spl_xslt/";

    /** The prefix for XSLT resource referenced from within XSLT scripts. */
    private static final String                          XSLT_RESOURCE_PROTOCOL                   = "spl://";

    /** The Constant XSLT_SCRIPT. */
    private static final String                          XSLT_SCRIPT                              = "main.xsl";

    /**
     * The name of file in {@value #XSLT_RESOURCE_LOCATION} classpath location
     * describing list of resources to copy to output directory.
     */
    private static final String                          COPY_RESOURCES_LIST                      = "copy-to-output.txt";

    /** The name of shared XML document with static data. */
    private static final String                          SHARED_INFO                              = "?shared-info.xml";

    /** The xslt_transformer. */
    private Transformer                                  xslt_transformer;

    /** The checker of statistic values. */
    private StatisticValueChecker                        checker;

    /** The output store directory. */
    private IStoreDirectory                              outputStoreDirectory;

    /** The evaluation configuration. */
    private ConfigurationBundle                          configuration;

    /**
     * The flag indicating if shared instances should be dropped from
     * descritor.
     */
    private boolean                                      dropSharedInstancesForXslt;

    /** The evaluated info. */
    private Info                                         evaluatedInfo;

    /**
     * The file mapping for processed objects.
     * Allows to create links to measurements and comparisons generated files.
     */
    private SPLOutputFileMapping                         fileMapping;

    /** The temporary file mapping. */
    private SPLOutputFileMapping                         temporaryfileMapping;

    /** The graphs mapping. */
    private BasicOutputFileMapping                       graphsMapping;

    /** The output links. */
    private final ArrayList<Link>                        outputLinks                              = new ArrayList<>();

    /** The annotation results. */
    private final LinkedList<AnnotationEvaluationResult> annotations                              = new LinkedList<>();

    /** The suspicious measurements set. */
    private SuspiciousMeasurementsResultDescriptor       suspiciousMeasurements;

    /** The global aliases summary. */
    private final AnnotationValidationFlags              globalAliasesSummary                     = new AnnotationValidationFlags();

    /** The generated samples. */
    private final Set<MeasurementSample>                 generatedSamples                         = new HashSet<>();

    /**
     * Gets the file mapping for processed objects.
     * 
     * @return the file mapping for processed objects
     */
    public SPLOutputFileMapping getFileMapping() {
        return fileMapping;
    }

    /**
     * Sets the temporary directory.
     * 
     * @param directory
     *            The new temporary directory.
     */
    public void setTemporaryDirectory(File directory) {
        try {
            directory.mkdirs();
            this.temporaryfileMapping = new StoreSplOutputFileMappingImpl(new LocalStoreDirectory(directory), ".xml");
        } catch (StoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the graphs mapping.
     * 
     * @return The graphs mapping.
     */
    public BasicOutputFileMapping getGraphsMapping() {
        return graphsMapping;
    }

    /**
     * Sets the graphs mapping.
     * 
     * @param graphsMapping
     *            The new graphs mapping.
     */
    public void setGraphsMapping(BasicOutputFileMapping graphsMapping) {
        this.graphsMapping = graphsMapping;
    }

    /**
     * Instantiates a new HTML evaluator output.
     */
    public Html2EvaluatorOutput() {
    }

    /**
     * Inits the.
     * 
     * @param configuration
     *            The configuration.
     * @param context
     *            The context.
     * @param statisticValueChecker
     *            The statistic value checker.
     * @param outputStoreDirectory
     *            The output store directory.
     */
    @Override
    public void init(ConfigurationBundle configuration, Info context, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory)
            throws OutputNotInitializedException
    {
        this.fileMapping = new StoreSplOutputFileMappingImpl(outputStoreDirectory, HTML_EXTENSION);
        this.checker = statisticValueChecker;
        this.outputStoreDirectory = outputStoreDirectory;
        this.configuration = configuration;
        this.configuration = configuration;
        this.evaluatedInfo = context;

        for (GeneratorAliasDeclaration generatorAliasDeclaration : context.getGlobalGeneratorAliases()) {
            globalAliasesSummary.addGeneratorAliasDeclaration(generatorAliasDeclaration);
        }

        for (MethodAliasDeclaration methodAliasDeclaration : context.getGlobalMethodAliases()) {
            globalAliasesSummary.addMethodAliasDeclaration(methodAliasDeclaration);
        }

        copyResources();

    }

    /**
     * Gets the transformer singleton instance.
     * <p>
     * If it does not exist, than it is created.
     * 
     * @return The transformer.
     * @throws TransformerConfigurationException
     *             The transformer configuration exception.
     */
    private Transformer getTransformer() throws TransformerConfigurationException {
        initTransformer();
        xslt_transformer.clearParameters();
        xslt_transformer.setParameter("CURRENT_TIME", DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        return xslt_transformer;
    }

    /**
     * Initializes the transformer.
     * 
     * @throws TransformerConfigurationException
     *             The transformer configuration exception.
     */
    private void initTransformer() throws TransformerConfigurationException {
        if (xslt_transformer == null) {
            String templateName = createResourcePath(XSLT_SCRIPT);
            Source xsltSource = new javax.xml.transform.stream.StreamSource(getClass().getResourceAsStream(templateName));

            final javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();

            tf.setErrorListener(new ErrorListener() {

                @Override
                public void warning(TransformerException exception) throws TransformerException {
                    logger.warn(exception, "XSLT warning");
                }

                @Override
                public void fatalError(TransformerException exception) throws TransformerException {
                    logger.fatal(exception, "XSLT fatal error");
                }

                @Override
                public void error(TransformerException exception) throws TransformerException {
                    logger.error(exception, "XSLT error");
                }
            });

            tf.setURIResolver(new URIResolver() {
                private byte[]            sharedInfoFile;
                private final URIResolver originalResolver = tf.getURIResolver();
                {
                    try {
                        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                        OutputResultDescriptor sharedDescriptor = new OutputResultDescriptor(evaluatedInfo, configuration, outputLinks, null);
                        XmlConversion.ConvertClassToXml(sharedDescriptor, byteOutput);
                        sharedInfoFile = byteOutput.toByteArray();
                        tryToSaveXmlDescriptorToTemporary(byteOutput.toString(), sharedDescriptor);
                        // mark it as
                        dropSharedInstancesForXslt = true;
                    } catch (ConversionException e) {
                        logger.error(e, "Unable to convert evaluated info.");
                    }
                }

                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    if (href.startsWith(XSLT_RESOURCE_PROTOCOL)) {
                        String name = href.substring(XSLT_RESOURCE_PROTOCOL.length());
                        if (name.equals(SHARED_INFO)) {
                            if (sharedInfoFile != null) {
                                return new javax.xml.transform.stream.StreamSource(new ByteArrayInputStream(sharedInfoFile));
                            }
                        }
                        InputStream stream = getClass().getResourceAsStream(createResourcePath(name));
                        if (stream != null) {
                            return new javax.xml.transform.stream.StreamSource(stream);
                        }
                    }
                    return originalResolver.resolve(href, base);
                }
            });
            xslt_transformer = tf.newTransformer(xsltSource);
        }
    }

    /**
     * <p>
     * Copies resources specified in resource list file (.
     * 
     * {@link #COPY_RESOURCES_LIST} ) to result output directory.
     */
    private void copyResources() {
        String path = createResourcePath(COPY_RESOURCES_LIST);
        InputStream inputStrem = getClass().getResourceAsStream(path);
        if (inputStrem == null) {
            logger.error("Unable to load file with list of resources to copy [%s]", path);
            return;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(inputStrem))) {
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && getClass().getResource(createResourcePath(line)) != null) {
                    try (InputStream source = getClass().getResourceAsStream(createResourcePath(line));
                            OutputStream destination = outputStoreDirectory.createFile(line).getOutputStream()) {
                        FileUtils.copy(source, destination);
                    } catch (IOException | NullPointerException | StoreException e) {
                        logger.error(e, "Unable to save HTML resource to output directory [%s].", line);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e, "Unable to save HTML resource to output directory.");
        }
    }

    /**
     * Creates the resource path inside {@link #XSLT_RESOURCE_LOCATION}.
     * 
     * @param resourceName
     *            The resource name.
     * @return The resource path.
     */
    private String createResourcePath(String resourceName) {
        return String.format("%s%s", XSLT_RESOURCE_LOCATION, resourceName);
    }

    @Override
    public void generateMeasurementOutput(MeasurementSample measurementSample) {
        try {
            IStoreFile targetFile = this.fileMapping.getMeasurementOutputFile(measurementSample.getMeasurement());
            @SuppressWarnings("deprecation")
            String itemID = measurementSample.getMeasurement().getId();
            if (itemID != null && !itemID.isEmpty()) {
                outputLinks.add(new Link(itemID, targetFile.getName()));
            }
        } catch (StoreException e) {
            logger.error(e, "Unable to allocate output file for measurement [%s].",
                    AnnotationPrettyPrinter.createMeasurementOutput(measurementSample.getMeasurement()));
        }
        addMeasurementIfSuspicious(measurementSample);
    }

    /**
     * Adds the measurement if suspicious.
     * 
     * @param measurementSample
     *            The measurement sample.
     */
    private void addMeasurementIfSuspicious(MeasurementSample measurementSample) {
        if (suspiciousMeasurements == null) {
            suspiciousMeasurements = new SuspiciousMeasurementsResultDescriptor(evaluatedInfo, configuration, checker, graphsMapping, outputLinks);
        }
        suspiciousMeasurements.addMeasurementIfSuspicious(measurementSample);
    }

    /**
     * Generates measurement output content.
     * 
     * @param measurementSample
     *            The measurement sample.
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateMeasurementOutputcontent(MeasurementSample measurementSample) {
        if (checkIfAlreadyGenerated(measurementSample)) {
            return fileMapping.getIStoreFile(measurementSample.getMeasurement()).getName();
        }

        MeasurementResultDescriptor descriptor =
                new MeasurementResultDescriptor(evaluatedInfo, configuration, measurementSample, checker, graphsMapping, outputLinks,
                        globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            IStoreFile targetFile = this.fileMapping.getMeasurementOutputFile(measurementSample.getMeasurement());
            runXslt(transformer, descriptor, targetFile);
            generatedSamples.add(measurementSample);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for measurement [%s].",
                    AnnotationPrettyPrinter.createMeasurementOutput(measurementSample.getMeasurement()));
        }
        return null;
    }

    @Override
    public void generateComparisonOutput(ComparisonEvaluationResult comparisonResult) {
        try {
            IStoreFile targetFile = this.fileMapping.getComparisonOutputFile(comparisonResult.getComparison());
            @SuppressWarnings("deprecation")
            String itemID = comparisonResult.getId();
            if (itemID != null && !itemID.isEmpty()) {
                outputLinks.add(new Link(itemID, targetFile.getName()));
            }
            @SuppressWarnings("deprecation")
            String itemID2 = comparisonResult.getComparison().getId();
            if (itemID2 != null && !itemID2.isEmpty()) {
                outputLinks.add(new Link(itemID2, targetFile.getName()));
            }
        } catch (StoreException e) {
            logger.error(e, "Unable to allocate output file for comparison [%s].",
                    AnnotationPrettyPrinter.createComparisonOutput(comparisonResult.getComparison(), configuration.getEvaluatorConfig().getEqualityInterval()));
        }
    }

    /**
     * Generates comparison output content.
     * 
     * @param comparisonResult
     *            The comparison result.
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateComparisonOutputContent(String backlink, ComparisonEvaluationResult comparisonResult) {
        ComparisonResultDescriptor descriptor =
                new ComparisonResultDescriptor(evaluatedInfo, configuration, comparisonResult, checker, graphsMapping, outputLinks,
                        globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            transformer.setParameter("BACKLINK", backlink);
            IStoreFile targetFile = this.fileMapping.getComparisonOutputFile(comparisonResult.getComparison());
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for comparison [%s].",
                    AnnotationPrettyPrinter.createComparisonOutput(comparisonResult.getComparison(), configuration.getEvaluatorConfig().getEqualityInterval()));
        }
        return null;
    }

    @Override
    public void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationResult) {
        try {
            IStoreFile targetFile = this.fileMapping.getFormulaOutputFile(formulaEvaluationResult);
            @SuppressWarnings("deprecation")
            String itemID = formulaEvaluationResult.getFormulaDeclaration().getId();
            if (itemID != null && !itemID.isEmpty()) {
                outputLinks.add(new Link(itemID, targetFile.getName()));
            }
        } catch (StoreException e) {
            logger.error(e, "Unable to allocate output file for formula [%s].",
                    AnnotationPrettyPrinter.createFormulaOutput(formulaEvaluationResult, configuration.getEvaluatorConfig().getEqualityInterval()));
        }
    }

    /**
     * Generates formula output content.
     * 
     * @param formulaEvaluationResult
     *            The formula evaluation result.
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateFormulaOutputContent(String backlink, FormulaEvaluationResult formulaEvaluationResult) {

        FormulaResultDescriptor descriptor =
                new FormulaResultDescriptor(evaluatedInfo, configuration, formulaEvaluationResult, checker, graphsMapping, outputLinks,
                        globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            transformer.setParameter("BACKLINK", backlink);
            IStoreFile targetFile = this.fileMapping.getFormulaOutputFile(formulaEvaluationResult);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for formula [%s].",
                    AnnotationPrettyPrinter.createFormulaOutput(formulaEvaluationResult, configuration.getEvaluatorConfig().getEqualityInterval()));
        }
        return null;
    }

    @Override
    public void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult) {
        annotations.add(annotationEvaluationResult);
        try {
            IStoreFile targetFile = this.fileMapping.getAnnotationOutputFile(annotationEvaluationResult);
            @SuppressWarnings("deprecation")
            String itemID = annotationEvaluationResult.getId();
            if (itemID != null && !itemID.isEmpty()) {
                outputLinks.add(new Link(itemID, targetFile.getName()));
            }
        } catch (StoreException e) {
            logger.error(e, "Unable to allocate output file for annotation [%s].", annotationEvaluationResult.getAnnotationLocation().getBasicSignature());
        }
    }

    /**
     * Generates annotation output content.
     * 
     * @param annotationEvaluationResult
     *            The annotation evaluation result.
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateAnnotationOutputContent(AnnotationEvaluationResult annotationEvaluationResult) {
        AnnotationResultDescriptor descriptor =
                new AnnotationResultDescriptor(evaluatedInfo, configuration, annotationEvaluationResult, checker, graphsMapping, outputLinks,
                        globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            IStoreFile targetFile = this.fileMapping.getAnnotationOutputFile(annotationEvaluationResult);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for annotation [%s].", annotationEvaluationResult.getAnnotationLocation().getBasicSignature());
        }
        return null;
    }

    /**
     * Html output creates content of output files during closing as links
     * between various files are necessary.
     */
    @Override
    public void close() {
        // make sure that links have no duplicities
        Set<Link> uniqueLinks = new HashSet<>(outputLinks);
        outputLinks.clear();
        outputLinks.addAll(uniqueLinks);

        try {
            initTransformer();
        } catch (TransformerConfigurationException e) {
            logger.error(e, "Unable to initialize XSLT transformation script.");
            return;
        }

        generateIndex();
        generateMeasurementsWithProblems();
        generateSuspiciousMeasurements();
        generateConfigurationPage();
        generateAnnotationPagesRecursive();
    }

    /**
     * Generates index page {@value #INDEX_PAGE_FILENAME}.
     * 
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateIndex() {
        OverviewResultDescriptor descriptor =
                new OverviewResultDescriptor(evaluatedInfo, configuration, annotations, checker, graphsMapping, outputLinks, globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            transformer.setParameter("SUSPICOUS_MEASUREMENTS_COUNT", suspiciousMeasurements.getSuspiciousMeasurements().size());
            IStoreFile targetFile = outputStoreDirectory.createFile(INDEX_PAGE_FILENAME);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for index file [%s].", INDEX_PAGE_FILENAME);
        }
        return null;
    }

    /**
     * Generates suspicious measurements page
     * {@value #SUSPICIOUS_MEASUREMENTS_PAGE_FILENAME}.
     * 
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateSuspiciousMeasurements() {
        SuspiciousMeasurementsResultDescriptor descriptor = suspiciousMeasurements;

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            IStoreFile targetFile = outputStoreDirectory.createFile(SUSPICIOUS_MEASUREMENTS_PAGE_FILENAME);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for suspicious measurements file [%s].", SUSPICIOUS_MEASUREMENTS_PAGE_FILENAME);
        }
        return null;
    }

    /**
     * Generates measurements with problems page
     * {@value #MEASUREMENTS_WITH_PROBLEMS_PAGE_FILENAME}.
     * 
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateMeasurementsWithProblems() {
        MeasurementsWithProblemsResultDescriptor descriptor =
                new MeasurementsWithProblemsResultDescriptor(evaluatedInfo, configuration, outputLinks, globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }

        try {
            Transformer transformer = getTransformer();
            IStoreFile targetFile = outputStoreDirectory.createFile(MEASUREMENTS_WITH_PROBLEMS_PAGE_FILENAME);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for index file [%s].", MEASUREMENTS_WITH_PROBLEMS_PAGE_FILENAME);
        }
        return null;
    }

    /**
     * Generates configuration page {@value #CONFIG_PAGE_FILENAME}.
     * 
     * @return The name of file the content was generated to, or {@code null}
     *         when generation failed.
     */
    private String generateConfigurationPage() {
        ConfigurationResultDescriptor descriptor = new ConfigurationResultDescriptor(evaluatedInfo, configuration, outputLinks, globalAliasesSummary);

        if (dropSharedInstancesForXslt) {
            descriptor.dropSharedInstances();
        }
        try {
            Transformer transformer = getTransformer();
            IStoreFile targetFile = outputStoreDirectory.createFile(CONFIG_PAGE_FILENAME);
            runXslt(transformer, descriptor, targetFile);
            return targetFile.getName();
        } catch (StoreException | IOException | TransformerException | ConversionException e) {
            logger.error(e, "Unable to save HTML output for configuration file [%s].", CONFIG_PAGE_FILENAME);
        }
        return null;
    }

    /**
     * Generates annotation pages and their sub-pages recursively.
     */
    private void generateAnnotationPagesRecursive() {
        for (AnnotationEvaluationResult annotation : annotations) {
            String backlink = generateAnnotationOutputContent(annotation);
            if (backlink == null) {
                continue;
            }
            for (FormulaEvaluationResult formula : annotation.getFormulaEvaluationResults()) {
                String formulaBacklink = generateFormulaOutputContent(backlink, formula);
                if (formulaBacklink == null) {
                    continue;
                }
                generateFormulaSubPagesRecursive(formulaBacklink, formula);
            }

        }
    }

    /**
     * Generates formula sub-pages recursive.
     * 
     * @param backlink
     *            The back link.
     * @param formula
     *            The formula.
     */
    private void generateFormulaSubPagesRecursive(String backlink, EvaluationResult formula) {
        if (formula.isFormulaResult()) {
            generateFormulaSubPagesRecursive(backlink, formula.asFormulaResult().getFormulaEvaluationResultRoot());
        } else if (formula.isLogicalOperationResult()) {
            LogicalOperationEvaluationResult logicaloperationResult = formula.asLogicalOperationResult();
            generateFormulaSubPagesRecursive(backlink, logicaloperationResult.getLeftOperandResult());
            generateFormulaSubPagesRecursive(backlink, logicaloperationResult.getRightOperandResult());
        } else if (formula.isComparisonEvaluationResult()) {
            ComparisonEvaluationResult comparisonResult = formula.asComparisonEvaluationResult();
            generateComparisonOutputContent(backlink, comparisonResult);
            generateMeasurementOutputcontent(comparisonResult.leftMeasurementSample);
            generateMeasurementOutputcontent(comparisonResult.rightMeasurementSample);
        } else {
            assert (false);
            throw new IllegalStateException("Unexpected evaluation result type");
        }
    }

    /**
     * Run XSLT transformation.
     * 
     * @param transformer
     *            The transformer.
     * @param objectForXml
     *            The object for XML.
     * @param outputFile
     *            The output file.
     * @throws TransformerException
     *             The transformer exception.
     * @throws ConversionException
     *             The conversion exception.
     * @throws StoreException
     *             The store exception.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void runXslt(Transformer transformer, Object objectForXml, IStoreFile outputFile)
            throws TransformerException, ConversionException, StoreException, IOException {
        try (OutputStream output = outputFile.getOutputStream()) {
            String xml = XmlConversion.ConvertClassToXml(objectForXml);

            tryToSaveXmlDescriptorToTemporary(xml, objectForXml);

            ByteArrayInputStream xmlInput = new ByteArrayInputStream(StringUtils.getStringBytes(xml));

            Source xmlSource = new javax.xml.transform.stream.StreamSource(xmlInput);
            Result outputTarget = new javax.xml.transform.stream.StreamResult(output);

            transformer.transform(xmlSource, outputTarget);

        } catch (StoreException | TransformerException | ConversionException e) {
            try {
                outputFile.delete();
            } catch (StoreException e1) {
                logger.error(e, "Unable to delete incomplete file [%s].", outputFile.getName());
            }
            throw e;
        }
    }

    /**
     * Try to save XML descriptor to temporary folder.
     * 
     * @param xml
     *            The XML.
     * @param objectForXml
     *            The object for XML.
     */
    private void tryToSaveXmlDescriptorToTemporary(String xml, Object objectForXml) {
        if (this.temporaryfileMapping != null) {
            try {
                IStoreFile targetFile = this.temporaryfileMapping.getOutputFile(objectForXml, objectForXml.getClass().getSimpleName(), ".xml");
                StoreUtils.saveToStoreFile(targetFile, xml);
            } catch (StoreException e) {
                logger.error(e, "Unable to save HTML XML descriptor chunk file [%s].", objectForXml);
            }
        }
    }

    /**
     * Checks if files for specified key have already been generated.
     * Files are considered generated, when HTML file mapping hold mapping for
     * supplied key.
     * 
     * @param key
     *            The key to check.
     * @return True, if files are considered to have been generated.
     */
    private boolean checkIfAlreadyGenerated(MeasurementSample key) {
        return this.generatedSamples.contains(key);
    }
}
