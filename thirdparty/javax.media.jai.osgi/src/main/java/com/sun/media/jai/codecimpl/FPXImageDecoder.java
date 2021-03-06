/*
 * $RCSfile: FPXImageDecoder.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.2 $
 * $Date: 2006/08/22 00:12:04 $
 * $State: Exp $
 */
package com.sun.media.jai.codecimpl;
import java.awt.image.RenderedImage;
import java.io.InputStream;
import java.io.IOException;
import com.sun.media.jai.codec.FPXDecodeParam;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageDecodeParam;
import com.sun.media.jai.codec.ImageDecoderImpl;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codecimpl.fpx.FPXImage;

/**
 * @since EA3
 */
public class FPXImageDecoder extends ImageDecoderImpl {

    public FPXImageDecoder(SeekableStream input,
                           ImageDecodeParam param) {
        super(input, param);
    }

    public RenderedImage decodeAsRenderedImage(int page) throws IOException {
        if (page != 0) {
            throw new IOException(JaiI18N.getString("FPXImageDecoder0"));
        }
        try {
            return new FPXImage(input, (FPXDecodeParam)param);
        } catch(Exception e) {
            throw CodecUtils.toIOException(e);
        }
    }
}
