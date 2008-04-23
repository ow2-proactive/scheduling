#! /bin/sh

if [ -z "$1" ] ; then
	echo "Please provide a branch name"
	exit 1
fi

REPO_URL="svn+ssh://scm.gforge.inria.fr/svn/proactive"
svn copy \
	${REPO_URL}/trunk \
	${REPO_URL}/branches/$1 \
	-m "Created the $1 branch"	