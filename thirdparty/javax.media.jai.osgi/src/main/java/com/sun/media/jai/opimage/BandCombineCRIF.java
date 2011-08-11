/*
 * $RCSfile: BandCombineCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:14 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import java.util.Map;

/**
 * A <code>CRIF</code> supporting the "BandCombine" operation in the
 * rendered and renderable image layers.
 *
 * @see javax.media.jai.operator.BandCombineDescriptor
 * @see BandCombineOpImage
 *
 *
 * @since EA3
 */
public class BandCombineCRIF extends CRIFImpl {

    /** Constructor. */
    public BandCombineCRIF() {
        super("bandcombine");
    }

    /**
     * Creates a new instance of <code>BandCombineOpImage</code>
     * in the rendered layer.
     *
     * @param args   The source image and the constants.
     * @param hints  Optionally contains destination image layout.
     */
    public RenderedImage create(ParameterBlock args,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        return new BandCombineOpImage(args.getRenderedSource(0),
                                      renderHints,
                                      layout,
                                      (double[][])args.getObjectParameter(0));
    }
}
