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
package org.geotools.gui.swing.propertyedit.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdesktop.swingx.JXList;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * CRS list model
 * 
 * @author Johann Sorel
 */
public class CRSListModel extends AbstractListModel{

    private static final String WKT_ID = "WKT"; //$NON-NLS-1$
    private static final String ALIASES_ID = "ALIASES"; //$NON-NLS-1$
    private static final String LAST_ID = "LAST_ID"; //$NON-NLS-1$
    private static final String NAME_ID = "NAME_ID"; //$NON-NLS-1$
    private static final String CUSTOM_ID = "CRS.Custom.Services"; //$NON-NLS-1$
    
    private List<String> datas = new ArrayList<String>();
    private JXList gui_list = null;
    
    public CRSListModel(){
    }
    
    
    /**
     * Takes in a CRS, finds it in the list and highlights it
     * 
     * @param crs
     */
    public void gotoCRS( CoordinateReferenceSystem crs ) {
        if (crs != null) {
            final List list = new ArrayList();
            list.addAll(datas);
            Set<Identifier> identifiers = new HashSet<Identifier>(crs.getIdentifiers());
            identifiers.add(crs.getName());
            
            final Set<Integer> candidates=new HashSet<Integer>();
            
            for( int i = 0; i < list.size(); i++ ) {
                for( Identifier identifier : identifiers ) {
                    final String item = (String) list.get(i);
                    if( sameEPSG( crs, identifier, item) || exactMatch( crs, identifier, item )){
                        int next = datas.indexOf(item);
                        gui_list.getSelectionModel().setSelectionInterval(next, next);
//                        codesList.setSelection(new StructuredSelection(item), false);
//                        list.setTopIndex(i);
                        return;
                    }
                    if (isMatch(crs, identifier, item)) {
                        candidates.add(i);
                    }
                }
            }
            if( !candidates.isEmpty() ){
                Integer next = candidates.iterator().next();
                gui_list.getSelectionModel().setSelectionInterval(next, next);
//                gui_list.getSelectionModel().
//                
//                codesList.setSelection(new StructuredSelection(list.getItem(next)), false);
//                list.setTopIndex(next);
            }

            
        }
    }
    
    /**
     * populates the codes list with a filtered list of CRS names
     */
    public void fillCodesList(String searchword) {
        String[] searchParms = searchword.toUpperCase().split(" "); //$NON-NLS-1$
        Set<String> descriptions = filterCRSNames(searchParms);
        //descriptions = filterCustomCRSs(descriptions, searchParms);
        List<String> list = new ArrayList<String>(descriptions);
        
        datas = list;
        fireContentsChanged(this, 0, datas.size());
        
    }
    
    /**
     * checks if all keywords in filter array are in input
     * 
     * @param input test string
     * @param filter array of keywords
     * @return true, if all keywords in filter are in the input, false otherwise
     */
    protected boolean matchesFilter( String input, String[] filter ) {
        for( String match : filter ) {
            if (!input.contains(match))
                return false;
        }
        return true;
    }
    
    private boolean exactMatch( CoordinateReferenceSystem crs, Identifier identifier, String item ) {
        return (crs==DefaultGeographicCRS.WGS84 && item.contains("EPSG:4326")) || item.equalsIgnoreCase(identifier.toString()); //$NON-NLS-1$
    }

    private boolean sameEPSG( CoordinateReferenceSystem crs, Identifier identifier, String item ) {
        String toString = identifier.toString();
        return toString.contains("EPSG:") && item.contains(toString); //$NON-NLS-1$
    }

    private boolean isMatch( CoordinateReferenceSystem crs, Identifier identifier, String item ) {
        return (crs==DefaultGeographicCRS.WGS84 && item.contains("4326")) || item.contains(identifier.toString()); //$NON-NLS-1$
    }
        
    /**
     * filters all CRS Names from all available CRS authorities
     * 
     * @param filter array of keywords
     * @return Set of CRS Names which contain all the filter keywords
     */
    protected Set<String> filterCRSNames( String[] filter ) {
        Set<String> descriptions = new TreeSet<String>();
        for( Object object : ReferencingFactoryFinder.getCRSAuthorityFactories(null) ) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory) object;
            try {
                Set<String> codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
                for( Object codeObj : codes ) {
                    String code = (String) codeObj;
                    String description;
                    try {
                        description = factory.getDescriptionText(code).toString();
                    } catch (Exception e1) {
                        description = "Unnamed"; 
                    }
                    description += " (" + code + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    if (matchesFilter(description.toUpperCase(), filter)){
                        descriptions.add(description);
                    }
                }
            } catch (FactoryException e) {
            	e.printStackTrace();
            }
        }
        return descriptions;
    }
    
//    public CoordinateReferenceSystem getCRS() {
//        if (folder == null)
//            return selectedCRS;
//        if (folder.getSelectionIndex() == 1) {
//            try {
//                String text = wktText.getText();
//                CoordinateReferenceSystem createdCRS = ReferencingFactoryFinder.getCRSFactory(null)
//                        .createFromWKT(text);
//
//                if (keywordsText.getText().trim().length() > 0) {
//                    Preferences node = findNode(createdCRS.getName().getCode());
//                    if( node!=null ){
//                        Preferences kn = node.node(ALIASES_ID);
//                        String[] keywords = keywordsText.getText().split(","); //$NON-NLS-1$
//                        kn.clear();
//                        for( String string : keywords ) {
//                            string=string.trim().toUpperCase();
//                            if(string.length()>0)
//                                kn.put(string,string);
//                        }
//                        kn.flush();
//                    }else{
//                        CoordinateReferenceSystem found = createCRS(createdCRS.getName().getCode());
//                        if (found != null && CRS.findMathTransform(found, createdCRS, true).isIdentity()) {
//                            saveKeywords(found);
//                            return found;
//                        }
//
//                        Set<Identifier> identifiers = new HashSet<Identifier>(createdCRS.getIdentifiers());
//                        for( Identifier identifier : identifiers ) {
//                            found = createCRS(identifier.toString());
//                            if (found != null && CRS.findMathTransform(found, createdCRS, true).isIdentity()) {
//                                saveKeywords(found);
//                                return found;
//                            }
//                        }
//                        return saveCustomizedCRS(text, true, createdCRS);
//                    }
//                }
//                
//                return createdCRS;
//            } catch (Exception e) {
//                UiPlugin.log("", e); //$NON-NLS-1$
//            }
//        }
//        if (selectedCRS == null) {
//            return createCRS(searchText.getText());
//        }
//        return selectedCRS;
//    }
    
    
    
    
    
    public int getSize() {
        return datas.size();
    }

    public Object getElementAt(int index) {
        return datas.get(index);
    }

    public JXList getList() {
        return gui_list;
    }

    public void setList(JXList gui_list) {
        this.gui_list = gui_list;
    }

}
