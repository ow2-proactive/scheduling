<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

<!-- Use chapter ids for html filenames -->
<xsl:param name="use.id.as.filename" select="'1'"/>

<!-- Make table use up all space. -->
<!-- <xsl:param name="default.table.width" select="100"></xsl:param> -->

<!-- Make graphics in pdf be smaller than page width, if needed-->
<!--  ??? How do I do that, I need to compare a measure of imagewidth with pagewidth! -->
<!-- default.image.width exists, should be set maybe only for pdf and big images?? -->

<!--  Watch OUT, THIS IS EXPERIMENTAL -->

<!-- Specifies that if an empty toc element is found in a source document, an automated TOC is generated. Note: Depending the value of the generate.toc parameter, setting this parameter to 1 could result in generation of duplicate automated TOCs. So the process.empty.source.toc parameter is primarily useful as an "override." By placing an empty toc in your document and setting this parameter to 1, you can force a TOC to be generated even if generate.toc says not to. -->
<xsl:param name="process.empty.source.toc" select="1"></xsl:param>
<!-- Specifies that the contents of a non-empty "hard-coded" toc element in a source document are processed to generate a TOC in output. Note: This parameter has no effect on automated generation of TOCs. An automated TOC may still be generated along with the "hard-coded" TOC. To suppress automated TOC generation, adjust the value of the generate.toc paramameter. The process.source.toc parameter also has no effect if the toc element is empty; handling for an empty toc is controlled by the process.empty.source.toc parameter. -->
<xsl:param name="process.source.toc" select="0"></xsl:param>

<!--  END OF EXPERIMENT -->

<!--  Make sure figures have a white background, in case of transparent pixels -->
<!--  Also removed figures borders, which are a nuisance -->
<!--<xsl:attribute-set name="figure.properties" use-attribute-sets="formal.object.properties">
  <xsl:attribute name="border-color">#000000</xsl:attribute>
  <xsl:attribute name="border-style">solid</xsl:attribute>
  <xsl:attribute name="border-width">0px</xsl:attribute>
  <xsl:attribute name="padding">1em</xsl:attribute>
  <xsl:attribute name="background-color">#FFFFFF</xsl:attribute>
</xsl:attribute-set>-->

<!-- Turn on admonition graphics. -->
<xsl:param name="admon.graphics" select="'1'"/>
<xsl:param name="admon.graphics.path"></xsl:param>

<!-- Configure the html stylesheet to use -->
<xsl:param name="html.stylesheet" select="'ProActive.css'"/>


<xsl:param name="callout.graphics" select="'1'"></xsl:param>
<xsl:param name="callout.graphics.path"></xsl:param>
<xsl:param name="callout.list.table" select="'1'"></xsl:param>

<!-- Put tables of contents and list of titles on separate file -->
<xsl:param name="chunk.section.depth" select="0"></xsl:param>
<xsl:param name="chunk.tocs" select="1"></xsl:param>
<xsl:param name="chunk.lots" select="1"></xsl:param>
 
<!-- force all sections to have a number assigned, like in 4.2.1.3 -->
<xsl:param name="section.autolabel" select="1"></xsl:param>
<!-- stop labelling at the fourth nesting level -->
<xsl:param name="section.autolabel.max.depth" select="4"></xsl:param>
<xsl:param name="section.label.includes.component.label" select="1"></xsl:param>

<xsl:param name="generate.index" select="0"></xsl:param>

<!-- Which components of the document should have a table of content ? -->
<!--<xsl:param name="generate.toc">
    appendix  toc,title
    book      toc,title,figure,table,example,equation
    chapter   title
    part      toc,title
    qandadiv  toc
    qandaset  toc
</xsl:param>-->

<xsl:param name="generate.toc">
    appendix  toc,title
    book      toc,title,figure,table,example,equation
    article   nop
    chapter   title
    part      toc,title
    qandadiv  toc
    qandaset  toc
</xsl:param>

<!-- Add copyright information to all the page footers -->
<xsl:template name="user.footer.content">
  <P class="copyright" align="right">
      © 2001-2006 
     <A href="http://www-sop.inria.fr/">INRIA Sophia Antipolis</A> All Rights Reserved
   </P>
</xsl:template>

</xsl:stylesheet>
