package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import java.io.*;
import org.geotools.xml.*;
import org.geotools.maven.xmlcodegen.*;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.apache.xml.serialize.*;
import org.eclipse.xsd.*;

public class SchemaClassTemplate
{
  protected static String nl;
  public static synchronized SchemaClassTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    SchemaClassTemplate result = new SchemaClassTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "";
  protected final String TEXT_2 = NL + NL + "import java.util.ArrayList;" + NL + "import java.util.Collections;" + NL + "import java.util.List;" + NL + "" + NL + "import org.opengis.feature.type.AttributeType;" + NL + "import org.opengis.feature.type.ComplexType;" + NL + "" + NL + "import org.geotools.feature.Name;" + NL + "import org.geotools.feature.type.AttributeDescriptorImpl;" + NL + "import org.geotools.feature.type.AttributeTypeImpl;" + NL + "import org.geotools.feature.type.ComplexTypeImpl;" + NL + "import org.geotools.feature.type.SchemaImpl;" + NL + "import org.geotools.feature.type.Name;" + NL;
  protected final String TEXT_3 = NL + "import ";
  protected final String TEXT_4 = ";";
  protected final String TEXT_5 = NL + NL + "public class ";
  protected final String TEXT_6 = "Schema extends SchemaImpl {" + NL;
  protected final String TEXT_7 = NL + "    /**" + NL + "     * <p>" + NL + "     *  <pre>" + NL + "     *   <code>";
  protected final String TEXT_8 = NL + "     *  ";
  protected final String TEXT_9 = NL + "     *" + NL + "     *    </code>" + NL + "     *   </pre>" + NL + "     * </p>" + NL + "     *" + NL + "     * @generated" + NL + "     */";
  protected final String TEXT_10 = NL + "    private static List ";
  protected final String TEXT_11 = "_TYPE_schema = new ArrayList();" + NL + "    static {";
  protected final String TEXT_12 = NL + "            ";
  protected final String TEXT_13 = "_TYPE_schema.add(" + NL + "                new AttributeDescriptorImpl(";
  protected final String TEXT_14 = NL + "                    ";
  protected final String TEXT_15 = ",";
  protected final String TEXT_16 = ",";
  protected final String TEXT_17 = ",";
  protected final String TEXT_18 = ",";
  protected final String TEXT_19 = ",null" + NL + "                )" + NL + "            );";
  protected final String TEXT_20 = NL + "    }" + NL + "    public static final ComplexType ";
  protected final String TEXT_21 = "_TYPE = " + NL + "        new ComplexTypeImpl(" + NL + "            new Name(\"";
  protected final String TEXT_22 = "\",\"";
  protected final String TEXT_23 = "\"), ";
  protected final String TEXT_24 = "_TYPE_schema, ";
  protected final String TEXT_25 = ",";
  protected final String TEXT_26 = NL + "            ";
  protected final String TEXT_27 = ",";
  protected final String TEXT_28 = ",";
  protected final String TEXT_29 = ", ";
  protected final String TEXT_30 = NL + "        );";
  protected final String TEXT_31 = NL + "    public static final ComplexType ";
  protected final String TEXT_32 = "_TYPE = " + NL + "        new ComplexTypeImpl(" + NL + "            new Name(\"";
  protected final String TEXT_33 = "\",\"";
  protected final String TEXT_34 = "\"), Collections.EMPTY_LIST, ";
  protected final String TEXT_35 = ",";
  protected final String TEXT_36 = NL + "            ";
  protected final String TEXT_37 = ",";
  protected final String TEXT_38 = ",";
  protected final String TEXT_39 = ", ";
  protected final String TEXT_40 = NL + "        );";
  protected final String TEXT_41 = NL + "    ";
  protected final String TEXT_42 = NL + "    public static final AttributeType ";
  protected final String TEXT_43 = "_TYPE = " + NL + "        new AttributeTypeImpl(" + NL + "            new Name(\"";
  protected final String TEXT_44 = "\",\"";
  protected final String TEXT_45 = "\"), ";
  protected final String TEXT_46 = ", ";
  protected final String TEXT_47 = ",";
  protected final String TEXT_48 = NL + "            ";
  protected final String TEXT_49 = ",";
  protected final String TEXT_50 = ",";
  protected final String TEXT_51 = ", ";
  protected final String TEXT_52 = NL + "        );";
  protected final String TEXT_53 = NL;
  protected final String TEXT_54 = NL + NL + "    public ";
  protected final String TEXT_55 = "Schema() {" + NL + "        super(\"";
  protected final String TEXT_56 = "\");" + NL + "        ";
  protected final String TEXT_57 = NL + "        put(new Name(\"";
  protected final String TEXT_58 = "\",\"";
  protected final String TEXT_59 = "\"),";
  protected final String TEXT_60 = "_TYPE);";
  protected final String TEXT_61 = NL + "    }" + NL + "}";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
      
