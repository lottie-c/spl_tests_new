<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="measurement-result-descriptor">
		<xsl:variable name="shortName">
			<xsl:call-template name="PRINTER.measurement.short">
				<xsl:with-param name="MEASUREMENT"
					select="/*/info/measurements/measurement[@msid = /*/measurement-sample/@msref]" />
				<xsl:with-param name="VERY_SHORT" select="true()" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="concat($shortName, ' | Measurement detail | SPL Results Overview')" />
			<xsl:with-param name="HEADING" select="'Measurement detail'" />
			<xsl:with-param name="BODY_CLASS" select="'measurement-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.measurement.detail" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.measurement.detail">
		<xsl:variable name="measurementSample" select="measurement-sample" />
		<xsl:variable name="measurement"
			select="/*/info/measurements/measurement[@msid = $measurementSample/@msref]" />
		<xsl:variable name="validationFlags" select="validation-flags" />

		<xsl:call-template name="PRINTER.measurementsample.info">
			<xsl:with-param name="MEASUREMENTSAMPLE" select="$measurementSample" />
			<xsl:with-param name="FULL" select="true()" />
		</xsl:call-template>

		<xsl:if test="$measurement/measurement-state/@ok != 'true'">

			<h2 class="error">Problem with measurement occured</h2>

			<table class="measurement-error">
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Last processed phase'" />
					<xsl:with-param name="VALUE"
						select="$measurement/measurement-state/@last-phase" />
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Message'" />
					<xsl:with-param name="VALUE"
						select="$measurement/measurement-state/message" />
				</xsl:call-template>
			</table>
			<p>
				<xsl:call-template name="LINKER.seeLog" />
			</p>
		</xsl:if>

		<xsl:if
			test="$measurement/measurement-state/@ok = 'true' or $measurement/measurement-state/@last-phase = 'EVALUATE'">

			<h2>Statistical data</h2>

			<table class="measurement-statistical-data statistical-data">
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Sample count'" />
					<xsl:with-param name="VALUE"
						select="$measurementSample/sampleStatistics/@sampleCount" />
					<xsl:with-param name="VALID"
						select="$validationFlags/@sample-count-ok" />
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Warmup count'" />
					<xsl:with-param name="VALUE"
						select="$measurementSample/sampleStatistics/@warmupCount" />
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Mean'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE"
								select="$measurementSample/sampleStatistics/@mean" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Standard deviation'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE"
								select="$measurementSample/sampleStatistics/@standardDeviation" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Standard deviation / mean'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.percent">
							<xsl:with-param name="VALUE"
								select="$validationFlags/@std-vs-mean" />
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="VALID"
						select="$validationFlags/@std-vs-mean-ok" />
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Median'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE"
								select="$measurementSample/sampleStatistics/@median" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Median / mean'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.percent">
							<xsl:with-param name="VALUE"
								select="$validationFlags/@median-vs-mean" />
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="VALID"
						select="$validationFlags/@median-vs-mean-ok" />
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Minimum'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE"
								select="$measurementSample/sampleStatistics/@minimum" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'Maximum'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE"
								select="$measurementSample/sampleStatistics/@maximum" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
			</table>

		</xsl:if>

		<xsl:call-template name="PRINTER.graphs">
			<xsl:with-param name="GRAPHS" select="graphs/graph" />
		</xsl:call-template>

		<h2>Links to comparisons with this measurement</h2>

		<xsl:variable name="comparisons"
			select="/*/info/annotation-locations/annotation-location/formula-declaration//comparison[*/@msref = $measurement/@msid]" />

		<ul>
			<xsl:for-each select="$comparisons">
				<xsl:call-template name="PRINTER.comparison.list">
					<xsl:with-param name="COMPARISON" select="." />
				</xsl:call-template>
			</xsl:for-each>
		</ul>
	</xsl:template>

</xsl:stylesheet>