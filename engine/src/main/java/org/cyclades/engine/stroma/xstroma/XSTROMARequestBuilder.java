package org.cyclades.engine.stroma.xstroma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;

public class XSTROMARequestBuilder {
    
    public static XSTROMARequestBuilder newInstance (String serviceBrokerName) {
        return new XSTROMARequestBuilder(serviceBrokerName);
    }
    
    public XSTROMARequestBuilder (String serviceBrokerName) {
        this.serviceBrokerName = serviceBrokerName;
    }
    
    public XSTROMARequestBuilder parameter (String key, String value) {
        List<String> values = parameters.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            parameters.put(key, values);
        }
        values.add(value);
        return this;
    }
    
    public XSTROMARequestBuilder parameters (Map<String, List<String>> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    public XSTROMARequestBuilder json () {
        metaTypeEnum = MetaTypeEnum.JSON;
        return this;
    }
    
    public XSTROMARequestBuilder xml () {
        metaTypeEnum = MetaTypeEnum.XML;
        return this;
    }
    
    public XSTROMARequestBuilder add (STROMARequestBuilder serviceRequestBuilder) {
        this.serviceRequestBuilders.add(serviceRequestBuilder);
        return this;
    }
    
    public XSTROMABrokerRequest build () throws Exception {
        XSTROMABrokerRequest brokerRequest = new XSTROMABrokerRequest(serviceBrokerName, metaTypeEnum, parameters);
        for (STROMARequestBuilder stromaBuilder : serviceRequestBuilders) brokerRequest.addSTROMARequest(stromaBuilder.build()); 
        return brokerRequest;
    }
    
    private final String serviceBrokerName;
    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private MetaTypeEnum metaTypeEnum = MetaTypeEnum.JSON;
    private List<STROMARequestBuilder> serviceRequestBuilders = new ArrayList<STROMARequestBuilder>();

}
