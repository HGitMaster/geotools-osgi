package org.geotools.arcsde.gce.imageio;

import java.util.NoSuchElementException;

import com.esri.sde.sdk.client.SeRaster;

/**
 * An enumeration that mirrors the different possible band interleave types in Arcsde (ie, {@code
 * SeRaster#SE_RASTER_INTERLEAVE_*})
 * 
 * @author Gabriel Roldan
 */
public enum InterleaveType {
    INTERLEAVE_BIL, INTERLEAVE_BIL_91, INTERLEAVE_BIP, INTERLEAVE_BIP_91, INTERLEAVE_BSQ, INTERLEAVE_BSQ_91, INTERLEAVE_NONE;
    static {
        INTERLEAVE_BIL.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BIL);
        INTERLEAVE_BIL_91.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BIL_91);
        INTERLEAVE_BIP.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BIP);
        INTERLEAVE_BIP_91.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BIP_91);
        INTERLEAVE_BSQ.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BSQ);
        INTERLEAVE_BSQ_91.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_BSQ_91);
        INTERLEAVE_NONE.setSdeTypeId(SeRaster.SE_RASTER_INTERLEAVE_NONE);
    }

    private int typeId;

    private void setSdeTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSeRasterInterleaveType() {
        return this.typeId;
    }

    public static InterleaveType valueOf(final int seRasterInterleaveType) {
        for (InterleaveType type : InterleaveType.values()) {
            if (type.getSeRasterInterleaveType() == seRasterInterleaveType) {
                return type;
            }
        }
        throw new NoSuchElementException("Raster interleave type " + seRasterInterleaveType
                + " does not exist");
    }
}
