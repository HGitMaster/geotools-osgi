Data
====

Supports access to feature information (ie vector information) from a range of data sources.

.. sidebar:: Details
   
   .. toctree::
      :maxdepth: 1
      
      faq
      internal

.. toctree::
   :maxdepth: 1
   
   datastore
   featuresource
   memory
   export

DataStore plugins:

* :doc:`/extension/app-schema`

.. toctree::
   :maxdepth: 1
   
   arcsde
   shape
   pregeneralized
   property

Unsupported plugins:

.. toctree::
   :maxdepth: 1
   
   caching
   csv
   dxf
   edigeo
   excel
   geometryless
   georest
   ogr
   postgis
   sfs
   sql
   wfs
   wfs-ng
   vpf


Additional DataStore plugins are available from :doc:`gt-jdbc <../jdbc/index>` for database access.

The Data module is all about hoisting data (usually in the form of features) off of external services, disk files etc... into your application. This is where you can finally start putting the toolkit to work.


.. image:: /images/gt-data.png

The gt-data module is the foundation for implementing additional :doc:`gt-api <../api/index>` DataStore formats:

* Abstract classes for :doc:`gt-api <../api/index>` DataStore implementors to start from such as **ContentDataStore** and **DirectoryDataStore**

The gt-data module requires plug ins for different formats to be supplied. The module does not define any additional interfaces.
