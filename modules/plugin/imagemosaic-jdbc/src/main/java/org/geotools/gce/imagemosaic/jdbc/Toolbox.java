/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;

/**
 * This class is a dispatcher class for utility classes. This class is also the
 * main class in the produced jar file.
 * 
 * 1) Import 2) DDL Generator
 * 
 * @author mcr
 * 
 */
class Toolbox {
	/**
	 * read args and delegate job to the corresponding utility class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Missing cmd import | ddl");
			System.exit(1);
		}

		String[] newArgs = new String[args.length - 1];

		for (int i = 0; i < newArgs.length; i++)
			newArgs[i] = args[i + 1];

		if ("import".equalsIgnoreCase(args[0])) {
			Import.start(newArgs);
		} else if ("ddl".equalsIgnoreCase(args[0])) {
			DDLGenerator.start(newArgs);
		} else {
			System.out.println("Unknwon cmd: " + args[0]);
			System.exit(1);
		}
	}

	public static URL getURLFromString(String source) {
		if (source == null) {
			return null;
		}

		URL sourceURL = null;
		File f = new File(source);
		try {
			if (f.exists()) {
				return f.toURI().toURL();
			}
			sourceURL = new URL(URLDecoder.decode((String) source,"UTF8"));
		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		return sourceURL;
	}

}
