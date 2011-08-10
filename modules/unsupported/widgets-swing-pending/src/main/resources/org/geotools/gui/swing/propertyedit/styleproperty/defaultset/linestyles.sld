

<StyledLayerDescriptor version="1.0.0" 
	xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
	xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<NamedLayer> 
<Name> lines demo </Name>

    <UserStyle>        
        <FeatureTypeStyle>
            
            <Rule><Name>country_road</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#440000</CssParameter>
                        <CssParameter name="stroke-width">1</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>road</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#000000</CssParameter>
                        <CssParameter name="stroke-width">2</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>main_road</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#FF0000</CssParameter>
                        <CssParameter name="stroke-width">3</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>brook</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#0000FF</CssParameter>
                        <CssParameter name="stroke-width">1</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">round</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>river</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#0000FF</CssParameter>
                        <CssParameter name="stroke-width">2</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">round</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>large_river</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#0000FF</CssParameter>
                        <CssParameter name="stroke-width">3</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">round</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">0.0 0.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>dashed_red_thin</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#FF0000</CssParameter>
                        <CssParameter name="stroke-width">1</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">5.0 2.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>

            <Rule><Name>dashed_blue_large</Name>
                <LineSymbolizer>
                    <Stroke>
                        <CssParameter name="stroke">#0000FF</CssParameter>
                        <CssParameter name="stroke-width">5</CssParameter>
                        <CssParameter name="stroke-opacity">1</CssParameter>
                        <CssParameter name="stroke-linecap">butt</CssParameter>
                        <CssParameter name="stroke-linejoin">round</CssParameter>
                        <CssParameter name="stroke-dasharray">10.0 3.0</CssParameter>
                        <CssParameter name="stroke-dashoffset">0.0</CssParameter>
                    </Stroke>
                </LineSymbolizer>
             </Rule>


        </FeatureTypeStyle>
    </UserStyle>

</NamedLayer>
</StyledLayerDescriptor>

