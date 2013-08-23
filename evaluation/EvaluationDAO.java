package edu.cmu.lti.oaqa.framework.evaluation;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;

public class EvaluationDAO {

  private Dataset dataset;
  private IRunResult runResult;
  private IEvaluationResult evaluationResult;
  private CollectionReader collectionReader;
  private CAS cas;
  
  public EvaluationDAO(String targetDir) throws ResourceInitializationException {
    try {
      collectionReader = UIMAObjectFactory.createCollectionReader(targetDir);
      cas = UIMAObjectFactory.createOAQACas();
      dataset =  new Dataset();
      
      // If we have time, we can improve response time by storing the
      // cas content on memory, but it's not straightforward
      runResult = new CasLoader(collectionReader, cas);
      // outputAPI = new Evaluater(runResult);
      evaluationResult = new ExtendedEvaluater(runResult, dataset);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }
  
  /**
   * @return the run result
   */
  public IRunResult getRunResult() {
    return runResult;
  }

  /**
   * @return the evaluation result
   */
  public IEvaluationResult getEvaluationResult() {
    return evaluationResult;
  }

  /**
   * @return the collectionReader
   */
  public CollectionReader getCollectionReader() {
    return collectionReader;
  }

  /**
   * @return the cas
   */
  public CAS getCas() {
    return cas;
  }

  /**
   * @return the dataset
   */
  public Dataset getDataset() {
    return dataset;
  }
  
}
