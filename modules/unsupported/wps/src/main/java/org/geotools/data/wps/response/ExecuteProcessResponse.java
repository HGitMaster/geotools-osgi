/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wps.response;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows11.ExceptionReportType;
import net.opengis.wps10.ExecuteResponseType;

import org.geotools.data.ows.Response;
import org.geotools.ows.ServiceException;
import org.geotools.wps.WPSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

/**
 * Represents the response from a server after an ExecuteProcess request
 * has been issued.
 * 
 * @author gdavis
 */
public class ExecuteProcessResponse extends Response {

    private ExecuteResponseType exeResponse;
    private ExceptionReportType excepResponse;

    /**
     * @param contentType
     * @param inputStream
     * @throws ServiceException 
     * @throws SAXException
     */
    public ExecuteProcessResponse( String contentType, InputStream inputStream ) throws IOException, ServiceException {
        super(contentType, inputStream);
        
        try {
	        //Map hints = new HashMap();
	        //hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WPSSchema.getInstance());
        	Configuration config = new WPSConfiguration();
        	Parser parser = new Parser(config);
	
	        Object object;
	        excepResponse = null;
	        exeResponse = null;
			try {
				//object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
				object =  parser.parse(inputStream);
			} catch (SAXException e) {
				throw (IOException) new IOException().
				initCause(e);
			} catch (ParserConfigurationException e) {
				throw (IOException) new IOException().initCause(e);
			}
			
			// try casting the response
			if (object instanceof ExecuteResponseType) {
				exeResponse = (ExecuteResponseType) object;
			}
			// exception caught on server and returned
			else if (object instanceof ExceptionReportType) {
				excepResponse = (ExceptionReportType) object;
			}
			
			
        } finally {
        	inputStream.close();
        }
    }

    public ExecuteResponseType getExecuteResponse() {
        return exeResponse;
    }
    
    public ExceptionReportType getExceptionResponse() {
        return excepResponse;
    }    

}
