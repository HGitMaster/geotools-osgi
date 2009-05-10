/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.introduction;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.measure.unit.SI;

import org.geotools.data.FeatureSource;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.demo.mappane.MapViewer;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.repository.GeoResource;
import org.geotools.repository.Service;
import org.geotools.repository.defaults.DefaultServiceFinder;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.Conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * DemoBase is the primary class of the three Demo* classes which combine into a 
 * demonstration application that introduces each of the major modules of the 
 * Geotools library. DemoBase creates an instance of each class and provides the
 * methods which will control almost all of the action in the demonstration.
 * 
 *     WARNING: This is a work in progress and is incomplete.
 * 
 * 
 * The Demo* classes are organized into a [data model / view / controller] split
 * common to modern applications.
 * 
 * Control starts in the 'main' method of this DemoBase class and immediately 
 * passses to the DemoGUI instance which is created. In the GUI, interactive 
 * control by the user is limited to the 'Quit' button and one other button. 
 * Each button, when pressed, returns the control to the 'button*' methods in 
 * the demoBase instance of this class. These 'button*' methods then call the 
 * remaining methods in the class. Readers can therefore follow the action of 
 * the demonstration application simply by reading in turn each of the methods 
 * below which follow the 'button*' methods.
 * 
 * The View is managed by demoGUI, an instance of the DemoGUI class, which 
 * creates the window of the 
 * demonstration application. Later on, the instance also creates the JMapPane 
 * in which the map will be rendered. The instance therefore holds on to the 
 * Renderer and the MapContext instances which  determine the contents which 
 * will actually be rendered and the details of the rendering such as the 
 * styling and projection information.
 * 
 * The data model is managed by demoData, an instance of the DemoData class, 
 * which holds all the Feature data (the geospatial data maintained by the 
 * application). 
 * The demoData instance holds several List objects which show how an 
 * application can manage data access itself. 
 * The instance also holds a single Catalog object which shows how an 
 * application can use the Geotools library Catalog system to manage data 
 * 
 * 
 * 
 * This tutorial shows the following elements: 
 * 
 *     (1) FeatureSource<SimpleFeatureType, SimpleFeature> creation:
 *           This creates, through several approaches, the handles which are
 *           used later for the manipulation of data.
 *           
 *         1.1 - a feature source from scratch
 *         1.2 - a feature source from a shapefile
 *         1.3 - a feature source from a WMS/WFS (an image)
 *         
 *     (.) Catalog creation:
 *           This creates a resource through which to handle all the features 
 *           used by a complex application.
 *     
 *     (.) Coordinate Transform creation:
 *           This creates a coordinate operation and uses that to transform the 
 *           data in a feature source to a different Coordinate Referencing 
 *           System.
 *     
 *     (.) Query creation:
 *           This creates a Filter/Expression to query a feature for its 
 *           contents and thereby to subset a feature.
 *     
 *     ...
 *     
 *     (5) Style creation:
 *           This creates the graphical elements which are used to display 
 *           the data.
 *     
 *     (6) Display:
 *           This creates a GUI MapViewerto display the data.
 *           
 *     (7) Image output:
 *           This renders an image to an image buffer and then dumps the image
 *           buffer to a file.
 *           
 *     ...
 *     
 * 
 * 
 * HISTORY: 
 *   This class regroups work from many different tutorials.
 *     Section 1.1 - "Feature from scratch" was inspired by earlier tutorials.
 *     Section 1.2 - "Feature from shapefile" was in Ian's MapViewer class.
 *    
 *     Section 5   - The style demo came from an email by Tom Howe on user-list.
 *     Section 6   - The GUI was inspired by Ian Turton's MapViewer demo.
 *     Section 7   - MakeImage with email advice from David Adler, Aaron B. Parks.
 * 
 * @author  Adrian Custer, gnuGIS
 * @author Justin Deoliveira, The Open Planning Project
 * 
 * @version 0.03
 * @since   2.3-M0
 *
 */
public class DemoBase {
    
    //TODO: Add the logger
//    private Logger textlog = 
//            org.geotools.util.logging.Logging.getLogger("org.geotools.demo.introduction.DemoBase");
    
