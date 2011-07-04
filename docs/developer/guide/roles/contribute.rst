Contributors
============

Anyone can contribute to the GeoTools project by editing the web site, by writing documentation, by answering questions on the email list, or by contributing code directly into the project.

Initially, newcomers to the project generally participate in an informal role. These types of contributors have no long term responsibility to the project.

Easy informal code contributions
--------------------------------

Informal participants can contribute small modifications to the project code by submitting a patch as an attachment to a JIRA task.

The best way to make a code contribution is to develop a formal patch against a checkout of the code from the SVN branch for which the code is being developed. That is, a contributor uses SVN to obtain the code of the branch, edits the files on that checkout, does a full maven build and test to make sure the patch compiles cleanly, and then uses the 'svn diff' command to generate a patch against the branch. Next, the contributor opens a JIRA issue against the subsystem in which the patch was made. The subject of the item should describe the contribution and ideally mention that a patch is attached.

JIRA will automatically notify the maintainer of the module since that is the best person to do the code review. If no one answers or comments in the subsequent few days, then the contributor can contact the developers' mailing list to let everyone know about the patch and find someone else competent to review the code and integrate the contribution into the code base or provide a request for improvements to the patch.

Large contributions
-------------------

Informal participants can also contribute larger contributions following essentially the same process as that just described for small code contributions but also including the formal transfer of the copyright over the contribution to the Open Source Geospatial Foundation (OSGeo).

Patches submitted to JIRA for large contributions should include the contributor name in the list of authors of the class documentation for any file in which the contributor has made significant changes. That is the contributor's name should be added using the @author javadoc tag.

GeoTools Contributor Agreement
------------------------------

Geotools has adopted a formal policy as part of the process of joining the Open Source Geospatial Foundation (OSGeo). All new contributors will be required to transfer copyright to the foundation.

Contributors wishing to become Committers must print out a copy of the copyright assignment document and either sign it themselves or have their employer sign the document, depending on the circumstances governing when and where the Contributor develops the code. It is up to the Contributor to understand the legal status of the code which the Contributor produces. The document should be sent to the address on the first page of the document. Any questions should be addressed to the developers' mailing list.

Signing a GeoTools Contribution Agreement" is intended to serve several purposes such as shielding the contributor from a direct legal attack by users of the code, enabling the Foundation to represent the interests of the Geotools project in a legal forum, and enabling the GeoTools project to switch licenses when necessary.
