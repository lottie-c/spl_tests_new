<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:spl="http://sourceforge.net/projects/spl-tools/">

	<xsl:template name="LINKER.link">
		<xsl:param name="HREF" />
		<xsl:param name="TEXT" />
		<xsl:param name="TARGET" select="''" />

		<a href="{$HREF}" target="{$TARGET}">
			<xsl:copy-of select="$TEXT" />
		</a>

	</xsl:template>

	<xsl:template name="LINKER.link.smart">
		<xsl:param name="HREF_ID" />
		<xsl:param name="TEXT" />
		<xsl:param name="TARGET" select="''" />

		<xsl:variable name="filename"
			select="/*/links/link[@ref eq $HREF_ID]/@filename" />

		<xsl:if test="$filename">
			<xsl:call-template name="LINKER.link">
				<xsl:with-param name="HREF" select="$filename" />
				<xsl:with-param name="TEXT" select="$TEXT" />
				<xsl:with-param name="TARGET" select="$TARGET" />
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="not($filename)">
			<xsl:copy-of select="$TEXT" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="LINKER.seeLog">
		<xsl:param name="TITLE" select="'See execution log for more information.'" />

		<xsl:call-template name="LINKER.link">
			<!-- TODO add file name through configuration -->
			<xsl:with-param name="HREF" select="'spl.log'" />
			<xsl:with-param name="TEXT" select="$TITLE" />
			<xsl:with-param name="TARGET" select="'_blank'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:function name="spl:toChars" as="xs:string*">
		<xsl:param name="arg" as="xs:string" />

		<xsl:sequence
			select=" 
	   for $ch in string-to-codepoints($arg)
	   return codepoints-to-string($ch)" />
	</xsl:function>

	<xsl:function name="spl:makeBreakableString" as="xs:string">
		<xsl:param name="arg" as="xs:string" />

		<xsl:value-of select="string-join(spl:toChars($arg), '&#8203;')" />
	</xsl:function>

	<xsl:function name="spl:makeBreakable" as="xs:string*">
		<xsl:param name="arg" as="item()*" />

		<xsl:sequence
			select="
			for $a in $arg
			return spl:makeBreakableString(string($a))
		" />
	</xsl:function>

	<xsl:template name="PRINTER.annotation.evaluation.summary">
		<xsl:param name="SUMMARY" />

		<table class="summary-data">
			<tr>
				<th />
				<th>
					<xsl:call-template name="IMAGE.ok" />
					Satisfied
				</th>
				<th>
					<xsl:call-template name="IMAGE.failed" />
					Failed
				</th>
				<th>
					<xsl:call-template name="IMAGE.unknown" />
					Undecidable
				</th>
				<th>
					<xsl:call-template name="IMAGE.error" />
					Not parsed
				</th>
				<th>
					<xsl:call-template name="IMAGE.sum" />
					All
				</th>
			</tr>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Formulas'" />
				<xsl:with-param name="VALUE"
					select="($SUMMARY/@satisfied, $SUMMARY/@failed, $SUMMARY/@unknown, $SUMMARY/@not-parsed, $SUMMARY/@formulas)" />
			</xsl:call-template>
		</table>
	</xsl:template>

	<xsl:template name="PRINTER.formula.comparison.summary">
		<xsl:param name="FORMULA" />

		<table class="summary-data">
			<tr>
				<th />
				<th>
					<xsl:call-template name="IMAGE.ok" />
					Satisfied
				</th>
				<th>
					<xsl:call-template name="IMAGE.failed" />
					Failed
				</th>
				<th>
					<xsl:call-template name="IMAGE.unknown" />
					Undecidable
				</th>
				<th>
					<xsl:call-template name="IMAGE.sum" />
					All
				</th>
			</tr>

			<xsl:variable name="logicalOperations"
				select="$FORMULA//flat-logical-operation-evaluation-result" />

			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Logical operations'" />
				<xsl:with-param name="VALUE"
					select="(count($logicalOperations[@result = 'OK']), count($logicalOperations[@result = 'FAILED']), count($logicalOperations[@result = 'NOT_COMPUTED']), count($logicalOperations) )" />
			</xsl:call-template>

			<xsl:variable name="comparisons"
				select="$FORMULA//flat-comparison-evaluation-result/comparison-evaluation-result/comparison-result-T" />
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Comparisons'" />
				<xsl:with-param name="VALUE"
					select="(count($comparisons[@result = 'OK']), count($comparisons[@result = 'FAILED']), count($comparisons[@result = 'NOT_COMPUTED']), count($comparisons) )" />
			</xsl:call-template>


		</table>
	</xsl:template>

	<xsl:template name="PRINTER.alias.summary">
		<xsl:param name="SUMMARY" />
		<xsl:param name="SUMMARY_TITLE" />

		<xsl:if test="$SUMMARY/@generator-aliases + $SUMMARY/@method-aliases > 0">
			<h3>
				<xsl:value-of select="$SUMMARY_TITLE" />
			</h3>
			<table class="statistical-data">
				<tr>
					<th />
					<th>
						<xsl:call-template name="IMAGE.ok" />
						Valid
					</th>
					<th>
						<xsl:call-template name="IMAGE.warning" />
						With warnings
					</th>
					<th>
						<xsl:call-template name="IMAGE.error" />
						Not parsed
					</th>
					<th>
						<xsl:call-template name="IMAGE.sum" />
						All
					</th>
				</tr>
				<xsl:if test="$SUMMARY/@generator-aliases > 0">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Generator aliases'" />
						<xsl:with-param name="VALUE"
							select="($SUMMARY/@generator-aliases-ok, $SUMMARY/@generator-aliases-warnings, $SUMMARY/@generator-aliases-errors, $SUMMARY/@generator-aliases)" />
					</xsl:call-template>
				</xsl:if>
				<xsl:if test=" $SUMMARY/@method-aliases > 0">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Method aliases'" />
						<xsl:with-param name="VALUE"
							select="($SUMMARY/@method-aliases-ok, $SUMMARY/@method-aliases-warnings, $SUMMARY/@method-aliases-errors, $SUMMARY/@method-aliases)" />
					</xsl:call-template>
				</xsl:if>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.alias.declarations">
		<xsl:param name="ANNOTATION" select="''" />
		<xsl:param name="LOCAL_SUMMARY" />
		<xsl:param name="LOCAL_SUMMARY_TITLE" />
		<xsl:param name="GLOBAL_SUMMARY" />
		<xsl:param name="GLOBAL_SUMMARY_TITLE" />

		<div class="alias-declarations">
			<!-- global aliases summary -->
			<xsl:if test="$GLOBAL_SUMMARY">
				<xsl:call-template name="PRINTER.alias.summary">
					<xsl:with-param name="SUMMARY" select="$GLOBAL_SUMMARY" />
					<xsl:with-param name="SUMMARY_TITLE" select="$GLOBAL_SUMMARY_TITLE" />
				</xsl:call-template>
			</xsl:if>

			<!-- global generators -->
			<xsl:variable name="values"
				select="/*/info/global-generators/generator-declaration" />
			<xsl:if test="$values">
				<h3>Global generators</h3>
				<xsl:call-template name="PRINTER.declarations">
					<xsl:with-param name="DECLARATIONS" select="$values" />
				</xsl:call-template>
			</xsl:if>
			<!-- global methods -->
			<xsl:variable name="values"
				select="/*/info/global-methods/method-declaration" />
			<xsl:if test="$values">
				<h3>Global methods</h3>
				<xsl:call-template name="PRINTER.declarations">
					<xsl:with-param name="DECLARATIONS" select="$values" />
				</xsl:call-template>
			</xsl:if>

			<!-- local aliases summary -->
			<xsl:if test="$LOCAL_SUMMARY">
				<xsl:call-template name="PRINTER.alias.summary">
					<xsl:with-param name="SUMMARY" select="$LOCAL_SUMMARY" />
					<xsl:with-param name="SUMMARY_TITLE" select="$LOCAL_SUMMARY_TITLE" />
				</xsl:call-template>
			</xsl:if>

			<xsl:if test="$ANNOTATION">
				<!-- local generators -->
				<xsl:variable name="values" select="$ANNOTATION/generator-declaration" />
				<xsl:if test="$values">
					<h3>Local generators</h3>
					<xsl:call-template name="PRINTER.declarations">
						<xsl:with-param name="DECLARATIONS" select="$values" />
					</xsl:call-template>
				</xsl:if>

				<!-- local methods -->
				<xsl:variable name="values" select="$ANNOTATION/method-declaration" />
				<xsl:if test="$values">
					<h3>Local methods</h3>
					<xsl:call-template name="PRINTER.declarations">
						<xsl:with-param name="DECLARATIONS" select="$values" />
					</xsl:call-template>
				</xsl:if>

			</xsl:if>
		</div>
	</xsl:template>

	<xsl:template name="PRINTER.declarations">
		<xsl:param name="DECLARATIONS" />

		<div class="ul-drift">
			<ul class="declarations">
				<xsl:for-each select="$DECLARATIONS">
					<xsl:call-template name="PRINTER.declaration">
						<xsl:with-param name="DECLARATION" select="." />
					</xsl:call-template>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>

	<xsl:template name="PRINTER.declaration">
		<xsl:param name="DECLARATION" />
		<xsl:param name="MODE" select="''" />

		<li classname="declaration parsed-declaration">
			<xsl:variable name="errors" select="$DECLARATION/error" />
			<xsl:variable name="warnings" select="$DECLARATION/warning" />
			<p>
				<xsl:choose>
					<xsl:when test="$MODE = 'satisfied'">
						<xsl:call-template name="IMAGE.ok" />
					</xsl:when>
					<xsl:when test="$MODE = 'failed'">
						<xsl:call-template name="IMAGE.failed" />
					</xsl:when>
					<xsl:when test="$MODE = 'unknown'">
						<xsl:call-template name="IMAGE.unknown" />
					</xsl:when>
					<xsl:when test="$errors">
						<xsl:call-template name="IMAGE.error" />
					</xsl:when>
					<xsl:when test="$warnings">
						<xsl:call-template name="IMAGE.warning" />
					</xsl:when>
					<xsl:when test="not($errors) and not($warnings)">
						<xsl:call-template name="IMAGE.ok" />
					</xsl:when>
				</xsl:choose>

				<span class="declaration-image">
					<xsl:call-template name="LINKER.link.smart">
						<xsl:with-param name="HREF_ID" select="$DECLARATION/@pdid" />
						<xsl:with-param name="TEXT"
							select="spl:makeBreakable($DECLARATION/image)" />
					</xsl:call-template>
				</span>
			</p>
			<xsl:if test="$errors or $warnings">
				<ul class="declaration-problems">
					<xsl:for-each select="$errors">
						<li>
							<xsl:call-template name="IMAGE.error" />
							<span class="error">
								<xsl:value-of select="./text" />
							</span>
						</li>
					</xsl:for-each>
					<xsl:for-each select="$warnings">
						<li>
							<xsl:call-template name="IMAGE.warning" />
							<span class="warning">
								<xsl:value-of select="./text" />
							</span>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>
		</li>
	</xsl:template>

	<xsl:template name="PRINTER.logicalOperation.list">
		<xsl:param name="LOGICAL_OPERATION" />

		<li class="logical-operation">
			<xsl:choose>
				<xsl:when test="$LOGICAL_OPERATION/@result eq 'OK'">
					<xsl:call-template name="IMAGE.ok" />
				</xsl:when>
				<xsl:when test="$LOGICAL_OPERATION/@result eq 'FAILED'">
					<xsl:call-template name="IMAGE.failed" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="IMAGE.unknown" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="$LOGICAL_OPERATION/@logical-operator" />
		</li>

		<xsl:variable name="comparisons"
			select="$LOGICAL_OPERATION/operands/flat-comparison-evaluation-result/comparison-evaluation-result" />
		<xsl:variable name="logicalOperations"
			select="$LOGICAL_OPERATION/operands/flat-logical-operation-evaluation-result" />
		<xsl:if test="$comparisons">
			<ul class="operands">
				<xsl:for-each select="$comparisons">
					<xsl:call-template name="PRINTER.comparison.list">
						<xsl:with-param name="COMPARISON_EVAL_RESULT"
							select="." />
					</xsl:call-template>
				</xsl:for-each>
			</ul>
		</xsl:if>
		<xsl:if test="logicalOperations">
			<ul class="operands">
				<xsl:for-each select="logicalOperations">
					<xsl:call-template name="PRINTER.logicalOperation.list">
						<xsl:with-param name="LOGICAL_OPERATION" select="." />
					</xsl:call-template>
				</xsl:for-each>
			</ul>
		</xsl:if>


	</xsl:template>

	<xsl:template name="PRINTER.comparison.list">
		<xsl:param name="COMPARISON_EVAL_RESULT" />
		<xsl:param name="COMPARISON"
			select="/*/info/annotation-locations/annotation-location/formula-declaration//comparison[@fid eq $COMPARISON_EVAL_RESULT/@fref]" />

		<li class="comparison">
			<xsl:if test="$COMPARISON_EVAL_RESULT">
				<xsl:variable name="statisticalResult"
					select="$COMPARISON_EVAL_RESULT/comparison-result-T/@result" />
				<xsl:choose>
					<xsl:when test="$statisticalResult eq 'OK'">
						<xsl:call-template name="IMAGE.ok">
							<xsl:with-param name="TOOLTIP"
								select="concat('t-test p-value: ', $COMPARISON_EVAL_RESULT/comparison-result-T/@pValue)" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$statisticalResult eq 'FAILED'">
						<xsl:call-template name="IMAGE.failed">
							<xsl:with-param name="TOOLTIP"
								select="concat('t-test p-value: ', $COMPARISON_EVAL_RESULT/comparison-result-T/@pValue)" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$statisticalResult eq 'NOT_COMPUTED'">
						<xsl:call-template name="IMAGE.unknown">
							<xsl:with-param name="TOOLTIP"
								select="$COMPARISON_EVAL_RESULT/comparison-result-T/error-message" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						[
						<xsl:value-of select="$statisticalResult" />
						]
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:call-template name="LINKER.link.smart">
				<xsl:with-param name="HREF_ID" select="$COMPARISON/@fid" />
				<xsl:with-param name="TEXT">
					<span class="comparison-name">
						<xsl:variable name="sign" select="$COMPARISON/sign/@type" />
						<xsl:choose>
							<xsl:when test="$sign eq 'EQI'">
								=
							</xsl:when>
							<xsl:when test="$sign eq 'EQ'">
								==
							</xsl:when>
							<xsl:when test="$sign eq 'GE'">
								&gt;=
							</xsl:when>
							<xsl:when test="$sign eq 'GT'">
								&gt;
							</xsl:when>
							<xsl:when test="$sign eq 'LE'">
								&lt;=
							</xsl:when>
							<xsl:when test="$sign eq 'LT'">
								&lt;
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$sign" />
							</xsl:otherwise>
						</xsl:choose>
						(
						<xsl:call-template name="PRINTER.lambda">
							<xsl:with-param name="LAMBDA" select="$COMPARISON/leftLambda" />
						</xsl:call-template>
						,
						<xsl:call-template name="PRINTER.lambda">
							<xsl:with-param name="LAMBDA" select="$COMPARISON/rightLambda" />
						</xsl:call-template>
						<xsl:if test="$sign eq 'EQI'">
							, +-
							<xsl:choose>
								<xsl:when test="$COMPARISON/@equality-interval">
									<xsl:value-of select="$COMPARISON/@equality-interval * 100" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of
										select="/*/configuration/evaluation-configuration/evaluator.statistics/@default-equality-interval * 100" />
								</xsl:otherwise>
							</xsl:choose>
							%
						</xsl:if>
						)
					</span>
				</xsl:with-param>
			</xsl:call-template>
		</li>
		<ul class="measurement-tree">
			<li class="measurement">
				<xsl:variable name="leftMeasurement"
					select="/*/info/measurements/measurement[@msid eq $COMPARISON/leftMethod/@msref]" />
				<xsl:call-template name="PRINTER.measurement.process.status">
					<xsl:with-param name="MEASUREMENT" select="$leftMeasurement" />
				</xsl:call-template>
				<xsl:call-template name="LINKER.link.smart">
					<xsl:with-param name="HREF_ID" select="$leftMeasurement/@msid" />
					<xsl:with-param name="TEXT">
						<xsl:value-of><!-- we don't want link to project or revision -->
							<xsl:call-template name="PRINTER.measurement.short">
								<xsl:with-param name="MEASUREMENT" select="$leftMeasurement" />
							</xsl:call-template>
						</xsl:value-of>
					</xsl:with-param>
				</xsl:call-template>

			</li>
			<li class="measurement">
				<xsl:variable name="rightMeasurement"
					select="/*/info/measurements/measurement[@msid eq $COMPARISON/rightMethod/@msref]" />
				<xsl:call-template name="PRINTER.measurement.process.status">
					<xsl:with-param name="MEASUREMENT" select="$rightMeasurement" />
				</xsl:call-template>
				<xsl:call-template name="LINKER.link.smart">
					<xsl:with-param name="HREF_ID" select="$rightMeasurement/@msid" />
					<xsl:with-param name="TEXT">
						<xsl:value-of><!-- we don't want link to project or revision -->
							<xsl:call-template name="PRINTER.measurement.short">
								<xsl:with-param name="MEASUREMENT" select="$rightMeasurement" />
							</xsl:call-template>
						</xsl:value-of>
					</xsl:with-param>
				</xsl:call-template>
			</li>
		</ul>
	</xsl:template>

	<xsl:template name="PRINTER.comparison.short">
		<xsl:param name="COMPARISON_EVAL_RESULT" />
		<xsl:param name="COMPARISON"
			select="/*/info/annotation-locations/annotation-location/formula-declaration//comparison[@fid eq $COMPARISON_EVAL_RESULT/@fref]" />
		<xsl:param name="VERY_SHORT" select="false()" />


		<xsl:variable name="sign" select="$COMPARISON/sign/@type" />
		<xsl:choose>
			<xsl:when test="$sign eq 'EQ'">
				=
			</xsl:when>
			<xsl:when test="$sign eq 'GE'">
				&gt;=
			</xsl:when>
			<xsl:when test="$sign eq 'GT'">
				&gt;
			</xsl:when>
			<xsl:when test="$sign eq 'LE'">
				&lt;=
			</xsl:when>
			<xsl:when test="$sign eq 'LT'">
				&lt;
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$sign" />
			</xsl:otherwise>
		</xsl:choose>
		(
		<xsl:call-template name="PRINTER.lambda">
			<xsl:with-param name="LAMBDA" select="$COMPARISON/leftLambda" />
		</xsl:call-template>
		,
		<xsl:call-template name="PRINTER.lambda">
			<xsl:with-param name="LAMBDA" select="$COMPARISON/rightLambda" />
		</xsl:call-template>
		)
		{
		<xsl:variable name="leftMeasurement"
			select="/*/info/measurements/measurement[@msid eq $COMPARISON/leftMethod/@msref]" />
		<xsl:call-template name="PRINTER.measurement.short">
			<xsl:with-param name="MEASUREMENT" select="$leftMeasurement" />
			<xsl:with-param name="VERY_SHORT" select="$VERY_SHORT" />
		</xsl:call-template>
		,
		<xsl:variable name="rightMeasurement"
			select="/*/info/measurements/measurement[@msid eq $COMPARISON/rightMethod/@msref]" />
		<xsl:call-template name="PRINTER.measurement.short">
			<xsl:with-param name="MEASUREMENT" select="$rightMeasurement" />
			<xsl:with-param name="VERY_SHORT" select="$VERY_SHORT" />
		</xsl:call-template>
		}
	</xsl:template>

	<xsl:template name="PRINTER.lambda">
		<xsl:param name="LAMBDA" />

		<xsl:for-each select="$LAMBDA/const">
			<xsl:value-of select="." />
			<xsl:value-of select="'*'" />
		</xsl:for-each>
		<xsl:for-each select="$LAMBDA/parameter">
			<xsl:value-of select="." />
			<xsl:value-of select="'*'" />
		</xsl:for-each>
		x
	</xsl:template>

	<xsl:template name="PRINTER.measurementsample.info">
		<xsl:param name="MEASUREMENTSAMPLE" />
		<xsl:param name="FULL" select="false()" />
		<xsl:param name="LAMBDA" select="''" />

		<xsl:variable name="measurement"
			select="/*/info/measurements/measurement[@msid = $MEASUREMENTSAMPLE/@msref]" />
		<xsl:variable name="generator"
			select="/*/info/generators/generator[@gid = $measurement/@gref]" />
		<xsl:variable name="method"
			select="/*/info/methods/method[@mid = $measurement/@mref]" />
		<xsl:variable name="validationFlags" select="validation-flags" />

		<table class="measurement-detail-table">
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Method'" />
				<xsl:with-param name="VALUE_SPAN">
					<span>
						<xsl:call-template name="PRINTER.method.full">
							<xsl:with-param name="METHOD" select="$method" />
						</xsl:call-template>
					</span>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Generator'" />
				<xsl:with-param name="VALUE_SPAN">
					<span>
						<xsl:call-template name="PRINTER.generator.full">
							<xsl:with-param name="GENERATOR" select="$generator" />
						</xsl:call-template>
					</span>
				</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Generator parameters'" />
				<xsl:with-param name="VALUE">
					<xsl:choose>
						<xsl:when test="$measurement/variables/variable">
							<xsl:value-of
								select="string-join($measurement/variables/variable, ', ')" />
						</xsl:when>
						<xsl:otherwise>
							no parameters specified
						</xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>

			<xsl:if test="$FULL">
				<xsl:if test="$MEASUREMENTSAMPLE/sampleStatistics/@measuredDate">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Measured date'" />
						<xsl:with-param name="VALUE"
							select="$MEASUREMENTSAMPLE/sampleStatistics/@measuredDate" />
					</xsl:call-template>
				</xsl:if>

				<xsl:if test="$measurement/@computer-name">
					<xsl:call-template name="PRINTER.tableRow">
						<xsl:with-param name="KEY" select="'Measured on'" />
						<xsl:with-param name="VALUE"
							select="concat($measurement/@computer-name,' (', $measurement/@computer-id,')')" />
					</xsl:call-template>
				</xsl:if>
			</xsl:if>
			<xsl:if test="not($LAMBDA = '')">
				<xsl:call-template name="PRINTER.tableRow">
					<xsl:with-param name="KEY" select="'λ(x)'" />
					<xsl:with-param name="VALUE">
						<xsl:call-template name="PRINTER.lambda">
							<xsl:with-param name="LAMBDA" select="$LAMBDA" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:if>
		</table>

	</xsl:template>

	<xsl:template name="PRINTER.measurement.process.status">
		<xsl:param name="MEASUREMENT" />

		<xsl:if test="$MEASUREMENT/measurement-state/@ok = 'true'">
			<xsl:call-template name="IMAGE.processed.evaluate">
				<xsl:with-param name="TOOLTIP"
					select="'No issues reported for this measurement.'" />
			</xsl:call-template>
		</xsl:if>

		<xsl:if test="$MEASUREMENT/measurement-state/@ok != 'true'">
			<xsl:choose>
				<xsl:when test="$MEASUREMENT/measurement-state/@last-phase = 'BUILD'">
					<xsl:call-template name="IMAGE.failed.build">
						<xsl:with-param name="TOOLTIP"
							select="$MEASUREMENT/measurement-state/message" />
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="$MEASUREMENT/measurement-state/@last-phase = 'EVALUATE'">
					<xsl:call-template name="IMAGE.failed.evaluate">
						<xsl:with-param name="TOOLTIP"
							select="$MEASUREMENT/measurement-state/message" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="IMAGE.unknown">
						<xsl:with-param name="TOOLTIP"
							select="concat($MEASUREMENT/measurement-state/@last-phase,': ', MEASUREMENT/measurement-state/message)" />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.measurement.process.message">
		<xsl:param name="MEASUREMENT" />

		<table class="measurement-error">
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Last processed phase'" />
				<xsl:with-param name="VALUE"
					select="$MEASUREMENT/measurement-state/@last-phase" />
			</xsl:call-template>
			<xsl:call-template name="PRINTER.tableRow">
				<xsl:with-param name="KEY" select="'Message'" />
				<xsl:with-param name="VALUE"
					select="$MEASUREMENT/measurement-state/message" />
			</xsl:call-template>
		</table>

	</xsl:template>

	<xsl:template name="PRINTER.graphs">
		<xsl:param name="GRAPHS" />

		<xsl:if test="$GRAPHS">
			<div class="section">
				<xsl:for-each select="$GRAPHS">
					<h3>
						<xsl:value-of select="graph-name" />
					</h3>
					<div class="graph">
						<img src="{filename}" border="0" alt="{graph-name}"
							width="{/*/configuration/evaluation-configuration/evaluator.graphs/@graph-image-width}"
							height="{/*/configuration/evaluation-configuration/evaluator.graphs/@graph-image-height}" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.format.ns">
		<xsl:param name="VALUE" />
		<xsl:choose>
			<xsl:when test="$VALUE > 10000000">
				<xsl:value-of select="format-number($VALUE div 1000000, '#0.00')" />
				ms
			</xsl:when>
			<xsl:when test="$VALUE > 10000">
				<xsl:value-of select="format-number($VALUE div 1000, '#0.00')" />
				µs
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="format-number($VALUE, '#0.00')" />
				ns
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="PRINTER.format.double">
		<xsl:param name="VALUE" />

		<xsl:value-of select="format-number($VALUE, '#0.00')" />
	</xsl:template>

	<xsl:template name="PRINTER.format.collection">
		<xsl:param name="COLLECTION" />
		<xsl:param name="TEMPLATE" />

		<xsl:for-each select="$COLLECTION">
			<span>
				<xsl:choose>
					<xsl:when test="$TEMPLATE eq 'PRINTER.format.ns'">
						<xsl:call-template name="PRINTER.format.ns">
							<xsl:with-param name="VALUE" select="." />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$TEMPLATE eq 'PRINTER.format.percent'">
						<xsl:call-template name="PRINTER.format.percent">
							<xsl:with-param name="VALUE" select="." />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$TEMPLATE eq 'PRINTER.format.pvalue'">
						<xsl:call-template name="PRINTER.format.pvalue">
							<xsl:with-param name="VALUE" select="." />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$TEMPLATE eq 'PRINTER.format.double'">
						<xsl:call-template name="PRINTER.format.double">
							<xsl:with-param name="VALUE" select="." />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="." />
					</xsl:otherwise>
				</xsl:choose>
			</span>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="PRINTER.format.pvalue">
		<xsl:param name="VALUE" />
		<xsl:value-of select="format-number($VALUE, '#0.0000')" />
	</xsl:template>

	<!-- converts double number to percent presentation, number is in interval 
		[0..1] -->
	<xsl:template name="PRINTER.format.percent">
		<xsl:param name="VALUE" />

		<xsl:value-of select="format-number($VALUE * 100, '#.00')" />
		%
	</xsl:template>

	<xsl:template name="PRINTER.tableRow">
		<xsl:param name="KEY" />
		<xsl:param name="VALUE" select="()" />
		<xsl:param name="VALUE_SPAN" select="()" />
		<xsl:param name="VALID" select="''" />
		<xsl:param name="ROW_CLASS" />

		<tr class="{$ROW_CLASS}">
			<xsl:if test="$KEY">
				<td class="key">
					<xsl:choose>
						<xsl:when test="string($VALID) = ('true')">
							<xsl:call-template name="IMAGE.ok" />
						</xsl:when>
						<xsl:when test="string($VALID) = ('false')">
							<xsl:call-template name="IMAGE.warning" />
						</xsl:when>
						<xsl:when test="$VALID = ''" />
					</xsl:choose>
					<xsl:copy-of select="$KEY" />
				</td>
			</xsl:if>
			<xsl:for-each select="$VALUE">
				<td class="value col{position()}">
					<xsl:value-of select="." />
				</td>
			</xsl:for-each>
			<xsl:for-each select="$VALUE_SPAN/span">
				<td class="value col{position()}">
					<xsl:copy-of select="node()" />
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template name="PRINTER.project.revision">
		<xsl:param name="REVISION" />

		<xsl:variable name="project" select="$REVISION/ancestor::project" />

		<a href="configuration.html#project-{$project/@pid}">
			<xsl:value-of select="$project/alias" />
		</a>
		@
		<a href="configuration.html#revision-{$REVISION/@rid}">
			<xsl:value-of select="$REVISION/alias" />
		</a>
	</xsl:template>

	<xsl:template name="PRINTER.measurement.short">
		<xsl:param name="MEASUREMENT" />
		<xsl:param name="VERY_SHORT" select="false()" />

		<xsl:call-template name="PRINTER.method.short">
			<xsl:with-param name="METHOD"
				select="/*/info/methods/method[@mid eq $MEASUREMENT/@mref]" />
			<xsl:with-param name="VERY_SHORT" select="$VERY_SHORT" />
		</xsl:call-template>
		[
		<xsl:call-template name="PRINTER.generator.short">
			<xsl:with-param name="GENERATOR"
				select="/*/info/generators/generator[@gid eq $MEASUREMENT/@gref]" />
			<xsl:with-param name="VERY_SHORT" select="$VERY_SHORT" />
		</xsl:call-template>
		]
		<xsl:value-of
			select="concat('(',string-join($MEASUREMENT/variables/variable, ', '),')')" />

	</xsl:template>

	<xsl:template name="PRINTER.generator.full">
		<xsl:param name="GENERATOR" />

		<xsl:variable name="revision"
			select="/*/info/projects/project/repository/revisions/revision[@rid eq $GENERATOR/revision/@rref]" />

		<xsl:call-template name="PRINTER.project.revision">
			<xsl:with-param name="REVISION" select="$revision" />
		</xsl:call-template>
		:
		<span>
			<xsl:value-of select="$GENERATOR/path" />
		</span>

		<xsl:if test="$GENERATOR/parameter">
			<xsl:value-of select="concat('(''',$GENERATOR/parameter, ''')')" />
		</xsl:if>
		<xsl:if test="$GENERATOR/genMethod">
			<span>
				<xsl:value-of select="concat('#',$GENERATOR/genMethod/name)" />
				<xsl:choose>
					<xsl:when test="$GENERATOR/genMethod/parameter">
						<xsl:value-of
							select="concat('(''',$GENERATOR/genMethod/parameter, ''')')" />
					</xsl:when>
					<xsl:otherwise>
						()
					</xsl:otherwise>
				</xsl:choose>
			</span>
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.generator.short">
		<xsl:param name="GENERATOR" />
		<xsl:param name="VERY_SHORT" select="false()" />

		<xsl:if test="not($VERY_SHORT)">
			<xsl:variable name="revision"
				select="/*/info/projects/project/repository/revisions/revision[@rid = $GENERATOR/revision/@rref]" />

			<xsl:call-template name="PRINTER.project.revision">
				<xsl:with-param name="REVISION" select="$revision" />
			</xsl:call-template>
			:
		</xsl:if>
		<span>
			<xsl:value-of select="tokenize($GENERATOR/path, '\.')[last()]" />
		</span>

		<xsl:if test="$GENERATOR/parameter">
			<xsl:value-of select="concat('(''',$GENERATOR/parameter, ''')')" />
		</xsl:if>
		<xsl:if test="$GENERATOR/genMethod">
			<span>
				<xsl:value-of select="concat('#',$GENERATOR/genMethod/name)" />
				<xsl:choose>
					<xsl:when test="$GENERATOR/genMethod/parameter">
						<xsl:value-of
							select="concat('(''',$GENERATOR/genMethod/parameter, ''')')" />
					</xsl:when>
					<xsl:otherwise>
						()
					</xsl:otherwise>
				</xsl:choose>
			</span>
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.method.full">
		<xsl:param name="METHOD" />

		<xsl:variable name="revision"
			select="/*/info/projects/project/repository/revisions/revision[@rid = $METHOD/revision/@rref]" />

		<xsl:call-template name="PRINTER.project.revision">
			<xsl:with-param name="REVISION" select="$revision" />
		</xsl:call-template>
		:
		<span>
			<xsl:value-of select="$METHOD/path" />
		</span>

		<xsl:if test="$METHOD/parameter">
			<xsl:value-of select="concat('(''',$METHOD/parameter, ''')')" />
		</xsl:if>
		<span>
			<xsl:value-of select="concat('#', $METHOD/name)" />
		</span>
		<xsl:if test="$METHOD/declared eq 'WITH_PARAMETERS'">
			<xsl:value-of
				select="concat('(', string-join($METHOD/parameterTypes, ','), ')')" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.method.short">
		<xsl:param name="METHOD" />
		<xsl:param name="VERY_SHORT" select="false()" />

		<xsl:if test="not($VERY_SHORT)">
			<xsl:variable name="revision"
				select="/*/info/projects/project/repository/revisions/revision[@rid = $METHOD/revision/@rref]" />

			<xsl:call-template name="PRINTER.project.revision">
				<xsl:with-param name="REVISION" select="$revision" />
			</xsl:call-template>
			:
		</xsl:if>
		<span>
			<xsl:value-of select="tokenize($METHOD/path, '\.')[last()]" />
		</span>

		<xsl:if test="$METHOD/parameter">
			<xsl:value-of select="concat('(''',$METHOD/parameter, ''')')" />
		</xsl:if>
		<span>
			<xsl:value-of select="concat('#', $METHOD/name)" />
		</span>
		<xsl:if test="$METHOD/declared eq 'WITH_PARAMETERS'">
			<xsl:value-of
				select="concat('(', string-join($METHOD/parameterTypes, ','), ')')" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="PRINTER.concat">
		<xsl:param name="COLLECTION" />
		<xsl:param name="WITH" />

		<xsl:for-each select="$COLLECTION">
			<xsl:if test="position() > 1">
				<xsl:value-of select="$WITH" />
			</xsl:if>
			<xsl:value-of select="." />
		</xsl:for-each>

	</xsl:template>



	<xsl:template name="SCRIPT.visibility">
		<xsl:param name="VISIBLE" select="true()" />
		<xsl:param name="TITLE" />
		<xsl:param name="TEXT" />
		<xsl:param name="TITLE_TAG" select="'h2'" />

		<xsl:variable name="sectionId" select="generate-id()" />

		<div class="toggleSectionTitle">
			<xsl:element name="{$TITLE_TAG}">
				<xsl:copy-of select="$TITLE" />
			</xsl:element>
			<p style="display:inline;">
				<a id="{$sectionId}_link" href="javascript:toggleVisibility('{$sectionId}')">
					<script type="text/javascript">
						document.write('Hide');
					</script>
				</a>
			</p>
		</div>
		<div id="{$sectionId}_section" style="display:block;" class="toggleSection">
			<div class="section">
				<xsl:copy-of select="$TEXT" />
			</div>
			<xsl:if test="not($VISIBLE)">
				<xsl:variable name="functionCall">
					<li call="toggleVisibility('{$sectionId}')" />
				</xsl:variable>

				<script type="text/javascript">
					<xsl:value-of select="$functionCall/li/@call" />
				</script>
			</xsl:if>
		</div>

	</xsl:template>

	<xsl:template name="IMAGE.ok">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="result_ok.png" border="0" alt="OK" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.error">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="result_error.png" border="0" alt="Error" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.failed">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="result_failed.png" border="0" alt="Failed" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.unknown">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="unknown.png" border="0" alt="Undecidable" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.warning">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="result_warning.png" border="0" alt="Warning" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.package">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="package.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.class">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="class.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.tooltip">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="tooltip.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.sum">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="sum.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.project">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="project.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.revisions">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="revisions.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.configuration.parameters">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="parameters.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.configuration.measurement">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="configure.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.configuration.evaluator">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="configure.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.show.detail">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="show-detail.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.failed.build">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="build_failed.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.failed.evaluate">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="evaluate_failed.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

	<xsl:template name="IMAGE.processed.evaluate">
		<xsl:param name="TOOLTIP" select="''" />
		<img src="evaluate_processed.png" border="0" alt="" title="{$TOOLTIP}" />
	</xsl:template>

</xsl:stylesheet>