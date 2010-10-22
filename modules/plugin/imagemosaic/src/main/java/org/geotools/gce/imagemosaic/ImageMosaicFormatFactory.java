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
package org.geotools.gce.imagemosaic;

import it.geosolutions.imageio.plugins.jp2ecw.JP2GDALEcwImageReaderSpi;
import it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageReaderSpi;
import it.geosolutions.imageio.plugins.jp2mrsid.JP2GDALMrSidImageReaderSpi;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.RenderingHints;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverageio.gdal.jp2ecw.JP2ECWFormatFactory;
import org.geotools.coverageio.gdal.jp2kak.JP2KFormatFactory;
import org.geotools.coverageio.gdal.jp2mrsid.JP2MrSIDFormatFactory;
import org.geotools.data.DataUtilities;

import com.sun.media.imageioimpl.common.PackageUtil;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

/**
 * Implementation of the GridCoverageFormat service provider interface for
 * mosaic of georeferenced images.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @since 2.3
 */

public final class ImageMosaicFormatFactory implements GridFormatFactorySpi {
        
    @Override
	protected void finalize() throws Throwable {
    	DefaultMultiThreadedLoader.shutdown();
		DefaultMultiThreadedLoader.shutdownNow();
	}

	static ExecutorService DefaultMultiThreadedLoader;
    
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(ImageMosaicFormatFactory.class);
	
	private static final String KAKADU_SPI = "it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi";

	static{
		initDefaultMultiThreadedLoader();
		
		replaceTIFF();
		
		if(JP2KAK()){
			replaceJP2KAK();
		}
		
		else{
			if(JP2ECW()){
				replaceECW();
			}
			if(JP2MRSID()){
				replaceMRSID();
			}
			if(JP2GDALKAK()){
				replaceGDALKAK();
			}
		}
		
	}