    /* The name of the test shapefile. */
    final String SHAPEFILENAME = "/countries.shp";
    /* The name of the test sld for the test shapefile. */
    final String SHAPEFILESLDNAME = "/countries.sld";
    /*The name of the URL for the test Web Feasture Service. */
    final String WFSSERVERURL = "http://www.refractions.net:8080/geoserver/wfs?REQUEST=GetCapabilities&";
    /* The connection information for the test PostGIS database server. */
    final String POSTGISSERVERURL = "www.refractions.net";
    final String POSTGISUSERNAME = "postgres";
    final String POSTGISDATABASE = "geotools";
    
    /* The filename for the output image */
    final String imageFileEnd = "image.png";
    
    /* Cartographic variables */
    final ReferencedEnvelope envNoEdges = 
                             new ReferencedEnvelope(-179.0,179.0,-80.0,80.0, 
                                                    DefaultGeographicCRS.WGS84);
    // TODO: move to demoGUI
    CoordinateReferenceSystem projCRS = null;

    /* The URI of the test shapefile. */
    URI SHAPEFILEURI;

    /* DemoGUI class */
    DemoGUI demoGUI;
    
    /* demo class */
    DemoData demoData;
    
    /* The constructor */
    public DemoBase(){
        
        demoData = new DemoData();
        try {
            //path to our shapefile 
            String shpPath = getClass().getResource(SHAPEFILENAME).toString();
            //convert spaces to %20 
            shpPath = shpPath.replaceAll(" ", "%20");
            SHAPEFILEURI =  new URI(shpPath);
        } catch (URISyntaxException uriex) {
            System.err.println("Unable to create shapefile uri: "+ uriex.getMessage());
        }
    }
    
    /* These callback methods are called by DemoGUI when the respective buttons 
     * are pressed. Each one calls other methods below. */ 
    
    public void buttonCreateFeatures(){

        demoGUI.textArea.append("Start: Create Features.\n");
        
        /* Create a Point Feature representing London as a FeatureCollection.*/
        SimpleFeature london = createLondonPointFeatureFromScratch();
        FeatureCollection<SimpleFeatureType, SimpleFeature> londonCollection = makeLondonFeatureCollection(london);
        loadLondonFeatureCollectionIntoList(londonCollection);
        demoGUI.textArea.append(" Done: Created London from scratch.\n");
        
        /* TODO: Load a shapefile with the given name into the List of DataStores.*/
        /* NB: then close it so we can load it into the catalog instead. */
        
        /* Load a shapefile with the given name into the local catalog. */
        loadShapefileIntoCatalog(SHAPEFILENAME);
        demoGUI.textArea.append(" Done: Loaded the Shapefile into the catalog.\n");
        
        /* Load a reference to a web source of features into the local catalog.*/
//        loadWebFeatureServiceIntoCatalog(WFSSERVERURL);
//        demoGUI.textArea.append(" Done: Loaded a Web Feature Service into the catalog.\n");
        
        /* Load a reference to a database source of features into the local catalog.*/
//        loadDatabaseIntoCatalog(POSTGISSERVERURL);
//        demoGUI.textArea.append(" Done: Loaded a database into the catalog.\n");
        
        /* TODO: Load a reference to an external catalog. */

        demoGUI.textArea.append("  End: Created Features.\n");
    }
    
    public void buttonCreateStyles(){
        demoGUI.textArea.append("Start: Create the styles.\n");
        
        Style lonstyl = createLondonStyleFromScratch();
        demoData.theStyleMap.put("londstyl",lonstyl);
        demoGUI.textArea.append(" Done: Created and loaded the London point Style.\n");
        
        Style shpstyl = createShapefileStyleFromSLDFile(SHAPEFILESLDNAME);
        demoData.theStyleMap.put("shpstyl",shpstyl);
        demoGUI.textArea.append(" Done: Created and loaded the Shape Style.\n");
        
        
        demoGUI.textArea.append("  End: Created the styles.\n");
    }
    
    
    
