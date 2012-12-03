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

if [ $# -lt 4 ]; then
	echo ""
	echo "usage:			cmd base_engine_url password backup_file_suffix nyxlet_name_no_suffix"
	echo ""
	echo "base_engine_url:	URL to engine, i.e. \"http://localhost:8080/cyclades\""
	echo "password:		The password to use, this will be ignored if none has been set on the target service"
	echo "backup_file_suffix:	Suffix to use for rolling back if necessary (for backup file), remember this value in case you need to roll back!"
	echo "nyxlet_name_no_suffix:	The name of the nyxlet...no suffix as this will be used for the properties file also"
	echo ""
	exit 1
fi

# 1 - Host
PARAM_BASE_ENGINE_URL=$1;
# 2 - Password
PARAM_PASSWORD=$2
# 3 - Any identifier for backup zip file
PARAM_ID=$3
# 4 - Nyxlet name (no suffix)
PARAM_NAME=$4


# Back up current Nyxlet directory
RESP=`curl -s "$PARAM_BASE_ENGINE_URL/admin/WEB-INF/nyxlets.$PARAM_ID?action=zipdirectory&data-type=xml&source=WEB-INF/nyxlets&password=$PARAM_PASSWORD"`
VALID=`echo  "$RESP" | grep "status-code=\"200\""`
if [ -n "$VALID" ]; then
        echo "Nyxlet directory backup succeeded"
else
        echo "Nyxlet directory backup failed: $RESP"
	exit 2
fi

# Delete Nyxlet
RESP=`curl -s "$PARAM_BASE_ENGINE_URL/admin/WEB-INF/nyxlets/$PARAM_NAME.nyxlet?action=delete&data-type=xml&password=$PARAM_PASSWORD"`
VALID=`echo  "$RESP" | grep "status-code=\"200\""`
if [ -n "$VALID" ]; then
        echo "Nyxlet delete succeeded"
else
        echo "Nyxlet delete failed: $RESP"
        exit 3
fi

# Delete Nyxlet property file
RESP=`curl -s "$PARAM_BASE_ENGINE_URL/admin/WEB-INF/nyxlets/$PARAM_NAME.properties?action=delete&data-type=xml&password=$PARAM_PASSWORD"`
VALID=`echo  "$RESP" | grep "status-code=\"200\""`
if [ -n "$VALID" ]; then
       	echo "Nyxlet properties delete succeeded"
else
       	echo "Nyxlet properties delete failed: $RESP"
fi

# Reload the service
RESP=`curl -s "$PARAM_BASE_ENGINE_URL?action=reload"`
VALID=`echo  "$RESP" | grep "Number of Nyxlets loaded"`
if [ -n "$VALID" ]; then
        echo "Nyxlet reload succeeded"
else
        echo "Nyxlet reload failed: $RESP"
        exit 4
fi
