

<StyledLayerDescriptor version="1.0.0" 
	xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
	xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<NamedLayer> 
<Name> lines demo </Name>

    <UserStyle>        
        <FeatureTypeStyle>
            
            <Rule><Name>s_bleusquare</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>square</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#0000FF</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>8</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>s_red_cercle</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>circle</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#FF0000</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>6</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>m_red_cercle</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>circle</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#FF0000</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>12</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>l_red_cercle</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>circle</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#FF0000</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>24</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>m_greencross</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>cross</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#00FF00</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>8</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>m_purplestar</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>star</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#FF00FF</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#000000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>8</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>

            <Rule><Name>m_redtriangle</Name>
                <PointSymbolizer>
                    <Graphic>
                        <Mark>
                            <WellKnownName>triangle</WellKnownName>
                            <Fill>
                                <CssParameter name="fill"><ogc:Literal>#FFFFFF</ogc:Literal></CssParameter>
                                <CssParameter name="fill-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                            </Fill>
                            <Stroke>
                                <CssParameter name="stroke"><ogc:Literal>#FF0000</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linecap"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-linejoin"><ogc:Literal>round</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-opacity"><ogc:Literal>1.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-width"><ogc:Literal>3.0</ogc:Literal></CssParameter>
                                <CssParameter name="stroke-dashoffset"><ogc:Literal>0.0</ogc:Literal></CssParameter>
                            </Stroke>
                        </Mark>
                        <Opacity><ogc:Literal>1</ogc:Literal></Opacity>
                        <Size><ogc:Literal>12</ogc:Literal></Size>
                        <Rotation><ogc:Literal>0.0</ogc:Literal></Rotation>
                    </Graphic>
                </PointSymbolizer>
            </Rule>
            
        </FeatureTypeStyle>
    </UserStyle>

</NamedLayer>
</StyledLayerDescriptor>

