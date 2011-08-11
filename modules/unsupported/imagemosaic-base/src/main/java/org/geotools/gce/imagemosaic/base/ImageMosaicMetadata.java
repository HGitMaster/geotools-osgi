package org.geotools.gce.imagemosaic.base;

import java.util.List;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface ImageMosaicMetadata
{
    String getName();

    Envelope getEnvelope();

    int getNumLevels();

    List<double[]> getResolutions();

    boolean hasBandAttributes();

    boolean hasColorCorrection();
    
    double getColorCorrection(int band);

    CoordinateReferenceSystem getCoordinateReferenceSystem();
    
    boolean getColorModelExpansion();
}
