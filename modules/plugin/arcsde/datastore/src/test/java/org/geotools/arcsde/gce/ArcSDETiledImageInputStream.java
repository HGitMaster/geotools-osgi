/**
 * 
 */
package org.geotools.arcsde.gce;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.imageio.RasterCellType;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

public class ArcSDETiledImageInputStream extends ImageInputStreamImpl
        implements ImageInputStream {

    private final SeConnection conn;

    private final SeRasterAttr attr;

    private final SeRow row;

    private SeRasterTile currTile;

    private byte[] currTileData;

    private int currTileDataIndex;

    private int totalReadCount;

    private int tileDataLength;

    private final RasterCellType cellType;

    public static class TileReader{
        
    }
    
    public ArcSDETiledImageInputStream(final SeConnection conn,
            final RasterCellType cellType, final SeRasterAttr attr, final SeRow row)
            throws IOException {
        super();
        this.conn = conn;
        this.attr = attr;
        this.row = row;
        this.cellType = cellType;
    }

    private void setRead(int readCount) {
        currTileDataIndex += readCount;
        totalReadCount += readCount;
    }

    private byte[] fetchTile() throws IOException {
        byte[] tileData = null;
        try {
            currTile = row.getRasterTile();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        if (currTile != null) {
            // wasn't it the last tile in the stream?
            // get pixel data AND bitmask data appended, if exist. Pixel data packed into
            // byte[] according to pixel type. This is the only generic way of getting at
            // the pixel data regardless of its data type
            tileData = currTile.getPixelData();
            // cut off the bitmask data from the pixel data length
            tileDataLength = currTile.getNumPixels() * (cellType.getBitsPerSample() / 8);
            System.err.println("Reading from tile " + currTile + ". Bitmask: "
                    + currTile.getBitMaskData().length);
        }
        return tileData;
    }

    private byte[] getTileData() throws IOException {
        if (currTileData == null) {
            currTileData = fetchTile();
            if (currTile == null) {
                return null;
            }
            setRead(0);
        }
        if (currTileDataIndex == tileDataLength) {
            currTileData = fetchTile();
            if (currTile == null) {
                return null;
            }
            currTileDataIndex = 0;
        }
        return currTileData;
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
        byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        int currByte = data[currTileDataIndex] & 0xFF;
        setRead(1);
        return currByte;
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        int readCount = 0;
        byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        int remaining = len;
        while (remaining > 0 && data != null) {
            int available = data.length - currTileDataIndex;
            if (available >= remaining) {
                System.arraycopy(data, currTileDataIndex, buff, off + readCount, remaining);
                readCount += remaining;
                setRead(remaining);
                remaining = 0;
            } else {
                System.arraycopy(data, currTileDataIndex, buff, off + readCount, available);
                remaining -= available;
                readCount += available;
                setRead(available);
                data = getTileData();
            }
        }
        return readCount;
    }

    @Override
    public void close() throws IOException {
        super.close();
        System.err.println("closing arcsde image input stream, read: " + totalReadCount);
        try {
            conn.close();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }
}