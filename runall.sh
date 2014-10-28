#!/bin/bash -e

OLDDIR=$(pwd)
DATADIR=$1

if [ "$DATADIR" = "" ]; then
    if [ ! -e /tmp/sample_ccdas-master ]; then
        cd /tmp/
        wget -q --no-check-certificate https://github.com/chb/sample_ccdas/archive/master.tar.gz -O master.tar.gz
        tar xzf master.tar.gz
    fi
    DATADIR=/tmp/sample_ccdas-master
fi

cd ${OLDDIR}

if [ -e out ]; then
    rm -rf out
fi
mkdir -p out/results

cd src

ls -1tr | grep -v ^\\. |\
   grep -v "CcdAnalysis.groovy" | \
   while read -r i; do groovy "$i" ${DATADIR} > ../out/results/${i}.out ; done
