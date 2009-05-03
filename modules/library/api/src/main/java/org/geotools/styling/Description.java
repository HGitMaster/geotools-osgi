package org.geotools.styling;

import org.opengis.util.InternationalString;

public interface Description extends org.opengis.style.Description {

    void setTitle( InternationalString title );
    
    void setAbstract( InternationalString title );
    
    /**
     * calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    void accept(StyleVisitor visitor);
}