    Object[] arguments = (Object[]) argument;
    Schema schema = (Schema) arguments[0];
    String prefix = (String) arguments[1];
    prefix = prefix.toUpperCase();
    
    SchemaGenerator sg = (SchemaGenerator) arguments[2];
    List types = sg.sort();

    stringBuffer.append(TEXT_2);
    
    HashMap ns2import = new HashMap();
    for (Iterator itr = sg.getImports().iterator(); itr.hasNext();) {
        Schema imported = (Schema)itr.next();
        String fullClassName = imported.getClass().getName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1);
        
        ns2import.put(imported.getURI(), className);

    stringBuffer.append(TEXT_3);
    stringBuffer.append(fullClassName);
    stringBuffer.append(TEXT_4);
    
    }

    stringBuffer.append(TEXT_5);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_6);
    
    for (Iterator itr = types.iterator(); itr.hasNext();) {
        AttributeType type = (AttributeType) itr.next();
        Name name = type.getName();

        String uri = name.getNamespaceURI();
        String local = name.getLocalPart();
        
        String binding = type.getBinding().getName() + ".class";
        String isIdentified = type.isIdentified() ? "true" : "false";
        String isAbstract = type.isAbstract() ? "true" : "false";
        
        String restrictions = "Collections.EMPTY_SET";
        String superType = "null";
        
        if (type.getSuper() != null) {
            superType = type.getSuper().getName()
                .getLocalPart().toUpperCase() + "_TYPE";
            String superURI = type.getSuper().getName().getNamespaceURI();
            if (!uri.equals(superURI)) {
                superType = ns2import.get(superURI) + "." + superType;
            }
        }
        
        String description = "null";

    stringBuffer.append(TEXT_7);
    
      XSDTypeDefinition xsdType = sg.getXSDType(type);
      OutputFormat output = new OutputFormat();
      output.setOmitXMLDeclaration(true);
      output.setIndenting(true);

      StringWriter writer = new StringWriter();
      XMLSerializer serializer = new XMLSerializer(writer,output);
    
      try {
        serializer.serialize(xsdType.getElement());
      }
      catch (IOException e) {
        e.printStackTrace();
        return null;
      }
      
      String[] lines = writer.getBuffer().toString().split("\n");
      for (int i = 0; i < lines.length; i++) {

    stringBuffer.append(TEXT_8);
    stringBuffer.append(lines[i].replaceAll("<","&lt;").replaceAll(">","&gt;"));
    
      }

    stringBuffer.append(TEXT_9);
    
        if (type instanceof ComplexType) {
            ComplexType cType = (ComplexType)type;

            if (!cType.getDescriptors().isEmpty()) {

    stringBuffer.append(TEXT_10);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_11);
    
                for (Iterator adItr = cType.getDescriptors().iterator(); adItr.hasNext();) {
                    PropertyDescriptor pd = (PropertyDescriptor) adItr.next();
                    if ( !(pd instanceof AttributeDescriptor) ) {
                        continue;
                    }
                    
                    AttributeDescriptor ad = (AttributeDescriptor) pd;
                    
                    AttributeType adType = ad.getType();
                    
                    String adTypeName = adType.getName().getLocalPart().toUpperCase() + 
                        "_TYPE";
                    String adTypeURI = adType.getName().getNamespaceURI();
                    if (!uri.equals(adTypeURI)) {
                        adTypeName = ns2import.get(adTypeURI) + "." + adTypeName;
                    }
                    String adName = "new Name(\"" + ad.getName().getNamespaceURI() + 
                        "\",\"" + ad.getName().getLocalPart() + "\")";
                    String min = ad.getMinOccurs() + "";
                    String max = ad.getMaxOccurs() + "";
                    String isNillable = ad.isNillable() ? "true" : "false";         

    stringBuffer.append(TEXT_12);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_13);
    stringBuffer.append(TEXT_14);
    stringBuffer.append(adTypeName);
    stringBuffer.append(TEXT_15);
    stringBuffer.append(adName);
    stringBuffer.append(TEXT_16);
    stringBuffer.append(min);
    stringBuffer.append(TEXT_17);
    stringBuffer.append(max);
    stringBuffer.append(TEXT_18);
    stringBuffer.append(isNillable);
    stringBuffer.append(TEXT_19);
    
                }

    stringBuffer.append(TEXT_20);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_21);
    stringBuffer.append(uri);
    stringBuffer.append(TEXT_22);
    stringBuffer.append(local);
    stringBuffer.append(TEXT_23);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_24);
    stringBuffer.append(isIdentified);
    stringBuffer.append(TEXT_25);
    stringBuffer.append(TEXT_26);
    stringBuffer.append(isAbstract);
    stringBuffer.append(TEXT_27);
    stringBuffer.append(restrictions);
    stringBuffer.append(TEXT_28);
    stringBuffer.append(superType);
    stringBuffer.append(TEXT_29);
    stringBuffer.append(description);
    stringBuffer.append(TEXT_30);
    
            }
            else {

    stringBuffer.append(TEXT_31);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_32);
    stringBuffer.append(uri);
    stringBuffer.append(TEXT_33);
    stringBuffer.append(local);
    stringBuffer.append(TEXT_34);
    stringBuffer.append(isIdentified);
    stringBuffer.append(TEXT_35);
    stringBuffer.append(TEXT_36);
    stringBuffer.append(isAbstract);
    stringBuffer.append(TEXT_37);
    stringBuffer.append(restrictions);
    stringBuffer.append(TEXT_38);
    stringBuffer.append(superType);
    stringBuffer.append(TEXT_39);
    stringBuffer.append(description);
    stringBuffer.append(TEXT_40);
              
            }

    stringBuffer.append(TEXT_41);
          
        }
        else {

    stringBuffer.append(TEXT_42);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_43);
    stringBuffer.append(uri);
    stringBuffer.append(TEXT_44);
    stringBuffer.append(local);
    stringBuffer.append(TEXT_45);
    stringBuffer.append(binding);
    stringBuffer.append(TEXT_46);
    stringBuffer.append(isIdentified);
    stringBuffer.append(TEXT_47);
    stringBuffer.append(TEXT_48);
    stringBuffer.append(isAbstract);
    stringBuffer.append(TEXT_49);
    stringBuffer.append(restrictions);
    stringBuffer.append(TEXT_50);
    stringBuffer.append(superType);
    stringBuffer.append(TEXT_51);
    stringBuffer.append(description);
    stringBuffer.append(TEXT_52);
          
        }

    stringBuffer.append(TEXT_53);
    
    }

    stringBuffer.append(TEXT_54);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_55);
    stringBuffer.append(schema.getURI());
    stringBuffer.append(TEXT_56);
    
    for (Iterator itr = types.iterator(); itr.hasNext();) {
        AttributeType type = (AttributeType) itr.next();
        Name name = type.getName();

        String local = name.getLocalPart();

    stringBuffer.append(TEXT_57);
    stringBuffer.append(schema.getURI());
    stringBuffer.append(TEXT_58);
    stringBuffer.append(local);
    stringBuffer.append(TEXT_59);
    stringBuffer.append(local.toUpperCase());
    stringBuffer.append(TEXT_60);
    
    }

    stringBuffer.append(TEXT_61);
    return stringBuffer.toString();
  }
}
