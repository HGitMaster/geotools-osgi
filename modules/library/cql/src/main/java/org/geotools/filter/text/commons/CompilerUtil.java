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

package org.geotools.filter.text.commons;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * Compiler Utility class.
 * 
 * <p>
 * This is an internal utility class with convenient methods for compiler actions.
 * 
 * This is intended as an internal interface used only in this module. Client modules
 * mustn't use it.
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
final public class CompilerUtil {

    private CompilerUtil(){
        // utility class
    }
    
    
    /**
     * Returns the filter resultant of the parsing process of predicate expression.
     * Makes Expressions for the predicate
     * @param language          the dialect of language
     * @param predicate         a valid search predicate for the language
     * @param filterFactory     a filter factory used to make the expression
     * @return an {@link Filter}
     * @throws CQLException
     */
    static final public Filter parseFilter(final Language language, final String predicate, FilterFactory filterFactory) throws CQLException {

        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";
        
        ICompiler compiler = CompilerFactory.makeCompiler(language, predicate, filterFactory);
        compiler.compileFilter();
        Filter result = compiler.getFilter();

        return result;
    }

    /**
     * Makes the Filter for the predicate
     * @param language          the dialect of language
     * @param predicate         a valid search predicate for the language
     * @return a {@link Filter}
     * @throws CQLException
     */
    static final public Filter parseFilter(final Language language, final String predicate) throws CQLException {

        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";
        
        Filter result = parseFilter(language, predicate, null);

        return result;
    }

    /**
     * Makes Expressions for the predicate
     * @param language          the dialect of language
     * @param predicate         a valid expression for the language
     * @param filterFactory     a filter factory used to make the expression
     * @return an {@link Expression}
     * @throws CQLException
     */
    static final public Expression parseExpression(final Language language, final String predicate, FilterFactory filterFactory) throws CQLException {

        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";
        
        ICompiler compiler = CompilerFactory.makeCompiler(language, predicate, filterFactory);
        compiler.compileExpression();
        Expression result = compiler.getExpression();

        return result;
    }

    /**
     * Makes Expressions for the predicate
     * @param language          the dialect of language
     * @param predicate         a valid expression for the language
     * @return an {@link Expression}
     * @throws CQLException
     */
    static final public Expression parseExpression(final Language language, final String predicate) throws CQLException {

        Expression result = parseExpression(language, predicate, null);

        return result;
    }
    


    /**
     * Makes a list of filters extracted from the sequence of search predicates
     * 
     * @param language          the dialect of language
     * @param predicate         a valid expression for the language
     * @param filterFactory     a filter factory used to make the each filter
     * @return a {@link List} of filters
     * @throws CQLException
     */
    public static List<Filter> parseFilterList(final Language language, String predicate,
            FilterFactory filterFactory) throws CQLException {
        
        assert language != null: "language cannot be null";
        assert predicate != null:"predicate cannot be null";

        ICompiler compiler = CompilerFactory.makeCompiler(language, predicate, filterFactory);
        compiler.compileFilterList();
        List<Filter> results = compiler.getFilterList();
        
        return results;
    }

    /**
     * Makes a list of filters extracted from the sequence of search predicates
     * 
     * @param language          the dialect of language
     * @param predicate         a valid expression for the language
     * @return a {@link List} of filters
     * @throws CQLException
     */
    public static List<Filter> parseFilterList(final Language language, String predicate) throws CQLException {
        
        List<Filter> results = parseFilterList(language, predicate, null);
        
        return results;
    }
    
}
