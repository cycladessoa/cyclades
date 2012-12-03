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
package org.cyclades.engine;

public class Definitions {

    // Time Specific
    public static final long SECOND     = 1000;
    public static final long MINUTE     = 60 * SECOND;
    public static final long HOUR       = 60 * MINUTE;

    // Error response from the engine (as opposed to a Nyxlet value, this will also be accompanied by a 500 response status)
    public final static short CYCLADES_ENGINE_ERROR_RESPONSE    = -1;

    // Configuration parameters
    public static final String MINIMIZE_MEMORY_FOOTPRINT_FLAG   = "minimizeMemoryFootprint";
    public static final String NYXLET_DIRECTORIES               = "nyxletDirectories";
    public static final String DEBUG_MODE                       = "debugMode";
    public static final String ENABLE_ENGINE_RELOAD             = "enableEngineReload";
    public static final String INITIALIZATION_DELEGATES         = "initializationDelegates";
    public static final String AUTH_DELEGATE                    = "authDelegate";
    public static final String CLASS_LOADER_STRATEGY            = "classLoaderStrategy";
    public static final String ISOLATED_CLASS_LOADER            = "isolated";
    public static final String COLLECTIVE_CLASS_LOADER          = "collective";
    public static final String SERVICE_REGISTRY                 = "nyxletRegistry";
}
