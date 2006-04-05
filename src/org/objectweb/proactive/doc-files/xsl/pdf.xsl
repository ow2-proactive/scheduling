<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 xmlns:date="http://exslt.org/dates-and-times"
 exclude-result-prefixes="date" version="1.0">

 <xsl:import href="http://docbook.sourceforge.net/release/xsl/1.69.1/fo/docbook.xsl" />
 <xsl:import href="common.xsl" />



 <!--  Changing font sizes -->
 <xsl:param name="monospace.font.family">Helvetica</xsl:param>

 <!-- This avoids having "Draft" mode set on. Avoids the other two lines -->
 <xsl:param name="fop.extensions" select="'1'" />
 <!-- <xsl:param name="draft.mode">no</xsl:param>  -->
 <!-- <xsl:param name="draft.watermark.image"></xsl:param>  -->
 <xsl:param name="draft.watermark.image" />

 <!-- Having long lines be broken up  -->
 <xsl:param name="hyphenate.verbatim">yes</xsl:param>

 <!-- Have screens written on darker background -->
 <xsl:attribute-set name="verbatim.properties">
  <xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
 </xsl:attribute-set>


<!-- set this parameter to a zero width value -->
<xsl:param name="body.start.indent">4pt</xsl:param>
<!--  set the title.margin.left parameter to the negative value of the desired indent.  -->
<xsl:param name="title.margin.left">-4pt</xsl:param> 

<!-- All xrefs have the numbering AND the title -->
<xsl:param name="xref.with.number.and.title" select="'1'" />


 <!--  Paper feed -->
 <xsl:param name="paper.type">A4</xsl:param>
 <xsl:param name="page.margin.inner">10mm</xsl:param>
 <xsl:param name="page.margin.outer">13mm</xsl:param>
 <xsl:param name="double.sided">1</xsl:param>


<xsl:attribute-set name="compact.list.item.spacing">
  <xsl:attribute name="space-before.optimum">0em</xsl:attribute>
  <xsl:attribute name="space-before.minimum">0em</xsl:attribute>
  <xsl:attribute name="space-before.maximum">0.2em</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="list.item.spacing">
  <xsl:attribute name="space-before.optimum">0.25em</xsl:attribute>
  <xsl:attribute name="space-before.minimum">0.1em</xsl:attribute>
  <xsl:attribute name="space-before.maximum">0.4em</xsl:attribute>
</xsl:attribute-set>



 <!-- The chapter entries of the toc are in bold, the parts in bold and 11pt. -->
 <xsl:template name="toc.line">
  <xsl:variable name="id">
   <xsl:call-template name="object.id" />
  </xsl:variable>

  <xsl:variable name="label">
   <xsl:apply-templates select="." mode="label.markup" />
  </xsl:variable>

  <xsl:variable name="line">
   <fo:inline keep-with-next.within-line="always">
    <xsl:choose>
     <xsl:when test="self::chapter or self::appendix or self::bibliography">
      <xsl:attribute name="font-weight">bold</xsl:attribute>
      <xsl:attribute name="font-size">12pt</xsl:attribute>
      <xsl:attribute name="color">#00257E</xsl:attribute>
     </xsl:when>
     <xsl:when test="self::part">
      <xsl:attribute name="font-weight">bold</xsl:attribute>
      <xsl:attribute name="font-size">14pt</xsl:attribute>
      <xsl:attribute name="color">#FFFFFF</xsl:attribute>
     </xsl:when>
    </xsl:choose>
    <fo:basic-link internal-destination="{$id}">

      <xsl:variable name="toc_line_name">
        <xsl:copy-of select="name(.)" />
      </xsl:variable >

     <xsl:if test="$toc_line_name = 'chapter' or $toc_line_name = 'part'  or $toc_line_name = 'appendix'">
       <xsl:call-template name="gentext">
          <xsl:with-param name="key" select="$toc_line_name" />
       </xsl:call-template>
       <xsl:call-template name="gentext.space" />
     </xsl:if>

     <xsl:if test="$label != ''">
      <xsl:copy-of select="$label" />
      <xsl:value-of select="$autotoc.label.separator" />
     </xsl:if>
     <xsl:apply-templates select="." mode="title.markup" />
    <xsl:call-template name="gentext.space" />
    <fo:leader leader-pattern="dots"
     leader-pattern-width="3pt" leader-alignment="reference-area"
     keep-with-next.within-line="always" />
     <xsl:call-template name="gentext.space" />
    <fo:page-number-citation ref-id="{$id}" />
    </fo:basic-link>
   </fo:inline>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="self::part">
    <fo:block text-align-last="justify"
     end-indent="{$toc.indent.width}pt"
     last-line-end-indent="-{$toc.indent.width}pt"
     background-color="#00257E" space-before="8mm" space-after="2mm"
     line-height="19pt" padding-top="2mm">
     <xsl:copy-of select="$line" />
    </fo:block>
   </xsl:when>
   <xsl:when test="self::chapter or self::appendix or self::bibliography">
    <fo:block text-align-last="justify"
     end-indent="{$toc.indent.width}pt"
     last-line-end-indent="-{$toc.indent.width}pt" space-before="3mm" space-after="1mm">
     <xsl:copy-of select="$line" />
    </fo:block>
   </xsl:when>
   <xsl:otherwise>
    <fo:block text-align-last="justify"
     end-indent="{$toc.indent.width}pt"
     last-line-end-indent="-{$toc.indent.width}pt">
     <xsl:copy-of select="$line" />
    </fo:block>
   </xsl:otherwise>
  </xsl:choose>
  
  
 </xsl:template>

 <!-- Make graphics in pdf be smaller than page width, if needed-->
 <!--  ??? How do I do that, I need to compare a measure of imagewidth with pagewidth! -->
 <!-- default.image.width exists, should be set maybe only for pdf and big images?? -->


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--    Changing the appearance of ALL the section titles    -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
 <xsl:attribute-set name="section.title.properties">
  <xsl:attribute name="font-weight">bold</xsl:attribute>
  <xsl:attribute name="color">#0010FF</xsl:attribute>
  <xsl:attribute name="font-size">10pt</xsl:attribute>
  <!--All titles not reconfigured are 10pt-->
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

 <xsl:attribute-set name="section.title.level3.properties">
  <xsl:attribute name="font-size">11pt</xsl:attribute>
 </xsl:attribute-set>

 <xsl:attribute-set name="section.title.level4.properties">
  <xsl:attribute name="font-size">10pt</xsl:attribute>
 </xsl:attribute-set>

