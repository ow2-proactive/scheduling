#!/bin/bash
xsltproc      \
   -output  html.titlepage.xsl  \
    titlepage.templates.xsl \
    html.titlepage.templates.xml
xsltproc      \
   -output  pdf.titlepage.xsl  \
    titlepage.templates.xsl \
    pdf.titlepage.templates.xml
