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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Compacts {@code INSERT TO ...} SQL statements. This program doesn't need to be included
 * in the Geotools JAR. It is run only once when a new SQL script is created. See
 * <A HREF="doc-files/HSQL.html">Creating EPSG database for HSQL</A>.
 *
 * @since 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/epsg-hsql/src/main/java/org/geotools/referencing/factory/epsg/Compactor.java $
 * @version $Id: Compactor.java 30656 2008-06-12 20:32:50Z acuster $
 * @author Martin Desruisseaux
 *
 * @todo Find some way to exclude this file from JAR during Maven build.
 */
final class Compactor {
    /**
     * No instantiation allowed.
     */
    private Compactor() {
    }

    /**
     * Run from the command line.
     */
    public static void main(final String[] args) throws IOException {
        final BufferedReader in    = new BufferedReader(new FileReader("EPSG.sql"));
        final BufferedWriter out   = new BufferedWriter(new FileWriter("EPSG-compact.sql"));
        final String lineSeparator = System.getProperty("line.separator", "\n");
        boolean insertDatum        = false;
        String insertStatement     = null;
        String line;
        while ((line=in.readLine()) != null) {
            if ((line=line.trim()).length() == 0) {
                // Skip blank lines.
                continue;
            }
            if (insertStatement != null) {
                if (line.startsWith(insertStatement)) {
                    // The previous instruction was already an INSERT INTO the same table.
                    line = line.substring(insertStatement.length()).trim();
                    line = removeUselessExponents(line);
                    if (insertDatum) {
                        line = removeRealizationEpochQuotes(line);
                    }
                    out.write(',');
                    out.write(lineSeparator);
                    out.write(line);
                    continue;
                }
                // Previous instruction was the last INSERT INTO for a given table.
                // We now have a new instruction. Append the pending cariage return.
                out.write(lineSeparator);
            }
            if (line.startsWith("INSERT INTO")) {
                insertDatum = line.startsWith("INSERT INTO EPSG_DATUM VALUES");
                int values = line.indexOf("VALUES", 11);
                if (values >= 0) {
                    // We are begining inserations in a new table.
                    values += 6; // Move to the end of "VALUES".
                    insertStatement = line.substring(0, values).trim();
                    line = line.substring(insertStatement.length()).trim();
                    line = removeUselessExponents(line);
                    if (insertDatum) {
                        line = removeRealizationEpochQuotes(line);
                    }
                    out.write(insertStatement);
                    out.write(lineSeparator);
                    out.write(line);
                    continue;
                }
            }
            insertStatement = null;
            out.write(line);
            out.write(lineSeparator);
        }
        out.close();
        in.close();
    }

    /**
     * For private usage by the following method only.
     */
    private static final Pattern uselessExponentPattern =
            Pattern.compile("([\\(\\,]\\-?\\d+\\.\\d+)E[\\+\\-]?0+([\\,\\)])");

    /**
     * Removes the useless "E0" exponents after floating point numbers.
     */
    private static String removeUselessExponents(String line) {
        StringBuilder cleaned = null;
        final Matcher matcher = uselessExponentPattern.matcher(line);
        while (true) {
            int lastIndex = 0;
            while (matcher.find()) {
                // Make sure this is not a quoted text.
                boolean quoted = false;
                for (int i=matcher.start(); (i=line.lastIndexOf('\'', i-1)) >= 0;) {
                    if (i==0 || line.charAt(i-1)!='\\') {
                        quoted = !quoted;
                    }
                }
                if (!quoted) {
                    // Found a number outside quotes. Replace.
                    if (cleaned == null) {
                        cleaned = new StringBuilder();
                    }
                    cleaned.append(line.substring(lastIndex, matcher.end(1)));
                    lastIndex = matcher.end();
                    cleaned.append(line.substring(matcher.start(2), lastIndex));
                }
            }
            if (lastIndex == 0) {
                return line;
            }
            cleaned.append(line.substring(lastIndex));
            line = cleaned.toString();
            matcher.reset(line);
            cleaned.setLength(0);
        }            
    }

    /**
     * Remove the quotes in REALIZATION_EPOCH column (i.e. change the type
     * from TEXT to INTEGER). This is the 5th column.
     */
    private static String removeRealizationEpochQuotes(final String line) {
        int index = getIndexForColumn(line, 5);
        if (line.charAt(index) != '\'') {
            return line;
        }
        final StringBuilder cleaned = new StringBuilder(line.substring(0, index));
        if (line.charAt(++index) == '\'') {
            cleaned.append("NULL");
        } else do {
            cleaned.append(line.charAt(index));
        }
        while (line.charAt(++index) != '\'');
        cleaned.append(line.substring(index+1));
        return cleaned.toString();
    }

    /**
     * Returns the start index for the given column in the specified {@code VALUES} string.
     * Column numbers start at 1.
     */
    private static int getIndexForColumn(final String line, int column) {
        if (--column == 0) {
            return 0;
        }
        boolean quote = false;
        final int length = line.length();
        for (int index=0; index<length; index++) {
            switch (line.charAt(index)) {
                case '\'': if (index==0 || line.charAt(index-1)!='\\') quote = !quote;
                           break;
                case  ',': if (!quote && --column==0) return index+1;
                           break;
            }
        }
        return length;
    }
}
