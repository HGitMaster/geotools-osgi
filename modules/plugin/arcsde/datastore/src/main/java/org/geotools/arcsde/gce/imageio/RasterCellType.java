package org.geotools.arcsde.gce.imageio;

import java.util.NoSuchElementException;

import com.esri.sde.sdk.client.SeRaster;

/**
 * An enumeration that mirrors the different possible cell resolutions in Arcsde (ie, {@code
 * SeRaster#SE_PIXEL_TYPE_*})
 * 
 * @author Gabriel Roldan
 */
public enum RasterCellType {
    TYPE_16BIT_S, TYPE_16BIT_U, TYPE_1BIT, TYPE_32BIT_REAL, TYPE_32BIT_S, TYPE_32BIT_U, TYPE_4BIT, TYPE_64BIT_REAL, TYPE_8BIT_S, TYPE_8BIT_U;
    static {
        TYPE_16BIT_S.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_16BIT_S);
        TYPE_16BIT_U.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_16BIT_U);
        TYPE_1BIT.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_1BIT);
        TYPE_32BIT_REAL.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_32BIT_REAL);
        TYPE_32BIT_S.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_32BIT_S);
        TYPE_32BIT_U.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_32BIT_U);
        TYPE_4BIT.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_4BIT);
        TYPE_64BIT_REAL.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_64BIT_REAL);
        TYPE_8BIT_S.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_8BIT_S);
        TYPE_8BIT_U.setSdeTypeId(SeRaster.SE_PIXEL_TYPE_8BIT_U);
    }

    private int typeId;

    private void setSdeTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSeRasterPixelType() {
        return this.typeId;
    }

    public static RasterCellType valueOf(final int seRasterPixelType) {
        for (RasterCellType type : RasterCellType.values()) {
            if (type.getSeRasterPixelType() == seRasterPixelType) {
                return type;
            }
        }
        throw new NoSuchElementException("Raster pixel type " + seRasterPixelType
                + " does not exist");
    }
}
