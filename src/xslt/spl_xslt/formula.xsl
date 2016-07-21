<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template match="formula-result-descriptor">
		<xsl:variable name="formulaResult" select="/*/flat-formula-evaluation-result" />
		<xsl:variable name="formula"
			select="/*/info/annotation-locations/annotation-location/formula-declaration[@pdid eq $formulaResult/@fdref]" />
		<xsl:variable name="formulaValidationFlags" select="formula-validation-flags" />
		<xsl:variable name="annotationLocation"
			select="$formula/ancestor::annotation-location" />

		<xsl:variable name="shortName"
			select="concat('[', count($formula/preceding-sibling::formula-declaration) + 1, '] ', $annotationLocation/@class, '.', $annotationLocation/@method, '(', $annotationLocation/@arguments-short, ')')" />

		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="concat($shortName, ' | Formula detail | SPL Results Overview')" />
			<xsl:with-param name="HEADING" select="'Formula detail'" />
			<xsl:with-param name="BACKLINK_TARGET" select="$BACKLINK"/>
			<xsl:with-param name="BACKLINK_NAME" select="'Back to annotation'"/>
			<xsl:with-param name="BODY_CLASS" select="'formula-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.formula.detail">
					<xsl:with-param name="formulaResult" select="$formulaResult" />
					<xsl:with-param name="formula" select="$formula" />
					<xsl:with-param name="formulaValidationFlags"
						select="$formulaValidationFlags" />
					<xsl:with-param name="annotationLocation" select="$annotationLocation" />
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.formula.detail">
		<xsl:param name="formulaResult" select="flat-formula-evaluation-result" />
		<xsl:param name="formula"
			select="/*/info/annotation-locations/annotation-location/formula-declaration[@pdid eq $formulaResult/@fdref]" />
		<xsl:param name="formulaValidationFlags" select="formula-validation-flags" />
		<xsl:param name="annotationLocation" select="$formula/ancestor::annotation-location" />

		<div class="section">
			<h2>Annotation location</h2>

			<p class="declaration-image annotation-location-signature">
				<xsl:value-of
					select="spl:makeBreakable($annotationLocation/@basic-signature)" />
			</p>
		</div>

		<h2>Formula image</h2>

		<p class="declaration-image">
			<xsl:value-of select="spl:makeBreakable($formula/image)" />
		</p>

		<xsl:if
			test="/*/info/global-generators/generator-declaration or /*/info/global-methods/method-declaration">
			<xsl:call-template name="SCRIPT.visibility">
				<xsl:with-param name="TITLE" select="'Alias declarations'" />
				<xsl:with-param name="TEXT">
					<p>
						Following alias declarations were declared globally or in
						annotation of this formula.
						<br />
						Note that declarations with errors are not available
						for usage in
						formula as those were not parsed successfully.
					</p>
					<xsl:call-template name="PRINTER.alias.declarations">
						<xsl:with-param name="ANNOTATION" select="$annotationLocation" />
					</xsl:call-template>
				</xsl:with-param>
				<xsl:with-param name="VISIBLE" select="false()" />
				<xsl:with-param name="TITLE_TAG" select="'h2'" />
			</xsl:call-template>
		</xsl:if>

		<div class="section">
			<h2>Formula fragments evaluation summary</h2>
			<xsl:call-template name="PRINTER.formula.comparison.summary">
				<xsl:with-param name="FORMULA" select="$formulaResult"/>
			</xsl:call-template>
		</div>

		<div class="formula-tree section">
			<h2>Results of formula fragments</h2>
			<ul class="formula-root ul-drift">
				<xsl:choose>
					<xsl:when test="$formulaResult/flat-logical-operation-evaluation-result">
						<xsl:call-template name="PRINTER.logicalOperation.list">
							<xsl:with-param name="LOGICAL_OPERATION"
								select="$formulaResult/flat-logical-operation-evaluation-result" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$formulaResult/flat-comparison-evaluation-result">
						<xsl:call-template name="PRINTER.comparison.list">
							<xsl:with-param name="COMPARISON_EVAL_RESULT"
								select="$formulaResult/flat-comparison-evaluation-result/comparison-evaluation-result" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<li class="error">
							<xsl:call-template name="IMAGE.error" />
							Unknown result type
						</li>
					</xsl:otherwise>
				</xsl:choose>
			</ul>
		</div>
	</xsl:template>

</xsl:stylesheet>