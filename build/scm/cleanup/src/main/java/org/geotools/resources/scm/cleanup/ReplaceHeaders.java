/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources.scm.cleanup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.console.CommandLine;
import org.geotools.console.Option;


/**
 * <p>
 * Alters a file or a whole directory, replacing the headers to a new format.
 * This code was written in 2008 to change the copyright in the headers from
 * being held by the "Geotools PMC" to being held by "OSGeo" but this code
 * should be reusable in other situations by redefining the String constants.
 * </p><p>
 * Depending on the file contents, a file can have different status values:
 * <ul>
 *   <li>Skipped : if no changes are done on this file which could happen if the
 *                 file does not contain any header, or if the file is already
 *                 well formated.</li>
 *   <li>Suspicious : if too many changes are done on this file, it is flagged
 *                    as suspicious but changed: users should manually inspect
 *                    the files with 'svn diff'.</li>
 *   <li>Copyright problems : if a copyright line is found which is not present
 *                            in the list of known copyrights the file is
 *                            flagged and not changed.</li>
 *   <li>Changed correctly : if all changes seem correct.</li>
 * </ul>
 * </p><p>
 * HOW TO USE THIS FILE:
 * <ol>
 *   <li>{@code cd root_of_checkout} (trunk/)</li>
 *   <li>{@code mvn clean install} (compile)</li>
 *   <li>{@code cp build/scm/cleanup/target/cleanup-2.6-SNAPSHOT.jar target/binaries/.}</li>
 *   <li>{@code java -jar target/binaries/cleanup-2.6-SNAPSHOT.jar -info -input "path/to/dir"}
 *        where the path must be in quotes. For example, use "modules/library/metadata"</li>
 * </ol>
 * </p><p>
 * The options to run are:<br/>
 * REQUIRED:
 * <ul>
 *   <li>{@code --input "dir-or-file"} The quoted path to the input directory: <b>WARNING</b>
 *                                     used on its own, this will clobber files in place</li>
 * </ul>
 * OPTIONAL:
 * <ul>
 *   <li>{@code --help}   --- gives usage</li>
 *   <li>{@code --write}  --- runs in writing mode if present. If not specified, runs in
 *                            information mode ; will not write any file (by default)</li>
 *   <li>{@code --output "path/to/existing/folder-or-file"} will recreate a file tree of
 *                            modified files in the quoted directory. If not present, input
 *                            files will be overwritten.</li>
 *   <li>{@code --insertSpacerLine}  --- adds a line above the first (C) line written.
 *                                       <b>WARNING</b> This will change all files.</li>
 * </ul>
 * </p><p>
 * NOTE: the file can be run several times on the same input without problems.
 * </p>
 *
 * @version $Id: ReplaceHeaders.java 31093 2008-07-28 23:06:31Z groldan $
 * @author Cédric Briançon
 */
public final class ReplaceHeaders extends CommandLine {

    private final static Logger LOGGER = Logger.getLogger("org.geotools.resources.scm.cleanup");

    private final String FIRST_LINE       = "GeoTools - The Open Source Java GIS Toolkit";
    private final String OSGEO            = "Open Source Geospatial Foundation (OSGeo)";

    private static final int CURRENT_YEAR = 2008;

    /**
     * Files that do not contain any change. They are already correct.
     */
    private final List<String> unchangedFiles = new ArrayList<String>();

    /**
     * Files that do not contain header. They should be handled by hand.
     */
    private final List<String> noHeaderFiles = new ArrayList<String>();

    /**
     * Files that have a header which does not match with the one we will get at the end.
     */
    private final List<String> wrongHeaderFiles = new ArrayList<String>();

    /**
     * Files which need to be verified, because they contain lots of changed.
     */
    private final List<String> suspiciousFiles = new ArrayList<String>();

    /**
     * Files which have copyright problems. This could happen to a file that has a
     * copyright not listed in the {@link #COPYRIGHTS_FOR_MARTIN} map.
     */
    private final List<String> copyrightProblemsFiles = new ArrayList<String>();

