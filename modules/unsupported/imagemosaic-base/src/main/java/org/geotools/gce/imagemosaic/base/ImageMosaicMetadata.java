package org.geotools.gce.imagemosaic.base;

import java.util.List;

import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface ImageMosaicMetadata
{
    String getName();

    GeneralEnvelope getEnvelope();

    int getNumLevels();


    List<double[]> getResolutions();

    boolean hasBandAttributes();

    int getBand(int position);

    boolean hasColorCorrection();
    
    double getColorCorrection(int band);

    CoordinateReferenceSystem getCoordinateReferenceSystem();
    
    boolean getColorModelExpansion();
}
