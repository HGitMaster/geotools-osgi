/*
 * $RCSfile: MlibShearRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:06 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.InterpolationTable;
import java.util.Map;
import javax.media.jai.BorderExtender;
import javax.media.jai.operator.ShearDescriptor;
import com.sun.media.jai.opimage.RIFUtil;

/**
 * A <code>RIF</code> supporting the "Shear" operation in the
 * rendered image mode using MediaLib.
 *
 * @see javax.media.jai.operator.ShearDescriptor
 * @see MlibAffineOpImage
 */
public class MlibShearRIF implements RenderedImageFactory {

    /** Constructor. */
    public MlibShearRIF() {}

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

        Interpolation interp = (Interpolation)args.getObjectParameter(4);

        RenderedImage source = args.getRenderedSource(0);

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

        float shear_amt = args.getFloatParameter(0);
        EnumeratedParameter shear_dir =
            (EnumeratedParameter)args.getObjectParameter(1);
        float xTrans = args.getFloatParameter(2);
        float yTrans = args.getFloatParameter(3);
        double[] backgroundValues = (double[])args.getObjectParameter(5);

        // Create the affine transform
        AffineTransform tr = new AffineTransform();

        if (shear_dir.equals(ShearDescriptor.SHEAR_HORIZONTAL)) {
            // SHEAR_HORIZONTAL
            tr.setTransform(1.0, 0.0, shear_amt, 1.0, xTrans, 0.0);
        } else {
            // SHEAR_VERTICAL
            tr.setTransform(1.0, shear_amt, 0.0, 1.0, 0.0, yTrans);
        }

        if (interp instanceof InterpolationNearest) {
            return new MlibAffineNearestOpImage(source, extender,
                                                hints, layout,
                                                tr,
                                                interp,
                                                backgroundValues);
        } else if (interp instanceof InterpolationBilinear) {
            return new MlibAffineBilinearOpImage(source,
                                                 extender, hints, layout,
                                                 tr,
                                                 interp,
                                                 backgroundValues);
        } else if (interp instanceof InterpolationBicubic ||
                   interp instanceof InterpolationBicubic2) {
            return new MlibAffineBicubicOpImage(source,
                                                extender, hints, layout,
                                                tr,
                                                interp,
                                                backgroundValues);
        } else if (interp instanceof InterpolationTable) {
            return new MlibAffineTableOpImage(source,
                                              extender, hints, layout,
                                              tr,
                                              interp,
                                              backgroundValues);
        } else {
           return null;
        }
    }
}
