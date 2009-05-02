/*
 * $RCSfile: MlibTranslateRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:08 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationTable;
import java.util.Map;
import javax.media.jai.BorderExtender;
import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.opimage.TranslateIntOpImage;

/**
 * A <code>RIF</code> supporting the "Translate" operation in the
 * rendered image mode using MediaLib.
 *
 * @see javax.media.jai.operator.TranslateDescriptor
 * @see MlibScaleOpImage
 */
public class MlibTranslateRIF implements RenderedImageFactory {

    private static final float TOLERANCE = 0.01F;

    /** Constructor. */
    public MlibTranslateRIF() {}

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
        RenderedImage source = args.getRenderedSource(0);
        float xTrans = args.getFloatParameter(0);
        float yTrans = args.getFloatParameter(1);
        Interpolation interp = (Interpolation) args.getObjectParameter(2);

	/* Get ImageLayout and TileCache from RenderingHints. */
	ImageLayout layout = RIFUtil.getImageLayoutHint(hints);

        if ((Math.abs(xTrans - (int)xTrans) < TOLERANCE) &&
            (Math.abs(yTrans - (int)yTrans) < TOLERANCE) &&
	    layout == null) { // TranslateIntOpImage can't deal with ImageLayout hint
            return new TranslateIntOpImage(source,
					   hints,
                                           (int)xTrans,
                                           (int)yTrans);
        } else {
            
            if (!MediaLibAccessor.isMediaLibCompatible(args, layout) ||
                !MediaLibAccessor.hasSameNumBands(args, layout) ||
		// Medialib cannot deal with source image having tiles with any
		// dimension greater than or equal to 32768
		source.getTileWidth() >= 32768 || 
		source.getTileHeight() >= 32768) {
                return null;
            }

            /* Get BorderExtender from hints if any. */
            BorderExtender extender = RIFUtil.getBorderExtenderHint(hints);

            /*
             * Call the Scale operation, since it encapsulates Translate
             * and is better optimized than Affine.
             */
            float xScale = 1.0F;
            float yScale = 1.0F;
            if (interp instanceof InterpolationNearest) {
                return new MlibScaleNearestOpImage(source, extender,
                                                   hints, layout,
                                                   xScale, yScale,
                                                   xTrans, yTrans,
                                                   interp);
            } else if (interp instanceof InterpolationBilinear) {
                return new MlibScaleBilinearOpImage(source,
                                                    extender, hints, layout,
                                                    xScale, yScale,
                                                    xTrans, yTrans,
                                                    interp);
            } else if (interp instanceof InterpolationBicubic ||
                       interp instanceof InterpolationBicubic2) {
                return new MlibScaleBicubicOpImage(source,
                                                   extender, hints, layout,
                                                   xScale, yScale,
                                                   xTrans, yTrans,
                                                   interp);
            } else if (interp instanceof InterpolationTable) {
                return new MlibScaleTableOpImage(source,
                                                 extender, hints, layout,
                                                 xScale, yScale,
                                                 xTrans, yTrans,
                                                 interp);
            } else {
                return null;
            }
        }
    }
}
