package org.geotools.styling;

import org.opengis.util.InternationalString;

public interface Description extends org.opengis.style.Description {

    /**
     * Human readable title.
     * <p>
     * @return the human readable title.
     */
    InternationalString getTitle();
    
    void setTitle( InternationalString title );
    
    /**
     * Define title using the current locale.
     * @param title
     */
    void setTitle( String title );
    
    
    /**
     * Human readable description.
     * @return a human readable description
     */
    InternationalString getAbstract();
    
    void setAbstract( InternationalString title );
    
    /**
     * Define description in the current locale.
     * 
     * @param title
     */
    void setAbstract( String title );
    
    /**
     * calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    void accept(StyleVisitor visitor);
}