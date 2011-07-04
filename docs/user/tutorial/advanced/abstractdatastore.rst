:Author: Jody Garnett
:Thanks: geotools-devel list
:Version: |release|
:License: Creative Commons with attribution

AbstractDataStore Tutorial
==========================

.. sectionauthor:: Jody Garnett <jody.garnett@gmail.org>

.. sidebar:: gt-property plugin
   
   This tutorial takes you through the steps of creating **ProeprtyDataStore** originally this
   format was only used by this tutorial to show how the DataStore API worked.
   
   Over time the property file format has become widely used due to its simplicity; as a result 
   **PropertyDataStore** is now considered a supported module in it's own right.
 
The GeoTools project strives to support as many geographical data formats as possible because
getting data into the GeoTools API allows access to a vast suite of tools. In order to transform
a data format into the GeoTools2 feature representation one must write an implementation of
the **DataStore** interface.

Once a DataStore implementation is written, any information written in that format becomes available
not only for GeoTools users, but also for projects built on top of GeoTools such as GeoServer
and uDig.

Writing a new DataStore for GeoTools is one of the best ways to get involved in the project, as
writing it will make clear many of the core concepts of the API. Finally, the modular nature of
GeoTools allows new DataStores to quickly become part of the next release, so that new formats
are can be distributed to to all GeoTools users.

References:

* :doc:`/library/data/property`
* `org.geotools.data.property <http://svn.osgeo.org/geotools/trunk/modules/plugin/property/src/main/java/org/geotools/data/property/>`_ (source code)

.. note::
   
   AbstractDataStore is the original GeoTools 2.0 class; since that time we have learned
   a number of tricks and have a much easier starting point for you in the form of
   **ContentDataStore**.
   
   While **ContentDataStore** is a lot less work to use; it is not yet as fully featured
   as AbstractDataStore. You may wish to try both tutorials before deciding on a course
   of action.

.. note::
 
   Help Review
   
   This article is being updated from GeoTools 2.0 - where it was in docbook.
    
   As is usual for open source documentation is held hostage pending a volunteer to QA, or money.
   Open source stops with the code, documentation sounds like work so please help with feedback!

Part 1 - Introducing PropertyDataStore
--------------------------------------

In this tutorial we will build a property file based DataStore, and in the process explore several
aspects of DataStores and their implementation.

We will be working with content in the following format::
  
  _=id:Integer,geom:Geometry,name:String
  rd1=1|wkt|road one
  rd2=2|wkt|road two
  
These examples use the file :download:`example.properties <artifacts/example.properties>`.
  
.. literalinclude:: artifacts/example.properties
  
If you want to follow along with this tutorial, start a new Java project in your favourite IDE,
and ensure that GeoTools is on your CLASSPATH.

The DataStore we will be writing (called "PropertyDataStore") takes a directory full of .properties
files and allows reading and writing to them:

* Each of the .properties files represents a "data set" - called a FeatureType by GeoTools
* Each of these "data sets" contains a set of Features.
  
  You can think of each of these .properties files as a table in a database
  or a shapefile (with its corresponding .dbf attributes file).

Each of the .properties is very much like a PSV (Pipe Separated Variety) database file. The first
line defines the names (and types) of the columns, and the rest of the lines contain the data;
each element ("column") separated by a '|' ("pipe") character.

Consider this file ("roads.property")::
  
    _=id:Integer,geom:Geometry,name:String
    rd1=1|LINESTRING(0 0,10 10)|road one
    rd2=2|LINESTRING(20 20,30 30)|road two

For the moment, ignore everything to the left of an "=". The first line indicates that there are
3 columns. The first one is called "id" (of type Integer), the next one called "geom" (of type
Geometry), and one called "name" (of type String).

The first row has id "1", geom "LINESTRING(0 0,10 10)", and name "road one".

Now, lets consider the information to the left of the "=" sign. The first line begins with
"_=". This indicates this is a special line - it defines the column names and types. The rest of
the lines start with a unique identifier ("rd1", and "rd2") - these will be the FIDs (Feature IDs)
for each row (ie. a single Feature). The FID is completely different from the id attribute - every
.properties file will have a FIDs, most will not have an "id" column/attribute.

So, the last data row (a Feature) has FID "rd2", id "2", geom "LINESTRING(20 20,30 30)", and
name "road two".

**Definitions**

As you walk through this tutorial, please remember the following:

FID
  Uniquely defines a Feature (row in the .properties file).

FeatureType
  Same as the name of the .properties file (ie. "roads" for roads.properties)

DataStore
  Access all the FeatureTypes (.properties files) in a directory

Schema
  Names of the columns and their types

Part 2 - Creating PropertyDataStore
-----------------------------------

Okay with the background out of the way we can get to work.

PropertyDataStore
^^^^^^^^^^^^^^^^^

The first step is to create a basic DataStore that only supports feature extraction. We will read data from a properties file into the GeoTools2 feature model.

To implement a DataStore we will subclass AbstractDataStore and implement three abstract methods:

* DataStore.getTypeNames()
* DataStore.getSchema( typeName )
* DataStore.getFeatureReader( typeName )

