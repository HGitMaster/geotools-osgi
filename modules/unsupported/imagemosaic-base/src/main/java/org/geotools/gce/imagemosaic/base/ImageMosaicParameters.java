package org.geotools.gce.imagemosaic.base;

import java.awt.Color;
import java.awt.Rectangle;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

public class ImageMosaicParameters
{
    private GeneralEnvelope requestedEnvelope;
    private Rectangle bounds;
    private Color inputTransparentColor;
    private Color outputTransparentColor;
    private double inputImageThreshold;
    private boolean blend;
    private OverviewPolicy overviewPolicy;
    private int maxNumTiles;
    
    public ImageMosaicParameters()
    {
        inputTransparentColor = AbstractImageMosaicFormat.INPUT_TRANSPARENT_COLOR
                .getDefaultValue();
        
        outputTransparentColor = AbstractImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
                .getDefaultValue();
        
        inputImageThreshold = AbstractImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
                .getDefaultValue();
    }

    public void evaluate(GeneralParameterValue[] params)
    {
        if (params == null)
            return;

        for (int i = 0; i < params.length; i++)
        {
            ParameterValue<?> param = (ParameterValue<?>) params[i];
            String name = param.getDescriptor().getName().getCode();
            if (name.equals(AbstractImageMosaicFormat.READ_GRIDGEOMETRY2D.getName()
                    .toString()))
            {
                GridGeometry2D gg = (GridGeometry2D) param.getValue();
                requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
                bounds = gg.getGridRange2D().getBounds();
                continue;
            }
            if (name.equals(AbstractImageMosaicFormat.INPUT_TRANSPARENT_COLOR.getName()
                    .toString()))
            {
                inputTransparentColor = (Color) param.getValue();
                continue;

            }
            if (name.equals(AbstractImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
                    .getName().toString()))
            {
                inputImageThreshold = ((Double) param.getValue()).doubleValue();
                continue;

            }
            if (name.equals(AbstractImageMosaicFormat.FADING.getName().toString()))
            {
                blend = ((Boolean) param.getValue()).booleanValue();
                continue;

            }
            if (name.equals(AbstractImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
                    .getName().toString()))
            {
                outputTransparentColor = (Color) param.getValue();
                continue;

            }
            if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName()
                    .toString()))
            {
                overviewPolicy = (OverviewPolicy) param.getValue();
                continue;
            }
            if (name.equals(AbstractImageMosaicFormat.MAX_ALLOWED_TILES.getName()
                    .toString()))
            {
                maxNumTiles = param.intValue();
                continue;
            }
        }
    }


    public GeneralEnvelope getRequestedEnvelope()
    {
        return requestedEnvelope;
    }

    public void setRequestedEnvelope(GeneralEnvelope requestedEnvelope)
    {
        this.requestedEnvelope = requestedEnvelope;
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public void setBounds(Rectangle bounds)
    {
        this.bounds = bounds;
    }

    public Color getInputTransparentColor()
    {
        return inputTransparentColor;
    }

    public void setInputTransparentColor(Color inputTransparentColor)
    {
        this.inputTransparentColor = inputTransparentColor;
    }

    public Color getOutputTransparentColor()
    {
        return outputTransparentColor;
    }

    public void setOutputTransparentColor(Color outputTransparentColor)
    {
        this.outputTransparentColor = outputTransparentColor;
    }

    public double getInputImageThreshold()
    {
        return inputImageThreshold;
    }

    public void setInputImageThreshold(double inputImageThreshold)
    {
        this.inputImageThreshold = inputImageThreshold;
    }

    public boolean isBlend()
    {
        return blend;
    }

    public void setBlend(boolean blend)
    {
        this.blend = blend;
    }

    public OverviewPolicy getOverviewPolicy()
    {
        return overviewPolicy;
    }

    public void setOverviewPolicy(OverviewPolicy overviewPolicy)
    {
        this.overviewPolicy = overviewPolicy;
    }

    public int getMaxNumTiles()
    {
        return maxNumTiles;
    }

    public void setMaxNumTiles(int maxNumTiles)
    {
        this.maxNumTiles = maxNumTiles;
    }
}