<!-- Q&A title customized // How do I say "It is = to sect1 properties ?" -->
 <xsl:attribute-set name="qandadiv.title.properties">
  <xsl:attribute name="font-weight">bold</xsl:attribute>
  <xsl:attribute name="color">#0010FF</xsl:attribute>
  <xsl:attribute name="font-size">14pt</xsl:attribute>
  <xsl:attribute name="space-before">7mm</xsl:attribute>
  <xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
 </xsl:attribute-set>

<!-- Q&A title customized // How do I say "It is = to sect2 properties ?-->
 <xsl:attribute-set name="question.title.properties">
  <xsl:attribute name="font-weight">bold</xsl:attribute>
  <xsl:attribute name="color">#0010FF</xsl:attribute>
  <xsl:attribute name="font-size">12pt</xsl:attribute>
  <xsl:attribute name="space-before">5mm</xsl:attribute>
 </xsl:attribute-set>


 <!-- helper for header.table below -->
 <xsl:template name="link.myhelper">
  <xsl:param name="class" select="''" />
  <xsl:param name="level" select="''" />

  <xsl:if test="$level/@id != ''">

    <fo:block space-before="5pt">
     <fo:basic-link
      internal-destination="{$level/@id}">
      <xsl:if test="$class != ''">
       <xsl:call-template name="gentext">
        <xsl:with-param name="key" select="$class" />
       </xsl:call-template>
     <xsl:call-template name="gentext.space" />
       <xsl:apply-templates select="$level" mode="label.markup" />
       <xsl:text>:</xsl:text>
       <xsl:call-template name="gentext.space" />
      </xsl:if>
      <xsl:apply-templates select="$level" mode="title.markup" />
     </fo:basic-link>
    </fo:block>
  </xsl:if>

 </xsl:template>

 <!-- adjust the headers, recalling chapter and part numbers -->
 <xsl:template name="header.table">
  <xsl:param name="pageclass" select="''" />
  <xsl:param name="sequence" select="''" />
  <xsl:param name="gentext-key" select="''" />

