/*
 * $RCSfile: InvertCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:29 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import java.util.Map;

/**
 * A <code>CRIF</code> supporting the "Invert" operation in the
 * rendered and renderable image layer.
 *
 * @see javax.media.jai.operator.InvertDescriptor
 * @see InvertOpImage
 *
 */
public class InvertCRIF extends CRIFImpl {

    /** Constructor. */
    public InvertCRIF() {
        super("invert");
    }

    /**
     * Creates a new instance of <code>InvertpImage</code> in the rendered
     * layer.
     *
     * @param paramBlock  The source images to be inverted.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        
        
        return new InvertOpImage(paramBlock.getRenderedSource(0),
                                 renderHints, layout);
    }
}
