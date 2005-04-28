#!/bin/bash

# Nombre de machines pour un registry
NB_HOSTS_BY_REGISTRY=8

# Minimum de RAM pour rester
MIN_RAM=512

windows()
{
(
    source /usr/local/bashutil/autoload_lib
    autoload_lib /usr/local/bashutil/lib/batch
    comm -12 <(host_expand +@pc-$1 -@pc-laptop -@m-laptop|sort) \
             <(ypmatch $1 machines|tr ' ' '\012'|sort)
)
}

function min_ram()
{
    export MIN_RAM
    awk '
BEGIN {
    min_ram = ENVIRON["MIN_RAM"]
}

{
    ram = $5
    if (ram >= min_ram)
	print
}'
}

power()
{
    echo "= Linux hosts"
    ~fm/src/grid/oasisgrid | min_ram > power

    echo "= Windows hosts"
    for P in $(ls proj); do
        [ $P = "semir" ] && continue
	echo "== Windows hosts in $P"
	windows $P | awk '{printf "%s 0\n", $0}' >> power
    done
}

[ -f power ] || power

awk '{print $1}' < power > hosts
awk '{printf "%d %s\n", $2 + $2 * ($3 - 1.0) / 2.0, $1 }' < power | sort -nr > order

rm proj/*/hosts

echo "= Hosts affectation"
for M in $(cat hosts); do
    P=$(ypmatch $M projets)
    echo $M >> proj/$P/hosts || (echo "Machine: $M Projet: $P inconnu"; false) || exit 1
    echo -n .
done

echo

function include_only()
{
    (cd proj/$1
     shift
     PATTERN=$(echo "^($*)$" | tr ' ' '|')
     grep -E "$PATTERN" < hosts > tmp
     mv tmp hosts)
}

function exclude()
{
    (cd proj/$1
     shift
     PATTERN=$(echo "^($*)$" | tr ' ' '|')
     grep -vE "$PATTERN" < hosts > tmp
     mv tmp hosts)
}

exclude maestro nice azur polya
exclude odyssee taquilee uros
include_only ariana rebel
exclude epidaure jekyll willis klebs
include_only orion libra ara

echo "= Configuration for each project"
for P in proj/*; do (
    cd $P || exit 1
    rm order
    echo -n .
    for M in $(cat hosts); do
	grep -E " $M$" ../../order
    done | sort -nr > order

    NB_REGISTRY=$(($(wc -l < hosts) / $NB_HOSTS_BY_REGISTRY))
    [ $NB_REGISTRY = 0 ] && NB_REGISTRY=1
    head -n $NB_REGISTRY order > registry

    NB_NOT_REGISTRY=$(($(wc -l < hosts) - $NB_REGISTRY))
    tail -n $NB_NOT_REGISTRY order > not_registry

    # not_registry.xml
    (cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configFile>
	<p2pconfig>
EOF

    for M in $(cut -d' ' -f2 < not_registry); do
	echo "		<host name=\"$M.inria.fr\"/>"
    done

    echo "		<configForHost>"
    cat PERIODS_NOT_REGISTRY

    echo "			<register>"
    for M in $(cut -d' ' -f2 < registry); do
	echo "				<registry url=\"$M.inria.fr\"/>"
    done
    
    cat <<EOF
			</register>
		</configForHost>
	</p2pconfig>
</configFile>
EOF
) > not_registry.xml

    # registry.xml
    (cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configFile>
	<p2pconfig>
EOF

    for M in $(cut -d' ' -f2 < registry); do
	echo "		<host name=\"$M.inria.fr\"/>"
    done

    echo "		<configForHost>"
    cat PERIODS_NOT_REGISTRY

    cat <<EOF
		</configForHost>
	</p2pconfig>
</configFile>
EOF
) > registry.xml

    # 2424.xml
    rm 2424.xml 2>/dev/null
    [ -f 2424 ] || continue
    (cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configFile>
	<p2pconfig>
EOF

    for M in $(cat 2424); do
	echo "		<host name=\"$M.inria.fr\"/>"
    done

    cat <<EOF
		<configForHost>
			<periods>
				<period>
					<start day="monday" hour="0" minute="0"/>
					<end day="sunday" hour="23" minute="59"/>
				</period>
			</periods>
			<register>
EOF

    for M in $(cut -d' ' -f2 < registry); do
	echo "				<registry url=\"$M.inria.fr\"/>"
    done

cat <<EOF
			</register>
		</configForHost>
	</p2pconfig>
</configFile>
EOF
) > 2424.xml
); done

echo

echo "= Building the cache"

cat proj/*/{2424.xml,not_registry.xml,registry.xml} | awk '
BEGIN {
    print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    print "<configFile>"
}

/<p2pconfig>/ {
    p2pconfig = 1;
}

/<\/p2pconfig>/ {
    print;
    p2pconfig = 0;
}

{
    if (p2pconfig)
	print;
}

END {
    print "</configFile>"
}
' > proactivep2p.xml

echo "= Finding the windows boxes"
cat proj/*/order | awk '/^0 / { print $2 }' > windows

echo "= Done"