1. To begin create the file PropertyDataStore as follows:

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :end-before: // definition end
    
   Our constructor is going to hold on to two fields:
   
   * file: the file we are reading
   * namespaceURI: namespace (used to tell different property datastore's apart)
   
   .. note::
      
      As we bring in each snippet of code you will need to import the mentioned
      classes. In the Eclipse IDE **Control-Shift-o** will organise imports
      and as a side effect import anything you are missing.

2. PropertyDataStore.getTypeNames()
   
   A DataStore may provide access to several different types of information. The method
   getTypeNames provides a list of the available types.

   For our purposes this list will be the name of the property files in a directory.

   Add the following implementation for getTypeNames():
    
   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getTypeNames start
      :end-before: // getTypeNames end
        
3. PropertyDataStore.getSchema( typeName )
   
   Schema information is provided by the FeatureType class. This method provides access to a
   FeatureType referenced by a type name.
   
   To implement this method we will need to do two things, read a line from a properties file,
   and interpret the line as a FeatureType.

   The DataUtilities class provides an assortment of helper functions. In this method we will
   use DataUtilities.createType( name, spec ).

   .. note::
   
      DataUtilities is a class especially designed for this tutorial.
      
      Those experienced with GeoTools may find these humble beginnings amusing
      given how widely used DataUtilities is today.

   Add getSchema( typeName ):

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getSchema start
      :end-before: // getSchema end

   Add property( typeName, key ):

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // property start
      :end-before: // property end

        
4. PropertyDataStore.getFeatureReader( typeName )
   
   FeatureReader is the low-level API provided by DataStore for accessing Feature content.
   
   The method AbstractDataStore.getFeatureReader( typeName ) is required by the superclass
   AbstractDataStore and is not part of the public DataStore API accessed by user. We will cover
   how this method is used at the end of this tutorial where we discuss optimisation.

   Add PropertyDataStore.getFeatureReader( typeName ):

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getFeatureReader start
      :end-before: // getFeatureReader end
   
   .. note::
      
      Next up we will be implementing PropertyFeatureReader mentioned above.
      
      If you are in Eclipse you can:
      
      1. Hold down **Control-Shift-1** to bring up a number of "Quickfixes"
      2. Select **Create class 'PropertyFeatureReader'** from the list
      3. The Create Class wizard is brought up with all the correct blanks filled in
      
        
PropertyFeatureReader
^^^^^^^^^^^^^^^^^^^^^

FeatureReader is similar to the Java Iterator construct, with the addition of
FeatureType (and IOExceptions).

FeatureReader interface:

* FeatureReader.getFeatureType()
* FeatureReader.next()
* FeatureReader.hasNext()
* FeatureReader.close()

To implement our FeatureReader, we will need to do several things: open a File and read through it
line by line, parsing Features as we go.

1. PropertyFeatureReader
   
   
   Create the file PropertyFeatureReader as follows:

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyFeatureReader.java
      :language: java
   
   The helper class PropertyAttributeReader will be used to accomplish the bulk of this work.
   
   .. note::
      
      Note the use of the GeoTools Logging system. GeoTools provides a wrapper around
      the usual suspects (Java Logging, Log4J, etc...) allowing users to configure
      the library to work with the logging system employed by their application.
      
      We are just that cool :-)

PropertyAttributeReader
^^^^^^^^^^^^^^^^^^^^^^^

The AttributeReader interface is used to provide access to individual attributes from a
storage medium. It is hoped that high level operations (such as Joining) could make
use of this capability.

.. note:: 
   
   If it makes sense for your data format you could just do all the work in your FeaureReader.
   
   Why would you break things up into AttributeReaders? If you had several files you were merging
   together (such as is the case for Shapefile which has shp, dbf, and shx files).
   

AttributeReader interface:

* AttributeReader.getAttributeCount
* AttributeReader.hasNext( index )
* AttributeReader.next()
* AttributeReader.read(int)
* AttributeReader.getAttributeType( index )
* AttributeReader.close()

Because this class actually does some work, we are going to include a few more comments
in the code to keep our heads on straight.

1. Create the file PropertyAttributeReader as follows:

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyAttributeReader.java
      :language: java
      :end-before: // class definition end

   Our constructor acquires the type information from the header, using a function from DataUtilities
   to interpret the type specification. The filename is used as the name for the resulting
   FeatureType, and the directory name is used for the namespace.
   
   The **BufferedReader**, reader, is opened and it will be this class that allows us to stream over
   contents as a series of Features.
   
   .. note::
      
      We are opening this in the constructor in order raise an IOException if the
      file cannot be used (rather than wait until next() is called).
   
   We will use a two part strategy for determining if more content is available. We will try and
   acquire the 'next' line in the hasNext() method, using the next() method to update 'line' to
   the contents of 'next'. All attribute operations will be performed against the current 'line'.

2. With these ideas in mind we can implement the required methods:
    
   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyAttributeReader.java
      :language: java
      :start-after: // implementation start
      :end-before: // implementation end

        
2. Finally, since our file format does support FeatureID we will need a way to let
   our FeatureReader know:

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyAttributeReader.java
      :language: java
      :start-after: // getFeatureID start
      :end-before: // getFeatureID end

   We can make use of getFeatureID() to supply a FeatureID for FeatureReader.
   
   .. note::
      
      Many other DataStores derive a FeatureID from their attributes, or the current
      line number.
      
      Since a FeatureID must start with a letter a common approach is to prepend
      the TypeName followed by a dot to the line number, or database row ID number.

      FeatureID generation example::

		public String deriveFeatureID(){
		    return type.getTypeName()+"."+id_number;
		}

        
DataStoreFactory
^^^^^^^^^^^^^^^^

To make your DataStore truly independent and plugable, you must create a class implementing the
**DataStoreFactorySpi** interface.

This allows the Service Provider Interface mechanism to dynamically plug in your new DataStore with
no implementation knowledge. Code that uses the DataStoreFinder can just add the new DataStore to
the classpath and it will work!

The DataStoreFactorySpi provides information on the Parameters required for construction.
DataStoreFactoryFinder provides the ability to create DataStores representing existing
information and the ability to create new physical storage.

1. PropertyDataStoreFactory
   
   * The "no argument" consturctor is required as it will be used by the
     Factory Service Provider (SPI) plug-in system.
   * getImplementationHints() is used to report on any "Hints" used for configuration
     by our factory. As an example our Factory could allow people to specify a specific
     FeatureFactory to use when creating a feature for each line.
     
   Create PropertyDataStoreFactory as follows:

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :end-before: // definition end

2. We have a couple of methods to describe the DataStore.

   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :start-after: // metadata start
      :end-before: // metadata end

3. The user is expected to supply a Map of connection parameters when creating
   a datastore.
   
   The allowable connection parameters are described using *Parameter* (as defined by gt-api docs).
   This captures the "key" used to store the value in the map, and the expected java
   type for the value.
   
   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :start-after: // getParametersInfo start
      :end-before: // getParametersInfo end
      
