#!/bin/bash -e
cd /tmp/
wget -q --no-check-certificate https://github.com/chb/sample_ccdas/archive/master.tar.gz
tar xzvf master

cd src
ls -1tr | grep -v ^\\. |\
   grep -v "CcdAnalysis.groovy" | \
   while read -r i; do groovy "$i" ~/tmp/sample_ccdas-master; done
