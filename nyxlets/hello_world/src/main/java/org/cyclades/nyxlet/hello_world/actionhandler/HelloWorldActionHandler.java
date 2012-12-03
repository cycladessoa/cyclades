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

package org.cyclades.nyxlet.hello_world.actionhandler;

import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;

/**
 * This is an example of a minimalistic ActionHandler. Note the keys in the annotation...a request will be dispatched to this
 * ActionHandler when the action is set to "sayhello" or "GET". Note that if there is no action specified in a HTTP request
 * to the Cyclades Service Engine, the action defaults to the HTTP method of that request, in uppercase. With the following
 * Annotation, a request would be dispatched to this ActionHandler in the following scenarios:
 *
 *  - action is set to "sayhello"
 *  - action is set to "GET"
 *  - no action is specified, and the request is a HTTP GET
 *
 * This pattern follows for all HTTP methods (i.e. PUT, DELETE etc...)
 *
 */
@AHandler({"sayhello", "GET"})
public class HelloWorldActionHandler extends ActionHandler {

    public HelloWorldActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldActionHandler.handle: ";
        try {
            /********************************************************************/
            /*******                  START CODE BLOCK                    *******/
            /*******                                                      *******/
            /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
            /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
            /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
            /******* OF THIS ACTION HANDLER.                              *******/
            /********************************************************************/

            StringBuilder message = new StringBuilder("Hello World: ");

            // Get any parameter values entered under the key "name"
            if (baseParameters.containsKey("name")) {
                for (String name : baseParameters.get("name")) message.append("[").append(name).append("] ");
            }

            // Using XMLStreamWriter. The benefit here is that it gets converted for you
            // automatically to JSON or XML behind the scenes. This is most likely the
            // best all around method to write data. This is also the most efficient option for
            // writing out large amount of data as it is streamed.
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            streamWriter.writeStartElement("message");
            streamWriter.writeCharacters(message.toString());
            streamWriter.writeEndElement();

            // Another option would be to write the response as a response parameter. This is most
            // convenient and appropriate for smaller and more trivial responses.
            stromaResponseWriter.addResponseParameter("message-as-parameter", message.toString());

            /********************************************************************/
            /*******                  END CODE BLOCK                      *******/
            /********************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    /**
     * Return a valid health check status. This one simply returns true, which
     * will always flag a healthy ActionHandler...more meaningful algorithms
     * can be used.
     *
     * @return true means this is a healthy ActionHandler
     * @throws Exception
     */
    @Override
    public boolean isHealthy () throws Exception {
        return true;
    }

    @Override
    public void init () throws Exception {
        // your initialization code here, if any
    }

    @Override
    public void destroy () throws Exception {
        // your destruction code here, if any
    }

}
