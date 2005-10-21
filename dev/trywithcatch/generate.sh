#!/bin/sh

FILES="Yylex.java parser.java sym.java"

java -cp JFlex.jar JFlex.Main java.lex
java -jar java-cup-v11a.jar java.cup
rm Yylex.java~
for FILE in $FILES; do
    (echo "package trywithcatch;"
    sed 's/java_cup/trywithcatch.java_cup/g') < "$FILE" > \
        "../../src/trywithcatch/$FILE"
done
