Using Subversion
================

The following helpful subversion tips, as so many others, are attributed to IanS and have been stolen from his email.

Subversion Repository
^^^^^^^^^^^^^^^^^^^^^

The GeoTools svn repository holds the source code for GeoTools:

* http://svn.osgeo.org/geotools/

This repository is setup with the following versions of GeoTools:

======================================= ====================================================================
Directory                               version
======================================= ====================================================================
http://svn.osgeo.org/geotools/trunk     this is what we are currently working on
http://svn.osgeo.org/geotools/tags      contains our official releases
http://svn.osgeo.org/geotools/branches  stable development branches; and wild experiments are located here
======================================= ====================================================================

Since SVN also provides web access, all these addresses can be navigated with a web browser.

Typical Development Environment
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Typically, a developer will create a local 'geotools' directory, move into that directory, and do a checkout of GeoTools trunk by ending up with::
   
   geotools/
   geotools/trunk/

The latter “geotools/trunk” directory has all the files used to build GeoTools. All directories, 'trunk' and below will each have a hidden '.svn' directory which holds the repository information.

Developers working on branches will create often create a directory for the branch they wish to work on and change into that directory before checking out the branch of their interest with a command like ending up with::
   
   geotools/
   geotools/trunk
   geotools/stable

This is often used when working on trunk to verify a bug fix; and then backporting the fix to the stable branch.

Ignoring Files
^^^^^^^^^^^^^^

Subversion uses a config file for your local ignores. It's very handy to set this up.
Here are the vital lines from the GeoTools config file used during setup::
   
   [GEOTOOLS:miscellany]
   ### Set global-ignores to a set of whitespace-delimited globs
   ### which Subversion will ignore in its 'status' output.
   global-ignores = *.so *.o *.lo *.la #*# .*.rej *.rej .*~ *~ .#* .DS_Store *.class CVS .nbattrs .nbintdb

As you can see these settings ignore a bunch of temporary files, mac specific files and others.

Ignoring Specific files
^^^^^^^^^^^^^^^^^^^^^^^^

You can set the "svn:ignore" property on a directory; listing a specific file to ignore::
   
   svn propset svn:ignore target .

The above line is used to ignore the target directory for the current folder (ie "." ); this is used so you don't accidentally commit all the generated source code and classes.

This makes those status and commit routines so much cleaner. This works on a folder by folder basis - that is, sub folders do not inherit their parents' props.

1. You could also ignore several things at once (one per line)::
     
     set EDITOR=notepad
     svn propedit svn:ignore .
2. Notepad will be opened allowing you to type::
     
     target
     *.patch
     Thumbs.db

Status / Update Differences
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Subversion allows you to work locally (off-line) in some cases. If you make changes, svn status applies to only the local state. svn update will sync with the repository.

One of my favourite cvs commands was cvs -n -q update -d which says be quiet, make no changes, but tell me what you would do.

Here is how to check what woudl be updated with subversion::
   
   svn -u status

Another nice feature is that you can revert any local changes offline::
   
   svn revert

This is especially handy if you are doing lots of automated changes (like replacing a mucked-up author's name in a project.xml file) and you make a mistake. Instead of a lengthy remote refresh, "clean" local copies are used.

Info
^^^^

Tells you about your checkout::
   
   svn info

Invaluable for finding those pesky urls and in terms of branches and tags, tells you where you are.

Log
^^^

Tells you info about commits/revisions::
   
   svn log

Blame
^^^^^

My favourite. Annotates a document with who changed what and when::
   
   svn blame Sample.java