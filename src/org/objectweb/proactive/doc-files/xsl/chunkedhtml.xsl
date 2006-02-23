<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:date="http://exslt.org/dates-and-times"  
                exclude-result-prefixes="date"  
                version="1.0">


<!-- Load the "several html chunks" style sheet -->
<xsl:import href="http://docbook.sourceforge.net/release/xsl/1.69.1/html/chunk.xsl"/>
<xsl:import href="html.xsl"/>

<!-- Use chapter ids for html filenames -->
<xsl:param name="use.id.as.filename">1</xsl:param>

<!-- We want to see title in navigation bars -->
<xsl:param name="navig.showtitles">1</xsl:param>
<xsl:param name="suppress.navigation">0</xsl:param>
<xsl:param name="suppress.header.navigation">0</xsl:param>


<!-- Only make new files for new chapters and parts, not for sections -->
<xsl:param name="chunk.section.depth">0</xsl:param>
<!-- Please put list of tables/examples/figures on separate page from full table of contents -->
<xsl:param name="chunk.separate.lots">1</xsl:param> 
<!--  Indent html output -->
<xsl:param name="chunker.output.indent">yes</xsl:param> 

<!-- Make the legal notice appear, but only as a link - no need for everyone to see it -->
<xsl:param name="generate.legalnotice.link">1</xsl:param >

<!-- No idea what these two do -->
<!-- <xsl:param name="chunk.tocs">0</xsl:param> -->
<!-- <xsl:param name="chunk.lots">0</xsl:param> -->

<!-- Replace the standard home button (bottom line, middle) by 'Table of Content' -->
<xsl:param name="local.l10n.xml" select="document('')" />
<l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
 <l:l10n language="en">
  <l:gentext key="nav-home" text="Table of Contents"/>
 </l:l10n>
</l:i18n>

<!-- Customizes the navigation bar (at the top) contain part title, then chapter title -->
<xsl:template name="header.navigation">
  <xsl:param name="prev" select="Prev"/>  <!--Values here are those if param not specified-->
  <xsl:param name="next" select="Next"/>  <!--Values here are those if param not specified-->
  <xsl:param name="nav.context"/>         <!-- just states you can pass a "nav.context" parameter -->

  
<xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <xsl:variable name="row1" select="$navig.showtitles != 0"/> 
  <xsl:variable name="row2" select="count($prev) &gt; 0
                                    or (count($up) &gt; 0 
                                        and generate-id($up) != generate-id($home)
                                        and $navig.showtitles != 0)
                                    or count($next) &gt; 0"/>

  <xsl:if test="$suppress.navigation = '0' and $suppress.header.navigation = '0'">
    <div class="navheader">
        <table width="100%" summary="Navigation header">
            <tr>
<!-- prev button -->
              <td width="15%" align="left">
                <xsl:if test="count($prev)>0">
                  <a accesskey="p">
                    <xsl:attribute name="href">
                      <xsl:call-template name="href.target">
                        <xsl:with-param name="object" select="$prev"/>
                      </xsl:call-template>
                    </xsl:attribute>
                    <xsl:call-template name="navig.content">
                      <xsl:with-param name="direction" select="'prev'"/>
                    </xsl:call-template>
                  </a>
                </xsl:if>
                <xsl:text>&#160;</xsl:text>
              </td>
<!--  part and chapter lines -->
              <td width="50%" align="center">
                <xsl:choose>
                  <!--  Not sure of the exact meaning, but interpret it as "is part and chapter strings exists" -->
                  <xsl:when test="count($up) > 0
                                  and generate-id($up) != generate-id($home)
                                  and $navig.showtitles != 0">
                    <!-- Insert the 'up' string, ie the Part title -->
               <a accesskey="p">
                 <xsl:attribute name="href">
                    <xsl:call-template name="href.target">
                       <xsl:with-param name="object" select="$up"/>
                    </xsl:call-template>
                 </xsl:attribute>
                 <xsl:apply-templates select="$up" mode="object.title.markup"/>
              </a>
              <br/>
                    <!-- Insert the 'local' string, ie the Chapter title -->                    
                    <!--  No need for link, we're already at the top of the page!  -->
                    <xsl:apply-templates select="." mode="object.title.markup"/>
                  </xsl:when>

                  <xsl:otherwise>
                     <!--  Just insert the local string, if there is no up string -->                    
                     <xsl:apply-templates select="." mode="object.title.markup"/>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
<!--  ProActive Logo -->
              <td width="20%" align="center">
                 <a href="http://ProActive.ObjectWeb.org">
                  <img> 
                    <xsl:attribute name="src"> <xsl:copy-of select="$header.image.filename"/> </xsl:attribute> 
                    <xsl:attribute name="alt">Back to the ProActive Home Page</xsl:attribute>
                    <xsl:attribute name="title">Back to the ProActive Home Page</xsl:attribute>
                   </img>
                  </a>
                </td>
<!-- next button -->
              <td width="15%" align="right">
                <xsl:text>&#160;</xsl:text>
                <xsl:if test="count($next)>0">
                  <a accesskey="n">
                    <xsl:attribute name="href">
                      <xsl:call-template name="href.target">
                        <xsl:with-param name="object" select="$next"/>
                      </xsl:call-template>
                    </xsl:attribute>
                    <xsl:call-template name="navig.content">
                      <xsl:with-param name="direction" select="'next'"/>
                    </xsl:call-template>
                  </a>
                </xsl:if>
              </td>
            </tr>
        </table>
      <xsl:if test="$header.rule != 0">
        <hr/>
      </xsl:if>
    </div>
  </xsl:if>
</xsl:template>

<!-- This is the customization for the part title pages. 
It's the line containing the title of the page -->
<!--<xsl:template name="division.title">  
  <hr/>Hey, lost division title<hr/>
</xsl:template>-->
<!-- TODO: see what this used to do. Because it does nothing now... 
<xsl:template name="division.title">
  <xsl:param name="node" select="."/>
  <h1>
    <xsl:attribute name="class">title</xsl:attribute>
    <xsl:call-template name="anchor">
      <xsl:with-param name="node" select="$node"/>
      <xsl:with-param name="conditional" select="0"/>
    </xsl:call-template>
    <xsl:apply-templates select="$node" mode="object.title.markup">
      <xsl:with-param name="allow-anchors" select="1"/>
    </xsl:apply-templates>
  </h1>
</xsl:template>-->

<!-- I don't know where the titlepage image got resized. So I'm rewritting this one -->
<!--  TODO: put this back ! removed for ant task
<xsl:template match="mediaobject" mode="book.titlepage.recto.auto.mode">
   <xsl:apply-templates select="." mode="book.titlepage.recto.mode"/>
</xsl:template>-->


<!-- This was required by the and target, but it should not be like this!  -->
<!--<xsl:template name="href.target"> </xsl:template>
<xsl:template name="navig.content"> </xsl:template>
<xsl:template name="datetime.format"> </xsl:template>
<xsl:template name="anchor"> </xsl:template>
<xsl:template name="component.title"> </xsl:template>-->

 
</xsl:stylesheet>