4. We have some code to check if a set of provided connection parameters
   can actually be used.
   
   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :start-after: // canProcess start
      :end-before: // canProcess end
   
   .. note::
      
      The directoryLookup has gotten considerably more complicated since this tutorial
      was first written. One of the benifits of PropertyDataStore being used
      in real world situtations.
   
5. Armed with a map of connection parameters we can now:
   
   * create a Datastore for an **existing** property file; and
   * create a datastore for a **new** property file
     
     Since initially our DataStore is read-only we will just throw an UnsupportedOperationException
     at this time.

   Here is the code that finally calls our PropertyDataStore constructor:
   
   .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :start-after: // createDataStore start
      :end-before: // createDataStore end
   
6. The Factory Service Provider (SPI) system operates by looking at the META-INF/services
   folder and checking for implemetnations of DataStoreFactorySpi

   To "register" our PropertyDataStoreFactory please create the following file:

   *  META-INF/services/org.geotools.data.DataStoreFactorySpi

   This file requires the filename of the factory that implements the DataStoreSpi interface.

   Fill in the following content for your **org.geotools.data.DataStoreFactorySpi** file::
    
       org.geotools.data.tutorial.PropertiesDataStoreFactory
   
That is it, in the next section we will try out your new DataStore.

Part 3 - Using Property DataStore to Read Files
-----------------------------------------------

In this part we examine the abilities of the PropertyDataStore implemented in Part 2.

DataStore
^^^^^^^^^

Now that we have implemented a simple DataStore we can explore some of the capabilites made available to us.

PropertyDataStore API for data access:

* DataStore.getTypeNames()
* DataStore.getSchema( typeName )
* DataStore.getFeatureReader( featureType, filter, transaction )
* DataStore.getFeatureSource( typeName )

If you would like to follow along with these examples you can
:download:`PropertyExamples.java </../src/main/java/org/geotools/data/property/PropertyExamples.java>`.

* DataStore.getTypeNames()
  
  The method getTypeNames provides a list of the available types.
  
  getTypeNames() example:

  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example1 start
     :end-before: // example1 end

  Produces the following output (given a directory with example.properties):

  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example1 start
     :end-before: example1 end

* DataStore.getSchema( typeName )
  
  The method getSchema( typeName ) provides access to a FeatureType referenced by a type name.

  getSchema( typeName ) example:

  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example2 start
     :end-before: // example2 end

  Produces the following output:

  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example2 start
     :end-before: example2 end

* DataStore.getFeatureReader( query, transaction )
  
  The method getFeatureReader( query, transaction ) allows access to the contents
  of our DataStore.
  
  The method signature may be more complicated than expected, we certainly did not talk
  about Query or Transactions when we implemented our PropertyDataStore. This is something
  that AbstractDataStore is handling for you and will be discussed later in the section
  on optimisation.

  * Query.getTypeName()
  
    Determines which FeatureType is being requested. In addition, Query supports the
    customization attributes, namespace, and typeName requested from the DataStore.
    While you may use DataStore.getSchema( typeName ) to retrieve the types as specified by
    the DataStore, you may also create your own FeatureType to limit the attributes returned
    or cast the result into a specific namespace.
  
  * Query.getFilter()
    
    Used to define constraints on which features should be fetched. The constraints
    can be on spatial and non-spatial attributes of the features.

  * Query.getPropertiesNames()
  
    Allows you to limit the number of properties of the returned Features to only those
    you are interested in.

  * Query.getMaxFeatures()
    
    Defines an upper limit for the number of features returned.
  
  * Query.getHandle()
    
    User-supplied name used to describe a query in user's terms in any generated error messages.
  
  * Query.getCoordinateSystem()
    
    Used to force the use of a user-supplied CoordinateSystem (rather than the one supplied
    by the DataStore). This capability will allow client code to use our DataStore with a
    CoordinateSystem of their choice. The coordinates returned by the DataStore will not be
    modified in any way.
  
  * Query.getCoordinateSystemReproject()
    
    Used to reproject the Geometries provided by a DataStore from their original value (or
    the one provided by Query.getCoordinateSystem) into a different coordinate system.
    The coordinate returned by the DataStore will be processed , either natively by
    Advanced DataStores, or using GeoTools reprojection routines.

  .. note::
     
     Since this tutorial was writen Query has expanding its capabilities
     (and the capabilities of your DataStore) to include support for reprojection.
     
     It also offers an "open ended" pathway for expansion using "query hints".
     
  * Transaction
    
    Allows access the contents of a DataStore during modification.

  With all of that in mind we can now proceed to our
  DataStore.getFeatureReader( featureType, filter, transaction ) example:
    
  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example3 start
     :end-before: // example3 end

  Produces the following output:
  
  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example3 start
     :end-before: example3 end

	
  Example with a quick "selection" Filter:
    
  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example4 start
     :end-before: // example4 end

  Produces the following output:
  
  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example4 start
     :end-before: example4 end

* DataStore.getFeatureSource( typeName )
  
  This method is the gateway to our high level as provided by an instance of FeatureSource,
  FeatureStore or FeatureLocking. The returned instance represents the contents of a single
  named FeatureType provided by the DataStore. The type of the returned instance indicates
  the capabilities available.
  
  This far in our tutorial PropertyDataStore will only support an instance of FeatureSource.

  Example getFeatureSource:
    
  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example5 start
     :end-before: // example5 end
  
  Producing the following output:

  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example5 start
     :end-before: example5 end


FeatureSource
^^^^^^^^^^^^^

FeatureSource provides the ability to query a DataStore and represents the contents of a single
FeatureType. In our example, the PropertiesDataStore represents a directory full of properties
files. FeatureSource will represent a single one of those files.

FeatureSource defines:

* FeatureSource.getFeatures( query ) - request features specified by query
* FeatureSource.getFeatures( filter ) - request features based on constraints
* FeatureSource.getFeatures() - request all features
* FeatureSource.getSchema() - acquire FeatureType
* FeatureSource.getBounds - return the bounding box of all features
* FeatureSource.getBounds( query ) - request bounding box of specified features
* FeatureSource.getCount( query ) - request number of features specified by query

