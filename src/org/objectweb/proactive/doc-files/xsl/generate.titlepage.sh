#!/bin/bash
ORIGINAL=/usr/local/xxe-std-30p1/addon/config/docbook/xsl
xsltproc      \
   -output  html.titlepage.xsl  \
    $ORIGINAL/template/titlepage.xsl \
    html.titlepage.templates.xml
xsltproc      \
   -output  pdf.titlepage.xsl  \
    $ORIGINAL/template/titlepage.xsl \
    pdf.titlepage.templates.xml
