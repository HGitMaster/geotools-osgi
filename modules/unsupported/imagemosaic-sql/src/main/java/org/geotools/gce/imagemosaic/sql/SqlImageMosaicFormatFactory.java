package org.geotools.gce.imagemosaic.sql;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.imagemosaic.base.AbstractImageMosaicFormatFactory;
import org.opengis.coverage.grid.Format;

@SuppressWarnings("deprecation")
public class SqlImageMosaicFormatFactory extends AbstractImageMosaicFormatFactory
{

    public AbstractGridFormat createFormat()
    {
        return new SqlImageMosaicFormat();
    }

}
