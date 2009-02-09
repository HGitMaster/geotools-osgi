/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.arcsde.gce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;

/**
 * An ImageIO Service Provider Interface for creating ArcSDERasterReaders.
 * 
 * @author sfarber
 * @deprecated leaving in test code by now until making sure we're not loosing test coverage
 */
@Deprecated
public class ArcSDERasterReaderSpi extends ImageReaderSpi {

    final public static String PYRAMID = "org.geotools.arcsde.gce.imageio.PYRAMID";

    final public static String RASTER_COLUMN = "org.geotools.arcsde.gce.imageio.RASTER_COLUMN";

    final public static String RASTER_TABLE = "org.geotools.arcsde.gce.imageio.RASTER_TABLE";

    final public static String SAMPLE_IMAGE = "org.geotools.arcsde.gce.imageio.SAMPLE_IMAGE";

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        // trust me, if you have to ask whether this plugin can decode a given
        // source, it can't.
        return false;
    }

    /**
     * Creates an ArcSDERasterReader (an ImageIO compatible ImageReader). This reader requires the
     * following keys to be set in the {@link java.util.Map} passed to this method:<br/>
     * 
     * <ul>
     * <li>ArcSDERasterReaderSpi.PYRAMID - a {@link ArcSDEPyramid} describing the pyramid that this
     * reader will be reading</li>
     * <li>ArcSDERasterReaderSpi.RASTER_COLUMN - a String containing the Raster column for this
     * readers RASTER_TABLE</li>
     * <li>ArcSDERasterReaderSpi.RASTER_TABLE - a String containing the name of the ArcSDE raster
     * table to read. Probably needs to be qualified with the schema name.</li>
     * <li>ArcSDERasterReaderSpi.SAMPLE_IMAGE - a sample {@link BufferedImage} compatible with the
     * target raster used as prototype to create the target image from if needed.
     * </ul>
     */
    @Override
    public ArcSDERasterReader createReaderInstance(final Object extension) throws IOException {
        if (extension instanceof Map) {
            Map map = (Map) extension;
            final ArcSDEPyramid pyramid = (ArcSDEPyramid) map.get(PYRAMID);
            final String table = (String) map.get(RASTER_TABLE);
            final String column = (String) map.get(RASTER_COLUMN);
            final BufferedImage sampleImage = (BufferedImage) map.get(SAMPLE_IMAGE);
            if (pyramid == null)
                throw new IllegalArgumentException(
                        "missing value for 'ArcSDERasterReaderSpi.PYRAMID' in supplied paramater map.");
            if (column == null)
                throw new IllegalArgumentException(
                        "missing value for 'ArcSDERasterReaderSpi.RASTER_COLUMN' in supplied paramater map.");
            if (table == null)
                throw new IllegalArgumentException(
                        "missing value for 'ArcSDERasterReaderSpi.RASTER_TABLE' in supplied paramater map.");
            if (sampleImage == null)
                throw new IllegalArgumentException(
                        "missing value for 'ArcSDERasterReaderSpi.SAMPLE_IMAGE' in supplied paramater map.");
            return new ArcSDERasterReader(this, pyramid, table, column, sampleImage);
        }
        throw new IllegalArgumentException(
                "ArcSDERasterReader needs a java.util.Map of parameters to be instantiated");
    }

    @Override
    public String getDescription(Locale locale) {
        return "ESRI ArcSDE database-stored raster image reader.";
    }

    /**
     * We completely ignore the setInput() call in this reader, so we'll happily accept anything.
     */
    @Override
    public Class[] getInputTypes() {
        return new Class[] { Object.class };
    }

}
