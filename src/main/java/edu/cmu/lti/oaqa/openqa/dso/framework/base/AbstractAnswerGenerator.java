package edu.cmu.lti.oaqa.openqa.dso.framework.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.ecd.phase.ProcessingStepUtils;
import edu.cmu.lti.oaqa.framework.data.AnswerArray;
import edu.cmu.lti.oaqa.framework.data.AnswerWrapper;
import edu.cmu.lti.oaqa.framework.data.QueryConceptList;
import edu.cmu.lti.oaqa.framework.data.QueryConceptWrapper;
import edu.cmu.lti.oaqa.framework.eval.Key;
import edu.cmu.lti.oaqa.framework.eval.retrieval.EvaluationAggregator;
//import edu.cmu.lti.oaqa.framework.data.AnswerArray;
//import edu.cmu.lti.oaqa.framework.data.AnswerWrapper;
//import edu.cmu.lti.oaqa.framework.data.QueryConceptList;
//import edu.cmu.lti.oaqa.framework.data.QueryConceptWrapper;
import edu.cmu.lti.oaqa.openqa.dso.data.AnswerCandidate;
import edu.cmu.lti.oaqa.openqa.dso.framework.Evaluation;
import edu.cmu.lti.oaqa.openqa.dso.framework.jcas.AnswerTypeJCasManipulator;
import edu.cmu.lti.oaqa.openqa.dso.framework.jcas.AnsJCasManipulator;
import edu.cmu.lti.oaqa.openqa.dso.framework.jcas.KeytermJCasManipulator;
import edu.cmu.lti.oaqa.openqa.dso.framework.jcas.ViewManager;
import edu.cmu.lti.oaqa.openqa.dso.framework.jcas.ViewType;

public abstract class AbstractAnswerGenerator extends AbstractLoggedComponent {

  public abstract void initialize();

  public abstract List<AnswerCandidate> generateFinalAnswers(String answerType,
          List<String> keyterms, List<AnswerCandidate> answerCandidates);

  public void process(JCas jcas) throws AnalysisEngineProcessException {
    try {
      String sequenceId=null;
      Pattern value=null;
      String answerType = AnswerTypeJCasManipulator.loadAnswerType(ViewManager.getView(jcas,
              ViewType.ANS_TYPE));

      List<AnswerCandidate> answerCandidates = AnsJCasManipulator.loadAnswerCandidates(ViewManager
              .getView(jcas, ViewType.IE));
      List<String> keyterms = KeytermJCasManipulator.loadKeyterms(ViewManager.getView(jcas,
              ViewType.KEYTERM));

      List<AnswerCandidate> finalAnswers = generateFinalAnswers(answerType, keyterms,
              answerCandidates);
      AnsJCasManipulator.storeCandidates(ViewManager.getView(jcas, ViewType.ANS), finalAnswers);

      JCas gsView = ViewManager.getView(jcas, ViewType.FINAL_ANSWER_GS);

      if (gsView != null) {
        List<QueryConceptWrapper> _gs = QueryConceptList.retrieveQueryConcepts(gsView);

        List<String> gs = new ArrayList<String>();

        for (QueryConceptWrapper w : _gs) {
          gs.add(w.getText());
          value=Pattern.compile(w.getText(),Pattern.CASE_INSENSITIVE);
        }

         sequenceId = ProcessingStepUtils.getSequenceId(jcas);
      }     
      HashMap<String,List<AnswerCandidate>> FinalAnswers=new HashMap<String,List<AnswerCandidate>>();
      HashMap<String,Pattern> answerKeyHashMap=new HashMap<String,Pattern>();
      answerKeyHashMap.put(sequenceId,value );
      FinalAnswers.put(sequenceId, finalAnswers);
      Evaluation temp=new Evaluation(FinalAnswers, answerKeyHashMap);
      System.out.println("The current MRR IS "+temp.getAnswerRecall());

    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

  }
}
