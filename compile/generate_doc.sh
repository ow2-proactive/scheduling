#!/bin/bash
# Script to create proactive docs, the ant task is soon to appear!

# shell values, in the ant script those are properties 
#<property name="docs.dir" value="${base}/docs"/>
base=..
docs_dir=$base/docs
html_dir=$docs_dir/html
pdf_dir=$docs_dir/pdf
howto_dir=$docs_dir/howto
xml_src_dir=$base/src/org/objectweb/proactive/doc-files
xsl_dir=$base/src/org/objectweb/proactive/doc-files/xsl/

# first regenerate the titlepage stuff
cd $xsl_dir
./generate.titlepage.sh
cd - > /dev/null

rm -rf $html_dir/* $pdf_dir/* $howto_dir/*
mkdir -p $html_dir $pdf_dir $howto_dir

# This copy part is done very easily with ant, even with file selection
# This copy part is needed for the html referencing, which is not done automatically
 echo "    COPYING IMAGE FILES "
 for f in components/ development/ eclipse_files/ guided_tour/ images/ osgi_files/ p2p_files/  ProActive.css ProActiveRefBook.pdf security_images/ test_documentation/ webservices/ ; do
    cp -r $xml_src_dir/$f $html_dir/
 done
 
echo "    GENERATING html pages in $html_dir"
xmlto xhtml -o $html_dir -m $xsl_dir/html.xsl $xml_src_dir/PA_index.xml

# searchpath indicates where to find the images
#echo "    GENERATING pdf in $pdf_dir"
#xmlto pdf -o $pdf_dir -m $xsl_dir/pdf.xsl $xml_src_dir/PA_index.xml   --searchpath $(pwd)/$base/src/org/objectweb/proactive/
echo "    IF YOU WANT TO GENERATE THE pdf (real slow)), UNCOMMENT ABOVE LINE"

# echo "    GENERATING howto in $howto_dir"
# # just a hack to copy the image needed in the figures section
 mkdir $howto_dir/images  && cp $xml_src_dir/images/e1.png $howto_dir/images/
 xmlto xhtml -o $howto_dir -m $xsl_dir/html.xsl $xml_src_dir/docBookTutorial/doc_howto.xml
 xmlto pdf -o $howto_dir -m $xsl_dir/pdf.xsl $xml_src_dir/docBookTutorial/doc_howto.xml   --searchpath $(pwd)/$base/src/org/objectweb/proactive/
