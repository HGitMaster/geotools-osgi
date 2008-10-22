/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.ows;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wms.WMSOperationType;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetLegendGraphicRequest;
import org.geotools.data.wms.response.GetLegendGraphicResponse;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Nested list of zero or more map Layers offered by this server. It contains
 * only fields for information that we currently find interesting. Feel free
 * to add your own.
 *
 * @author rgould
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wms/src/main/java/org/geotools/data/ows/Layer.java $
 */
public class Layer implements Comparable<Layer> {
    
    /** A machine-readable (typically one word) identifier */
    private String name;

    /** The title is for informative display to a human. */
    private String title;
    
    private String _abstract;
    private String[] keywords;

    /** A set of Strings representing SRSs */
    private Set<String> srs = null;
    
    /** the union of the layers's SRSs and the parent's SRSs */
    private Set<String> allSRSCache = null;
    
    /**
     * A HashMap representings the bounding boxes on each layer. The Key is the
     * CRS (or SRS) of the bounding box. The Value is the BoundingBox object
     * itself.
     */
    private HashMap<Object,CRSEnvelope> boundingBoxes = null;

    /**
     * A boundingbox containing the minimum rectangle of the map data in
     * EPSG:4326
     */
    private CRSEnvelope latLonBoundingBox = null;

    /** A list of type org.opengis.layer.Style */
    private List<org.opengis.layer.Style> styles;
    private Boolean queryable = null;
    
    private double scaleHintMin = Double.NaN;
    private double scaleHintMax = Double.NaN;
    private double scaleDenominatorMin = Double.NaN;
    private double scaleDenominatorMax = Double.NaN;
    
    private Layer parent;
    private Layer[] children;

	private Map envelopeCache=Collections.synchronizedMap(new WeakHashMap());
	
	/**
	 * Crate a layer with no human readable title.
	 * <p>
	 * These layers are simply for organization and storage of common
	 * settings (like SRS or style settings). These settings will be
	 * valid for all children. 
	 */
    public Layer() {
        this(null );
    }

    /**
     * Create a layer with an optional title
     *
     * @param title
     */
    public Layer(String title) {
        this.title = title;
    }

