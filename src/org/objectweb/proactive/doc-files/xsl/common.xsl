<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">


 <!-- Where should the titles of formal objects be placed? -->
 <xsl:param name="formal.title.placement">
  figure after 
  example after 
  equation after 
  table after 
  procedure after
 </xsl:param>



 <!-- Specifies that if an empty toc element is found in a source document, an automated TOC is generated. Note: Depending the value of the generate.toc parameter, setting this parameter to 1 could result in generation of duplicate automated TOCs. So the process.empty.source.toc parameter is primarily useful as an "override." By placing an empty toc in your document and setting this parameter to 1, you can force a TOC to be generated even if generate.toc says not to. -->
 <xsl:param name="process.empty.source.toc">1</xsl:param>
 <!-- Specifies that the contents of a non-empty "hard-coded" toc element in a source document are processed to generate a TOC in output. Note: This parameter has no effect on automated generation of TOCs. An automated TOC may still be generated along with the "hard-coded" TOC. To suppress automated TOC generation, adjust the value of the generate.toc paramameter. The process.source.toc parameter also has no effect if the toc element is empty; handling for an empty toc is controlled by the process.empty.source.toc parameter. -->
 <xsl:param name="process.source.toc">0</xsl:param>


 <!-- Turn on admonition graphics. -->
 <xsl:param name="admon.graphics" select="'1'" />
 <!-- <xsl:param name="admon.graphics.path"></xsl:param> -->
 <!--  TODO : make nice graphics for the next/prev buttons-->


 <!--  TODO : are we going to use these callout graphics ? We're not using images here!-->
 <xsl:param name="callout.graphics">1</xsl:param>
 <xsl:param name="callout.graphics.path"></xsl:param>
 <xsl:param name="callout.list.table">1</xsl:param>

 <!-- force all sections to have a number assigned, like "1. First section"-->
 <xsl:param name="section.autolabel">1</xsl:param>
 <!-- stop labelling at the fourth nesting level -->
 <xsl:param name="section.autolabel.max.depth">4</xsl:param>
 <!-- sections bear the names of their inherited sections, like in "4.2.1.3. A subsubsubsection" -->
 <xsl:param name="section.label.includes.component.label">1</xsl:param>
 <!--  Force chapter 2 of part 3 to be labelled as Chap III.2 -->
 <!-- <xsl:param name="component.label.includes.part.label">1</xsl:param> -->


<!--  <xsl:param name="generate.index">0</xsl:param> -->

 <!--  Which levels should be having a toc? I say : book, parts, appendixes and q&a only -->
 <xsl:param name="generate.toc">
  appendix nop
  book toc,title,figure,table,example,equation
  article nop 
  part toc,title 
  chapter nop
  appendix toc
  qandaset toc
 </xsl:param>

 <!-- The header image -->
 <xsl:param name="header.image.filename">images/ProActiveLogoSmall.png</xsl:param>
 <!-- the 3 institutes images -->
 <xsl:param name="threeinstitutes.image.filename">images/logo-cnrs-inria-unsa.png</xsl:param>
 <!--  The objectweb logo -->
 <xsl:param name="objectweb.image.filename">images/logo-ObjectWeb.png</xsl:param>

<!-- Strange: affiliation,phone, is a docbook elements, which has no corresponding template! -->
<xsl:template match="affiliation">
       <xsl:apply-templates/>
</xsl:template>

<!--  Redefining ==> get the phone|fax string in the output.-->
<xsl:template match="phone|fax">
  <xsl:value-of select="name(.)"/>
  <xsl:text>: </xsl:text>
  <xsl:call-template name="inline.charseq"/>
</xsl:template>

</xsl:stylesheet>
