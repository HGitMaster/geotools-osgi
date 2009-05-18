package org.geotools.gce.imagemosaic.base;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ImageMosaicMetadataImpl implements ImageMosaicMetadata
{
    private int numLevels;
    private boolean hasBandAttributes;
    private boolean hasColorCorretion;
    private CoordinateReferenceSystem crs;
    private List<double[]> resolutions;

    public ImageMosaicMetadataImpl(int numLevels)
    {
        this.numLevels = numLevels;
        this.resolutions = new ArrayList<double[]>(numLevels);
    }

    public int getBand(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getColorCorrection(int band)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean getColorModelExpansion()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return crs;
    }

    public GeneralEnvelope getEnvelope()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNumLevels()
    {
        return numLevels;
    }

    public List<double[]> getResolutions()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasBandAttributes()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasColorCorrection()
    {
        // TODO Auto-generated method stub
        return false;
    }
}
