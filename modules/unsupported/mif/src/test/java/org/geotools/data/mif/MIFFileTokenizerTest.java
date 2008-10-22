/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.vividsolutions.jts.io.ParseException;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mif/src/test/java/org/geotools/data/mif/MIFFileTokenizerTest.java $
 */
public class MIFFileTokenizerTest extends TestCase {
    MIFFileTokenizer tok = null;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFFileTokenizerTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        tok = new MIFFileTokenizer(new BufferedReader(
                    new FileReader(
                        new File(MIFTestUtils.fileName("MIFFileTokenizer.txt")))));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        tok = null;
        super.tearDown();
    }

    /*
     * Class under test for boolean readLine()
     */
    public void testReadLine() {
        assertEquals(true, tok.readLine());

        try {
            assertEquals("one", tok.getToken(' '));
            assertEquals("two", tok.getToken(' '));
            assertEquals("three", tok.getToken(' ', true, false));
            assertEquals(2, tok.getLineNumber());
        } catch (ParseException e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetLineNumber() {
        assertEquals(true, tok.readLine());
        assertEquals(true, tok.readLine());
        assertEquals(2, tok.getLineNumber());
    }
}
