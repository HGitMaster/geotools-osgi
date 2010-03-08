/*
 * $RCSfile: MlibInvertOpImage.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:58 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import java.util.Map;
import com.sun.medialib.mlib.*;

/**
 * An <code>OpImage</code> implementing the "Invert" operation
 * using MediaLib.
 *
 * @see javax.media.jai.operator.InvertDescriptor
 * @see MlibInvertRIF
 *
 * @since 1.0
 *
 */
final class MlibInvertOpImage extends PointOpImage {

    /** Constructor. */
    public MlibInvertOpImage(RenderedImage source,
                             Map config,
                             ImageLayout layout) {
        super(source, layout, config, true);
        // Set flag to permit in-place operation.
        permitInPlaceOperation();
    }

    /**
     * Performs the "Invert" operation on a rectangular region of
     * the same.
     */
    protected void computeRect(Raster[] sources,
                               WritableRaster dest,
                               Rectangle destRect) {
        int formatTag = MediaLibAccessor.findCompatibleTag(sources, dest);

        MediaLibAccessor srcMA =
            new MediaLibAccessor(sources[0], destRect, formatTag);
        MediaLibAccessor dstMA =
            new MediaLibAccessor(dest, destRect, formatTag);

        mediaLibImage[] srcMLI = srcMA.getMediaLibImages();
        mediaLibImage[] dstMLI = dstMA.getMediaLibImages();

        switch (dstMA.getDataType()) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_INT:
            for (int i = 0 ; i < dstMLI.length; i++) {
                Image.Invert(dstMLI[i], srcMLI[i]);
            }
            break;

        case DataBuffer.TYPE_FLOAT:
        case DataBuffer.TYPE_DOUBLE:
            for (int i = 0 ; i < dstMLI.length; i++) {
                Image.Invert_Fp(dstMLI[i], srcMLI[i]);
            }
            break;

        default:
            throw new RuntimeException(JaiI18N.getString("Generic2"));
        }

        if (dstMA.isDataCopy()) {
            dstMA.clampDataArrays();
            dstMA.copyDataToRaster();
        }
    }
}
