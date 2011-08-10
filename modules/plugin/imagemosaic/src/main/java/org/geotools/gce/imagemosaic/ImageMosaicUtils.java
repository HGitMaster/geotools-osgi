/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic;

import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.gce.imagemosaic.IndexBuilder.ExceptionEvent;
import org.geotools.gce.imagemosaic.IndexBuilder.IndexBuilderConfiguration;
import org.geotools.gce.imagemosaic.IndexBuilder.ProcessingEvent;
import org.geotools.gce.imagemosaic.IndexBuilder.ProcessingEventListener;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.geometry.BoundingBox;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;

/**
 * Sparse utilities for the various mosaic classes. I use them to extract complex code from other places.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 *
 */
class ImageMosaicUtils {
	/**{@link AffineTransform} that can be used to go from an image datum placed at the center of pixels to one that is placed at ULC.*/
	final static AffineTransform CENTER_TO_CORNER= AffineTransform.getTranslateInstance(
			PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER),
			PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER));
	/**{@link AffineTransform} that can be used to go from an image datum placed at the ULC corner of pixels to one that is placed at center.*/
	final static AffineTransform CORNER_TO_CENTER= AffineTransform.getTranslateInstance(
			-PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER),
			-PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER));
	/**
	 * Very simple bean to hold the configuration of the mosaic.
	 * 
	 * @author Simone Giannecchini, GeoSolutions S.A.S.
	 * @author Stefan Alfons Krueger (alfonx), Wikisquare.de : Support for jar:file:foo.jar/bar.properties URLs
	 */
	static final class MosaicConfigurationBean {

		/**
		 * <code>true</code> it tells us if the mosaic points to absolute paths or to relative ones. (in case of <code>false</code>).
		 */
		private boolean absolutePath;
		
		/**
		 * <code>true</code> if we need to expand to RGB(A) the single tiles in case they use a different {@link IndexColorModel}.
		 */
		private boolean expandToRGB;
		
		/** The envelope for the whole mosaic.**/
		private Envelope2D envelope2D;
		
		/** OverviewLevel levels */
		private double[][] levels;
		
		/** name for the mosaic.*/
		private String name;
		
		/** number of levels*/
		private int levelsNum;
		
		/** location attribute name*/
		private String locationAttribute;
		
		/**Suggested SPI for the various tiles. May be null.**/
		private String suggestedSPI;
		

		/**
		 * @return the suggestedSPI
		 */
		public String getSuggestedSPI() {
			return suggestedSPI;
		}
		/**
		 * @param suggestedSPI the suggestedSPI to set
		 */
		public void setSuggestedSPI(String suggestedSPI) {
			this.suggestedSPI = suggestedSPI;
		}
		
		public boolean isAbsolutePath() {
			return absolutePath;
		}
		public void setAbsolutePath(boolean absolutePath) {
			this.absolutePath = absolutePath;
		}
		public boolean isExpandToRGB() {
			return expandToRGB;
		}
		public void setExpandToRGB(boolean expandToRGB) {
			this.expandToRGB = expandToRGB;
		}
		public Envelope2D getEnvelope2D() {
			return envelope2D;
		}
		public void setEnvelope2D(Envelope2D envelope2D) {
			this.envelope2D = envelope2D;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getLevelsNum() {
			return levelsNum;
		}
		public void setLevelsNum(int levelsNum) {
			this.levelsNum = levelsNum;
		}
		public double[][] getLevels() {
			return levels.clone();
		}
		public void setLevels(double[][] levels) {
			this.levels = levels.clone();
		}
		public String getLocationAttribute() {
			return locationAttribute;
		}
		public void setLocationAttribute(String locationAttribute) {
			this.locationAttribute = locationAttribute;
		}
		
		


	}
	
	/**
	 * Logger. 
	 */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger(ImageMosaicUtils.class.toString());
	/**
	 * Default wildcard for creating mosaics.
	 */
	static final String DEFAULT_WILCARD = "*.*";
	
	/**
	 * Default path behavior with respect to absolute paths.
	 */
	static final boolean DEFAULT_PATH_BEHAVIOR = false;
	
	/**
	 * Cached instance of {@link URLImageInputStreamSpi} for creating {@link ImageInputStream} instances.
	 */
	private static ImageInputStreamSpi cachedStreamSPI=new URLImageInputStreamSpi();
	
	/**
	 * Creates a mosaic for the provided input parameters.
	 * 
	 * @param location  path to the directory where to gather the elements for the mosaic.
	 * @param indexName name to give to this mosaic
	 * @param wildcard wildcard to use for walking through files. We are using commonsIO for this task
	 * @return <code>true</code> if everything is right, <code>false</code>if something bad happens, in which case the reason should be logged to the logger.
	 */
	static boolean createMosaic(
			final String location,
			final String indexName,
			final String wildcard,
			final boolean absolutePath) {
		
		
		//create a mosaic index builder and set the relevant elements
		final IndexBuilderConfiguration configuration = new IndexBuilderConfiguration();
		configuration.setAbsolute(absolutePath);
		configuration.setRootMosaicDirectory(location);
		configuration.setIndexingDirectories(Arrays.asList(location));
		configuration.setIndexName(indexName);		
		final IndexBuilder indexBuilder= new IndexBuilder(configuration);
		//this is going to help us with  catching exceptions and logging them
		final Queue<Throwable> exceptions=new LinkedList<Throwable>();
		try{


			final IndexBuilder.ProcessingEventListener listener= new ProcessingEventListener(){
		
				@Override
				public void exceptionOccurred(ExceptionEvent event) {
					final Throwable t=event.getException();
					exceptions.add(t);
					if(LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE,t.getLocalizedMessage(),t);
					
				}
				
				@Override
				public void getNotification(ProcessingEvent event) {
					if(LOGGER.isLoggable(Level.FINE))
						LOGGER.fine(event.getMessage());
					
				}
	
				
			
				
		
				
			};
			indexBuilder.addProcessingEventListener(listener);
			indexBuilder.run();
		}
		catch (Throwable e) {
			LOGGER.log(Level.SEVERE,"Unable to build mosaic",e);
			return false;
		}
		finally
		{
			indexBuilder.dispose();
		}
		
		//check that nothing bad happened
		if(exceptions.size()>0)
			return false;
		return true;
	}

	


    
    static String getMessageFromException(Exception exception) {
        if(exception.getLocalizedMessage() != null)
            return exception.getLocalizedMessage();
        else
            return exception.getMessage();
    }

		static URL checkSource(Object source) throws MalformedURLException,
				DataSourceException {
			URL sourceURL=null;
			File sourceFile = null;
			// /////////////////////////////////////////////////////////////////////
			//
			// Check source
			//
			// /////////////////////////////////////////////////////////////////////
			//if it is a URL or a String let's try to see if we can get a file to check if we have to build the index
			if (source instanceof File)
			{
				sourceFile = (File)source; 				
				sourceURL = DataUtilities.fileToURL(sourceFile);
				sourceURL = checkURLForMosaicQuery((URL) sourceURL);
			}
			else if (source instanceof URL)
			{
				sourceURL = checkURLForMosaicQuery((URL) source);
				if (sourceURL.getProtocol().equals("file")) {
					sourceFile =DataUtilities.urlToFile(sourceURL);
				} 
			}
			else if (source instanceof String) { 
				//is it a File?
				final String tempSource=(String) source;
			    File tempFile = new File(tempSource);
			    if (!tempFile.exists()) 
			    {
			    	//is it a URL
			    	try{
			    		sourceURL= new URL(tempSource);
			    		sourceURL = checkURLForMosaicQuery(sourceURL);
			    		source =DataUtilities.urlToFile(sourceURL);	    		
			    	}catch (MalformedURLException e) {
			    		sourceURL=null;
			    		source=null;
					}
			    }
			    else
		        {
			    	sourceURL =  DataUtilities.fileToURL(tempFile); 

			    	// so that we can do our magic here below
			    	sourceFile=tempFile;
		        }
			}
			        
	
			// at this point we have tried to convert the thing to a File as hard as we could, let's see what we can do
			if (sourceFile != null)
			   {
				if(!sourceFile.isDirectory())
					sourceURL = DataUtilities.fileToURL((File) sourceFile);
				else
				{
					//it's a directory, let's look for a possible properties files that we want to load
					final String locationPath=sourceFile.getAbsolutePath();
					final String defaultWildcardString=DEFAULT_WILCARD;
					final String defaultIndexName=FilenameUtils.getName(locationPath);
					
					//now let's see f we have at least a properties file with its own shapefile
					final File[] properties = sourceFile.listFiles((FilenameFilter)FileFilterUtils.makeFileOnly(FileFilterUtils.suffixFileFilter(".properties")) );
					//now get the first one with a shapefile
					File shapeFile=null;
					for(File propFile:properties){
						final File shpFile= new File(locationPath,FilenameUtils.getBaseName(propFile.getName())+".shp");
						if(shpFile.exists()&&shpFile.isFile()&&shpFile.canRead()&&propFile.canRead()&&propFile.isFile())
						{
							shapeFile=shpFile;
							break;
						}
					}
					
					//did we find anything?
					if(shapeFile==null)
					{
						//try to build a mosaic inside this directory and see what happens    	
						createMosaic(locationPath, defaultIndexName,defaultWildcardString,DEFAULT_PATH_BEHAVIOR);   
						shapeFile= new File(locationPath,defaultIndexName+".shp");
						File propertiesFile = new File(locationPath,defaultIndexName+".properties");
						if(!shapeFile.exists()||!shapeFile.canRead()||!propertiesFile.exists()||!propertiesFile.canRead())
							sourceURL=null;
						else
							// now set the new source and proceed
							sourceURL= shapeFile.toURI().toURL(); //TODO Comment by Stefan Krueger: Shouldn't we use DataUtilities.fileToURL(file) 
						
						
					}
					else
						// now set the new source and proceed
						sourceURL= shapeFile.toURI().toURL(); // TODO Comment by Stefan Krueger: Shouldn't we use DataUtilities.fileToURL(file) 
					
				}
			   }
				else {
					// SK: We don't set SourceURL to null now, just because it doesn't point to a file
					// sourceURL=null;
				}
			return sourceURL;
		}
		
	/**
	 * Checks the provided {@link URL} in order to see if it is a a query to build a mosaic or not.
	 * 
	 * @param sourceURL
	 * @return a modified version of the provided {@link URL} which points to a shapefile in case we created a mosaic, or to the original {@link URL}otherwise.
	 */
	static URL checkURLForMosaicQuery(final URL sourceURL ) {
        	////
        	//
        	// Query with parameters, it might be that the user is
			// trying to build the mosaic specifying the params as
			// well
        	//
        	////
            if (sourceURL.getProtocol().equalsIgnoreCase("file")) {
            	final String query=sourceURL.getQuery();
            	if(query!=null)
            	{
            		final String[] tokens = query.split("\\&");
            		final String locationPath=sourceURL.getPath();// remove 'file:' prefix
            		String indexName=null;
            		final File sourceDir= new File(locationPath);
            		if(!(sourceDir.isDirectory()&&sourceDir.exists()&&sourceDir.canRead()))
            			return null;
            		String wildcardString=null;
            		boolean absolutePath=DEFAULT_PATH_BEHAVIOR;
            		for(String token:tokens)
            		{
            			//splitting token
            			final String[] values=token.split("\\=");
            			if(values[0].equalsIgnoreCase("name"))
            				indexName=values[1];
            			else
            				if(values[0].equalsIgnoreCase("w")||values[0].equalsIgnoreCase("wildcard"))
            					wildcardString=values[1];
            				else
                				if(values[0].equalsIgnoreCase("p")||values[0].equalsIgnoreCase("path"))
                					absolutePath=Boolean.parseBoolean(values[1]);

            			
            		}
            		
            		//now check if the shapefle is already there
            		final File shapeFile= new File(locationPath,indexName+".shp");
          			File propertiesFile = new File(locationPath,indexName+".properties");
        			if(!shapeFile.exists()||!shapeFile.canRead()||!shapeFile.isFile()||!propertiesFile.exists()||!propertiesFile.canRead()||!propertiesFile.isFile())
    				{
        				//try to build it
        				createMosaic(locationPath, indexName!=null?indexName:FilenameUtils.getBaseName(locationPath),wildcardString!=null?wildcardString:DEFAULT_WILCARD,absolutePath);
    					
    				}  
        			
        			//check URL again
        			if(!shapeFile.exists()||!shapeFile.canRead()||!shapeFile.isFile()||!propertiesFile.exists()||!propertiesFile.canRead()||!propertiesFile.isFile())
        				return null;
					else
						try {
							return shapeFile.toURI().toURL();
						} catch (MalformedURLException e) {
							if(LOGGER.isLoggable(Level.FINE))
								LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
						}            		
    				
    				
						
            		
            	}
            		
            }
            
            return  sourceURL;
        }






	static MosaicConfigurationBean loadPropertiesFile(final URL sourceURL, final CoordinateReferenceSystem crs, final String defaultLocationAttribute){
			//ret value
		    final MosaicConfigurationBean retValue= new MosaicConfigurationBean();

			
			//
			// load the properties file
			//
			final Properties properties = new Properties();
			URL propsURL = DataUtilities.changeUrlExt(sourceURL, "properties");
			
			InputStream stream = null;
			InputStream openStream = null;
			try {
				openStream =  propsURL.openStream();
				stream = new BufferedInputStream(openStream);
				properties.load(stream);
			} catch (FileNotFoundException e) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);				
				return null;
			} catch (IOException e) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);				
				return null;
			}
			finally {
				
				if(stream!=null)
					IOUtils.closeQuietly(stream);
				
				if (openStream != null)
					IOUtils.closeQuietly(openStream);
					
			}
			

			//
			// load the envelope
			//
			if(!properties.containsKey("Envelope2D"))
			{
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Required key Envelope2D not found.");		
				return  null;
			}
			final String envelope = properties.getProperty("Envelope2D").trim();
			String[] pairs = envelope.split(" ");
			final double cornersV[][] = new double[2][2];
			String pair[];
			for (int i = 0; i < 2; i++) {
				pair = pairs[i].split(",");
				cornersV[i][0] = Double.parseDouble(pair[0]);
				cornersV[i][1] = Double.parseDouble(pair[1]);
			}
			final GeneralEnvelope originalEnvelope = new GeneralEnvelope(cornersV[0], cornersV[1]);
			originalEnvelope.setCoordinateReferenceSystem(crs);
			retValue.setEnvelope2D(new Envelope2D(originalEnvelope));
		
			//
			// resolutions levels
			//			
			int levelsNumber = Integer.parseInt(properties.getProperty("LevelsNum","1").trim()) ;
			retValue.setLevelsNum(levelsNumber);
			if(!properties.containsKey("Levels"))
			{
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Required key Levels not found.");		
				return  null;
			}			
			final String levels = properties.getProperty("Levels").trim();
			pairs = levels.split(" ");
			if(pairs==null||pairs.length!=levelsNumber)
			{
				LOGGER.severe("Levels number is different from the provided number of levels resoltion.");
				return null;
			}
			final double[][] resolutions = new double[levelsNumber][2];
       		for (int i = 0; i < levelsNumber; i++) {
       			pair = pairs[i].split(",");
    			if(pair==null||pair.length!=2)
    			{
    				LOGGER.severe("OverviewLevel number is different from the provided number of levels resoltion.");
    				return null;
    			}       			
       			resolutions[i][0] = Double.parseDouble(pair[0]);
       			resolutions[i][1] = Double.parseDouble(pair[1]);
       		}
       		retValue.setLevels(resolutions);

       		//
			// suggested spi is optional
			//
			if(properties.containsKey("SuggestedSPI"))
			{
				String suggestedSPI = properties.getProperty("SuggestedSPI").trim();
				retValue.setSuggestedSPI(suggestedSPI);
			}			

			//
			// name is not optional
			//
			if(!properties.containsKey("Name"))
			{
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Required key Name not found.");		
				return  null;
			}			
			String coverageName = properties.getProperty("Name").trim();
			retValue.setName(coverageName);

			// need a color expansion?
			// this is a newly added property we have to be ready to the case where
			// we do not find it.
			final boolean expandMe=Boolean.valueOf(properties.getProperty("ExpandToRGB","false").trim());	
			retValue.setExpandToRGB(expandMe);
			
			//
			// Absolute or relative path
			//
			boolean absolutePath = Boolean.parseBoolean(properties.getProperty("AbsolutePath", Boolean.toString(ImageMosaicUtils.DEFAULT_PATH_BEHAVIOR)).trim());
			retValue.setAbsolutePath(absolutePath);
			
		
		    //
		    //  location
		    //	
			retValue.setLocationAttribute(properties.getProperty("LocationAttribute",ImageMosaicUtils.DEFAULT_LOCATION_ATTRIBUTE).trim());
					
			//retrn value
			return retValue;			
		}






	/**
	 * Returns a suitable threshold depending on the {@link DataBuffer} type.
	 * 
	 * <p>
	 * Remember that the threshold works with >=.
	 * 
	 * @param dataType
	 *            to create a low threshold for.
	 * @return a minimum threshold value suitable for this data type.
	 */
	 static double getThreshold(int dataType) {
		switch (dataType) {
		case DataBuffer.TYPE_BYTE:
		case DataBuffer.TYPE_USHORT:
			// this may cause problems and truncations when the native mosaic operations is enabled
			return 0.0;
		case DataBuffer.TYPE_INT:
			return Integer.MIN_VALUE;
		case DataBuffer.TYPE_SHORT:
			return Short.MIN_VALUE;
		case DataBuffer.TYPE_DOUBLE:
			return -Double.MAX_VALUE;
		case DataBuffer.TYPE_FLOAT:
			return -Float.MAX_VALUE;
		}
		return 0;
	}






	/**
	 * Builds a {@link ReferencedEnvelope} from a {@link GeographicBoundingBox}.
	 * This is useful in order to have an implementation of {@link BoundingBox}
	 * from a {@link GeographicBoundingBox} which strangely does implement
	 * {@link GeographicBoundingBox}.
	 * 
	 * @param geographicBBox
	 *                the {@link GeographicBoundingBox} to convert.
	 * @return an instance of {@link ReferencedEnvelope}.
	 */
	static ReferencedEnvelope getReferencedEnvelopeFromGeographicBoundingBox(
	        final GeographicBoundingBox geographicBBox) {
	    ImageMosaicUtils.ensureNonNull("GeographicBoundingBox", geographicBBox);
	    return new ReferencedEnvelope(geographicBBox.getEastBoundLongitude(),
	            geographicBBox.getWestBoundLongitude(), geographicBBox
	                    .getSouthBoundLatitude(), geographicBBox
	                    .getNorthBoundLatitude(), DefaultGeographicCRS.WGS84);
	}






	/**
	 * @param transparentColor
	 * @param image
	 * @return
	 * @throws IllegalStateException
	 */
	static RenderedImage makeColorTransparent(
			final Color transparentColor, final RenderedImage image)
			throws IllegalStateException {
		final ImageWorker w = new ImageWorker(image);
		if (image.getSampleModel() instanceof MultiPixelPackedSampleModel)
			w.forceComponentColorModel();
		return w.makeColorTransparent(transparentColor).getRenderedImage();
	}






	static ImageReadParam cloneImageReadParam(
			ImageReadParam param) {
	
	
	    // The ImageReadParam passed in is non-null. As the
	    // ImageReadParam class is not Cloneable, if the param
	    // class is simply ImageReadParam, then create a new
	    // ImageReadParam instance and set all its fields
	    // which were set in param. This will eliminate problems
	    // with concurrent modification of param for the cases
	    // in which there is not a special ImageReadparam used.
	
	    // Create a new ImageReadParam instance.
	    ImageReadParam newParam = new ImageReadParam();
	
	    // Set all fields which need to be set.
	
	    // IIOParamController field.
	    if(param.hasController()) {
	        newParam.setController(param.getController());
	    }
	
	    // Destination fields.
	    newParam.setDestination(param.getDestination());
	    if(param.getDestinationType() != null) {
	        // Set the destination type only if non-null as the
	        // setDestinationType() clears the destination field.
	        newParam.setDestinationType(param.getDestinationType());
	    }
	    newParam.setDestinationBands(param.getDestinationBands());
	    newParam.setDestinationOffset(param.getDestinationOffset());
	
	    // Source fields.
	    newParam.setSourceBands(param.getSourceBands());
	    newParam.setSourceRegion(param.getSourceRegion());
	    if(param.getSourceMaxProgressivePass() != Integer.MAX_VALUE) {
	        newParam.setSourceProgressivePasses(
	            param.getSourceMinProgressivePass(),
	            param.getSourceNumProgressivePasses());
	    }
	    if(param.canSetSourceRenderSize()) {
	        newParam.setSourceRenderSize(param.getSourceRenderSize());
	    }
	    newParam.setSourceSubsampling(param.getSourceXSubsampling(),
	                                  param.getSourceYSubsampling(),
	                                  param.getSubsamplingXOffset(),
	                                  param.getSubsamplingYOffset());
	
	    // Replace the local variable with the new ImageReadParam.
	    return newParam;
	
	}






	/**
	 * Makes sure that an argument is non-null.
	 * 
	 * @param name
	 *                Argument name.
	 * @param object
	 *                User argument.
	 * @throws IllegalArgumentException
	 *                 if {@code object} is null.
	 */
	static void ensureNonNull(final String name, final Object object)
	        throws NullPointerException {
	    if (object == null) {
	        throw new NullPointerException(Errors.format(
	                ErrorKeys.NULL_ARGUMENT_$1, name));
	    }
	}





	static IOFileFilter excludeFilters(final IOFileFilter inputFilter,
			IOFileFilter ...filters) {
		IOFileFilter retFilter=inputFilter;
		for(IOFileFilter filter:filters){
			retFilter=FileFilterUtils.andFileFilter(
					retFilter, 
					FileFilterUtils.notFileFilter(filter));
		}
		return retFilter;
	}





	/**
	 * Look for an {@link ImageReader} instance that is able to read the provided {@link ImageInputStream}, which must be non null.
	 * 
	 * <p>
	 * In case no reader is found, <code>null</code> is returned.
	 * 
	 * @param inStream an instance of {@link ImageInputStream} for which we need to find a suitable {@link ImageReader}. 
	 * @return a suitable instance of {@link ImageReader} or <code>null</code> if one cannot be found.
	 */
	static ImageReader getReader(
			final ImageInputStream inStream) {
		ensureNonNull("inStream", inStream);
		// get a reader
		inStream.mark();
		final Iterator<ImageReader> readersIt = ImageIO.getImageReaders(inStream);
		if(!readersIt.hasNext())
		{
			return null;
		}
		return readersIt.next();
	}





	/**
	 * Retrieves the dimensions of the {@link RenderedImage} at index <code>imageIndex</code> for the provided 
	 * {@link ImageReader} and {@link ImageInputStream}.
	 * 
	 * <p>
	 * Notice that none of the input parameters can be <code>null</code> or a {@link NullPointerException} will be thrown.
	 * Morevoer the <code>imageIndex</code> cannot be negative or an {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param imageIndex the index of the image to get the dimensions for.
	 * @param inStream the {@link ImageInputStream} to use as an input 
	 * @param reader the {@link ImageReader} to decode the image dimensions.
	 * @return a {@link Rectangle} that contains the dimensions for the image at index <code>imageIndex</code>
	 * @throws IOException in case the {@link ImageReader} or the {@link ImageInputStream} fail.
	 */
	static Rectangle  getDimension(
			final int imageIndex,
			final ImageInputStream inStream, 
			final ImageReader reader) throws IOException {
		ensureNonNull("inStream", inStream);
		ensureNonNull("reader", reader);
		if(imageIndex<0)
			throw new IllegalArgumentException(Errors.format(ErrorKeys.INDEX_OUT_OF_BOUNDS_$1,imageIndex));
		inStream.reset();
		reader.setInput(inStream);
		return new Rectangle(0,0,reader.getWidth(imageIndex),reader.getHeight(imageIndex));
	}





	/**
	 * Retrieves an {@link ImageInputStream} for the provided input {@link File}.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	static ImageInputStream getInputStream(final File file)
			throws IOException {
		final ImageInputStream inStream= ImageIO.createImageInputStream(file);
		if(inStream==null)
			return null;
		return inStream;
	}
	
	/**
	 * Retrieves an {@link ImageInputStream} for the provided input {@link URL}.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	static ImageInputStream getInputStream(final URL url)
	throws IOException {
		final ImageInputStream inStream= cachedStreamSPI.createInputStreamInstance(url,ImageIO.getUseCache(),ImageIO.getCacheDirectory());
		if(inStream==null)
			return null;
		return inStream;
	}


	/**
	 * Checks that the provided <code>dimensions</code> when intersected with the source region 
	 * used by the provided {@link ImageReadParam} instance does not result in an empty {@link Rectangle}.
	 * 
	 * <p>
	 * Input parameters cannot be null.
	 * 
	 * @param readParameters an instance of {@link ImageReadParam} for which we want to check the source region element.
	 * @param dimensions an instance of {@link Rectangle} to use for the check.
	 * @return <code>true</code> if the intersection is not empty, <code>false</code> otherwise.
	 */
	static boolean checkEmptySourceRegion(
			final ImageReadParam readParameters,
			final Rectangle dimensions) {
		ensureNonNull("readDimension", dimensions);
		ensureNonNull("readP", readParameters);
		final Rectangle sourceRegion=readParameters.getSourceRegion();
		Rectangle.intersect(sourceRegion, dimensions, sourceRegion);	
		if(sourceRegion.isEmpty())
			return true;
		readParameters.setSourceRegion(sourceRegion);
		return false;
	}





	/**
	 * Default priority for the underlying {@link Thread}.
	 */
	public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;
	/**
	 * Default location attribute name.
	 */
	public static final String DEFAULT_LOCATION_ATTRIBUTE = "location";
	
	public static final String DEFAULT_INDEX_NAME = "index";

	/**
	 * Checks that a {@link File} is a real file, exists and is readable.
	 * 
	 * @param file the {@link File} instance to check. Must not be null.
	 * 
	 * @return <code>true</code> in case the file is a real file, exists and is readable; <code>false </code> otherwise.
	 */
	static boolean checkFileReadable(final File file){
		if(LOGGER.isLoggable(Level.FINE))
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("Checking file:").append(FilenameUtils.getFullPath(file.getAbsolutePath())).append("\n");
			builder.append("canRead:").append(file.canRead()).append("\n");
			builder.append("isHidden:").append(file.isHidden()).append("\n");
			builder.append("isFile").append(file.isFile()).append("\n");
			builder.append("canWrite").append(file.canWrite()).append("\n");
			LOGGER.fine(builder.toString());
		}		
		if (!file.exists() || !file.canRead()|| !file.isFile()) 
			return false;
		return true;
	}





	/**
	 * @param testingDirectory
	 * @return 
	 * @throws IllegalArgumentException
	 * @throws IOException 
	 */
	static String checkInputDirectory(String testingDirectory)
			throws IllegalArgumentException {
		File inDir = new File(testingDirectory);
		if (!inDir.isDirectory()||!inDir.canRead()) {
			LOGGER.severe("Provided input dir does not exist or is not a dir!");
			throw new IllegalArgumentException(
					"Provided input dir does not exist or is not a dir!");
		}
		try {
			testingDirectory = inDir.getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		testingDirectory=FilenameUtils.normalize(testingDirectory);
		if(!testingDirectory.endsWith(File.separator))
			testingDirectory=testingDirectory+File.separator;
		//test to see if things are still good
		inDir = new File(testingDirectory);
		if (!inDir.isDirectory()||!inDir.canRead()) {
			LOGGER.severe("Provided input dir does not exist or is not a dir!");
			throw new IllegalArgumentException(
					"Provided input dir does not exist or is not a dir!");
		}
		return testingDirectory;
	}





	static boolean checkURLReadable(URL url) {
		try {
			url.openStream().close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
