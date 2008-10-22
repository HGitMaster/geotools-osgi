/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.maven;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Iterator;

// Maven and Plexus dependencies
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.DefaultConsumer;


// Note: javadoc in class and fields descriptions must be XHTML.
/**
 * Invokes the RMI compiler (<code>rmic</code>).
 * 
 * @goal rmic
 * @phase compile
 * @description Invokes the RMI compiler (rmic).
 * @source $URL: http://gtsvn.refractions.net/trunk/build/maven/rmic/src/main/java/org/geotools/maven/RMICompiler.java $
 * @version $Id: RMICompiler.java 30567 2008-06-08 11:26:56Z acuster $
 * @author Martin Desruisseaux
 */
public class RMICompiler extends AbstractMojo {
    /**
     * Directory where the output class files will be located.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Comma-separated list of classes to compile.
     *
     * @parameter
     * @required
     */
    private String includes;

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Invokes the RMI compiler.
     *
     * @throws MojoExecutionException if the plugin execution failed.
     */
    public void execute() throws MojoExecutionException {
        /*
         * Prepares the command line instruction using the Plexus utilities.
         * First, we set the executable (full path to rmic.exe).
         */
        final Commandline cmd = new Commandline();
        String command = "/../bin/rmic";
        if (SystemUtils.IS_OS_WINDOWS) {
            command += ".exe";
        }
        final File executable = new File(SystemUtils.getJavaHome(), command);
        cmd.setExecutable(executable.getAbsolutePath());
        cmd.setWorkingDirectory(new File(outputDirectory).getAbsolutePath());
        /*
         * Sets the arguments: the classpath, the output directory and the list
         * if all class files to compile.
         */
        final Collection pathElements;
        try {
            pathElements = project.getCompileClasspathElements();
        }
        catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Unresolved compile dependencies", e );
        }
        final String   pathSeparator = System.getProperty("path.separator");
        final StringBuffer classpath = new StringBuffer();
        for (final Iterator it=pathElements.iterator(); it.hasNext();) {
            if (classpath.length() != 0) {
                classpath.append(pathSeparator);
            }
            classpath.append(it.next());
        }
        if (classpath.length() != 0) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setValue(classpath.toString());
        }
        int n = 0;
        if (includes != null) {
            final StringTokenizer items = new StringTokenizer(includes, ",");
            while (items.hasMoreTokens()) {
                cmd.createArgument().setValue(items.nextToken().trim());
                n++;
            }
        }
        /*
         * Run the command.
         */
        final int exitCode;
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        try {
            exitCode = CommandLineUtils.executeCommandLine(cmd, new DefaultConsumer(), err);
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Unable to execute rmic command", e);
        }
        if (exitCode != 0) {
            throw new MojoExecutionException("Exit code: " + exitCode + " - " + err.getOutput());
        }
        getLog().info("Compiled " + n + " RMI classes.");
    }
}
