/*
 *    GeoTools - The Open Source Java GIS Tookit
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
package org.geotools.maven.taglet;

import java.util.regex.Matcher;
import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests the {@link Source} taglet.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/build/maven/javadoc/src/test/java/org/geotools/maven/taglet/SourceTest.java $
 * @version $Id: SourceTest.java 30766 2008-06-19 13:24:46Z acuster $
 * @author Martin Desruisseaux
 */
public class SourceTest {
    /**
     * Tests the regular expression validity using the tag for this source file.
     */
    @Test
    public void testCurrentTag() {
        Source  s = new Source();
        Matcher m;
        String tag, url, group, category, module;
        tag = "$URL: http://gtsvn.refractions.net/trunk/build/maven/javadoc/src/test/java/org/geotools/maven/taglet/SourceTest.java $";
        //The url above is only converted from $URL: http://gtsvn.refractions.net/trunk/build/maven/javadoc/src/test/java/org/geotools/maven/taglet/SourceTest.java $ if we have obtained the 
        //  file using a standard access mechanism to SVN. This fails, for 
        //  example, with mercurial converstion 'hg convert svnrepo hgrepo'
        if ( !tag.equals("$URL: http://gtsvn.refractions.net/trunk/build/maven/javadoc/src/test/java/org/geotools/maven/taglet/SourceTest.java $") ){
            m = s.findURL.matcher(tag);
            assertTrue(m.matches());

            // Try to match the URL provided by SVN.
            url = m.group(1).trim();
            m = s.findModule.matcher(url);
            assertTrue(m.matches());
            group    = m.group(1);
            category = m.group(2);
            module   = m.group(3);
            assertEquals("build", group);
            assertEquals("maven", category);
            assertEquals("javadoc", module);
        }

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/tags/2.4-M0/modules/library/api/src/main/java/org/geotools/catalog/ResolveChangeListener.java";
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        group    = m.group(1);
        category = m.group(2);
        module   = m.group(3);
        assertEquals("modules", group);
        assertEquals("library", category);
        assertEquals("api", module);

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/tags/2.2-RC4/modules/library/referencing/src/main/java/org/geotools/referencing/CRS.java";
        tag = Source.SVN_KEYWORD_DELIMITER + "URL: " + url + ' ' + Source.SVN_KEYWORD_DELIMITER;
        m = s.findURL.matcher(tag);
        assertTrue(m.matches());
        assertEquals(url, m.group(1).trim());
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        group    = m.group(1);
        category = m.group(2);
        module   = m.group(3);
        assertEquals("modules", group);
        assertEquals("library", category);
        assertEquals("referencing", module);
    }
}