FeatureSource also defines an event notification system and provides access to the DataStore
which created it. You may have more than one FeatureSource operating against a file at any time.

FeatureCollection
^^^^^^^^^^^^^^^^^

.. sidebar:: FeatureResults
   
   FeatureResults is the original name of FeatureCollection.
   Some of these methods have been replaced such as the use of
   DataUtilities.collection( featureCollection ) to load
   the contents into memory.
   
   It is interesting to note the design goal of capturing a
   prepared statement (rather than loading the features into memory).
   
   The class was renamed FeatureCollection to help those migrating
   from GeoTools 1.0.
   
While the FeatureSource API does allow you to represent a named FeatureType, it still does not
allow direct access to a FeatureReader. The getFeatures methods actually return an instance of
FeatureCollection.

FeatureCollection defines:

* FeatureCollection.getSchmea()
* FeatureCollection.features() - access to a FeatureIterator
* FeatureCollection.accepts( visitor, progress )
* FeatureCollection.getBounds() - bounding box of features
* FeatureCollection.getCount() - number of features
* DataUtilities.collection( featureCollection ) - used to load features into memory

FeatureCollection is the closest thing we have to a prepared request. Many DataStores are able to
provide optimised implementations that handles the above methods natively.

* FeatureCollection Example:
  
  .. literalinclude:: /../src/main/java/org/geotools/data/property/PropertyExamples.java
     :language: java
     :start-after: // example6 start
     :end-before: // example6 end
  
  With the following output:

  .. literalinclude:: artifacts/output
     :language: text
     :start-after: example6 start
     :end-before: example6 end

.. note::
   
   In the above example, FeatureSource.count(Query.ALL) will return -1, indicating that the value
   is expensive for the DataStore to calculate, or at least that our PropertyDataStore
   implementation does not provide an optimised implementation.
   
   FeatureCollection.size() will always produce an answer
   
   You can think of this as:
   
   * FeatureSource is a way to perform a quick check for a precanned answer for count and bounds.
     Some formats such as shapefile will keep this information in the header at the top of the
     file.
   * FeatureCollection checks the contents, and possibly checks each item, for an answer to
     size and bounds.
     
Care should be taken when using the collection() method to capture the contents of a DataStore in
memory. GIS applications often produce large volumes of information and can place a strain
on memory use.

Part 4 - Making Property DataStore Writable
-------------------------------------------

In this part we will complete the PropertyDataStore started above. At the end of this section we
will have a full functional PropertyDataStore supporting both read and write operations.

The DataStore API has two methods that are involved in making content writable.

* DataStore.getFeatureWriter( typeName )
* DataStore.createSchema( featureType )

AbstractDataStore asks us to implement two things in our subclass:

* PropertyDataStore() constructor must call super( true ) to indicate we are
  working with writable content
* PropertyDataStore.getFeatureWriter( typeName )

FeatureWriter defines the following methods:

* FeatureWriter.getFeatureType
* FeatureWriter.hasNext
* FeatureWriter.next
* FeatureWriter.write
* FeatureWriter.remove
* FeatureWriter.close

Change notification for users is made available through several FeatureSource methods:

* FeatureSource.addFeatureListener( featureListener )
* FeatureSource.removeFeatureListener( featureListener )

To trigger the featureListeners we will make use of several helper methods in AbstractDataSource:

* AbstractDataStore.fireAdded( feature )
* AbstractDataStore.fireRemoved( feature )
* AbstractDataStore.fireChanged( before, after )

PropertyDataStoreFactory
^^^^^^^^^^^^^^^^^^^^^^^^

Now that we are going to be writing files we can fill in the createNewDataStore method.

1. Open up PropertyDataStoreFactory and replace createNewDataStore with the following:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStoreFactory.java
      :language: java
      :start-after: // createNewDataStore start
      :end-before: // createNewDataStore end
   
   No surprises here; the code simply creates a directory for PropertyDataStore to work in.
  
PropertyDataStore
^^^^^^^^^^^^^^^^^

1. To start with, we need to make a change to our PropertyDataStore constructor:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // constructor start
      :end-before: // constructor end

   This change will tell AbstractDataStore that our subclass is willing to modify Features.

2. Implement createSchema( featureType)
   
   This method provides the ability to create a new FeatureType. For our DataStore we
   will use this to create new properties files.
   
   To implement this method we will once again make use of the DataUtilities class.
   
   Add createSchema( featureType ):
   
   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // createSchema start
      :end-before: // createSchema end

3. Implement getFeatureWriter( typeName )
   
   FeatureWriter is the low-level API storing Feature content. This method is not part of the
   public DataStore API and is only used by AbstractDataStore.
   
   Add getFeatureWriter( typeName ):
    
   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getFeatureWriter start
      :end-before: // getFeatureWriter end
  
  FeatureWriter is less intuitive than FeatureReader in that it does not follow the example of
  Iterator as closely.

PropertyFeatureWriter
^^^^^^^^^^^^^^^^^^^^^

Our implementation of a FeatureWriter needs to do two things: support the FeatureWriter interface
and inform the DataStore of modifications.

1. Create the file PropertyFeatureWriter.java:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: */
      :end-before: // constructor end

   Our constructor creates a PropertyAttributeReader to access the existing contents of
   the DataStore. We made use of PropertyAttributeReader to implement
   PropertyFeatureReader in Section 1.

  We also create a PropertyAttributeWriter operating against a temporary file. When the
  FeatureWriter is closed we will delete the original file and replace it with our new file.

2. Add FeatureWriter.getFeatureType() implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // getFeatureType start
      :end-before: // getFeatureType end

3. Add hasNext() implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // next start
      :end-before: // next end
    
   Our FeatureWriter makes use of two Features:
   
   * original: the feature provided by PropertyAttributeReader
   * live: a duplicate of original provided to the user for modification
   
   When the FeatureWriter is used to write or remove information, the contents of both live
   and feature are set to null. If this has not been done already we will write out the
   current feature.

4. Add the helper function writeImplementation( Feature ):

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // writeImplementation start
      :end-before: // writeImplementation end

