if [ $# -ne 2 ]
then
echo "Usage: $0 <file> <newNumber>" 
exit
fi

sed "s|.*\(TAILLE DU MAILLAGE\)|$2 $2 $2     \1|" $1
