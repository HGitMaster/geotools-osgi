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
package org.geotools.referencing.factory.wms;

import java.util.Collection;

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link AutoCRSFactory}.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/referencing/src/test/java/org/geotools/referencing/factory/wms/AUTOTest.java $
 * @version $Id: AUTOTest.java 30641 2008-06-12 17:42:27Z acuster $
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class AUTOTest {
    /**
     * The factory to test.
     */
    private CRSAuthorityFactory factory;

    /**
     * Initializes the factory to test.
     */
    @Before
    public void setUp() {
        factory = new AutoCRSFactory();
    }

    /**
     * Tests the registration in {@link ReferencingFactoryFinder}.
     */
    @Test
    public void testFactoryFinder() {
        final Collection<String> authorities = ReferencingFactoryFinder.getAuthorityNames();
        assertTrue(authorities.contains("AUTO"));
        assertTrue(authorities.contains("AUTO2"));
        factory = ReferencingFactoryFinder.getCRSAuthorityFactory("AUTO", null);
        assertTrue(factory instanceof AutoCRSFactory);
        assertSame(factory, ReferencingFactoryFinder.getCRSAuthorityFactory("AUTO2", null));
    }

    /**
     * Checks the authority names.
     */
    @Test
    public void testAuthority() {
        final Citation authority = factory.getAuthority();
        assertTrue (Citations.identifierMatches(authority, "AUTO"));
        assertTrue (Citations.identifierMatches(authority, "AUTO2"));
        assertFalse(Citations.identifierMatches(authority, "EPSG"));
        assertFalse(Citations.identifierMatches(authority, "CRS"));
    }

    /**
     * UDIG requires this to work.
     */
    @Test
    public void test42001() throws FactoryException {
        final ProjectedCRS utm = factory.createProjectedCRS("AUTO:42001,0.0,0.0");
        assertNotNull("auto-utm", utm);
        assertSame   (utm, factory.createObject("AUTO :42001, 0,0"));
        assertSame   (utm, factory.createObject("AUTO2:42001, 0,0"));
        assertSame   (utm, factory.createObject(      "42001, 0,0"));
        assertNotSame(utm, factory.createObject("AUTO :42001,30,0"));
        assertEquals ("Transverse_Mercator", utm.getConversionFromBase().getMethod().getName().getCode());
    }
}
