#******************************************************************************
# Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
#    Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#    Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
#    may be used to endorse or promote products derived from this software without
#    specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
# OF THE POSSIBILITY OF SUCH DAMAGE.
#*******************************************************************************
#!/bin/sh

if [ $# -lt 2 ]; then 
	echo "\nusage: cmd output_dir base_dir_name\n"
	exit 1
fi

set -e

ROOT_GENERATION_DIR=$2
OUTPUT_DIRECTORY=$1/$ROOT_GENERATION_DIR


case $1 in
    /*) absolute=1 ;;
    *) absolute=0 ;;
esac

if [ $absolute -eq 1 ]; then
    OUTPUT_DIRECTORY=$1/$ROOT_GENERATION_DIR
else
    OUTPUT_DIRECTORY=`pwd`/$1/$ROOT_GENERATION_DIR
fi

echo "Starting document generation....output directory[$OUTPUT_DIRECTORY]"

cd $(dirname $0)
pushd `pwd`

rm -rf $OUTPUT_DIRECTORY
mkdir -p $OUTPUT_DIRECTORY/

cp -r ./docs $OUTPUT_DIRECTORY/.

cd $OUTPUT_DIRECTORY

echo "Running asciidoc on all files with the suffix .asciidoc"
find . -type f -name '*.asciidoc' -exec asciidoc {} \;

echo "Generating HTML link to canonical document entry point..."
data='<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">'
data=$data'<html>'
data=$data'<head>'
data=$data'<title>Redirection Page</title>'
data=$data'<meta http-equiv="REFRESH" content="0;url=docs/index.html"></HEAD>'
data=$data'<BODY>'
data=$data'You should be redirected to "docs/index.html"'
data=$data'</BODY>'
data=$data'</HTML>'
echo $data > index.html

#################################
#   Javdoc Section (optional)   #
#################################

### General java docs ###
JAVADOC_DIR="$OUTPUT_DIRECTORY/docs/javadoc"
mkdir -p $JAVADOC_DIR
popd
gradle javadoc
cp -r ./build/docs/javadoc/* $JAVADOC_DIR/.
#################################

#################################
#     End Javdoc Section        #
#################################

echo "Zipping the documentation package for distribution..."
cd $OUTPUT_DIRECTORY/..
zip -rq $ROOT_GENERATION_DIR.zip $ROOT_GENERATION_DIR

echo "Finished documentation package generation"
echo "Output documentation directory:[$OUTPUT_DIRECTORY]"
echo "Documentation entry point:[$OUTPUT_DIRECTORY/index.html]"
echo "Output zip file:[$OUTPUT_DIRECTORY.zip]"
