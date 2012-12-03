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
package org.cyclades.engine.nyxlet;

import java.util.HashMap;
import java.util.Map;
import org.cyclades.collectiveutils.list.BinarySortedListFacade;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;

public class RESTfulDispatchServiceDelegateStrategy {
    public RESTfulDispatchServiceDelegateStrategy () {}

    public void addServiceModule (Nyxlet serviceModule)  throws Exception {
        RESTfulDispatchServiceModuleWrapper serviceModuleWrapper = new RESTfulDispatchServiceModuleWrapper(serviceModule);
        BinarySortedListFacade<RESTfulDispatchServiceModuleWrapper> list = serviceModuleCategoryMap.get(serviceModuleWrapper.getRRDCategory());
        if (list == null) list = new BinarySortedListFacade<RESTfulDispatchServiceModuleWrapper>();
        list.add(serviceModuleWrapper);
        serviceModuleCategoryMap.put(serviceModuleWrapper.getRRDCategory(), list);
    }

    public Nyxlet getServiceModuleMatch (String RRDCategory, NyxletSession sessionDelegate)  throws Exception {
        final String eLabel = "RESTfulDispatchServiceModuleStrategy.getServiceModuleMatch: ";
        try {
            BinarySortedListFacade<RESTfulDispatchServiceModuleWrapper> list = serviceModuleCategoryMap.get(RRDCategory);
            if (list == null) throw new Exception("No match for RRD Category: " + RRDCategory);
            for (RESTfulDispatchServiceModuleWrapper wrapper : list) {
                if (wrapper.getServiceModule().isRRDMatch(sessionDelegate)) {
                    return wrapper.getServiceModule();
                }
            }
            return null;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    } // end of getServiceModuleMatch(...)

    public void clear () {
        serviceModuleCategoryMap.clear();
    }

    private Map<String, BinarySortedListFacade<RESTfulDispatchServiceModuleWrapper>> serviceModuleCategoryMap = new HashMap<String, BinarySortedListFacade<RESTfulDispatchServiceModuleWrapper>>();
}

class RESTfulDispatchServiceModuleWrapper implements Comparable<RESTfulDispatchServiceModuleWrapper> {
    public RESTfulDispatchServiceModuleWrapper (Nyxlet serviceModule)  throws Exception {
        this.serviceModule = serviceModule;
        String[] fields = serviceModule.getRRDString().split("[|]");
        if (fields.length != 3) throw new Exception("Invalid RRD format, should be \"[group]|[uri_part_mapping]|[priority]\"");
        RRDCategory =  fields[0].trim();
        priority = Integer.parseInt(fields[2]);
    }

    @Override
        public int compareTo(RESTfulDispatchServiceModuleWrapper o) {
        if (getPriority() > ((RESTfulDispatchServiceModuleWrapper)o).getPriority()) {
            return 1;
        } else if (getPriority() < ((RESTfulDispatchServiceModuleWrapper)o).getPriority()) {
            return -1;
        } else {
            return 0;
        }
    }

    public Nyxlet getServiceModule () {
        return serviceModule;
    }
    public String getRRDCategory () {
        return RRDCategory;
    }
    public int getPriority () {
        return priority;
    }

    private Nyxlet serviceModule;
    private String RRDCategory;
    private int priority = 100;
}
