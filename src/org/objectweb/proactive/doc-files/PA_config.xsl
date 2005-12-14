<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">
<!-- Use ids for filenames -->
<xsl:param name="use.id.as.filename" select="'1'"/>

<!-- Turn on admonition graphics. -->
<xsl:param name="admon.graphics" select="'1'"/>
<xsl:param name="admon.graphics.path"></xsl:param>

<!-- Configure the stylesheet to use -->
<xsl:param name="html.stylesheet" select="'doc-files/ProActive.css'"/>


<xsl:param name="callout.graphics" select="'1'"></xsl:param>
<xsl:param name="callout.graphics.path"></xsl:param>
<xsl:param name="callout.list.table" select="'1'"></xsl:param>
<xsl:param name="generate.section.toc.level" select="0"></xsl:param>
<xsl:param name="generate.chapter.toc.level" select="0"></xsl:param>

<!-- Put tables of contents and list of titles on separate file -->
<xsl:param name="chunk.section.depth" select="0"></xsl:param>
<xsl:param name="chunk.tocs" select="1"></xsl:param>
<xsl:param name="chunk.lots" select="1"></xsl:param>
 
<xsl:param name="section.autolabel" select="1"></xsl:param>
<xsl:param name="section.autolabel.max.depth" select="4"></xsl:param>
<xsl:param name="section.label.includes.component.label" select="1"></xsl:param>

<xsl:param name="generate.index" select="0"></xsl:param>

<xsl:param name="generate.toc">
<!--
appendix  toc,title
article/appendix  nop
article   toc,title
book      toc,title,figure,table,example,equation
chapter   toc,title
part      toc,title
preface   toc,title
qandadiv  toc
qandaset  toc
reference toc,title
sect1     toc
sect2     toc
sect3     toc
sect4     toc
sect5     toc
section   toc
set       toc,title
-->
appendix  toc,title
book      toc,title,figure,table,example,equation
chapter   title
part      toc,title
qandadiv  toc
qandaset  toc
</xsl:param>


<!--
<xsl:template name="user.header.navigation" xmlns="http://www.w3.org/1999/xhtml">
<center>
<SCRIPT type="text/javascript">
google_ad_client = "pub-9976612598143264";
google_ad_width = 728;
google_ad_height = 90;
google_ad_format = "728x90_as";
google_ad_channel ="";
google_ad_type = "text_image";
</SCRIPT>
<SCRIPT type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</SCRIPT>
</center>
</xsl:template>
-->
<xsl:template name="user.footer.content">
  <P class="copyright" align="right">
   <!--&#x00A9;-->  © 2001-2005 
     <a href="http://www-sop.inria.fr/">INRIA Sophia Antipolis</a>
     All Rights Reserved
   </P> 
</xsl:template>

</xsl:stylesheet>