    /**
     * Files which are correctly changed by this process.
     */
    private final List<String> correctlyChangedFiles = new ArrayList<String>();

    /**
     * A set of the first line to consider in the old version of the Geotools headers.
     */
    private static final Set<String> GEOTOOLS_OLD = new HashSet<String>();
    static {
        GEOTOOLS_OLD.add("GeoTools - OpenSource mapping toolkit");
        GEOTOOLS_OLD.add("Geotools2 - OpenSource mapping toolkit");
        GEOTOOLS_OLD.add("GeoTools2 - OpenSource mapping toolkit");
        GEOTOOLS_OLD.add("GeoTools 2 - OpenSource mapping toolkit");
        GEOTOOLS_OLD.add("GeoTools - The Open Source Java GIS Tookit");
    }

    /**
     * A set of copyrights that the process can suppress, because we know what to do
     * with them, and they can be suppressed.
     */
    private static final Set<String> RECOGNIZED_COPYRIGHTS = new HashSet<String>();
    static {
        RECOGNIZED_COPYRIGHTS.add("Geotools Project Manag");
        RECOGNIZED_COPYRIGHTS.add("GeoTools Project Manag");
        RECOGNIZED_COPYRIGHTS.add("Computational Geography");
        RECOGNIZED_COPYRIGHTS.add("Pêches et Océans Canada");
        RECOGNIZED_COPYRIGHTS.add("Fisheries and Oceans Canada");
        RECOGNIZED_COPYRIGHTS.add("Institut de Recherche pour le Développement");
        RECOGNIZED_COPYRIGHTS.add("Geomatys");
        RECOGNIZED_COPYRIGHTS.add("Open Source Geospatial Foundation (OSGeo)");
        RECOGNIZED_COPYRIGHTS.add("Refractions");
        RECOGNIZED_COPYRIGHTS.add("GeoSolutions");
        RECOGNIZED_COPYRIGHTS.add("Vision for New York");
        RECOGNIZED_COPYRIGHTS.add("Frank Warmerdam");
        RECOGNIZED_COPYRIGHTS.add("CSIRO");
        RECOGNIZED_COPYRIGHTS.add("Axios");
        RECOGNIZED_COPYRIGHTS.add("IVU");
        RECOGNIZED_COPYRIGHTS.add("Lisasoft");
        RECOGNIZED_COPYRIGHTS.add("TOPP");
        /*RECOGNIZED_COPYRIGHTS.add("Gerald Evenden");*/
    }

    /**
     * Number of files that have some changes in their headers.
     */
    private int numFilesChanged = 0;

    /**
     * Command-line option for the input file or directory to manage.
     */
    @Option(description="Input file or directory.", mandatory=true)
    private String input;

    /**
     * Command-line option for the output file or directory.
     */
    @Option(description="Output file or directory.")
    private String output;

    /**
     * Command-line option to define whether the script is launched in information mode
     * (read-only) or in writing mode.
     */
    @Option(description="Writing mode if present, otherwise read-only mode (just displays information)")
    private boolean write;

    /**
     * Command-line option for the output file or directory.
     */
    @Option(description="Insert a spacer line in the header between the url and the copyright.")
    private boolean insertSpacerLine;


