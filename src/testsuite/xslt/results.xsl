<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>
	
	<xsl:template match="/">
		<html>
			<head>
  				<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1" />
  				<title>Results</title>
  			</head>
			<body>
				<h1 style="text-align: center;">Messages</h1>
				
					<xsl:for-each select="//Result">
						<xsl:call-template name="Message" />
					</xsl:for-each>
				
			</body>
		</html>
		
	</xsl:template>
	
<!-- Message -->
	
	<xsl:template name="Message">
		<p><xsl:call-template name="DateFormat_SHORT">
				<xsl:with-param name="date" select="Date" />
			</xsl:call-template>
			<xsl:if test="@type = -3"><i> [INFORMATION] </i></xsl:if>
			<xsl:if test="@type = -2"><i> [MESSAGE] </i></xsl:if>
			<xsl:if test="@type = -1"><i> [RESULT] </i></xsl:if>
			<xsl:if test="@type = 0"><b><span style="color: rgb(0, 0, 255);"> [MESSAGE] </span></b></xsl:if>
			<xsl:if test="@type = 1"><b><span style="color: rgb(0, 0, 255);"> [RESULT] </span></b></xsl:if>
			<xsl:if test="@type = 2"><b><span style="color: rgb(255, 0, 0);"> [ERROR] </span></b></xsl:if>
			<xsl:value-of select="TestName" /> : <xsl:value-of select="Message" />
			<xsl:if test="@failed">
				<xsl:if test="@failed = 'true'"><b><span style="color: rgb(255, 0, 0);"> [FAILED] </span></b></xsl:if>
				<xsl:if test="@failed = 'false'"><b><span style="color: rgb(0, 255, 0);"> [SUCCESS] </span></b></xsl:if>
			</xsl:if>
			<xsl:if test="Benchmark/@time">
				--> <i>Runs in </i><b><xsl:value-of select ="Benchmark/@time" /></b><i>ms</i>
			</xsl:if>
			<xsl:if test="Exception">
				<p class="error">Stack Trace :<br />
    				<xsl:call-template name="Replace">
        				<xsl:with-param name="string" select="Exception"/>
    				</xsl:call-template>
				</p>
			</xsl:if>
		</p>
	</xsl:template>	
	
<!-- Date Fromater -->
	
	<xsl:template name="DateFormat_FULL">
			<xsl:param name="date" select="UNDEFINED" />
			<xsl:if test="$date/@month = 0">January</xsl:if>
			<xsl:if test="$date/@month = 1">February</xsl:if>
			<xsl:if test="$date/@month = 2">March</xsl:if>
			<xsl:if test="$date/@month = 3">April</xsl:if>
			<xsl:if test="$date/@month = 4">May</xsl:if>
			<xsl:if test="$date/@month = 5">June</xsl:if>
			<xsl:if test="$date/@month = 6">July</xsl:if>
			<xsl:if test="$date/@month = 7">August</xsl:if>
			<xsl:if test="$date/@month = 8">September</xsl:if>
			<xsl:if test="$date/@month = 9">October</xsl:if>
			<xsl:if test="$date/@month = 10">November</xsl:if>
			<xsl:if test="$date/@month = 11">December</xsl:if>
			<xsl:value-of select="concat(' ',$date/@day)" />, 
			<xsl:value-of select="$date/@year" /> at <xsl:value-of select="$date/Time/@hour" />:<xsl:value-of select="$date/Time/@minute" />:<xsl:value-of select="$date/Time/@second" />.<xsl:value-of select="$date/Time/@millisecond" />
		</xsl:template>
		
			<xsl:template name="DateFormat_SHORT">
			<xsl:param name="date" select="UNDEFINED" />
			<xsl:value-of select="$date/@month+1"/>/<xsl:value-of select="$date/@day" />/<xsl:value-of select="$date/@year" /> at <xsl:value-of select="$date/Time/@hour" />:<xsl:value-of select="$date/Time/@minute" />:<xsl:value-of select="$date/Time/@second" />.<xsl:value-of select="$date/Time/@millisecond" />
		</xsl:template>

<!-- Replace \n with </br> -->

	<xsl:template name="Replace">
    	<xsl:param name="string"/>
    	<xsl:choose>
        	<xsl:when test="contains($string,'&#10;')">
        		<xsl:value-of select="substring-before($string,'&#10;')"/>
            	<br/>
          		<xsl:call-template name="Replace">
                	<xsl:with-param name="string" select="substring-after($string,'&#10;')"/>
            	</xsl:call-template>
        	</xsl:when>
        	<xsl:otherwise>
            	<xsl:value-of select="$string"/>
        	</xsl:otherwise>
    	</xsl:choose>
	</xsl:template>
</xsl:stylesheet>