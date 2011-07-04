ECQL
----

The ECQL language is intended as an extension of CQL, thus you can write all predicates supported by CQL and use the new expression possibilities defined in the new syntax rules.

References

* `ECQL Parser Design <http://docs.codehaus.org/display/GEOTOOLS/ECQL+Parser+Design>`_ (wiki with BNF)
* `GeoServer CQL Examples <http://docs.geoserver.org/latest/en/user/tutorials/cql/cql_tutorial.html>`_ (geoserver)

ECQL Utility Class
^^^^^^^^^^^^^^^^^^

The ECQL utility class is method compatible allowing you to use it as a drop-in replacement for CQL.

.. image:: /images/ecql.PNG

Running
'''''''

As you can see above the ECQL class can be run on the command line.

It allows you to try out the ECQL examples on this page; and produces the XML Filter representation of the result.::
    
    ECQL Filter Tester
    "Seperate with \";\" or \"quit\" to finish)
    >attr > 10
    <?xml version="1.0" encoding="UTF-8"?>
    <ogc:PropertyIsGreaterThan xmlns="http://www.opengis.net/ogc" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
    <ogc:PropertyName>attr</ogc:PropertyName>
    <ogc:Literal>10</ogc:Literal>
    </ogc:PropertyIsGreaterThan>
    
    >quit
    Bye!

Examples
''''''''

* Filter by Comparing Values
  
  The CQL language limited us to referencing a propertyName against
  a more general expression.

  ECQL allows you to use full expressions everywhere:

  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
    :language: java
    :start-after: // expressionLessThanOrEqualToProperty start
    :end-before: // expressionLessThanOrEqualToProperty end        

  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
    :language: java
    :start-after: // comparisonUsingExpressions start
    :end-before: // comparisonUsingExpressions end        
        
  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
    :language: java
    :start-after: // betweenPredicate start
    :end-before: // betweenPredicate end        
        
  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
    :language: java
    :start-after: // betweenUsingExpression start
    :end-before: // betweenUsingExpression end        


* Filter by a List of Features' ID
  
  The Filter XML format allows the definition of an **Id** Filter
  capturing a set of FeatureIDs (often representing a selection).
  
  Using string as id::

        Filter filter = ECQL.toFilter("IN ('river.1', 'river.2')");
  
  Using integer as id::
  
        Filter filter = ECQL.toFilter("IN (300, 301)");
  
  Filter based in a set of values::
  
        Filter filter = ECQL.toFilter("length IN (4100001,4100002, 4100003 )");
        Filter filter = ECQL.toFilter("length IN ( (1+2), 3-4, [5*6] )");
        Filter filter = ECQL.toFilter("huc_8 IN (abs(-1), area(the_geom)");
  
  We tried a couple of experiments, not all of them worked leaving
  us with the following deprecated syntax::
  
        Filter filter = ECQL.toFilter("ID IN ('river.1', 'river.2')");


* Filter using a text pattern:

  Filter for a text pattern using **LIKE** keyword:

  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
     :language: java
     :start-after: // ecql likePredicate start
     :end-before: // ecql likePredicate end

  Case insensitive example with **ILIKE** keyword:

  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
     :language: java
     :start-after: // ecql ilikePredicate start
     :end-before: // ecql ilikePredicate end
     
  ECQL allows you to test any two expression, including literals:

  .. literalinclude:: /../src/main/java/org/geotools/cql/ECQLExamples.java
     :language: java
     :start-after: // ecql likePredicateInString start
     :end-before: // ecql likePredicateInString end


* Filter by spatial relation:
  
  The ability to use a full expression also applies to spatial operations
  allowing us to process a geometry using a function as in the following
  example::
  
        Filter filter = ECQL.toFilter("DISJOINT(the_geom, POINT(1 2))");
        Filter filter = ECQL.toFilter("DISJOINT(buffer(the_geom, 10) , POINT(1 2))");
        Filter filter = ECQL.toFilter(
                "DWITHIN(buffer(the_geom,5), POINT(1 2), 10, kilometers)");

* Filter by Comparing time values::
  
        Filter filter = ECQL.toFilter("DATE BEFORE 2006-12-31T01:30:00Z");

* Filter Nulls::
  
        Filter filter = ECQL.toFilter(" Name IS NULL");
        Filter filter = ECQL.toFilter("centroid( the_geom ) IS NULL");

* Property Exist Predicate::
        
        Filter resultFilter = ECQL.toFilter("aProperty EXISTS");

* Expression
  
  Expressions support is unchanged::
        
        Expression expr = ECQL.toExpression("X + 1");

* Filter list
  
  Filter list is still supported using a ";" to separate entries::
        
        List<Filter> list = ECQL.toFilterList("X=1; Y<4");

