<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>
	
	<xsl:template match="/">
		<html>
			<head>
  				<link  rel="stylesheet" href="stylesheet.css" type="text/css" />
  				<title><xsl:value-of select="Manager/Name" /></title>
  			</head>
			<body>
				<h1 style="text-align: center;">Results of BENCHMARKS</h1>
				
				<p><b>Manager name : </b><xsl:value-of select="Manager/Name" /></p>
				<p><b>Manager description : </b><xsl:value-of select="Manager/Description" /></p>
				<p><b>Date of running : </b><xsl:call-template name="DateFormat_FULL">
												<xsl:with-param name="date" select="Manager/Date" />
											</xsl:call-template>		
				</p>
				<p>Each tests runs : <b><xsl:value-of select="Manager/NbRuns" /> times</b></p>
				
				<!-- Quick Navigation -->

				<h2><a name="nav"></a>Quick navigation</h2>
					<ul>
						<li><a href="#summary">Summary</a></li>
						<li><a href="#globals">Globals Results</a></li>
						<li><a href="#bench">Benchmarks Descriptions</a></li>
					</ul>
					
				<!-- Globals Results -->
				
				<h2><a name="summary"></a>Summary</h2>
				<xsl:for-each select="//Group">
					<h3><a name="group{1+count(ancestor::Group)+count(preceding::Group)}"></a><xsl:value-of select="Name"/></h3>
					<p><xsl:value-of select="Description" /></p>
					<p>To see all results of this group in a <a href="bench_results_files/Group{1+count(ancestor::Group)+count(preceding::Group)}.png">BarChart</a>.</p>
				</xsl:for-each>
				<p><address><a href="#nav">Return to Quick navigation</a></address></p>
				
				<h2><a name="globals"></a>Globals Results</h2>
				<xsl:for-each select="//AllMessages//Result">
					<xsl:if test="@type > -1 ">
						<xsl:call-template name="MessageGlobals" />
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="Results/Result">
					<p><xsl:value-of select="TestName" /> : <xsl:value-of select="TestDescription" /></p>
				</xsl:for-each>
				<p><address><a href="#nav">Return to Quick navigation</a></address></p>
				
				<h2><a name="bench"></a>Benchmarks Descriptions</h2>
				<xsl:for-each select="//Group">
					<xsl:for-each select="Results/Result">
						<xsl:if test="@failed">
							<p><xsl:value-of select="TestName" /> : <xsl:value-of select="TestDescription" /></p>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
				<p><address><a href="#nav">Return to Quick navigation</a></address></p>
				
				<p class="textSmall">All charts are created with <a href="http://www.jfree.org/jfreechart/index.html">JFreeChart</a>.</p>
			</body>
		</html>
		
	</xsl:template>
	
	<!-- MessageGlobals -->
	
	<xsl:template name="MessageGlobals">
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
			<xsl:if test="Group">
				<h3><xsl:value-of select="Name"/></h3>
				<p><xsl:value-of select="Description"/></p>
			</xsl:if>
			<xsl:if test="Benchmark">
			    	<xsl:variable name="i" ><xsl:number level="any"  from="AllMessages" count="Benchmark" /></xsl:variable>
					<a href="bench_results_files/Bench{$i}.png">See the chart</a> Max=<xsl:value-of select ="Benchmark/MaxTime" /><xsl:value-of select="Benchmark/Unit" /> Moy=<xsl:value-of select ="Benchmark/MoyTime" /><xsl:value-of select="Benchmark/Unit" /> STDEV=<xsl:value-of select ="format-number(number(Benchmark/STDEV),'.00')" /><xsl:value-of select="Benchmark/Unit" /> --> <i>Min </i><b><xsl:value-of select ="Benchmark/BestTime" /></b><i><xsl:value-of select="Benchmark/Unit" /></i>
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
