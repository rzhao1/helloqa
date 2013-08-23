package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.oaqa.model.Question;

import edu.cmu.lti.oaqa.framework.JCasManipulator;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.AnswerCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;


public class CasLoader implements IRunResult {

  // questionIds.size() does not always equal to passages.size()
  private List<String> questionIds;
  private Map<String, String> questions;
  private Map<String, String> answerTypes;
  private Map<String, String> icEventTypes;
  private Map<String, List<String>> keyterms;
  private Map<String, List<String>> passages;
  private Map<String, List<String>> candidates;
  private Map<String, List<String>> finalAnswers;
  
  public CasLoader(CollectionReader cr, CAS cas) {
    questionIds = new ArrayList<String>();
    questions = new LinkedHashMap<String, String>();
    answerTypes = new LinkedHashMap<String, String>();
    icEventTypes = new LinkedHashMap<String, String>();
    keyterms = new LinkedHashMap<String, List<String>>();
    passages = new LinkedHashMap<String, List<String>>();
    candidates = new LinkedHashMap<String, List<String>>();
    finalAnswers = new LinkedHashMap<String, List<String>>();
    
    try {
      while (cr.hasNext()) {
        cr.getNext(cas);
        JCas jcas = cas.getJCas();
        JCas questionView = ViewManager.getQuestionView(cas.getJCas());
        Question q = JCasManipulator.loadQuestion(questionView);
        String qid = q.getId();
        questionIds.add(qid);
        questions.put(qid,questionView.getDocumentText());
        
        List<String> keyterms = JCasManipulator.loadKeyterms(questionView);
        for ( String keyterm : keyterms ) {
          addKeyterm(qid, keyterm);
        }
        String answerType = JCasManipulator.loadAnswerType(questionView);
        answerTypes.put(qid, answerType);
        
        String icEventType = JCasManipulator.loadIcEvent(questionView);
        icEventTypes.put(qid, icEventType);
        
        JCas documentView =  ViewManager.getDocumentView(jcas);
        List<RetrievalResult> documents = JCasManipulator.loadDocuments(documentView);
        for ( RetrievalResult retrievalResult : documents ) {
          addPassage(qid, retrievalResult.getText());
        }
        
        JCas candidateView =  ViewManager.getCandidateView(jcas);
        List<AnswerCandidate> candidates = JCasManipulator.loadAnswerCandidates(candidateView);
        for ( AnswerCandidate ac : candidates ) {
          addCandidate(qid, ac.getText());
        }
        JCas finalAnswerView =  ViewManager.getFinalAnswerView(jcas);
        List<AnswerCandidate> finalAnswers = JCasManipulator.loadAnswerCandidates(finalAnswerView);
        for ( AnswerCandidate fa : finalAnswers ) {
          addFinalAnswer(qid, fa.getText());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    SortedSet<String> map = new TreeSet<String>();
    for ( String qid : questionIds ) {
      try {
        int qidNum = Integer.parseInt(qid);
        qid = String.format("%06d", qidNum);
        // Fires when qid is a number.
      } catch (Exception e) {}
      map.add(qid);
    }
    questionIds = new ArrayList<String>();
    for ( String key : map ) {
      questionIds.add(key.replaceFirst("^0+", ""));
    }
    
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

  public void addKeyterm(String questionId, String keyterm) {
    addToMap(questionId, keyterm, keyterms);
  }
  
  private void addToMap(String key, String value, Map<String, List<String>> map) {
    List<String> list = map.get(key);
    if (list == null) {
      list = new ArrayList<String>();
    }
    list.add(value);
    map.put(key, list);
  }
  
  @Override
  /**
   * Getter for the candidates
   * @return all candidates in the collection grouped by question id
   */
  public Map<String, List<String>> getCandidates() {
    return candidates;
  }
  
  @Override
  /**
   * Getter for the candidates
   * @return all candidates in the collection grouped by question id
   */
  public Map<String, List<String>> getFinalAnswers() {
    return finalAnswers;
  }

  @Override
  /**
   * Getter for the passages
   * @return all passages in the collection grouped by question id
   */
  public Map<String, List<String>> getPassages() {
    return passages;
  }

  @Override
  /**
   * @return the questionIds
   */
  public List<String> getQuestionIds() {
    return questionIds;
  }

  @Override
  /**
   * @return the answerTypes
   */
  public Map<String, String> getAnswerTypes() {
    return answerTypes;
  }  

  @Override
  /**
   * @return the keyterms
   */
  public Map<String, List<String>> getKeyterms() {
    return keyterms;
  }

@Override
public Map<String, String> getICEventTypes() {
	return icEventTypes;
}


}
