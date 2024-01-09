package sun.awt.image.codec;

import com.sun.image.codec.jpeg.JPEGQTable;

public class JPEGParam
{
    public static final String APP0_MARKER = null;

    public static final int COLOR_ID_UNKNOWN = 0;

    public static final int COLOR_ID_GRAY = 1;

    public static final int COLOR_ID_RGB = 2;

    public static final int COLOR_ID_YCbCr = 3;

    public static final int COLOR_ID_CMYK = 4;

    private int colodIdGray;

    private int nbands;

    public JPEGParam(int colorIdGray, int nbands)
    {
        this.colodIdGray = colorIdGray;
        this.nbands = nbands;
    }

    public int getHorizontalSubsampling(int i)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getVerticalSubsampling(int i)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public JPEGQTable getQTable(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getQTableComponentMapping(int i)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object isTableInfoValid()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object isImageInfoValid()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getRestartInterval()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getMarker(String app0Marker)
    {
        // TODO Auto-generated method stub
        return null;
    }


    public void setHorizontalSubsampling(int i, int val)
    {
        // TODO Auto-generated method stub
        
    }

    public void setVerticalSubsampling(int i, int val)
    {
        // TODO Auto-generated method stub
        
    }

    public void setQTableComponentMapping(int i, int val)
    {
        // TODO Auto-generated method stub
        
    }

    public void setQTable(int val, JPEGQTable jpegqTable)
    {
        // TODO Auto-generated method stub
        
    }

    public void setQuality(float fval, boolean b)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRestartInterval(int val)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageInfoValid(boolean b)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTableInfoValid(boolean b)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMarkerData(String app0Marker, Object object)
    {
        // TODO Auto-generated method stub
        
    }

    public int getWidth()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getHeight()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setWidth(int width)
    {
        // TODO Auto-generated method stub
        
    }

    public void setHeight(int height)
    {
        // TODO Auto-generated method stub
        
    }

}
