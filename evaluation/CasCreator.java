package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

public class CasCreator {

  private final static String typeSystemDescName = "OAQATypes.xml";

  public static CAS createOAQACas() throws 
    IOException, InvalidXMLException, 
    ResourceInitializationException {

    File tsDescriptor = new File( "descriptors", typeSystemDescName );
    //File tsDescriptor = ResourceUtil.findResourceAndSave(typeSystemDescName, false);
    XMLInputSource tsIn = new XMLInputSource(tsDescriptor);
    TypeSystemDescription tsDescription 
     = UIMAFramework.getXMLParser().parseTypeSystemDescription(tsIn);
    CAS cas = CasCreationUtils.createCas(tsDescription, null, null);
    tsIn.close();
    return cas;
  }
  
}
