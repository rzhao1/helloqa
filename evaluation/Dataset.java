package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.Map;

public class Dataset {

  private DatasetLoader loader;
  
  public Dataset() {
    this.loader = new DatasetLoader();
  }
  
  /**
   * Returns gold answer types
   * @return gold answer types
   */
  public Map<String, String> getGoldAnswerTypes(){
    return loader.getGoldAnswerTypes();
  }
  
  /**
   * This method returns questions (it's not a system output though) 
   * @return question
   */
  public Map<String, String> getQuestions(){
    return loader.getQuestions();
  }
  
  /**
   * This method returns the answer keys.
   * 
   * @return answer keys
   */
  public Map<String, String> getAnswerKeys(){
    return loader.getAnswerKeys();
  }
  
}
