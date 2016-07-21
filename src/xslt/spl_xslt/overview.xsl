<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:param name="SUSPICOUS_MEASUREMENTS_COUNT" select="0" />

	<xsl:template match="overview-result-descriptor">
		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE" select="'Index | SPL Results Overview'" />
			<xsl:with-param name="HEADING" select="'SPL Results Overview'" />
			<xsl:with-param name="BODY_CLASS" select="'overview-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.overview" />
			</xsl:with-param>
			<xsl:with-param name="DISPLAY_HOME_LINK" select="false()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.overview">
		<xsl:variable name="overviewResult" select="annotation-evaluation-result" />
		<xsl:variable name="packages" select="packages" />
		<xsl:variable name="overviewValidationFlags" select="evaluation-summary" />

		<xsl:call-template name="SCRIPT.visibility">
			<xsl:with-param name="TITLE" select="'Alias declarations'" />
			<xsl:with-param name="VISIBLE" select="false()" />
			<xsl:with-param name="TITLE_TAG" select="'h2'" />
			<xsl:with-param name="TEXT">
				<p>
					Following alias declarations were declared globally.
					<br />
					Note that declarations with errors are not available
					for usage in
					formula as those were not
					parsed successfully.
				</p>

				<xsl:call-template name="PRINTER.alias.declarations">
					<xsl:with-param name="GLOBAL_SUMMARY" select="/*/global-aliases-summary" />
					<xsl:with-param name="GLOBAL_SUMMARY_TITLE"
						select="'Globally defined aliases summary'" />
					<xsl:with-param name="LOCAL_SUMMARY" select="$overviewValidationFlags" />
					<xsl:with-param name="LOCAL_SUMMARY_TITLE"
						select="'Aliases defined in all annotations'" />
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>

		<div class="section">
			<h2>Evaluation summary</h2>
			<xsl:call-template name="PRINTER.annotation.evaluation.summary">
				<xsl:with-param name="SUMMARY" select="$overviewValidationFlags" />
			</xsl:call-template>

			<xsl:choose>
				<xsl:when test="$packages/package">

					<div class="annotations-table section">
						<h3>Evaluated annotations</h3>

						<xsl:call-template name="SPL.page.overview.summary.table">
							<xsl:with-param name="PACKAGES" select="$packages/package" />
						</xsl:call-template>

					</div>

				</xsl:when>
				<xsl:otherwise>

					<div class="section">
						<h3>No annotations found</h3>
						<p>
							SPL Tools Framework did not found any SPL annotations to
							evaluate.
							This can happen in case that no SPL annotations were
							present in
							project with alias
							<strong>THIS</strong>
							or when compilation of project with alias
							<strong>THIS</strong>
							fails.
							<xsl:call-template name="LINKER.seeLog" />
						</p>
					</div>
				</xsl:otherwise>
			</xsl:choose>

			<div class="section">
				<h3>Additional execution details</h3>
				<p>
					See
					<xsl:call-template name="LINKER.link">
						<xsl:with-param name="HREF" select="'configuration.html'" />
						<xsl:with-param name="TEXT" select="'configuration page'" />
					</xsl:call-template>
					for detailed information about project, revisions and parameters.
					Note that some values obtained from INI configuration (such as machine
					access details) are not available in this report.
				</p>
				<xsl:choose>
					<xsl:when test="$SUSPICOUS_MEASUREMENTS_COUNT > 0">
						<p>
							See
							<xsl:call-template name="LINKER.link">
								<xsl:with-param name="HREF"
									select="'suspicious-measurements.html'" />
								<xsl:with-param name="TEXT"
									select="'suspicious measurements page'" />
							</xsl:call-template>
							for list of performed measurements witch are marked as
							<em>suspicious</em>
							(such as not enough samples, standard deviation / mean is too high
							or median / mean is too different).
						</p>
					</xsl:when>
					<xsl:otherwise>
						<p>
							No measurements marked as
							<em>suspicious</em>
							.
						</p>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when
						test="/*/info/measurements/measurement[measurement-state/@ok != 'true']">
						<p>
							See
							<xsl:call-template name="LINKER.link">
								<xsl:with-param name="HREF"
									select="'measurements-with-problems.html'" />
								<xsl:with-param name="TEXT"
									select="'measurements with problems page'" />
							</xsl:call-template>
							for detailed information about measurements with build,
							evaluation or other problems.
						</p>
					</xsl:when>
					<xsl:otherwise>
						<p>No measurements have build, evaluation or other problems.</p>
					</xsl:otherwise>
				</xsl:choose>
				<p>
					See
					<xsl:call-template name="LINKER.seeLog">
						<xsl:with-param name="TITLE" select="'execution log'" />
					</xsl:call-template>
					for detailed information about SPL Tools Framework execution
					events.
				</p>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="SPL.page.overview.summary.table">
		<xsl:param name="PACKAGES" />

		<table class="overview-summary-table">

			<tr>
				<th>Name</th>

				<th>
					<xsl:call-template name="IMAGE.ok">
						<xsl:with-param name="TOOLTIP" select="'Satisfied'" />
					</xsl:call-template>
				</th>
				<th>
					<xsl:call-template name="IMAGE.failed">
						<xsl:with-param name="TOOLTIP" select="'Failed'" />
					</xsl:call-template>
				</th>
				<th>
					<xsl:call-template name="IMAGE.unknown">
						<xsl:with-param name="TOOLTIP" select="'Undecidable'" />
					</xsl:call-template>
				</th>
				<th>
					<xsl:call-template name="IMAGE.error">
						<xsl:with-param name="TOOLTIP" select="'Not parsed'" />
					</xsl:call-template>
				</th>
				<th>
					<xsl:call-template name="IMAGE.sum">
						<xsl:with-param name="TOOLTIP" select="'All'" />
					</xsl:call-template>
				</th>
			</tr>

			<xsl:for-each select="$PACKAGES">

				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY">
						<xsl:call-template name="IMAGE.package" />
						<xsl:value-of select="spl:makeBreakable(@name)" />
					</xsl:with-param>
					<xsl:with-param name="VALUE"
						select="(sum(class/method/annotation/summary/@satisfied), sum(class/method/annotation/summary/@failed), sum(class/method/annotation/summary/@unknown), sum(class/method/annotation/summary/@not-parsed), sum(class/method/annotation/summary/@formulas))" />
					<xsl:with-param name="ROW_CLASS" select="'package-row'" />
				</xsl:call-template>

				<xsl:for-each select="class">

					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY">
							<xsl:call-template name="IMAGE.class" />
							<xsl:value-of select="spl:makeBreakable(@name)" />
						</xsl:with-param>
						<xsl:with-param name="VALUE"
							select="(sum(method/annotation/summary/@satisfied), sum(method/annotation/summary/@failed), sum(method/annotation/summary/@unknown), sum(method/annotation/summary/@not-parsed), sum(method/annotation/summary/@formulas))" />
						<xsl:with-param name="ROW_CLASS" select="'class-row'" />
					</xsl:call-template>

					<xsl:for-each select="method/annotation">

						<xsl:variable name="summary" select="summary" />
						<xsl:variable name="moreSatisfied"
							select="$summary/@satisfied >= $summary/@failed" />

						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY">
								<xsl:choose>
									<xsl:when test="$summary/@formulas = 0">
										<xsl:call-template name="IMAGE.warning">
											<xsl:with-param name="TOOLTIP" select="'No formulas declared'" />
										</xsl:call-template>
									</xsl:when>

									<xsl:otherwise>
										<xsl:if test="$summary/@satisfied > 0">
											<xsl:call-template name="IMAGE.ok">
												<xsl:with-param name="TOOLTIP"
													select="concat($summary/@satisfied, ' satisfied')" />
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="$summary/@failed > 0">
											<xsl:call-template name="IMAGE.failed">
												<xsl:with-param name="TOOLTIP"
													select="concat($summary/@failed, ' failed')" />
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="$summary/@unknown > 0">
											<xsl:call-template name="IMAGE.unknown">
												<xsl:with-param name="TOOLTIP"
													select="concat($summary/@unknown, ' undecidable')" />
											</xsl:call-template>
										</xsl:if>
										<xsl:if test="$summary/@not-parsed > 0">
											<xsl:call-template name="IMAGE.error">
												<xsl:with-param name="TOOLTIP"
													select="concat($summary/@not-parsed, ' not parsed')" />
											</xsl:call-template>
										</xsl:if>
									</xsl:otherwise>
								</xsl:choose>

								<xsl:call-template name="LINKER.link.smart">
									<xsl:with-param name="HREF_ID" select="@annotation-id" />
									<xsl:with-param name="TEXT" select="spl:makeBreakable(@name)" />
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="VALUE"
								select="($summary/@satisfied, $summary/@failed, $summary/@unknown, $summary/@not-parsed, $summary/@formulas)" />
							<xsl:with-param name="ROW_CLASS" select="'annotation-row'" />
						</xsl:call-template>

					</xsl:for-each>

				</xsl:for-each>

			</xsl:for-each>

		</table>

	</xsl:template>

</xsl:stylesheet>