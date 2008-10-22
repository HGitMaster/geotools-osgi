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

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.geotools.util.SoftValueHashMap;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;
import org.w3c.dom.Document;

/**
 * External graphic factory accepting an Expression that can be evaluated to a
 * URL pointing to a SVG file. The <code>format</code> must be
 * <code>image/svg+xml</code>, thought for backwards compatibility
 * <code>image/svg-xml</code> and <code>image/svg</code> are accepted as
 * well.
 * 
 * @author Andrea Aime - TOPP
 */
public class SVGGraphicFactory implements ExternalGraphicFactory {

    /** Parsed SVG glyphs cache */
    Map<URL, Document> glyphCache = new SoftValueHashMap<URL, Document>();

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

        // decode the SVG, and use a cache to avoid repeated retrival and decode
        // of the svg
        InternalTranscoder svgTranscoder = new InternalTranscoder();
        TranscoderInput in;
        if (glyphCache.containsKey(svgfile)) {
            in = new TranscoderInput((Document) glyphCache.get(svgfile));
        } else {
            in = new TranscoderInput(svgfile.openStream());
        }
        // use the size suggested, when available
        if (size > 0) {
            svgTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, new Float(size));
        } 
        TranscoderOutput out = new TranscoderOutput();
        svgTranscoder.transcode(in, out);
        glyphCache.put(svgfile, svgTranscoder.getDocument());

        // for the moment, return the thing as an IconImage, thought we should
        // find a better way to do this, maybe using some hints. Returning the image
        // is probably a lot faster for raster rendering, but for PDF/SVG and printing a
        // vector rendering will be more accurate
        return new ImageIcon(svgTranscoder.getImage());
    }

}