<!--  Left is the current node level if it's not chapter: it can be part/TOC/LOT  
      if current node is chapter, display part information. -->
  <xsl:variable name="leftS">
 
   <xsl:choose>

    <xsl:when test="$gentext-key = 'part'">
     <xsl:call-template name="link.myhelper">
      <xsl:with-param name="level" select="." />
      <xsl:with-param name="class" select="$gentext-key"/>
     </xsl:call-template>
    </xsl:when>

    <xsl:otherwise>
     <xsl:call-template name="link.myhelper">
      <xsl:with-param name="level" select=".." />
      <xsl:with-param name="class" select="'part'"/>
     </xsl:call-template>
    </xsl:otherwise>

   </xsl:choose>
  </xsl:variable>

  <!-- Right is only put if current node != Part -->
  <xsl:variable name="rightS">
 
    <xsl:if test="$gentext-key != 'part'">
      <xsl:call-template name="link.myhelper">
      <xsl:with-param name="level" select="." />
      <xsl:with-param name="class">
          <xsl:if test="$gentext-key = 'chapter'" >
             <xsl:copy-of select="$gentext-key"/>
       </xsl:if>
      </xsl:with-param>
     </xsl:call-template>
        </xsl:if>
        
  </xsl:variable>

  <xsl:variable name="candidate">
   <fo:table table-layout="fixed" width="100%">
    <xsl:call-template name="head.sep.rule">
     <xsl:with-param name="pageclass"
      select="$pageclass" />
     <xsl:with-param name="sequence" select="$sequence" />
     <xsl:with-param name="gentext-key"
      select="$gentext-key" />
    </xsl:call-template>

    <fo:table-column column-number="1" />
    <fo:table-column column-number="2" />
    <fo:table-column column-number="3" />

    <fo:table-body>
     <fo:table-row height="15pt">

      <fo:table-cell text-align="left"
       display-align="before">
       <xsl:copy-of select="$leftS" />
      </fo:table-cell>

      <fo:table-cell text-align="center"
       display-align="before">
       <fo:external-graphic>
        <xsl:attribute name="src">
         <xsl:call-template
          name="fo-external-image">
          <xsl:with-param name="filename"
           select="$header.image.filename" />
         </xsl:call-template>
        </xsl:attribute>
        <xsl:attribute name="height">
         14pt
        </xsl:attribute>
        <xsl:attribute
         name="background-color">
         #FFFFFF
        </xsl:attribute>
       </fo:external-graphic>
      </fo:table-cell>

      <fo:table-cell text-align="right"
       display-align="before">
       <xsl:copy-of select="$rightS" />
      </fo:table-cell>
     </fo:table-row>
    </fo:table-body>
   </fo:table>
  </xsl:variable>

  <!-- Really output a header only if not one of the first pages of book -->
  <xsl:if test="$gentext-key != 'book'">
    <xsl:copy-of select="$candidate" />
  </xsl:if>
 </xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--  Changing the footer, which contains the page number    -->
<!--  This footer template's only modification concerns the  -->
<!--     final test, ie which pages have a footer line.      -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="footer.table">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <xsl:choose>
      <xsl:when test="$pageclass = 'index'">
          <xsl:attribute name="margin-left">0pt</xsl:attribute>
      </xsl:when>
  </xsl:choose>

  <xsl:variable name="column1">
    <xsl:choose>
      <xsl:when test="$double.sided = 0">1</xsl:when>
      <xsl:when test="$sequence = 'first' or $sequence = 'odd'">1</xsl:when>
      <xsl:otherwise>3</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="column3">
    <xsl:choose>
      <xsl:when test="$double.sided = 0">3</xsl:when>
      <xsl:when test="$sequence = 'first' or $sequence = 'odd'">3</xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="candidate">
    <fo:table table-layout="fixed" width="100%">
      <xsl:call-template name="foot.sep.rule">
        <xsl:with-param name="pageclass" select="$pageclass"/>
        <xsl:with-param name="sequence" select="$sequence"/>
        <xsl:with-param name="gentext-key" select="$gentext-key"/>
      </xsl:call-template>
      <fo:table-column column-number="1">
        <xsl:attribute name="column-width">
          <xsl:text>proportional-column-width(</xsl:text>
          <xsl:call-template name="header.footer.width">
            <xsl:with-param name="location">footer</xsl:with-param>
            <xsl:with-param name="position" select="$column1"/>
          </xsl:call-template>
          <xsl:text>)</xsl:text>
        </xsl:attribute>
      </fo:table-column>
      <fo:table-column column-number="2">
        <xsl:attribute name="column-width">
          <xsl:text>proportional-column-width(</xsl:text>
          <xsl:call-template name="header.footer.width">
            <xsl:with-param name="location">footer</xsl:with-param>
            <xsl:with-param name="position" select="2"/>
          </xsl:call-template>
          <xsl:text>)</xsl:text>
        </xsl:attribute>
      </fo:table-column>
      <fo:table-column column-number="3">
        <xsl:attribute name="column-width">
          <xsl:text>proportional-column-width(</xsl:text>
          <xsl:call-template name="header.footer.width">
            <xsl:with-param name="location">footer</xsl:with-param>
            <xsl:with-param name="position" select="$column3"/>
          </xsl:call-template>
          <xsl:text>)</xsl:text>
        </xsl:attribute>
      </fo:table-column>

      <fo:table-body>
        <fo:table-row height="14pt">
          <fo:table-cell text-align="left"
                         display-align="after">
            <xsl:if test="$fop.extensions = 0">
              <xsl:attribute name="relative-align">baseline</xsl:attribute>
            </xsl:if>
            <fo:block>
              <xsl:call-template name="footer.content">
                <xsl:with-param name="pageclass" select="$pageclass"/>
                <xsl:with-param name="sequence" select="$sequence"/>
                <xsl:with-param name="position" select="'left'"/>
                <xsl:with-param name="gentext-key" select="$gentext-key"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell text-align="center"
                         display-align="after">
            <xsl:if test="$fop.extensions = 0">
              <xsl:attribute name="relative-align">baseline</xsl:attribute>
            </xsl:if>
            <fo:block>
              <xsl:call-template name="footer.content">
                <xsl:with-param name="pageclass" select="$pageclass"/>
                <xsl:with-param name="sequence" select="$sequence"/>
                <xsl:with-param name="position" select="'center'"/>
                <xsl:with-param name="gentext-key" select="$gentext-key"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell text-align="right"
                         display-align="after">
            <xsl:if test="$fop.extensions = 0">
              <xsl:attribute name="relative-align">baseline</xsl:attribute>
            </xsl:if>
            <fo:block>
              <xsl:call-template name="footer.content">
                <xsl:with-param name="pageclass" select="$pageclass"/>
                <xsl:with-param name="sequence" select="$sequence"/>
                <xsl:with-param name="position" select="'right'"/>
                <xsl:with-param name="gentext-key" select="$gentext-key"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:variable>

  <!-- Really output a footer?  book titlepages have no footers at all -->
  <xsl:if test="$gentext-key != 'book'">
      <xsl:copy-of select="$candidate"/>
  </xsl:if>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - -  -->
