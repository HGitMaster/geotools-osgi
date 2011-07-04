Maven Build
------------

Several GeoTools modules depend on other GeoTools modules, so the first thing you will want to do is perform a full build so that you have a jar from each module installed in your local repository.

Your First Build
^^^^^^^^^^^^^^^^

1. Start by going to where you have the source code::
     
     cd C:\java\geotools\trunk
2. And check that we actually have the source code::
     
     C:\java\geotools\trunk>dir
      Volume in drive C is INTERNAL
      Volume Serial Number is 3CA5-71DD
      Directory of C:\java\geotools\trunk
     26/04/2007  11:12 AM    <DIR>          .
     26/04/2007  11:12 AM    <DIR>          ..
     11/01/2007  12:25 AM    <DIR>          build
     01/12/2006  01:27 AM    <DIR>          demo
     04/11/2006  01:04 PM    <DIR>          doc
     16/07/2006  07:56 AM    <DIR>          licenses
     07/04/2007  10:36 AM    <DIR>          modules
     26/04/2007  11:12 AM            52,450 pom.xml
     22/10/2006  09:11 AM             3,705 README.txt
     26/04/2007  10:08 AM    <DIR>          target
                    2 File(s)         56,155 bytes
                    8 Dir(s)  15,264,776,192 bytes free

3. Make sure you are connected to the internet
4. Start your first build::
     
     C:\java\geotools\trunk>mvn install

5. If all is well, Maven should download the required .jar files and build GeoTools modules.
6. At the end of this process it will display a list of all the modules which were built and installed correctly.::
     
      [INFO] ------------------------------------------------------------------------
      [INFO] BUILD SUCCESSFUL
      [INFO] ------------------------------------------------------------------------
      [INFO] Total time: 9 months, 3 weeks, 12 hours, 3 minuets, and 43 seconds
      [INFO] Finished at: Sat Feb 12 16:05:08 EST 2011
      [INFO] Final Memory: 41M/87M
      [INFO] ------------------------------------------------------------------------

7. The first build takes a while due to the download time for the .jar files.

If you have any trouble check the common build failures at the bottom of this page.

Build Failure
^^^^^^^^^^^^^

It is all well and good to recognise a successful build, but how do you recognise a build that has failed?

1. If your build fails you will get feedback like this::
     
     [INFO] ------------------------------------------------------------------------
     [ERROR] BUILD FAILURE
     [INFO] ------------------------------------------------------------------------
     [INFO] There are test failures.
     [INFO] ------------------------------------------------------------------------
     [INFO] For more information, run Maven with the -e switch
     [INFO] ------------------------------------------------------------------------
     [INFO] Total time: 7 minutes 56 seconds
     [INFO] Finished at: Mon Nov 20 12:15:48 PST 2006
     [INFO] Final Memory: 23M/42M
     [INFO] ------------------------------------------------------------------------
2. You need to scan back through the output and find the "<<< FAILURE!"::
     
     Running org.geotools.data.mif.MIFDataStoreTest
     Tests run: 9, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 6.703 sec <<< FAILURE!
   
3. You can open the test results of the indicated failure in order to see what went wrong.
   Test results are contained in the target directory.

Expected Build times
^^^^^^^^^^^^^^^^^^^^

Depending on your hardware and internet connection:

* Building the first time, where maven needs to download everything, may take 20 to 30 minuets.
* Future builds check for the most recent .jar files from the internet. The checking is based of md5 checksums and does not take long. Building subsequently may take 10 minuets depending on your hardware and internet connection.
* After everything is downloaded can build “offline” and avoid the checking of mdf5 checksums resulting in a faster build of 5-7 minuets.
* Finally you can turn off tests (danger!) and build offline to get a build under 2 minuets

Tips to speed up a build:

* Do not do a “clean” build if you do not have to
* Rebuild a single module after you have modified it
* Update your “settings.xml” file to point to a “mirror” in your country - allowing you to download closer to home
* Build offline (when everything is downloaded to your local repository)

Really Building All Modules
^^^^^^^^^^^^^^^^^^^^^^^^^^^

GeoTools plays host to a number of experiment "unsupported" modules; if you would like to help out on any of these modules (or get a preview of new features)::
   
   mvn install -Dall

The "-Dall" acts as a switch to part engages several profiles; you can also do this by hand with -P

The following “profiles” are included by the “-Dall”:

=================== ========== ===================================================================
Profile             \-Dall     Builds
=================== ========== ===================================================================
``-Pgdal``          included   include modules that depend on having gdal installed into your JRE
``-Ppending``       included   several experimental modules
``-Praster``        included   
``-Pswing``         included   
``-Pworkflow``      included   process and wps support
``-Parchive``                  modules that no longer work
=================== ========== ===================================================================

Building Offline
^^^^^^^^^^^^^^^^

When working offline, you can bypass the checking of md5 and downloading files.

To do this use the following::
   
   C:\java\geotools\trunk>mvn -o install

By avoiding the check of md5 files you can take drastically reduce build time.

Building an Individual module
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Provided you have done at least one complete build you should be able to build individual modules one-at-a-time.

1. Change to the modules home directory::
     
     cd modules/library/cql
     
2. Use maven to compile - it should do a complete build::
     
      mvn compile

3. Use maven to update the local repository - it should run the test cases and install the jar in the local
   repository for other modules (or applications) to use when they build.::
     
     mvn install

If you have not done a full build yet then the build may fail because it can't find the jar for a module it depends on.

* An error caused by not having another GT2 module installed can be a little misleading::
    
    Error: unable to download main-2.1.x.jar
  
