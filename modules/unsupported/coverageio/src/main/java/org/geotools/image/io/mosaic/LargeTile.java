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

import java.awt.Rectangle;
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;


/**
 * A tile with larger capacity than the default {@link Tile} implementation. We should have very
 * few instances of this tile, since it consume more memory than the default implementation.
 * <p>
 * This class is not public because we don't want to expose the size limitation in public API.
 * If we need to provides a way to avoid the size limitation, we should consider to create a
 * factory instead.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/coverageio/src/main/java/org/geotools/image/io/mosaic/LargeTile.java $
 * @version $Id: LargeTile.java 30729 2008-06-16 09:26:48Z desruisseaux $
 * @author Martin Desruisseaux
 */
final class LargeTile extends Tile {
    /**
     * For cross-version compatibility during serialization.
     */
    private static final long serialVersionUID = -390809968753673788L;

    /**
     * The size of the image to be read.
     */
    private int width, height;

    /**
     * Creates a tile for the given region with default subsampling.
     *
     * @param provider
     *          The image reader provider to use. The same provider is typically given to every
     *          {@code Tile} objects to be given to the same {@link TileManager} instance, but
     *          this is not mandatory. If {@code null}, the provider will be inferred from the
     *          input. If it can't be inferred, then an exception is thrown.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index to be given to the image reader for reading this tile.
     * @param region
     *          The region in the destination image. The {@linkplain Rectangle#width width} and
     *          {@linkplain Rectangle#height height} should match the image size.
     *
     * @throws IllegalArgumentException
     *          If a required argument is {@code null} or some argument has an invalid value.
     */
    public LargeTile(ImageReaderSpi provider, Object input, int imageIndex, Rectangle region)
                throws IllegalArgumentException
    {
        super(provider, input, imageIndex, region);
        // (width, height) will be set indirectly through 'setSize' invocation.
        try {
            assert region.equals(getRegion());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets the tile size to the given values.
     *
     * @param dx The tile width.
     * @param dy The tile height.
     * @throws IllegalArgumentException if the given size are negative.
     */
    @Override
    final void setSize(final int dx, final int dy) throws IllegalArgumentException {
        super.setSize(Math.min(dx, 0xFFFF), Math.min(dy, 0xFFFF));
        width  = dx;
        height = dy;
    }

    /**
     * Returns the upper-left corner in the destination image, with the image size.
     *
     * @return The region in the destination image.
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getImageReader reader} and this operation failed.
     */
    @Override
    public Rectangle getRegion() throws IOException {
        final Rectangle region = super.getRegion();
        region.width  = width;
        region.height = height;
        return region;
    }
}
