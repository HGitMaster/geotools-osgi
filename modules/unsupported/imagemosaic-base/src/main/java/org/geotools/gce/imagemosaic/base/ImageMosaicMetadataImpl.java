package org.geotools.gce.imagemosaic.base;

import java.util.ArrayList;
import java.util.List;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ImageMosaicMetadataImpl implements ImageMosaicMetadata
{
    private String name;
    private int numLevels;
    private boolean hasBandAttributes;
    private boolean hasColorCorrection;
    private CoordinateReferenceSystem crs;
    private List<double[]> resolutions;
    private Envelope envelope;
    private boolean colorModelExpansion;

    public ImageMosaicMetadataImpl(int numLevels)
    {
        this.numLevels = numLevels;
        this.resolutions = new ArrayList<double[]>(numLevels);
    }

    public double getColorCorrection(int band)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean getColorModelExpansion()
    {
        return colorModelExpansion;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return crs;
    }

    public Envelope getEnvelope()
    {
        return envelope;
    }

    public String getName()
    {
        return name;
    }

    public int getNumLevels()
    {
        return numLevels;
    }

    public List<double[]> getResolutions()
    {
        return resolutions;
    }

    public boolean hasBandAttributes()
    {
        return hasBandAttributes;
    }

    public boolean hasColorCorrection()
    {
        return hasColorCorrection;
    }
    
    public void setHasBandAttributes(boolean hasBandAttributes)
    {
        this.hasBandAttributes = hasBandAttributes;
    }

    public void setHasColorCorrection(boolean hasColorCorrection)
    {
        this.hasColorCorrection = hasColorCorrection;
    }

    public void setCrs(CoordinateReferenceSystem crs)
    {
        this.crs = crs;
    }

    public void setResolutions(List<double[]> resolutions)
    {
        this.resolutions = resolutions;
    }

    public void setEnvelope(Envelope envelope)
    {
        this.envelope = envelope;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setColorModelExpansion(boolean expansion)
    {
        this.colorModelExpansion = expansion;
    }
}
