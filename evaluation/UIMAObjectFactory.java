package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.examples.xmi.XmiCollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;

public class UIMAObjectFactory {

  private final static String typeSystemDescName = "OAQATypes.xml";

  private final static String collectionReaderDescName = "XmiCollectionReader.xml";

  public static CAS createOAQACas() throws 
    IOException, InvalidXMLException, 
    ResourceInitializationException {

    File tsDescriptor = new File( "descriptors", typeSystemDescName );
    XMLParser parser = UIMAFramework.getXMLParser();
    TypeSystemDescription tsDescription 
     = parser.parseTypeSystemDescription( new XMLInputSource(tsDescriptor));
    CAS cas = CasCreationUtils.createCas(tsDescription, null, null);
    return cas;
  }
  
  public static CollectionReader createCollectionReader( String targetDir )
  throws Exception{
    CollectionReader cr = null;
    File crDescriptor = new File("descriptors", collectionReaderDescName);
    XMLInputSource crIn = new XMLInputSource(crDescriptor);
    ResourceSpecifier crSpecifier = UIMAFramework.getXMLParser().parseResourceSpecifier(crIn);
    cr = (XmiCollectionReader) UIMAFramework.produceCollectionReader(crSpecifier);
    crIn.close();
    cr.setConfigParameterValue("InputDirectory", targetDir);
    cr.reconfigure();
    return cr;
  }
  
}
