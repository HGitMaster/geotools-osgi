

<StyledLayerDescriptor version="1.0.0" 
	xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
	xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<NamedLayer> 
<Name> lines demo </Name>

    <UserStyle>        
        <FeatureTypeStyle>
            
            <Rule><Name>plage</Name>
                <PolygonSymbolizer>
                    <Fill> 
                        <CssParameter name="fill">#EEEE99</CssParameter> 
                        <CssParameter name="fill-opacity">0.5</CssParameter> 
                    </Fill>
                    <Stroke>
                        <CssParameter name="stroke">#AAAA55</CssParameter>
                        <CssParameter name="stroke-width">1</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>                    
                </PolygonSymbolizer>
             </Rule>

            
        </FeatureTypeStyle>
    </UserStyle>

</NamedLayer>
</StyledLayerDescriptor>