	/**
	 * Initialize the multithreaded loader for loading granules in 
	 * parallel inside {@link ImageMosaicReader}s instances.
	 */
	private static void initDefaultMultiThreadedLoader() {
	    // defaults   
	    int corePoolSize = ImageMosaicUtils.DEFAULT_CORE_POOLSIZE;
            int maxPoolSize = ImageMosaicUtils.DEFAULT_MAX_POOLSIZE;;
            int keepAliveSeconds = ImageMosaicUtils.DEFAULT_KEEP_ALIVE;
            ImageMosaicUtils.QueueType queueType=ImageMosaicUtils.DEFAULT_QUEUE_TYPE;
            

            String configFile = null;
            Properties props = null;
            //
            // STEP 0 Java property
            //
            final String filePath = System.getProperty("mosaic.threadpoolconfig.path");
            if (filePath != null && filePath.trim().length() > 0) {
                configFile = filePath;
                if (configFile != null){
                    final File file = new File(configFile);
                    if (file != null && file.exists() && file.canRead()){
                        props = ImageMosaicUtils.loadPropertiesFromURL(DataUtilities.fileToURL(file));
                    }
                }
            }            

            //
            // STEP 1 User home config file
            //
            // Looking for file referred by "mosaic.threadpoolconfig.path" property
            if (props == null) {
                //Looking for file on user home
                final String userHome = System.getProperty("user.home");
                if (userHome != null && userHome.length()>0) {
                    configFile = userHome+File.separatorChar+ImageMosaicUtils.THREADPOOL_CONFIG_FILE;
                    final File file = new File(configFile);
                    if (file != null && file.exists() && file.canRead()){
                        props = ImageMosaicUtils.loadPropertiesFromURL(DataUtilities.fileToURL(file));
                    }
                }
            }
            
            //Init properties
            if (props != null){
                final String corePool = props.getProperty("corePoolSize");
                if (corePool != null){
                    corePoolSize = Integer.parseInt(corePool);
                }
                final String maxPool = props.getProperty("maxPoolSize");
                if (maxPool != null){
                    maxPoolSize = Integer.parseInt(maxPool);
                }
                final String keepAlive = props.getProperty("keepAliveSeconds");
                if (keepAlive != null){
                    keepAliveSeconds = Integer.parseInt(keepAlive);
                }
                final String queueT = props.getProperty("queueType");
                if (queueT != null){
                	try{
                		queueType=ImageMosaicUtils.QueueType.valueOf(queueT);
                	}finally{
                		queueType=ImageMosaicUtils.QueueType.getDefault();
                	}
                }                
            }
            
            switch (queueType) {
			case UNBOUNDED:
				DefaultMultiThreadedLoader = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveSeconds,
	                    TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
				break;
			case DIRECT:
				DefaultMultiThreadedLoader = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveSeconds,
	                    TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
				break;
			default:// UNBOUNDED
				DefaultMultiThreadedLoader = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveSeconds,
	                    TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());				
				break;
			}
            
        
    }            
	/**
	 * Tells me if this plugin will work on not given the actual installation.
	 * 
	 * <p>
	 * Dependecies are mostly from JAI and ImageIO so if they are installed you
	 * should not have many problems.
	 * 
	 * @return False if something's missing, true otherwise.
	 */
	public boolean isAvailable() {
		boolean available = true;

		// if these classes are here, then the runtine environment has
		// access to JAI and the JAI ImageI/O toolbox.
		try {
			Class.forName("javax.media.jai.JAI");
			Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
		} catch (ClassNotFoundException cnf) {
			available = false;
		}

		return available;
	}

	private static boolean JP2KAK() {
		try{
			@SuppressWarnings("unused")
			Class<?> cl = Class.forName(KAKADU_SPI);
			Class<?> utilityClass = Class.forName("it.geosolutions.util.KakaduUtilities");
			final Method method = utilityClass.getDeclaredMethod("isKakaduAvailable", (Class[])null);
			if (method != null){
				Boolean isAvailable = (Boolean) method.invoke(null, null);
				return isAvailable.booleanValue();
			}
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} catch (SecurityException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} catch (NoSuchMethodException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} catch (IllegalArgumentException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} catch (IllegalAccessException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} catch (InvocationTargetException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, "Unable to load kakadu JPEG2000 reader spi",e);
		} 
		return false;
	}

	private static void replaceJP2KAK() {
		try{
			Class.forName(KAKADU_SPI);

			// imageio kakJP2 reader
			final String imageioJ2KImageReaderCodecName=J2KImageReaderCodecLibSpi.class.getName();
			
			if(PackageUtil.isCodecLibAvailable()){
				boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, KAKADU_SPI, imageioJ2KImageReaderCodecName, "JPEG 2000");
	        	if(!succeeded)
	        		if (LOGGER.isLoggable(Level.WARNING))
	        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+KAKADU_SPI+":"+imageioJ2KImageReaderCodecName);	
			}
        	
			// imageio kakJP2 reader
			final String imageioJ2KImageReaderName=J2KImageReaderSpi.class.getName();
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, KAKADU_SPI, imageioJ2KImageReaderName, "JPEG 2000");
        	if(!succeeded)
        		if (LOGGER.isLoggable(Level.FINE))
        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+KAKADU_SPI+":"+imageioJ2KImageReaderName);	
	        
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, "Unable to load specific JPEG2000 reader spi",e);
		} 
	}

	private static void replaceTIFF() {
		try{
			//check if our tiff plugin is in the path
			final String customTiffName=it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi.class.getName();
			Class.forName(customTiffName);

			// imageio tiff reader
			final String imageioTiffName=TIFFImageReaderSpi.class.getName();
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, customTiffName, imageioTiffName, "tiff");
        	if(!succeeded)
        		if (LOGGER.isLoggable(Level.FINE))
        			LOGGER.warning("Unable to set ordering between tiff readers spi");	
	        
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING,"Unable to load specific TIFF reader spi",e);
		} 
		
	}

	private static boolean JP2GDALKAK() {
		return new JP2KFormatFactory().isAvailable();
	}

	private static void replaceGDALKAK() {
		try{
			//check if our kakJP2 plugin is in the path
			final String kakJP2=JP2GDALKakaduImageReaderSpi.class.getName();
			Class.forName(kakJP2);

			// imageio kakJP2 reader
			final String imageioJ2KImageReaderCodecName=J2KImageReaderCodecLibSpi.class.getName();
			
			if(PackageUtil.isCodecLibAvailable()){
				boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, kakJP2, imageioJ2KImageReaderCodecName, "JPEG 2000");
	        	if(!succeeded)
	        		if (LOGGER.isLoggable(Level.FINE))
	        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+kakJP2+":"+imageioJ2KImageReaderCodecName);	
			}
        	
			// imageio kakJP2 reader
			final String imageioJ2KImageReaderName=J2KImageReaderSpi.class.getName();
			
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, kakJP2, imageioJ2KImageReaderName, "JPEG 2000");
        	if(!succeeded)
        		if (LOGGER.isLoggable(Level.FINE))
        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+kakJP2+":"+imageioJ2KImageReaderName);	
	        
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, "Unable to load specific JPEG2000 reader spi",e);
		} 
		
	}

	private static void replaceMRSID() {
		try{
			//check if our mrsidJP2 plugin is in the path
			final String mrsidJP2=JP2GDALMrSidImageReaderSpi.class.getName();
			Class.forName(mrsidJP2);

			// imageio tiff reader
			final String imageioJ2KImageReaderCodecName=J2KImageReaderCodecLibSpi.class.getName();
			
			if(PackageUtil.isCodecLibAvailable()){
				boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, mrsidJP2, imageioJ2KImageReaderCodecName, "JPEG 2000");
	        	if(!succeeded)
	        		if (LOGGER.isLoggable(Level.FINE))
	        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+mrsidJP2+":"+imageioJ2KImageReaderCodecName);	
			}
        	
			// imageio mrsidJP2 reader
			final String imageioJ2KImageReaderName=J2KImageReaderSpi.class.getName();
			
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, mrsidJP2, imageioJ2KImageReaderName, "JPEG 2000");
        	if(!succeeded)
        		if (LOGGER.isLoggable(Level.FINE))
        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+mrsidJP2+":"+imageioJ2KImageReaderName);	
	        
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, "Unable to load specific JPEG2000 reader spi",e);
		} 
		
	}

	private static boolean JP2MRSID() {
		return new JP2MrSIDFormatFactory().isAvailable();
	}

	private static void replaceECW() {
		try{
			//check if our ecwJP2 plugin is in the path
			final String ecwJP2=JP2GDALEcwImageReaderSpi.class.getName();
			Class.forName(ecwJP2);

			// imageio ecwJP2 reader
			final String imageioJ2KImageReaderCodecName=J2KImageReaderCodecLibSpi.class.getName();
			
			if(PackageUtil.isCodecLibAvailable()){
				boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, ecwJP2, imageioJ2KImageReaderCodecName, "JPEG 2000");
	        	if(!succeeded)
	        		if (LOGGER.isLoggable(Level.FINE))
	        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+ecwJP2+":"+imageioJ2KImageReaderCodecName);	
			}
        	
			// imageio ecwJP2 reader
			final String imageioJ2KImageReaderName=J2KImageReaderSpi.class.getName();
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, ecwJP2, imageioJ2KImageReaderName, "JPEG 2000");
        	if(!succeeded)
        		if (LOGGER.isLoggable(Level.FINE))
        			LOGGER.warning("Unable to set ordering between jp2 readers spi-"+ecwJP2+":"+imageioJ2KImageReaderName);	
	        
		} catch (ClassNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, "Unable to load specific JPEG2000 reader spi",e);
		} 
		
	}

	private static boolean JP2ECW() {
		return new JP2ECWFormatFactory().isAvailable();
	}

	/**
	 * @see GridFormatFactorySpi#createFormat().
	 */
	public ImageMosaicFormat createFormat() {
		return new ImageMosaicFormat();
	}

	/**
	 * Returns the implementation hints. The default implementation returns an
	 * empty map.
	 * 
	 * @return An empty map.
	 */
	public Map<RenderingHints.Key, ?> getImplementationHints() {
		return Collections.emptyMap();
	}
}
