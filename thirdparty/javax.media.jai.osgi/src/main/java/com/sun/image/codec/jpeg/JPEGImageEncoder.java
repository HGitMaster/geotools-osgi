package com.sun.image.codec.jpeg;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;

public class JPEGImageEncoder
{
    private final OutputStream out;

    public JPEGImageEncoder(OutputStream out)
    {
        this.out = out;
    }

    public void encode(BufferedImage bi) throws IOException
    {
        javax.imageio.ImageIO.write(bi, "jpg", out);
    }

    public void encode(Raster tile00) throws IOException
    {
        throw new IOException("Not implemented");
    }
}
