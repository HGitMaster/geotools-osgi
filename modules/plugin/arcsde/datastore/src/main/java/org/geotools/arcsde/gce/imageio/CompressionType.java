package org.geotools.arcsde.gce.imageio;

import java.util.NoSuchElementException;

import com.esri.sde.sdk.client.SeRaster;

/**
 * An enumeration that mirrors the different possible raster compression types in Arcsde (ie,
 * {@code SeRaster#SE_COMPRESSION_*})
 * 
 * @author Gabriel Roldan
 */
public enum CompressionType {
    COMPRESSION_JP2, COMPRESSION_JPEG, COMPRESSION_LZ77, COMPRESSION_NONE;
    static {
        COMPRESSION_JP2.setSdeTypeId(SeRaster.SE_COMPRESSION_JP2);
        COMPRESSION_JPEG.setSdeTypeId(SeRaster.SE_COMPRESSION_JPEG);
        COMPRESSION_LZ77.setSdeTypeId(SeRaster.SE_COMPRESSION_LZ77);
        COMPRESSION_NONE.setSdeTypeId(SeRaster.SE_COMPRESSION_NONE);
    }

    private int typeId;

    private void setSdeTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSeCompressionType() {
        return this.typeId;
    }

    public static CompressionType valueOf(final int seCompressionType) {
        for (CompressionType type : CompressionType.values()) {
            if (type.getSeCompressionType() == seCompressionType) {
                return type;
            }
        }
        throw new NoSuchElementException("Compression type " + seCompressionType
                + " does not exist");
    }
}
