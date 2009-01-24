package org.geotools.arcsde.gce.imageio;

import java.util.NoSuchElementException;

import com.esri.sde.sdk.client.SeRaster;

/**
 * An enumeration that mirrors the different possible raster interpolation types in Arcsde (ie,
 * {@code SeRaster#SE_INTERPOLATION_*})
 * 
 * @author Gabriel Roldan
 */
public enum InterpolationType {
    INTERPOLATION_BICUBIC, INTERPOLATION_BILINEAR, INTERPOLATION_NEAREST, INTERPOLATION_NONE;
    static {
        INTERPOLATION_BICUBIC.setSdeTypeId(SeRaster.SE_INTERPOLATION_BICUBIC);
        INTERPOLATION_BILINEAR.setSdeTypeId(SeRaster.SE_INTERPOLATION_BILINEAR);
        INTERPOLATION_NEAREST.setSdeTypeId(SeRaster.SE_INTERPOLATION_NEAREST);
        INTERPOLATION_NONE.setSdeTypeId(SeRaster.SE_INTERPOLATION_NONE);
    }

    private int typeId;

    private void setSdeTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSeInterpolationType() {
        return this.typeId;
    }

    public static InterpolationType valueOf(final int seInterpolationType) {
        for (InterpolationType type : InterpolationType.values()) {
            if (type.getSeInterpolationType() == seInterpolationType) {
                return type;
            }
        }
        throw new NoSuchElementException("Interpolation type " + seInterpolationType
                + " does not exist");
    }
}
