<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:date="http://exslt.org/dates-and-times"
 exclude-result-prefixes="date" version="1.0">

 <xsl:import href="common.xsl" />

 <!-- Import profiled highlighting color -->
 <xsl:import href="../highlighting/xsl/html-hl.xsl" />
 
 <xsl:import href="../docbook-xsl/common/personal-templates.xsl" />

 <!-- Configure the html stylesheet to use -->
 <xsl:param name="html.stylesheet" select="'main.css'" />
 <!-- Just use the image size for the html output. Width=... has no effect. -->
 <xsl:param name="ignore.image.scaling">1</xsl:param>
<!-- Relative position for the htmlized java files -->
 <xsl:param name="html.java.files">../ProActive_src_html/org/objectweb/proactive/</xsl:param>
 <xsl:param name="html.java.suffix">.html</xsl:param>
<!-- Relative position for the example deployment descriptors -->
 <xsl:param name="html.xml.files">../</xsl:param>

<!--- - - - - - - - - - - - - - - - - - - - - - - - - - --> 
<!-- Add copyright information to all the page footers. -->
<!--- - - - - - - - - - - - - - - - - - - - - - - - - - -->
 <xsl:template name="user.footer.content">
  <P class="copyright" align="right">
   © 1997-2009
   <A href="http://www-sop.inria.fr/">
    INRIA Sophia Antipolis
   </A>
   All Rights Reserved
  </P>
 </xsl:template>

<!--- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- Redefining the corporate authors, by outputing just pictures -->
<!--- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
 <xsl:template match="corpauthor" mode="book.titlepage.recto.mode">
  <xsl:apply-templates mode="titlepage.mode" />
   <br/>  <!--BAD: adding some white space through br-->
   <table border="0" cellpadding="0" cellspacing="0">
   <tbody>
    <tr>
     <td>
      <a href="http://www.inria.fr"><img border="0">
       <xsl:attribute name="src">images/logo-INRIA.png</xsl:attribute>
       <xsl:attribute name="alt">INRIA</xsl:attribute>
       <xsl:attribute name="title">A CNRS-INRIA-UNSA Research team</xsl:attribute>
      </img></a>
     </td>
     <td style="width: 30px;"/><!-- Empty cell to have room between images -->
     <td>
      <a href="http://www.unice.fr"><img border="0">
       <xsl:attribute name="src">images/logo-UNSA.png</xsl:attribute>
       <xsl:attribute name="alt">UNSA</xsl:attribute>
       <xsl:attribute name="title">A CNRS-INRIA-UNSA Research team</xsl:attribute>
      </img></a>
     </td>
     <td style="width: 30px;"/><!-- Empty cell to have room between images -->
     <td>
      <a href="http://www.cnrs.fr"><img border="0">
       <xsl:attribute name="src">images/logo-CNRS.png</xsl:attribute>
       <xsl:attribute name="alt">CNRS-I3S</xsl:attribute>
       <xsl:attribute name="title">A CNRS-INRIA-UNSA Research team</xsl:attribute>
      </img></a>
     </td>
    </tr>
   </tbody>
  </table>
 </xsl:template>

<!--- - -  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- Redefining the media objects in the titlepage (adding alt text) -->
<!--- - -  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
 <xsl:template match="bookinfo/mediaobject" mode="book.titlepage.recto.mode">
  <xsl:variable name="object"  select="imageobject"/>
  <xsl:variable name="filename">
    <xsl:call-template name="mediaobject.filename">
      <xsl:with-param name="object" select="$object"/>
    </xsl:call-template>
  </xsl:variable>

    <xsl:if test="@id"><a name="{@id}"/></xsl:if>
    <img>
        <xsl:attribute name="src"><xsl:copy-of select="$filename"/></xsl:attribute>
        <xsl:attribute name="alt">ProActive: Programming, Composing, Deploying on the Grid</xsl:attribute>
    </img> 
 </xsl:template> 


	<!--  The appearance of the Subtitle -->
	<xsl:template match="bookinfo/subtitle"
		mode="book.titlepage.recto.mode">

		<!-- Main title -->
		<xsl:choose>
			<xsl:when test="@role='main'">
			  <h1><xsl:value-of select="." /></h1>
			</xsl:when>
		</xsl:choose>

		<!-- motto -->
		<xsl:choose>
			<xsl:when test="@role='motto'">
				<h5>
				   <xsl:value-of select="." />
				</h5>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
