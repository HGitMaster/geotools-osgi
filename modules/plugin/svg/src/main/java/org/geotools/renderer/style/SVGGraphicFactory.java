/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.renderer.style;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.Icon;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.util.SoftValueHashMap;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * External graphic factory accepting an Expression that can be evaluated to a URL pointing to a SVG
 * file. The <code>format</code> must be <code>image/svg+xml</code>, thought for backwards
 * compatibility <code>image/svg-xml</code> and <code>image/svg</code> are accepted as well.
 * 
 * @author Andrea Aime - TOPP
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/branches/2.6.x/modules/plugin/svg/src/main/java/org/geotools
 *         /renderer/style/SVGGraphicFactory.java $
 */
public class SVGGraphicFactory implements ExternalGraphicFactory {

    /** Parsed SVG glyphs cache */
    Map<URL, RenderableSVG> glyphCache = new SoftValueHashMap<URL, RenderableSVG>();

    /** The possible mime types for SVG */
    static final Set<String> formats = new HashSet<String>() {
        {
            add("image/svg");
            add("image/svg-xml");
            add("image/svg+xml");
        }

    };

    public Icon getIcon(Feature feature, Expression url, String format, int size) throws Exception {
        // check we do support the declared format
        if (format == null || !formats.contains(format.toLowerCase()))
            return null;

        // grab the url
        URL svgfile = url.evaluate(feature, URL.class);
        if (svgfile == null)
            throw new IllegalArgumentException(
                    "The specified expression could not be turned into an URL");

        // turn the svg into a document and cache results
        RenderableSVG svg = null;
        if (glyphCache.containsKey(svgfile)) {
            svg = glyphCache.get(svgfile);
        } else {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document doc = f.createDocument(url.toString());
            svg = new RenderableSVG(doc);
            synchronized (glyphCache) {
                glyphCache.put(svgfile, svg);
            }
        }

        return new SVGIcon(svg, size);
    }

    private static class SVGIcon implements Icon {

        private int width;

        private int height;

        private RenderableSVG svg;

        public SVGIcon(RenderableSVG svg, int size) {
            this.svg = svg;

            // defines target width and height for render, based on the SVG bounds
            // and the specified desired height (if height is not provided, then
            // SVG bounds are used)
            Rectangle2D bounds = svg.bounds;
            double targetWidth = bounds.getWidth();
            double targetHeight = bounds.getHeight();
            if (size > 0) {
                double shapeAspectRatio = (bounds.getHeight() > 0 && bounds.getWidth() > 0) ? bounds
                        .getWidth()
                        / bounds.getHeight()
                        : 1.0;
                targetWidth = shapeAspectRatio * size;
                targetHeight = size;
            }
            this.width = (int) Math.round(targetWidth);
            this.height = (int) Math.round(targetHeight);
        }

        public int getIconHeight() {
            return height;
        }

        public int getIconWidth() {
            return width;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            svg.paint((Graphics2D) g, width, height, x, y);
        }
    }

    private static class RenderableSVG {
        Rectangle2D bounds;

        private GraphicsNode node;

        public RenderableSVG(Document doc) {
            this.node = getGraphicNode(doc);
            this.bounds = getSvgDocBounds(doc);
            if (bounds == null)
                bounds = node.getBounds();
        }

        /**
         * Retrieves an SVG document's specified bounds.
         * 
         * @param svgLocation
         *            an URL that specifies the SVG.
         * @return a {@link Rectangle2D} with the corresponding bounds. If the SVG document does not
         *         specify any bounds, then null is returned.
         * @throws IOException
         * 
         */
        private Rectangle2D getSvgDocBounds(Document doc) {
            NodeList list = doc.getElementsByTagName("svg");
            Node svgNode = list.item(0);

            NamedNodeMap attrbiutes = svgNode.getAttributes();
            Node widthNode = attrbiutes.getNamedItem("width");
            Node heightNode = attrbiutes.getNamedItem("height");

            if (widthNode != null && heightNode != null) {
                double width = Double.parseDouble(widthNode.getNodeValue());
                double height = Double.parseDouble(heightNode.getNodeValue());
                return new Rectangle2D.Double(0.0, 0.0, width, height);
            }

            return null;
        }

        /**
         * Retrieves a Batik {@link GraphicsNode} for a given SVG.
         * 
         * @param svgLocation
         *            an URL that specifies the SVG.
         * @return the corresponding GraphicsNode.
         * @throws IOException
         * @throws URISyntaxException
         */
        private GraphicsNode getGraphicNode(Document doc) {
            // instantiates objects needed for building the node
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamic(true);

            // creates node builder and builds node
            GVTBuilder builder = new GVTBuilder();
            return builder.build(ctx, doc);
        }

        public void paint(Graphics2D g, int width, int height, int x, int y) {
            // saves the old transform;
            // (needs synchronizing since we will mess around with the node's transform)
            AffineTransform oldTransform = g.getTransform();
            try {
                if (oldTransform == null)
                    oldTransform = new AffineTransform();
     
                AffineTransform transform = new AffineTransform(oldTransform);
     
                // adds scaling to the transform so that we respect the declared size
                transform.scale(width / bounds.getWidth(), height / bounds.getHeight());
                g.setTransform(transform);
                node.paint(g);
            } finally {
                g.setTransform(oldTransform);
            }

        }
    }

}
