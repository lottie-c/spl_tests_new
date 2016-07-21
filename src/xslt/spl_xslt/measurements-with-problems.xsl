<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="measurements-with-problems-result-descriptor">
		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="'Measurements with problems | SPL Results Overview'" />
			<xsl:with-param name="HEADING" select="'Measurements with problems'" />
			<xsl:with-param name="BODY_CLASS"
				select="'measurements-with-problems-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.measurements.with.problems" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.measurements.with.problems">
		<xsl:variable name="measurements"
			select="/*/info/measurements/measurement[measurement-state/@ok != 'true']" />

		<xsl:variable name="build"
			select="$measurements[measurement-state/@last-phase = 'BUILD']" />

		<xsl:variable name="evaluate"
			select="$measurements[measurement-state/@last-phase = 'EVALUATE']" />
		<xsl:variable name="other"
			select="$measurements[not(measurement-state/@last-phase = ('BUILD','EVALUATE'))]" />

		<xsl:call-template
			name="SPL.page.measurements.with.problems.PRINTER.measurements">
			<xsl:with-param name="MEASUREMENTS" select="$build" />
			<xsl:with-param name="TITLE"
				select="'Measurements with build errors'" />
			<xsl:with-param name="IMAGE">
				<xsl:call-template name="IMAGE.failed.build" />
			</xsl:with-param>
		</xsl:call-template>

		<xsl:call-template
			name="SPL.page.measurements.with.problems.PRINTER.measurements">
			<xsl:with-param name="MEASUREMENTS" select="$evaluate" />
			<xsl:with-param name="TITLE"
				select="'Measurements with evaluation errors'" />
			<xsl:with-param name="IMAGE">
				<xsl:call-template name="IMAGE.failed.evaluate" />
			</xsl:with-param>
		</xsl:call-template>

		<xsl:call-template
			name="SPL.page.measurements.with.problems.PRINTER.measurements">
			<xsl:with-param name="MEASUREMENTS" select="$other" />
			<xsl:with-param name="TITLE"
				select="'Measurements with other errors'" />
			<xsl:with-param name="IMAGE">
				<xsl:call-template name="IMAGE.unknown" />
			</xsl:with-param>
		</xsl:call-template>


	</xsl:template>

	<xsl:template
		name="SPL.page.measurements.with.problems.PRINTER.measurements">
		<xsl:param name="MEASUREMENTS" />
		<xsl:param name="TITLE" select="''" />
		<xsl:param name="IMAGE" select="''" />

		<xsl:if test="$MEASUREMENTS">
			<h3>
				<xsl:copy-of select="$IMAGE" />
				<xsl:copy-of select="$TITLE" />
			</h3>
		</xsl:if>

		<xsl:for-each select="$MEASUREMENTS">
			<p>
				<xsl:copy-of select="$IMAGE" />
				<xsl:call-template name="LINKER.link.smart">
					<xsl:with-param name="HREF_ID" select="@msid" />
					<xsl:with-param name="TEXT">
						<xsl:value-of><!-- we don't want link to project or revision -->
							<xsl:call-template name="PRINTER.measurement.short">
								<xsl:with-param name="MEASUREMENT" select="." />
							</xsl:call-template>
						</xsl:value-of>
					</xsl:with-param>
				</xsl:call-template>
			</p>
			<xsl:call-template name="PRINTER.measurement.process.message">
				<xsl:with-param name="MEASUREMENT" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>