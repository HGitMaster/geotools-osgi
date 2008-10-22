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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.commons.Language;
import org.geotools.filter.text.cql2.CQLLikePredicateTest;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.PropertyIsLike;

/**
 * Test for like predicate
 * 
 * <p>
 *
 * <pre>
 *  &lt;text predicate &gt; ::=
 *      &lt;expression &gt; [ NOT ] <b>LIKE</b>  &lt;character pattern &gt;
 *
 * </pre>
 * <p>
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class TXTLikePredicateTest extends CQLLikePredicateTest {
    
    public TXTLikePredicateTest(){
        super(Language.TXT);
    }
    
    /**
     * Test Text Predicate
     * <p>
     * Sample: strConcat('aa', 'bbcc') like '%bb%'
     * </p>
     */
    @Test
    public void functionlikePredicate() throws Exception {

        // Like strConcat('aa', 'bbcc') like '%bb%'
        Filter resultFilter = CompilerUtil.parseFilter(this.language, FilterTXTSample.FUNCTION_LIKE_TXT_PATTERN);

        Assert.assertNotNull("Filter expected", resultFilter);

        Assert.assertTrue(resultFilter instanceof PropertyIsLike);
        
        PropertyIsLike expected = (PropertyIsLike) FilterTXTSample.getSample(FilterTXTSample.FUNCTION_LIKE_TXT_PATTERN);

        Assert.assertEquals("like filter was expected", expected, resultFilter);
    }
    
    /**
     * Test Text Predicate
     * <p>
     * Sample: 'aabbcc' like '%bb%'
     * </p>
     */
    @Test
    public void literallikePredicate() throws Exception {

        Filter resultFilter = CompilerUtil.parseFilter(this.language, FilterTXTSample.LITERAL_LIKE_TXT_PATTERN);

        Assert.assertNotNull("Filter expected", resultFilter);

        Assert.assertTrue(resultFilter instanceof PropertyIsLike);
        
        PropertyIsLike expected = (PropertyIsLike) FilterTXTSample.getSample(FilterTXTSample.LITERAL_LIKE_TXT_PATTERN);

        Assert.assertEquals("like filter was expected", expected, resultFilter);

    }

    @Test
    public void literalNotlikePredicate() throws Exception {

        Filter resultFilter = CompilerUtil.parseFilter(this.language, FilterTXTSample.LITERAL_NOT_LIKE_TXT_PATTERN);

        Assert.assertNotNull("Filter expected", resultFilter);

        Assert.assertTrue(resultFilter instanceof Not);
        
        Not expected = (Not) FilterTXTSample.getSample(FilterTXTSample.LITERAL_NOT_LIKE_TXT_PATTERN);
        
        Assert.assertTrue(expected.getFilter() instanceof PropertyIsLike);
        
        Assert.assertEquals("like filter was expected", expected, resultFilter);
    }
    
}
