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
package org.cyclades.pool;

import java.util.Map;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Simple class to enable flexible population of a GenericObjectPool.Config object from meta. This enables setting the fields of a
 * GenericObjectPool.Config by passing in a Map<String, String> representing the settings. The keys can be mapped as necessary, or
 * simply left as is to default to the key names matching the field names of a GenericObjectPool.Config, exactly.
 *
 * Example scenario: One may wish to set the "lifo" value of a GenericObjectPool.Config Object by using a key/value pair of "lifo"/"true",
 * or may want to create a custom mapping for the same functionality like "pool_lifo"/"true".
 *
 * For the most part, the default will work, where no keys need to be re-mapped and the "build (Map<String, String> initializationMap)" can
 * simply be used. Custom mappings would be useful if somehow the default key names conflict with other keys used for other purposes in the
 * meta.
 */
public class GenericObjectPoolConfigBuilder {

    public GenericObjectPool.Config build (Map<String, String> initializationMap) {
        return build(initializationMap, "");
    }

    /**
     * Build a GenericObjectPool.Config Object using the given Map, and appending the given prefix to key access to that Map
     *
     * @param initializationMap Values to populate the GenericObjectPool.Config from
     * @param prefix            Prefix to add to current keys so they match the keys of the Map passed in
     * @return GenericObjectPool.Config
     */
    public GenericObjectPool.Config build (Map<String, String> initializationMap, String prefix) {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        if (initializationMap.containsKey(prefix + lifoKey)) config.lifo = Boolean.parseBoolean(initializationMap.get(prefix + lifoKey));
        if (initializationMap.containsKey(prefix + maxActiveKey)) config.maxActive = Integer.parseInt(initializationMap.get(prefix + maxActiveKey));
        if (initializationMap.containsKey(prefix + maxIdleKey)) config.maxIdle = Integer.parseInt(initializationMap.get(prefix + maxIdleKey));
        if (initializationMap.containsKey(prefix + maxWaitKey)) config.maxWait = Long.parseLong(initializationMap.get(prefix + maxWaitKey));
        if (initializationMap.containsKey(prefix + minEvictableIdleTimeMillisKey)) config.minEvictableIdleTimeMillis = Long.parseLong(initializationMap.get(prefix + minEvictableIdleTimeMillisKey));
        if (initializationMap.containsKey(prefix + minIdleKey)) config.minIdle = Integer.parseInt(initializationMap.get(prefix + minIdleKey));
        if (initializationMap.containsKey(prefix + numTestsPerEvictionRunKey)) config.numTestsPerEvictionRun = Integer.parseInt(initializationMap.get(prefix + numTestsPerEvictionRunKey));
        if (initializationMap.containsKey(prefix + softMinEvictableIdleTimeMillisKey)) config.softMinEvictableIdleTimeMillis = Long.parseLong(initializationMap.get(prefix + softMinEvictableIdleTimeMillisKey));
        if (initializationMap.containsKey(prefix + testOnBorrowKey)) config.testOnBorrow = Boolean.parseBoolean(initializationMap.get(prefix + testOnBorrowKey));
        if (initializationMap.containsKey(prefix + testOnReturnKey)) config.testOnReturn = Boolean.parseBoolean(initializationMap.get(prefix + testOnReturnKey));
        if (initializationMap.containsKey(prefix + testWhileIdleKey)) config.testWhileIdle = Boolean.parseBoolean(initializationMap.get(prefix + testWhileIdleKey));
        if (initializationMap.containsKey(prefix + timeBetweenEvictionRunsMillisKey)) config.timeBetweenEvictionRunsMillis = Long.parseLong(initializationMap.get(prefix + timeBetweenEvictionRunsMillisKey));
        if (initializationMap.containsKey(prefix + whenExhaustedActionKey)) config.whenExhaustedAction = Byte.parseByte(initializationMap.get(prefix + whenExhaustedActionKey));
        return config;
    }


    public GenericObjectPoolConfigBuilder lifoKey (String lifoKey) {
        this.lifoKey = lifoKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder maxActiveKey (String maxActiveKey) {
        this.maxActiveKey = maxActiveKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder maxIdleKey (String maxIdleKey) {
        this.maxIdleKey = maxIdleKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder maxWaitKey (String maxWaitKey) {
        this.maxWaitKey = maxWaitKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder minEvictableIdleTimeMillisKey (
            String minEvictableIdleTimeMillisKey) {
        this.minEvictableIdleTimeMillisKey = minEvictableIdleTimeMillisKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder minIdleKey (String minIdleKey) {
        this.minIdleKey = minIdleKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder numTestsPerEvictionRunKey (String numTestsPerEvictionRunKey) {
        this.numTestsPerEvictionRunKey = numTestsPerEvictionRunKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder softMinEvictableIdleTimeMillisKey (
            String softMinEvictableIdleTimeMillisKey) {
        this.softMinEvictableIdleTimeMillisKey = softMinEvictableIdleTimeMillisKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder testOnBorrowKey (String testOnBorrowKey) {
        this.testOnBorrowKey = testOnBorrowKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder testOnReturnKey (String testOnReturnKey) {
        this.testOnReturnKey = testOnReturnKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder testWhileIdleKey (String testWhileIdleKey) {
        this.testWhileIdleKey = testWhileIdleKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder timeBetweenEvictionRunsMillisKey (
            String timeBetweenEvictionRunsMillisKey) {
        this.timeBetweenEvictionRunsMillisKey = timeBetweenEvictionRunsMillisKey;
        return this;
    }
    public GenericObjectPoolConfigBuilder whenExhaustedActionKey (String whenExhaustedActionKey) {
        this.whenExhaustedActionKey = whenExhaustedActionKey;
        return this;
    }

    private String lifoKey                              = "lifo";
    private String maxActiveKey                         = "maxActive";
    private String maxIdleKey                           = "maxIdle";
    private String maxWaitKey                           = "maxWait";
    private String minEvictableIdleTimeMillisKey        = "minEvictableIdleTimeMillis";
    private String minIdleKey                           = "minIdle";
    private String numTestsPerEvictionRunKey            = "numTestsPerEvictionRun";
    private String softMinEvictableIdleTimeMillisKey    = "softMinEvictableIdleTimeMillis";
    private String testOnBorrowKey                      = "testOnBorrow";
    private String testOnReturnKey                      = "testOnReturn";
    private String testWhileIdleKey                     = "testWhileIdle";
    private String timeBetweenEvictionRunsMillisKey     = "timeBetweenEvictionRunsMillis";
    private String whenExhaustedActionKey               = "whenExhaustedAction";

}
