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
package org.geotools.renderer.lite;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.Decimator;
import org.geotools.geometry.jts.LiteShape2;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.geotools.util.Range;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Default LabelCache Implementation
 *
 * DJB (major changes on May 11th, 2005): 1.The old version of the labeler, if
 * given a *set* of points, lines, or polygons justed labels the first item in
 * the set. The sets are formed when you want to only put a single "Main St" on
 * the map even if you have a bunch of small "Main St" segments.
 *
 * I changed this to be much much wiser.
 *
 * Basically, the new way looks at the set of geometries that its going to put a
 * label on and find the "best" one that represents it. That geometry is then
 * labeled (see below for details on where that label is placed).
 *
 * 2. I changed the actual drawing routines;
 *
 * 1. get the "representative geometry" 2. for points, label as before 3. for
 * lines, find the middle point on the line (old version just averaged start and
 * end points) and centre label on that point (rotated) 4. for polygon, put the
 * label in the middle
 *
 * 3.
 *
 * ie. for lines, try the label at the 1/3, 1/2, and 2/3 location. Metric is how
 * close the label bounding box is to the line.
 *
 * ie. for polygons, bisect the polygon (about the centroid) in to North, South,
 * East and West polygons. Use the location that has the label best inside the
 * polygon.
 *
 * After this is done, you can start doing constraint relaxation...
 *
 * 4. TODO: deal with labels going off the edge of the screen (much reduced
 * now). 5. TODO: add a "minimum quality" parameter (ie. if you're labeling a
 * tiny polygon with a tiny label, dont bother). Metrics are descibed in #3. 6.
 * TODO: add ability for SLD to tweak parameters (ie. "always label").
 *
 *
 * ------------------------------------------------------------------------------------------
 * I've added extra functionality; a) priority -- if you set the <Priority> in a
 * TextSymbolizer, then you can control the order of labelling ** see mailing
 * list for more details b) <VendorOption name="group">no</VendorOption> ---
 * turns off grouping for this symbolizer c) <VendorOption name="spaceAround">5</VendorOption> --
 * do not put labels within 5 pixels of this label.
 *
 * @author jeichar
 * @author dblasby
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/render/src/main/java/org/geotools/renderer/lite/LabelCacheDefault.java $
 */
public final class LabelCacheDefault implements LabelCache {

	/**
	 * labels that arent this good will not be shown
	 */
	public double MIN_GOODNESS_FIT = 0.7;

	public double DEFAULT_PRIORITY = 1000.0;

	/** Map<label, LabelCacheItem> the label cache */
	protected Map labelCache = new HashMap();

	/** non-grouped labels get thrown in here* */
	protected ArrayList labelCacheNonGrouped = new ArrayList();

	public boolean DEFAULT_GROUP = false; // what to do if there's no grouping option

	public int DEFAULT_SPACEAROUND = 0;
	
	/**
	 * When true, the text is rendered as its GlyphVector outline (as a geometry) instead of using
	 * drawGlypVector. Pro: labels and halos are perfectly centered, some people prefer the 
	 * extra antialiasing obtained. Cons: possibly slower, some people do not like the 
	 * extra antialiasing :) 
	 */
	protected boolean outlineRenderingEnabled = false;
	
	protected SLDStyleFactory styleFactory=new SLDStyleFactory();
	boolean stop=false;
	Set enabledLayers=new HashSet();
	Set activeLayers=new HashSet();
	
	LineLengthComparator lineLengthComparator = new LineLengthComparator ();

	private boolean needsOrdering=false;
	
	public void stop() {
		stop = true;
		activeLayers.clear();
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#start()
	 */
	public void start() {
		stop = false;
	}
	
	public void clear() {
		if( !activeLayers.isEmpty() ){
			throw new IllegalStateException( activeLayers+" are layers that started rendering but have not completed," +
					" stop() or endLayer() must be called before clear is called" );
		}
		needsOrdering=true;
        labelCache.clear();
        labelCacheNonGrouped.clear();
		enabledLayers.clear();
	}

	public void clear(String layerId){
		if( activeLayers.contains(layerId) ){
			throw new IllegalStateException( layerId+" is still rendering, end the layer before calling clear." );
		}
		needsOrdering=true;

		for (Iterator iter = labelCache.values().iterator(); iter.hasNext();) {
			LabelCacheItem item = (LabelCacheItem) iter.next();
			if( item.getLayerIds().contains(layerId) )
				iter.remove();
		}
		for (Iterator iter = labelCacheNonGrouped
				.iterator(); iter.hasNext();) {
			LabelCacheItem item = (LabelCacheItem) iter.next();
			if( item.getLayerIds().contains(layerId) )
				iter.remove();
		}
		
		enabledLayers.remove(layerId);
		
	}
	
	public void disableLayer(String layerId) {
		needsOrdering=true;
		enabledLayers.remove(layerId);
	}
	/**
	 * @see org.geotools.renderer.lite.LabelCache#startLayer()
	 */
	public void startLayer(String layerId) {
		enabledLayers.add(layerId);
		activeLayers.add(layerId);
	}

	/**
	 * get the priority from the symbolizer its an expression, so it will try to
	 * evaluate it: 1. if its missing --> DEFAULT_PRIORITY 2. if its a number,
	 * return that number 3. if its not a number, convert to string and try to
	 * parse the number; return the number 4. otherwise, return DEFAULT_PRIORITY
	 *
	 * @param symbolizer
	 * @param feature
	 */
	public double getPriority(TextSymbolizer symbolizer, SimpleFeature feature) {
		if (symbolizer.getPriority() == null)
			return DEFAULT_PRIORITY;

		// evaluate
        try {
            Double number = (Double) symbolizer.getPriority().evaluate( feature, Double.class );
            return number.doubleValue();
        } catch (Exception e) {
			return DEFAULT_PRIORITY;
		}
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#put(org.geotools.renderer.style.TextStyle2D,
	 *      org.geotools.renderer.lite.LiteShape)
	 */
	public void put(String layerId, TextSymbolizer symbolizer, SimpleFeature feature, LiteShape2 shape, NumberRange scaleRange) 
	{
		needsOrdering=true;
		try{
			//get label and geometry				
		    String label = (String) symbolizer.getLabel().evaluate(feature, String.class);
		    
			if (label == null) return;
            
		    label = label.trim();
		    if (label.length() ==0){
		    	return; // dont label something with nothing!
            }
		    double priorityValue = getPriority(symbolizer,feature);
		    boolean group = isGrouping(symbolizer);
		    if (!(group))
		    {
		    	TextStyle2D textStyle=(TextStyle2D) styleFactory.createStyle(feature, symbolizer, scaleRange);
		    	
				LabelCacheItem item = new LabelCacheItem(layerId, textStyle, shape,label);
				item.setPriority(priorityValue);
				item.setSpaceAround(getSpaceAround(symbolizer));
				labelCacheNonGrouped.add(item);
			} else { // / --------- grouping case ----------------

				// equals and hashcode of LabelCacheItem is the hashcode of
				// label and the
				// equals of the 2 labels so label can be used to find the
				// entry.

				// DJB: this is where the "grouping" of 'same label' features
				// occurs
				LabelCacheItem lci = (LabelCacheItem) labelCache.get(label);
				if (lci == null) // nothing in there yet!
				{
					TextStyle2D textStyle = (TextStyle2D) styleFactory
							.createStyle(feature, symbolizer, scaleRange);
					LabelCacheItem item = new LabelCacheItem(layerId, textStyle, shape,
							label);
					item.setPriority(priorityValue);
					item.setSpaceAround(getSpaceAround(symbolizer));
					labelCache.put(label, item);
				} else {
					// add only in the non-default case or non-literal. Ie.
					// area()
					if ((symbolizer.getPriority() != null)
							&& (!(symbolizer.getPriority() instanceof Literal)))
						lci.setPriority(lci.getPriority() + priorityValue); // djb--
					// changed
					// because
					// you
					// do
					// not
					// always
					// want
					// to
					// add!

					lci.getGeoms().add(shape.getGeometry());
				}
			}
		} catch (Exception e) // DJB: protection if there's a problem with the
		// decimation (getGeometry() can be null)
		{
			// do nothing
		}
	}

	/**
	 * pull space around from the sybolizer options - defaults to
	 * DEFAULT_SPACEAROUND.
	 *
	 * <0 means "I can overlap other labels" be careful with this.
	 *
	 * @param symbolizer
	 */
	private int getSpaceAround(TextSymbolizer symbolizer) {
		String value = symbolizer.getOption("spaceAround");
		if (value == null)
			return DEFAULT_SPACEAROUND;
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return DEFAULT_SPACEAROUND;
		}
	}

