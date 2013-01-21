package org.cyclades.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.stroma.xstroma.STROMARequestBuilder;
import org.cyclades.engine.stroma.xstroma.XSTROMABrokerRequest;
import org.cyclades.engine.stroma.xstroma.XSTROMARequestBuilder;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.io.StreamUtils;

public class HttpXSTROMARequest {
    
    public static void execute (String url, OutputStream out, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage) throws Exception {
        execute (url, out, 0, 0, xstromaRequest, xstromaMessage);
    }
    
    public static void execute (String url, OutputStream out, int connectionTimeout, int readTimeout, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage) throws Exception {
        StringBuilder requestURL = new StringBuilder(url);
        String xstromaString;
        if (xstromaMessage) {
            requestURL.append("?").append(ServiceBrokerNyxletImpl.XSTROMA_MESSAGE);
            xstromaString = xstromaRequest.toXSTROMAMessage();
        } else {
            createParameterizedXSTOMAUrl(requestURL, xstromaRequest).toString();
            xstromaString = xstromaRequest.generateData();
        }
        InputStream is = null;
        try {
            is = ResourceRequestUtils.getInputStreamHTTP(requestURL.toString(), "POST", new ByteArrayInputStream(xstromaString.getBytes()), headerProperties, connectionTimeout, readTimeout);
            StreamUtils.write(is, out);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }
    
    private static StringBuilder createParameterizedXSTOMAUrl (StringBuilder urlBuilder, XSTROMABrokerRequest xstromaRequest) {
        urlBuilder.append("?data-type=").append(xstromaRequest.getMetaTypeEnum().name());
        Map<String, List<String>> parameters = xstromaRequest.getParameters();
        for (Map.Entry<String, List<String>> parametersEntry : parameters.entrySet()) {
            String key = parametersEntry.getKey();
            List<String> values = parametersEntry.getValue();
            for (String value : values) {
                urlBuilder.append("&").append(key).append("=").append(value);
            }
        }
        return urlBuilder;
    }
    
    public static void main (String[] args) {
        try {
            if (args.length < 4) {
                System.out.println("usage: cmd service_broker_url xml|json use_xstroma_message service_name [parameter_key parameter_value] ...");
            }
            int i = 0;
            String url = args[i++];
            boolean xml = args[i++].equalsIgnoreCase("xml");
            boolean useXSTROMAMessage = args[i++].equalsIgnoreCase("true");
            String serviceName = args[i++];
            XSTROMARequestBuilder xstromaBuilder = XSTROMARequestBuilder.newBuilder(null);
            if (xml) xstromaBuilder.xml();
            STROMARequestBuilder stromaBuilder = STROMARequestBuilder.newBuilder(serviceName);
            for (;i < args.length;) {
                stromaBuilder.parameter(args[i++], args[i++]);
            }
            HttpXSTROMARequest.execute(url, System.out, xstromaBuilder.add(stromaBuilder.build()).build(), useXSTROMAMessage);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
    
    private static final Map<String, String> headerProperties;
    static {
        headerProperties = new HashMap<String, String>();
        headerProperties.put("STROMA", "true");
    }
    
}
