/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.List;

import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.commons.Language;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;


/**
 * <b>TODO WARNING THIS IS A WORK IN PROGRESS.</b>
 * 
 * <p>
 * <b>TXT Query Language</b> is an extension of <b>CQL</b>. This class presents the operations available 
 * to parse the TXT language and generates the correspondent filter.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Here are some usage examples. Refer to the <a href="http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design">complete
 * grammar</a> to see what exactly you can do.
 *
 * <pre>
 * <code>
 *       Filter filter = TXT.toFilter(<b>"POP_RANK  &gt;  6"</b>);
 *        
 *       Filter filter = TXT.toFilter(<b>"POP_RANK &gt; 3 AND POP_RANK &lt; 6"</b>);
 *        
 *       Filter filter = TXT.toFilter(<b>"area(the_geom) &gt; 3000"</b>);
 *        
 *       Filter filter = TXT.toFilter(<b>"Name LIKE '%omer%'"</b>);
 *       
 *       Filter filter = TXT.toFilter(<b>"RELATE( the_geom1,the_geom2) like 'T**F*****'"</b>);
 *
 *       Filter filter = TXT.toFilter(<b>"DISJOINT(buffer(the_geom, 10) , POINT(1 2))"</b>);
 *
 *       Filter filter = TXT.toFilter(<b>"ID IN ('river.1', 'river.2')"</b>);
 *       
 *       Filter filter = TXT.toFilter(<b>"LENGHT IN (4100001,4100002, 4100003 )"</b>);
 *
 *       List &lt;Filter&gt; list = TXT.toFilterList(<b>"LENGHT = 100; NAME like '%omer%'"</b>);
 *
 *       Expression expression = TXT.toExpression(<b>"LENGHT + 100"</b>);
 *
 * </code>
 * </pre>
 * </p>
 * @author Jody Garnett
 * @author Mauricio Pazos (Axios Engineering)
 * 
 * @since 2.6
 */
class TXT {
    
    private TXT(){
        // do nothing, private constructor
        // to indicate it is a pure utility class
    }

    /**
     * Parses the input string in TXT format into a Filter, using the
     * systems default FilterFactory implementation.
     *
     * @param txtPredicate
     *            a string containing a query predicate in TXT format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>txtPredicate</code>.
     */
    public static Filter toFilter(final String txtPredicate)
        throws CQLException {
        Filter filter = TXT.toFilter(txtPredicate, null);

        return filter;
    }

    /**
     * Parses the input string in TXT format into a Filter, using the
     * provided FilterFactory.
     *
     * @param txtPredicate
     *            a string containing a query predicate in TXT format.
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter. If it is null the method finds the default implementation.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>Predicate</code>.
     */
    public static Filter toFilter(final String txtPredicate, final FilterFactory filterFactory)
        throws CQLException {

        Filter result = CompilerUtil.parseFilter(Language.TXT, txtPredicate, filterFactory);

        return result;
    }
    

    /**
     * Parses the input string in TXT format into an Expression, using the
     * systems default FilterFactory implementation.
     *
     * @param txtExpression  a string containing an TXT expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>txtExpression</code>.
     */
    public static Expression toExpression(String txtExpression)
        throws CQLException {
        return toExpression(txtExpression, null);
    }

    /**
     * Parses the input string in TXT format and makes the correspondent Expression , 
     * using the provided FilterFactory.
     *
     * @param txtExpression
     *            a string containing a TXT expression.
     *
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression. If it is null the method finds the default implementation.    
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>txtExpression</code>.
     */
    public static Expression toExpression(final String txtExpression,
            final FilterFactory filterFactory) throws CQLException {

        Expression expression = CompilerUtil.parseExpression(Language.TXT,
                txtExpression, filterFactory);

        return expression;
    }

    /**
     * Parses the input string, which has to be a list of TXT predicates
     * separated by <code>;</code> into a <code>List</code> of
     * <code>Filter</code>s, using the provided FilterFactory.
     *
     * @param txtSequencePredicate
     *            a list of OGC CQL predicates separated by <code>|</code>
     *
     * @return a List of {@link Filter}, one for each input CQL statement
     */
    public static List<Filter> toFilterList(final String txtSequencePredicate)
        throws CQLException {
        
        List<Filter> filters =  CompilerUtil.parseFilterList(Language.TXT, txtSequencePredicate);

        return filters;
    }
    
    
}
