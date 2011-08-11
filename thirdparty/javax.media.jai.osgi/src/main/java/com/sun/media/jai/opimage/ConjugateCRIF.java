/*
 * $RCSfile: ConjugateCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:19 $
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
 * A <code>CRIF</code> supporting the "Conjugate" operation in the
 * rendered and renderable image layer.
 *
 * @see javax.media.jai.operator.ConjugateDescriptor
 * @see ConjugateOpImage
 *
 * @since EA4
 */
public class ConjugateCRIF extends CRIFImpl {

    /** Constructor. */
    public ConjugateCRIF() {
        super("conjugate");
    }

    /**
     * Creates a new instance of <code>ConjugateOpImage</code> in the rendered
     * layer.
     *
     * @param paramBlock  The source image of which to take the conjugate.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        return new ConjugateOpImage(paramBlock.getRenderedSource(0),
                                    renderHints, layout);
    }
}
