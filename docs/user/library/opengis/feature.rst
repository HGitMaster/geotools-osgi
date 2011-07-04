Feature
-------

The Feature class is used to represents "something that can be drawn on a map". 

References:

* `org.opengis.feature <http://docs.geotools.org/stable/javadocs/org/opengis/feature/package-summary.html>`_ (javadocs)
* :doc:`../main/feature` gt-main code examples

Data Structure
^^^^^^^^^^^^^^

Feature stores your information in a data structure composed of Feature, Attributes, and Associations.


.. image:: /images/feature_data_model.PNG

To directly create a Feature you can make use of **FeatureFactory**, or use a builder which makes things a little easier (as it fills in defaults)::

   //create the builder
   SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
   
   //add the values
   builder.add( "Canada" );
   builder.add( 1 );
   builder.add( 20.5 );
   builder.add( new Point( -124, 52 ) );

   //build the feature with provided ID
   SimpleFeature feature = builder.buildFeature( "fid.1" );

In practice you can use a feature much like a java.util.Map to get and put values. The difference is that the "keys" are formally defined by the FeatureType (which helps us swap information between systems).

This is a little bit easier with SimpleFeature which allows direct access to attribute values:
  
  feature.setAttribute( "turbidity", 3.7 );
  Object value = feature.getAttribute( "turbidity" ); // returns double 3.7 stored above

For more information see the examples listed in gt-main :doc:`../main/feature`.
