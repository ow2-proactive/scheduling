<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:xlink="http://www.w3.org/1999/xlink" >
	<xsl:output name="svg" method="xml" indent="yes"
            doctype-system="http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd"
            doctype-public="-//W3C//DTD SVG 1.0//EN"
            standalone="no"/>
	<xsl:param name="dest" select="UNDEFINED"/>
	<xsl:param name="rectW" select="20" />
	<xsl:param name="height" select="630" />
	
	<xsl:template match="/">
		
		<!-- Global Results -->
		
		<!-- Individual Group Results -->
		<xsl:for-each select="//Group">
			
			<!-- Variables -->
			<xsl:variable name="numGroup" select="string(1+count(ancestor::Group)+count(preceding::Group))"/>
			<xsl:variable name="width" select="if ((220+(count(Results/Result/Benchmark))* ( $rectW + 10) )>800) then  220+(count(Results/Result/Benchmark))* ($rectW + 10) else 800" />
			
			<xsl:result-document href="{concat($dest,'/group',$numGroup,'.svg')}" format="svg">
				
				<!-- SVG -->
    			<svg xmlns="http://www.w3.org/2000/svg"  xmlns:xlink="http://www.w3.org/1999/xlink" width="{$width}" height="{$height}" viewbox="0 0 {$width} {$height}">
    				
    				<desc><xsl:value-of select="Name" /> : <xsl:value-of select="Description" /></desc>
    				
    				<!-- Background -->
    				<rect width="{$width}" height="{$height}" fill="#00257E" />
    				
    				<!-- Title -->
    				<text x="10" y="{10+18}" style="fill:white;stroke:none; font-weight: bold;font-family:Verdana, Helvetica, Arial, sans-serif; font-size:18pt"><xsl:value-of select="Name" /></text>
    				
    				<!-- Axes -->
    				<g style="stroke:white;stroke-width:1;font-family: Verdana, Helvetica, Arial, sans-serif; font-size:10pt;fill:white;">
    					<defs>
    						<g id="arrowMarker">
               					<g>
                    				<line x1="6" y1="-2" x2="0" y2="0"/>
                    				<line x1="6" y1="+2" x2="0" y2="0"/>
               					</g>
          					</g>
          					<marker id="startMarker" markerWidth="48" markerHeight="24" viewBox="-4 -4 25 5" orient="auto" refX="0" refY="0" markerUnits="strokeWidth">
               					<g>
                    				<use xlink:href="#arrowMarker" transform="rotate(180)" />
               					</g>
          					</marker> 
          				</defs>
          				<!-- Y -->
                    	<line x1="20" y1="{ $height - 30 }" x2="20" y2="40"  marker-end="url(#startMarker)"/>
                    	<text x="1" y="60" stroke-width="0">ms</text>
                    	<!--X -->
                    	<line x1="20" y1="{ $height - 30 }" x2="{$width - 180}" y2="{ $height - 30 }"  marker-end="url(#startMarker)"/>
                    	<text x="{$width - 240}" y="{ $height - 1 }" stroke-width="0">Benchmarks</text>
    				</g>
    				
    				<!-- Results -->
    				<xsl:variable name ="maxVal" >
						<xsl:call-template name ="max" >
							 <xsl:with-param name ="list" select ="Results/Result/Benchmark" />
 						</xsl:call-template>
 					</xsl:variable>
    				<g style="stroke:none;fill:red;font-family: Verdana, Helvetica, Arial, sans-serif; font-size:10pt;">
    					<xsl:call-template name="drawRect">
        					<xsl:with-param name="pos" select="1"/>
        					<xsl:with-param name="current" select="0" />
        					<xsl:with-param name="results" select="Results"/>
        					<xsl:with-param name="posX" select="30" />
        					<xsl:with-param name="maxTime" select="$maxVal" />		
        					<xsl:with-param name="width" select="$width" />
    					</xsl:call-template>
    				</g>
      			</svg>
      			
 			</xsl:result-document>
 			
		</xsl:for-each>

	</xsl:template>

	<!-- DrawRect -->
	<xsl:template name="drawRect">
    	<xsl:param name="pos"/>
    	<xsl:param name="current"/>
    	<xsl:param name="results"/>
    	<xsl:param name="posX" />
    	<xsl:param name="maxTime" />
    	<xsl:param name="width" />
    	<xsl:variable name="node" select="child::Results/Result[position()=$pos]"/>
    	<xsl:if test="not($pos > count($results/Result/Benchmark))">
    		<xsl:if test="not($results/Result/Benchmark)">
    			<xsl:call-template name="drawRect">
        			<xsl:with-param name="pos" select="$pos + 1"/>
        			<xsl:with-param name="current" select="$current"/>
        			<xsl:with-param name="results" select="$results" />
        			<xsl:with-param name="posX" select="$posX" />
        			<xsl:with-param name="maxTime" select="$maxTime" />
        			<xsl:with-param name="width" select="$width" />
    			</xsl:call-template>
    		</xsl:if>
    		
    		<xsl:variable name ="rectColor" >
				<xsl:call-template name ="color" >
					<xsl:with-param name ="ind" select ="$current" />
 				</xsl:call-template>
 			</xsl:variable>
 			
 			<!-- SVG -->
    		<rect x="{$posX + $rectW * $current }" y="{ $height - (30 + (($node/Benchmark * ($height - 90)) div $maxTime)) }" width="{ $rectW }" height="{ ($node/Benchmark * ($height - 90)) div $maxTime }" fill="{ $rectColor }" />
    		<text x="{$posX + $rectW * $current - 5 }" y="{ $height - (30 + (($node/Benchmark * ($height - 90)) div $maxTime)) - 5 }" fill="white" >
    			<xsl:value-of select="format-number(number($node/Benchmark), '.00')" />
    		</text>
    		<text x="{$posX + $rectW * $current + 5}" y="{ $height - 15}" fill="{ $rectColor }">
    			<xsl:value-of select="$current" />
    		</text>
    		
    		<xsl:call-template name="drawRect">
        		<xsl:with-param name="pos" select="$pos + 1"/>
        		<xsl:with-param name="current" select="$current + 1"/>
        			<xsl:with-param name="results" select="$results" />
        			<xsl:with-param name="posX" select="$posX + 10" />
        			<xsl:with-param name="maxTime" select="$maxTime" />
        			<xsl:with-param name="width" select="$width" />
    		</xsl:call-template>
    	</xsl:if>
    </xsl:template>
    
    <!-- Max -->
    <xsl:template name ="max" >
 		<xsl:param name ="list" />
		<xsl:choose>
			<xsl:when test ="$list" >
 				<xsl:variable name ="first" select ="$list[1]"  />
				<xsl:variable name ="max-of-rest">
					<xsl:call-template name ="max" >
 						<xsl:with-param name ="list" select ="$list[position()!=1]" />
 					</xsl:call-template>
 				</xsl:variable>
				<xsl:choose>
					<xsl:when test ="number( $first ) > number( $max-of-rest )" >
 						<xsl:value-of select ="$first" />
 					</xsl:when>
					<xsl:otherwise>
 						<xsl:value-of select ="$max-of-rest" />
 					</xsl:otherwise>
 				</xsl:choose>
 			</xsl:when>
 			<xsl:otherwise>0</xsl:otherwise>
 		</xsl:choose>
 	</xsl:template>
    	
    <!-- Color -->
    <xsl:template name ="color" >
    	<xsl:param name ="ind" />
    	<xsl:variable name="numColor" select="$ind mod 20" />
    	<xsl:if test="$numColor = 0">bisque</xsl:if>
    	<xsl:if test="$numColor = 1">chartreuse</xsl:if>
    	<xsl:if test="$numColor = 2">crimson</xsl:if>
    	<xsl:if test="$numColor = 3">darkcyan</xsl:if>
    	<xsl:if test="$numColor = 4">deeppink</xsl:if>
    	<xsl:if test="$numColor = 5">deepskyblue</xsl:if>
    	<xsl:if test="$numColor = 6">fuchsia</xsl:if>
    	<xsl:if test="$numColor = 7">gold</xsl:if>
    	<xsl:if test="$numColor = 8">green</xsl:if>
    	<xsl:if test="$numColor = 9">greenyellow</xsl:if>
    	<xsl:if test="$numColor = 10">khaki</xsl:if>
    	<xsl:if test="$numColor = 11">lightblue</xsl:if>
    	<xsl:if test="$numColor = 12">lightcoral</xsl:if>
    	<xsl:if test="$numColor = 13">lightgray</xsl:if>
    	<xsl:if test="$numColor = 14">maroon</xsl:if>
    	<xsl:if test="$numColor = 15">olive</xsl:if>
    	<xsl:if test="$numColor = 16">orange</xsl:if>
    	<xsl:if test="$numColor = 17">orangered</xsl:if>
    	<xsl:if test="$numColor = 18">palegreen</xsl:if>
    	<xsl:if test="$numColor = 19">palevioletred</xsl:if>
    </xsl:template>
    
</xsl:stylesheet>