<!-- Changing the first page appearance -->
<!-- - - - - - - - - - - - - - - - - -  -->

<!--  Changing the font for the authors on titlepage -->
  <xsl:template match="bookinfo/author" mode="book.titlepage.recto.mode">
  <fo:inline color="#0010FF" >
   <xsl:attribute name="font-weight">bold</xsl:attribute>
   <xsl:attribute name="font-size">16pt</xsl:attribute>
     <xsl:apply-templates mode="titlepage.mode" /> 
  </fo:inline>
 </xsl:template>

<!--  The appearance of the Subtitle -->
  <xsl:template match="bookinfo/subtitle" mode="book.titlepage.recto.mode">
  <fo:inline color="#0010FF" >
   <xsl:attribute name="font-weight">bold</xsl:attribute>
   <xsl:attribute name="font-size">34pt</xsl:attribute>
     <xsl:apply-templates mode="titlepage.mode" /> 
  </fo:inline>

 </xsl:template>


<!-- Remove the copyright from the second page, as it appears in the legal notice anyways. -->
 <xsl:template match="copyright" mode="book.titlepage.verso.auto.mode"/> 
 
 <xsl:template match="legalnotice" mode="titlepage.mode">
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>
  <fo:block id="{$id}">
    <xsl:call-template name="formal.object.heading">
      <xsl:with-param name="title">Legal Notice</xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates mode="titlepage.mode"/>
  </fo:block>
</xsl:template>

   
  <xsl:template name="book.titlepage.recto">
  <fo:block break-after="page">

<!-- The Main Title -->
            <fo:block text-align="center" space-before="0cm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/mediaobject"/>
            </fo:block>

<!-- The Subtitle -->
            <fo:block text-align="center" space-before="40mm"  
                   margin-left="2cm" margin-right="2cm"  line-height="25mm"
                   border-color="blue" border-width="0.7mm" border-style="solid"
                   padding-top="5mm">
              <xsl:apply-templates 
                     mode="book.titlepage.recto.mode" 
                     select="bookinfo/subtitle"/>
            </fo:block>

<!-- Two logos and the author's name in a 1x3 table -->
   <fo:table table-layout="fixed" space-before="45mm">

    <fo:table-column />
    <fo:table-column />
    <fo:table-column />

    <fo:table-body>
     <fo:table-row>
      <fo:table-cell padding-right="5mm">
       <fo:block text-align="right">
          <fo:external-graphic >
        <xsl:attribute name="src">images/logo-INRIA.png</xsl:attribute>
           </fo:external-graphic>
       </fo:block>
      </fo:table-cell>
      <fo:table-cell padding-top="20mm">
       <fo:block text-align="center" >
                                                          <xsl:apply-templates 
                                                            mode="book.titlepage.recto.mode" 
                                                            select="bookinfo/author"/>
       </fo:block>
      </fo:table-cell>
      <fo:table-cell padding-left="5mm">
       <fo:block text-align="left"  space-before="3mm">
        <fo:external-graphic>
         <xsl:attribute name="src">images/logo-objectweb250.png</xsl:attribute>
         </fo:external-graphic>
       </fo:block>
      </fo:table-cell>
     </fo:table-row>
    </fo:table-body>
   </fo:table>

