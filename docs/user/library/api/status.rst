API Status
----------

References:

* `Jody Garnett <jody.garnett@gmail.com>`_ (Module Maintainer)
* Preferred method of contact is the geotools user email list.
* `Issue Tracker <https://jira.codehaus.org/browse/GEOT>`_ (No specific component)

Check
^^^^^

* |star| IP:
  Reivew is `available <http://svn.osgeo.org/geotools/trunk/modules/library/api/src/site/apt/review.apt>`_
* |star|
  Releasible: no blocking issues in Jira
* |star| Quality Assurance:
  Primiarly interfaces results in low amount of code coverage; code
  contains one known warning (reference to deprecated FeatureLockFactory)
* Stability:
  The interfaces here are stable and subject to review and discussion before being
  considered for the gt-api module. The large number of deprecations present in the filter
  interfaces prevent gt-api from being considered in good shape.
* |star| Supported:
  User documentation is available, module maintainer follows the user and devel lists.

.. |star| image:: /images/star_yellow.gif
             :alt: Star

For more information see the
`developers guide <http://docs.geotools.org/latest/developer/guide/procedures/check.html>`_

Recent Development
^^^^^^^^^^^^^^^^^^

* For the 2.2.x branch the API was created to isolate geotools interfaces from implementation.
* For the 2.3.x branch some modification was made to support the use of Expressions against
  more then just features.
* For the 2.4.x
* For 2.5.x the feature model code was removed (and replaced by gt-opengis feature model)
* For 2.6.x Parameter was isoalted from DataStoreFactorySPI.Param in orde to be reused
  by gt-process code.
* For 2.7.x the style interfaces were updated to be a read/write reflection of gt-opengis style
  interfaces. In additional several interfaces such as Query and FeaureLock were changed to
  be normal classes.

Status
^^^^^^

Here is what the module can tell you right now about GeoTools:

* the API here is pretty good, based on a specification and so on
* for work like Filter or Styling you will see a migration plan to stable geoapi interfaces
* for work like DataStore it represents an API we are happy with

And here is what you will not see:

* Any "FactoryFinder" or any geotools "glue" code that covers for lack of consistency
* Code that patches the API usability - such as DataUtilities

Build
^^^^^

There are no special build requirements.
