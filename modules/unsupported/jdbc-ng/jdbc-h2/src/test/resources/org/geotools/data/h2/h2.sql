--       (C) 2005 by David Blasby and The Open Planning Project 
--       http://openplans.org 
-- 
--       Released under the LGPL 
-- 
--       contact: dblasby@openplans.org 

CREATE ALIAS AsWKT for "org.geotools.data.h2.JTS.AsWKT";
CREATE ALIAS AsText for "org.geotools.data.h2.JTS.AsWKT";
CREATE ALIAS EnvelopeAsText for "org.geotools.data.h2.JTS.EnvelopeAsText";
CREATE ALIAS GeomFromText for "org.geotools.data.h2.JTS.GeomFromText";
CREATE ALIAS Envelope for "org.geotools.data.h2.JTS.Envelope";
CREATE ALIAS GetSRID FOR "org.geotools.data.h2.JTS.GetSRID";
CREATE ALIAS GeometryType for "org.geotools.data.h2.JTS.GeometryType";

COMMIT;
