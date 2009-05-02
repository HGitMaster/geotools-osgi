/*
 * $RCSfile: MlibBoxFilterRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/08/31 22:35:25 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Arrays;
import java.util.Map;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import com.sun.media.jai.opimage.RIFUtil;
import com.sun.medialib.mlib.*;

/**
 * A <code>RIF</code> supporting the "BoxFilter" operation in the rendered
 * image layer.
 *
 * @see javax.media.jai.operator.BoxFilterDescriptor
 * @see com.sun.media.jai.mlib.MlibSeparableConvolveOpImage
 *
 * @since EA4
 *
 */
public class MlibBoxFilterRIF extends MlibConvolveRIF {

    /** Constructor. */
    public MlibBoxFilterRIF() {}

    /**
     * Create a new instance of a convolution <code>OpImage</code>
     * representing a box filtering operation in the rendered mode.
     * This method satisfies the implementation of RIF.
     *
     * @param paramBlock  The source image and the convolution kernel.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get the operation parameters.
        int width = paramBlock.getIntParameter(0);
        int height = paramBlock.getIntParameter(1);
        int xOrigin = paramBlock.getIntParameter(2);
        int yOrigin = paramBlock.getIntParameter(3);

        // Allocate and initialize arrays.
        float[] dataH = new float[width];
        Arrays.fill(dataH, 1.0F/(float)width);
        float[] dataV = null;
        if(height == width) {
            dataV = dataH;
        } else {
            dataV = new float[height];
            Arrays.fill(dataV, 1.0F/(float)height);
        }

        // Construct a separable kernel.
        KernelJAI kernel = new KernelJAI(width, height, xOrigin, yOrigin,
                                         dataH, dataV);

        // Construct the parameters for the "Convolve" RIF.
        ParameterBlock args = new ParameterBlock(paramBlock.getSources());
        args.add(kernel);

        // Return the result of the "Convolve" RIF.
        return super.create(args, renderHints);
    }
}
