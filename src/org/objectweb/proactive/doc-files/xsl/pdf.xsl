<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

<xsl:import href="common.xsl"/>
<xsl:import href="pdf.titlepage.xsl"/>


<!-- This is good for pdf generation : the blank lines around titles are squeezed -->
<!--<xsl:param name="line-height">1.2</xsl:param>-->  <!--normal value is 1.2-->


<!--  Paper feed -->
<xsl:param name="paper.type">A4</xsl:param>
<xsl:param name="page.margin.inner">10mm</xsl:param>
<xsl:param name="page.margin.outer">13mm</xsl:param>
<xsl:param name="double.sided">1</xsl:param>

<!-- Make table use up all space. -->
<!-- <xsl:param name="default.table.width" >100</xsl:param> -->

<!-- Make graphics in pdf be smaller than page width, if needed-->
<!--  ??? How do I do that, I need to compare a measure of imagewidth with pagewidth! -->
<!-- default.image.width exists, should be set maybe only for pdf and big images?? -->

<!--  Make sure figures have a white background, in case of transparent pixels -->
<!--  Also removed figures borders, which are a nuisance -->
<!--<xsl:attribute-set name="figure.properties" use-attribute-sets="formal.object.properties">
  <xsl:attribute name="border-color">#000000</xsl:attribute>
  <xsl:attribute name="border-style">solid</xsl:attribute>
  <xsl:attribute name="border-width">0px</xsl:attribute>
  <xsl:attribute name="padding">1em</xsl:attribute>
  <xsl:attribute name="background-color">#FFFFFF</xsl:attribute>
</xsl:attribute-set>-->

<!--  Changing the appearance of ALL the section titles -->
<xsl:attribute-set name="section.title.properties">
  <xsl:attribute name="font-weight">bold</xsl:attribute> 
  <xsl:attribute name="color">blue</xsl:attribute>
  <xsl:attribute name="font-size">11pt</xsl:attribute> <!--All titles not reconfigured are 11pt-->
</xsl:attribute-set>  

<!-- Extra configure for sect1 and sect2 -->
<xsl:attribute-set name="section.title.level1.properties">
  <xsl:attribute name="font-size">14pt</xsl:attribute>
<!--  Adding a grey box under all section1 titles -->
  <xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
</xsl:attribute-set>  

<xsl:attribute-set name="section.title.level2.properties">
  <xsl:attribute name="font-size">12pt</xsl:attribute>
</xsl:attribute-set>  

<!-- adjust the headers, recalling chapter numbers -->
<!-- TODO: align left or right, depending in the page. -->
<xsl:template name="header.table">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <!-- Really output a header? -->
  <xsl:choose>
    <xsl:when test="$pageclass = 'titlepage' and $gentext-key = 'book' and $sequence='first'">
      <!-- no, book titlepages have no headers at all -->
    </xsl:when>
    <xsl:when test="$sequence = 'blank' and $headers.on.blank.pages = 0">
      <!-- no output on blank pages -->
    </xsl:when>
    <xsl:when test="$gentext-key = 'part'">
      <!-- parts are big enough on their page, let's not repeat the same text twice! -->
    </xsl:when>
    <xsl:otherwise>
      <!-- Insert the current chapter title -->
      <xsl:apply-templates select="." mode="object.title.markup"/>
      <!-- Insert the PA logo  To do that, you need a table, with text left, and image right-->
<!--      <fo:external-graphic content-height="1.2cm">
      <xsl:attribute name="src">
        <xsl:call-template name="fo-external-image">
          <xsl:with-param name="filename" select="$header.image.filename"/>
        </xsl:call-template>
      </xsl:attribute>
      </fo:external-graphic>-->
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Footer : only have the page number -->
<xsl:template name="footer.table">
  <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format" text-align="center">
     <fo:page-number/>
  </fo:block> 
</xsl:template>

<!-- Having long lines be broken up  -->
<xsl:param name="hyphenate.verbatim">yes</xsl:param>

<!--<xsl:attribute-set name="monospace.verbatim.properties">
    <xsl:attribute name="wrap-option">wrap</xsl:attribute>-->
<!-- CXXXXX    <xsl:attribute name="hyphenation-character">&#x00B6;</xsl:attribute> -->
<!-- </xsl:attribute-set> -->

<!-- Playing around with the bullet possibilities -->
<!--<xsl:template name="itemizedlist.label.markup">
  <xsl:param name="itemsymbol" select="'disc'"/> -  
</xsl:template>-->

<!-- Trying to improve itemized lists rendering -->
<!-- FIND AND MODIFY <xsl:template match="itemizedlist"> -->

<!-- JUST TO HAVE THE THREE LOGOS ON TITLE PAGE -->
<xsl:template match="corpauthor" mode="book.titlepage.recto.mode">
  <fo:inline color="blue">
    <xsl:apply-templates mode="titlepage.mode"/>
  </fo:inline>
  <fo:external-graphic>
       <xsl:attribute name="background-color" select="'#OOOOOO'" />      
       <xsl:attribute name="src" width="2.5in">  
            <xsl:copy-of select="$threeinstitutes.image.filename"/> 
      </xsl:attribute>    
  </fo:external-graphic>
</xsl:template>

</xsl:stylesheet>
