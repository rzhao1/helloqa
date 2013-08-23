package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SystemResponseLoader {

  private Map<String, List<String>> candidates;
  private Map<String, List<String>> finalAnswers;
  private Map<String, List<String>> passages;
  
  public SystemResponseLoader() {
    candidates = new LinkedHashMap<String, List<String>>();
    finalAnswers = new LinkedHashMap<String, List<String>>();
    passages = new LinkedHashMap<String, List<String>>();
  }
  
  /**
   * Store candidate information linked with question id
   * @param questionId
   * @param candidate
   */
  public void addCandidate(String questionId, String candidate) {
    addToMap(questionId, candidate, candidates);
  }
  
  /**
   * Store candidate information linked with question id
   * @param questionId
   * @param candidate
   */
  public void addFinalAnswer(String questionId, String finalAnswer) {
    addToMap(questionId, finalAnswer, finalAnswers);
  }
  
  /**
   * Store passage information linked with question id
   * @param questionId
   * @param candidate
   */
  public void addPassage(String questionId, String passage) {
    addToMap(questionId, passage, passages);
  }
  
  private void addToMap(String key, String value, Map<String, List<String>> map) {
    List<String> list = map.get(key);
    if (list == null) {
      list = new ArrayList<String>();
    }
    list.add(value);
    map.put(key, list);
  }
  
  /**
   * Getter for the candidates
   * @return all candidates in the collection grouped by question id
   */
  public Map<String, List<String>> getCandidates() {
    return candidates;
  }
  
  /**
   * Getter for the candidates
   * @return all candidates in the collection grouped by question id
   */
  public Map<String, List<String>> getFinalAnswers() {
    return finalAnswers;
  }

  /**
   * Getter for the passages
   * @return all passages in the collection grouped by question id
   */
  public Map<String, List<String>> getPassages() {
    return passages;
  }
}
