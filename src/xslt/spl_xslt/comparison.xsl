<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="comparison-result-descriptor">
		<xsl:variable name="shortName">
			<xsl:call-template name="PRINTER.comparison.short">
				<xsl:with-param name="COMPARISON_EVAL_RESULT" select="comparison-evaluation-result" />
				<xsl:with-param name="VERY_SHORT" select="true()" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="concat($shortName, ' | Comparison detail | SPL Results Overview')" />
			<xsl:with-param name="HEADING" select="'Comparison detail'" />
			<xsl:with-param name="BACKLINK_TARGET" select="$BACKLINK" />
			<xsl:with-param name="BACKLINK_NAME" select="'Back to formula'" />
			<xsl:with-param name="BODY_CLASS" select="'comparison-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.comparison.detail" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.comparison.detail">
		<xsl:variable name="comparisonResult" select="comparison-evaluation-result" />
		<xsl:variable name="comparison"
			select="/*/info/annotation-locations/annotation-location/formula-declaration//comparison[@fid eq $comparisonResult/@fref]" />
		<xsl:variable name="comparisonValidationFlags" select="comparison-validation-flags" />

		<xsl:variable name="leftMeasurementSample" select="left-measurement-sample" />
		<xsl:variable name="leftValidationFlags" select="left-sample-validation-flags" />

		<xsl:variable name="rightMeasurementSample" select="right-measurement-sample" />
		<xsl:variable name="rightValidationFlags" select="right-sample-validation-flags" />
		<div class="ul-drift section">
			<ul class="comparison-detail">
				<xsl:call-template name="PRINTER.comparison.list">
					<xsl:with-param name="COMPARISON_EVAL_RESULT"
						select="$comparisonResult" />
					<xsl:with-param name="COMPARISON" select="$comparison" />
				</xsl:call-template>
			</ul>
		</div>

		<xsl:call-template name="SCRIPT.visibility">
			<xsl:with-param name="VISIBLE" select="false()" />
			<xsl:with-param name="TITLE" select="'Operand declaration details'" />
			<xsl:with-param name="TEXT">
				<div class="section">
					<h3>Left operand</h3>
					<xsl:call-template name="PRINTER.measurementsample.info">
						<xsl:with-param name="MEASUREMENTSAMPLE" select="$leftMeasurementSample" />
						<xsl:with-param name="LAMBDA" select="$comparison/leftLambda" />
					</xsl:call-template>
				</div>
				<div class="section">
					<h3>Right operand</h3>
					<xsl:call-template name="PRINTER.measurementsample.info">
						<xsl:with-param name="MEASUREMENTSAMPLE" select="$rightMeasurementSample" />
						<xsl:with-param name="LAMBDA" select="$comparison/rightLambda" />
					</xsl:call-template>
				</div>
			</xsl:with-param>
		</xsl:call-template>

		<xsl:variable name="statisticalResult"
			select="$comparisonResult/comparison-result-T/@result" />
		<xsl:variable name="statisticalResultMWW"
			select="$comparisonResult/comparison-result-MWW/@result" />
		<xsl:variable name="statisticalResultKS"
			select="$comparisonResult/comparison-result-KS/@result" />
		<xsl:choose>
			<xsl:when test="$statisticalResult = 'NOT_COMPUTED'">
				<div class="section">
					<h2 class="error">Comparison result was not computed</h2>
					<xsl:choose>
						<xsl:when test="$comparisonResult/comparison-result-T/error-message">
							<xsl:for-each
								select="tokenize($comparisonResult/comparison-result-T/error-message, '\n')">
								<p>
									<xsl:value-of select="." />
								</p>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<p class="error">
								This usually means, that data for one or both
								measurement data samples were not found.
							</p>

						</xsl:otherwise>
					</xsl:choose>
					<p>
						<xsl:call-template name="LINKER.seeLog" />
					</p>
				</div>
			</xsl:when>
			<xsl:when test="$statisticalResult = ('OK', 'FAILED')">
				<xsl:variable name="isSatisfied" select="$statisticalResult eq 'OK'" />
				<xsl:variable name="isSatisfiedMWW" select="$statisticalResultMWW eq 'OK'" />

				<xsl:variable name="isSatisfiedKS" select="$statisticalResultKS eq 'OK'" />
			
				<div class="section">

					<h2>Comparison statistical data</h2>

					<h3> T Test</h3>
					<p> The T-Test compares the means of the sample distributions. This test is reliable when the data is approximately normal or sample size is very high (due to the Central Limit Theorem), it is however prone to false positives when this is not true.</p>
					<table class="comparison-statistical-data statistical-data">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="' Comparison evaluation result '" />
							<xsl:with-param name="VALUE" select="$statisticalResult" />
							<xsl:with-param name="VALID" select="$isSatisfied" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="$comparisonResult/comparison-result-T/@pValue" />
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="VALID" select="$isSatisfied" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'limit p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="/*/configuration/evaluation-configuration/evaluator.statistics/@t-test-limit-p-value" />
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
					</table>

					<h3>Mann Whitney Wilcoxon Test</h3>
					<p> The Mann Whitney Wilcoxon test compares the medians of two population. This test should be more accurate when dealing with non-normal distributions, especially when sample size is low.</p>
					<table class="comparison-statistical-data statistical-data">	
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="'Comparison evaluation result'" />
							<xsl:with-param name="VALUE" select="$statisticalResultMWW" />
							<xsl:with-param name="VALID" select="$isSatisfiedMWW" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="$comparisonResult/comparison-result-MWW/@pValue" />
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="VALID" select="$isSatisfiedMWW" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'limit p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="/*/configuration/evaluation-configuration/evaluator.statistics/@t-test-limit-p-value" />
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
					</table>
					<h3>Kolmogorov Smirnov Test</h3>
					<p> The Kolmogorov Smirnov Test, tests whether two distributions are equal, it is useful for testing whether the performance of one test is better than another over the whole distribution, see the Empirical Distribution graph below.</p>
					<table class="comparison-statistical-data statistical-data">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="'Comparison evaluation result'" />
							<xsl:with-param name="VALUE" select="$statisticalResultKS" />
							<xsl:with-param name="VALID" select="$isSatisfiedKS" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="$comparisonResult/comparison-result-KS/@pValue" />
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="VALID" select="$isSatisfiedKS" />
						</xsl:call-template>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'limit p-value'" />
							<xsl:with-param name="VALUE">
								<xsl:call-template name="PRINTER.format.pvalue">
									<xsl:with-param name="VALUE"
										select="/*/configuration/evaluation-configuration/evaluator.statistics/@t-test-limit-p-value" />
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>

					
					</table>


				</div>

				<div class="section">
					<h2>Samples statistical data</h2>
					<table class="comparison-samples-statistical-data statistical-data">
						<tr>
							<th></th>
							<th>Left sample</th>
							<th>Right sample</th>
						</tr>
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Sample count'" />
							<xsl:with-param name="VALUE"
								select="($leftMeasurementSample/sampleStatistics/@sampleCount, $rightMeasurementSample/sampleStatistics/@sampleCount)" />
							<xsl:with-param name="VALID"
								select="$leftValidationFlags/@sample-count-ok eq 'true' and $rightValidationFlags/@sample-count-ok eq 'true'" />
						</xsl:call-template>


						<!-- mean -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($leftMeasurementSample/sampleStatistics/@mean, $rightMeasurementSample/sampleStatistics/@mean)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.ns'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Mean'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
						</xsl:call-template>

						<!-- mean with lambda applied -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($comparisonValidationFlags/@left-mean-with-lambda, $comparisonValidationFlags/@right-mean-with-lambda)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.ns'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Mean (lambda applied)'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
						</xsl:call-template>

						<!-- computed lambda multiplier -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($comparisonValidationFlags/@left-lambda, $comparisonValidationFlags/@right-lambda)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.double'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="'Computed lambda multiplier'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
						</xsl:call-template>

						<!-- standard deviation -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($leftMeasurementSample/sampleStatistics/@standardDeviation, $rightMeasurementSample/sampleStatistics/@standardDeviation)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.ns'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Standard deviation'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
						</xsl:call-template>

						<!-- standard deviation / mean -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($leftValidationFlags/@std-vs-mean, $rightValidationFlags/@std-vs-mean)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.percent'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Standard deviation / mean'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
							<xsl:with-param name="VALID"
								select="$leftValidationFlags/@std-vs-mean-ok eq 'true' and $rightValidationFlags/@std-vs-mean-ok eq 'true'" />
						</xsl:call-template>

						<!-- median -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($leftMeasurementSample/sampleStatistics/@median, $rightMeasurementSample/sampleStatistics/@median)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.ns'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Median'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
						</xsl:call-template>

						<!-- median / mean -->
						<xsl:variable name="values">
							<xsl:call-template name="PRINTER.format.collection">
								<xsl:with-param name="COLLECTION"
									select="($leftValidationFlags/@median-vs-mean, $rightValidationFlags/@median-vs-mean)" />
								<xsl:with-param name="TEMPLATE" select="'PRINTER.format.percent'" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Median / mean'" />
							<xsl:with-param name="VALUE_SPAN" select="$values" />
							<xsl:with-param name="VALID"
								select="$leftValidationFlags/@median-vs-mean-ok eq 'true' and $rightValidationFlags/@median-vs-mean-ok eq 'true'" />
						</xsl:call-template>

					</table>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="section error">
					<h2>
						<xsl:call-template name="IMAGE.error" />
						Unexpected evaluation result
						[
						<xsl:value-of select="$statisticalResult" />
						]
					</h2>
				</div>
			</xsl:otherwise>
		</xsl:choose>


		<xsl:call-template name="PRINTER.graphs">
			<xsl:with-param name="GRAPHS" select="graphs/graph" />
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
