<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:date="http://exslt.org/dates-and-times"  
                exclude-result-prefixes="date"  
                version="1.0">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/1.69.1/fo/docbook.xsl"/>
<xsl:import href="common.xsl"/>

<!--  Changing font sizes -->
<!-- <xsl:param name="body.font.family">Times New Roman</xsl:param>  -->
<!-- <xsl:param name="body.font.master">11</xsl:param> -->
<!--<xsl:param name="title.font.family">Times New Roman</xsl:param> 
<xsl:param name="footnote.font.size">9</xsl:param>-->
<xsl:param name="monospace.font.family">Helvetica</xsl:param> 
<!-- <xsl:param name="monospace.font.size">5</xsl:param> -->

<!-- This avoids having "Draft" mode set on. Avoids the other two lines -->
<xsl:param name="fop.extensions" select="'1'"/>
<!-- <xsl:param name="draft.mode">no</xsl:param>  -->
<!-- <xsl:param name="draft.watermark.image"></xsl:param>  -->


<!-- This is good for pdf generation : the blank lines around titles are squeezed -->
<!--<xsl:param name="line-height">1.2</xsl:param>-->  <!--normal value is 1.2-->


<!-- Remove headers on blank pages please! -->
<xsl:param name="headers.on.blank.pages">0</xsl:param>


<!--  Paper feed -->
<xsl:param name="paper.type">A4</xsl:param>
<xsl:param name="page.margin.inner">10mm</xsl:param>
<xsl:param name="page.margin.outer">13mm</xsl:param>
<xsl:param name="double.sided">1</xsl:param>

<!-- Make tables use up all space. -->
<!-- <xsl:param name="default.table.width" >100</xsl:param> -->

<!-- The chapter entries of the toc are in bold, the parts in bold and 11pt. -->
<xsl:template name="toc.line">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="label">
    <xsl:apply-templates select="." mode="label.markup"/>
  </xsl:variable>

  <fo:block text-align-last="justify"
            end-indent="{$toc.indent.width}pt"
            last-line-end-indent="-{$toc.indent.width}pt">
    <fo:inline keep-with-next.within-line="always">
      <xsl:choose>
        <xsl:when test="self::chapter">
          <xsl:attribute name="font-weight">bold</xsl:attribute>
        </xsl:when>
        <xsl:when test="self::part">
          <xsl:attribute name="background-color">#FFFF00</xsl:attribute>
          <xsl:attribute name="font-weight">bold</xsl:attribute>
          <xsl:attribute name="font-size">11pt</xsl:attribute>
          <xsl:attribute name="color">#00257E</xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <fo:basic-link internal-destination="{$id}">
        <xsl:if test="$label != ''">
          <xsl:copy-of select="$label"/>
          <xsl:value-of select="$autotoc.label.separator"/>
        </xsl:if>
        <xsl:apply-templates select="." mode="title.markup"/>
      </fo:basic-link>
    </fo:inline>
    <fo:inline keep-together.within-line="always">
      <xsl:text> </xsl:text>
      <fo:leader leader-pattern="dots"
                 leader-pattern-width="3pt"
                 leader-alignment="reference-area"
                 keep-with-next.within-line="always"/>
      <xsl:text> </xsl:text>
      <fo:basic-link internal-destination="{$id}">
        <fo:page-number-citation ref-id="{$id}"/>
      </fo:basic-link>
    </fo:inline>
  </fo:block>
</xsl:template>

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

<!-- helper for function below -->
<xsl:template name="link.myhelper">
  <xsl:param name="class" select="''"/>
  <xsl:param name="level" select="''"/>
 
   <fo:block space-before="5pt">
  <fo:basic-link internal-destination="{$level/@id}">
      <xsl:value-of select="$class"/> 
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="$level" mode="label.markup"/> 
      <xsl:text>: </xsl:text>
      <xsl:apply-templates select="$level" mode="title.markup"/> 
  </fo:basic-link>
   </fo:block>
  
</xsl:template>

