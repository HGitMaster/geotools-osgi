/*
 * $RCSfile: MlibAffineRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:49 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.InterpolationTable;
import java.util.Map;
import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.opimage.TranslateIntOpImage;

/**
 * A <code>RIF</code> supporting the "Affine" operation in the
 * rendered image mode using MediaLib.
 *
 * @see javax.media.jai.operator.AffineDescriptor
 * @see MlibAffineOpImage
 * @see MlibScaleOpImage
 *
 * @since EA4
 */
public class MlibAffineRIF implements RenderedImageFactory {

    private static final float TOLERANCE = 0.01F;

    /** Constructor. */
    public MlibAffineRIF() {}

    /**
     * Creates a new instance of <code>MlibAffineOpImage</code> in
     * the rendered image mode.
     *
     * @param args  The source image, the <code>AffineTransform</code>,
     *              and the <code>Interpolation</code>.
     * @param hints  May contain rendering hints and destination image layout.
     */
    public RenderedImage create(ParameterBlock args,
                                RenderingHints hints) {
        /* Get ImageLayout and TileCache from RenderingHints. */
        ImageLayout layout = RIFUtil.getImageLayoutHint(hints);

        // Get operation parameters.
        AffineTransform transform =
            (AffineTransform)args.getObjectParameter(0);
        Interpolation interp = (Interpolation)args.getObjectParameter(1);
        double[] backgroundValues = (double[])args.getObjectParameter(2);

        RenderedImage source = args.getRenderedSource(0);
	
        if (!MediaLibAccessor.isMediaLibCompatible(args, layout) ||
            !MediaLibAccessor.hasSameNumBands(args, layout) ||
	    // Medialib cannot deal with source image having tiles with any
	    // dimension greater than or equal to 32768
	    source.getTileWidth() >= 32768 || 
	    source.getTileHeight() >= 32768) {
            return null;
        }

        SampleModel sm = source.getSampleModel();
        boolean isBilevel = (sm instanceof MultiPixelPackedSampleModel) &&
            (sm.getSampleSize(0) == 1) &&
            (sm.getDataType() == DataBuffer.TYPE_BYTE ||
             sm.getDataType() == DataBuffer.TYPE_USHORT ||
             sm.getDataType() == DataBuffer.TYPE_INT);
        if (isBilevel) {
            // Let Java code handle it, reformatting is slower
            return null;
        }

        // Get BorderExtender from hints if any.
        BorderExtender extender = RIFUtil.getBorderExtenderHint(hints);

        /* Get the affine transform. */
        double[] tr = new double[6];
        transform.getMatrix(tr);

        /*
         * Check and see if the affine transform is doing a copy.
         * If so call the copy operation.
         */
        if ((tr[0] == 1.0) &&
            (tr[3] == 1.0) &&
            (tr[2] == 0.0) &&
            (tr[1] == 0.0) &&
            (tr[4] == 0.0) &&
            (tr[5] == 0.0)) {
            /* It's a copy. */
            return new MlibCopyOpImage(source, hints, layout);
        }

        /*
         * Check and see if the affine transform is in fact doing
         * a Translate operation. That is a scale by 1 and no rotation.
         * In which case call translate. Note that only integer translate
         * is applicable. For non-integer translate we'll have to do the
         * affine.
         */
        if ((tr[0] == 1.0) &&
            (tr[3] == 1.0) &&
            (tr[2] == 0.0) &&
            (tr[1] == 0.0) &&
            (Math.abs(tr[4] - (int) tr[4]) < TOLERANCE) &&
            (Math.abs(tr[5] - (int) tr[5]) < TOLERANCE) &&
	    layout == null) { // TranslateIntOpImage can't deal with ImageLayout hint
            /* It's a integer translate. */
            return new TranslateIntOpImage(source,
					   hints,
                                           (int)tr[4],
                                           (int)tr[5]);
        }

        /*
         * Check and see if the affine transform is in fact doing
         * a Scale operation. In which case call Scale which is more
         * optimized than Affine.
         */
        if ((tr[0] > 0.0) &&
            (tr[2] == 0.0) &&
            (tr[1] == 0.0) &&
            (tr[3] > 0.0)) {
            /* It's a scale. */
            if (interp instanceof InterpolationNearest) {
                return new MlibScaleNearestOpImage(source,
						   extender,
                                                   hints,
                                                   layout,
                                                   (float)tr[0], // xScale
                                                   (float)tr[3], // yScale
                                                   (float)tr[4], // xTrans
                                                   (float)tr[5], // yTrans
                                                   interp);
            } else if (interp instanceof InterpolationBilinear) {
                return new MlibScaleBilinearOpImage(source,
                                                    extender,
                                                    hints,
                                                    layout,
                                                    (float)tr[0], // xScale
                                                    (float)tr[3], // yScale
                                                    (float)tr[4], // xTrans
                                                    (float)tr[5], // yTrans
                                                    interp);
            } else if (interp instanceof InterpolationBicubic ||
                       interp instanceof InterpolationBicubic2) {
                return new MlibScaleBicubicOpImage(source,
                                                   extender,
                                                   hints,
                                                   layout,
                                                   (float)tr[0], // xScale
                                                   (float)tr[3], // yScale
                                                   (float)tr[4], // xTrans
                                                   (float)tr[5], // yTrans
                                                   interp);
            } else if (interp instanceof InterpolationTable) {
                return new MlibScaleTableOpImage(source,
                                                 extender,
                                                 hints,
                                                 layout,
                                                 (float)tr[0], // xScale
                                                 (float)tr[3], // yScale
                                                 (float)tr[4], // xTrans
                                                 (float)tr[5], // yTrans
                                                 interp);
            } else {
                return null;
            }
        }

        /* Have to do an Affine. */
        if (interp instanceof InterpolationNearest) {
            return new MlibAffineNearestOpImage(source,
						extender,
                                                hints,
                                                layout,
                                                transform,
                                                interp,
                                                backgroundValues);
        } else if (interp instanceof InterpolationBilinear) {
            return new MlibAffineBilinearOpImage(source,
                                                 extender,
                                                 hints,
                                                 layout,
                                                 transform,
                                                 interp,
                                                 backgroundValues);
        } else if (interp instanceof InterpolationBicubic ||
                   interp instanceof InterpolationBicubic2) {
            return new MlibAffineBicubicOpImage(source,
                                                extender,
                                                hints,
                                                layout,
                                                transform,
                                                interp,
                                                backgroundValues);
        } else if (interp instanceof InterpolationTable) {
            return new MlibAffineTableOpImage(source,
                                              extender,
                                              hints,
                                              layout,
                                              transform,
                                              interp,
                                              backgroundValues);
        } else {
            return null;
        }
    }
}
