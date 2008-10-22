/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.repository.defaults;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;


/**
 * DOCUMENT ME!
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/ASTFactory.java $
 */
public class ASTFactory {
    private ASTFactory() { /* not used */
    }

    /**
     * Creates an AST for the pattern The pattern uses the following
     * conventions: use " " to surround a phase use + to represent 'AND' use -
     * to represent 'OR' use ! to represent 'NOT' use ( ) to designate scope
     *
     * @param str Search pattern
     *
     * @return AST
     */
    public static AST parse(String str) {
        List tokens = tokenize(str);
        Stack s = new Stack();
        ListIterator li = tokens.listIterator();

        // create the ast
        while (li.hasNext())
            node(s, li);

        if (s.size() == 1) {
            return (AST) s.pop();
        }

        if (s.size() > 1) {
            // assume they are anded ... TODO balance the tree
            while (s.size() > 1) {
                AST p1 = (AST) s.pop();
                AST p2 = (AST) s.pop();
                s.push(new And(p1, p2));
            }

            return (AST) s.pop();
        }

        System.err.println("An internal error creating an AST may have occured"); //$NON-NLS-1$

        return null;
    }

    protected static List tokenize(String pattern) {
        List l = new LinkedList();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            switch (c) {
            case '(':
                l.add("("); //$NON-NLS-1$

                break;

            case ')':
                l.add(")"); //$NON-NLS-1$

                break;

            case '+':
                l.add("+"); //$NON-NLS-1$

                break;

            case '-':
                l.add("-"); //$NON-NLS-1$

                break;

            case '!':
                l.add("!"); //$NON-NLS-1$

                break;

            case '"':

                // greedy grab
                int j = pattern.indexOf('"', i + 1);
                l.add(pattern.substring(i + 1, j));
                i = j;

                break;

            case ' ':
            case '\t':
            case '\n':

                // skip
                break;

            case 'A': // ND

                if ((pattern.charAt(i + 1) == 'N')
                        && (pattern.charAt(i + 2) == 'D')) {
                    // it's a +
                    l.add("+"); //$NON-NLS-1$
                    i += 2;
                }

                break;

            case 'O': // R

                if (pattern.charAt(i + 1) == 'R') {
                    // it's a +
                    l.add("-"); //$NON-NLS-1$
                    i += 1;
                }

                break;

            case 'N': // OT

                if ((pattern.charAt(i + 1) == 'O')
                        && (pattern.charAt(i + 2) == 'T')) {
                    // it's a +
                    l.add("!"); //$NON-NLS-1$
                    i += 2;
                }

            default:
                // greedy grab
                j = i + 1;

                while ((j < pattern.length()) && (pattern.charAt(j) != '"')
                        && (pattern.charAt(j) != '+')
                        && (pattern.charAt(j) != '-')
                        && (pattern.charAt(j) != '!')
                        && (pattern.charAt(j) != '(')
                        && (pattern.charAt(j) != ')')
                        && (pattern.charAt(j) != ' ')
                        && (pattern.charAt(j) != '\t')
                        && (pattern.charAt(j) != '\n'))
                    j++;

                l.add(pattern.substring(i, j));
                i = ((i == j) ? j : (j - 1));
            }
        }

        return l;
    }

    private static void node(Stack s, ListIterator li) {
        if (li.hasNext()) {
            String token = (String) li.next();

            char c = token.charAt(0);

            switch (c) {
            case '(':
                // child is what we want
                node(s, li);

                break;

            case ')':

                // ignore this
                break;

            case '+':

                AST prev = (AST) s.pop();
                node(s, li);

                AST next = (AST) s.pop();
                s.push(new And(prev, next));

                break;

            case '-':
                prev = (AST) s.pop();
                node(s, li);
                next = (AST) s.pop();
                s.push(new Or(prev, next));

                break;

            case '!':
                node(s, li);
                next = (AST) s.pop();
                s.push(new Not(next));

                break;

            default:
                s.push(new Literal(token));
            }
        }
    }

    private static class And implements AST {
        private AST left;
        private AST right;

        private And() { /* should not be used */
        }

        public And(AST left, AST right) {
            this.left = left;
            this.right = right;
        }

        /**
         * TODO summary sentence for accept ...
         *
         * @param datum
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#accept(java.lang.String)
         */
        public boolean accept(String datum) {
            return (left != null) && (right != null) && left.accept(datum)
            && right.accept(datum);
        }

        /**
         * TODO summary sentence for type ...
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#type()
         */
        public int type() {
            return AND;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getLeft()
         */
        public AST getLeft() {
            return left;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getRight()
         */
        public AST getRight() {
            return right;
        }
    }

    private static class Or implements AST {
        private AST left;
        private AST right;

        private Or() { /* should not be used */
        }

        public Or(AST left, AST right) {
            this.left = left;
            this.right = right;
        }

        /**
         * TODO summary sentence for accept ...
         *
         * @param datum
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#accept(java.lang.String)
         */
        public boolean accept(String datum) {
            return ((right != null) && right.accept(datum))
            || ((left != null) && left.accept(datum));
        }

        /**
         * TODO summary sentence for type ...
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#type()
         */
        public int type() {
            return OR;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getLeft()
         */
        public AST getLeft() {
            return left;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getRight()
         */
        public AST getRight() {
            return right;
        }
    }

    private static class Not implements AST {
        private AST child;

        private Not() { /* should not be used */
        }

        public Not(AST child) {
            this.child = child;
        }

        /**
         * TODO summary sentence for accept ...
         *
         * @param datum
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#accept(java.lang.String)
         */
        public boolean accept(String datum) {
            return !((child != null) && child.accept(datum));
        }

        /**
         * TODO summary sentence for type ...
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#type()
         */
        public int type() {
            return NOT;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getLeft()
         */
        public AST getLeft() {
            return child;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getRight()
         */
        public AST getRight() {
            return null;
        }
    }

    private static class Literal implements AST {
        private String value;

        private Literal() { /* should not be used */
        }

        public Literal(String value) {
            this.value = value;
        }

        /**
         * TODO summary sentence for accept ...
         *
         * @param datum
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#accept(java.lang.String)
         */
        public boolean accept(String datum) {
            // TODO check this
            return (value != null) && (datum != null)
            && (datum.toUpperCase().indexOf(value.toUpperCase()) > -1);
        }

        /**
         * TODO summary sentence for type ...
         *
         *
         * @see net.refractions.udig.catalog.internal.DefaultCatalog.AST#type()
         */
        public int type() {
            return LITERAL;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getLeft()
         */
        public AST getLeft() {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.refractions.udig.catalog.util.AST#getRight()
         */
        public AST getRight() {
            return null;
        }

        public String toString() {
            return value;
        }
    }
}
