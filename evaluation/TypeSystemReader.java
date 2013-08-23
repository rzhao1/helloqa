package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.Iterator;

import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.oaqa.model.Question;

/**
 * Extracts type system object from jcas
 *
 */
public class TypeSystemReader {

  /**
   * Get Question as UIMA data model 
   * @param questionView
   * @return question
   */
  public static Question getQuestion(JCas questionView) {
    AnnotationIndex<?> index = questionView.getAnnotationIndex(Question.type);
    Iterator<?> it = index.iterator();
    
    Question question = null;
    if (it.hasNext()) {
      question = (Question)it.next();
    }
    return question;
  }
  
  
}
