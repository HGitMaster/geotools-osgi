/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.hdf;

// J2SE dependencies
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;

// Geomatys dependencies
import org.geotools.image.Palette;


/**
 * Fournisseurs de décodeur d'images HDF contenant des données de concentrations en chlorophylle-a.
 *
 * @version $Id: CHL_ReaderSpi.java 30680 2008-06-13 10:22:22Z acuster $
 * @author Antoine Hnawia
 * @author Martin Desruisseaux (IRD)
 */
public class CHL_ReaderSpi extends AbstractReaderSpi {
    /**
     * Nom d'un dataset "chlorophylle".
     */
    private static final String DATASET_NAME = "l3m_data";

    /**
     * Nom de la palette de couleur pour la chlorophylle.
     */
    private static final String PALETTE_NAME = "seawifs";

    /**
     * Taille du model de couleur pour la CHL.
     */
    private static final int PALETTE_SIZE = 65536;

    /**
     * Construit un nouveau fournisseur de service.
     */
    public CHL_ReaderSpi() {
        super(DATASET_NAME, null);
        names = new String[] {
            "HDF-CHL"
        };
        MIMETypes = new String[] {
            "application/x-hdf/CHL-8d",
            "application/x-hdf/CHL-mo"
        };
        suffixes = new String[] {
            "L3m_8D_XS_CHLO_9",
            "L3m_MO_XS_CHLO_9"
        };
    }

    /**
     * Construit un décodeur d'image HDF.
     */
    public ImageReader createReaderInstance(final Object extension) throws IOException {
        final DefaultReader reader = new DefaultReader(this);
        reader.setBitCount(16, 0);
        return reader;
    }

    /**
     * Retourne le type d'image que créeront les décodeurs HDF.
     */
    protected ImageTypeSpecifier getRawImageType() throws IOException {
        return Palette.forNodataLast(PALETTE_NAME, PALETTE_SIZE);
    }
}
