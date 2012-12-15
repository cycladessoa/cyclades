package org.cyclades.engine.util;

import java.util.ArrayList;
import java.util.List;
import org.cyclades.engine.CycladesEngine;
import org.cyclades.engine.EngineContext;
import org.cyclades.io.ResourceRequestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON {

    /**
     * Returns a JSONArray as a List<JSONObject> whose members have been resolved to an external link if 
     * JSONObject.has("link")
     * 
     * @param jsonArrayString The string representation of the JSONObject list
     * @return A list of the JSONObjects as resolved be the resolveLinkedJSONObject method
     * @throws Exception
     */
    public static List<JSONObject> resolveLinkedJSONObjects (String jsonArrayString) throws Exception {
        List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
        JSONArray jsonArray = new JSONArray(jsonArrayString);
        for (int i = 0; i < jsonArray.length(); i++) jsonObjectList.add(resolveLinkedJSONObject(jsonArray.getJSONObject(i)));
        return jsonObjectList;
    }
    
    /**
     * Resolves a JSONObject that evaluates to true for the condition jsonObject.has("link")
     * 
     * The link field can be one of the following:
     *  - An absolute directory
     *  - A relative directory, resolved against the Cyclades Service Engine's canonical directory if initialized
     *  - A http URL
     * 
     * @param jsonObject
     * @return The JSONObject, resolved if applicable
     * @throws JSONException
     * @throws Exception
     */
    public static JSONObject resolveLinkedJSONObject (JSONObject jsonObject) throws JSONException, Exception {
        if (!jsonObject.has("link")) return jsonObject;
        String link = jsonObject.getString("link");
        EngineContext engineContext = CycladesEngine.getEngineContext();
        if (engineContext != null) link = engineContext.getCanonicalEngineDirectoryPath(link);
        return new JSONObject(new String(ResourceRequestUtils.getData(link, null)));
    }
    
}
