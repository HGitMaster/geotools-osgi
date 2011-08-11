/*
 * $RCSfile: MosaicRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:36 $
 * $State: Exp $
 */package com.sun.media.jai.opimage;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Vector;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.operator.MosaicType;

/**
 * A <code>RIF</code> supporting the "Mosaic" operation in the rendered
 * image layer.
 *
 * @since JAI 1.1.2
 * @see javax.media.jai.operator.MosaicDescriptor
 */
public class MosaicRIF implements RenderedImageFactory {

    /** Constructor. */
    public MosaicRIF() {}

    /**
     * Renders a "Mosaic" operation node.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        return
            new MosaicOpImage(paramBlock.getSources(),
                              RIFUtil.getImageLayoutHint(renderHints),
                              renderHints,
                              (MosaicType)paramBlock.getObjectParameter(0),
                              (PlanarImage[])paramBlock.getObjectParameter(1),
                              (ROI[])paramBlock.getObjectParameter(2),
                              (double[][])paramBlock.getObjectParameter(3),
                              (double[])paramBlock.getObjectParameter(4));
    }
}