5. Add next() implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // next start
      :end-before: // next end

   The next method is used for two purposes:
   
   * To access Features for modification or removal (when working through existing content)
   * To create new Features (when working past the end of the file)
   
   To access existing Features, the AttributeReader is advanced, the current attribute and feature ID assembled into a Feature. This Feature is then duplicated and returned to the user. We will later compare the original to the user's copy to check if any modifications have been made.

6. Add write() implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // write start
      :end-before: // write end

   In the write method we will need to check to see whether the user has changed anything. If so,
   we will need to remember to issue event notification after writing out their changes.

7. Add remove() implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // remove start
      :end-before: // remove end

  To implement remove, we simply won't write out the original Feature.
  Most of the method is devoted to gathering up the information needed to issue
  a feature removed event.

8. Add close() Implementation:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureWriter.java
      :language: java
      :start-after: // close start
      :end-before: // close end

   To implement close() we must remember to write out any remaining features in the DataStore
   before closing our new file. To implement this we have performed a small optimization: we
   simply echo the line acquired by the PropertyFeatureReader.
   
   The last thing our FeatureWriter must do is replace the existing File with our new one.

PropertyAttributeWriter
^^^^^^^^^^^^^^^^^^^^^^^

In the previous section we explored the capabilities of our PropertyWriter through actual use;
now we can go ahead and define the class.

1. Create PropertyAttributeWriter:
    
   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: */
      :end-before: // constructor end

   A BufferedWriter is created over the provided File, and the provided featureType is used to
   implement getAttribtueCount() and getAttributeType( index ).

2. Add hasNext() and next() implementations:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: // next start
      :end-before: // next end

  Our FeatureWriter does not provide any content of its own. FeatureWriters that are backed by
  JDBC ResultSets or random access file may use hasNext() to indicate that they are streaming
  over existing result set.
  
  Our implementation of next() will simply start a newLine for the feature that is about to be written.

3. Add writeFeatureID():

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: // writeFeatureID start
      :end-before: // writeFeatureID end

  Our file format is capable of storing FeatureIDs. Many DataStores will need to derive or encode
  FeatureID information into their Attributes.

4. Add write( int index, Object value ):

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: // write start
      :end-before: // write end

  Our implementation needs to prepend an equals sign before the first Attribute, or a bar for any
  other attribute.
  
  We also make sure to encode any newlines in String content, Geometry as wkt, and use the Converters
  class to handle any other objects correctly.

5. Add close():

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: // close start
      :end-before: // close end

6. Finally, to implement our FeatureWriter.close() optimization, we need to implement echoLine():

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeWriter.java
      :language: java
      :start-after: // echoLine start
      :end-before: // echoLine end

Part 5 - Using PropertyDataStore to Write Files
-----------------------------------------------

In this part we will explore the full capabilities of our completed PropertyDataStore.

Now that we have completed our PropertyDataStore implementation, we can explore the remaining
capabilities of the DataStore API.

PropertyDataStore API for data modification:

* PropertyDataStore.createSchema( featureType )
* PropertyDataStore.getFeatureWriter( typeName, filter, Transaction )
* PropertyDataStore.getFeatureWriter( typeName, Transaction )
* PropertyDataStore.getFeatureWriterAppend( typeName, Transaction )
* PropertyDataStore.getFeatureSource( typeName )

FeatureSource
^^^^^^^^^^^^^

The DataStore.getFeatureSource( typeName ) method is the gateway to our high level api, as
provided by an instance of FeatureSource, FeatureStore or FeatureLocking.

Now that we have implemented writing operations, the result of this method supports:

* FeatureSource: the query operations outlined in DataStore Tutorial 2: Use
* FeatureStore: modification and transaction support
* FeatureLocking: Interaction with a Feature-based Locking

FeatureStore
''''''''''''

FeatureStore provides Transaction support and modification operations. FeatureStore is an
extension of FeatureSource. You may check the result of getFeatureSource( typeName ) with the instanceof operator.

Example of FeatureStore use:

.. literalinclude:: /../../modules/plugin/property/src/test/java/org/geotools/data/property/PropertyExamples.java
   :language: java
   :start-after: // featureStoreExample start
   :end-before: // featureStoreExample end

FeatureStore defines:
    
* FeatureStore.addFeatures( featureReader)
* FeatureStore.removeFeatures( filter )
* FeatureStore.modifyFeatures( type, value, filter )
* FeatureStore.modifyFeatures( types, values, filter )
* FeatureStore.setFeatures( featureReader )
* FeatureStore.setTransaction( transaction )

Once again, many DataStores are able to provide optimised implementations of these operations.

Transaction Example:

.. literalinclude:: /../../modules/plugin/property/src/test/java/org/geotools/data/property/PropertyExamples.java
   :language: java
   :start-after: // transactionExample start
   :end-before: // transactionExample end

This produces the following output:

  .. literalinclude:: artifacts/output2
     :language: text
     :start-after: transactionExample start
     :end-before: transactionExample end

.. note::
   
   Please review the above code example carefully as it is the best explanation
   of transaction independence you will find.
   
   Specifically:
   
   * "auto-commit" represents the current contents of the file on disk
   * Notice how the transactions only reflect the changes the user made relative to
     the current file contents.
     
     This is shown after t1 commit, where transaction t2 is seeing 4 features (ie the
     current file contents plus the one feature that has been added on t2).
   * This really shows that FeatureSource and FeatureStore are simply "views" into your data.
     
     * If you configure two FeatureStores with the same transaction they will act the same.
     * Transaction is important and represents what you are working on
       FeatureStore is not as important and is just used to make working with your data
       easier (or more efficient) than direct use of FeatureWriter.
  
FeatureLocking
''''''''''''''

FeatureLocking is the last extension to our high-level API. It provides support for preventing
modifications to features for the duration of a Transaction, or a period of time.

FeatureLocking defines:

