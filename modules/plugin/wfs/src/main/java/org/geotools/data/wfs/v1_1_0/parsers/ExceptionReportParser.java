/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.opengis.ows10.ExceptionReportType;
import net.opengis.ows10.ExceptionType;
import net.opengis.wfs.BaseRequestType;
import net.opengis.wfs.DescribeFeatureTypeType;
import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetGmlObjectType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.TransactionType;

import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.protocol.wfs.WFSException;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;

/**
 * A WFS response parser that parses server exception reports into {@link WFSException} objects.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: ExceptionReportParser.java 31823 2008-11-11 16:11:49Z groldan $
 * @since 2.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/v1_1_0/parsers/ExceptionReportParser.java $
 */
@SuppressWarnings( { "nls", "unchecked" })
public class ExceptionReportParser implements WFSResponseParser {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * @param wfs
     *            the {@link WFSDataStore} that sent the request
     * @param response
     *            a response handle to a service exception report
     * @return a {@link WFSException} containing the server returned exception report messages
     * @see WFSResponseParser#parse(WFSProtocol, WFSResponse)
     */
    public Object parse(WFS_1_1_0_DataStore wfs, WFSResponse response) {
        WFSConfiguration configuration = new WFSConfiguration();
        Parser parser = new Parser(configuration);
        InputStream responseStream = response.getInputStream();
        Charset responseCharset = response.getCharacterEncoding();
        Reader reader = new InputStreamReader(responseStream, responseCharset);
        Object parsed;
        try {
            parsed = parser.parse(reader);
            if (!(parsed instanceof net.opengis.ows10.ExceptionReportType)) {
                return new IOException("Unrecognized server error");
            }
        } catch (Exception e) {
            return new WFSException("Exception parsing server exception report", e);
        }
        net.opengis.ows10.ExceptionReportType report = (ExceptionReportType) parsed;
        List<ExceptionType> exceptions = report.getException();

        BaseRequestType originatingRequest = response.getOriginatingRequest();
        StringBuilder msg = new StringBuilder("WFS returned an exception.");
        msg.append(" Target URL: " + response.getTargetUrl());
        if (originatingRequest != null) {
            Encoder encoder = new Encoder(configuration);
            encoder.setIndentSize(1);
            try {

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                QName encodeElementName = getElementName(originatingRequest);
                encoder.encode(originatingRequest, encodeElementName, out);
                String requestStr = out.toString();

                msg.append(". Originating request is: \n").append(requestStr).append("\n");
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error encoding request for exception report", e);
            }
        }
        WFSException result = new WFSException(msg.toString());
        for (ExceptionType ex : exceptions) {
            result.addExceptionReport(String.valueOf(ex.getExceptionText()));
        }
        return result;
    }

    private QName getElementName(BaseRequestType originatingRequest) {
        QName encodeElementName;
        if (originatingRequest instanceof GetFeatureType) {
            encodeElementName = WFS.GetFeature;
        } else if (originatingRequest instanceof DescribeFeatureTypeType) {
            encodeElementName = WFS.DescribeFeatureType;
        } else if (originatingRequest instanceof GetCapabilitiesType) {
            encodeElementName = WFS.GetCapabilities;
        } else if (originatingRequest instanceof GetGmlObjectType) {
            encodeElementName = WFS.GetGmlObject;
        } else if (originatingRequest instanceof LockFeatureType) {
            encodeElementName = WFS.LockFeature;
        } else if (originatingRequest instanceof TransactionType) {
            encodeElementName = WFS.Transaction;
        } else {
            throw new IllegalArgumentException("Unkown xml element name for " + originatingRequest);
        }
        return encodeElementName;
    }
}
