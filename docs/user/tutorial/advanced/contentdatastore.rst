:Author: Jody Garnett
:Thanks: geotools-devel list
:Version: |release|
:License: Creative Commons with attribution

ContentDataStore Tutorial
=========================

.. sectionauthor:: Jody Garnett <jody.garnett@gmail.org>

Writing a new DataStore for GeoTools is one of the best ways to get involved in the project, as
writing it will make clear many of the core concepts of the API.

The modular nature of GeoTools allows new DataStores to quickly become part of the next release
(we have an "unsupported" directory just for this purpose), so that new formats are can 
quickly be distributed to to all GeoTools users.

.. note::
   
   AbstractDataStore is the original GeoTools 2.0 class; since that time we have learned
   a number of tricks and have a much easier starting point for you in the form of
   **ContentDataStore**.
   
   While **ContentDataStore** is a lot less work to use; it is not yet as fully featured
   as AbstractDataStore. You may wish to try both tutorials before deciding on a course
   of action.

**Terminology**

The DataStore interface borrows most of its concepts and some of its syntax from the OpenGIS
Consortium (OGC) Web Feature Server Specification:

* Feature - atomic unit of geographic information
* FeatureType - keeps track of what attributes each Feature can hold
* FeatureID - a unique id associated with each Feature (must start with a non-numeric character)

Introduction
------------

An earlier tutorial produced a bit of code to read in a comma seperated value file; and produce
a feature collection (which we could save out using the shapefile datastore class).

This time out we are going to make a DataStore.

Here was the sample file we used:

#. Create a text file location.csv and copy and paste the following locations into it:

   .. literalinclude:: artifacts/locations.csv

#. Or download :download:`locations.csv <artifacts/locations.csv>`.


Approach
^^^^^^^^

Here is our strategy for representing GeoTools concepts with a CSV file.

* FeatureID or FID - uniquely defines a Feature.
  
  We will use the row number in our CSV file.

* FeatureType
  
  Same as the name of the .csv file (ie. "locations" for locations.csv)

* DataStore
  
  We will create a CSVDataStore to access all the FeatureTypes (.csv files) in a directory

* FeatureType or Schema

  We will represent the names of the columns in our CSV (and if possible their types).

* Geometry
  
  We will try and recognise several columns and map them into Point x and y ordinates.
    
JavaCSV Reader
^^^^^^^^^^^^^^

To read csv files this time out we are going to make use of the Java CSV Reader project.

* http://www.csvreader.com/java_csv.php

Time to create a new project making use of this library:

#. Create a *csv* project using maven
#. Use the following maven dependencies:
   
   .. literalinclude:: artifacts/pom.xml
      :language: xml
      :start-after: </properties>
      :end-before: <repositories>

#. Or download :download:`pom.xml <artifacts/pom.xml>`  

Creating CSVDataStore
---------------------

The first step is to create a basic DataStore that only supports feature extraction. We will read
data from a csv file into the GeoTools feature model.

To implement a DataStore we will subclass ContentDataStore. This is a helpful base class for
making new kinds of content available to GeoTools. The GeoTools library works with an interaction
model very similar to a database - with transactions and locks. ContentDataStore is going to handle
all of this for us - as long as we can teach it how to access our content.

ContentDataStore requires us to implement the following two methods:

* createTypeNames() - name of all the different kinds of content (tables or types). In a CSV file we
  will only have one kind of content
* createFeatureSource(ContentEntry entry)

The class *ContentEntry* is a bit of a scratch pad used to keep track of things for each type.

Initially we are going to make a read-only datastore accessing CSV content:

#. To begin create the file CSVDataStore extending ContentDataStore

   .. literalinclude:: /../src/main/java/org/geotools/tutorial/datastore/CSVDataStore.java
      :language: java
      :start-after: // header start
      :end-before: // header end
      
#. We are going to be working with a single CSV file

   .. literalinclude:: /../src/main/java/org/geotools/tutorial/datastore/CSVDataStore.java
      :language: java
      :start-after: // constructor start
      :end-before: // constructor end

Listing TypeNames
^^^^^^^^^^^^^^^^^

A DataStore may provide access to several different types of information. The method createTypeNames
provides a list of the available types. This is called once; and then the same list is returned
by ContentDataStore.getTypeNames() each time. (This allows you to do some real work; such as
connecting to a web service or parsing a large file, without worrying about doing it
many times).

For our purposes this list will be the name of the csv file.

#. We can now implement createTypeNames() returning a filename

   .. literalinclude:: /../src/main/java/org/geotools/tutorial/datastore/CSVDataStore.java
      :language: java
      :start-after: // createTypeNames start
      :end-before: // createTypeNames end

To be continued
---------------

I have not been able to complete writing this tutorial ... to volunteer to test please
ask for Jody Garnett on the geotools user list.

Here are the working downloads:

* :download:`CSVDataStore.java </../../modules/unsupported/csv/src/main/java/org/geotools/data/csv/CSVDataStore.java>`
* :download:`CSVDataStoreFactory.java </../../modules/unsupported/csv/src/main/java/org/geotools/data/csv/CSVDataStoreFactory.java>`
* :download:`CSVFeatureReader.java </../../modules/unsupported/csv/src/main/java/org/geotools/data/csv/CSVFeatureReader.java>`
* :download:`CSVFeatureSource.java </../../modules/unsupported/csv/src/main/java/org/geotools/data/csv/CSVFeatureSource.java>`
* :download:`META-INF/services/org.geotools.data.DataStoreFactorySpi </../../modules/unsupported/csv/src/main/resources/META-INF/services/org.geotools.data.DataStoreFactorySpi>`