    public void buttonCreateMap(){
        demoGUI.textArea.append("Start: Create a map.\n");
        
        demoGUI.initialize_JMapPane();
        demoGUI.textArea.append(" Done: Initialized the MapPane.\n");
        
        /* Add the London FeatureCollection<SimpleFeatureType, SimpleFeature> as one layer in the map. */
        FeatureCollection<SimpleFeatureType, SimpleFeature> lfc = 
                   (FeatureCollection<SimpleFeatureType, SimpleFeature>) demoData.theFeatureCollectionList.get(0);
        Style lsty = (Style) demoData.theStyleMap.get("londstyl");
        MapLayer m0 = new DefaultMapLayer(lfc,lsty);
        demoGUI.context.addLayer(m0);
        
        /* Add the Shapefile FeatureSource<SimpleFeatureType, SimpleFeature> below the first layer. */
        FeatureSource<SimpleFeatureType, SimpleFeature> shpFS = getAShapefileFeatureSourceFromCatalog();
        Style shpsty = (Style) demoData.theStyleMap.get("shpstyl");
        MapLayer m1 = new DefaultMapLayer(shpFS,shpsty);
        demoGUI.context.addLayer(0,m1);
        
        
//      demoGUI.context.addLayer(webFS,webStyl);
//      demoGUI.context.addLayer(dbFS,dbStyl);
        
        /* Configure JMapPane */
        //demoGUI.jmp.setHighlightLayer(demoGUI.context.getLayer(0));
//        jmp.setSize(200,600);
        //TODO: Set boundary to all that's visible, disabled for projection
//        jmp.setMapArea(context.getLayerBounds());
        demoGUI.jmp.setMapArea(envNoEdges);

        /* Paint */
        demoGUI.frame.repaint();
        demoGUI.frame.doLayout();

        demoGUI.textArea.append(" Done: Loaded the map.\n");
//        load_JMapPane();
        
        demoGUI.textArea.append("  End: Created a map.\n");
        
//        create_the_map();
    }
    
    public void buttonProjectMap(){
      create_ProjectedCRS_from_DefaultGeogCRS();
      display_projected_as_Mercator();
        
    }
    
    public void buttonCaptureImage(){
        capture_as_image();
        
    }
    
    public void xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx(){
        
    }
    
