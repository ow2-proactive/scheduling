<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>
    <xsl:template match="/xs:schema/xs:import[@namespace = 'http://www.w3.org/2001/XMLSchema-instance']">
        <!-- do not copy it -->
    </xsl:template>
    <xsl:template match="xs:attribute[@ref='xsi:schemaLocation']">
        <!-- do not copy it -->
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>