<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

<!-- Use chapter ids for html filenames -->
<xsl:param name="use.id.as.filename" select="'1'"/>

<!-- Make table use up all space. -->
<!-- <xsl:param name="default.table.width" select="100"></xsl:param> -->

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
<xsl:param name="generate.toc">
    appendix  toc,title
    book      toc,title,figure,table,example,equation
    chapter   title
    part      toc,title
    qandadiv  toc
    qandaset  toc
</xsl:param>

<!-- Add copyright information to all the page footers -->
<xsl:template name="user.footer.content">
  <P class="copyright" align="right">
   <!--&#x00A9;-->  © 2001-2005 
     <a href="http://www-sop.inria.fr/">INRIA Sophia Antipolis</a> All Rights Reserved
   </P>
</xsl:template>

</xsl:stylesheet>