    /**
     * Replace existing header of the Geotools source code with the {@code OSGeo}
     * copyright.
     *
     * @param args The command line arguments. Should contain a {@code -input} value, at
     *             least.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    public ReplaceHeaders(final String[] args) throws FileNotFoundException, IOException {
        super(args);
        final File in = new File(input);
        if (!in.exists()) {
            throw new FileNotFoundException("Input file does not exists.");
        }
        if (!write) {
            System.out.println("xxxxxx  INFORMATION MODE  xxxxxx");
            System.out.println(" /!\\ Nothing will be written /!\\ \n");
        } else {
            System.out.println("xxxxxx  WRITING MODE  xxxxxx\n");
            System.out.println("File(s) changed :");
        }
        if (in.isDirectory()) {
            final FileFilter javaFilter = new FileFilter() {
                public boolean accept(File arg0) {
                    return arg0.getName().endsWith(".java") ||
                           arg0.isDirectory();
                }
            };
            browseFiles(in, javaFilter);
        } else {
            fixHeaders(in, (output == null) ? in : new File(output));
        }
        writeSummary();
        writeVerification();
    }

    /**
     * Recursively browse all files in a directory, and launch the
     * {@link #fixHeaders(java.io.File, java.io.File)} process.
     *
     * @param root Root directory where to begin the process.
     * @param filter A filter to extract only the chosen files. Here selects only
     *               java files.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    private void browseFiles(final File root, final FileFilter filter)
            throws FileNotFoundException, IOException
    {
        for (final File candidate : root.listFiles(filter)) {
            if (candidate.isDirectory()) {
                browseFiles(candidate, filter);
            } else {
                fixHeaders(candidate,
                    (output == null) ? candidate : new File(output));
            }
        }
    }

    /**
     * Corrects the header of an input file, and returns the output file that will contain
     * the result.
     *
     * @param input  The input file to read.
     * @param output The output file where to write the result, or {@code null} to
     *               overwrite same files.
     * @throws FileNotFoundException if the input/output files do not exist.
     * @throws IOException
     */
    private void fixHeaders(final File input, final File output) throws FileNotFoundException,
            IOException
    {
        final InputStreamReader inputStream = new InputStreamReader(new FileInputStream(input));
        final BufferedReader reader         = new BufferedReader(inputStream);
        final StringBuilder textIn          = new StringBuilder();
        final StringBuilder textOut         = new StringBuilder();
        // Fix a default initial value, which should be greater than copyright dates found in
        // the java class header. The current year is then chosen.
        int startCopyright = CURRENT_YEAR;
        int linesDeleted = 0, linesChanged = 0, linesWithCopyright = 0, numOldFirstLine = 0;
        try {
            String line;
            // Defines whether the (c) value has been found
            boolean hasCopyright = false;
            final Map<String,Integer> copyrightsRead = new HashMap<String,Integer>();
            final Set<String> unknowCopyrights = new HashSet<String>();



            /* *****************************************************************
             * Reading part
             */
readLine:   while ((line = reader.readLine()) != null) {
                textIn.append(line).append("\n");
                for (final String oldLine : GEOTOOLS_OLD) {
                    if (line.contains(oldLine)) {
                        linesChanged++;
                        numOldFirstLine++;
                        textOut.append(line.replaceAll(oldLine, FIRST_LINE)).append("\n");
                        continue readLine;
                    }
                }
                if (line.contains("License as published by the Free Software Foundation; either")) {
                    linesChanged++;
                    textOut.append(line.replaceAll("License as published by the Free Software Foundation; either",
                            "License as published by the Free Software Foundation;")).append("\n");
                    continue;
                }
                if (line.contains("version 2.1 of the License, or (at your option) any later version.")) {
                    linesChanged++;
                    textOut.append(line.replaceAll("version 2\\.1 of the License, or \\(at your option\\) any later version",
                            "version 2\\.1 of the License")).append("\n");
                    continue;
                }
                // Lines like " *    (C) 2005"
                if (line.matches("(\\s)+\\*(\\s)+\\([cC]\\)(\\s)+[0-9]{4}(.)*")) {
                    final String copyrightName = getCopyrightFullText(line);
                    if ( !isCopyrightKnown(copyrightName) ) {
                        textOut.append(line).append("\n");
                        unknowCopyrights.add(copyrightName);
                    }
                    linesWithCopyright++;
                    hasCopyright = true;
                    copyrightsRead.put(copyrightName, getCopyrigthStartTime(line));
                    continue;
                }
                //We have processed all the copyright lines
                if (hasCopyright) {
                    //Toggle if we add a line to the header above the copyright.
                    if (insertSpacerLine) {
                        textOut.append(" *\n");
                        linesChanged++;
                    }
                    textOut.append(" *    (C) ");
                    startCopyright = Collections.min(copyrightsRead.values());
                    if (startCopyright < CURRENT_YEAR) {
                        textOut.append(startCopyright).append("-");
                    }
                    textOut.append(CURRENT_YEAR).append(", ").append(OSGEO).append("\n");
                    hasCopyright = false;
                    linesDeleted = linesWithCopyright - 1;
                    linesChanged++;
                }

                //We did not match any cases so we simply write the line as it was
                textOut.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            /* *****************************************************************
             * Analysis part
             * @todo: add default header if no header found.
             */
            final String filePath = input.getAbsolutePath();
            // Verify if input and output are the same, which means if no change has been applied.
            // This could happen for two cases :
            //   - The file does not contain any header => nothing is done
            //   - The file already has a good copyright header.
            if (textIn.toString().contentEquals(textOut.toString())) {
                if (copyrightsRead.size() == 0) {
                    noHeaderFiles.add(filePath);
                    return;
                } else {
                    unchangedFiles.add(filePath);
                }
            } else {
                // Specify the status of the current file by putting it in the matching list.
                numFilesChanged++;
                if (!unknowCopyrights.isEmpty()) {
                    if (write) {
                        System.out.println("/!\\ Copyright problems /!\\ ==> " + filePath);
                        System.out.println("\t\t\t\t|__\tLines deleted : " + linesDeleted +
                                "\tLines changed : " + linesChanged);
                    }
                    for (String unknownCopyright : unknowCopyrights) {
                        copyrightProblemsFiles.add(filePath);
                        if (write) {
                            System.out.println("\t\t\t\tUnknown copyright \"" + unknownCopyright +
                                    "\". You should handle it by hand.");
                        }
                    }
                }
                // If too many changes are done on a file, it is considered as suspect, which means the user
                // should have a look to this file to verify that all proposal changes are rigth.
                if (linesChanged > 5 || linesDeleted > 2 || numOldFirstLine > 1) {
                    if (write) {
                        System.out.println("???     Suspicious     ??? ==> " + filePath);
                        System.out.println("\t\t\t\t|__\tLines deleted : " + linesDeleted +
                                "\tLines changed : " + linesChanged);
                    }
                    suspiciousFiles.add(filePath);
                } else {
                    if (write) {
                        System.out.println("||| Changed correctly  ||| ==> " + filePath +
                                "\tLines deleted : " + linesDeleted + "\tLines changed : " + linesChanged);
                    }
                    correctlyChangedFiles.add(filePath);
                }

                /* *****************************************************************
                 * Writing part (only if the script is launched in the writing mode
                 * and the output contains some changes).
                 */
                if (write) {
                    final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(output));
                    final BufferedWriter buffer = new BufferedWriter(writer);
                    buffer.append(textOut.toString());
                    buffer.close();
                }
            }

            /* *****************************************************************
             * Verification part
             */
            if (!matchHeader(textOut.toString())) {
                wrongHeaderFiles.add(filePath);
            }
        } finally {
            reader.close();
            inputStream.close();
        }
    }

