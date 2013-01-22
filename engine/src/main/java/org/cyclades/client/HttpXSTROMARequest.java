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
    
    public static String toString (XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage) throws Exception {
        if (xstromaMessage) return xstromaRequest.toXSTROMAMessage();
        return xstromaRequest.generateData();
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
            if (System.getProperty("run_demo_request", "false").equalsIgnoreCase("true")) {
                HttpXSTROMARequest.execute(
                    "http://localhost:8080/cyclades/servicebroker", 
                    System.out,
                    XSTROMARequestBuilder.newBuilder(null).parameter("transaction-data", "123A").add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "foo").build()).xml().build(),
                    true
                );
                return;
            }
            if (args.length < 4) {
                System.out.println("Simple command line tool to create and execute one XSTROMA request via HTTP for one service\n");
                System.out.println("usage: cmd [-Drun_demo_request=true]");
                System.out.println("Runs the following sample java code:\n");
                System.out.println("HttpXSTROMARequest.execute(");
                System.out.println("\t\"http://localhost:8080/cyclades/servicebroker\",");
                System.out.println("\tSystem.out,");
                System.out.println("\tXSTROMARequestBuilder.newBuilder(null).parameter(\"transaction-data\", \"123A\").add(STROMARequestBuilder.newBuilder(\"helloworld\").parameter(\"action\", \"sayhello\").parameter(\"name\", \"foo\").build()).xml().build(),");
                System.out.println("\ttrue");
                System.out.println(");\n");
                System.out.println("usage: cmd [-Dprint_payload_only=true] service_broker_url data_type use_xstroma_message service_name [parameter_key parameter_value] ...");
                System.out.println("service_broker_url: i.e. \"http://localhost:8080/cyclades/servicebroker\"");
                System.out.println("data_type: \"json\" or \"xml\"");
                System.out.println("use_xstroma_message: \"true\" or \"false\", use conventional HTTP payload request or use the XSTROMA message format");
                System.out.println("service_name: The name of the STROMA service to request, i.e. \"helloworld\"");
                System.out.println("[parameter_key parameter_value]: Any number of key/value pairs for the request, will be added to the XSTROMA level");
                System.exit(1);
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
                xstromaBuilder.parameter(args[i++], args[i++]);
            }
            XSTROMABrokerRequest xstromaRequest = xstromaBuilder.add(stromaBuilder.build()).build();
            if (System.getProperty("print_payload_only", "false").equalsIgnoreCase("true")) {
                System.out.println(toString(xstromaRequest, useXSTROMAMessage));
                return;
            }
            HttpXSTROMARequest.execute(url, System.out, xstromaRequest, useXSTROMAMessage);
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
