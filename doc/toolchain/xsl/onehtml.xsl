<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:date="http://exslt.org/dates-and-times"
 exclude-result-prefixes="date" version="1.0">


 <!-- Say we need the "one single html file" style -->
 <xsl:import
  href="http://docbook.sourceforge.net/release/xsl-ns/1.73.2/html/docbook.xsl" />
 <xsl:import href="html.xsl" />

 <!--  Changing font sizes -->
 <xsl:param name="body.font.family">Times New Roman</xsl:param>
 <!-- <xsl:param name="body.font.master">11</xsl:param> -->
 <!--<xsl:param name="title.font.family">Times New Roman</xsl:param> 
  <xsl:param name="footnote.font.size">9</xsl:param>-->
 <xsl:param name="monospace.font.family">Helvetica</xsl:param>
 <!-- <xsl:param name="monospace.font.size">5</xsl:param> -->


 <!-- Add copyright information to all the page footers. -->
 <xsl:template name="user.footer.content">
  <P class="copyright" align="right">
   © 1997-2009
   <A href="http://www-sop.inria.fr/">
    INRIA Sophia Antipolis
   </A>
   All Rights Reserved
  </P>
 </xsl:template>



 <!-- Just use the image size for the html output. Width=... has no effect. -->
 <xsl:param name="ignore.image.scaling">1</xsl:param>

 <xsl:template match="legalnotice" mode="titlepage.mode">
  <xsl:variable name="id">
   <xsl:call-template name="object.id" />
  </xsl:variable>
  <xsl:choose>
   <xsl:when test="$generate.legalnotice.link != 0">
    <xsl:variable name="filename">
     <xsl:call-template name="make-relative-filename">
      <xsl:with-param name="base.dir"
       select="$base.dir" />
      <xsl:with-param name="base.name">
       <xsl:apply-templates mode="chunk-filename"
        select="." />
      </xsl:with-param>
     </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="title">
     <xsl:apply-templates select="." mode="title.markup" />
    </xsl:variable>

    <xsl:variable name="href">
     <xsl:apply-templates mode="chunk-filename"
      select="." />
    </xsl:variable>

    <a href="{$href}">
     <xsl:copy-of select="$title" />
    </a>

    <xsl:call-template name="write.chunk">
     <xsl:with-param name="filename" select="$filename" />
     <xsl:with-param name="quiet"
      select="$chunk.quietly" />
     <xsl:with-param name="content">
      <xsl:call-template name="user.preroot" />
      <html>
       <head>
        <xsl:call-template
         name="system.head.content" />
        <xsl:call-template name="head.content" />
        <xsl:call-template
         name="user.head.content" />
       </head>
       <body>
        <xsl:call-template
         name="body.attributes" />
        <div class="{local-name(.)}">
         <xsl:apply-templates
          mode="titlepage.mode" />
        </div>
       </body>
      </html>
     </xsl:with-param>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
    <div class="{local-name(.)}">
     <a name="{$id}" />
     <xsl:apply-templates mode="titlepage.mode" />
    </div>
   </xsl:otherwise>
  </xsl:choose>
  <hr />
 </xsl:template>


 <!-- Making the parts and chapters stick out in the toc  -->
 <xsl:param name="header.rule">1</xsl:param>

 <!--  Which levels should be having a toc? For one htmmml file, only one toc, at the beginning. -->
 <xsl:param name="generate.toc">
  appendix nop
  book toc,title,figure,table,example,equation
  article nop 
  part title
  chapter title
  qandaset toc
 </xsl:param>


</xsl:stylesheet>


