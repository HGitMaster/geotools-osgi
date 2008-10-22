This module is used to allow geotools to communicate with the EPSG database made available
as a Microsoft Access database.

The instructions are included formally in the javadocs for this module
(see org/geotools/referencing/factory/espg/doc-files/Access.html) reproduced
here for your convience.

-- Text of Access.html ---

The EPSG database is available in MS-Access format. Installing this database
is the recommanded way to get the EPSG factory running. You don't need the
MS-Access software; only the ODBC driver for MS-Access is required, and this
driver is usually bundled with all Windows installation. Steps to follow:

1. Download the MS-Access EPSG database from http://www.epsg.org and copy the
EPSG_v6.mdb anywhere in yours file system. You can rename it at yours convenience.

2. Open the ODBC data sources dialog box from the Windows's Control Panel.

3. Click the Add... button and select Microsoft Access Driver (*.mdb).

4. In the Data source field, enter EPSG.

5. Click on the Select... button and select yours (potentially renamed) EPSG_v6.mdb file.

Make sure that the JAR file from plugin/epsg-access (which contains only the
AccessDataSource class and its META-INF/services/ registration) is in yours classpath.
