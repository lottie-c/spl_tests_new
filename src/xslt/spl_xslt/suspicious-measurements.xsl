<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="suspicious-measurements-result-descriptor">
		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="'Suspicious measurements | SPL Results Overview'" />
			<xsl:with-param name="HEADING" select="'Suspicious measurements'" />
			<xsl:with-param name="BODY_CLASS" select="'suspicious-measurements-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.suspicious.measurements" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.suspicious.measurements">
		<p>
			Those measurements have not passed validation of measurement values
			check using following configuration.
		</p>

		<table class="statistical-table">

			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Minimum sample count'" />
				<xsl:with-param name="VALUE"
					select="/*/configuration/evaluation-configuration/evaluator.statistics/@minimum-sample-count-warning-limit" />
			</xsl:call-template>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY"
					select="'Maximum standard deviation / mean limit'" />
				<xsl:with-param name="VALUE">
					<xsl:value-of
						select="format-number(/*/configuration/evaluation-configuration/evaluator.statistics/@maximum-standard-deviation-vs-mean-difference-warning-limit, '#.00')" />
					%
				</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY"
					select="'Median / mean acceptable interval'" />
				<xsl:with-param name="VALUE">
					+-
					<xsl:value-of
						select="format-number(/*/configuration/evaluation-configuration/evaluator.statistics/@maximum-median-vs-mean-difference-warning-limit, '#.00')" />
					%
				</xsl:with-param>
			</xsl:call-template>

		</table>

		<p>
			Just click on table header column text to sort rows by values in
			the selected column.
		</p>

		<table class="sortable suspicious-measurements-table">
			<tr class="header">
				<th>Measurement</th>
				<th class="col1">Samples</th>
				<th class="col2 sorttable_nosort"></th>
				<th class="col3">
					Mean
					<br />
					<span class="unit">[ns]</span>
				</th>
				<th class="col4">
					Std
					<br />
					<span class="unit">[ns]</span>
				</th>
				<th class="col5">
					Median
					<br />
					<span class="unit">[ns]</span>
				</th>
				<th class="col6">
					Std/Mean
					<br />
					<span class="unit">[%]</span>
				</th>
				<th class="col7 sorttable_nosort"></th>
				<th class="col8">
					Median/Mean
					<br />
					<span class="unit">[%]</span>
				</th>
				<th class="col9 sorttable_nosort"></th>
			</tr>

			<xsl:for-each select="suspicious-measurement">
				<xsl:variable name="values">
					<span>
						<xsl:value-of select="@samples" />
					</span>
					<span>
						<xsl:if test="@is-samples-suspicious = 'true'">
							<xsl:call-template name="IMAGE.warning" />
						</xsl:if>
					</span>
					<span>
						<xsl:value-of select="format-number(@mean, '#.00')" />
					</span>
					<span>
						<xsl:value-of select="format-number(@std, '#.00')" />
					</span>
					<span>
						<xsl:value-of select="format-number(@median, '#.00')" />
					</span>
					<span>
						<xsl:value-of select="format-number(@std-vs-mean * 100, '#.00')" />
					</span>
					<span>
						<xsl:if test="@is-std-vs-mean-suspicious = 'true'">
							<xsl:call-template name="IMAGE.warning" />
						</xsl:if>
					</span>
					<span>
						<xsl:value-of select="format-number(@median-vs-mean * 100, '#.00')" />
					</span>
					<span>
						<xsl:if test="@is-median-vs-mean-suspicious = 'true'">
							<xsl:call-template name="IMAGE.warning" />
						</xsl:if>
					</span>
				</xsl:variable>


				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY">
						<xsl:call-template name="LINKER.link.smart">
							<xsl:with-param name="HREF_ID" select="@mref" />
							<xsl:with-param name="TEXT" select="spl:makeBreakable(@name)" />
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="VALUE_SPAN" select="$values" />
				</xsl:call-template>

			</xsl:for-each>

		</table>

	</xsl:template>

</xsl:stylesheet>