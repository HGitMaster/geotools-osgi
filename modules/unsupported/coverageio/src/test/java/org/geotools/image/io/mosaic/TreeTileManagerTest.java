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
package org.geotools.image.io.mosaic;

import java.io.IOException;


/**
 * Tests {@link TreeTileManager}.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/coverageio/src/test/java/org/geotools/image/io/mosaic/TreeTileManagerTest.java $
 * @version $Id: TreeTileManagerTest.java 30679 2008-06-13 10:19:41Z acuster $
 * @author Martin Desruisseaux
 */
public final class TreeTileManagerTest extends TileManagerTest {
    /**
     * The tile manager factory to be given to the {@linkplain #builder builder}. This method
     * make sure that only {@link TreeTileManager} instances are created. Then we inherit the
     * test suite from the base class.
     *
     * @return The tile manager factory to use.
     * @throws IOException If an I/O operation was required and failed.
     */
    @Override
    protected TileManagerFactory getTileManagerFactory() throws IOException {
        return new TileManagerFactory(null) {
            @Override
            protected TileManager createGeneric(final Tile[] tiles) throws IOException {
                return new TreeTileManager(tiles);
            }
        };
    }
}
