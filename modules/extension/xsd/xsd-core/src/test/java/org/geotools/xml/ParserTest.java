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
package org.geotools.xml;

import junit.framework.TestCase;
import java.util.List;
import org.geotools.ml.MLConfiguration;
import org.geotools.ml.Mail;
import org.geotools.ml.bindings.MLSchemaLocationResolver;
import org.xml.sax.SAXException;


public class ParserTest extends TestCase {
    public void testParse() throws Exception {
        Parser parser = new Parser(new MLConfiguration());
        List mails = (List) parser.parse(MLSchemaLocationResolver.class.getResourceAsStream(
                    "mails.xml"));

        assertEquals(2, mails.size());

        Mail mail = (Mail) mails.get(0);
        assertEquals(0, mail.getId().intValue());

        mail = (Mail) mails.get(1);
        assertEquals(1, mail.getId().intValue());
    }

    public void testParseValid() throws Exception {
        Parser parser = new Parser(new MLConfiguration());
        parser.setValidating(true);
        parser.parse(MLSchemaLocationResolver.class.getResourceAsStream("mails.xml"));

        assertEquals(0, parser.getValidationErrors().size());
    }

    public void testParseInValid() throws Exception {
        Parser parser = new Parser(new MLConfiguration());
        parser.setValidating(true);
        parser.parse(MLSchemaLocationResolver.class.getResourceAsStream("mails-invalid.xml"));

        assertFalse(0 == parser.getValidationErrors().size());
        
        //test immeediate failure case
        parser.setFailOnValidationError(true);
        try {
            parser.parse(MLSchemaLocationResolver.class.getResourceAsStream("mails-invalid.xml"));    
            fail( "should have thrown an error with setFailOnValidationError set");
        }
        catch( SAXException e ) {
        }
        
    }
}
