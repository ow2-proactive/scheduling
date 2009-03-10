<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:date="http://exslt.org/dates-and-times"
                exclude-result-prefixes="date" version="1.0">

	<xsl:import href="../docbook-xsl/profiling/profile.xsl" />

	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- These templates change fileref attributes of textdata tags from relative paths to absolute paths-->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<xsl:param name="tmp.dir"/>

	<xsl:template match="/">
			 <xsl:apply-templates/>
	 </xsl:template>

	<xsl:template match="*">
		<xsl:copy>
			 <xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	 </xsl:template>

	<xsl:template match="@*">
		<xsl:variable name="filename">
			<xsl:value-of select="."/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="name()='fileref' and name(..)='textdata'">
						<xsl:attribute name="fileref">
							<xsl:value-of select="concat('file://',$tmp.dir,$filename)"/>
						</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="text() | comment() | processing-instruction()">
		<xsl:copy/>
	</xsl:template>

</xsl:stylesheet>

