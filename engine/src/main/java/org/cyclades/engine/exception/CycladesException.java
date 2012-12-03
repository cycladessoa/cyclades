/*******************************************************************************
 * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cyclades.engine.exception;

import org.cyclades.engine.ResponseCodeEnum;

public class CycladesException extends Exception {

    public CycladesException (String message) {
        super(message);
    }

    public CycladesException (String message, Throwable cause) {
        super(message, cause);
    }

    public CycladesException (Throwable cause) {
        super(cause);
    }

    public CycladesException (String message, short code) {
        super(message);
        this.code = code;
    }

    public CycladesException (String message, String friendlyMessage, String dataString) {
        super(message);
        this.friendlyMessage = friendlyMessage;
        this.dataString = (dataString == null) ? "" : dataString;
    }

    public CycladesException (String message, short code, String friendlyMessage, String dataString) {
        super(message);
        this.code = code;
        this.friendlyMessage = friendlyMessage;
        this.dataString = (dataString == null) ? "" : dataString;
    }

    public CycladesException (CycladesException exception) {
        super(exception.getMessage());
        this.code = exception.getCode();
        this.friendlyMessage = exception.getFriendlyMessage();
        this.dataString = exception.getDataString();
    }

    public void setFriendlyMessage (String friendly) {
        this.friendlyMessage = friendly;
    }

    public String getFriendlyMessage () {
        return this.friendlyMessage;
    }

    public void setCode (short code) {
        this.code = code;
    }

    public short getCode () {
        return this.code;
    }

    public void setDataString (String dataString) {
        this.dataString = dataString;
    }

    public String getDataString () {
        return this.dataString;
    }

    // Default in case this is not set
    protected short code = ResponseCodeEnum.GENERAL_ERROR.getCode();

    // We need this null to know if it is available or not
    private String friendlyMessage = null;

    // We will always append this, so keep default as empty string
    private String dataString = "";
}
