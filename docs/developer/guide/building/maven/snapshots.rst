Maven Snapshots
---------------

The current **live** jar published by GeoTools is known as a SNAPSHOT. Projects that want to work directly with GeoTools will depend “SNAPSHOT” rather than specifying a version number.

Dependency SNAPSHOT Changes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

We depend on a "snapshot" of several projects (usually project GeoTools community members are involved such as ImageIO-EXT). In these cases an email will be sent to the developer list asking people to "update with -U"

To respond to one of these emails include "-u" in your next build.

1. Update::
     
     svn up
     
2. build using the -U option::
      
      mvn clean install -U -Dmaven.test.skip=true

The above example skipped the tests (which is common when you are trying for a quick update), please note by definition that "-U" is not compatible with the "-o" offline mode.

Deploy GeoTools SNAPSHOT
^^^^^^^^^^^^^^^^^^^^^^^^

If you are working on GeoServer or uDig or another project that depends on the latest greatest GeoTools release you will need to know how to deploy a SNAPSHOT (so members of your developer community do not get compile errors).

Usually do this after a commit:

1. Update to make sure you are not missing out on anyones work::
     
     svn up
     
2. Build with tests to make sure your commit is not fatal::
     
     mvn clean install
     
3. Commit - remember to include any Jira numbers in your log message::
      
      svn commit -m "Change to fix shapefile charset handling, see GEOT-1437"
      
4. Ensure your ~/.m2/settings.xml has your webdav credentials (these are the same as your svn access credentials)::
      
      <?xml version="1.0" encoding="ISO-8859-1"?> 
      <settings> 
        <offline>false</offline> <!-- set to true to build udig offline -->
        <servers>
          <server> 
            <id>refractions</id> 
            <username>NAME</username> 
            <password>PASSOWRD</password> 
          </server> 
        </servers>  
      </settings>

5. Deploy for members of your community::
      
      mvn deploy -Dmaven.test.skip=true

6. Let your community know via email!

Note a nightly build machine will also generate SNAPSHOTS each day.