	/**
	 * look at the options in the symbolizer for "group". return its value if
	 * not present, return "DEFAULT_GROUP"
	 *
	 * @param symbolizer
	 */
	private boolean isGrouping(TextSymbolizer symbolizer) {
		String value = symbolizer.getOption("group");
		if (value == null)
			return DEFAULT_GROUP;
		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")
				|| value.equalsIgnoreCase("1");
	}

	/**
	 * @see org.geotools.renderer.lite.LabelCache#endLayer(java.awt.Graphics2D,
	 *      java.awt.Rectangle)
	 */
	public void endLayer(String layerId, Graphics2D graphics, Rectangle displayArea) 
	{
		activeLayers.remove(layerId);
	}

	/**
	 * return a list with all the values in priority order. Both grouped and
	 * non-grouped
	 *
	 *
	 */
	public List orderedLabels() {
		ArrayList al = getActiveLabels();
		
		Collections.sort(al);
		Collections.reverse(al);
		return al;		
	}
	private ArrayList getActiveLabels() {
		Collection c = labelCache.values();
		ArrayList al = new ArrayList(); // modifiable (ie. sortable)
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			LabelCacheItem item = (LabelCacheItem) iter.next();
			if( isActive(item.getLayerIds()) )
					al.add(item);
		}

		for (Iterator iter = labelCacheNonGrouped.iterator(); iter.hasNext();) {
			LabelCacheItem item = (LabelCacheItem) iter.next();
			if( isActive(item.getLayerIds()) )
					al.add(item);
		}
		return al;
	}
	private boolean isActive(Set layerIds) {
		for (Iterator iter = layerIds.iterator(); iter.hasNext();) {
			String string = (String) iter.next();
			if( enabledLayers.contains(string) )
				return true;
			
		}
		return false;
	}
	/**
	 * @see org.geotools.renderer.lite.LabelCache#end(java.awt.Graphics2D,
	 *      java.awt.Rectangle)
	 */
	public void end(Graphics2D graphics, Rectangle displayArea) 
	{
	    final Object antialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	    final Object textAntialiasing = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        try {
            // if we are asked to antialias only text but we're drawing using the outline
            // method, we need to re-enable graphics antialiasing during label painting
            if (outlineRenderingEnabled
                    && antialiasing == RenderingHints.VALUE_ANTIALIAS_OFF
                    && textAntialiasing == RenderingHints.VALUE_TEXT_ANTIALIAS_ON) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            }
            paintLabels(graphics, displayArea);
        } finally {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
        }
    }

    void paintLabels(Graphics2D graphics, Rectangle displayArea) {
        if( !activeLayers.isEmpty() ){
			throw new IllegalStateException( activeLayers+" are layers that started rendering but have not completed," +
					" stop() or endLayer() must be called before end() is called" );
		}
		List glyphs=new ArrayList();
		
        // Hack: let's reduce the display area width and height by one pixel.
        // If the rendered image is 256x256, proper rendering of polygons and
        // lines occurr only if the display area is [0,0; 256,256], yet if you
        // try to render anything at [x,256] or [256,y] it won't show.
        // So, to avoid labels that happen to touch the border being cut
        // by one pixel, we reduce the display area. 
        // Feels hackish, don't have a better solution at the moment thought
        displayArea = new Rectangle(displayArea);
        displayArea.width -= 1;
        displayArea.height -= 1;

		GeometryFactory factory = new GeometryFactory();
		Geometry displayGeom = factory.toGeometry(new Envelope(displayArea
				.getMinX(), displayArea.getMaxX(), displayArea.getMinY(),
				displayArea.getMaxY()));

		
		List items; // both grouped and non-grouped
		if ( needsOrdering ){
			items = orderedLabels();
		} else {
			items = getActiveLabels();
		}
		for (Iterator labelIter = items.iterator(); labelIter.hasNext();) {
			if (stop)
				return;
			try {
				// LabelCacheItem labelItem = (LabelCacheItem)
				// labelCache.get(labelIter.next());
				LabelCacheItem labelItem = (LabelCacheItem) labelIter.next();
				labelItem.getTextStyle().setLabel(labelItem.getLabel());
				GlyphVector glyphVector = labelItem.getTextStyle()
						.getTextGlyphVector(graphics);

				// DJB: simplified this. Just send off to the point,line,or
				// polygon routine
				// NOTE: labelItem.getGeometry() returns the FIRST geometry, so
				// we're assuming that lines & points arent mixed
				// If they are, then the FIRST geometry determines how its
				// rendered (which is probably bad since it should be in
				// area,line,point order
				// TOD: as in NOTE above

				Geometry geom = labelItem.getGeometry();

				AffineTransform oldTransform = graphics.getTransform();
				/*
				* Just use identity for tempTransform because display area is 0,0,width,height
				* and oldTransform may have a different origin. OldTransform will be used later
				* for drawing.
				* -rg & je
				*/
 				AffineTransform tempTransform = new AffineTransform();

				Geometry representativeGeom = null;

				if ((geom instanceof Point) || (geom instanceof MultiPoint))
					representativeGeom = paintPointLabel(glyphVector,
							labelItem, tempTransform, displayGeom);
				else if (((geom instanceof LineString) && !(geom instanceof LinearRing))
						|| (geom instanceof MultiLineString))
					representativeGeom = paintLineLabel(glyphVector, labelItem,
							tempTransform, displayGeom);
				else if (geom instanceof Polygon
						|| geom instanceof MultiPolygon
						|| geom instanceof LinearRing)
					representativeGeom = paintPolygonLabel(glyphVector,
							labelItem, tempTransform, displayGeom);

				// DJB: this is where overlapping labels are forbidden (first
				// out of the map has priority)
				Rectangle glyphBounds = glyphVector.getPixelBounds(null, 0, 0);

				glyphBounds=tempTransform.createTransformedShape(glyphBounds).getBounds();
				
                // is  this offscreen? We assume offscreen as anything that is outside
                // or crosses the rendering borders, since in tiled rendering
                // we have to insulate ourself from other tiles
				if (!(displayArea.contains(glyphBounds))) 
					continue;

				// we wind up using the translated shield location a number of
				// times, in overlap calculations, offscreen
				// calculations, etc. Let's just pre-calculate it here, as we do
				// the offscreen calculation.
				Rectangle2D shieldBounds = null;
				if (labelItem.getTextStyle().getGraphic() != null) {
					Rectangle area = labelItem.getTextStyle()
							.getGraphicDimensions();
                    Rectangle untransformedBounds = glyphVector.getPixelBounds(
                            new FontRenderContext(new AffineTransform(),true, false), 0, 0);
                    // center the graphics on the labels back
                    double[] shieldVerts = new double[] {
                            -area.width / 2 + untransformedBounds.x - untransformedBounds.width / 2,
                            -area.height / 2 + untransformedBounds.y - untransformedBounds.height / 2,
                            area.width / 2,
                            area.height / 2}; 
                    // transform to rendered space
                    tempTransform.transform(shieldVerts, 0, shieldVerts, 0, 2);
                    shieldBounds = new Rectangle2D.Double(shieldVerts[0] + glyphBounds.width / 2,
                            shieldVerts[1] + glyphBounds.height / 2, shieldVerts[2] - shieldVerts[0],
                            shieldVerts[3] - shieldVerts[1]);
                    // if glyph is only partially outside of the display area, don't render it
                    // for the same req
                    if (!displayArea.contains(shieldBounds))
                        continue; 
				}

				// take into account radius so that halo do not overwrite other labels
                // that are too close to the current one
				int space = labelItem.getSpaceAround();
                int haloRadius = Math.round(labelItem.getTextStyle().getHaloFill() != null ? 
                        labelItem.getTextStyle().getHaloRadius() : 0);
				if (space >= 0) // if <0 then its okay to have overlapping items
				{
					if (overlappingItems(glyphBounds, glyphs, space + haloRadius))
						continue;
					if (shieldBounds != null
							&& overlappingItems(shieldBounds.getBounds(),
									glyphs, space))
						continue;
				}

				if (goodnessOfFit(glyphVector, tempTransform,
						representativeGeom) < MIN_GOODNESS_FIT)
					continue;

				try {
					/*
					* Merge the tempTransform with the transform provided by graphics. This is the
					* proper transform that should be used for drawing.
					* -je & rg
					*/
					AffineTransform newTransform = new AffineTransform(oldTransform);
					newTransform.concatenate(tempTransform);
 				    graphics.setTransform(newTransform);

					if (labelItem.getTextStyle().getGraphic() != null) {

						// draw the label shield first, underneath the halo
						LiteShape2 tempShape = new LiteShape2(
								new GeometryFactory()
										.createPoint(new Coordinate(
												glyphBounds.width / 2.0, -1.0
														* glyphBounds.height
														/ 2.0)), null, null,
								false, false);

						// labels should always draw, so we'll just force this
						// one to draw by setting it's min/max scale to 0<10 and
						// then
						// drawing at scale 5.0 on the next line
						labelItem.getTextStyle().getGraphic().setMinMaxScale(
								0.0, 10.0);
						new StyledShapePainter(this).paint(graphics, tempShape,
								labelItem.getTextStyle().getGraphic(), 5.0);
						graphics.setTransform(tempTransform);
					}

					java.awt.Shape outline = glyphVector.getOutline();
					if (labelItem.getTextStyle().getHaloFill() != null) {
						graphics.setPaint(labelItem.getTextStyle()
								.getHaloFill());
						graphics.setComposite(labelItem.getTextStyle()
								.getHaloComposite());
						
						graphics.setStroke(new BasicStroke(2f * haloRadius, BasicStroke.CAP_ROUND,
								BasicStroke.JOIN_ROUND));
						graphics.draw(outline);
					}
					// DJB: added this because several people were using
					// "font-color" instead of fill
					// It legal to have a label w/o fill (which means dont
					// render it)
					// This causes people no end of trouble.
					// If they dont want to colour it, then they should use a
					// filter
					// DEFAULT (no <Fill>) --> BLACK
					// NOTE: re-reading the spec says this is the correct
					// assumption.
					Paint fill = labelItem.getTextStyle().getFill();
					Composite comp = labelItem.getTextStyle().getComposite();
					if (fill == null) {
						fill = Color.BLACK;
						comp = AlphaComposite.getInstance(
								AlphaComposite.SRC_OVER, 1.0f); // 100% opaque
					}
					if (fill != null) {
						graphics.setPaint(fill);
						graphics.setComposite(comp);
						if(outlineRenderingEnabled)
						    graphics.fill(outline);
						else
						    graphics.drawGlyphVector(glyphVector, 0, 0);
						Rectangle bounds = glyphVector.getPixelBounds(
								new FontRenderContext(tempTransform, true,
										false), 0, 0);
						int extraSpace = labelItem.getSpaceAround();
						if (extraSpace >= 0) // if <0 then we dont record
						// (something can overwrite it)
						{
							bounds = new Rectangle(bounds.x - extraSpace,
									bounds.y - extraSpace, bounds.width
											+ extraSpace, bounds.height
											+ extraSpace);
							if ((shieldBounds != null)) {
								bounds.add(shieldBounds);
							}
                            bounds.grow(haloRadius, haloRadius);
							glyphs.add(bounds);
						}
					}
				} finally {
					graphics.setTransform(oldTransform);
				}
			} catch (Exception e) {
                // the decimation can cause problems - we
			    // try to minimize it
                // do nothing
			}
    	}
    }

	/**
	 * how well does the label "fit" with the geometry. 1. points ALWAYS RETURNS
	 * 1.0 2. lines ALWAYS RETURNS 1.0 (modify polygon method to handle rotated
	 * labels) 3. polygon + assume: polylabels are unrotated + assume: polygon
	 * could be invalid + dont worry about holes
	 *
	 * like to RETURN area of intersection between polygon and label bounds, but
	 * thats expensive and likely to give us problems due to invalid polygons
	 * SO, use a sample method - make a few points inside the label and see if
	 * they're "close to" the polygon The method sucks, but works well...
	 *
	 * @param glyphVector
	 * @param tempTransform
	 * @param representativeGeom
	 */
	private double goodnessOfFit(GlyphVector glyphVector,
			AffineTransform tempTransform, Geometry representativeGeom) {
		if (representativeGeom instanceof Point) {
			return 1.0;
		}
		if (representativeGeom instanceof LineString) {
			return 1.0;
		}
		if (representativeGeom instanceof Polygon) {
			Rectangle glyphBounds = glyphVector.getPixelBounds(
					new FontRenderContext(tempTransform, true, false), 0, 0);
			try {
				Polygon p = simplifyPoly((Polygon) representativeGeom);
				int count = 0;
				int n = 10;
				double mindistance = (glyphBounds.height);
				for (int t = 1; t < (n + 1); t++) {
					Coordinate c = new Coordinate(glyphBounds.x
							+ ((double) glyphBounds.width)
							* (((double) t) / (n + 1)), glyphBounds
							.getCenterY());
					Point pp = new Point(c, representativeGeom
							.getPrecisionModel(), representativeGeom.getSRID());
					if (p.distance(pp) < mindistance)

					{
						count++;
					}
				}
				return ((double) count) / n;
			} catch (Exception e) {
				representativeGeom.geometryChanged(); // djb -- jessie should
				// do this during
				// generalization
				Envelope ePoly = representativeGeom.getEnvelopeInternal();
				Envelope eglyph = new Envelope(glyphBounds.x, glyphBounds.x
						+ glyphBounds.width, glyphBounds.y, glyphBounds.y
						+ glyphBounds.height);
				Envelope inter = intersection(ePoly, eglyph);
				if (inter != null)
					return (inter.getWidth() * inter.getHeight())
							/ (eglyph.getWidth() * eglyph.getHeight());
				return 0.0;
			}
		}
		return 0.0;
	}

	/**
	 * Remove holes from a polygon
	 *
	 * @param polygon
	 */
	private Polygon simplifyPoly(Polygon polygon) {
	    if(polygon.getNumInteriorRing() == 0)
	        return polygon;
	    
		LineString outer = polygon.getExteriorRing();
		if (outer.getStartPoint().distance(outer.getEndPoint()) != 0) {
			List clist = new ArrayList(Arrays.asList(outer.getCoordinates()));
			clist.add(outer.getStartPoint().getCoordinate());
			outer = outer.getFactory().createLinearRing(
					(Coordinate[]) clist.toArray(new Coordinate[clist.size()]));
		}
		LinearRing r = (LinearRing) outer;

		return outer.getFactory().createPolygon(r, null);
	}

	/**
	 * Determines whether labelItems overlaps a previously rendered label.
	 *
	 * @param glyphs
	 *            list of bounds of previously rendered glyphs/shields.
	 * @param bounds
	 *            new rectangle to check
	 * @param extraSpace
	 *            extra space added to edges of bounds during check
	 * @return true if labelItem overlaps a previously rendered glyph.
	 */
	private boolean overlappingItems(Rectangle bounds, List glyphs,
			int extraSpace) {
		bounds = new Rectangle(bounds.x - extraSpace, bounds.y - extraSpace,
				bounds.width + extraSpace, bounds.height + extraSpace);
		Rectangle oldBounds;
		for (Iterator iter = glyphs.iterator(); iter.hasNext();) {
			oldBounds = (Rectangle) iter.next();
			if (oldBounds.intersects(bounds))
				return true;
		}
		return false;
	}

	private Geometry paintLineLabel(GlyphVector glyphVector,
			LabelCacheItem labelItem, AffineTransform tempTransform,
			Geometry displayGeom) {
		LineString line = (LineString) getLineSetRepresentativeLocation(
				labelItem.getGeoms(), displayGeom);

		if (line == null)
			return null;

		TextStyle2D textStyle = labelItem.getTextStyle();

		paintLineStringLabel(glyphVector, line, textStyle, tempTransform);
		return line;
	}

	/**
	 * This handles point and line placement.
	 *
	 * 1. lineplacement -- calculate a rotation and location (and does the perp
	 * offset) 2. pointplacement -- reduce line to a point and ignore the
	 * calculated rotation
	 *
	 * @param glyphVector
	 * @param line
	 * @param textStyle
	 * @param tempTransform
	 */
	private void paintLineStringLabel(GlyphVector glyphVector, LineString line,
			TextStyle2D textStyle, AffineTransform tempTransform) {
		//Point start = line.getStartPoint();
		//Point end = line.getEndPoint();
		//double dx = end.getX() - start.getX();
		//double dy = end.getY() - start.getY();
		//double slope = dy / dx;
		//double theta = Math.atan(slope);
		// double rotation=theta;

		Rectangle2D textBounds = glyphVector.getVisualBounds();
		Point centroid = middleLine(line, 0.5); // DJB: changed from centroid to
		// "middle point" -- see
		// middleLine() dox
		// DJB: this is also where you could do "voting" and looking at other
		// locations on the line to label (ie. 0.33,0.66)
		tempTransform.translate(centroid.getX(), centroid.getY());
		double displacementX = 0;
		double displacementY = 0;

		// DJB: this now does "centering"
		// displacementX = (textStyle.getAnchorX() +
		// (-textBounds.getWidth()/2.0))
		// + textStyle.getDisplacementX();
		// displacementY = (textStyle.getAnchorY() +
		// (textBounds.getHeight()/2.0))
		// - textStyle.getDisplacementY();

		double anchorX = textStyle.getAnchorX();
		double anchorY = textStyle.getAnchorY();

		// undo the above if its point placement!
		double rotation;
		if (textStyle.isPointPlacement()) {
			rotation = textStyle.getRotation(); // use the one the user
			// supplied!
		} else // lineplacement
		{
			rotation = middleTheta(line, 0.5);
			displacementY -= textStyle.getPerpendicularOffset(); // move it
			// off the
			// line
			anchorX = 0.5; // centered
			anchorY = 0.5; // centered, sitting on line
		}

		displacementX = (anchorX * (-textBounds.getWidth()))
				+ textStyle.getDisplacementX();
		displacementY += (anchorY * (textBounds.getHeight()))
				- textStyle.getDisplacementY();

		if (rotation != rotation) // IEEE def'n x=x for all x except when x is
			// NaN
			rotation = 0.0;
		if (Double.isInfinite(rotation))
			rotation = 0; // weird number
		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
	}

	/**
	 * Simple to paint a point (or set of points) Just choose the first one and
	 * paint it!
	 *
	 */
	private Geometry paintPointLabel(GlyphVector glyphVector,
			LabelCacheItem labelItem, AffineTransform tempTransform,
			Geometry displayGeom) {
		// get the point onto the shape has to be painted
		Point point = getPointSetRepresentativeLocation(labelItem.getGeoms(),
				displayGeom);
		if (point == null)
			return null;

		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(point.getX(), point.getY());
		double displacementX = 0;
		double displacementY = 0;

		// DJB: this probably isnt doing what you think its doing - see others
		displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
				+ textStyle.getDisplacementX();
		displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
				- textStyle.getDisplacementY();

		if (!textStyle.isPointPlacement()) {
			// lineplacement. We're cheating here, since we cannot line label a
			// point
			displacementY -= textStyle.getPerpendicularOffset(); // just move
			// it up
			// (yes, its
			// cheating)
		}

		double rotation = textStyle.getRotation();
		if (rotation != rotation) // IEEE def'n x=x for all x except when x is
			// NaN
			rotation = 0.0;
		if (Double.isInfinite(rotation))
			rotation = 0; // weird number

		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
		return point;
	}

	/**
	 * returns the representative geometry (for further processing)
	 *
	 * TODO: handle lineplacement for a polygon (perhaps we're supposed to grab
	 * the outside line and label it, but spec is unclear)
	 */
	private Geometry paintPolygonLabel(GlyphVector glyphVector,
			LabelCacheItem labelItem, AffineTransform tempTransform,
			Geometry displayGeom) {
		Polygon geom = getPolySetRepresentativeLocation(labelItem.getGeoms(),
				displayGeom);
		if (geom == null)
			return null;

		Point centroid;

		try {
			centroid = geom.getCentroid();
		} catch (Exception e) // generalized polygons causes problems - this
		// tries to hid them.
		{
			try {
				centroid = geom.getExteriorRing().getCentroid();
			} catch (Exception ee) {
				try {
					centroid = geom.getFactory().createPoint(
							geom.getCoordinate());
				} catch (Exception eee) {
					return null; // we're hooped
				}
			}
		}

		TextStyle2D textStyle = labelItem.getTextStyle();
		Rectangle2D textBounds = glyphVector.getVisualBounds();
		tempTransform.translate(centroid.getX(), centroid.getY());
		double displacementX = 0;
		double displacementY = 0;

		// DJB: this now does "centering"
		displacementX = (textStyle.getAnchorX() * (-textBounds.getWidth()))
				+ textStyle.getDisplacementX();
		displacementY = (textStyle.getAnchorY() * (textBounds.getHeight()))
				- textStyle.getDisplacementY();

		if (!textStyle.isPointPlacement()) {
			// lineplacement. We're cheating here, since we've reduced the
			// polygon to a point, when we should be trying to do something
			// a little smarter (like find its median axis!)
			displacementY -= textStyle.getPerpendicularOffset(); // just move
			// it up
			// (yes, its
			// cheating)
		}

		double rotation = textStyle.getRotation();
		if (rotation != rotation) // IEEE def'n x=x for all x except when x is
			// NaN
			rotation = 0.0;
		if (Double.isInfinite(rotation))
			rotation = 0; // weird number

		tempTransform.rotate(rotation);
		tempTransform.translate(displacementX, displacementY);
		return geom;
	}

	/**
	 *
	 * 1. get a list of points from the input geometries that are inside the
	 * displayGeom NOTE: lines and polygons are reduced to their centroids (you
	 * shouldnt really calling this with lines and polys) 2. choose the most
	 * "central" of the points METRIC - choose anyone TODO: change metric to be
	 * "closest to the centoid of the possible points"
	 *
	 * @param geoms
	 *            list of Point or MultiPoint (any other geometry types are
	 *            rejected
	 * @param displayGeometry
	 * @return a point or null (if there's nothing to draw)
	 */
	Point getPointSetRepresentativeLocation(List geoms, Geometry displayGeometry) {
		ArrayList pts = new ArrayList(); // points that are inside the
		// displayGeometry

		Iterator it = geoms.iterator();
		Geometry g;
		while (it.hasNext()) {
			g = (Geometry) it.next();
			if (!((g instanceof Point) || (g instanceof MultiPoint))) // handle
				// lines,polys,
				// gc,
				// etc..
				g = g.getCentroid(); // will be point
			if (g instanceof Point) {
				if (displayGeometry.intersects(g)) // this is robust!
					pts.add(g); // possible label location
			} else if (g instanceof MultiPoint) {
				for (int t = 0; t < g.getNumGeometries(); t++) {
					Point gg = (Point) g.getGeometryN(t);
					if (displayGeometry.intersects(gg))
						pts.add(gg); // possible label location
				}
			}
		}
		if (pts.size() == 0)
			return null;

		// do better metric than this:
		return (Point) pts.get(0);
	}

	/**
	 * 1. make a list of all the geoms (not clipped) NOTE: reject points,
	 * convert polygons to their exterior ring (you shouldnt be calling this
	 * function with points and polys) 2. join the lines together 3. clip
	 * resulting lines to display geometry 4. return longest line
	 *
	 * NOTE: the joining has multiple solution. For example, consider a Y (3
	 * lines): * * 1 2 * * * 3 * solutions are: 1->2 and 3 1->3 and 2 2->3 and 1
	 *
	 * (see mergeLines() below for detail of the algorithm; its basically a
	 * greedy algorithm that should form the 'longest' possible route through
	 * the linework)
	 *
	 * NOTE: we clip after joining because there could be connections "going on"
	 * outside the display bbox
	 *
	 *
	 * @param geoms
	 * @param displayGeometry
	 *            must be poly
	 */
	LineString getLineSetRepresentativeLocation(List geoms,
			Geometry displayGeometry) {
		ArrayList lines = new ArrayList(); // points that are inside the
		// displayGeometry

		Iterator it = geoms.iterator();
		Geometry g;
		// go through each geometry in the set.
		// if its a polygon or multipolygon, get the boundary (reduce to a line)
		// if its a line, add it to "lines"
		// if its a multiline, add each component line to "lines"
		while (it.hasNext()) {
			g = (Geometry) it.next();
			if (!((g instanceof LineString) || (g instanceof MultiLineString)
					|| (g instanceof Polygon) || (g instanceof MultiPolygon)))
				continue;

			if ((g instanceof Polygon) || (g instanceof MultiPolygon)) {
				g = g.getBoundary(); // line or multiline m
				// TODO: boundary included the inside rings, might want to
				// replace this with getExteriorRing()
				if (!((g instanceof LineString) || (g instanceof MultiLineString)))
					continue; // protection
			} else if (g instanceof LineString) {
				if (g.getLength() != 0)
					lines.add(g);
			} else // multiline
			{
				for (int t = 0; t < g.getNumGeometries(); t++) {
					LineString gg = (LineString) g.getGeometryN(t);
					lines.add(gg);
				}
			}
		}
		if (lines.size() == 0)
			return null;

		// at this point "lines" now is a list of linestring

		// join
		// this algo doesnt always do what you want it to do, but its pretty
		// good
		Collection merged = this.mergeLines(lines);

		// clip to bounding box
		ArrayList clippedLines = new ArrayList();
		it = merged.iterator();
		LineString l;
		MultiLineString ll;
		Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
		while (it.hasNext()) {
			l = (LineString) it.next();
			ll = clipLineString(l, (Polygon) displayGeometry, displayGeomEnv);
			if ((ll != null) && (!(ll.isEmpty()))) {
				for (int t = 0; t < ll.getNumGeometries(); t++)
					clippedLines.add(ll.getGeometryN(t)); // more robust
				// clipper -- see
				// its dox
			}
		}

		// clippedLines is a list of LineString, all cliped (hopefully) to the
		// display geometry. we choose longest one
		if (clippedLines.size() == 0)
			return null;
		double maxLen = -1;
		LineString maxLine = null;
		LineString cline;
		for (int t = 0; t < clippedLines.size(); t++) {
			cline = (LineString) clippedLines.get(t);
			if (cline.getLength() > maxLen) {
				maxLine = cline;
				maxLen = cline.getLength();
			}
		}
		return maxLine; // longest resulting line
	}

	/**
	 * try to be more robust dont bother returning points
	 *
	 * This will try to solve robustness problems, but read code as to what it
	 * does. It might return the unclipped line if there's a problem!
	 *
	 * @param line
	 * @param bbox
	 *            MUST BE A BOUNDING BOX
	 */
	public MultiLineString clipLineString(LineString line, Polygon bbox,
			Envelope displayGeomEnv) {
		
		Geometry clip = line;
		line.geometryChanged();// djb -- jessie should do this during
		// generalization
		if (displayGeomEnv.contains(line.getEnvelopeInternal())) {
			// shortcut -- entirely inside the display rectangle -- no clipping
			// required!
			LineString[] lns = new LineString[1];
			lns[0] = (LineString) clip;
			return line.getFactory().createMultiLineString(lns);
		}
		try {
		        // the representative geometry does not need to be accurate, let's
                        // simplify it further before doing the overlay to reduce the overlay cost
                        Decimator d = new Decimator(10, 10);
                        d.decimate(line);
                        line.geometryChanged();
			clip = EnhancedPrecisionOp.intersection(line, bbox);
		} catch (Exception e) {
			// TODO: should try to expand the bounding box and re-do the
			// intersection, but line-bounding box
			// problems are quite rare.
			clip = line;// just return the unclipped version
		}
		if (clip instanceof MultiLineString)
			return (MultiLineString) clip;
		if (clip instanceof LineString) {
			LineString[] lns = new LineString[1];
			lns[0] = (LineString) clip;
			return line.getFactory().createMultiLineString(lns);
		}
		// otherwise we've got a point or line&point or empty
		if (clip instanceof Point)
			return null;
		if (clip instanceof MultiPoint)
			return null;

		// its a GC (Line intersection Poly cannot be a polygon/multipoly)
		GeometryCollection gc = (GeometryCollection) clip;
		ArrayList lns = new ArrayList();
		Geometry g;
		for (int t = 0; t < gc.getNumGeometries(); t++) {
			g = gc.getGeometryN(t);
			if (g instanceof LineString)
				lns.add(g);
			// dont think multilinestring is possible, but not sure
		}

		// convert to multilinestring
		if (lns.size() == 0)
			return null;

		return line.getFactory().createMultiLineString(
				(LineString[]) lns.toArray(new LineString[1]));

	}

	/**
	 * 1. make a list of all the polygons clipped to the displayGeometry NOTE:
	 * reject any points or lines 2. choose the largest of the clipped
	 * geometries
	 *
	 * @param geoms
	 * @param displayGeometry
	 */
	Polygon getPolySetRepresentativeLocation(List geoms,
			Geometry displayGeometry) {
		ArrayList polys = new ArrayList(); // points that are inside the
		// displayGeometry

		Iterator it = geoms.iterator();
		Geometry g;
		// go through each geometry in the input set
		// if its not a polygon or multipolygon ignore it
		// if its a polygon, add it to "polys"
		// if its a multipolgon, add each component to "polys"
		while (it.hasNext()) {
			g = (Geometry) it.next();
			if (!((g instanceof Polygon) || (g instanceof MultiPolygon)))
				continue;

			if (g instanceof Polygon) {
				polys.add(g);
			} else // multipoly
			{
				for (int t = 0; t < g.getNumGeometries(); t++) {
					Polygon gg = (Polygon) g.getGeometryN(t);
					polys.add(gg);
				}
			}
		}
		if (polys.size() == 0)
			return null;

		// at this point "polys" is a list of polygons

		// clip
		ArrayList clippedPolys = new ArrayList();
		it = polys.iterator();
		Polygon p;
		MultiPolygon pp;
		Envelope displayGeomEnv = displayGeometry.getEnvelopeInternal();
		while (it.hasNext()) {
			p = (Polygon) it.next();
			pp = clipPolygon(p, (Polygon) displayGeometry, displayGeomEnv);
			if ((pp != null) && (!(pp.isEmpty()))) {
				for (int t = 0; t < pp.getNumGeometries(); t++)
					clippedPolys.add(pp.getGeometryN(t)); // more robust
				// version -- see
				// dox
			}
		}
		// clippedPolys is a list of Polygon, all cliped (hopefully) to the
		// display geometry. we choose largest one
		if (clippedPolys.size() == 0)
			return null;
		if (clippedPolys.size() == 1)
			return (Polygon) clippedPolys.get(0);
		double maxSize = -1;
		Polygon maxPoly = null;
		Polygon cpoly;
		for (int t = 0; t < clippedPolys.size(); t++) {
			cpoly = (Polygon) clippedPolys.get(t);
			if (cpoly.getArea() > maxSize) {
				maxPoly = cpoly;
				maxSize = cpoly.getArea();
			}
		}
		return maxPoly;
	}

	/**
	 * try to do a more robust way of clipping a polygon to a bounding box. This
	 * might return the orginal polygon if it cannot clip TODO: this is a bit
	 * simplistic, there's lots more to do.
	 *
	 * @param poly
	 * @param bbox
	 * @param displayGeomEnv
	 *
	 * @return a MutliPolygon
	 */
	public MultiPolygon clipPolygon(Polygon poly, Polygon bbox,
			Envelope displayGeomEnv) {

		Geometry clip = poly;
		poly.geometryChanged();// djb -- jessie should do this during
		// generalization
		if (displayGeomEnv.contains(poly.getEnvelopeInternal())) {
			// shortcut -- entirely inside the display rectangle -- no clipping
			// required!
			Polygon[] polys = new Polygon[1];
			polys[0] = (Polygon) clip;
			return poly.getFactory().createMultiPolygon(polys);
		}

		try {
		        // the representative geometry does not need to be accurate, let's
	                // simplify it further before doing the overlay to reduce the overlay cost
	                Decimator d = new Decimator(10, 10);
	                d.decimate(poly);
	                poly.geometryChanged();
			clip = EnhancedPrecisionOp.intersection(poly, bbox);
		} catch (Exception e) {
			// TODO: should try to expand the bounding box and re-do the
			// intersection.
			// TODO: also, try removing the interior rings of the polygon

			clip = poly;// just return the unclipped version
		}
		if (clip instanceof MultiPolygon)
			return (MultiPolygon) clip;
		if (clip instanceof Polygon) {
			Polygon[] polys = new Polygon[1];
			polys[0] = (Polygon) clip;
			return poly.getFactory().createMultiPolygon(polys);
		}
		// otherwise we've got a point or line&point or empty
		if (clip instanceof Point)
			return null;
		if (clip instanceof MultiPoint)
			return null;
		if (clip instanceof LineString)
			return null;
		if (clip instanceof MultiLineString)
			return null;

		// its a GC
		GeometryCollection gc = (GeometryCollection) clip;
		ArrayList plys = new ArrayList();
		Geometry g;
		for (int t = 0; t < gc.getNumGeometries(); t++) {
			g = gc.getGeometryN(t);
			if (g instanceof Polygon)
				plys.add(g);
			// dont think multiPolygon is possible, but not sure
		}

		// convert to multipoly
		if (plys.size() == 0)
			return null;

		return poly.getFactory().createMultiPolygon(
				(Polygon[]) plys.toArray(new Polygon[1]));
	}

	/**
	 * see middlePoint() find the segment that the point is apart of, and return
	 * the slope.
	 *
	 * @param l
	 * @param percent
	 */
	double middleTheta(LineString l, double percent) {
		if (percent >= 1.0)
			percent = 0.99; // for precision
		if (percent <= 0)
			percent = 0.01; // for precision

		double len = l.getLength();
		double dist = percent * len;

		double running_sum_dist = 0;
		CoordinateSequence pts = l.getCoordinateSequence();
		double segmentLen;
		double dx;
		double dy;
		double slope;
		final int length = pts.size();
		Coordinate curr = new Coordinate();
		Coordinate next = new Coordinate();
		for (int i = 0; i < length - 1; i++) {
			pts.getCoordinate(i, curr);
			pts.getCoordinate(i + 1, next);
			segmentLen = curr.distance(next);

			if ((running_sum_dist + segmentLen) >= dist) {
				// it is on this segment pts[i] to pts[i+1]
				dx = (next.x - curr.x);
				dy = (next.y - curr.y);
				slope = dy / dx;
				return Math.atan(slope);
			}
			running_sum_dist += segmentLen;
		}
		return 0;
	}

	/**
	 * calculate the middle of a line. The returning point will be x% (0.5 =
	 * 50%) along the line and on the line.
	 *
	 *
	 * @param l
	 * @param percent
	 *            0=start, 0.5=middle, 1.0=end
	 */
	Point middleLine(LineString l, double percent) {
		if (percent >= 1.0)
			percent = 0.99; // for precision
		if (percent <= 0)
			percent = 0.01; // for precision

		double len = l.getLength();
		double dist = percent * len;

		double running_sum_dist = 0;
		Coordinate[] pts = l.getCoordinates();
		double segmentLen;
		final int length = pts.length;
		double r;
		Coordinate c;
		for (int i = 0; i < length - 1; i++) {
			segmentLen = pts[i].distance(pts[i + 1]);

			if ((running_sum_dist + segmentLen) >= dist) {
				// it is on this segment
				r = (dist - running_sum_dist) / segmentLen;
				c = new Coordinate(pts[i].x + (pts[i + 1].x - pts[i].x) * r,
						pts[i].y + (pts[i + 1].y - pts[i].y) * r);
				return l.getFactory().createPoint(c);
			}
			running_sum_dist += segmentLen;
		}

		return l.getEndPoint(); // precision protection
	}

	Collection mergeLines(Collection lines) {
		LineMerger lm = new LineMerger();
		lm.add(lines);
		Collection merged = lm.getMergedLineStrings(); // merged lines

		// Collection merged = lines;

		if (merged.size() == 0) {
			return null; // shouldnt happen
		}
		if (merged.size() == 1) // simple case - no need to continue merging
		{
			return merged;
		}

		Hashtable nodes = new Hashtable(merged.size() * 2); // coordinate ->
		// list of lines
		Iterator it = merged.iterator();
		while (it.hasNext()) {
			LineString ls = (LineString) it.next();
			putInNodeHash(ls, nodes);
		}

		ArrayList result = new ArrayList();
		ArrayList merged_list = new ArrayList(merged);

		// SORT -- sorting is important because order does matter.
		Collections.sort(merged_list, lineLengthComparator); // sorted
		// long->short
		processNodes(merged_list, nodes, result);
		// this looks for differences between the two methods.
		// Collection a = mergeLines2(lines);
		// if (a.size() != result.size())
		// {
		// System.out.println("bad");
		// boolean bb= false;
		// if (bb)
		// {
		// Collection b = mergeLines(lines);
		// }
		// }
		return result;
	}

	/**
	 * pull a line from the list, and: 1. if nothing connects to it (its
	 * issolated), add it to "result" 2. otherwise, merge it at the start/end
	 * with the LONGEST line there. 3. remove the original line, and the lines
	 * it merged with from the hashtables 4. go again, with the merged line
	 *
	 * @param edges
	 * @param nodes
	 * @param result
	 *
	 */
	public void processNodes(List edges, Hashtable nodes, ArrayList result) {
		int index = 0; // index into edges
		while (index < edges.size()) // still more to do
		{
			// 1. get a line and remove it from the graph
			LineString ls = (LineString) edges.get(index);
			Coordinate key = ls.getCoordinateN(0);
			ArrayList nodeList = (ArrayList) nodes.get(key);
			if (nodeList == null) // this was removed in an earlier iteration
			{
				index++;
				continue;
			}
			if (!nodeList.contains(ls)) {
				index++;
				continue; // already processed
			}
			removeFromHash(nodes, ls); // we're removing this from the network

			Coordinate key2 = ls.getCoordinateN(ls.getNumPoints() - 1);
			ArrayList nodeList2 = (ArrayList) nodes.get(key2);

			// case 1 -- this line is independent
			if ((nodeList.size() == 0) && (nodeList2.size() == 0)) {
				result.add(ls);
				index++; // move to next line
				continue;
			}

			if (nodeList.size() > 0) // touches something at the start
			{
				LineString ls2 = getLongest(nodeList); // merge with this one
				ls = merge(ls, ls2);
				removeFromHash(nodes, ls2);
			}
			if (nodeList2.size() > 0) // touches something at the start
			{
				LineString ls2 = getLongest(nodeList2); // merge with this one
				ls = merge(ls, ls2);
				removeFromHash(nodes, ls2);
			}
			// need for further processing
			edges.set(index, ls); // redo this one.
			putInNodeHash(ls, nodes); // put in network
		}
	}

	public void removeFromHash(Hashtable nodes, LineString ls) {
		Coordinate key = ls.getCoordinateN(0);
		ArrayList nodeList = (ArrayList) nodes.get(key);
		if (nodeList != null) {
			nodeList.remove(ls);
		}
		key = ls.getCoordinateN(ls.getNumPoints() - 1);
		nodeList = (ArrayList) nodes.get(key);
		if (nodeList != null) {
			nodeList.remove(ls);
		}
	}

	public LineString getLongest(ArrayList al) {
		if (al.size() == 1)
			return (LineString) (al.get(0));
		double maxLength = -1;
		LineString result = null;
		final int size = al.size();
		LineString l;
		for (int t = 0; t < size; t++) {
			l = (LineString) al.get(t);
			if (l.getLength() > maxLength) {
				result = l;
				maxLength = l.getLength();
			}
		}
		return result;
	}

	public void putInNodeHash(LineString ls, Hashtable nodes) {
		Coordinate key = ls.getCoordinateN(0);
		ArrayList nodeList = (ArrayList) nodes.get(key);
		if (nodeList == null) {
			nodeList = new ArrayList();
			nodeList.add(ls);
			nodes.put(key, nodeList);
		} else
			nodeList.add(ls);
		key = ls.getCoordinateN(ls.getNumPoints() - 1);
		nodeList = (ArrayList) nodes.get(key);
		if (nodeList == null) {
			nodeList = new ArrayList();
			nodeList.add(ls);
			nodes.put(key, nodeList);
		} else
			nodeList.add(ls);
	}

	/**
	 * merges a set of lines together into a (usually) smaller set. This one's
	 * pretty dumb, we use the JTS method (which doesnt merge on degree 3 nodes)
	 * and try to construct less lines.
	 *
	 * There's multiple solutions, but we do this the easy way. Usually you will
	 * not be given more than 3 lines (especially after jts is finished with).
	 *
	 * Find a line, find a lines that it "connects" to and add it. Keep going.
	 *
	 * DONE: be smarter - use length so the algorithm becomes greedy.
	 *
	 * This isnt 100% correct, but usually it does the right thing.
	 *
	 * NOTE: this is O(N^2), but N tends to be <10
	 *
	 * @param lines
	 */
	Collection mergeLines2(Collection lines) {
		LineMerger lm = new LineMerger();
		lm.add(lines);
		Collection merged = lm.getMergedLineStrings(); // merged lines
		// Collection merged = lines;

		if (merged.size() == 0) {
			return null; // shouldnt happen
		}
		if (merged.size() == 1) // simple case - no need to continue merging
		{
			return merged;
		}

		// key to this algorithm is the sorting by line length!

		// basic method:
		// 1. grab the first line in the list of lines to be merged
		// 2. search through the rest of lines (longer ones = first checked) for
		// a line that can be merged
		// 3. if you find one, great, merge it and do 2 things - a) update the
		// search geometry with the merged geometry and b) delete the other
		// geometry
		// if not, keep looking
		// 4. go back to step #1, but use the next longest line
		// 5. keep going until you've completely gone through the list and no
		// merging's taken place

		ArrayList mylines = new ArrayList(merged);

		boolean keep_going = true;
		while (keep_going) {
			keep_going = false; // no news is bad news
			Collections.sort(mylines, lineLengthComparator); // sorted
			final int size = mylines.size(); // long->short
			LineString major, minor, merge;
			for (int t = 0; t < size; t++) // for each line
			{
				major = (LineString) mylines.get(t); // this is the
				// search
				// geometry
				// (step #1)
				if (major != null) {
					for (int i = t + 1; i < mylines.size(); i++) // search
					// forward
					// for a
					// joining
					// thing
					{
						minor = (LineString) mylines.get(i); // forward
						// scan
						if (minor != null) // protection because we remove an
						// already match line!
						{
							merge = merge(major, minor); // step 3
							// (null =
							// not
							// mergeable)
							if (merge != null) {
								// step 3a
								keep_going = true;
								mylines.set(i, null);
								mylines.set(t, merge);
								major = merge;
							}
						}
					}
				}
			}
			// remove any null items in the list (see step 3a)

			mylines = (ArrayList) removeNulls(mylines);

		}

		// return result
		return removeNulls(mylines);

	}

	/**
	 * given a list, return a new list thats the same as the first, but has no
	 * null values in it.
	 *
	 * @param l
	 */
	ArrayList removeNulls(List l) {
		ArrayList al = new ArrayList();
		Iterator it = l.iterator();
		Object o;
		while (it.hasNext()) {
			o = it.next();
			if (o != null) {
				al.add(o);
			}
		}
		return al;
	}

	/**
	 * reverse direction of points in a line
	 */
	LineString reverse(LineString l) {
		List clist = Arrays.asList(l.getCoordinates());
		Collections.reverse(clist);
		return l.getFactory().createLineString(
				(Coordinate[]) clist.toArray(new Coordinate[1]));
	}

	/**
	 * if possible, merge the two lines together (ie. their start/end points are
	 * equal) returns null if not possible
	 *
	 * @param major
	 * @param minor
	 */
	LineString merge(LineString major, LineString minor) {
		Coordinate major_s = major.getCoordinateN(0);
		Coordinate major_e = major.getCoordinateN(major.getNumPoints() - 1);
		Coordinate minor_s = minor.getCoordinateN(0);
		Coordinate minor_e = minor.getCoordinateN(minor.getNumPoints() - 1);

		if (major_s.equals2D(minor_s)) {
			// reverse minor -> major
			return mergeSimple(reverse(minor), major);

		} else if (major_s.equals2D(minor_e)) {
			// minor -> major
			return mergeSimple(minor, major);
		} else if (major_e.equals2D(minor_s)) {
			// major -> minor
			return mergeSimple(major, minor);
		} else if (major_e.equals2D(minor_e)) {
			// major -> reverse(minor)
			return mergeSimple(major, reverse(minor));
		}
		return null; // no merge
	}

	/**
	 * simple linestring merge - l1 points then l2 points
	 */
	private LineString mergeSimple(LineString l1, LineString l2) {
		ArrayList clist = new ArrayList(Arrays.asList(l1.getCoordinates()));
		clist.addAll(Arrays.asList(l2.getCoordinates()));

		return l1.getFactory().createLineString(
				(Coordinate[]) clist.toArray(new Coordinate[1]));
	}

	/**
	 * sorts a list of LineStrings by length (long=1st)
	 *
	 */
	private final class LineLengthComparator implements java.util.Comparator {
		public int compare(Object o1, Object o2) // note order - this sort
		// big->small
		{
			return Double.compare(((LineString) o2).getLength(),
					((LineString) o1).getLength());
		}
	}

	// djb: replaced because old one was from sun's Rectangle class
	private Envelope intersection(Envelope e1, Envelope e2) {
		Envelope r = e1.intersection(e2);
		if (r.getWidth() < 0)
			return null;
		if (r.getHeight() < 0)
			return null;
		return r;
	}

	public void enableLayer(String layerId) {
		needsOrdering=true;
		enabledLayers.add(layerId);
	}

    public boolean isOutlineRenderingEnabled() {
        return outlineRenderingEnabled;
    }

    /**
     * Sets the text rendering mode. 
     * When true, the text is rendered as its GlyphVector outline (as a geometry) instead of using
     * drawGlypVector. Pro: labels and halos are perfectly centered, some people prefer the 
     * extra antialiasing obtained. Cons: possibly slower, some people do not like the 
     * extra antialiasing :) 
     */
    public void setOutlineRenderingEnabled(boolean outlineRenderingEnabled) {
        this.outlineRenderingEnabled = outlineRenderingEnabled;
    }
	
}
