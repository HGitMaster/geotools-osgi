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
package org.geotools.referencing;

import java.io.LineNumberReader;
import org.geotools.test.TestData;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Run a test scripts. Scripts include a test suite provided by OpenGIS.
 * Each script contains a list of source and target coordinates reference systems (in WKT),
 * source coordinate points and expected coordinate points after the transformation from
 * source CRS to target CRS.
 * <p>
 * This is probably the most important test case for the whole CRS module.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/referencing/src/test/java/org/geotools/referencing/ScriptTest.java $
 * @version $Id: ScriptTest.java 30641 2008-06-12 17:42:27Z acuster $
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux (IRD)
 */
public final class ScriptTest {
    /**
     * Run the specified test script.
     *
     * @throws Exception If a test failed.
     */
    private void runScript(final String filename) throws Exception {
        final LineNumberReader in = TestData.openReader(this, filename);
        final ScriptRunner test = new ScriptRunner(in);
        test.executeAll();
        in.close();
    }

    /**
     * Run "AbridgedMolodensky.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testAbridgedMolodesky() throws Exception {
        runScript("scripts/AbridgedMolodensky.txt");
    }

    /**
     * Run "Molodensky.txt".
     *
     * @throws IOException If {@link #MT_MOLODENSKY_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    @Test
    public void testMolodesky() throws Exception {
        runScript("scripts/Molodensky.txt");
    }

    /**
     * Run "Simple.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testSimple() throws Exception {
        runScript("scripts/Simple.txt");
    }

    /**
     * Run "Projections.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testProjections() throws Exception {
        runScript("scripts/Projections.txt");
    }

    /**
     * Run "Mercator.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testMercator() throws Exception {
        runScript("scripts/Mercator.txt");
    }

    /**
     * Run the "ObliqueMercator.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testObliqueMercator() throws Exception {
        runScript("scripts/ObliqueMercator.txt");
    }

    /**
     * Run "TransverseMercator.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testTransverseMercator() throws Exception {
        runScript("scripts/TransverseMercator.txt");
    }

    /**
     * Run "AlbersEqualArea.txt"
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testAlbersEqualArea() throws Exception {
        runScript("scripts/AlbersEqualArea.txt");
    }

    /**
     * Run "LambertAzimuthalEqualArea.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testLambertAzimuthalEqualArea() throws Exception {
        runScript("scripts/LambertAzimuthalEqualArea.txt");
    }

    /**
     * Run "LambertConic.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testLambertConic() throws Exception {
        runScript("scripts/LambertConic.txt");
    }

    /**
     * Run "Stereographic.txt".
     *
     * @throws Exception If a test failed.
     */
    // hwellmann: fails on Java 1.6.0
    //@Test
    public void testStereographic() throws Exception {
        runScript("scripts/Stereographic.txt");
    }

    /**
     * Run "Orthographic.txt".
     *
     * @throws Exception If a test failed.
     */
    // hwellmann: fails on Java 1.6.0
    //@Test
    public void testOrthographic() throws Exception {
        runScript("scripts/Orthographic.txt");
    }

    /**
     * Run "NZMG.txt"
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testNZMG() throws Exception {
    	runScript("scripts/NZMG.txt");
    }

    /**
     * Run "Krovak.txt"
     *
     * @throws Exception If a test failed.
     */
    @Test
    public void testKrovak() throws Exception {
    	runScript("scripts/Krovak.txt");
    }

    /**
     * Run "OpenGIS.txt".
     *
     * @throws Exception If a test failed.
     */
    @Test
    @Ignore
    public void testOpenGIS() throws Exception {
        runScript("scripts/OpenGIS.txt");
    }

    /**
     * Run "NADCON.txt"
     *
     * @throws Exception If a test failed.
     */
    @Test
    @Ignore
    public void testNADCON() throws Exception {
        runScript("scripts/NADCON.txt");
    }
}