* FeatureLocking.setFeatureLock( featureLock )
* FeatureLocking.lockFeatures( query ) - lock features specified by query
* FeatureLocking.lockFeatures( filter ) - lock according to constraints
* FeatureLocking.lockFeatures() - lock all
* FeatureLocking.unLockFeatures( query )
* FeatureLocking.unLockFeatures( filter )
* FeatureLocking.unLockFeatures()
* FeatureLocking.releaseLock( string )
* FeatureLocking.refreshLock( string )

The concept of a FeatureLock matches the description provided in the OGC Web Feature Server
Specification. Locked Features can only be used via Transactions that have been provided with
the correct authorization.

FeatureWriter
^^^^^^^^^^^^^

We have a number of FeatureWriters available for different uses; these implementations
are used by the default implementation of AbstractFeatureStore and AbstractFeatureLocking.

These classes serve as a good example of how to use FeatureWriter.

FeatureWriter Filter
''''''''''''''''''''

The DataStore.getFeatureWriter( typeName, filter, transaction ) method 
creates a FeatureWriter used to modify features indicated by a constraint.

Example - removing all features:

.. literalinclude:: /../../modules/plugin/property/src/test/java/org/geotools/data/property/PropertyExamples.java
   :language: java
   :start-after: // removeAllExample start
   :end-before: // removeAllExample end

This FeatureWriter does not allow the addition of new content. It can be used for modification and removal only.

DataStores can often provide an optimized implementation.

FeatureWriter
'''''''''''''

The DataStore.getFeatureWriter( typeName, transaction )  method creates a general purpose
FeatureWriter. New content may be added after iterating through the
provided content.

Example - completely replace all features:

.. literalinclude:: /../../modules/plugin/property/src/test/java/org/geotools/data/property/PropertyExamples.java
   :language: java
   :start-after: // replaceAll start
   :end-before: // replaceAll end

FeatureWriter Append
''''''''''''''''''''

The DataStore.getFeatureWriterAppend( typeName, transaction ) method creates a FeatureWriter
for adding content.

Example - making a copy:

.. literalinclude:: /../../modules/plugin/property/src/test/java/org/geotools/data/property/PropertyExamples.java
   :language: java
   :start-after: // copyContent start
   :end-before: // copyContent end

DataStores can often provide an optimised implementation of this method.

Part 6 - Optimisation of PropertyDataStore
------------------------------------------

In this part we will explore several optimisation techniques using our PropertyDataStore.

Low-Level Optimisation
^^^^^^^^^^^^^^^^^^^^^^

AbstractDataStore provides a lot of functionality based on the five methods we implemented in the
Tutorials. By examining its implementation we have an opportunity to discuss several issues with
DataStore development. Please keep these issues in mind when applying your own DataStore
optimisations.

In general the "Gang of Four" decorator pattern is used to layer functionality around the
raw **FeatureReader** and **FeatureWriters** you provided. This is very similar to the design
of the **java-io** library (where a BufferedInputStream can be wrapped around a raw
FileInputStream).

From AbstractDataStore getFeatureReader( featureType, filter, transaction ):

.. note::
   
   Historically Filter.ALL and Filter.NONE were used as placeholder,
   as crazy as it sounds, Filter.ALL filters out ALL (accepts none)
   Filter.NONE filters out NONE (accepts ALL)/
   
   These two have been renamed in GeoTools 2.3 for the following:
   
   * Filter.ALL has been replaced with Filter.EXCLUDE
   * Filter.NONE has been replaced with Filter.INCLUDE

Here is an example of how AbstractDataStore applies wrappers around your raw feature reader::

    public  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,Transaction transaction) throws IOException {
        Filter filter = query.getFilter();
        String typeName = query.getTypeName();
        String propertyNames[] = query.getPropertyNames();
        
        ....
        
        if (filter == Filter.EXCLUDES) {
            return new EmptyFeatureReader(featureType);
        }
        String typeName = featureType.getTypeName();
        FeatureReader reader = getFeatureReader(typeName);
        if (filter != Filter.INCLUDES) {
            reader = new FilteringFeatureReader(reader, filter);
        }
        if (transaction != Transaction.AUTO_COMMIT) {
            Map diff = state(transaction).diff(typeName);
            reader = new DiffFeatureReader(reader, diff);
        }
        if (!featureType.equals(reader.getFeatureType())) {
            reader = new ReTypeFeatureReader(reader, featureType);
        }
        return reader;
    }

Support classes used:

* EmptyFeatureReader represents an empty result (when using Filter.ALL)
* FilteringFeatureReader skips over filtered elements using hasNext()
* TransactionStateDiff records a difference Map for the Transaction
* DiffFeatureReader is used as a wrapper, allowing the Features to be checked for removal, or
  modification before being provided to the user. Any additions performed against the
  Transaction are also returned.
* ReTypeFeatureReader allows on the fly Schema change

From AbstractDataStore getFeatureWriter( typeName, filter, transaction)::
    
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
            Transaction transaction) throws IOException {
        if (filter == Filter.ALL) {
            FeatureType featureType = getSchema(typeName);
            return new EmptyFeatureWriter(featureType);
        }
        FeatureWriter writer;
        
        if (transaction == Transaction.AUTO_COMMIT) {
            writer = getFeatureWriter(typeName);
        } else {
            writer = state(transaction).writer(typeName);
        }
        if (lockingManager != null) {
            writer = lockingManager.checkedWriter(writer, transaction);
        }
        if (filter != Filter.NONE) {
            writer = new FilteringFeatureWriter(writer, filter);
        }
        return writer;
    }

Support classes used:

* EmptyFeatureWriter represents an empty result
* TransactionStateDiff records a difference map for the Transaction, and provides a FeatureWrapper
  around a FeatureReader where modifications are stored in the difference Map
* FeatureLocking support is provided InProcessLockingManager in the form of a wrapper that will
  prevent modification taking place with out correct authorization
* FilteringFeatureWriter is used to skip over any Features not meeting the constraints

Every helper class we discussed above can be replaced if your external data source supports the
functionality.

