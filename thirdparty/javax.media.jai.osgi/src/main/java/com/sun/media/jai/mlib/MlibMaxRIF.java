/*
 * $RCSfile: MlibMaxRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:59 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.ImageLayout;
import java.util.Map;
import com.sun.media.jai.opimage.RIFUtil;

/**
 * A <code>RIF</code> supporting the "Max" operation in the
 * rendered image mode using MediaLib.
 *
 * @see javax.media.jai.operator.MaxDescriptor
 * @see MlibMaxOpImage
 *
 * @since 1.0
 *
 */
public class MlibMaxRIF implements RenderedImageFactory {

    /** Constructor. */
    public MlibMaxRIF() {}

    /**
     * Creates a new instance of <code>MlibMaxOpImage</code> in
     * the rendered image mode.
     *
     * @param args  The source images.
     * @param hints  May contain rendering hints and destination image layout.
     */
    public RenderedImage create(ParameterBlock args,
                                RenderingHints hints) {
        /* Get ImageLayout and TileCache from RenderingHints. */
        ImageLayout layout = RIFUtil.getImageLayoutHint(hints);
        

        if (!MediaLibAccessor.isMediaLibCompatible(args, layout) ||
            !MediaLibAccessor.hasSameNumBands(args, layout)) {
            return null;
        }

        return new MlibMaxOpImage(args.getRenderedSource(0),
                                  args.getRenderedSource(1),
                                  hints, layout);
    }
}
