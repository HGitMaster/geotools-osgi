<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld"
  xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.cubewerx.com/schemas/sld/1.0.0-cw/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>Countries</Name>
    <UserStyle>
      <Name>Countries by Continent</Name>
      <IsDefault>1</IsDefault>
      <FeatureTypeStyle>
        <FeatureTypeName>countries</FeatureTypeName>
        <Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>North America</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#FF0000</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#EA9C9C</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Europe</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#0000FF</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#9A9ADE</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Latin America</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#00FF00</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#A3E8A3</CssParameter>
						</Fill>
          </PolygonSymbolizer>
					
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Sub Saharan Africa</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#7F7F7F</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#A9A9A9</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>NorthAfrica</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#7F7F7F</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#A9A9A9</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Asia</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#FFA500</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#FBD58E</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Australia</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#8B6914</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#CFA53E</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
          <Name>Region</Name>
					<Filter>
						 <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>REGION</ogc:PropertyName>
              <ogc:Literal>Antarctica</ogc:Literal>
            </ogc:PropertyIsEqualTo>
					</Filter>
          <PolygonSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#34C2F1</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
						<Fill>
							<CssParameter name="fill">#ADD8E6</CssParameter>
						</Fill>
          </PolygonSymbolizer>
        </Rule>
				<Rule>
					<TextSymbolizer>
						<Label>
							<ogc:PropertyName>NAME</ogc:PropertyName>
						</Label>
						<Font>
							<CssParameter name="font-style">normal</CssParameter>
						</Font>
					</TextSymbolizer>
				</Rule>
				
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
