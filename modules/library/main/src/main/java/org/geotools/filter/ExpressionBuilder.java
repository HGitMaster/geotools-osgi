/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.parser.ExpressionException;
import org.geotools.filter.parser.ExpressionParser;
import org.geotools.filter.parser.ExpressionParserTreeConstants;
import org.geotools.filter.parser.Node;
import org.geotools.filter.parser.ParseException;
import org.geotools.filter.parser.Token;
import org.geotools.filter.parser.TokenMgrError;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.SpatialOperator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;


/**
 * ExpressionBuilder is the main entry point for parsing Expressions and Filters
 * from a non-xml language.
 * <p>
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/filter/ExpressionBuilder.java $
 * @version GeoTools 2.2 Revised to take a FilterFactory.
 * @author  Ian Schneider
 * @deprecated Please use CQL
 */
public class ExpressionBuilder {
    
    static final Set<String> GEOMETRY_FILTERS = new HashSet<String>() {
        {
            add("EQUALS");
            add("DISJOINT");
            add("INTERSECTS");
            add("TOUCHES");
            add("CROSSES");
            add("WITHIN");
            add("CONTAINS");
            add("OVERLAPS");
        }
    };
    
    private org.opengis.filter.FilterFactory2 factory;

    public ExpressionBuilder() {
        this(CommonFactoryFinder.getFilterFactory2(null));
    }

    public ExpressionBuilder(org.opengis.filter.FilterFactory2 factory) {
        this.factory = factory;
    }
	
    public void setFilterFactory( org.opengis.filter.FilterFactory2 factory ){
    	this.factory = factory;
    }
    /**
     * @deprecated please use parser
     * @param schema
     * @param input
     * @throws ParseException
     */
    public static Object parse(SimpleFeatureType schema, String input) throws ParseException {
    	ExpressionBuilder builder = new ExpressionBuilder();
    	return builder.parser( schema, input );
    }
    /**
     * Parse the input string into either a Filter or an Expression.
     */
    public Object parser(SimpleFeatureType schema, String input) throws ParseException {
        ExpressionCompiler c = new ExpressionCompiler( schema,input);
        try {
            c.CompilationUnit();
        } catch (TokenMgrError tme) {
            throw new ExpressionException(tme.getMessage(),c.getToken(0));
        }
        if (c.exception != null)
            throw c.exception;
        
        StackItem item = (StackItem) c.stack.peek();
        return item.built;
        
    }
    
    /**
     * Parse the input string into either a Filter or an Expression.
     * @deprecated Please make use of parser( input )
     */
    public static Object parse(String input) throws ParseException {
        return parse(null,input);        
    }
    /**
     * Parse the input string into either a Filter or an Expression.
     * <p>
     * Not schema is provided for reference during construction.
     * </p>
     * @throws ParseException 
     */
    public Object parser( String input ) throws ParseException{
    	return parser(null, input);
    }
    
