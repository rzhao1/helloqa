package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.List;
import java.util.Map;

/**
 * This class is the input interface of evaluation.
 * 
 * @author Yuanpeng_Li
 * @author Hideki Shima
 * 
 */
public interface IRunResult {

  /**
   * This is a run time thing so keep it here, rather than in Dataset
   * @return question ids in the target dataset
   */
  public List<String> getQuestionIds();
  
	 /**
   * This method returns answer types
   * 
   * @return answer type
   */
  Map<String, String> getAnswerTypes();
  
  Map<String, String> getICEventTypes();
  
  /**
   * This method returns key terms.
   * 
   * @return key terms
   */
  Map<String, List<String>> getKeyterms();

	/**
	 * This method returns retrieved passages.
	 * 
	 * @return retrieved passages
	 */
	Map<String, List<String>> getPassages();

	/**
	 * This method returns candidates.
	 * 
	 * @return candidate answers
	 */
	Map<String, List<String>> getCandidates();
	
	/**
	 * This method returns final answers.
	 * 
	 * @return final answers
	 */
	Map<String, List<String>> getFinalAnswers();
	
}