<!-- The other 2 missing logos, also in a table -->
   <fo:table table-layout="fixed" space-before="2mm">
     <fo:table-column  />
     <fo:table-column  />
     <fo:table-body>
     <fo:table-row>
      <fo:table-cell  padding-right="10mm">
       <fo:block text-align="right">
       <fo:external-graphic>
        <xsl:attribute name="src">images/logo-UNSA.png</xsl:attribute>
        <xsl:attribute name="height">60pt</xsl:attribute>
        <xsl:attribute name="content-height">60pt</xsl:attribute>
       </fo:external-graphic>
       </fo:block>
      </fo:table-cell>
      <fo:table-cell  padding-left="1cm">
       <fo:block text-align="left">
        <fo:external-graphic>
         <xsl:attribute name="src">images/logo-CNRS.png</xsl:attribute>
         <xsl:attribute name="height">60pt</xsl:attribute>
         <xsl:attribute name="content-height">60pt</xsl:attribute>
        </fo:external-graphic>
       </fo:block>
      </fo:table-cell>
     </fo:table-row>

    </fo:table-body>
   </fo:table>

 <!-- The Revision and copyright -->
   <fo:table table-layout="fixed" space-before="1cm" >

    <fo:table-column  />
    <fo:table-column  />
        <fo:table-body>
        <fo:table-row>
          <fo:table-cell>

      <fo:block text-align="left" font-size="12pt" font-weight="bold">
       V
       <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/revhistory/revision/revnumber" />
       <xsl:call-template name="gentext.space" />
       <xsl:call-template name="gentext.space" />
       <xsl:call-template name="gentext.space" />
       <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/revhistory/revision/date" />
      </fo:block>

          </fo:table-cell>
          <fo:table-cell>
            <fo:block text-align="right"  font-size="12pt" font-weight="bold">
               <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/copyright" />
            </fo:block>
          </fo:table-cell> 
        </fo:table-row >  
    </fo:table-body>
   </fo:table>
   
  </fo:block>

 </xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - -->
<!--        Making Part TOC appear on part pages   -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - -->

 <!--  force PART TOCs to appear on the same page as the Part title  
  (google gmane.text.docbook.apps Bob Stayton Removing extra blank pages in fo TOC)-->
<xsl:template name="part.titlepage.before.verso" priority="1">
  <xsl:variable name="toc.params">
   <xsl:call-template name="find.path.params">
    <xsl:with-param name="table"
     select="normalize-space($generate.toc)" />
   </xsl:call-template>
  </xsl:variable>
  <xsl:if test="contains($toc.params, 'toc')">
   <xsl:call-template name="division.toc">
    <xsl:with-param name="toc.context" select="." />
   </xsl:call-template>
  </xsl:if>
</xsl:template>

 <!-- DANGER: THIS MEANS TOCS FOR PARTS ARE FORCED -->
 <!-- Turn off the traditional full part toc -->
<xsl:template name="generate.part.toc"/>

<!-- HOW A PART TOC IS GENERATED -->
<xsl:template match="part" mode="toc">
  <xsl:param name="toc-context" select="."/>

  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="cid">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="$toc-context"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:call-template name="toc.line"/>

  <xsl:variable name="nodes" select="qandaset|chapter|appendix|preface|reference|
                                     refentry|article|index|glossary|
                                     bibliography"/>

  <xsl:variable name="depth.from.context" select="count(ancestor::*)-count($toc-context/ancestor::*)"/>

  <xsl:if test="$toc.section.depth > 0 
                and $toc.max.depth > $depth.from.context
                and $nodes">
    <fo:block id="toc.{$cid}.{$id}">
      <xsl:attribute name="margin-left">
        <xsl:call-template name="set.toc.indent"/>
      </xsl:attribute>
      
      <xsl:apply-templates select="$nodes" mode="toc">
        <xsl:with-param name="toc-context" select="$toc-context"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:if>
</xsl:template>

<!-- qandaset bug - had to remove some ids. -->
<xsl:template match="question">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>

  <xsl:variable name="entry.id">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="parent::*"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="label_and_text">
    <xsl:apply-templates select="." mode="label.markup" />
    <xsl:apply-templates select="." mode="intralabel.punctuation"/>
    <xsl:call-template name="gentext.space" />
    <xsl:apply-templates select="*[local-name(.)!='label']"/> 
  </xsl:variable >

  <fo:block id="{$id}" xsl:use-attribute-sets="question.title.properties">
     <xsl:value-of select="$label_and_text"/> 
  </fo:block>
</xsl:template>

<!-- qandaset bug - had to remove some ids. -->
<xsl:template match="answer">
  <xsl:variable name="entry.id">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="parent::*"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="deflabel">
        <xsl:value-of select="(ancestor-or-self::*[@defaultlabel])[last()]/@defaultlabel"/>
  </xsl:variable>

    <fo:block id="{$entry.id}" >
      <xsl:choose>
        <xsl:when test="$deflabel = 'none'">
           <xsl:message>
             <xsl:text>qandaentry: 'answer' has no text!</xsl:text><xsl:copy-of select="."/>
           </xsl:message>
          <fo:block/>
        </xsl:when>
        <xsl:otherwise>
          <fo:block>
            <xsl:variable name="answer.label">
              <xsl:apply-templates select="." mode="label.markup"/>
            </xsl:variable>
            <xsl:copy-of select="$answer.label"/>
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:block>
    <fo:block >
      <xsl:apply-templates select="*[local-name(.)!='label']"/>
    </fo:block>
</xsl:template>

