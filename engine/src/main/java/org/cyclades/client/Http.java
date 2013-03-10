package org.cyclades.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.stroma.xstroma.STROMARequest;
import org.cyclades.engine.stroma.xstroma.XSTROMABrokerRequest;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.io.StreamUtils;

public class Http {
    
    /******************************************************************************/
    /*                        X-STROMA Requests                                   */
    /******************************************************************************/
    
    public static byte[] execute (String url, XSTROMABrokerRequest xstromaRequest) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(url, xstromaRequest, baos);
        return baos.toByteArray();
    }
    
    public static void execute (String url, XSTROMABrokerRequest xstromaRequest, OutputStream out) throws Exception {
        execute (url, xstromaRequest, false, out, 0, 0);
    }
    
    public static byte[] execute (String url, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(url, xstromaRequest, xstromaMessage, baos);
        return baos.toByteArray();
    }
    
    public static void execute (String url, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage, OutputStream out) throws Exception {
        execute (url, xstromaRequest, xstromaMessage, out, 0, 0);
    }
    
    public static byte[] execute (String url, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage, int connectionTimeout, int readTimeout) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(url, xstromaRequest, xstromaMessage, baos, connectionTimeout, readTimeout);
        return baos.toByteArray();
    }
    
    public static void execute (String url, XSTROMABrokerRequest xstromaRequest, boolean xstromaMessage, OutputStream out, int connectionTimeout, int readTimeout) throws Exception {
        StringBuilder requestURL = new StringBuilder(url);
        String xstromaString;
        if (xstromaMessage) {
            requestURL.append("?").append(ServiceBrokerNyxletImpl.XSTROMA_MESSAGE);
            xstromaString = xstromaRequest.toXSTROMAMessage();
        } else {
            requestURL.append(createParameterizedXSTOMAUrl(xstromaRequest));
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
    
    private static String createParameterizedXSTOMAUrl (XSTROMABrokerRequest xstromaRequest) throws URISyntaxException {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("data-type=").append(xstromaRequest.getMetaTypeEnum().name());
        Map<String, List<String>> parameters = xstromaRequest.getParameters();
        for (Map.Entry<String, List<String>> parametersEntry : parameters.entrySet()) {
            String key = parametersEntry.getKey();
            List<String> values = parametersEntry.getValue();
            for (String value : values) {
                urlBuilder.append("&").append(key).append("=").append(value);
            }
        }
        return new URI(null, null, null, urlBuilder.toString(), null).toString();
    }
    
    /******************************************************************************/
    /*                          STROMA Requests                                   */
    /******************************************************************************/
    
    public static byte[] execute (String url, STROMARequest stromaRequest) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(url, stromaRequest, baos);
        return baos.toByteArray();
    }
    
    public static void execute (String url, STROMARequest stromaRequest, OutputStream out) throws Exception {
        execute (url, stromaRequest, out, 0, 0);
    }
    
    public static byte[] execute (String url, STROMARequest stromaRequest, int connectionTimeout, int readTimeout) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(url, stromaRequest, baos, connectionTimeout, readTimeout);
        return baos.toByteArray();
    }
    
    public static void execute (String url, STROMARequest stromaRequest, OutputStream out, int connectionTimeout, int readTimeout) throws Exception {
        StringBuilder requestURL = new StringBuilder(url);
        // Tack on "action" as a query parameter, as for STROMA requests (HTTP), a STROMA embedded action is not readable until it
        // is too late if a there exists another ActionHandler with the default POST alias that ignores STROMA parameters.
        // In other words, make action explicit as a query parameter for all STROMA HTTP requests!
        if (stromaRequest.getParameters().containsKey("action")) {
            requestURL.append((url.indexOf("?") < 0) ? "?" : "&");
            requestURL.append("action=").append(stromaRequest.getParameters().get("action").get(0));
        }
        String stromaDataString = stromaRequest.generateData();
        InputStream is = null;
        try {
            is = ResourceRequestUtils.getInputStreamHTTP(requestURL.toString(), "POST", new ByteArrayInputStream(stromaDataString.getBytes()), headerProperties, connectionTimeout, readTimeout);
            StreamUtils.write(is, out);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }
    
    private static final Map<String, String> headerProperties;
    static {
        headerProperties = new HashMap<String, String>();
        headerProperties.put("STROMA", "true");
    }
    
}