<!--- - - - - - - - - - - - - - - - - - - - - - - - --> 
<!-- Specifying how the titlepage should look like -->
<!--- - - - - - - - - - - - - - - - - - - - - - - - -->
 <xsl:template name="book.titlepage.recto">

  <div> <xsl:attribute name="align">center</xsl:attribute><!-- Everything in titlepage is centered -->

      <a href="http://www.objectweb.org"><img border="0"> <!-- Start off with ObjectWeb -->
		<xsl:attribute name="src">images/logoOW2.png</xsl:attribute>
	    <xsl:attribute name="alt">ObjectWeb</xsl:attribute>
        <xsl:attribute name="title">A project of the ObjectWeb Consortium</xsl:attribute>
       </img></a><br/>

  <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/mediaobject" />  <!-- The Main Title -->
  <br/>
  <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/subtitle" />   <!-- The Subtitle -->
  <br/>
  <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/author" /> <!-- The authors -->
  <br/>
  <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/corpauthor" /> <!-- The three Logos -->
  <!-- The Revision and copyright in a table -->
  <table border="0" cellpadding="0" cellspacing="0">
   <tbody>
    <tr>
     <td>
      <b>Version<xsl:call-template name="gentext.space" />
       <xsl:copy-of select="$VERSION" />          <!--This variable is passed as a parameter in the ant task-->
       <xsl:call-template name="gentext.space" />
       <xsl:copy-of select="$TODAY" />            <!--This variable is passed as a parameter in the ant task-->
      </b>
     </td>
     <td style="width: 50px;"/>   <!--Empty cell to leave room between cells-->
     <td>
       <b><xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/copyright" /></b>
     </td>
    </tr>
   </tbody>
  </table>
  </div>
  <hr /> <!-- A line to draw a separation -->
  <xsl:apply-templates mode="book.titlepage.recto.mode" select="bookinfo/abstract" /> <!-- The abstract -->

 </xsl:template>

<!-- - - - - - - - - - - - - - - - - - - - --> 
<!--  Customizing the TOC line appearance  -->
<!-- - - - - - - - - - - - - - - - - - - - --> 
 <xsl:template name="toc.line">
  <xsl:param name="toc-context" select="." />
  <xsl:param name="depth" select="1" />
  <xsl:param name="depth.from.context" select="8" />

  <xsl:variable name="localname" select="local-name(.)" />

  <xsl:variable name="thelink">
   <span>
    <xsl:attribute name="class">
     <xsl:copy-of select="$localname" />
    </xsl:attribute>
    <a>
     <xsl:attribute name="href">
      <xsl:call-template name="href.target">
       <xsl:with-param name="context" select="$toc-context" />
      </xsl:call-template>
     </xsl:attribute>

     <xsl:variable name="label">
      <xsl:apply-templates select="." mode="label.markup" />
     </xsl:variable>

     <xsl:if test="$localname='chapter' or $localname='part' or $localname='appendix'">
       <xsl:call-template name="gentext">
          <xsl:with-param name="key" select="$localname" />
       </xsl:call-template>
       <xsl:call-template name="gentext.space" />
     </xsl:if>

     <xsl:if test="$label != ''">
      <xsl:copy-of select="$label" />
      <xsl:value-of select="$autotoc.label.separator" />
     </xsl:if>

     <xsl:apply-templates select="." mode="titleabbrev.markup" />
    </a>
   </span>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$localname = 'part'">
    <h2>
     <xsl:copy-of select="$thelink" />
    </h2>
   </xsl:when>
   <xsl:otherwise>
    <xsl:copy-of select="$thelink" />
   </xsl:otherwise>
  </xsl:choose>

</xsl:template>


<!-- - - - - - - - - - - - - -  - - - - - - - - - - - - - - - - - -->
<!--         Inserting qandasets and qandadivs in TOC             -->
<!-- - - - - -  - - - - - - - - - - - - - - - - - - - - - - - - - -->

<!-- I've only changed the nodes to contain qandaset -->
<xsl:template match="preface|chapter|appendix|article" mode="toc">
  <xsl:param name="toc-context" select="."/>
  <xsl:variable name="nodes" select="section|sect1
                                         |simplesect[$simplesect.in.toc != 0]
                                         |refentry|qandaset/qandadiv
                                         |glossary|bibliography|index
                                         |bridgehead[$bridgehead.in.toc != 0]"/>
  <xsl:call-template name="subtoc">
    <xsl:with-param name="toc-context" select="$toc-context"/>
    <xsl:with-param name="nodes" select="$nodes"/>
  </xsl:call-template>
</xsl:template>

<!-- A div copies its own name, and skips the qandaentry to go on to the question. -->
<xsl:template match="qandadiv" mode="toc">
  <xsl:call-template name="subtoc">
    <xsl:with-param name="toc-context" select="."/>
    <xsl:with-param name="nodes" select="qandadiv|qandaentry/question"/>
  </xsl:call-template>
</xsl:template> 

<xsl:template match="question" mode="titleabbrev.markup"><xsl:copy-of select="."/></xsl:template> 

