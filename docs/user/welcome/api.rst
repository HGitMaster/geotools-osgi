Public API
==========

As an open source library you are free to call any of the GeoTools classes needed for your application to be delivered on time.

However, GeoTools offers a clean approach which should cause the least amount of disruption to your code during library upgrades as GeoTools matures.

GeoTools cleanly separates out several groups of application programming interfaces (API) from the internal implementation of the library.

If you write your code against these interfaces they offer a stable target as the library upgrades. In the event these interfaces are changed (sometimes standards change on us) the interfaces will be deprecated for a single release cycle allowing you a chance to upgrade.

Interfaces for Concepts and Ideas
---------------------------------

Formally these interfaces come from three locations:

* gt-opengis - interfaces backed by ISO and OGC standards (as explained in the Use of Standards page).
* jts topology suite - is a Java Geometry library implementing the Simple Features for SQL (SFSQL) OGC standard.
* gt-api - interfaces provided by GeoTools.

We also have one stalled work in progress:

* gt-opengis has a set of ISO19107 geometry interfaces waiting on an interested party to look into curves and 3D.

These interfaces represent spatial concepts and data structures in your application and are suitable for use in method signatures.

Classes for Implementation
--------------------------

While interfaces are used to represent the data structures employed by GeoTools, we also provide public classes to get the job done.

Public classes are provided for the purpose of:

* Utility classes to make things easier to work with. Examples are the CQL, DataUtilities and JTS classes. Each of these provide public methods to help you make the most of the services provided by GeoTools.
* Helping glue the library together at runtime - an example is the FactoryFinders which allow you to look up available implementations on the CLASSPATH.
* GeoTools "Extensions" provide additional services on top of the library and require additional public classes to make this happen. An example is the ColorBrewer class provied by gt-brewer.

You can make use of public classes in these modules directly, in all cases they are utilities to get the job done. These classes are suitable for use in your import section. There is no need to use these classes in your method signatures as they do not represent data structures.

Factories for Creation
----------------------

Interfaces only define what a data structure should look like, and do not provide a way to create an object. In Java the work around is to provide a "factory" that provides "create" methods which you can use instead of **new**.

GeoTools provides Factory classes allowing you to create the various objects used by the library, such as Features, Styles, Filters, CoordinateReferencingSystems, and DataStores.

GeoTools provides a FactoryFinder system to locate the available factory implementations on the CLASSPATH. By using a FactoryFinder your code can be built to function using only interfaces.

For more information review the page How to Create Something which outlines how to locate an appropriate implementation at runtime.

Separation of Concerns
^^^^^^^^^^^^^^^^^^^^^^

While you could find and use each of the various Factory implementations directly this would introduce a **dependency** between your code and that exact implementation. This idea of depending on a specific implementation makes your code brittle with respect to change, and prevents you from taking advantage of a better implementation when it is made available in the future.

Bad practice with direct dependency on ShapeFileDataStoreFactory::
   
   ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
   ShapeFileDataStore = factory.createDataStore( file );

This code example would have been fine for GeoTools 2.1, however for GeoTools 2.2 an "indexed" shapefile datastore was created with far better performance. The factory would be smart enough to create
an IndexedShapeFileDataStore if an index file was available.

Here is a replacement that allows GeoTools to return an indexed datastore if one is available::
   
    DataStore dataStore = DataStoreFinder.getDataStore( file );

The DataStoreFinder class looks up all the DataStoreFactory implementations available on the CLASSPATH and sorts out which one can
make a DataStore to access the provided file.