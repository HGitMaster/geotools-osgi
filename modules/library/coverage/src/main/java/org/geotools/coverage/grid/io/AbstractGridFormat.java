/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.grid.io;

import java.util.Map;

import javax.imageio.ImageWriteParam;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * AbstractGridFormat is a convenience class so subclasses only need to populate
 * a Map class and set the read and write parameter fields.
 * 
 * 
 * 
 * For example the ArcGridFormat has the following method which sets up all the
 * required information: <code>private void setInfo(){ HashMap info=new
 * HashMap(); info.put("name", "ArcGrid"); info.put("description", "Arc Grid
 * Coverage Format"); info.put("vendor", "Geotools"); info.put("docURL",
 * "http://gdal.velocet.ca/projects/aigrid/index.html"); info.put("version",
 * "1.0");  mInfo=info;  readParameters=new GeneralParameterDescriptor[2];
 * readParameters[0]=ArcGridOperationParameter.getGRASSReadParam();
 * readParameters[0]=ArcGridOperationParameter.getCompressReadParam();
 * writeParameters=new GeneralParameterDescriptor[2];
 * writeParameters[0]=ArcGridOperationParameter.getGRASSWriteParam();
 * writeParameters[0]=ArcGridOperationParameter.getCompressWriteParam();
 * }</code>
 * 
 * @author jeichar
 * @author Simone Giannecchini, GeoSolutions
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.3.x/module/main/src/org/geotools/data/coverage/grid/AbstractGridFormat.java $
 */
@SuppressWarnings("deprecation")
public abstract class AbstractGridFormat implements Format {

	/**
	 * The Map object is used by the information methods(such as getName()) as a
	 * data source. The keys in the Map object (for the associated method) are
	 * as follows: getName() key = "name" value type=String getDescription() key =
	 * "description" value type=String getVendor() key = "vendor" value
	 * type=String getDocURL() key = "docURL" value type=String getVersion() key =
	 * "version" value type=String Naturally, any methods that are overridden
	 * need not have an entry in the Map
	 */
	protected Map<String,String> mInfo;

	/**
	 * {@link ParameterValueGroup} that controls the reading process for a
	 * {@link GridCoverageReader} through the
	 * {@link GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])}
	 * method.
	 */
	protected ParameterValueGroup readParameters;

	/**
	 * {@link ParameterValueGroup} that controls the writing process for a
	 * {@link GridCoverageWriter} through the
	 * {@link GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage, org.opengis.parameter.GeneralParameterValue[])}
	 * method.
	 */
	protected ParameterValueGroup writeParameters;

	/**
	 * Default {@link CoordinateReferenceSystem} used by all the plugins.
	 */
	private static CoordinateReferenceSystem crs;
	static {
		try {
			crs = CRS.decode("EPSG:4326", true);
		} catch (NoSuchAuthorityCodeException e) {
			crs = DefaultGeographicCRS.WGS84;
		} catch (FactoryException e) {
			crs = DefaultGeographicCRS.WGS84;
		}
	}

	/**
	 * This {@link GeneralParameterValue} cacn be provided to the
	 * {@link GridCoverageReader}s through the
	 * {@link GridCoverageReader#read(GeneralParameterValue[])} methid in order
	 * to pick up the best matching resolution level and (soon) the best
	 * matching area.
	 */
	public static final DefaultParameterDescriptor<GridGeometry2D> READ_GRIDGEOMETRY2D = new DefaultParameterDescriptor<GridGeometry2D>(
			"ReadGridGeometry2D", GridGeometry2D.class, null, null);

	/**
	 * This {@link GeneralParameterValue} cacn be provided to the
	 * {@link GridCoverageWriter}s through the
	 * {@link GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage, GeneralParameterValue[])}
	 * method in order to control the writing process in terms of compression,
	 * tiling, etc.GridGeometry2D
	 */
	public static final DefaultParameterDescriptor<GeoToolsWriteParams> GEOTOOLS_WRITE_PARAMS = new DefaultParameterDescriptor<GeoToolsWriteParams>(
			"WriteParameters", GeoToolsWriteParams.class, null, null);


	/**
	 * This {@link GeneralParameterValue} can be provided to the
	 * {@link GridCoverageReader}s through the
	 * {@link GridCoverageReader#read(GeneralParameterValue[])} method in order
	 * to specify the type of image read operation requested: using a JAI 
	 * ImageRead operation (leveraging on Deferred Execution Model, 
     * Tile Caching,...), or the direct {@code ImageReader}'s read methods.
	 */
	public static final DefaultParameterDescriptor<Boolean> USE_JAI_IMAGEREAD = new DefaultParameterDescriptor<Boolean>(
			Hints.USE_JAI_IMAGEREAD.toString(), Boolean.class, new Boolean[] {
					Boolean.TRUE, Boolean.FALSE }, Boolean.TRUE);
	
