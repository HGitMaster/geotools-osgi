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
package org.geotools.tile.nasa;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.geotools.tile.TileServiceInfo;
import org.geotools.tile.TileStratagy;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.opengis.util.ProgressListener;

final class WorldWindTileServiceInfo implements TileServiceInfo {    

    final static URI SCHEMA;
    private final URI SOURCE;
    
    private Document dom;
    private WorldWindTileStrategy stratagy;
    
    static {
        try {
            SCHEMA = new URI("LayerSet.xsd");
        } catch (URISyntaxException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }
    
    WorldWindTileServiceInfo( URI source, Document doc) {        
        dom = doc;
        SOURCE = source;
    }
    
    /**
     * @param stratagy
     */
    WorldWindTileServiceInfo( URL url, ProgressListener monitor ) {        
        SAXBuilder builder = new SAXBuilder(false);       
        URLConnection connection;
        try {
            connection = url.openConnection();
            dom = builder.build(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException( e );
        } catch (JDOMException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
        try {
            SOURCE = new URI( url.toExternalForm() );
        } catch (URISyntaxException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
        stratagy = new WorldWindTileStrategy( url );
    }
    
    public String getAbstract() {
        Element root = dom.getRootElement();
        Element layerSet = root.getChild("LayerSet");
        return LayerSetParser.description( layerSet );        
    }
    
    /**
     * Description is used to bundle up all the child layer set descriptions
     * for searching.
     * <p>
     * In a perfect world these woudl show up as "Folders"...
     * </p>
     */
    public String getDescription() {
        Element root = dom.getRootElement();
        
        List children = LayerSetParser.childLayerSets( root );
        if( children.isEmpty() ) return null;
        
        StringBuffer buf = new StringBuffer();
        
        // check for subchildren
        for( Iterator i = children.iterator(); i.hasNext();){
            String description = LayerSetParser.description( (Element) i.next() );
            if( description != null ) {
                buf.append( description );
                buf.append( "\n" );                    
            }
        }
        return buf.toString();    
    }
    
    public Icon getIcon() {
        Element root = dom.getRootElement();
        return LayerSetParser.infoIcon( root );
    }

    public String[] getKeywords() {
        return new String[]{ "tile" };
    }
    
    public URI getPublisher() {
        return null;
    }
    
    public URI getSchema() {
        return SCHEMA;
    }
    
    public URI getSource() {
        return SOURCE;
    }
    
    public String getTitle() {
        Element root = dom.getRootElement();
        
        return root.getAttributeValue("Name");
    }
    
    /**
     * List<URI> of all children identification.
     * <p>
     * This list is used to quickly provide a list of members based on a quick
     * scan of the dom.  Individual TileMapInfo entries will need to be constructed
     * with the childInfo method on an as needed basis.
     * </p>
     * To keep everything unique different identification stratagies are used
     * based on the child type:
     * <ul>
     * <li>TerrainTileService: ServerUrl#DataSetName
     * <li>QuadTileSet/ImageAccessor/ImageTileService: ServerUrl#DataSetName
     * <li>QuadTileSet/ImageAccessor/WMSAccessor: ServerGetMapUrl#WMSLayerName
     * </ul>
     * 
     * @return List<URI> child names
     */
    List childrenIds(){        
        List children = LayerSetParser.childLayers( dom.getRootElement() );
        List ids = new ArrayList( children.size() );
        for( Iterator i=children.iterator(); i.hasNext(); ){
            Element child = (Element) i.next();
            Accessor access = Accessor.create( child );
            if( access != null ){
                ids.add( access.getIdentifier() );
            }
        }        
        return ids;
    }

    QuadTileMapInfo getInfo( URI id ) {
        List children = LayerSetParser.childLayers( dom.getRootElement() );
        for( Iterator i=children.iterator(); i.hasNext(); ){
            Element child = (Element) i.next();
            Accessor access = Accessor.create( child );            
            if( access != null && access.getIdentifier().equals( id )){
                return new QuadTileMapInfo( child, access );
            }
        }        
        return null;
    }

    public TileStratagy getTileStratagy() {
        return stratagy;
    }
    
}