* Maven failed to find main-2.1,x.jar in the local repository where a full build should have put it
* Maven tried to download the file from the internet (and failed)
* If you see an error like that, either do a full build or change into the module which is missing (main in this case) and type.::
   
   maven install

Avoiding Tests
^^^^^^^^^^^^^^

You may also notice that running the tests takes a fair amount of time. While these tests need to be run before you commit for the day, you may want to forgo the wait while experimenting.

The following will build the tests - but not run them::
   
   mvn -DskipTests install

This is useful for installing the postgis module test jar; which is used by the postgis-version module as a dependency.

The following will not even build the tests::
   
   mvn -Dmaven.test.skip=true install

Resuming After a Failure
^^^^^^^^^^^^^^^^^^^^^^^^

When doing a full build of GeoTools it can be disheartening when a build fails 90% of the way through causing you to fix and start again.

The -rf (resume from) parameter of is useful in these cases. It is used to resume a multi-module build such as GeoTools from a specific location to avoid rebuilding those modules you have already successfully build.

1. For instance, consider quickly building offline::
     
     mvn install -o -Dall

2. If **modules/library/data** failed due to a missing jar you can resume the build in online mode::
     
     mvn install -rf modules/library/data

This same technique can be used to restart a build after fixing a failed test in a module.

Common Build Problems
^^^^^^^^^^^^^^^^^^^^^

The following common problems occur during a::
   mvn -U clean install

Unable to find org.geotools.maven:javadoc:jar
'''''''''''''''''''''''''''''''''''''''''''''

We have a little of a chicken-and-the-egg problem here. To build the jar by hand.

1. Change to the module directory::
      
      cd build/maven/javadoc

2. Build the javadoc module
     
      mvn install

3. You can now return to the root of the project and restart your build.

Note that this plugin requires your JAVA_HOME to be set to a JDK as it makes use of the tools.jar (in order to build javadocs).

Failure of Metadata RangeSetTest
''''''''''''''''''''''''''''''''

This looks like the following::
   
   [INFO] ----------------------------------------------------------------------------
   [INFO] Building Metadata
   [INFO]    task-segment: [clean, install]
   [INFO] ----------------------------------------------------------------------------
   [INFO] [clean:clean]
   ...
   Running org.geotools.util.RangeSetTest
   Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.031 sec <<< FAILURE!

Navigating into the directory to look at the actual error::
   
   C:\java\geotools\trunk\modules\library\metadata\target\surefire-reports>more *RangeSetTest.txt
   -------------------------------------------------------------------------------
   Test set: org.geotools.util.RangeSetTest
   -------------------------------------------------------------------------------
   Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.031 sec <<< FAILURE!
   testRangeRemoval(org.geotools.util.RangeSetTest)  Time elapsed: 0 sec  <<< ERROR!
   java.lang.NoClassDefFoundError: javax/media/jai/util/Range
           at org.geotools.util.RangeSetTest.testRangeRemoval(RangeSetTest.java:58)

This indicates that Java Advanced Imaging has not been installed into the JRE (please see the dependencies section and try again).

On GeoTools trunk you can try the following experimental option. This will download and use just the JAI jar files, you wont get native performance - but for a build do you even care?::
   
   mvn install -Pnojai

Failure of GridCoverageRendererTest
'''''''''''''''''''''''''''''''''''

This looks like the following::
   
   [INFO] ----------------------------------------------------------------------------
   [INFO] Building Render
   [INFO]    task-segment: [install]
   [INFO] ----------------------------------------------------------------------------
   ...
   Running org.geotools.renderer.lite.GridCoverageRendererTest
   Tests run: 2, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.062 sec <<< FAILURE!
   Details:
   
   C:\java\geotools\trunk\modules\library\render\target\surefire-reports>more *GridCoverageRendererTest.txt
   -------------------------------------------------------------------------------
   Test set: org.geotools.renderer.lite.GridCoverageRendererTest
   -------------------------------------------------------------------------------
   Tests run: 2, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.062 sec <<< FAILURE!
   testPaint(org.geotools.renderer.lite.GridCoverageRendererTest)  Time elapsed: 0.047 sec  <<< ERROR!
   java.lang.NullPointerException
        at org.geotools.renderer.lite.GridCoverageRendererTest.getGC(GridCoverageRendererTest.java:103)
        at org.geotools.renderer.lite.GridCoverageRendererTest.testPaint(GridCoverageRendererTest.java:163)
   
   testReproject(org.geotools.renderer.lite.GridCoverageRendererTest)  Time elapsed: 0 sec  <<< ERROR!
   java.lang.NullPointerException
        at org.geotools.renderer.lite.GridCoverageRendererTest.getGC(GridCoverageRendererTest.java:103)
        at org.geotools.renderer.lite.GridCoverageRendererTest.testReproject(GridCoverageRendererTest.java:199)

This indicates that Image IO support has not been installed into the JRE (please see the dependencies section and try again).

Unable to Delete Directory on Windows
'''''''''''''''''''''''''''''''''''''

Build systems like maven (that smash files around for a living) are generally incompatible with Microsoft Indexing Service.
From Lim Goh on email

I would also like to point out for future reference that the Windows
Indexing Service is not 100% compatible with maven, and causes some
maven builds to break. Developers who use Windows 7 64-bit (or
anything close like Vista or 32-bit) may have unsuccessful build due
to "unable to delete directory". If that happens please try to disable
Windows Indexing Service for the entire svn working copy and try
again. Hopefully this will fix the problem.

With this in mind it is also advisable for mac developers to “ignore” build directories from Time Machine (as the files change constantly and Time Machine will burn up your space trying to keep track of it all).