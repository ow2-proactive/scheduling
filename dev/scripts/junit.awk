# Example of usage :
#
#   ./build junit | awk -f ../dev/scripts/junit.awk
#
# or
#
#   ./build junit | tee junit.log | awk -f ../dev/scripts/junit.awk
#

BEGIN {
    nberr = 0;
    prev = "";
    current = "";
    errors = "";
}

{
  if ( $0 ~ /\[junit\]/ ) {
    prev = current;
    current = $3;
    
    if ( $0 ~ /(Failures|Errors): [1-9]+/ ) {
        nberr++;
	errors = errors "\n  " prev
    }

    sub(/run: [0-9]+/, "\033[32;1m&\033[0m");
    sub(/(Failures|Errors): [1-9]+/, "\033[31;1m&\033[0m");
    sub(/[0-9.]+ sec/, "\033[29;1m&\033[0m");

    print
  }
}

END {
    if ( nberr > 0 ) {
	print "\033[31;1mThere was " nberr " failure" ( nberr>1 ? "s" : "" ) " : " errors "\033[0m"
    }
}
