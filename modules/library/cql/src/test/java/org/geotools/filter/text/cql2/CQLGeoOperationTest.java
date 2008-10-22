/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.text.cql2;

import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.commons.Language;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

/**
 * Test Geo Operations.
 * <p>
 * 
 * <pre>
 *   &lt;routine invocation &gt; ::=
 *           &lt;geoop name &gt; &lt;georoutine argument list &gt;[*]
 *       |   &lt;relgeoop name &gt; &lt;relgeoop argument list &gt;
 *       |   &lt;routine name &gt; &lt;argument list &gt;
 *   &lt;geoop name &gt; ::=
 *           EQUAL | DISJOINT | INTERSECT | TOUCH | CROSS | [*]
 *           WITHIN | CONTAINS |OVERLAP | RELATE [*]
 *   That rule is extended with bbox for convenience.
 *   &lt;bbox argument list &gt;::=
 *       &quot;(&quot;  &lt;attribute &gt; &quot;,&quot; &lt;min X &gt; &quot;,&quot; &lt;min Y &gt; &quot;,&quot; &lt;max X &gt; &quot;,&quot; &lt;max Y &gt;[&quot;,&quot;  &lt;srs &gt;] &quot;)&quot;
 *       &lt;min X &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;min Y &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;max X &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;max Y &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;srs &gt; ::=
 * </pre>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class CQLGeoOperationTest {

    
    protected final Language language;

    /**
     * New instance of CQLTemporalPredicateTest
     */
    public CQLGeoOperationTest(){
        
        this(Language.CQL);
    }

    /**
     * New instance of CQLTemporalPredicateTest
     * @param language
     */
    public CQLGeoOperationTest(final Language language){
        
        assert language != null: "language cannot be null value";
        
        this.language = language;
    }
        

    @Test
    public void disjoint() throws CQLException{
        

        Filter resultFilter = CompilerUtil.parseFilter(language, "DISJOINT(ATTR1, POINT(1 2))");

        Assert.assertTrue("Disjoint was expected", resultFilter instanceof Disjoint);
    }

    @Test
    public void Intersects() throws CQLException {
        Filter resultFilter = CompilerUtil.parseFilter(language,"INTERSECT(ATTR1, POINT(1 2))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);
        
        //test bug GEOT-1980
        CompilerUtil.parseFilter(language,"INTERSECT(GEOLOC, POINT(615358 312185))");
        
        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);
    }

    @Test
    public void touches() throws CQLException{
        Filter resultFilter = CompilerUtil.parseFilter(language,"TOUCH(ATTR1, POINT(1 2))");

        Assert.assertTrue("Touches was expected", resultFilter instanceof Touches);
    }

    public void crosses() throws CQLException {
        Filter resultFilter = CompilerUtil.parseFilter(language,"CROSS(ATTR1, POINT(1 2))");

        Assert.assertTrue("Crosses was expected", resultFilter instanceof Crosses);
        
    }
    @Test
    public void contains() throws CQLException      {
        Filter resultFilter = CompilerUtil.parseFilter(language,"CONTAINS(ATTR1, POINT(1 2))");

        Assert.assertTrue("Contains was expected", resultFilter instanceof Contains);
        
    }
    
    @Test
    public void overlaps() throws Exception {
        Filter resultFilter;


        resultFilter = CompilerUtil.parseFilter(language,"OVERLAP(ATTR1, POINT(1 2))");

        Assert.assertTrue("Overlaps was expected", resultFilter instanceof Overlaps);

        
    }
    @Test
    public void equals() throws CQLException{
        // EQUALS
        Filter resultFilter = CompilerUtil.parseFilter(language,"EQUAL(ATTR1, POINT(1 2))");

        Assert.assertTrue("not an instance of Equals", resultFilter instanceof Equals);

        resultFilter = CompilerUtil.parseFilter(language,"WITHIN(ATTR1, POLYGON((1 2, 1 10, 5 10, 1 2)) )");

        Assert.assertTrue("Within was expected", resultFilter instanceof Within);
        
    }

    @Test
    public void bbox() throws CQLException{
        
        Filter resultFilter;
        
        // BBOX
        resultFilter = CompilerUtil.parseFilter(language,"BBOX(ATTR1, 10.0,20.0,30.0,40.0)");
        Assert.assertTrue("BBox was expected", resultFilter instanceof BBOX);
        BBOX bboxFilter = (BBOX) resultFilter;
        Assert.assertEquals(bboxFilter.getMinX(), 10.0, 0.1);
        Assert.assertEquals(bboxFilter.getMinY(), 20.0, 0.1);
        Assert.assertEquals(bboxFilter.getMaxX(), 30.0, 0.1);
        Assert.assertEquals(bboxFilter.getMaxY(), 40.0, 0.1);
        Assert.assertEquals(null, bboxFilter.getSRS());

        // BBOX using EPSG
        resultFilter = CompilerUtil.parseFilter(language,"BBOX(ATTR1, 10.0,20.0,30.0,40.0, 'EPSG:4326')");
        Assert.assertTrue("BBox was expected", resultFilter instanceof BBOX);
        bboxFilter = (BBOX) resultFilter;
        Assert.assertEquals("EPSG:4326", bboxFilter.getSRS());
        
    }
    
}