<!-- Copy of the "figure|table|example|equation|procedure" mode="toc" template in autotoc.xsl> -->
<xsl:template match="question" mode="toc">
  <xsl:param name="toc-context" select="."/>
  <xsl:element name="{$toc.listitem.type}">
    <xsl:variable name="label">
      <xsl:apply-templates select="." mode="label.markup"/>
    </xsl:variable>
    <xsl:copy-of select="$label"/>
    <xsl:if test="$label != ''">
      <xsl:value-of select="$autotoc.label.separator"/>
    </xsl:if>
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="href.target"/>
      </xsl:attribute>
      <xsl:apply-templates select="." mode="titleabbrev.markup"/>
    </a>
  </xsl:element>
</xsl:template>


<!-- - - - - - - - - - - - - -  - - - - - - - - - - - - - - - - - -->
<!--  Treat differently xrefs if @role=javaFileSrc or xmlFileSrc  -->
<!-- - - - - -  - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="xref" name="xref">
  <xsl:choose>
    <xsl:when test="@role='javaFileSrc'">
      <xsl:variable name="javaRef" >
       <xsl:value-of select="concat(translate(substring-before(@linkend,'.java'),'.','/'),'.java')" />
      </xsl:variable>
      <a href="{$html.java.files}{$javaRef}{$html.java.suffix}"><xsl:value-of select="$javaRef"/></a>
    </xsl:when>
    <xsl:when test="@role='xmlFileSrc'">
      <xsl:variable name="xmlRef" >
        <xsl:value-of select="concat(translate(substring-before(@linkend,'.xml'),'.','/'),'.xml')" />
      </xsl:variable>
      <a href="{$html.xml.files}{$xmlRef}"><xsl:value-of select="$xmlRef"/></a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="normal.xref"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template> 
 
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--  Copied from xref.xsl.  The possible role treatment is done above  -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="normal.xref">
  <xsl:variable name="targets" select="key('id',@linkend)"/>
  <xsl:variable name="target" select="$targets[1]"/>
  <xsl:variable name="refelem" select="local-name($target)"/>

  <xsl:call-template name="check.id.unique">
    <xsl:with-param name="linkend" select="@linkend"/>
  </xsl:call-template>

  <xsl:call-template name="anchor"/>

  <xsl:choose>
    <xsl:when test="count($target) = 0">
      <xsl:message>
        <xsl:text>XRef to nonexistent id: </xsl:text>
        <xsl:value-of select="@linkend"/>
      </xsl:message>
      <xsl:text>???</xsl:text>
    </xsl:when>

    <xsl:when test="@endterm">
      <xsl:variable name="href">
        <xsl:call-template name="href.target">
          <xsl:with-param name="object" select="$target"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="etargets" select="key('id',@endterm)"/>
      <xsl:variable name="etarget" select="$etargets[1]"/>
      <xsl:choose>
        <xsl:when test="count($etarget) = 0">
          <xsl:message>
            <xsl:value-of select="count($etargets)"/>
            <xsl:text>Endterm points to nonexistent ID: </xsl:text>
            <xsl:value-of select="@endterm"/>
          </xsl:message>
          <a href="{$href}">
            <xsl:text>???</xsl:text>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <a href="{$href}">
            <xsl:apply-templates select="$etarget" mode="endterm"/>
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>

    <xsl:when test="$target/@xreflabel">
      <a>
        <xsl:attribute name="href">
          <xsl:call-template name="href.target">
            <xsl:with-param name="object" select="$target"/>
          </xsl:call-template>
        </xsl:attribute>
        <xsl:call-template name="xref.xreflabel">
          <xsl:with-param name="target" select="$target"/>
        </xsl:call-template>
      </a>
    </xsl:when>

    <xsl:otherwise>
      <xsl:variable name="href">
        <xsl:call-template name="href.target">
          <xsl:with-param name="object" select="$target"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:if test="not(parent::citation)">
        <xsl:apply-templates select="$target" mode="xref-to-prefix"/>
      </xsl:if>

      <a href="{$href}">
        <xsl:if test="$target/title or $target/*/title">
          <xsl:attribute name="title">
            <xsl:apply-templates select="$target" mode="xref-title"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="$target" mode="xref-to">
          <xsl:with-param name="referrer" select="."/>
          <xsl:with-param name="xrefstyle">
            <xsl:choose>
              <xsl:when test="@role and not(@xrefstyle) and $use.role.as.xrefstyle != 0">
                <xsl:value-of select="@role"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@xrefstyle"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:apply-templates>
      </a>

      <xsl:if test="not(parent::citation)">
        <xsl:apply-templates select="$target" mode="xref-to-suffix"/>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
	
	<xsl:template match="programlisting//text()">
		<xsl:variable name="expandedText">
			<xsl:call-template name="expandTabs">
				<xsl:with-param name="text" select="." />
			</xsl:call-template>
		</xsl:variable>

		<xsl:value-of select="$expandedText" />
	</xsl:template>

</xsl:stylesheet>
