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
package org.geotools.gpx.binding.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.gpx.GPX;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.gpx.bean.GpxType;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;


public class GPXBindingTest extends TestCase {
    public void test() throws IOException, SAXException, ParserConfigurationException {
        
        URL u = TestData.url(this, "test.gpx");
        
        InputStream input = u.openStream();
        
        GPXConfiguration configuration = new GPXConfiguration();

        Parser parser = new Parser(configuration);
        GpxType po = (GpxType) parser.parse(input);

        assertNotNull(po);
        
        Encoder encoder = new Encoder(configuration);
        encoder.setNamespaceAware(false);
        
        encoder.encode(po, GPX.gpx, new devpernull());
    }
}

class devpernull extends OutputStream {

    @Override
    public void write(int b) throws IOException {
        // TODO Auto-generated method stub
        
    }
    
}