    public SimpleFeature createLondonPointFeatureFromScratch(){

        // Wikipedia gives London as:  51?? 30.4167??? N 0?? 7.65??? W 
       // NOTE: in Gt 2.2 axis order is Long/Lat throughout; in 2.3 the CRS rules
       Coordinate ptc = new Coordinate(0.1275d,51.507d);
       GeometryFactory geomFac = new GeometryFactory();
       Point ptG = geomFac.createPoint(ptc);
       
       /* Name Attribute */
       String name = "London";
       
       /* Population Attribute */
       Integer pop = new Integer(7500000);
   
       /* AttributeTypes, starting with Geometry using pre-made CRS */
       AttributeTypeBuilder atb = new AttributeTypeBuilder();
       atb.setName("the_geom");
       atb.setBinding(ptG.getClass());
       atb.setNillable(true);
       atb.setLength(1);
       atb.setDefaultValue(null);
       atb.setCRS(org.geotools.referencing.crs.DefaultGeographicCRS.WGS84);
       
       GeometryDescriptor ptGA = 
               (GeometryDescriptor) atb.buildDescriptor("the_geom");
       
       atb.setName("CITYNAME");
       atb.setBinding(String.class);
       atb.setLength(48);
              
       AttributeDescriptor cityAT = atb.buildDescriptor("CITYNAME"); 
       
       atb.setName("CITYPOP");
       atb.setBinding(Integer.class);
       
       AttributeDescriptor popAT = atb.buildDescriptor("CITYPOP");        
       
       /* FeatureType */
       AttributeDescriptor[] ptATs = new AttributeDescriptor[3];
       ptATs[0] = ptGA;
       ptATs[1] = cityAT;
       ptATs[2] = popAT;

       SimpleFeatureType ptFT = null;
       try{
          ptFT = FeatureTypes.newFeatureType(ptATs, "Metropolis");
       } 
       catch (SchemaException schex){
           String msg = "SchemaException on FeatureType creation: "+ schex;
           new IOException( msg ).initCause( schex );
       }
       
       
       /* Feature */
       Object [] ptElems = { ptG, name, pop };
       
       SimpleFeature ptF = null;
       try {
           ptF = SimpleFeatureBuilder.build(ptFT, ptElems, null);
       } 
       catch (IllegalAttributeException iaex){
           System.err.println("IllegalAttributeException on Feature creation: " + iaex);
//           String msg = "IllegalAttributeException on Feature creation: " + iaex;
//           throw (IOException) new IOException( msg ).initCause( iaex );
       }
        
        return ptF;
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> makeLondonFeatureCollection(SimpleFeature f){
        
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();
        fc.add(f);
        return fc;
    }
    
    public void loadLondonFeatureCollectionIntoList(FeatureCollection<SimpleFeatureType, SimpleFeature> fc){
        demoData.theFeatureCollectionList.add(fc);
    }
    
    /**
     * Loads a shapefile service into the catalog.
     * 
     * @throws IOException Any I/O errors loading into the catalog.
     */
    public void loadShapefileIntoCatalog(String shpname){
        
        //create shapefile datastore parameters
        URL shapefileURL = getClass().getResource( shpname );
        Map params = new HashMap();
        params.put( ShapefileDataStoreFactory.URLP.key, shapefileURL );
        //load the services, there should be only one service
        DefaultServiceFinder finder = new DefaultServiceFinder(demoData.localCatalog);
        List services = finder.aquire(SHAPEFILEURI, params);
        
        //add the service to the catalog
        demoData.localCatalog.add( (Service) services.get( 0 ) );
    }

    /**
     * Loads a Web Feature Service into the catalog.
     * 
     * @param wfsurl a string URL for the Web Feature Service location.
     */
    public void loadWebFeatureServiceIntoCatalog(String wfsurl){
    
        //create wfs datastore parameters
        URL wfsURL =null;
        try {
            wfsURL = new URL( wfsurl );
        } catch (MalformedURLException murlex){
            System.err.println("MalformedURLException on creation of the WFS url: "+ murlex.getMessage() );
        }
        Map params = new HashMap();
        params.put( WFSDataStoreFactory.URL.key, wfsURL );
        
        //load the service, there should be only one
        DefaultServiceFinder finder = new DefaultServiceFinder( demoData.localCatalog );
        List services = finder.aquire( params );
        System.out.println("size is: " + services.size());
        
        //add the service to the catalog
        Service s = (Service) services.get( 0 );
        demoData.localCatalog.add( s );
    }
    
    /**
     * Loads a postgis database into the catalog.
     * 
     * @throws IOException Any I/O errors loading into the catalog.
     */
    public void loadDatabaseIntoCatalog(String dburl) {
        
        //set up connection parameters
        URL pgURL = null;
        try {
            pgURL = new URL( POSTGISSERVERURL );
        } catch (MalformedURLException murlex){
            System.err.println("MalformedURLException on creating the PostGIS URL: "+ murlex);
        }
        Map params = new HashMap();
        params.put( PostgisDataStoreFactory.HOST.key, pgURL );
        params.put( PostgisDataStoreFactory.USER.key, POSTGISUSERNAME );
        params.put( PostgisDataStoreFactory.DATABASE.key, POSTGISDATABASE );
        
        //load the service, there should be only one
        DefaultServiceFinder finder = new DefaultServiceFinder( demoData.localCatalog );
        List services = finder.aquire( params );
        
        //add the service to the catalog
        demoData.localCatalog.add( (Service) services.get( 0 ) );
        
    }
    
    /**
     * Creates a Style for the London point feature.
     * 
     * @return a Style appropriate for the point feature.
     */
    public Style createLondonStyleFromScratch(){
        
        /* Point style from scratch */
        StyleBuilder builder = new StyleBuilder();
        Mark mark    = builder.createMark("circle", Color.RED);
        Graphic g    = builder.createGraphic(null,mark,null);
        Symbolizer s = builder.createPointSymbolizer(g);
        
        Style memStyle = builder.createStyle( s );
        return memStyle;
        
    }
    
    // TODO: This should be done through the catalog *not* directly from the file.
    public Style createShapefileStyleFromSLDFile(String shpSLDfile){
    
        // Make the sldURL from the sldName 
        URL sldURL = MapViewer.class.getResource( shpSLDfile );
        
        // Create the shapefile Style, uses StyleFactory and an SLD URL
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        SLDParser stylereader = null;
        try {
            stylereader = new SLDParser(sf,sldURL);
        } 
        catch (IOException ioex){
            System.out.println("IOException on SLDfile read: " + ioex);
        }
        Style[] shpStylArr = stylereader.readXML();
        Style shpStyle = shpStylArr[0];
        
        return shpStyle;
        
    }
    
    /**
     * Gets a FeatureCollection<SimpleFeatureType, SimpleFeature> with the shapefile from the catalog.
     * <p>
     * This method <b>must</b> be called after {@link #loadShapefileIntoCatalog(String)}.
     * </p>
     * @return The shapefile feature source.
     * 
     * @throws IOException Any I/O errors that occur accessing the shapefile resource.
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getAShapefileFeatureSourceFromCatalog(){
//    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollectionForShapefile() throws IOException {
        
        //lookup service, should be only one
        List serviceList = demoData.localCatalog.find( SHAPEFILEURI, null );
        Service service = (Service) serviceList.get( 0 );
        
        //shapefiles only contain a single resource
        List resourceList = null;
        try{
            resourceList = service.members( null );
        } catch (IOException ioex){
            System.err.println("An IOException occurred on service resolve: " + ioex.getMessage() );
        }
        GeoResource resource = (GeoResource) resourceList.get( 0 );
        
        FeatureSource<SimpleFeatureType, SimpleFeature> shpFS = null;
        try{
            shpFS = (FeatureSource<SimpleFeatureType, SimpleFeature>) resource.resolve( FeatureSource.class, null );
        } catch (IOException ioex){
            System.err.println("IOException on resoloving shape resource to FeatureSource: " + ioex.getMessage() );
        }
        return shpFS;
    }

    public void xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx(){
        
    }
    
//    
//    /**
//     * Gets a FeatureCollection<SimpleFeatureType, SimpleFeature> with the shapefile from the catalog.
//     * <p>
//     * This method <b>must</b> be called after {@link #loadShapefileIntoCatalog()}.
//     * </p>
//     * @return The shapefile feature source.
//     * 
//     * @throws IOException Any I/O errors that occur accessing the shapefile resource.
//     */
//    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollectionForShapefile(){
////    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollectionForShapefile() throws IOException {
//        
//        //create the uri to lookup
//        URI uri = null;
//        try {
//            uri =  new URI( getClass().getResource( SHAPEFILENAME ).toString() );
//        } 
//        catch ( URISyntaxException uriex ) {
//            System.err.println( "Unable to create shapefile uri"+ uriex.getMessage() );
////            throw (IOException) new IOException( "Unable to create shapefile uri").initCause( uriex );
//        }
//        
//        //lookup service, should be only one
//        List serviceList = demoData.localCatalog.find( uri, null );
//        Service service = (Service) serviceList.get( 0 );
//        
//        //shapefiles only contain a single resource
//        List resourceList = null;
//        try{
//            resourceList = service.members( null );
//        } catch (IOException ioex){
//            System.err.println("An IOException occurred on service resolve: " + ioex.getMessage() );
//        }
//        GeoResource resource = (GeoResource) resourceList.get( 0 );
//
////        return (FeatureSource<SimpleFeatureType, SimpleFeature>) resource.resolve( FeatureSource.class, null );
//        FeatureCollection<SimpleFeatureType, SimpleFeature> shpFC = null;
//        try {
//            shpFC = (FeatureCollection<SimpleFeatureType, SimpleFeature>) resource.resolve( FeatureCollection.class, null );
//        } catch (IOException ioex){
//            System.err.println("An IOException occurred on resolving the resource: " + ioex.getMessage() );
//        }
//        return shpFC;
//    }

    
    /**
     * Loads all the wfs feature sources from the wfs service.
     * <p>
     * This method <b>must</b> be called 
     * </p>
     * @return a java List of FeatureSources. 
     * @throws IOException
     */
    public List getListOfFeatureSourcesForWebFeatureService() throws IOException {
        
        //create the uri to lookup
        URI uri = null;
        try {
            uri =  new URI( WFSSERVERURL );
        } 
        catch ( URISyntaxException e ) {
            throw (IOException) new IOException( "Unable to create wfs uri").initCause( e );
        }
        
        //lookup service, should only be one
        List services = demoData.localCatalog.find( uri, null );
        Service service = (Service) services.get( 0 );
        
        //wfs contains many resources
        List resources = service.members( null );
        List featureSources = new ArrayList();
        
        for ( Iterator r = resources.iterator(); r.hasNext(); ) {
            GeoResource resource = (GeoResource) r.next();
            if ( resource.canResolve( FeatureSource.class ) ) {
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = 
                    (FeatureSource<SimpleFeatureType, SimpleFeature>) resource.resolve( FeatureSource.class, null );
                featureSources.add( featureSource );
            }
        }
        
//         Iterator r = featureSources.iterator();
//         ((FeatureSource<SimpleFeatureType, SimpleFeature>) r.next()).
        return featureSources;
    }

    /*
     * Create a Mercator ProjectedCRS from DefaultGeogCRS.
     */
    public void create_ProjectedCRS_from_DefaultGeogCRS(){
        
        demoGUI.textArea.append("Start: Create ProjectedCRS from DefaultGeographicCRS.\n");
        
        /* Properties of the Projected CRS */
        Map props = new HashMap();
        props.put(IdentifiedObject.NAME_KEY, "My arbitrary name"); // Mandatory
//        props.put(ReferenceSystem.VALID_AREA_KEY,e); // Optional
        
        
        /* Geographic CoordinateReferenceSystem */
        //TODO: this is hard coded below because the compiler doesn't work.
        CoordinateReferenceSystem geogCRS = 
            org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
        
        
        /* Defining Conversion: Name, Parameters */
        final String dcName = "A Mercator";
        /* Parameters for the Mercator */
        DefaultMathTransformFactory mtf = new DefaultMathTransformFactory();
        ParameterValueGroup pvg = null;
        try {
            pvg = mtf.getDefaultParameters("Mercator_1SP");
        } catch (NoSuchIdentifierException nsiex){
            System.err.println("On DefaultPrameterGroup creation: "+ nsiex.getMessage());
        }
        //Start Test Output
//            ParameterDescriptorGroup dg = pvg.getDescriptor()
//            for (GeneralParameterDescriptor descriptor : dg.descriptors()) {
//                System.out.println(descriptor.getName().getCode());
//            }
        //End Test Output
        DefiningConversion dc = new DefiningConversion(dcName,pvg);
        //TODO: Added to make the compiler happy, could merge with above.
        Conversion c = (Conversion) dc;
        
        
        
        /* Coordinate System */
        Map map = new HashMap();
        CSFactory csFactory = ReferencingFactoryFinder.getCSFactory(null);
        CoordinateSystemAxis xAxis = null;
        CoordinateSystemAxis yAxis = null;
        CartesianCS worldCS = null;
        try {
            map.clear();
            map.put("name", "Cartesian X axis");
            xAxis = csFactory.createCoordinateSystemAxis(map, "X", AxisDirection.EAST, SI.METER);
            map.clear();
            map.put("name", "Cartesian Y axis");
            yAxis = csFactory.createCoordinateSystemAxis(map, "Y", AxisDirection.NORTH, SI.METER);
            map.clear();
            map.put("name", "Cartesian CS");
            worldCS = csFactory.createCartesianCS(map, xAxis, yAxis);
        } catch (FactoryException fex) {
            System.err.println("On cartesianCS creation: " + fex.getMessage());
        }
        
        /* Projected CRS */
        ReferencingFactoryContainer fg = ReferencingFactoryContainer.instance(null);
        try{
           projCRS = fg.createProjectedCRS(props,
                        org.geotools.referencing.crs.DefaultGeographicCRS.WGS84,
                                            c,
                                            worldCS);
//           //TODO: figure out why this breaks but above works.
//           projCRS = fg.createProjectedCRS(props,
//                   geogCRS,
//                   dc,
//                   worldCS);
        } catch (FactoryException fex) {
            System.err.println("On projectedCRS creation: " + fex.getMessage());
        }
//        System.out.println(projCRS.toWKT())

        demoGUI.textArea.append("  End: Created ProjectedCRS from DefaultGeographicCRS.\n");
    }
    
    
    
    
//    /*
//     * Create a Mercator ProjectedCRS from Well-Known Text.
//     */
//    public void projectedCRSfromWKT(){
//        
//        demoGUI.textArea.append("");
//        CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
//        String wkt = "PROJCS[\"Mercator Attempt\", "
//                       + "GEOGCS[\"WGS84\", "
//                           + "DATUM[\"WGS84\", "
//                           + "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "
//                           + "PRIMEM[\"Greenwich\", 0.0], "
//                           + "UNIT[\"degree\",0.017453292519943295], "
//                           + "AXIS[\"Longitude\",EAST], "
//                           + "AXIS[\"Latitude\",NORTH]], "
//                       + "PROJECTION[\"Mercator_1SP\"], "
//                       + "PARAMETER[\"semi_major\", 6378137.0], "
//                       + "PARAMETER[\"semi_minor\", 6356752.314245179], "
//                       + "PARAMETER[\"central_meridian\", 0.0], "
//                       + "PARAMETER[\"scale_factor\", 1.0], "
//                       + "PARAMETER[\"false_easting\", 0.0], "
//                       + "PARAMETER[\"false_northing\", 0.0], "
//                       + "UNIT[\"metre\",1.0], "
//                       + "AXIS[\"x\",EAST], "
//                       + "AXIS[\"y\",NORTH]]";
//        CoordinateReferenceSystem prjCRS=null;
//        try{
//            prjCRS = crsFactory.createFromWKT(wkt);
//        } catch (FactoryException fe){
//            System.err.println("On prjCRS creation a FactoryException :"+fe.getMessage());
//        }
//        Envelope e = new Envelope(-170.0,170.0,-80.0,80.0);
//        context.setAreaOfInterest(e, prjCRS);
//        
//        demoGUI.textArea.append("");
//    }
 
    
    
    
    /*
     * A Mercator Projected Map.
     * 
     * Reproject features on the screen.
     * 
     * 
     */
    public void display_projected_as_Mercator(){
        try {
            demoGUI.textArea.append("Start: Project the map.\n");
            
            ReferencedEnvelope llEnvelope = new ReferencedEnvelope(envNoEdges, DefaultGeographicCRS.WGS84);
            ReferencedEnvelope projEnvelope = llEnvelope.transform(projCRS, true);
            demoGUI.context.setAreaOfInterest(projEnvelope);
            
            demoGUI.jmp.setContext(demoGUI.context);
    
            demoGUI.jmp.setMapArea(projEnvelope);
    
            demoGUI.frame.repaint();
            demoGUI.frame.doLayout();
            
            demoGUI.jmp.setMapArea(demoGUI.jmp.getContext().getAreaOfInterest());
            demoGUI.jmp.reset();
    
            demoGUI.textArea.append("  End: Projected the map.\n");
        } catch(Exception te) {
            demoGUI.textArea.append("Error occurred during projection");
        }
    }
        
    
    
    
    
    
    
    /*
     * Make graphical image files, one from scratch and the other from the 
     * jmappane contents.
     *   TODO: add to catalog---great for pre/post transform comparisons
     *   TODO: clean this up, isolate resolution and size
     *   
     */
    public void capture_as_image(){

        demoGUI.textArea.append("Start: Capture an image.\n");
        /*
         * 1. Create an image from scratch
         */
        //Size of the final image, will be too big for the input
        int w = 1800;
        int h = 800;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        
        //TODO: HACK HACK HACK  need a real pixel to world transform
        AffineTransform trsf = new AffineTransform(new double[]{1.0,1.0,1.0,1.0});

        //      DefaultMathTransformFactory dmtf = new DefaultMathTransformFactory();
//      try{
//              trsf = dmtf.createAffineTransform(new Matrix2(1,1,1,1));
//      } catch (Exception e){
//              ;
//      }
//              transform =
//                      renderer.worldToScreenTransform(
//                                                              g,
//                                                              new Rectangle(0, 0, w, h),
//                                                              worldbounds);
                
        demoGUI.renderer.paint(g, new Rectangle(0, 0, w, h), trsf);
        try{
            ImageIO.write(image, "png", new File("workspace/gtdemo-new-"+imageFileEnd));
        } catch (IOException ioex) {
            System.err.println("IO Exception on image file write: "+ ioex);
        }
        g.dispose();
        
        /*
         * 2. Create an image from the jmappane contents
         */
        //spit the image out to a file
        int ww = demoGUI.jmp.getWidth()+40;
        int hh = demoGUI.jmp.getHeight()+40;
        BufferedImage imageOut = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imageOut.createGraphics();
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, ww, hh);
        demoGUI.jmp.paint(g2);
        try{
            ImageIO.write(imageOut, "png", new File("workspace/gtdemo-jmp-"+imageFileEnd));
        } catch (IOException ioex) {
            System.err.println("IO Exception on image file write: "+ ioex);
        }
        g2.dispose();

        demoGUI.textArea.append("  End: Captured an image.\n");
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        System.out.println("DemoApp Tutorial: Start...");

        DemoBase db = new DemoBase();
//        db.demoData = new DemoData();
        /* The 'this' reference is so the callbacks below can be called. */
        db.demoGUI  = new DemoGUI(db); 
        
        
        System.out.println("DemoApp Tutorial: End of non-GUI thread.");

    }

}
