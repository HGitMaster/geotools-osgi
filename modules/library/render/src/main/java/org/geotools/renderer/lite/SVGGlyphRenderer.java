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

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.geotools.renderer.style.SVGGraphicFactory;
import org.w3c.dom.Document;

/**
 * Turns SVG vector drawings into buffered images geotools can use for rendering
 * 
 * @author James
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/SVGGlyphRenderer.java $
 * @deprecated, use {@link SVGGraphicFactory} instead
 */
public class SVGGlyphRenderer implements GlyphRenderer {
    private static Hashtable cache = new Hashtable(10);

    private static final java.util.List formats = java.util.Collections
            .unmodifiableList(java.util.Arrays.asList(new String[] { "image/svg" }));

    /** The logger for the rendering module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.rendering");

    // static {
    // do register our xml reader wrapper against batik so that we can use
    // jaxp instead of the hard-coded xerces implementation
    // XMLResourceDescriptor.setXMLParserClassName(BatikXMLReader.class.getName());
    // }

    /**
     * mini cache to stop re-loading SVG files. Dont know how effective this is,
     * but...
     */
    private Document getDocument(URL url) throws Exception {
        if (cache.contains(url))
            return (Document) cache.get(url);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

        Document doc = f.createDocument(url.toString());
        cache.put(url, doc);
        return doc;
    }

    /** Creates a new instance of SVGGlyphRenderer */
    public SVGGlyphRenderer() {
    }

    public boolean canRender(String format) {
        return (format.toLowerCase().equals("image/svg+xml"));
    }

    public java.util.List getFormats() {
        return formats;
    }

    public java.awt.image.BufferedImage render(org.geotools.styling.Graphic graphic,
            org.geotools.styling.ExternalGraphic eg, Object feature, int height) {
        try {
            BufferedImage img;
            URL svgfile = eg.getLocation();
            InternalTranscoder magic = new InternalTranscoder();

            if (height > 0)
                magic.addTranscodingHint(InternalTranscoder.KEY_HEIGHT, new Float(height));

            Document inputDoc = getDocument(svgfile);
            // TranscoderInput in = new TranscoderInput(svgfile .openStream());
            magic.transcode(inputDoc);
            img = magic.getImage();
            return img;
        } catch (java.io.IOException mue) {
            LOGGER.log(Level.WARNING, "Unable to load external svg file", mue);
            return null;
        } catch (Exception te) {
            LOGGER.log(Level.WARNING, "Unable to load external svg file", te);
            return null;
        }
    }

}
