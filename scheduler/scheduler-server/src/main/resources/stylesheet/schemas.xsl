<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="urn:proactive:jobdescriptor:dev"
                xmlns:dev="urn:proactive:jobdescriptor:dev"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="dev" >

    <xsl:output standalone="no" method="xml" cdata-section-elements="dev:description dev:code dev:visualization"/>
    <xsl:variable name ="nsdev">urn:proactive:jobdescriptor:dev</xsl:variable>

    <xsl:template match="@* | comment() | processing-instruction()">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="*" >
        <xsl:element name="{local-name()}" namespace="{$nsdev}">
            <xsl:apply-templates select="@* | node()" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:element name="{name()}" namespace="{$nsdev}">
            <xsl:copy-of select="namespace::*[name()]"/>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="xsi:schemaLocation">
                <xsl:value-of select="'urn:proactive:jobdescriptor:dev http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/dev/schedulerjob.xsd'"/>
            </xsl:attribute>

            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>