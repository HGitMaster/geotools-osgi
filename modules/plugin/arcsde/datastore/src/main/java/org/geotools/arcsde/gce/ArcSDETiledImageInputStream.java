/**
 * 
 */
package org.geotools.arcsde.gce;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

public class ArcSDETiledImageInputStream extends ImageInputStreamImpl implements ImageInputStream {

    private TileReader tileReader;

    public ArcSDETiledImageInputStream(TileReader tileReader) throws IOException {
        super();
        this.tileReader = tileReader;
    }

    /**
     * Returns <code>-1L</code> to indicate that the stream has unknown length. Subclasses must
     * override this method to provide actual length information.
     * 
     * @return -1L to indicate unknown length.
     */
    public long length() {
        return -1L;
    }

    @Override
    public int read() throws IOException {
        return tileReader.read();
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        return tileReader.read(buff, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        tileReader.close();
    }
}