<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www-sop.inria.fr/oasis/ProActive/schemas" xmlns:p="http://www-sop.inria.fr/oasis/ProActive/schemas"
    xmlns:my="http://www-sop.inria.fr/oasis/ProActive/schemas"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0">
    <xsl:param name="nameList"/>


    <xsl:param name="valueList"/>

    <xsl:function name="my:replaceAll">
        <xsl:param name="value"/>
        <xsl:param name="index"/>
        <xsl:param name="accumulated"/>
        <xsl:choose>
            <xsl:when test="$index > count($accumulated)">
                <xsl:value-of select="$value"/>
            </xsl:when>
            <xsl:when test="matches($accumulated[$index], '.*\$\{[A-Za-z_0-9]+\}.*')">
                <!-- <xsl:value-of select='QName($nameList[$index],"RecursiveDef")'></xsl:value-of>-->
                <xsl:value-of
                    select="error(QName($nameList[$index],'RecursiveDef'),concat('The variable definition is recursive : ''',$nameList[$index],''' with value ''',$accumulated[$index],''''))"/>

            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of
                    select="my:replaceAll(replace($value, concat('\$\{',$nameList[$index],'\}'), $accumulated[$index]), $index + 1, $accumulated)"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="node()" priority="2">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*|text()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()" priority="1">
        <xsl:value-of select="my:replaceAll(.,1,$valueList)"/>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{name(.)}" select="my:replaceAll(.,1,$valueList)"/>
    </xsl:template>
</xsl:stylesheet>
