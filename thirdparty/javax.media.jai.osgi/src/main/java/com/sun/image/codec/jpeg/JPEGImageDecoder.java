package com.sun.image.codec.jpeg;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

import sun.awt.image.codec.JPEGParam;

public class JPEGImageDecoder
{
    private final InputStream in;

    public JPEGImageDecoder(InputStream in)
    {
        this.in = in;
    }

    public BufferedImage decodeAsBufferedImage() throws IOException
    {
        return javax.imageio.ImageIO.read(in);
    }

    public Raster decodeAsRaster() throws IOException
    {
        return decodeAsBufferedImage().getRaster();
    }

    public JPEGDecodeParam getJPEGDecodeParam()
    {
        return new JPEGDecodeParam(JPEGParam.COLOR_ID_UNKNOWN, 3);
    }
}
