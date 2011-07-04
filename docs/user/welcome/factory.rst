Where to get a Factory
----------------------

It really depends on your application, depending on your environment you may locate a factory by either:

* Using a GeoTools "FactoryFinder". Most factory finders are provided by the main module. They will hunt down an implementation on the CLASSPATH for you to use.
* Use of "Container" - you may find an implementation provided as part of your application container (especially for a Java EE application). You can take this approach in normal applications with a container implementation like Spring, or PicoContainer
* Use of "JNDI" - your application may also store an implementation in JNDI (this approach is often used to locate a DataSource in a JEE application)
* Direct use of a known factory. You can always create a new Factory yourself and make use of it to create interfaces.
* Direct use of an implementation. You may decide to duck the factory game completely and make use of a specific implementation using new.

These examples will usually use a factory finder of some sort. For the details please review the How to Find a Factory page.

FactoryFinder
^^^^^^^^^^^^^

While the use of Factories has become common place (especially in development environments like Spring). GeoTools has its own "FactoryFinder" classes, unique to project, which is how the library looks up what plugins are available for use.

These facilities are also available for use in your own application.

FactoryFinder uses the "built-in" Java plug-in system known as Factory Service Provide Interface. This technique allows a jar to indicate what services it makes available (in this case implementations of a factory interface). 

To make this easier to use we have a series of utility classes called "FactoryFinders". These classes work as a match maker - looking around at what is available on the CLASSPATH. They will perform the "search" and locate a the implementation you need.

Here is an example::
   
   FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory( null );

About FactorySPI
""""""""""""""""

The "FactorySPI" system is the out of the box plug in system that ships with Java. That is why we like it - we are sure you already are using the Java software after all. The SPI part is pronounced "spy" and stands for Service, Provider, Interface.

The FactorySPI system has a look on your CLASSPATH and locates implementations of a requested service scattered around all the jars you have. It does this by looking in the jar MANIFEST folder in a services directory.

Factory SPI is a runtime plugin system; so your application can "discover" and use new abilities that GeoTools provides over time. As our shapefile support gets better and better your application will notice and make use of the best implementation for the job.

If you are curious you can make use of the FactorySPI system yourself to locate anything we got going on in GeoTools::
   
   Hints hints = GeoTools.getDefaultHints();
   FactoryRegistry registry = new FactoryCreator(Arrays.asList(new Class[] {FilterFactory.class,}));
   Iterator i = registry.getServiceProviders( FilterFactory.class, null, hints );
   while( i.hasNext() ){
       FilterFactory factory = (FilterFactory) i.next();
   }

Notes:

* keep you FactoryRegistery around, hold it in a static field or global lookup service such as JNDI.
* The registry usually creates one instance (the first time you ask) and will return it to you again next time
* Specifically it will create you one instance per configuration (ie that Hints object), so if you ask again using the same hints you will get the same instance back

Think of FactoryRegistry keeping instances as singletons for you.  In the same manner as it is a Java best practice (when making a singleton) to "partition" by ThreadGroup (so different applets use different singletons). FactoryRegistry does not follow this practice - it uses Hints to "partition" - so two applets that are configured the same will end up using the same FilterFactory.

Application specific Alternatives
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Here are a couple of alternatives for stitching together your application.

Container
"""""""""

A container is a great way to take care of a lot of the boiler plate
code involved in working with factories. Much of this documentation
will use PicoContainer (just because it is small), while many real
world applications use the Spring container.

A container is basically a Map where you can look up instances.
In common use the instances are factories, and what makes a container
valuable is its ability automate the process of "wiring up" the
factories together.

Popular techniques:

* reflection - picocontainer looks the constructors using reflection to see if any of the required parameters are available
* configuration - Spring uses a big xml file marking how each factory is created

The other nice thing is the container can put off creating the
factories until you actually ask for them.::
  
  container.registerImplementationClass( PositionFactory.class, PositionFactoryImpl.class );
  container.registerImplementationClass( CoordinateFactory.class, CoordinateFactoryImpl.class );
  container.registerImplementationClass( PrimitiveFactory.class, PrimitiveFactoryImpl.class );
  container.registerImplementationClass( ComplexFactory.class, ComplexFactoryImpl.class );
  container.registerImplementationClass( AggregateFactory.class AggregateFactoryImpl.class );
  
  container.registerInstance( CoordianteReferenceSystem.class, CRS.decode("EPSG:4326") );
  
  WKTParser parser = (WKTParser) container.newInstance( WKTParser.class );

