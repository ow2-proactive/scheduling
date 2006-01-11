#!/bin/bash
# Script to create proactive docs, the ant task is soon to appear!

# shell values, in the ant script those are properties 
#<property name="docs.dir" value="${base}/docs"/>
base=..
docs_dir=$base/docs
html_dir=$docs_dir/html
pdf_dir=$docs_dir/pdf
xml_src_dir=$base/src/org/objectweb/proactive/doc-files

rm -rf $html_dir/* $pdf_dir/*
mkdir -p $html_dir
mkdir -p $pdf_dir

# This copy part is done very easily with ant, even with file selection
echo "    COPYING IMAGE FILES "
for f in components/ development/ eclipse_files/ guided_tour/ images/ osgi_files/ p2p_files/ mpi_files/ PA_config.xsl ProActive.css ProActiveRefBook.pdf security_images/ test_documentation/ webservices/ ; do
    cp -r $xml_src_dir/$f $html_dir/
done

echo "    GENERATING html pages in $html_dir"
xmlto xhtml -o $html_dir -m $xml_src_dir/PA_config.xsl $xml_src_dir/PA_index.xml

#echo "    GENERATING pdf in $pdf_dir"
#xmlto pdf -o $pdf_dir -m $xml_src_dir/PA_config.xsl $xml_src_dir/PA_index.xml   --searchpath $(pwd)/$base/src/org/objectweb/proactive/
