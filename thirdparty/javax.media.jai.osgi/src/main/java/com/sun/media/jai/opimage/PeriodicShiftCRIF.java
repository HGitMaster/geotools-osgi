/*
 * $RCSfile: PeriodicShiftCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:40 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderableImageOp;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationBicubic;
import java.util.Map;

/**
 * This image factory supports image operator <code>PeriodicShiftOpImage</code>
 * in the rendered and renderable image layers.
 *
 * @see PeriodicShiftOpImage
 */
public class PeriodicShiftCRIF extends CRIFImpl {

    /** Constructor. */
    public PeriodicShiftCRIF() {
        super("periodicshift");
    }

    /**
     * Creates a new instance of <code>PeriodicShiftOpImage</code>
     * in the rendered layer. This method satisfies the
     * implementation of RIF.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        
        
        // Get the source image.
        RenderedImage source = paramBlock.getRenderedSource(0);

        // Get the translation parameters.
        int shiftX = paramBlock.getIntParameter(0);
        int shiftY = paramBlock.getIntParameter(1);

        // Return the OpImage.
        return new PeriodicShiftOpImage(source, renderHints, layout, shiftX, shiftY);
    }
}