<!--  more Q&A specifics  -->
<xsl:template match="qandaset">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>

  <xsl:variable name="label-width">
    <xsl:call-template name="dbfo-attribute">
      <xsl:with-param name="pis" select="processing-instruction('dbfo')"/>
      <xsl:with-param name="attribute" select="'label-width'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="label-length">
    <xsl:choose>
      <xsl:when test="$label-width != ''">
        <xsl:value-of select="$label-width"/>
      </xsl:when>
      <xsl:when test="descendant::label">
        <xsl:call-template name="longest.term">
          <xsl:with-param name="terms" select="descendant::label"/>
          <xsl:with-param name="maxlength" select="20"/>
        </xsl:call-template>
        <xsl:text>em * 0.50</xsl:text>
      </xsl:when>
      <xsl:otherwise>2.5em</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>


<!-- Output a TOC for every qandaset -->
    <fo:block >
        <xsl:attribute name="margin-left">
          <xsl:call-template name="set.toc.indent">
             <xsl:with-param name="reldepth" select="2"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:call-template name="component.toc"/>
        <xsl:call-template name="component.toc.separator"/>
    </fo:block >

  <fo:block id="{$id}">
    <xsl:if test="blockinfo/title|info/title|title">
       <fo:block xsl:use-attribute-sets="qandaset.title.properties">
         <xsl:apply-templates select="." mode="label.markup" />
         <xsl:value-of select="$autotoc.label.separator" />
         <xsl:apply-templates select="." mode="title.markup" />
       </fo:block>
    </xsl:if>

    <xsl:apply-templates select="*[name(.) != 'title'
                                 and name(.) != 'titleabbrev'
                                 and name(.) != 'qandadiv'
                                 and name(.) != 'qandaentry']"/>

    <xsl:if test="qandadiv">
       <xsl:apply-templates select="qandadiv"/>
    </xsl:if>

    <xsl:if test="qandaentry">
        <xsl:apply-templates select="qandaentry"/>
    </xsl:if>
  </fo:block>
</xsl:template>

<!--  more Q&A specifics  -->
<xsl:template match="qandadiv">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>

  <xsl:variable name="label-width">
    <xsl:call-template name="dbfo-attribute">
      <xsl:with-param name="pis"
                      select="processing-instruction('dbfo')"/>
      <xsl:with-param name="attribute" select="'label-width'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="label-length">
    <xsl:choose>
      <xsl:when test="$label-width != ''">
        <xsl:value-of select="$label-width"/>
      </xsl:when>
      <xsl:when test="descendant::label">
        <xsl:call-template name="longest.term">
          <xsl:with-param name="terms" select="descendant::label"/>
          <xsl:with-param name="maxlength" select="20"/>
        </xsl:call-template>
        <xsl:text>em * 0.50</xsl:text>
      </xsl:when>
      <xsl:otherwise>2.5em</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <fo:block id="{$id}">
    <xsl:if test="blockinfo/title|info/title|title">
       <fo:block xsl:use-attribute-sets="qandadiv.title.properties">
         <xsl:apply-templates select="." mode="label.markup" />
         <xsl:value-of select="$autotoc.label.separator" />
         <xsl:apply-templates select="." mode="title.markup" />
       </fo:block>
    </xsl:if>

    <xsl:apply-templates select="*[name(.) != 'title'
                                 and name(.) != 'titleabbrev'
                                 and name(.) != 'qandaentry']"/>

    <xsl:if test="qandaentry">
        <xsl:apply-templates select="qandaentry"/>
    </xsl:if>
  </fo:block>
</xsl:template>


<!-- DEBUG METHOD. -->
<xsl:template name="toto.tutu">
  <fo:block background-color="#FF0000">Toto Tutu</fo:block>
</xsl:template>

<!-- emphasis in programlistings contains color -->
<xsl:template match="emphasis">
  <xsl:variable name="depth">
    <xsl:call-template name="dot.count">
      <xsl:with-param name="string">
    <xsl:number level="multiple"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="@role='bold' or @role='strong'">
      <xsl:call-template name="inline.boldseq"/>
    </xsl:when>
    <xsl:when test="@role='underline'">
      <fo:inline text-decoration="underline">
        <xsl:call-template name="inline.charseq"/>
      </fo:inline>
    </xsl:when>
    <xsl:when test="@role='strikethrough'">
      <fo:inline text-decoration="line-through">
        <xsl:call-template name="inline.charseq"/>
      </fo:inline>
    </xsl:when>

