package com.sun.image.codec.jpeg;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.InputStream;
import java.io.OutputStream;

import sun.awt.image.codec.JPEGParam;

public class JPEGCodec
{
    public static JPEGImageDecoder createJPEGDecoder(InputStream in)
    {
        return new JPEGImageDecoder(in);
    }
    
    public static JPEGImageDecoder createJPEGDecoder(InputStream in, JPEGDecodeParam param)
    {
        return new JPEGImageDecoder(in);
    }

    public static JPEGEncodeParam getDefaultJPEGEncodeParam(BufferedImage bi)
    {
        return new JPEGEncodeParam(JPEGParam.COLOR_ID_UNKNOWN, 3);
    }

    public static JPEGImageEncoder createJPEGEncoder(OutputStream output,
            JPEGEncodeParam j2dEP)
    {
        return new JPEGImageEncoder(output);
    }


    public static JPEGEncodeParam getDefaultJPEGEncodeParam(Raster tile00,
            int jpegColorID)
    {
        return new JPEGEncodeParam(JPEGParam.COLOR_ID_UNKNOWN, 3);
    }
}
