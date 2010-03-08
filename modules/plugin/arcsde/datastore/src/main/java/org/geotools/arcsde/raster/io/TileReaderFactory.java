package org.geotools.arcsde.raster.io;

import java.awt.Rectangle;

import org.geotools.arcsde.raster.info.RasterDatasetInfo;
import org.geotools.arcsde.session.ISessionPool;

public class TileReaderFactory {

    /**
     * 
     * @param preparedQuery
     * @param row
     * @param nativeType
     * @param targetType
     * @param noDataValues
     * @param numberOfBands
     * @param requestedTiles
     * @param tileSize
     * @return
     */
    public static TileReader getInstance(final ISessionPool sessionPool,
            final RasterDatasetInfo rasterInfo, final long rasterId, final int pyramidLevel,
            final Rectangle requestedTiles) {

        final TileReader tileReader;

        TileReader nativeTileReader = new NativeTileReader(sessionPool, rasterInfo, rasterId,
                pyramidLevel, requestedTiles);

        tileReader = nativeTileReader;

        return tileReader;
    }
}