    /**
     * Returns a formatted error string, showing the original input, along with
     * a pointer to the location of the error and the error message itself.
     */
    public static String getFormattedErrorMessage(ParseException pe,String input) {
        StringBuffer sb = new StringBuffer(input);
        sb.append('\n');
        Token t = pe.currentToken;
        while (t.next != null)
            t = t.next;
        int column = t.beginColumn - 1;
        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }
        sb.append('^').append('\n');
        sb.append(pe.getMessage());
        return sb.toString();
    }
    
    class ExpressionCompiler extends ExpressionParser implements ExpressionParserTreeConstants {
    	Stack stack = new Stack();
        ExpressionException exception = null;
        String input;
        SimpleFeatureType schema;
        WKTReader reader;
        ExpressionCompiler(SimpleFeatureType schema, String input) {
            super(new StringReader(input));
            this.input = input;
            this.schema = schema;
        }
        
        StackItem popStack() {
            return (StackItem) stack.pop();
        }
        
        org.opengis.filter.expression.Expression expression() throws ExpressionException {
            StackItem item = null;
            try {
                item = popStack();
                return (org.opengis.filter.expression.Expression) item.built;
            } catch (ClassCastException cce) {
                throw new ExpressionException("Expecting Expression, but found Filter",item.token);
            } catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack",getToken(0));
            }
        }
        
        org.opengis.filter.Filter filter() throws ExpressionException {
            StackItem item = null;
            try {
                item = popStack();
                return (org.opengis.filter.Filter) item.built;
            } catch (ClassCastException cce) {
                throw new ExpressionException("Expecting Filter, but found Expression",item.token);
            } catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack",getToken(0));
            }
        }
        
        double doubleValue() throws ExpressionException {
            try {
                return ((Number) expression().evaluate(null)).doubleValue();
            } catch (ClassCastException cce) {
                throw new ExpressionException("Expected double",getToken(0));
            }
        }
        
        int intValue() throws ExpressionException {
            try {
                return ((Number) expression().evaluate(null)).intValue();
            } catch (ClassCastException cce) {
                throw new ExpressionException("Expected int",getToken(0));
            }
        }
        
        String stringValue() throws ExpressionException {
            return expression().evaluate(null).toString();
        }
        
        public void jjtreeOpenNodeScope(Node n) {
        }
        
        public void jjtreeCloseNodeScope(Node n) throws ParseException {
            try {
                Object built = buildObject(n);
                if (built == null) throw new RuntimeException("INTERNAL ERROR : Node " + n + " resulted in null build");
                stack.push(new StackItem(built,getToken(0)));
            } finally {
                n.dispose();
            }
        }
        
        String token() {
            return getToken(0).image;
        }
        
        org.opengis.filter.expression.Expression mathExpression(Class type) throws ExpressionException {
            try {
                org.opengis.filter.expression.Expression right = expression();
                org.opengis.filter.expression.Expression left = expression();
                org.opengis.filter.expression.Expression e;
                if(Add.class == type){
                    e = factory.add(left, right);
                }else if(Subtract.class == type){
                    e = factory.subtract(left, right);
                }else if(Divide.class == type){
                    e = factory.divide(left, right);
                }else if(Multiply.class == type){
                    e = factory.multiply(left, right);
                }else{
                    throw new IllegalArgumentException();
                }
                return e;
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building MathExpression",getToken(0),ife);
            }
        }
        
        org.opengis.filter.Filter logicFilter(Class type) throws ExpressionException {
            try {
                org.opengis.filter.Filter right = filter();
                org.opengis.filter.Filter left = filter();
                if(Or.class == type){
                    return factory.or(left,right);
                }else if(And.class == type){
                    return factory.and(left, right);
                }
                throw new IllegalArgumentException();
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building LogicFilter",getToken(0),ife);
            }
        }
        
        org.opengis.filter.Filter compareFilter(Class type) throws ExpressionException {
            try {
                org.opengis.filter.expression.Expression right = expression();
                org.opengis.filter.expression.Expression left = expression();
                org.opengis.filter.Filter f;
                if (PropertyIsLessThanOrEqualTo.class == type) {
                    f = factory.lessOrEqual(left, right);
                } else if (PropertyIsLessThan.class == type) {
                    f = factory.less(left, right);
                } else if (PropertyIsGreaterThanOrEqualTo.class == type) {
                    f = factory.greaterOrEqual(left, right);
                } else if (PropertyIsGreaterThan.class == type) {
                    f = factory.greater(left, right);
                } else if (PropertyIsEqualTo.class == type) {
                    f = factory.equals(left, right);
                } else if (PropertyIsNotEqualTo.class == type) {
                    f = factory.notEqual(left, right, false);
                } else {
                    throw new IllegalArgumentException();
                }
                
                return f;
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building CompareFilter",getToken(0),ife);
            }
        }
        
        org.opengis.filter.Filter betweenFilter() throws ExpressionException {
            try {   
                org.opengis.filter.expression.Expression right = expression();
                org.opengis.filter.expression.Expression middle = expression();
                org.opengis.filter.expression.Expression left = expression();
                org.opengis.filter.Filter f = factory.between(middle, left, right);
                return f;
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building CompareFilter",getToken(0),ife);
            }
        }
        
        Object buildObject(Node n) throws ExpressionException {
            short type;
            switch (n.getType()) {
                
                // Literals
                // note, these should never throw because the parser grammar
                // constrains input before we ever reach here!
                case JJTINTEGERNODE:
                    return factory.literal(Integer.parseInt(token()));
                case JJTFLOATINGNODE:
                    return factory.literal(Double.parseDouble(token()));
                case JJTSTRINGNODE:
                    return factory.literal(n.getToken().image);
                case JJTATTNODE:
                    try {
                        String attName = token();
                        if(schema != null){
                            if(null == schema.getDescriptor(attName)){
                                throw new IllegalArgumentException(attName + " not found in schema");
                            }
                        }
                        return factory.property(attName);
                    } catch (IllegalFilterException ife) {
                        throw new ExpressionException("Exception building AttributeExpression",getToken(0),ife);
                    }
                case JJTFUNCTIONNODE:
                    return parseFunction(n);
                    
                    
                    // Math Nodes
                case JJTADDNODE:
                    return mathExpression(Add.class);
                case JJTSUBTRACTNODE:
                    return mathExpression(Subtract.class);
                case JJTMULNODE:
                    return mathExpression(Multiply.class);
                case JJTDIVNODE:
                    return mathExpression(Divide.class);
                    
                    
                    // Logic Nodes
                case JJTORNODE:
                    return logicFilter(Or.class);
                case JJTANDNODE:
                    return logicFilter(And.class);
                case JJTNOTNODE:
                    return factory.not(filter());
                    
                    // Between Node
                case JJTBETWEENNODE:
                    return betweenFilter();
                    
                    // Compare Nodes
                case JJTLENODE:
                    return compareFilter(PropertyIsLessThanOrEqualTo.class);
                case JJTLTNODE:
                    return compareFilter(PropertyIsLessThan.class);
                case JJTGENODE:
                    return compareFilter(PropertyIsGreaterThanOrEqualTo.class);
                case JJTGTNODE:
                    return compareFilter(PropertyIsGreaterThan.class);
                case JJTEQNODE:
                    return compareFilter(PropertyIsEqualTo.class);
                case JJTNENODE:
                    return compareFilter(PropertyIsNotEqualTo.class);
                    
                    
                    // Geometries:
                case JJTWKTNODE:
                    Token end = n.getToken();
                    while (true) {
                        if (end.next == null)
                            break;
                        end = end.next;
                    }
                    return geometry(n.getToken(),end);
                    
                    
                    // Unsupported (for now)
                case JJTTRUENODE:
                case JJTFALSENODE:
                    throw new ExpressionException("Unsupported syntax",getToken(0));
            }
            
            return null;
        }
        
        org.opengis.filter.expression.Literal geometry(Token start, Token end)
                throws ExpressionException {
            if (reader == null)
                reader = new WKTReader();
            try {
                Geometry g = reader.read(input.substring(start.beginColumn - 1,end.endColumn));
                return factory.literal(g);
            } catch (com.vividsolutions.jts.io.ParseException e) {
                throw new ExpressionException(e.getMessage(),start);
            } catch (Exception e) {
                throw new ExpressionException("Error building WKT Geometry",start,e);
            }
        }
        
        Object parseFunction(Node n) throws ExpressionException {
            String function = n.getToken().image;
            if ("box".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 4) {
                    throw new ExpressionException("Bounding Box filter requires 4 arguments",getToken(0));
                }
                
                double d4 = doubleValue();
                double d3 = doubleValue();
                double d2 = doubleValue();
                double d1 = doubleValue();
                try {
                    return new BBoxExpressionImpl(new Envelope(
                    d1,d2,d3,d4
                    ));
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BBoxExpression",getToken(0),ife);
                }
            } else if ("id".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("Feature ID filter requires 1 argument",getToken(0));
                }
                return factory.id(Collections.singleton(factory.featureId(stringValue())));
            } else if ("between".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 3) {
                    throw new ExpressionException("Between filter requires 3 arguments",getToken(0));
                }
                org.opengis.filter.expression.Expression two = expression();
                org.opengis.filter.expression.Expression att = expression();
                org.opengis.filter.expression.Expression one = expression();
                try {
                    PropertyIsBetween b = factory.between(att, one, two);
                    return b;
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BetweenFilter",getToken(0),ife);
                }
                
            } else if ("like".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 2) {
                    throw new ExpressionException("Like filter requires at least two arguments",getToken(0));
                }
                String pattern = stringValue();
                Expression expr = expression();
                try {
                    PropertyIsLike f = factory.like(expr, pattern, "*", ".?", "//");
                    return f;
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building LikeFilter", getToken(0), ife);
                }
            } else if ("null".equalsIgnoreCase(function) || "isNull".equalsIgnoreCase(function)) {
                Expression e = expression();
                
                try {
                    if (e instanceof org.opengis.filter.expression.Literal) {
                        e = factory.property(((org.opengis.filter.expression.Literal) e).evaluate(
                                null).toString());
                    }
                    org.opengis.filter.Filter nf = factory.isNull(e);
                    return nf;
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building NullFilter",getToken(0),ife);
                }
            }
            
            if (GEOMETRY_FILTERS.contains(function.toUpperCase()))
                return buildGeometryFilter(function);
            
            //GR: GEOT-1192, don't know how to fetch the number of arguments
            //before creating the function
            FilterFactory f = (FilterFactory) factory;
            FunctionExpression func = f.createFunctionExpression(function);
            if (func == null) throw new ExpressionException("Could not build function : " + function,getToken(0));

            int nArgs = func.getArgCount();
            if (n.jjtGetNumChildren() != nArgs) {
                throw new ExpressionException(function + " function requires " + nArgs + " arguments",getToken(0));
            }

            Expression[] args = new Expression[func.getArgCount()];
            for (int i = 0; i < args.length; i++) {
                args[i] = expression();
            }
            
            func.setParameters(Arrays.asList(args));
            return func; 
            
        }
        
        SpatialOperator buildGeometryFilter(String type) throws ExpressionException {
            Expression right = expression();
            Expression left = expression();
            if(type.equalsIgnoreCase("EQUALS"))
                return factory.equal(right, left);
            else if(type.equalsIgnoreCase("DISJOINT"))
                return factory.disjoint(right, left);
            else if(type.equalsIgnoreCase("INTERSECTS"))
                return factory.intersects(right, left);
            else if(type.equalsIgnoreCase("TOUCHES"))
                return factory.touches(right, left);
            else if(type.equalsIgnoreCase("CROSSES"))
                return factory.crosses(right, left);
            else if(type.equalsIgnoreCase("WITHIN"))
                return factory.within(right, left);
            else if(type.equalsIgnoreCase("CONTAINS"))
                return factory.contains(right, left);
            else if(type.equalsIgnoreCase("OVERLAPS"))
                return factory.overlaps(right, left);
            else
                throw new ExpressionException("Exception building GeometryFilter",getToken(0));
        }
    }
    
    static class StackItem {
        Object built;
        Token token;
        StackItem(Object b,Token t) {
            built = b;
            token = t;
        }
    }
    
    public static final void main(String[] args) throws Exception {
        System.out.println("Expression Tester");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        FilterTransformer t = new FilterTransformer();
        t.setIndentation(4);
        while (true) {
            System.out.print("> ");
            String line = r.readLine();
            if (line.equals("quit"))
                break;
            try {
                Object b = ExpressionBuilder.parse(line);
                t.transform(b, System.out);
                System.out.println();
            } catch (ParseException pe) {
                System.out.println(ExpressionBuilder.getFormattedErrorMessage(pe, line));
            }
        } 
    }
}
