/*
 * $RCSfile: GradientRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:27 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.util.Map;

/**
 * @see GradientOpImage
 */
public class GradientRIF implements RenderedImageFactory {

    /** Constructor. */
    public GradientRIF() {}

    /**
     * Create a new instance of GradientOpImage in the rendered layer.
     * This method satisfies the implementation of RIF.
     *
     * @param paramBlock  The source image and the gradient's
     *                    horizontal kernel & vertical kernel.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
         // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        // Get BorderExtender from renderHints if any.
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);
        
        RenderedImage source = paramBlock.getRenderedSource(0);

        // Get the Horizontal & Vertical kernels
        KernelJAI kern_h = (KernelJAI)paramBlock.getObjectParameter(0);
        KernelJAI kern_v = (KernelJAI)paramBlock.getObjectParameter(1);
        
        return new GradientOpImage(source,
                                   extender,
                                   renderHints,
                                   layout,
                                   kern_h,
                                   kern_v);
    }
}
