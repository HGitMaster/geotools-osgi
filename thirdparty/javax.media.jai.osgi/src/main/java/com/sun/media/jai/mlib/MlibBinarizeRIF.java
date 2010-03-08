/*
 * $RCSfile: MlibBinarizeRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:51 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.RenderedImageFactory;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import java.util.Map;
import com.sun.media.jai.opimage.RIFUtil;

/**
 * A <code>RIF</code> supporting the "Binarize" operation in the
 * rendered image mode using MediaLib.
 *
 * @see javax.media.jai.operator.BinarizeDescriptor
 */
public class MlibBinarizeRIF implements RenderedImageFactory {

    /** Constructor. */
    public MlibBinarizeRIF() {}

    /**
     * Creates a new instance of <code>MlibBinarizeOpImage</code> in
     * the rendered image mode.
     *
     * @param args  The source image, thresh value
     * @param hints  May contain rendering hints and destination image layout.
     */
    public RenderedImage create(ParameterBlock args,
                                RenderingHints hints) {
        // Get the source and its SampleModel.
	RenderedImage source = args.getRenderedSource(0);
        SampleModel sm = source.getSampleModel();

        // Check that the source is single-banded and mediaLib compatible.
        // Ignore the layout because if it doesn't specify a bilevel image
        // then MlibBinarizeOpImage will revise it.
        if (!MediaLibAccessor.isMediaLibCompatible(args) ||
            sm.getNumBands() > 1) {
            return null;
        }

        // Get the threshold value.
        double thresh = args.getDoubleParameter(0);

	// java set all 0's or 1's fast
	if ((thresh > 255|| thresh <=0) && sm.getDataType()== DataBuffer.TYPE_BYTE ||
	    (thresh > Short.MAX_VALUE|| thresh <=0) && sm.getDataType()== DataBuffer.TYPE_SHORT||
	    (thresh > Integer.MAX_VALUE|| thresh <=0) && sm.getDataType()== DataBuffer.TYPE_INT)
	        return null;

        // Get ImageLayout from RenderingHints.
        ImageLayout layout = RIFUtil.getImageLayoutHint(hints);
        
	return new MlibBinarizeOpImage(source,
				       layout,
				       hints,
				       thresh);
    }
}
