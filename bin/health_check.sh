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
	echo "This is the Health Check Monitor Script (HCMS). It is intended to be run periodically, "
	echo "for instance as a cron job.";
	echo ""
	echo "usage: cmd url fail_condition_pattern temp_output_file id [mailing_list_file(file contains comma-seperated email list)]"
	echo ""
	echo "url:				The URL whose response we will be searching"
	echo "fail_condition_pattern:		The pattern to look for that, if found, will flag an error condition"
	echo "temp_output_file:		The URI to the file that the response will be written to (if failure is detected)"
	echo "id:				An identifier to include in the email subject lines"
	echo "mailing_list_file(optional):	a file containing one line comprised of a comma seperated list of email addresses to send notifications"
	echo ""
	echo "NOTE: Notifications will be sent in the following scenarios:"
	echo "    - A failure state is detected following a non failure state"
	echo "    - A different failure state is detected following a failure state (a delta in the health output between two consecutive failure states)"
	echo "    - A non failure state is detected following a failure state"
	echo ""
	echo "NOTE: The email subject will be in the following format:"
	echo "    [HCMS][host name][id parameter][FAIL|SUCCESS]"
	echo "    The body of the email will be the health check response itself"
	echo ""
	echo "Example of using the health_check.sh script to trigger a  health diagnosis of the Cyclades Engine and all Nyxlets every 5 minutes:"
	echo "*/5 * * * * /.../cyclades/bin/health_check.sh http://localhost:8080/cyclades?action=healthcheck false /tmp/cron_out 12345 /opt/my_email_list >/dev/null 2>&1"
	echo ""
	exit 1
fi

TEMP_OUT_FILE=$3
TRANSACTION_ID=$4
EMAIL_SUBJECT_FAILURE="[HCMS][$(hostname)][$TRANSACTION_ID][FAIL]"
EMAIL_SUBJECT_RECOVERY="[HCMS][$(hostname)][$TRANSACTION_ID][SUCCESS]"

if [ $# -gt 4 ]; then
        EMAIL_LIST=`cat $5`
fi

HEALTH_RESPONSE=`curl -D $TEMP_OUT_FILE.header  $1`
CURL_EXIT_CODE=$?

if [ $CURL_EXIT_CODE -gt 0  ]; then
	HEALTH_RESPONSE_ERROR="URL request failed:[$1] curl exit code:[$CURL_EXIT_CODE]"
	HEALTH_RESPONSE=$HEALTH_RESPONSE_ERROR
else 
	HTTP_RESPONSE=`cat  "$TEMP_OUT_FILE.header" | grep "200 OK"`
	if [ -n "$HTTP_RESPONSE" ]; then
		HAS_TAG=`echo $HEALTH_RESPONSE | grep $2`
	else
		HEALTH_RESPONSE_ERROR="URL request failed:[$1]: Non 200 HTTP status code"
        	#HEALTH_RESPONSE=$HEALTH_RESPONSE_ERROR
	fi
fi

if [ -n "$HAS_TAG" -o -n "$HEALTH_RESPONSE_ERROR" ]; then
	echo "Fail condition has been detected"
	# Send a notification if this condition has not been processed before. If it has,
	# send a notification if the output has changed, otherwise, send no notificaction so
	# we dont flood folks with emails.
	if [ -s $TEMP_OUT_FILE ]; then
		echo $HEALTH_RESPONSE > $TEMP_OUT_FILE.new
		DIFF_RESPONSE=`diff $TEMP_OUT_FILE $TEMP_OUT_FILE.new`
		if [ -n "$DIFF_RESPONSE" ]; then
			echo "Delta detected in error condition, send email"
			if [ -n "$EMAIL_LIST" ]; then
				echo $HEALTH_RESPONSE | mail -s "$EMAIL_SUBJECT_FAILURE" $EMAIL_LIST
			fi
		else
			echo "Same failure condition still present, no output deltas detected...do not send another notification"
		fi
	else
		echo "Sending initial failure detection email"
		if [ -n "$EMAIL_LIST" ]; then
                	echo $HEALTH_RESPONSE | mail -s "$EMAIL_SUBJECT_FAILURE" $EMAIL_LIST
                fi
	fi
	echo $HEALTH_RESPONSE > $TEMP_OUT_FILE
else 
	echo "Failure condition has not been detected"
	# Send a notification if the failure condition does not exist but the output file exists. This will
	# send notification on the recovery condition.
	if [ -s $TEMP_OUT_FILE ]; then
		rm $TEMP_OUT_FILE $TEMP_OUT_FILE.new
		echo "Sending recovery email" 
		if [ -n "$EMAIL_LIST" ]; then
                	echo $HEALTH_RESPONSE | mail -s "$EMAIL_SUBJECT_RECOVERY" $EMAIL_LIST
                fi
        fi
fi
