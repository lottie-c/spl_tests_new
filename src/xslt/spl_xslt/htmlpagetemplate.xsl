<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template name="SPL.page.template">
		<xsl:param name="TITLE" />
		<xsl:param name="HEADING" />
		<xsl:param name="BODY" />
		<xsl:param name="BODY_CLASS" select="''" />
		<xsl:param name="DISPLAY_HOME_LINK" select="true()" />
		<xsl:param name="BACKLINK_TARGET" select="''" />
		<xsl:param name="BACKLINK_NAME" select="''" />

		<html>
			<head>
				<title>
					<xsl:value-of select="$TITLE" />
				</title>
				<meta name="generator"
					content="SPL Tools Framework, www.sourceforge.net/projects/spl-tools/" />
				<meta http-equiv="content-type" content="text/html; charset=utf-8" />

				<link href="spl.png" rel="icon" type="image/png" />
				<link rel="stylesheet" type="text/css" href="main.css" />

				<script language="JavaScript" type="text/javascript">
					<xsl:text disable-output-escaping="yes">
					// toggles visibility for section created with SCRIPT.visibility template
					function toggleVisibility(item) {
						var bh = document.getElementById(item + '_link');
						var section = document.getElementById(item + '_section');
						
						if (section.style.display == 'block'){
							section.style.display = 'none';
							bh.innerHTML = 'Show';
						} else {
							section.style.display = 'block';
							bh.innerHTML = 'Hide';
						}
					}
					// checks if window is iframe and if so, than 
					// switch #wrap for #no-wrap for CSS stylesheet
					function checkForIframe() {
	                    if (window.location != window.parent.location){
	                      document.getElementById('wrap').id = 'no-wrap';
	                    }
					}
					</xsl:text>
				</script>
				<script src="sorttable.js" type="text/javascript">
				<xsl:text disable-output-escaping="yes"> </xsl:text>
				</script>
			</head>
			<body class="{$BODY_CLASS}" onLoad="javascript:checkForIframe();">
				<div id="wrap">
					<p id="breadcrumb">
						<!-- <a href="javascript:void(0)" onclick="self.history.back();">Back in history</a> -->
						<xsl:if test="$DISPLAY_HOME_LINK">
							<a href="index.html">Back to main page</a>
						</xsl:if>
						<xsl:if test="$BACKLINK_TARGET != ''">
							<a href="{$BACKLINK_TARGET}">
								<xsl:value-of select="$BACKLINK_NAME" />
							</a>
						</xsl:if>
						<a href="configuration.html">Configuration overview</a>
						<a href="spl.log" target="_blank">Execution log</a>
					</p>
					<div id="main">
						<h1>
							<xsl:copy-of select="$HEADING" />
						</h1>
						<xsl:copy-of select="$BODY" />
					</div>
					<xsl:if test="$CURRENT_TIME">
						<p id="footer">
							Generated at
							<xsl:value-of select="$CURRENT_TIME" />
							.
						</p>
					</xsl:if>
				</div>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>