External Transaction Support
''''''''''''''''''''''''''''

All JDBC DataStores support the concept of Transactions natively. JDBDataStore supplies an
example of using Transaction.State to store JDBC connection rather than the Difference map
used above.::
    
    public class JDBCTransactionState implements State {
        private Connection connection;
        public JDBCTransactionState( Connection c) throws IOException{
            connection = c;
        }
        public Connection getConnection(){
            return connection;
        }
        public void commit() throws IOException {
            connection.commit();
        }
        public void rollback() throws IOException {
            connection.rollback();            
        }
    }

For the purpose of PropertyDataStore we could create a Transaction.State class that records a
temporary File name used for a difference file. By externalising differences to a file rather
than Memory we will be able to handle larger data sets; and recover changes in the event of
an application crash.

Another realistic example is making use of Java Enterprise Edition session information allow
"per user" edits.

External Locking Support
''''''''''''''''''''''''

Several DataStores have an environment that can support native locking. By replacing the use
of the InProcessLockingManager we can make use of native Strong Transaction Support.

Single Use Feature Writers
''''''''''''''''''''''''''

We have a total of three distinct uses for FeatureWriters:

* AbstractDataStore.getFeatureWriter( typeName, transaction )
  
  General purpose FeatureWriter
* AbstractDataStore.getFeatureWriter( typeName, filter, transaction )
  
  An optimised version that does not create new content can be created.
* AbstractDataStore.getFeatureWriterAppend( typeName, transaction)
  
  An optimised version that duplicates the origional file, and opens it in append mode can be
  created. We can also perform special tricks such as returning a Feature delegate to the user,
  which records when it has been modified.

High-Level Optimisation
^^^^^^^^^^^^^^^^^^^^^^^

DataStore, FeatureSource and FeatureStore provide a few methods specifically set up
for optimisation.

DataStore Optimisation
''''''''''''''''''''''

AbstractDataStore leaves open a number of methods for high-level optmisations:

* PropertyDataStore.getCount( query )
* PropertyDatastore.getBounds( query )

These methods are designed to allow you to easily report the contents of information that
is often contained in your file header. Implementing them is optional, and each method
provides a way for you to indicate if the information is unavilable.

* PropertyDatastore.getFeatureSource( typeName );

By default the implementations returned are based simply on FeatureReader and FeatureWriter.
Override this method to return your own subclasses that are tuned for your data format.

FeatureStore Optimisation
'''''''''''''''''''''''''

DataStores operating against rich external data sources can often perform high level optimisations.
JDBCDataStores for instance can often construct SQL statements that completely fulfil a request
without making use of FeatureWriters at all.

When performing these queries please remember two things:

1. Check the lockingManager - If you are not providing your own native locking support, please
   check the user's authorisation against the the lockingManager
2. Event Notification - Remember to fire the appropriate notification events when contents change,
   Feature Caches will depend on this notification to accurately track the contents of your
   DataStore