    /**
     * Returns every BoundingBox associated with this layer. The
     * <code>HashMap</code> returned has each bounding box's CRS/SRS value as
     * the key, and the value is the <code>BoundingBox</code> object itself.
     *
     * Implements inheritance: if this layer's bounding box is null, query ancestors until
     * the first bounding box is found or no more ancestors
     * 
     * @return a HashMap of all of this layer's bounding boxes or null if no
     * bounding boxes found
     */
    public HashMap<Object,CRSEnvelope> getBoundingBoxes() {
       if (boundingBoxes == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             HashMap<Object,CRSEnvelope> bb = parent.getBoundingBoxes();
             if (bb != null)
                return bb;
             else 
                parent = parent.getParent();
          }
       } 
       // May return null. But that is OK since spec says 0 or more may be specified 
        return boundingBoxes;
    }

    /**
     * Sets this layer's bounding boxes. The HashMap must have each
     * BoundingBox's  CRS/SRS value as its key, and the
     * <code>BoundingBox</code> object as its value.
     *
     * @param boundingBoxes a HashMap containing bounding boxes
     */
    public void setBoundingBoxes(HashMap boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    /**
     * Gets the name of the <code>Layer</code>. It is designed to be machine
     * readable, and if it is present, this layer is determined to be drawable
     * and is a valid candidate for use in a GetMap or GetFeatureInfo request.
     *
     * @return the machine-readable name of the layer
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this layer. Giving the layer name indicates that it
     * can be drawn during a GetMap or GetFeatureInfo request.
     *
     * @param name the layer's new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Accumulates all of the srs/crs specified for this layer and all srs/crs inherited from
     * its ancestors. No duplicates are returned.
     * 
     * @return Set of all srs/crs for this layer and its ancestors
     */
    public Set getSrs() {
        synchronized (this) {
            if( allSRSCache==null ){
                allSRSCache = new HashSet(srs);
                // Get my ancestor's srs/crs
                Layer parent = this.getParent();
                if (parent != null) {
                   Set parentSrs = parent.getSrs();
                   if (parentSrs != null)  //got something, add to accumulation
                       allSRSCache.addAll(parentSrs);
                }
            }
            // May return an empty list, but spec says at least one must be specified. Perhaps, need
            // to check and throw exception if set is empty. I'm leaving that out for now since 
            // it changes the method signature and would potentially break existing users of this class
            return allSRSCache;
        }
       
    }

    public void setSrs(Set srs) {
        this.srs = srs;
    }

    /**
     * Accumulates all of the styles specified for this layer and all styles inherited from
     * its ancestors. No duplicates are returned.
     * 
     * The List that is returned is of type List<org.opengis.layer.Style>. Before 2.2-RC0
     * it was of type List<java.lang.String>.
     * 
     * @return List of all styles for this layer and its ancestors
     */
    public List getStyles() {
       ArrayList allStyles = new ArrayList();
       // Get my ancestor's styles
       Layer parent = this.getParent();
       if (parent != null) {
          List parentStyles = parent.getStyles();
          if (parentStyles != null)  //got something, add to accumulation
             allStyles.addAll(parentStyles);
       }
       // Now add my styles, if any
       // Brute force check for duplicates. The spec says duplicates are not allowed:
       // (para 7.1.4.5.4) "A child shall not redefine a Style with the same Name as one 
       // inherited from a parent. A child may define a new Style with a new Name that is 
       // not available for the parent Layer."
       if ((styles != null) && !styles.isEmpty()) {
          for (Iterator iter = styles.iterator(); iter.hasNext();) {
             Object style = iter.next();
            if (!allStyles.contains(style))
                allStyles.add(style);
          }
       }
       
       // May return an empty list, but that is OK since spec says 0 or more styles may be specified 
       return allStyles;
    }

    public void setStyles(List styles) {
        this.styles = styles;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Determines if this layer is queryable. Implements inheritance: if this layer's 
     * Queryable attribute is null, check ancestors until the first Queryable attribute is found 
     * or no more ancestors. If a Queryable attribute is not found for this layer, it will return
     * the default value of false.
     * 
     * @return true is this layer is Queryable
     */
    public boolean isQueryable() {
       if (queryable == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             Boolean q = parent.getQueryable();
             if (q != null)
                return q.booleanValue();
             else 
                parent = parent.getParent();
          }
          // At this point a attribute was not found so return default 
          return false;
       } 
        return queryable.booleanValue();
    }
    
    private Boolean getQueryable() {
       return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = new Boolean(queryable);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Layer layer) {        
        if ((this.getName() != null) && (layer.getName() != null)) {
            return this.getName().compareTo(layer.getName());
        }

        return this.getTitle().compareTo(layer.getTitle());
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the parent.
     */
    public Layer getParent() {
        return parent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent The parent to set.
     */
    public void setParent(Layer parent) {
        this.parent = parent;
    }

    /**
     * Returns the LatLonBoundingBox for this layer. Implements inheritance: if this layer's 
     * bounding box is null, query ancestors until the first bounding box is found 
     * or no more ancestors.
     * 
     * @return the LatLonBoundingBox for this layer or null if no lat/lon bounding box is found
     */
    public CRSEnvelope getLatLonBoundingBox() {
       if (latLonBoundingBox == null) {
          Layer parent = this.getParent();
          while (parent != null) {
             CRSEnvelope llbb = parent.getLatLonBoundingBox();
             if (llbb != null)
                return llbb;
             else 
                parent = parent.getParent();
          }
          // We should never get to falling out of the while loop w/o a LatLonBoundingBox
          // being found. The WMS spec says one is required. So perhaps if we don't find one,
          // then throw an exception. I'm leaving that out for now since it changes the method signature
          // and would potentially break existing users of this class
       } 
       // May return null! 
       return latLonBoundingBox;
    }

    public void setLatLonBoundingBox(CRSEnvelope latLonBoundingBox) {
        this.latLonBoundingBox = latLonBoundingBox;
    }
    public Layer[] getChildren() {
        return children;
    }
    public void setChildren( Layer[] children ) {
        this.children = children;
    }
    /**
     * The abstract contains human-readable information about this layer
     * @return Returns the _abstract.
     */
    public String get_abstract() {
        return _abstract;
    }
    /**
     * @param _abstract The _abstract to set.
     */
    public void set_abstract( String _abstract ) {
        this._abstract = _abstract;
    }
    /**
     * Keywords are Strings to be used in searches
     * 
     * @return Returns the keywords.
     */
    public String[] getKeywords() {
        return keywords;
    }
    /**
     * @param keywords The keywords to set.
     */
    public void setKeywords( String[] keywords ) {
        this.keywords = keywords;
    }
    /**
     * Max scale denominator for which it is appropriate to draw this layer.
     * <p>
     * Scale denominator is calculated based on the bounding box of the central
     * pixel in a request (ie not a scale based on real world size of the entire
     * layer).
     * @param Max scale denominator for which it is approprirate to draw this layer
     */
    public void setScaleDenominatorMax( double scaleDenominatorMax ) {
        this.scaleDenominatorMax = scaleDenominatorMax;
    }
    /**
     * Max scale denominator for which it is appropriate to draw this layer.
     * <p>
     * Scale denominator is calculated based on the bounding box of the central
     * pixel in a request (ie not a scale based on real world size of the entire
     * layer).
     * <p>
     * Some web map servers will refuse to render images at a scale greater than
     * the value provided here.
     * <p>
     * return Max scale denominator for which it is appropriate to draw this layer.
     */
    public double getScaleDenominatorMax() {
        return scaleDenominatorMax;
    }
    /**
     * Min scale denominator for which it is appropriate to draw this layer.
     * <p>
     * Scale denominator is calculated based on the bounding box of the central
     * pixel in a request (ie not a scale based on real world size of the entire
     * layer).
     * @param  Min scale denominator for which it is appropriate to draw this layer
     */
    public void setScaleDenominatorMin( double scaleDenominatorMin ) {
        this.scaleDenominatorMin = scaleDenominatorMin;
    }
    /**
     * Min scale denominator for which it is appropriate to draw this layer.
     * <p>
     * Scale denominator is calculated based on the bounding box of the central
     * pixel in a request (ie not a scale based on real world size of the entire
     * layer).
     * <p>
     * Some web map servers will refuse to render images at a scale less than
     * the value provided here.
     * <p>
     * return  Min scale denominator for which it is appropriate to draw this layer
     */
    public double getScaleDenominatorMin() {
        return scaleDenominatorMin;
    }
    /**
     * Maximum scale for which this layer is considered good.
     * <p>
     * We assume this calculation is done in a similar manner to getScaleDenominatorMax();
     * but a look at common web services such as JPL show this not to be the case.
     * <p>
     * @return The second scale hint value (understood to mean the max value)
     * @deprecated Use getScaleDenomiatorMax() as there is less confusion over meaning
     */
    public double getScaleHintMax() {
		return scaleHintMax;
	}

    /**
     * Maximum scale for which this layer is considered good.
     * <p>
     * We assume this calculation is done in a similar manner to setScaleDenominatorMax();
     * but a look at common web services such as JPL show this not to be the case.
     * <p>
     * @param The second scale hint value (understood to mean the max value)
     * @deprecated Use setScaleDenomiatorMax() as there is less confusion over meaning
     */
	public void setScaleHintMax(double scaleHintMax) {
		this.scaleHintMax = scaleHintMax;
	}
    /**
     * Minimum scale for which this layer is considered good.
     * <p>
     * We assume this calculation is done in a similar manner to getScaleDenominatorMin();
     * but a look at common web services such as JPL show this not to be the case.
     * <p>
     * @return The first scale hint value (understood to mean the min value)
     * @deprecated Use getScaleDenomiatorMin() as there is less confusion over meaning
     */
	public double getScaleHintMin() {
		return scaleHintMin;
	}

    /**
     * Minimum scale for which this layer is considered good.
     * <p>
     * We assume this calculation is done in a similar manner to setScaleDenominatorMin();
     * but a look at common web services such as JPL show this not to be the case.
     * <p>
     * param  The first scale hint value (understood to mean the min value)
     * @deprecated Use setScaleDenomiatorMin() as there is less confusion over meaning
     */
	public void setScaleHintMin(double scaleHintMin) {
		this.scaleHintMin = scaleHintMin;
	}

	public GeneralEnvelope getEnvelope(CoordinateReferenceSystem crs) {
		{
			GeneralEnvelope result=(GeneralEnvelope) envelopeCache.get(crs);
			if( result!=null )
				return result;
		}
		Collection identifiers = crs.getIdentifiers();
        if( crs==DefaultGeographicCRS.WGS84 || crs==DefaultGeographicCRS.WGS84_3D ){
            identifiers=Arrays.asList(new String[]{"EPSG:4326"}); //$NON-NLS-1$
        }
        for (final Iterator i=identifiers.iterator(); i.hasNext();) {
            String epsgCode = i.next().toString();

            CRSEnvelope tempBBox = null;
            Layer parentLayer = this;

            //Locate a BBOx if we can
            while( tempBBox == null && parentLayer != null ) {
                HashMap boxes = parentLayer.getBoundingBoxes();
                if(boxes!=null){
                	tempBBox = (CRSEnvelope) boxes.get(epsgCode);
                
                	parentLayer = parentLayer.getParent();
                }
            }
    
            //Otherwise, locate a LatLon BBOX
    
            if (tempBBox == null && ("EPSG:4326".equals(epsgCode.toUpperCase()))) { //$NON-NLS-1$
                CRSEnvelope latLonBBox = null;
    
                parentLayer = this;
                while (latLonBBox == null && parentLayer != null) {
                    latLonBBox = parentLayer.getLatLonBoundingBox();
                    if (latLonBBox != null) {
                        try {
                            new GeneralEnvelope(new double[] {latLonBBox.getMinX(), latLonBBox.getMinY()}, 
                                    new double[] { latLonBBox.getMaxX(), latLonBBox.getMaxY() });
                            break;
                        } catch (IllegalArgumentException e) {
                            //TODO LOG here
                            //log("Layer "+layer.getName()+" has invalid bbox declared: "+tempBbox.toString());
                            latLonBBox = null;
                        }
                    }
                    parentLayer = parentLayer.getParent();
                }
                
                if (latLonBBox == null) {
                    //TODO could convert another bbox to latlon?
                    tempBBox = new CRSEnvelope("EPSG:4326", -180, -90, 180, 90);
                }else{
                	tempBBox = new CRSEnvelope("EPSG:4326", latLonBBox.getMinX(), latLonBBox.getMinY(), latLonBBox.getMaxX(), latLonBBox.getMaxY());
                }
            }
            
            if (tempBBox == null) {
                //Haven't found a bbox in the requested CRS. Attempt to transform another bbox
                
                String epsg = null;
                if (getLatLonBoundingBox() != null) {
                    CRSEnvelope latLonBBox = getLatLonBoundingBox();
                    tempBBox = new CRSEnvelope("EPSG:4326", latLonBBox.getMinX(), latLonBBox.getMinY(), latLonBBox.getMaxX(), latLonBBox.getMaxY());
                    epsg = "EPSG:4326";
                }
                
                if (tempBBox == null && getBoundingBoxes() != null && getBoundingBoxes().size() > 0) {
                    tempBBox = (CRSEnvelope) getBoundingBoxes().values().iterator().next();
                    epsg = tempBBox.getEPSGCode();
                }
                
                if (tempBBox == null) {
                    continue;
                }
                
                GeneralEnvelope env = new GeneralEnvelope(new double[] { tempBBox.getMinX(), tempBBox.getMinY()}, 
                        new double[] { tempBBox.getMaxX(), tempBBox.getMaxY() });
                
                CoordinateReferenceSystem fromCRS = null;
                try {
                    fromCRS = CRS.decode(epsg);
                    
                    ReferencedEnvelope oldEnv = new ReferencedEnvelope(
                		env.getMinimum(0),env.getMaximum(0), env.getMinimum(1),env.getMaximum(1),fromCRS);
                    ReferencedEnvelope newEnv = oldEnv.transform(crs, true);
                    
                    env = new GeneralEnvelope(new double[] {newEnv.getMinimum(0), newEnv.getMinimum(1)}, 
                                              new double[] {newEnv.getMaximum(0), newEnv.getMaximum(1)});
                    env.setCoordinateReferenceSystem(crs);
                    
                    //success!!
                    envelopeCache.put(crs, env);
                    return env;
                    
                } catch (NoSuchAuthorityCodeException e) {
                    // TODO Catch e
                } catch (FactoryException e) {
                    // TODO Catch e
                } catch (MismatchedDimensionException e) {
                    // TODO Catch e
                } catch (TransformException e) {
                    // TODO Catch e
                }
            }
            
            //TODO Attempt to figure out the valid area of the CRS and use that.
            
            if (tempBBox != null) {
                GeneralEnvelope env = new GeneralEnvelope(new double[] { tempBBox.getMinX(), tempBBox.getMinY()}, 
                        new double[] { tempBBox.getMaxX(), tempBBox.getMaxY() });
                env.setCoordinateReferenceSystem(crs);
                return env;
            }
    
        }
        return null;
	}
}