	/**
	 * This {@link GeneralParameterValue} can be provided to the
	 * {@link GridCoverageReader}s through the
	 * {@link GridCoverageReader#read(GeneralParameterValue[])} method in order
	 * to specify the policy a reader should adopt when chhosing the right
	 * overview during a read operation.
	 */
	public static final DefaultParameterDescriptor<OverviewPolicy> OVERVIEW_POLICY = new DefaultParameterDescriptor<OverviewPolicy>(
			Hints.OVERVIEW_POLICY.toString(), OverviewPolicy.class, new OverviewPolicy[] {
					OverviewPolicy.IGNORE,
					OverviewPolicy.NEAREST,
					OverviewPolicy.QUALITY,
					OverviewPolicy.SPEED },
			OverviewPolicy.QUALITY);
	
	/**
	 * @see org.opengis.coverage.grid.Format#getName()
	 */
	public String getName() {
		return (String) mInfo.get("name");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDescription()
	 */
	public String getDescription() {
		return (String) mInfo.get("description");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVendor()
	 */
	public String getVendor() {
		return (String) mInfo.get("vendor");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDocURL()
	 */
	public String getDocURL() {
		return (String) mInfo.get("docURL");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVersion()
	 */
	public String getVersion() {
		return (String) mInfo.get("version");
	}

	/**
	 * Gets a {@link GridCoverageReader} for this format able to create
	 * coverages out of the <code>source</code> object.
	 * 
	 * <p>
	 * In case this {@link Format} cannot reader the provided
	 * <code>source</code> object <code>null</code> is returned.
	 * 
	 * @param source
	 *            The source object to parse.
	 * @return A reader for this {@link Format} or null.
	 */
	abstract public AbstractGridCoverage2DReader getReader(Object source);

	/**
	 * 
	 * Gets a {@link GridCoverageReader} for this format able to create
	 * coverages out of the <code>source</code> object using the provided
	 * <code>hints</code>.
	 * 
	 * <p>
	 * In case this {@link Format} cannot reader the provided
	 * <code>source</code> object <code>null</code> is returned.
	 * 
	 * @param source
	 *            The source object to parse. *
	 * @param hints
	 *            The {@link Hints} to use when trying to instantiate this
	 *            reader.
	 * @return A reader for this {@link Format} or null.
	 */
	abstract public AbstractGridCoverage2DReader getReader(Object source, Hints hints);

	/**
	 * Retrieves a {@link GridCoverageWriter} suitable for writing to the
	 * provided <code>destination</code> with this format.
	 * 
	 * <p>
	 * In case no writers are availaible <code>null</code> is returned.
	 * 
	 * @param destination
	 *            The destinatin where to write.
	 * @return A {@link GridCoverageWriter} suitable for writing to the provided
	 *         <code>destination</code> with this format.
	 */
	abstract public GridCoverageWriter getWriter(Object destination);

	/**
	 * Tells me if this {@link Format} can read the provided <code>input</code>.
	 * 
	 * 
	 * @param input
	 *            The input object to test for suitablilty.
	 * @return True if this format can read this object, False otherwise.
	 */
	public abstract boolean accepts(Object input);

	/**
	 * @see org.geotools.data.coverage.grid.Format#equals(org.geotools.data.coverage.grid.Format)
	 */
        public boolean equals(Format f) {
		if (f.getClass() == getClass()) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.Format#getReadParameters()
	 */
	public ParameterValueGroup getReadParameters() {
		if (this.readParameters == null)
			throw new UnsupportedOperationException(
					"This format does not support usage of read parameters.");
		return (ParameterValueGroup) this.readParameters.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.Format#getWriteParameters()
	 */
	public ParameterValueGroup getWriteParameters() {
		if (this.writeParameters == null)
			throw new UnsupportedOperationException(
					"This format does not support usage of write parameters.");
		return (ParameterValueGroup) this.writeParameters.clone();
	}

	/**
	 * getDefaultCRS
	 * 
	 * This method provides the user with a default crs WGS84
	 */
	static public CoordinateReferenceSystem getDefaultCRS() {
		return crs;
	}

	/**
	 * Returns an instance of {@link ImageWriteParam} that can be used to
	 * control a subsequent
	 * {@link GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage, org.opengis.parameter.GeneralParameterValue[])};
	 * <p>
	 * Be careful with using the {@link ImageWriteParam} since their usage is
	 * still experimental.
	 * 
	 * @return an instance of {@link ImageWriteParam}.
	 */
	public abstract GeoToolsWriteParams getDefaultImageIOWriteParameters();
}
