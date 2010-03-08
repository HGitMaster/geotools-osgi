.. _crslab:

CRS Lab
=======

This tutorial gives a visual demonstration of coordinate reference systems by displaying
a shapefile and showing how changing the map projection morphs the shape of the features.

Dependencies
------------
 
Please ensure your pom.xml includes the following::

  <dependencies>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-swing</artifactId>
      <version>${geotools.version}</version>
      <!-- For this module we explicitly exclude some of its own -->
      <!-- dependencies from being downloaded because they are   -->
      <!-- big and we don't need them                            -->
      <exclusions>
        <exclusion>
          <groupId>org.apache.xmlgraphics</groupId>
          <artifactId>batik-transcoder</artifactId>
          </exclusion>
        </exclusions>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-shapefile</artifactId>
      <version>${geotools.version}</version>
    </dependency>
    <dependency>
      <groupId>org.geotools</groupId>
      <artifactId>gt-epsg-hsql</artifactId>
      <version>${geotools.version}</version>
    </dependency>
  </dependencies>

Example
-------

The example code is available
 * Directly from svn: CRSLab.java_
 * Included in the demo directory when you download the GeoTools source code

.. _CRSLab.java: http://svn.osgeo.org/geotools/trunk/demo/example/src/main/java/org/geotools/demo/CRSLab.java 
 
Main Application
----------------
1. Please create the file **CRSLab.java**
2. Copy and paste in the following code:

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start source
      :end-before: // docs end main

Displaying the shapefile
------------------------

This method opens and connects to a shapefile and uses a **JMapFrame** to display it. It should look familiar to you from 
the :ref:`quickstart` example.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start display
      :end-before: // docs end display

Notice that we are customizing the JMapFrame by adding three buttons to its toolbar.

The mapFrame.enableTool(JMapFrame.Tool.NONE) statement requests that an empty toolbar be initially created.

Button actions
--------------

In the method above we initialized each of the toolbar buttons with an Action. Let's look at each of these now.

Change CRS Action
~~~~~~~~~~~~~~~~~

The first two statements set the name (displayed on the button) and the tooltip.

The important bit is where we display a JCRSChooser dialog to prompt the user for a new CRS which we pass to the MapContext. This has
the effect of refreshing the map display.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start crs action
      :end-before: // docs end crs action

Export Action
~~~~~~~~~~~~~

This simply delegates to the exportToShapefile method which we'll look at shortly.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start export action
      :end-before: // docs end export action

Validate Action
~~~~~~~~~~~~~~~

This action also delegates to a helper method, validateFeatureGeometry, but it uses the SwingWorker
utility class to run the validation process in a background thread in case we are dealing with a large
shapefile.

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start validate action
      :end-before: // docs end validate action

Exporting reprojected data to a shapefile
-----------------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start export
      :end-before: // docs end export

Validating feature geometry
---------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start validate
      :end-before: // docs end validate


Running the application
-----------------------

A good shapefile to use with this example is the **bc_border** map which can be downloaded as part of the `uDig sample data`__.

.. _udigdata: http://udig.refractions.net/docs/data-v1_2.zip

__ udigdata_

*To be continued...*

An alternative export to shapefile method
-----------------------------------------

   .. literalinclude:: ../../../../../demo/example/src/main/java/org/geotools/demo/CRSLab.java
      :language: java
      :start-after: // docs start export2
      :end-before: // docs end export2