<!--  Added this test, which can be triggered after code beautifier -->
    <xsl:when test="@role='keyword' or @role='codeword' or @role='typeword' or @role='comment' or @role='string'">
        <xsl:call-template name="myinline.emphasis">
          <xsl:with-param name="role" select="@role"/>
         </xsl:call-template>
    </xsl:when>

    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$depth mod 2 = 1">
          <fo:inline font-style="normal">
            <xsl:apply-templates/>
          </fo:inline>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="inline.italicseq"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- A way to color in the emphasis, key being the 'role' attribute in {keyword,codeword,typeword,comment,string} -->
 <xsl:template name="myinline.emphasis">
  <xsl:param name="role" select="''" />

  <xsl:param name="content">
   <xsl:apply-templates />
  </xsl:param>

  <fo:inline>
   <xsl:if test="@id">
    <xsl:attribute name="id">
     <xsl:value-of select="@id" />
    </xsl:attribute>
   </xsl:if>

   <xsl:if test="@role = 'comment' or @role = 'string'">
    <xsl:attribute name="font-style">italic</xsl:attribute>
   </xsl:if>
   <xsl:attribute name="color">
    <xsl:choose>
     <xsl:when test="@role = 'keyword'">#00257E</xsl:when>
     <xsl:when test="@role = 'codeword'">#00257E</xsl:when>
     <xsl:when test="@role = 'typeword'">#931793</xsl:when>
     <xsl:when test="@role = 'comment'">#0f9626</xsl:when>
     <xsl:when test="@role = 'string'">#707070</xsl:when>
     <xsl:otherwise>#F00000</xsl:otherwise> <!-- This should never happen. -->
    </xsl:choose>
   </xsl:attribute>

   <xsl:if test="@dir">
    <xsl:attribute name="direction">
     <xsl:choose>
      <xsl:when test="@dir = 'ltr' or @dir = 'lro'">ltr</xsl:when>
      <xsl:otherwise>rtl</xsl:otherwise>
     </xsl:choose>
    </xsl:attribute>
   </xsl:if>
   <xsl:copy-of select="$content" />
  </fo:inline>

 </xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - -  -->
<!-- adding the questions of the FAQ in the TOC -->
<!-- - - - - - - - - - - - - - - - - - - - - -  -->

<!-- How to write a toc, for an appendix -->
<xsl:template match="appendix" mode="toc">
  <xsl:param name="toc-context" select="."/>

  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="cid">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="$toc-context"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:call-template name="toc.line"/>

  <xsl:variable name="nodes" select="section|sect1
                                     |simplesect[$simplesect.in.toc != 0]
                                     |refentry|qandaset|qandadiv|question"/>

  <xsl:variable name="depth.from.context" select="count(ancestor::*)-count($toc-context/ancestor::*)"/>

  <xsl:if test="$toc.section.depth > 0 and $toc.max.depth > $depth.from.context and $nodes">
    <fo:block id="toc.{$cid}.{$id}">
      <xsl:attribute name="margin-left">
        <xsl:call-template name="set.toc.indent"/>
      </xsl:attribute>
              
      <xsl:apply-templates select="$nodes" mode="toc">
        <xsl:with-param name="toc-context" select="$toc-context"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:if>
</xsl:template>

<xsl:template name="component.toc">
  <xsl:param name="toc-context" select="."/>

  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="cid">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="$toc-context"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="nodes" select="section|sect1|refentry
                                     |article|bibliography|glossary
                                     |appendix|index|qandaset|qandadiv|question"/>
  <xsl:if test="$nodes">
    <fo:block id="toc...{$id}" xsl:use-attribute-sets="toc.margin.properties">
      <xsl:call-template name="table.of.contents.titlepage"/>
      <xsl:apply-templates select="$nodes" mode="toc">
        <xsl:with-param name="toc-context" select="$toc-context"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:if>
</xsl:template>


<xsl:template match="question" mode="toc"> 
  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="label">
    <xsl:apply-templates select="." mode="label.markup"/>
  </xsl:variable>

  <xsl:variable name="markup">
      <xsl:value-of select="."/>
  </xsl:variable>

<fo:block text-align-last="justify"
            text-align="start"
            end-indent="{$toc.indent.width}pt"
            last-line-end-indent="15pt" >

<!-- Hacking the question toc line indent, to skip the qandaset gap -->
      <xsl:attribute name="margin-left">
        <xsl:call-template name="set.toc.indent">
          <xsl:with-param name="reldepth" select="3"/>
        </xsl:call-template>
      </xsl:attribute>

    
<fo:inline keep-with-next.within-line="always">
      <fo:basic-link internal-destination="{$id}">
        <xsl:if test="$label != ''">
          <xsl:copy-of select="$label"/>
          <xsl:value-of select="$autotoc.label.separator"/>
        </xsl:if>
        <xsl:copy-of select="$markup"/>
      </fo:basic-link>
    </fo:inline>
    <fo:inline keep-together.within-line="always">
     <xsl:call-template name="gentext.space" />
      <fo:leader leader-pattern="dots"
                 leader-pattern-width="3pt"
                 leader-alignment="reference-area"
                 keep-with-next.within-line="always"/>
     <xsl:call-template name="gentext.space" />

      <fo:basic-link internal-destination="{$id}">
        <fo:page-number-citation ref-id="{$id}"/>
      </fo:basic-link>
    </fo:inline>
  </fo:block>
</xsl:template>


<xsl:template match="qandadiv" mode="toc">
  <xsl:call-template name="toc.line"/>
  <!--  Should be done through a nodes scheme, but it's harder -->
  <!--  Indeed, I'm skipping out the direct sons, to have the question grandchildren only-->
  <xsl:for-each select="./qandaentry/question">
     <xsl:apply-templates select="." mode="toc" />
  </xsl:for-each > 
