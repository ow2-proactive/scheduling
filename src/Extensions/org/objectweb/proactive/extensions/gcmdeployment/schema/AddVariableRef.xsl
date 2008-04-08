<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="1.0">
    <xsl:template match="/">
        <xsl:apply-templates select="*" />
    </xsl:template>
    <xsl:template match="xsd:simpleType[contains(@name,'OrVariable')]"
        priority="3">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*|text()">
                <xsl:with-param name="add" select="true()"></xsl:with-param>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="node()" priority="2">
        <xsl:param name="add" />
        <xsl:copy>
            <xsl:choose>
                <xsl:when test="$add">
                    <xsl:apply-templates select="node()|@*|text()">
                        <xsl:with-param name="add"
                            select="true()" />
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()|@*|text()" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@*" priority="2">
        <xsl:attribute name="{name(.)}"><xsl:value-of
            select="." /></xsl:attribute>
    </xsl:template>
    <xsl:template match="@memberTypes" priority="3">
        <xsl:param name="add" />
        <xsl:choose>
            <xsl:when test="$add">
                <xsl:attribute name="{name(.)}"><xsl:value-of
                    select="concat(.,' variableRefType')" /></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="{name(.)}"><xsl:value-of
                    select="." /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="text()" priority="1">
        <xsl:value-of select="." />
    </xsl:template>
</xsl:stylesheet>