<!-- adjust the headers, recalling chapter and part numbers -->
<xsl:template name="header.table">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <xsl:variable name="leftS">
    <xsl:choose>

      <xsl:when test="$sequence = 'even'">
       <xsl:call-template name="link.myhelper">
           <xsl:with-param name="level" select="." />
       </xsl:call-template>
      </xsl:when>

      <xsl:otherwise>
        <xsl:call-template name="link.myhelper">
          <xsl:with-param name="level" select=".." />
          <xsl:with-param name="class">
            <xsl:call-template name="gentext">
              <xsl:with-param name="key" select="'Part'"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>

      </xsl:choose>
  </xsl:variable>

  <xsl:variable name="rightS">
     <xsl:choose>

      <xsl:when test="$sequence = 'odd'">
         <xsl:call-template name="link.myhelper">
            <xsl:with-param name="level" select="." />
         </xsl:call-template>
      </xsl:when>
      
      <xsl:when test="$sequence = 'even'">
        <xsl:call-template name="link.myhelper">
          <xsl:with-param name="level" select=".." />
          <xsl:with-param name="class">
             <xsl:call-template name="gentext">
                <xsl:with-param name="key" select="'Part'"/>
             </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>

     </xsl:choose>
  </xsl:variable>

  <xsl:variable name="candidate">
    <fo:table table-layout="fixed" width="100%">
      <xsl:call-template name="head.sep.rule">
        <xsl:with-param name="pageclass" select="$pageclass"/>
        <xsl:with-param name="sequence" select="$sequence"/>
        <xsl:with-param name="gentext-key" select="$gentext-key"/>
      </xsl:call-template>

      <fo:table-column column-number="1"/>
      <fo:table-column column-number="2"/>
      <fo:table-column column-number="3"/>

      <fo:table-body>
        <fo:table-row height="15pt">

          <fo:table-cell text-align="left" display-align="before">
             <xsl:copy-of select="$leftS"/>
          </fo:table-cell>

          <fo:table-cell text-align="center" display-align="before">
            <fo:external-graphic content-height="20pt">
              <xsl:attribute name="src">
               <xsl:call-template name="fo-external-image">
                 <xsl:with-param name="filename" select="$header.image.filename"/>
               </xsl:call-template>
              </xsl:attribute>
            <xsl:attribute name="height">14pt</xsl:attribute> 
            <xsl:attribute name="background-color">#FFFFFF</xsl:attribute>
           </fo:external-graphic>          
          </fo:table-cell>

          <fo:table-cell text-align="right" display-align="before">
            <xsl:copy-of select="$rightS"/>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:variable>

<!-- Really output a header? -->
 <xsl:choose>
  <xsl:when test="$pageclass = 'body'">
      <!-- Insert the table defined above -->
    <xsl:copy-of select="$candidate"/>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<!-- Having long lines be broken up  -->
<xsl:param name="hyphenate.verbatim">yes</xsl:param>


<!-- Playing around with the bullet possibilities -->
<!--<xsl:template name="itemizedlist.label.markup">
  <xsl:param name="itemsymbol" select="'disc'"/> -  
</xsl:template>-->

<!-- Trying to improve itemized lists rendering -->
<!-- FIND AND MODIFY <xsl:template match="itemizedlist"> -->

<!-- JUST TO HAVE THE THREE LOGOS ON TITLE PAGE -->
<xsl:template match="corpauthor" mode="book.titlepage.recto.mode">
  <fo:inline color="blue">
    <xsl:apply-templates mode="titlepage.mode"/> <!--If there is text, include it-->
  </fo:inline>
  <!-- Now just put the image -->  
  <fo:external-graphic>
       <xsl:attribute name="src">  
            <xsl:copy-of select="$threeinstitutes.image.filename"/> 
      </xsl:attribute>    
  </fo:external-graphic>
</xsl:template>



<!-- No "Draft" splayed across the page -->
<xsl:param name="draft.watermark.image"></xsl:param> 

<!-- Have screens written on darker background -->
<xsl:attribute-set name="verbatim.properties">
   <xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
   <!-- <xsl:attribute name="border">0.5pt solid blue</xsl:attribute> -->
   <!-- <xsl:attribute name="padding">4mm</xsl:attribute> -->
</xsl:attribute-set>

<!-- Changing the first page appearance -->
<xsl:template name="book.titlepage.recto">
  <fo:block>
    <fo:table inline-progression-dimension="100%" table-layout="fixed">
      <fo:table-column column-width="50%"/>
      <fo:table-column column-width="50%"/>
      <fo:table-body>
<!-- The Main Title -->
        <fo:table-row >
          <fo:table-cell number-columns-spanned="2">
            <fo:block text-align="center" space-after="5cm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/mediaobject"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
<!-- The Subtitle -->
        <fo:table-row >
          <fo:table-cell number-columns-spanned="2">
            <fo:block text-align="center"  space-after="5cm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/subtitle"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>

<!-- The authors -->
        <fo:table-row >
          <fo:table-cell number-columns-spanned="2" >
            <fo:block text-align="center"  space-after="3cm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/author"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>

<!-- TODO: The Logos -->

        <fo:table-row >
          <fo:table-cell number-columns-spanned="2">
            <fo:block text-align="center"  space-after="3cm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/corpauthor"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>

<!-- The Revision and copyright -->
        <fo:table-row>
          <fo:table-cell>
            <fo:block text-align="left" >
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/revhistory"/>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block text-align="right">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/copyright"/>
            </fo:block>
          </fo:table-cell> 
        </fo:table-row >  
      </fo:table-body> 
    </fo:table>
  </fo:block>
</xsl:template>


<!--  The history displayed on titlepage is only the top element -->
<xsl:template match="revhistory" mode="book.titlepage.verso.mode">
  <xsl:apply-templates select="revision[1]/revnumber"/>
</xsl:template>


<!--  PART TOCs appear on the same page as the Part title  
(google gmane.text.docbook.apps Bob Stayton Removing extra blank pages in fo TOC)-->
<xsl:template name="part.titlepage.before.verso" priority="1">
  <xsl:variable name="toc.params">
    <xsl:call-template name="find.path.params">
      <xsl:with-param name="table" select="normalize-space($generate.toc)"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:if test="contains($toc.params, 'toc')">
    <xsl:call-template name="division.toc">
      <xsl:with-param name="toc.context" select="."/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- Turn off the traditional full part toc -->
<xsl:template name="generate.part.toc"> </xsl:template>

</xsl:stylesheet>