Cacheing and FeatureListener
''''''''''''''''''''''''''''

A common optimisation is to trade memory use for speed by use of a cache. In this section we will
present a simple cache for getBounds() and getCount(Query.ALL).

The best place to locate your cache is in your DataStore implementation, you will need to keep
a separate cache for each Transaction by making use of Transaction.State. By implementing a cache
in the DataStore all operations can benefit.

Another popular technique is to locate the cache in an instance of FeatureSource. While the cache
will be duplicated when multiple FeatureStores are in use, it is convenient to locate the cache 
next to the high-level operations that can best benefit.

Finally FeatureResults represents a great opportunity to cache results, rather than reissue them
repeatedly.

FeatureListener (and associated FeatureEvents) provides notification of modification which can be
used to keep your cache implementation in sync with the DataStore.

PropertyDataStore
^^^^^^^^^^^^^^^^^

We can fill in the following methods for PropertyDataStore:

1. getCount( Query )
   
   We would like to improve this by recognizing the special case where the user has asked for
   the count of all of the features. In this case the number of Features is the same as
   the number of lines in the file (minus one for the header information and any comments).

   Things to look out for when reviewing the code:
   
   * File time stamp used to indicate when we cached the value, this is used to invalidate our
     cache if the file is changed at all.
   * A little bit of care is taken to avoid counting the header, any comment lines,
     and multiple lines
   
   We can offer a simple optimisation by counting the number of lines in our file,
   when the Query requests all features (using Filter.INLCUDE):

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getCount start
      :end-before: // getCount end

2. getBounds( Query )
   
   Our file format does not offer an easy way to sort out the bounds (spatial file formats
   often include this information in the header). As such we won't be implementing getBounds()

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getBounds start
      :end-before: // getBounds end

3. getFeatureSource( typeName )

   We will be returning the following classes (which we will create in the next section).

   * PropertyFeatureSource - if the file is read-only
   * PropertyFeatureStore - if the file is writable

   Here is what that looks like:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // getFeatureSource start
      :end-before: // getFeatureSource stop

   For a writable file we extend AbstractFeatureLocking which supports thread-safe Locking, and
   provides the correct hooks into the AbstractDataStore listenerManager.

PropertyFeatureSource
^^^^^^^^^^^^^^^^^^^^^

To implement a caching example we are going to produce our own implementation of FeatureSource:

1. Create the file PropertyFeatureSource:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureSource.java
      :language: java
      :start-after: */
      :end-before: // constructor end

2. We are extending AbstractFeatureSource here, as such we not need to implement
   FeatureSource.getCount( query ) as the default implementation will call up to
   PropertyDataStore.getCount( query ) implemented earlier.
   
3. We can however generate the bounds information and cache the result.
   
   Once again we are using a timestamp of the file to notice if the file is changed
   on disk.

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureSource.java
      :language: java
      :start-after: // getBounds start
      :end-before: // getBounds end

4. Earlier we modified PropertyDataStore to create an instance of this class if the file
   was read-only.
   
PropertyFeatureStore
^^^^^^^^^^^^^^^^^^^^

We are going to perform a similar set of optimisations to PropertyFeatureStore; with the added
wrinkle of listening to feature events so we can update our cached values in the event
modifications are made.

1. Create PropertyFeatureStore as follows:
   
   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureStore.java
      :language: java
      :start-after: */
      :end-before: // constructor end
   
   FeatureEvent provides a bounding box which we can use to selectively invalidate cacheBounds
   
2. Yes it is a little awkward not being able to smoothly extend PropertyFeatureSource (this
   is one of the fixes we have addressed for ContentDataStore covered in the next tutorial).

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureStore.java
      :language: java
      :start-after: // implementation start
      :end-before: // implementation end

3. This time we can implement getCount( query ) locally; being sure to check both
   the filter (includes all features) and the transaction (auto_commit):
   
   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureStore.java
      :language: java
      :start-after: // getCount start
      :end-before: // getCount end

4. In a similar fashion getBounds( query ) can be generated and cached:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyFeatureStore.java
      :language: java
      :start-after: // getBounds start
      :end-before: // getBounds end
      
5. We have already modified PropertyDataStore to return an instance of this class
   if the file was writable.

Part 7 - Quality Assurance
--------------------------

Since this tutorial has been written the gt-property module has been pressed into service as a
supported module in its own right.

References:

* :doc:`gt-property </library/data/property>` (User Guide)
* http://docs.geotools.org/latest/developer/guide/procedures/create.html (Developers Guide)
* http://docs.geotools.org/latest/developer/guide/procedures/supported.html (Developers Guide)

To get an idea of what kind of "extra" is required for a supported module:

* Package up as a maven module.
* JUnit Test Cases

Multiline
^^^^^^^^^  

A bug report asking that gt-properties support multi-line string values.
  
The suggestion was to use the Properties class, which we could not do and still retain our idea
of streaming the content from disk.
  
The following change was made To PropertyAttributeReader allow for multi-line entries:

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyAttributeReader.java
      :language: java
      :start-after: // multiline start
      :end-before: // multiline end

References:

* http://download.oracle.com/javase/6/docs/api/java/util/Properties.html#load(java.io.Reader)

.. hide
   
   Properties are processed in terms of lines. There are two kinds of line, natural lines and
   logical lines. A natural line is defined as a line of characters that is terminated either
   by a set of line terminator characters (\n or \r or \r\n) or by the end of the stream. A
   natural line may be either a blank line, a comment line, or hold all or some of a
   key-element pair. A logical line holds all the data of a key-element pair, which may be spread
   out across several adjacent natural lines by escaping the line terminator sequence with a
   backslash character \. Note that a comment line cannot be extended in this manner; every natural
   line that is a comment must have its own comment indicator, as described below. Lines are read
   from input until the end of the stream is reached.
   
   A natural line that contains only white space characters is considered blank and is ignored. A
   comment line has an ASCII '#' or '!' as its first non-white space character; comment lines are
   also ignored and do not encode key-element information. In addition to line terminators, this
   format considers the characters space (' ', '\u0020'), tab ('\t', '\u0009'), and form feed
   ('\f', '\u000C') to be white space.
   
   If a logical line is spread across several natural lines, the backslash escaping the
   line terminator sequence, the line terminator sequence, and any white space at the start of
   the following line have no affect on the key or element values. The remainder of the
   discussion of key and element parsing (when loading) will assume all the characters
   constituting the key and element appear on a single natural line after line continuation
   characters have been removed. Note that it is not sufficient to only examine the character
   preceding a line terminator sequence to decide if the line terminator is escaped; there must
   be an odd number of contiguous backslashes for the line terminator to be escaped. Since the
   input is processed from left to right, a non-zero even number of 2n contiguous backslashes
   before a line terminator (or elsewhere) encodes n backslashes after escape processing.
   
   The key contains all of the characters in the line starting with the first non-white space
   character and up to, but not including, the first unescaped '=', ':', or white space character
   other than a line terminator. All of these key termination characters may be included in the
   key by escaping them with a preceding backslash character; for example,::

     \:\=

   would be the two-character key ":=". Line terminator characters can be included using \r and
   \n escape sequences. Any white space after the key is skipped; if the first non-white space
   character after the key is '=' or ':', then it is ignored and any white space characters after
   it are also skipped. All remaining characters on the line become part of the associated element
   string; if there are no remaining characters, the element is the empty string "". Once the raw
   character sequences constituting the key and element are identified, escape processing is
   performed as described above.

   As an example, each of the following three lines specifies the key "Truth" and the associated
   element value "Beauty"::
       
        Truth = Beauty
               Truth:Beauty
        Truth                  :Beauty
 
   As another example, the following three lines specify a single property::

        fruits                           apple, banana, pear, \
                                         cantaloupe, watermelon, \
                                         kiwi, mango
 
   The key is "fruits" and the associated element is::
        
        "apple, banana, pear, cantaloupe, watermelon, kiwi, mango"

   Note that a space appears before each \ so that a space will appear after each comma in the
   final result; the \, line terminator, and leading white space on the continuation line are
   merely discarded and are not replaced by one or more other characters.

   As a third example, the line:
   
       cheeses
   
   specifies that the key is "cheeses" and the associated element is the empty string "".

   Characters in keys and elements can be represented in escape sequences similar to those used
   for character and string literals (see 3.3 and 3.10.6 of the Java Language Specification).

   The differences from the character escape sequences and Unicode escapes used for characters and
   strings are:
   
   * Octal escapes are not recognized.
   * The character sequence \b does not represent a backspace character.
   * The method does not treat a backslash character, \, before a non-valid escape character as an
     error; the backslash is silently dropped. For example, in a Java string the sequence "\z"
     would cause a compile time error. In contrast, this method silently drops the backslash.
     Therefore, this method treats the two character sequence "\b" as equivalent to the single
     character 'b'.
   * Escapes are not necessary for single and double quotes; however, by the rule above, single and
     double quote characters preceded by a backslash still yield single and double quote
     characters, respectively.
   * Only a single 'u' character is allowed in a Uniocde escape sequence.
   
Info
^^^^

Another reported issue. You can fill in a "info" data strucutre to more accurately describe your
information to the uDig or GeoServer catalog.

   .. literalinclude:: /../../modules/plugin/property/src/main/java/org/geotools/data/property/PropertyDataStore.java
      :language: java
      :start-after: // info start
      :end-before: // info end