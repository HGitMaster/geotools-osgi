Render FAQ
----------

How do I display a shapefile?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Have a look at :doc:`/tutorial/index` which provides a quickstart showing how to do this.

Q: What is SLD?
^^^^^^^^^^^^^^^

Style Layer Descriptor Specification (SLD) is an OGC standard
defining styles used when drawing maps. Think of it as CSS for maps.

GeoTools rendering is based around the Style Layer Descriptor standard,
for details please see the gt-opengis  module.

This standard is divided into two parts:

* Style Layer Descriptor 1.1 covers the integration of styles
  with a web map server.

* Symbology Encoding 1.1 covers the definition of a feature type style
  controlling how a rendering engine draws features.

Q: What is a MapContext
^^^^^^^^^^^^^^^^^^^^^^^

This module actually defines a new data structure - MapContext. A  
MapContext is made up of a series of MapLayers which are drawn in
order.

This is not part of the formal gt-opengis module because it is not
considered finished, done, or complete. The GeoTools community is
actively looking to collaborate with other projects (such as OpenJUMP,
uDig and deegree) in order to collaborate on these ideas. If open 
source collaboration fails we will look to traditional collaboration
with a standards body in the form of the the OGC working group on "Open
Web Context" documents.

Q: What about the Widget?
^^^^^^^^^^^^^^^^^^^^^^^^^

This module only draws into a Graphics2D context (so printer or image
buffer). You can use this functionality in your own Swing widget in
order to draw something on screen.

* gt-swing
  
  GeoTools does not provide comprehensive GUI components but there is a
  basic Swing widget JMapFrame in the **gt-swing** module which is used
  in GeoTools example code. This can be used directly in your
  applications or it could form the starting point for writing your own
  Swing components. See JMapFrame for more details.

* gt-swt
  
  The gt-swing JMapFrame has been ported to an SWT widget as an 
  unsupported module.

* udig
  
  uDig is an application and SDK built around the Eclipse platform,
  please consider this a real world solution suitable for desktop
  applications. It makes use of the SWT toolkit so while you can use it
  as an example of how to do really fast rendering you will not be able
  to directly use the code in a swing application.