/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.wfs.v1_1_0;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.wfs.protocol.wfs.GetFeatureParser;
import org.geotools.data.wfs.v1_1_0.parsers.EmfAppSchemaParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

/**
 * Adapts a {@link GetFeatureParser} to the geotools {@link FeatureReader}
 * interface, being the base for all the data content related implementations in
 * the WFS module.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: WFSFeatureReader.java 31792 2008-11-06 19:17:35Z groldan $
 * @since 2.5.x
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/v1_1_0/WFSFeatureReader.java $
 * @see WFS110ProtocolHandler#getFeatureReader(org.geotools.data.Query,
 *      org.geotools.data.Transaction)
 */
class WFSFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private SimpleFeature next;

    private GetFeatureParser parser;

    private SimpleFeatureType featureType;

    public WFSFeatureReader(final GetFeatureParser parser) throws IOException {
        this.parser = parser;
        this.next = parser.parse();
        if (this.next != null) {
            //this is the FeatureType as parsed by the StreamingParser, we need a simple view
            FeatureType parsedType = next.getFeatureType();
            this.featureType = EmfAppSchemaParser.toSimpleFeatureType(parsedType);
        }
    }

    /**
     * @see FeatureReader#close()
     */
    public void close() throws IOException {
        final GetFeatureParser parser = this.parser;
        this.parser = null;
        this.next = null;
        if (parser != null) {
            parser.close();
        }
    }

    /**
     * @see FeatureReader#getFeatureType()
     */
    public SimpleFeatureType getFeatureType() {
        if (featureType == null) {
            throw new IllegalStateException(
                    "No features were retrieved, shouldn't be calling getFeatureType()");
        }
        return featureType;
    }

    /**
     * @see FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        return next != null;
    }

    /**
     * @see FeatureReader#next()
     */
    public SimpleFeature next() throws IOException, NoSuchElementException {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        SimpleFeature current = this.next;
        this.next = parser.parse();
        return current;
    }

}
