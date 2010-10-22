/*$************************************************************************************************
 **
 ** $Id: PositionImpl.java 34133 2009-10-12 08:14:05Z mbedward $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/geometry/PositionImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.geometry;

// OpenGIS direct dependencies
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.Position;


/**
 * A union type consisting of either a {@linkplain DirectPosition direct position} or of a
 * reference to a {@linkplain Point point} from which a {@linkplain DirectPosition direct
 * position} shall be obtained. The use of this data type allows the identification of a
 * position either directly as a coordinate (variant direct) or indirectly as a reference
 * to a {@linkplain Point point} (variant indirect).
 *  
 * @UML datatype GM_Position
 * @author ISO/DIS 19107
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/jts-wrapper/src/main/java/org/geotools/geometry/jts/spatialschema/geometry/geometry/PositionImpl.java $
 * @version 2.0
 */
public class PositionImpl implements Position {
    
    //*************************************************************************
    //  Fields
    //*************************************************************************
    
    private DirectPosition position;
    
    //*************************************************************************
    //  Constructor
    //*************************************************************************
    
    public PositionImpl(final DirectPosition position) {
        this.position = position;
    }
    
    //*************************************************************************
    //  implement the Position interface
    //*************************************************************************

    
    /**
     * @inheritDoc
     * @see org.opengis.geometry.coordinate.Position#getPosition()
     */
    @Deprecated
    public DirectPosition getPosition() {
        return position;
    }
    
    /**
     * @inheritDoc
     * @see org.opengis.geometry.coordinate.Position#getDirectPosition()
     */
    public DirectPosition getDirectPosition() {
        return position;
    }
}
