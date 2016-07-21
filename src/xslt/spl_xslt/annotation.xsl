<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="annotation-result-descriptor">
		<xsl:param name="annotationResult" select="annotation-evaluation-result" />
		<xsl:param name="annotationLocation"
			select="/*/info/annotation-locations/annotation-location[@aid eq $annotationResult/@aref]" />

		<xsl:variable name="shortName"
			select="concat($annotationLocation/@class, '.', $annotationLocation/@method, '(', $annotationLocation/@arguments-short, ')')" />


		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="concat($shortName, ' | Annotation detail | SPL Results Overview')" />
			<xsl:with-param name="HEADING" select="'Annotation detail'" />
			<xsl:with-param name="BODY_CLASS" select="'annotation-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.annotation.detail" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.annotation.detail">
		<xsl:param name="annotationResult" select="annotation-evaluation-result" />
		<xsl:param name="annotation"
			select="/*/info/annotation-locations/annotation-location[@aid eq $annotationResult/@aref]" />
		<xsl:param name="annotationValidationFlags" select="annotation-validation-flags" />

		<div class="section">
			<h2>Annotation location</h2>

			<p class="declaration-image annotation-location-signature">
				<xsl:value-of select="spl:makeBreakable($annotation/@basic-signature)" />
			</p>
		</div>

		<xsl:if
			test="/*/info/global-generators/generator-declaration or /*/info/global-methods/method-declaration or $annotation/generator-declaration or $annotation/method-declaration">
			<xsl:call-template name="SCRIPT.visibility">
				<xsl:with-param name="VISIBLE" select="false()" />
				<xsl:with-param name="TITLE_TAG" select="'h2'" />
				<xsl:with-param name="TITLE" select="'Alias declarations'" />
				<xsl:with-param name="TEXT">
					<p>
						Following alias declarations were declared globally or in this
						annotation.
						<br />
						Note that declarations with errors are not available
						for usage in
						formula as those were not parsed successfully.
					</p>

					<xsl:call-template name="PRINTER.alias.declarations">
						<xsl:with-param name="ANNOTATION" select="$annotation" />
						<xsl:with-param name="GLOBAL_SUMMARY" select="/*/global-aliases-summary" />
						<xsl:with-param name="GLOBAL_SUMMARY_TITLE"
							select="'Globally defined aliases summary'" />
						<xsl:with-param name="LOCAL_SUMMARY" select="$annotationValidationFlags" />
						<xsl:with-param name="LOCAL_SUMMARY_TITLE"
							select="'Locally defined aliases summary'" />
					</xsl:call-template>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>


		<h2>Evaluation summary</h2>

		<xsl:call-template name="PRINTER.annotation.evaluation.summary">
			<xsl:with-param name="SUMMARY" select="$annotationValidationFlags" />
		</xsl:call-template>

		<xsl:variable name="evaluatedFormulas"
			select="$annotationResult/formula-evaluation-results/formula-result" />
		<xsl:if test="$evaluatedFormulas">
			<div class="section">
				<h3>Evaluated formulas</h3>
				<div class="ul-drift">
					<ul class="declarations evaluated-formulas-root">
						<xsl:for-each select="$evaluatedFormulas">
							<xsl:variable name="iterated" select="." />
							<xsl:variable name="declaration"
								select="$annotation/formula-declaration[@pdid eq $iterated/@pdref]" />
							<xsl:choose>
								<xsl:when test="./@result = 'OK'">
									<xsl:call-template name="PRINTER.declaration">
										<xsl:with-param name="DECLARATION" select="$declaration" />
										<xsl:with-param name="MODE" select="'satisfied'" />
									</xsl:call-template>
								</xsl:when>
								<xsl:when test="./@result = 'FAILED'">
									<xsl:call-template name="PRINTER.declaration">
										<xsl:with-param name="DECLARATION" select="$declaration" />
										<xsl:with-param name="MODE" select="'failed'" />
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="PRINTER.declaration">
										<xsl:with-param name="DECLARATION" select="$declaration" />
										<xsl:with-param name="MODE" select="'unknown'" />
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</ul>
				</div>
			</div>
		</xsl:if>

		<xsl:variable name="values"
			select="$annotation/formula-declaration[not(@pdid = $evaluatedFormulas/@pdref)]" />
		<xsl:if test="$values">
			<div class="section">
				<h3>Formulas with parser or alias reference errors</h3>
				<p>Those formulas were not evaluated due to parser or reference
					integrity errors. See log file for the details.
				</p>
				<div class="not-parsed-formulas">
					<xsl:call-template name="PRINTER.declarations">
						<xsl:with-param name="DECLARATIONS" select="$values" />
					</xsl:call-template>
				</div>
			</div>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>