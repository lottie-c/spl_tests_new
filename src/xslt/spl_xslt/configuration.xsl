<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:variable name="configuration-page" select="configuration.html" />

	<xsl:template match="configuration-result-descriptor">
		<xsl:call-template name="SPL.page.template">
			<xsl:with-param name="TITLE"
				select="'Configuration details | SPL Results Overview'" />
			<xsl:with-param name="HEADING" select="'Configuration details'" />
			<xsl:with-param name="BODY_CLASS" select="'configuration-page'" />
			<xsl:with-param name="BODY">
				<xsl:call-template name="SPL.page.configuration" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="SPL.page.configuration">
		<xsl:variable name="info" select="/*/info" />
		<xsl:variable name="projects" select="$info/projects/project" />
		<xsl:variable name="parameters" select="$info/parameters/parameter" />
		<xsl:variable name="deploymentConfiguration"
			select="/*/configuration/deployment-configuration" />
		<xsl:variable name="evaluatorConfiguration"
			select="/*/configuration/evaluation-configuration" />

		<ul class="content-list ul-drift">
			<li>
				<xsl:call-template name="IMAGE.project" />
				<a href="#projects">
					Declared projects
				</a>
			</li>

			<ul>
				<xsl:for-each select="$projects">
					<li>
						<xsl:call-template name="IMAGE.project" />
						<a href="#project-{@pid}">
							<xsl:value-of select="spl:makeBreakable(alias)" />
						</a>
					</li>

				</xsl:for-each>
			</ul>

			<li>
				<xsl:choose>
					<xsl:when test="$parameters">
						<xsl:call-template name="IMAGE.configuration.parameters" />
						<a href="#configuration-parameters">
							Configuration parameters
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="IMAGE.configuration.parameters" />
						<a id="configuration-parameters">
							No configuration parameters set
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</li>


			<li>
				<xsl:call-template name="IMAGE.configuration.measurement" />
				<a href="#measurement-configuration">
					Measurement and deployment configuration
				</a>
			</li>

			<li>
				<xsl:call-template name="IMAGE.configuration.evaluator" />
				<a href="#evaluation-configuration">
					Evaluation configuration
				</a>
			</li>
		</ul>

		<h2>
			<a id="projects">Declared projects</a>
		</h2>

		<xsl:for-each select="$projects">
			<div class="section">
				<h3>
					<xsl:call-template name="IMAGE.project" />
					<a id="project-{@pid}">
						<xsl:value-of select="spl:makeBreakable(alias)" />
					</a>
				</h3>

				<xsl:variable name="repository" select="repository" />
				<table class="project-details-table">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Repository type'" />
						<xsl:with-param name="VALUE"
							select="spl:makeBreakable($repository/@type)" />
					</xsl:call-template>
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Repository URL'" />
						<xsl:with-param name="VALUE">
							<xsl:choose>
								<xsl:when
									test="starts-with($repository/@url, 'http://') or starts-with($repository/@url, 'https://')">
									<a href="{$repository/@url}" target="_blank">
										<xsl:value-of select="spl:makeBreakable($repository/@url)" />
									</a>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="spl:makeBreakable($repository/@url)" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>

					</xsl:call-template>
					<xsl:if test="build">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Build comand'" />
							<xsl:with-param name="VALUE" select="spl:makeBreakable(build)" />
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="classpaths">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Class paths'" />
							<xsl:with-param name="VALUE"
								select="spl:makeBreakable(string-join(classpaths/*,', '))" />
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="scan-patterns">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="'Scan patterns'" />
							<xsl:with-param name="VALUE"
								select="spl:makeBreakable(string-join(scan-patterns/*,', '))" />
						</xsl:call-template>
					</xsl:if>
				</table>

				<xsl:if test="$repository/revisions/revision">
					<h4>
						<xsl:call-template name="IMAGE.revisions" />
						Revisions
					</h4>
					<table class="revisions-table">
						<tr>
							<th>Alias</th>
							<th>Value</th>
							<th>
								<xsl:call-template name="IMAGE.tooltip">
									<xsl:with-param name="TOOLTIP" select="'Methods using revision'" />
								</xsl:call-template>
								M
							</th>
							<th>
								<xsl:call-template name="IMAGE.tooltip">
									<xsl:with-param name="TOOLTIP"
										select="'Generators using revision'" />
								</xsl:call-template>
								G
							</th>
							<th>
								<xsl:call-template name="IMAGE.tooltip">
									<xsl:with-param name="TOOLTIP"
										select="'Measurements using revision'" />
								</xsl:call-template>
								MS
							</th>
						</tr>
						<xsl:for-each select="$repository/revisions/revision">
							<xsl:variable name="values">
								<span>
									<a id="revision-{@rid}">
										<xsl:if test="comment">
											<xsl:call-template name="IMAGE.tooltip">
												<xsl:with-param name="TOOLTIP" select="comment" />
											</xsl:call-template>
										</xsl:if>
										<xsl:value-of select="spl:makeBreakable(alias)" />
									</a>
								</span>
								<span>
									<xsl:if test="identification">
										<xsl:call-template name="IMAGE.tooltip">
											<xsl:with-param name="TOOLTIP" select="identification" />
										</xsl:call-template>
									</xsl:if>
									<xsl:value-of select="spl:makeBreakable(value)" />
								</span>
								<xsl:variable name="revision" select="." />
								<xsl:variable name="generators"
									select="/*/info/generators/generator[revision/@rref eq $revision/@rid]/@gid" />
								<xsl:variable name="methods"
									select="/*/info/methods/method[revision/@rref eq $revision/@rid]/@mid" />
								<xsl:variable name="measurements"
									select="/*/info/measurements/measurement[@gref = $generators or @mref = @methods]" />
								<span>
									<xsl:value-of select="count($methods)" />
								</span>
								<span>
									<xsl:value-of select="count($generators)" />
								</span>
								<span>
									<xsl:value-of select="count($measurements)" />
								</span>
							</xsl:variable>
							<xsl:call-template name="PRINTER.tableRow">
								<xsl:with-param name="VALUE_SPAN" select="$values" />
							</xsl:call-template>
						</xsl:for-each>
					</table>
				</xsl:if>
			</div>
		</xsl:for-each>

		<xsl:if test="$parameters">
			<div class="section">
				<h2>
					<a id="configuration-parameters">
						<xsl:call-template name="IMAGE.configuration.parameters" />
						Configuration parameters
					</a>
				</h2>
				<table class="parameters-table">
					<xsl:for-each select="$parameters">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="VALUE"
								select="(spl:makeBreakable(name), spl:makeBreakable(value))" />
						</xsl:call-template>
					</xsl:for-each>
				</table>
			</div>
		</xsl:if>

		<div class="section">
			<h2>
				<a id="measurement-configuration">
					<xsl:call-template name="IMAGE.configuration.measurement" />
					Measurement and deployment configuration
				</a>
			</h2>
			<table class="parameters-table">
				<xsl:for-each select="$deploymentConfiguration/@*">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
						<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
					</xsl:call-template>
				</xsl:for-each>
			</table>
		</div>

		<div class="section">
			<h2>
				<a id="evaluation-configuration">
					<xsl:call-template name="IMAGE.configuration.evaluator" />
					Evaluation configuration
				</a>
			</h2>
			<table class="parameters-table">
				<xsl:for-each select="$evaluatorConfiguration/@*">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
						<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
					</xsl:call-template>
				</xsl:for-each>



				<xsl:for-each
					select="$evaluatorConfiguration/*[name(.) ne 'evaluator.graphs.colors' and name(.) ne 'evaluator.graphs.measurement' and name(.) ne 'evaluator.graphs.comparison']">
					<tr>
						<th colspan="2">
							<xsl:value-of select="name(.)" />
						</th>
					</tr>
					<xsl:for-each select="@*">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
							<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
						</xsl:call-template>
					</xsl:for-each>
				</xsl:for-each>

				<xsl:for-each select="$evaluatorConfiguration/evaluator.graphs.colors">
					<tr>
						<th colspan="2">
							<xsl:value-of select="spl:makeBreakable(name(.))" />
						</th>
					</tr>
					<xsl:for-each select="@*">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
							<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
						</xsl:call-template>
					</xsl:for-each>
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'text color'" />
						<xsl:with-param name="VALUE_SPAN">
							<span>
								<xsl:call-template name="PRINTER.color.node">
									<xsl:with-param name="COLOR" select="text-color" />
								</xsl:call-template>
							</span>
						</xsl:with-param>
					</xsl:call-template>

					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'background color'" />
						<xsl:with-param name="VALUE_SPAN">
							<span>
								<xsl:call-template name="PRINTER.color.node">
									<xsl:with-param name="COLOR" select="background-color" />
								</xsl:call-template>
							</span>
						</xsl:with-param>
					</xsl:call-template>

					<xsl:for-each select="sample-color">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="concat('color for sample ', position())" />
							<xsl:with-param name="VALUE_SPAN">
								<span>
									<xsl:call-template name="PRINTER.color.node">
										<xsl:with-param name="COLOR" select="." />
										<xsl:with-param name="PRINT_APLPHA"
											select="@background-transparent = 'true'" />
									</xsl:call-template>
								</span>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:for-each>
				</xsl:for-each>
				<xsl:for-each select="$evaluatorConfiguration/evaluator.graphs.measurement">
					<tr>
						<th colspan="2">
							<xsl:value-of select="spl:makeBreakable(name(.))" />
						</th>
					</tr>
					<xsl:for-each select="@*">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
							<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
						</xsl:call-template>
					</xsl:for-each>
					<xsl:for-each select="graph">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="concat('comparison graph ', position())" />
							<xsl:with-param name="VALUE" select="@type" />
						</xsl:call-template>
					</xsl:for-each>
				</xsl:for-each>
				<xsl:for-each select="$evaluatorConfiguration/evaluator.graphs.comparison">
					<tr>
						<th colspan="2">
							<xsl:value-of select="spl:makeBreakable(name(.))" />
						</th>
					</tr>
					<xsl:for-each select="@*">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY" select="spl:makeBreakable(name(.))" />
							<xsl:with-param name="VALUE" select="spl:makeBreakable(.)" />
						</xsl:call-template>
					</xsl:for-each>
					<xsl:for-each select="graph">
						<xsl:call-template name="PRINTER.tableRow">
							<xsl:with-param name="KEY"
								select="concat('comparison graph ', position())" />
							<xsl:with-param name="VALUE" select="@type" />
						</xsl:call-template>
					</xsl:for-each>
				</xsl:for-each>
			</table>
		</div>

	</xsl:template>

	<xsl:template name="PRINTER.color.node">
		<xsl:param name="COLOR" />
		<xsl:param name="PRINT_APLPHA" select="false()" />

		<span
			style="color:rgb({$COLOR/@red},{$COLOR/@green},{$COLOR/@blue});background-color:rgb({$COLOR/@red},{$COLOR/@green},{$COLOR/@blue});"
			class="color-box">X</span>

		rgb(
		<xsl:value-of select="$COLOR/@red" />
		,
		<xsl:value-of select="$COLOR/@green" />
		,
		<xsl:value-of select="$COLOR/@blue" />
		)
		<xsl:if test="$PRINT_APLPHA">
			with transparent alpha
			<xsl:value-of select="$COLOR/@alpha" />
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>