    /**
     * Returns the full text of a line containing a copyright, without the dates
     * information, or {@code null} if the line specified does not contain any date
     * (and so is not a copyright line).
     *
     * @param line A line which contains a copyright.
     */
    private static String getCopyrightFullText(final String line) {
        final String[] lineSplittedOnNumbers = line.split("[0-9]{4}(,)?");
        return (lineSplittedOnNumbers.length > 0) ?
            lineSplittedOnNumbers[lineSplittedOnNumbers.length - 1].trim() :
            null;
    }

    /**
     * Returns the start time of a copyright contained in a line, or {@code -1} if
     * the line given does not contain any copyright information.
     *
     * @param line A line which contains a copyright.
     */
    private static int getCopyrigthStartTime(final String line) {
        final String[] lineSplittedOnSpace = line.split("\\W");
        int i = 0;
        while (i < lineSplittedOnSpace.length && !lineSplittedOnSpace[i].matches("[0-9]{4}")) {
            i++;
        }
        if (i == lineSplittedOnSpace.length) {
            // Should never happened. Copyrights always have date information.
            return -1;
        }
        return Integer.valueOf(lineSplittedOnSpace[i]);
    }

    /**
     * Returns {@code true} if the copyright in parameter is present in the list of
     * known copyrights. False otherwise.
     */
    private boolean isCopyrightKnown(String copyrightName) {
        for (String knownCopyright : RECOGNIZED_COPYRIGHTS) {
            if (copyrightName.contains(knownCopyright)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the header read matches with the wished header. False if some
     * differencies are present.
     *
     * @param text A string containing the whole file.
     */
    private boolean matchHeader(final String text) {
        final String[] textSplitedOnEoL = text.split("\n", 16);
        final String[] pattern = {
                "/*",
                " *    GeoTools - The Open Source Java GIS Toolkit",
                " *    http://geotools.org",
                " *",
                " *    (C) 2008, Open Source Geospatial Foundation (OSGeo)",
                " *",
                " *    This library is free software; you can redistribute it and/or",
                " *    modify it under the terms of the GNU Lesser General Public",

                " *    License as published by the Free Software Foundation;",
                " *    version 2.1 of the License.",
             // " *    License as published by the Free Software Foundation; either",
             // " *    version 2.1 of the License, or (at your option) any later version.",

                " *",
                " *    This library is distributed in the hope that it will be useful,",
                " *    but WITHOUT ANY WARRANTY; without even the implied warranty of",
                " *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU",
                " *    Lesser General Public License for more details."
        };
        for (int i = 0; i<pattern.length; i++) {
            if (!textSplitedOnEoL[i].contains("(C)")
                    && !textSplitedOnEoL[i].trim().contentEquals(pattern[i].trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write a summary of what have been done in the writing mode, or what it will do in the
     * information mode.
     */
    private void writeSummary() {
        System.out.println("===========================================================");
        System.out.println("========                 Summary                   ========");
        System.out.println("===========================================================");
        if (numFilesChanged == 0) {
            System.out.println("= No file changed");
        } else {
            System.out.println("= " + numFilesChanged + " file(s) with changes");

            System.out.println("=\t+ " + suspiciousFiles.size() + " suspicious file(s)");
            if (!write) {
                for (String candidate : suspiciousFiles) {
                    System.out.println("=\t\t" + candidate);
                }
            }
            System.out.println("=\t+ " + correctlyChangedFiles.size() + " file(s) correctly changed");
            if (!write) {
                for (String candidate : correctlyChangedFiles) {
                    System.out.println("=\t\t" + candidate);
                }
            }
            System.out.println("= ");
            System.out.println("= " + copyrightProblemsFiles.size() + " file(s) have copyright problems");
            if (!write) {
                for (String candidate : copyrightProblemsFiles) {
                    System.out.println("=\t" + candidate);
                }
            }
        }
        System.out.println("=");
        final int sizeUnchanged = unchangedFiles.size();
        if (sizeUnchanged != 0) {
            System.out.println("= " + sizeUnchanged + " file(s) skipped (no changes, because already well-formed)");
            for (final String path : unchangedFiles) {
                System.out.println("=\t" + path);
            }
        } else {
            System.out.println("= No file skipped.");
        }
        System.out.println("=");
        final int sizeNoHeader = noHeaderFiles.size();
        if (sizeNoHeader != 0) {
            System.out.println("= " + sizeNoHeader + " file(s) with no header. You should add it by hand.");
            for (final String path : noHeaderFiles) {
                System.out.println("=\t" + path);
            }
        } else {
            System.out.println("= No file with no header.");
        }
    }

    /**
     * Write the result of the verification process on all files.
     */
    private void writeVerification() {
        System.out.println("=");
        System.out.println("===========================================================");
        System.out.println("========               Verification                ========");
        System.out.println("===========================================================");
        System.out.println("=");
        final int size = wrongHeaderFiles.size();
        if (size == 0) {
            System.out.println("= All files were correctly processed.");
            return;
        }
        System.out.println("= "+ size + " files with header not matching");
        for (final String wrongHeaderFile : wrongHeaderFiles) {
            System.out.println("= " + wrongHeaderFile);
        }
    }

    /**
     * Just launches the process, with arguments specified by user.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new ReplaceHeaders(args);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
