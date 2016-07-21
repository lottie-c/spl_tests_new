<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:param name="CURRENT_TIME" select="''" />
	<xsl:param name="BACKLINK" select="''" />

	<xsl:include href="spl://htmlpagetemplate.xsl" />
	<xsl:include href="spl://annotation.xsl" />
	<xsl:include href="spl://comparison.xsl" />
	<xsl:include href="spl://formula.xsl" />
	<xsl:include href="spl://measurement.xsl" />
	<xsl:include href="spl://overview.xsl" />
	<xsl:include href="spl://measurements-with-problems.xsl" />
	<xsl:include href="spl://suspicious-measurements.xsl" />
	<xsl:include href="spl://configuration.xsl" />
	<xsl:include href="spl://shared.xsl" />

	<xsl:output method="xml"
		doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
		doctype-public="-//W3C//DTD XHTML 1.1//EN" indent="yes" />

	<xsl:template match="/">
		<!-- simple rule to load shared XML document -->
		<xsl:variable name="shared" select="document('spl://?shared-info.xml')" />

		<!-- add info to document we will transform -->
		<xsl:variable name="documentRootElement" select="name(*)" />
		<xsl:variable name="modifiedDocument">
			<xsl:element name="{$documentRootElement}">
				<xsl:copy-of select="/*/*" />
				<xsl:copy-of select="$shared/*/*" />
			</xsl:element>
		</xsl:variable>
		<xsl:apply-templates select="$modifiedDocument/*" />
	</xsl:template>

</xsl:stylesheet>