</xsl:template>


<!-- Representation of a qandaset and siblings in a TOC -->
<xsl:template match="qandaset" mode="toc">
  <xsl:param name="toc-context" select="."/>

  <xsl:variable name="id">
    <xsl:call-template name="object.id"/>
  </xsl:variable>

  <xsl:variable name="cid">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="$toc-context"/>
    </xsl:call-template>
  </xsl:variable>

<!-- This is the line that makes qandasets appear in the TOC. Removed on purpose.  -->
<!--   <xsl:call-template name="toc.line"/> -->
<!-- In fact, we only want the siblings, not the qandaset -->

  <xsl:variable name="nodes" select="qandadiv|qandaentry|question"/>

  <xsl:if test="$nodes">
    <fo:block id="toc.{$cid}.{$id}">
     <!-- Removed to leave take the room not used by the hidden qandasets.
        <xsl:attribute name="margin-left">
        <xsl:call-template name="set.toc.indent"/>
      </xsl:attribute>
     -->         
      <xsl:apply-templates select="$nodes" mode="toc">
        <xsl:with-param name="toc-context" select="$toc-context"/>
      </xsl:apply-templates>
    </fo:block>
  </xsl:if>
</xsl:template>



<!-- Just a copy of labels.xsl, line 272 which applied to 'sect1' -->
<!-- I also needed to skip the parent qandaset, so a ".." is turned into a "../.." -->
<xsl:template match="qandadiv" mode="label.markup">
  <!-- if the parent is a component, maybe label that too -->
  <xsl:variable name="parent.is.component">
    <xsl:call-template name="is.component">
      <xsl:with-param name="node" select="../.."/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="component.label">
    <xsl:if test="$section.label.includes.component.label != 0
                  and $parent.is.component != 0">
      <xsl:variable name="parent.label">
        <xsl:apply-templates select="../.." mode="label.markup"/>
      </xsl:variable>
      <xsl:if test="$parent.label != ''">
        <xsl:apply-templates select="../.." mode="label.markup"/>
        <xsl:apply-templates select="../.." mode="intralabel.punctuation"/>
      </xsl:if>
    </xsl:if>
  </xsl:variable>


  <xsl:variable name="is.numbered">
    <xsl:call-template name="label.this.section"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="@label">
      <xsl:value-of select="@label"/>
    </xsl:when>
    <xsl:when test="$is.numbered != 0">
      <xsl:variable name="format">
        <xsl:call-template name="autolabel.format">
          <xsl:with-param name="format" select="$section.autolabel"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:copy-of select="$component.label"/>
      <xsl:number format="{$format}" count="qandadiv"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>


<xsl:template match="question" mode="label.markup">
  <xsl:variable name="lparent" select="(ancestor::set
                                       |ancestor::book
                                       |ancestor::chapter
                                       |ancestor::appendix
                                       |ancestor::preface
                                       |ancestor::qandaset
                                       |ancestor::qandadiv
                                       |ancestor::section
                                       |ancestor::simplesect
                                       |ancestor::sect1
                                       |ancestor::sect2
                                       |ancestor::sect3
                                       |ancestor::sect4
                                       |ancestor::sect5
                                       |ancestor::refsect1
                                       |ancestor::refsect2
                                       |ancestor::refsect3)[last()]"/>

  <xsl:variable name="lparent.prefix">
    <xsl:apply-templates select="$lparent" mode="label.markup"/>
  </xsl:variable>

  <xsl:variable name="prefix">
    <xsl:if test="$qanda.inherit.numeration != 0">
      <xsl:choose>
        <xsl:when test="ancestor::qandadiv">
          <xsl:apply-templates select="ancestor::qandadiv[1]" mode="label.markup"/>
          <xsl:apply-templates select="ancestor::qandadiv[1]"
                               mode="intralabel.punctuation"/>
        </xsl:when>
        <xsl:when test="$lparent.prefix != ''">
          <xsl:apply-templates select="$lparent" mode="label.markup"/>
          <xsl:apply-templates select="$lparent" mode="intralabel.punctuation"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>

  <xsl:variable name="inhlabel"
                select="ancestor-or-self::qandadiv/@defaultlabel[1]"/>

  <xsl:variable name="deflabel">
    <xsl:choose>
      <xsl:when test="$inhlabel != ''">
        <xsl:value-of select="$inhlabel"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$qanda.defaultlabel"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="label" select="label"/>

  <xsl:choose>
    <xsl:when test="count($label)>0">
      <xsl:apply-templates select="$label"/>
    </xsl:when>

    <xsl:when test="$deflabel = 'number' and local-name(.) = 'question'">
      <xsl:value-of select="$prefix"/>
      <xsl:number level="multiple" count="qandaentry" format="1"/>
    </xsl:when>
  </xsl:choose>

</xsl:template>

</xsl:stylesheet>

<!-- 
<xsl:message>
<xsl:text> OK, question.toc </xsl:text> <xsl:copy-of select="$id" /> 
</xsl:message>
 -->