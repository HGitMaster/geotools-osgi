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
package org.geotools.tile.cache;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.tile.TileDraw;
import org.geotools.tile.cache.SimpleTileCache.DirectTileRange;

public class SimpleTileCacheTest extends TestCase {
    SimpleTileCache cache;
    TestDraw draw;
    protected void setUp() throws Exception {
        super.setUp();
        cache = new SimpleTileCache();
        draw = new TestDraw();
    }
    protected void tearDown() throws Exception {
        cache.close();
        super.tearDown();
    }
    
    public void testDraw(){
        SimpleTileCache.DirectTileRange tiles = (DirectTileRange)
            cache.createRange( draw, new Rectangle(1,1,3,3));
//        assertEquals( 4, draw.placeholder );
        assertEquals( 0, draw.tile );
        
        assertNotNull( tiles );
        assertFalse( tiles.isLoaded() );
        
        Set set = tiles.getTiles();
        assertNotNull( set );
//        assertEquals( 4, set.size() );
        
        tiles.load( null );
        assertTrue( tiles.isLoaded() );
//        assertEquals( 4, draw.placeholder );
//        assertEquals( 4, draw.tile );
        
        tiles.refresh( null );
        assertTrue( tiles.isLoaded() );
//        assertEquals( 4, draw.placeholder );
//        assertEquals( 8, draw.tile );        
    }
    
    static GridCoverageFactory factory = new GridCoverageFactory();
    
    /**
     * Sample TestDraw working in WSG84 providing 8 tiles.
     * 
     * @author jgarnett
     */
    class TestDraw extends TileDraw {
        int placeholder=0;
        int tile=0;
        
        
        public GridCoverage2D drawPlaceholder( int row, int col ) {
            placeholder++;
            
            Envelope2D rectangle = createRectangle( row, col );
            RenderedImage image=createImage();
            return factory.create( "Grid"+row+"x"+col, image, rectangle );            
        }
        
        RenderedImage createImage( int row, int col ){
            BufferedImage image = new BufferedImage( 90, 90, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setBackground( col <= 2 ? Color.BLUE : Color.RED );
            
            int x = col % 2 == 0 ? 0 : 90;
            int startAngle = row == 1 ? ( x )
                                         : ( col % 2 == 0 ? 180 : 270 );
            int y = row == 1 ? 90 : 0;
            g.fillArc(x,y,180,180,startAngle,90);

            g.setColor( Color.BLACK );
            String message = row+","+col;
            g.drawString( message, 30, 45 );
            return image;
        }
        RenderedImage createImage(){
            BufferedImage image = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
            return image;
        }
        Envelope2D createRectangle( int row, int col ){
            double x = (row * 90.0) - 90.0;
            double y = (row * 90.0) - 180.0;
            double w = 90.0;
            double h = 90.0;
            
            return new Envelope2D( DefaultGeographicCRS.WGS84,x, y, w, h );
        }

        public GridCoverage2D drawTile( int row, int col ) {
            tile++;
            
            Envelope2D rectangle = createRectangle( row, col );
            RenderedImage image=createImage( row, col );
            return factory.create( "Grid"+row+"x"+col, image, rectangle ); 
        }
        public String name( int row, int col ) {
            return "test("+row+"x"+col+")";
        }        
    }
}
