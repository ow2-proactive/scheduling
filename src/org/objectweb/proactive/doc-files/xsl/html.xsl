<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:date="http://exslt.org/dates-and-times"  
                exclude-result-prefixes="date"  
                version="1.0">

<xsl:import href="common.xsl"/>

<!-- Configure the html stylesheet to use -->
<xsl:param name="html.stylesheet" select="'ProActive.css'"/>

<!-- Add copyright information to all the page footers. -->
<xsl:template name="user.footer.content">
  <P class="copyright" align="right">
      © 2001-2006 
     <A href="http://www-sop.inria.fr/">INRIA Sophia Antipolis</A> All Rights Reserved
   </P>
</xsl:template>


<!-- Just use the image size for the html output. Width=... has no effect. -->
<xsl:param name="ignore.image.scaling">1</xsl:param> 


<!--  Adding the generation date in the headers of the files -->
<!-- TODO : put this back - please beware, java based xml proc does not know of time stamps
<xsl:template name="user.head.content">  
  <meta name="date">  
    <xsl:attribute name="content">  
      <xsl:call-template name="datetime.format">  
        <xsl:with-param name="date" select="date:date-time()"/>  
        <xsl:with-param name="format" select="'Y-m-d'"/>  
      </xsl:call-template>
    </xsl:attribute>
  </meta>
</xsl:template>-->

<!-- Making the parts and chapters stick out in the toc  -->
<!-- Hum, I don't know where the tocs are generated... -->

<!-- Redefining the corporate authors, by adding a picture just after the string. 
This should not be done this way. The media object should have been in the corpauthor block. -->
<xsl:template match="corpauthor" mode="book.titlepage.recto.mode">
  <xsl:apply-templates mode="titlepage.mode"/>
  <table border="0" cellpadding="0" cellspacing="2">
    <tbody>
      <tr>
        <td>
          <img> 
            <xsl:attribute name="src">  <xsl:copy-of select="$threeinstitutes.image.filename"/> </xsl:attribute>
            <xsl:attribute name="alt">A CNRS-INRIA-UNSA Research team</xsl:attribute>
            <xsl:attribute name="title">A CNRS-INRIA-UNSA Research team</xsl:attribute>
          </img>
        </td>
        <td>
          <a href="http://www.objectweb.org">
           <img> 
             <xsl:attribute name="src"> <xsl:copy-of select="$objectweb.image.filename"/> </xsl:attribute>
             <xsl:attribute name="alt">ObjectWeb</xsl:attribute>
             <xsl:attribute name="title">ObjectWeb</xsl:attribute>
           </img>
          </a>
        </td>
      </tr>
    </tbody>
  </table>
</xsl:template>

<!-- Specifying how the titlepage should look like -->
<xsl:template name="book.titlepage.recto">
  <!-- The Main Title -->
    <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/mediaobject"/>
  <!-- The Subtitle -->
    <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/subtitle"/>
  <!-- The authors -->
    <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/author"/>
  <!-- TODO: The Logos -->
    <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/corpauthor"/>
  <!-- The Revision and copyright in a table -->
    <table style="width: 100%; text-align: left;" border="0" cellpadding="0" cellspacing="2">
      <tbody>
        <tr>
          <td>Version   <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/revision/revnumber"/> </td>
          <td> <xsl:apply-templates mode="book.titlepage.recto.mode"  select="bookinfo/revision/date"/> </td>
          <td> <xsl:apply-templates mode="book.titlepage.recto.mode"  select="bookinfo/copyright"/> </td>
        </tr>
      </tbody>
    </table>
  <!-- A line to do draw a separation -->
    <hr/>
  <!-- The abstract -->
    <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/abstract"/>
</xsl:template>

</xsl:stylesheet>
