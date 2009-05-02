/*
 * $RCSfile: TransposeCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:46 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.CRIFImpl;
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import java.util.Map;

/**
 * @see TransposeOpImage 
 * @see TransposeBinaryOpImage 
 */
public class TransposeCRIF extends CRIFImpl {

    /** Constructor. */
    public TransposeCRIF() {
        super("transpose");
    }

    /**
     * Creates a Tranpose operation.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        RenderedImage source = paramBlock.getRenderedSource(0);

        EnumeratedParameter type =
            (EnumeratedParameter)paramBlock.getObjectParameter(0);

        SampleModel sm = source.getSampleModel();
        if ((sm instanceof MultiPixelPackedSampleModel) &&
            (sm.getSampleSize(0) == 1) &&
            (sm.getDataType() == DataBuffer.TYPE_BYTE || 
             sm.getDataType() == DataBuffer.TYPE_USHORT || 
             sm.getDataType() == DataBuffer.TYPE_INT)) {
            return new TransposeBinaryOpImage(source, renderHints, layout,
                                              type.getValue());
        } else {
            return new TransposeOpImage(source, renderHints, layout,
                                        type.getValue());
        }
    }
}
