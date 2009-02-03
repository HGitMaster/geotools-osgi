package org.geotools.arcsde.gce.imageio;

import java.awt.image.DataBuffer;
import java.util.NoSuchElementException;

import com.esri.sde.sdk.client.SeRaster;

/**
 * An enumeration that mirrors the different possible cell resolutions in Arcsde (ie, {@code
 * SeRaster#SE_PIXEL_TYPE_*})
 * 
 * @author Gabriel Roldan
 */
public enum RasterCellType {
    TYPE_16BIT_S(16, DataBuffer.TYPE_SHORT, true), //
    TYPE_16BIT_U(16, DataBuffer.TYPE_USHORT, false), //
    TYPE_1BIT(1, DataBuffer.TYPE_BYTE, false), //
    TYPE_32BIT_REAL(32, DataBuffer.TYPE_FLOAT, true), //
    TYPE_32BIT_S(32, DataBuffer.TYPE_INT, true), //
    TYPE_32BIT_U(32, DataBuffer.TYPE_INT, false), //
    TYPE_4BIT(4, DataBuffer.TYPE_BYTE, false), //
    TYPE_64BIT_REAL(64, DataBuffer.TYPE_DOUBLE, true), //
    TYPE_8BIT_S(8, DataBuffer.TYPE_BYTE, true), //
    TYPE_8BIT_U(8, DataBuffer.TYPE_BYTE, false);
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

    private final int bitsPerSample;
    
    private final int dataBufferType;

    private final boolean signed;
    
    private RasterCellType(final int bitsPerSample, final int dataBufferType, final boolean signed) {
        this.bitsPerSample = bitsPerSample;
        this.dataBufferType = dataBufferType;
        this.signed = signed;
    }

    private void setSdeTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSeRasterPixelType() {
        return this.typeId;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public int getDataBufferType() {
        return dataBufferType;
    }

    public boolean isSigned() {
        return signed;
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