In the above example the WKTParser needs to be constructed with a PositionFactory, CoordinateFactory, PrimitiveFactory and ComplexFactory. Each one of these factories can only be constructed for a specific CoordinateReferenceSystem.

If we were not using a container to manage our factories it would of taken three times the number of lines of code just to set up a WKTParser.

JNDI
""""

If you are writing a Java EE Application there is a big global map in the sky called "InitialContext". Literally this is a map you can do look up by name and find Java instances in. It is so global in fact that the instances will be shared between applications.

This idea of a global cross application map is great for configuration and common services. If you are working with a Java EE application you will often find such things as:

* a CRSAuthorityFactory registered for any code wanting to use the referencing module
* a database listed under the Name "jdbc/EPSG" used to hold the EPSG tables
* a GeometryFactory, or FeatureTypeFactory and so on ...

Here is the GeoTools code that looks up a DataSource for an EPSG authority::
  
  Context context = JNDI.getInitialContext(null);
  DataSource source = (DataSource) context.lookup("jdbc/EPSG");

The JNDI interfaces are shipped with Java; and two implementations are provided (one to talk to LDAP directories such as organisations deploy for email address information, and another for configuration information stored on the file system with your JRE).

The difference between JNDI and a Container:

* JNDI is not a container - it is an interface that ships with Java that
  lets you ask things of a "directory service".
  
  A Java EE Application Server runs programs in a "container" and part
  of the "container configuration" is making sure that JNDI is set up
  and pointing to the Services (ie global variables) that the
  Application Server makes available to all applications.
  
  This same directory service can be used by you to share global
  variables between applications. Some things like the CRSAuthority
  can be treated as a "utility" and it makes sense to only have one
  of them for use from several applications at once.

Because making use of an application container is a good idea, and too hard to set up. There are a lot of alternative "light weight" containers available. Examples include pico container, JBoss container, Spring container and many many more. These containers focus on the storing of global variables (and making a lot of the difficult configuration automatic - like what factory needs to be created first).

Direct use of Factory
^^^^^^^^^^^^^^^^^^^^^

Sometimes you just need to go ahead and code it like you mean it. The GeoTools plugin system does have its place and purpose; but if you know exactly what you are doing; or want to test an exact situation you can dodge the plugin system and do the work by hand.

You can just use a specific factory that is known to you::
  
  DataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();
  
  File file = new File("example.shp");
  Map map = Collections.singletonMap( "url", file.toURL() );

  DataStore dataStore = factory.createDataStore( map );

You are depending on a specific class here (so it is not a real plug-in based solution in which GeoTools can find you the best implementation for the job). There is a good chance however that the factory will set you up with a pretty good implementation.

* Factory classes are Public in Name Only
  
  Factory classes are only public because we have to (so the factory
  finders can call them) - some programming environments such as OSGi
  will take special care to prevent you making direct use of these
  classes.
  
  If you are working on the uDig project you may find that class loader
  settings have prevented you from directly referring to one of these
  factory classes.

You can provide a "hint" asking the Factory Finder to retrieve you a specific instance::
  
  Hints hints = new Hints( Hints.FILTER_FACTORY, "org.geotools.filter.StrictFactory" );
  FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory( hints );

You can skip the whole Factory madness and just do normal Java coding::
  
  File file = new File("example.shp");
  URI namespace = new URI("refractions");
  boolean useMemoryMapped = true;
  ShapefileDataStore shapefile = new ShapefileDataStore( example.toURL(), namespace, useMemoryMapped );

You are depending on a exact class here, violating the plug-in system and so on. Chances are that GeoTools should not let you do this (by making the constructor package visible and forcing you to use the associated DataStoreFactory instead).

This option is fine for quick hacks, you may find that the ShapefileDataStore has additional methods (to handle such things as forcing the "prj" file to be rewritten.::
  
  shapefile.forceSchemaCRS( CRS.decode( "EPSG:4326" ) );
