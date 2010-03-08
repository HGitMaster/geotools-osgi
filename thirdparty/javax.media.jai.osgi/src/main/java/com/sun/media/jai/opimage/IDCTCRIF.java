/*
 * $RCSfile: IDCTCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:28 $
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
 * A <code>CRIF</code> supporting the "IDCT" operation in the rendered
 * image layer.
 *
 * @since Beta
 * @see javax.media.jai.operator.IDCTDescriptor
 *
 */
public class IDCTCRIF extends CRIFImpl {

    /** Constructor. */
    public IDCTCRIF() {
        super("idct");
    }

    /**
     * Creates a new instance of an IDCT operator.
     *
     * @param paramBlock The scaling type.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        
        
        RenderedImage source = paramBlock.getRenderedSource(0);

        return new DCTOpImage(source, renderHints, layout, new FCT(false, 2));
    }
}
