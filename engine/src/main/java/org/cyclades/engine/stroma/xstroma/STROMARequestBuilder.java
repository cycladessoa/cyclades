package org.cyclades.engine.stroma.xstroma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STROMARequestBuilder {
    
    public static STROMARequestBuilder newInstance (String serviceName) {
        return new STROMARequestBuilder(serviceName);
    }
    
    public STROMARequestBuilder (String serviceName) {
        this.serviceName = serviceName;
    }
    
    public STROMARequestBuilder data(String data) {
        this.data = data;
        return this;
    }
    
    public STROMARequestBuilder parameter (String key, String value) {
        List<String> values = parameters.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            parameters.put(key, values);
        }
        values.add(value);
        return this;
    }
    
    public STROMARequestBuilder parameters (Map<String, List<String>> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    public STROMARequest build () throws Exception {
        return new STROMARequest(serviceName, parameters, data);
    }
    
    private final String serviceName;
    private String data = null;
    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();

}
