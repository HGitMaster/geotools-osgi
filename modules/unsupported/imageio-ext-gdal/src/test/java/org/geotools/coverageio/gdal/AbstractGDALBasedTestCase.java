/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverageio.gdal;

import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.test.TestData;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Base testing class initializing JAI properties to be used during tests.
 */
public class AbstractGDALBasedTestCase extends TestCase {

    protected final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.coverageio.gdal");

    protected static void forceDataLoading(final GridCoverage2D gc) {
        assertNotNull(gc);

        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            PlanarImage.wrapRenderedImage(gc.getRenderedImage()).getTiles();
        }
    }

    /**
     * A String containing the name of the supported format. It will be used to
     * customize the messages.
     */
    private String supportedFormat;

    /**
     * The {@code GridFormatFactorySpi} provided by the specific subclass to
     * handle a specific format.
     */
    private GridFormatFactorySpi factorySpi;

    public AbstractGDALBasedTestCase(String name, String supportedFormat,
        GridFormatFactorySpi factorySpi) {
        super(name);
        this.supportedFormat = supportedFormat;
        this.factorySpi = factorySpi;
    }

    protected void setUp() throws Exception {
        super.setUp();
        ImageIO.setUseCache(false);
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(16 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
        JAI.getDefaultInstance().getTileScheduler().setParallelism(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
        JAI.getDefaultInstance().getTileScheduler().setPriority(5);
    }

    protected boolean testingEnabled() {
        boolean available = factorySpi.isAvailable();

        if (!available) {
            LOGGER.warning(supportedFormat + " libraries are not available, skipping tests!");
        }

        return available;
    }
}
