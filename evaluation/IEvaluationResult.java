package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.List;

/**
 * This is the output interface for evaluation.
 * 
 * @author Yuanpeng_Li
 * @author Jun Araki
 * 
 */
public interface IEvaluationResult {

  /**
   * This method returns the number of questions with the correctly classified answer type
   * questions with the correctly classified answer type / number of questions.
   * 
   * @return passage recall.
   */
  double getAnswerTypeAccuracy() throws Exception;

  /**
   * This method returns the number of questions with at least one relevant passages in the first
   * 100 passages / number of questions.
   * 
   * @return passage recall.
   */
  double getPassageRecall() throws Exception;

  double getPassageRecallStructured() throws Exception;

  /**
   * This method returns the number of questions with at least one correct candidate / number of
   * questions.
   * 
   * @return question recall.
   */
  double getCandidateRecall() throws Exception;

  /**
   * This method returns the number of questions with at least one correct final answer / number of
   * questions.
   * 
   * @return question recall.
   */
  double getFinalAnswerRecall() throws Exception;

  /**
   * This method returns the number of questions with the top candidate correct / number of
   * questions.
   * 
   * @return accuracy
   */
  double getCandidateAccuracy() throws Exception;

  /**
   * This method returns the number of questions with the top final answer correct / number of
   * questions.
   * 
   * @return accuracy
   */
  double getFinalAnswerAccuracy() throws Exception;

  /**
   * This method returns the average number of candidates.
   * 
   * @return average number of candidates
   */
  double getAverageCandidateNumber() throws Exception;

  /**
   * This method returns the average number of final answers.
   * 
   * @return average number of final answers
   */
  double getAverageFinalAnswerNumber() throws Exception;

  /**
   * This method returns the mean reciprocal rank (MRR) with the specified highest ranked answers.
   * 
   * @return the mean reciprocal rank
   */
  double getMeanReciprocalRank(int maxRank) throws Exception;

  /**
   * This method returns the mean average precision (MAP).
   * 
   * @return the mean average precision
   */
  double getMeanAveragePrecision() throws Exception;

  /**
   * This method returns an array in which each element indicates whether the passage is relevant.
   * 
   * @param questionId
   * @return list of booleans indicating the relevance of passages.
   */
  boolean[] getPassageRelevance(String questionId) throws Exception;

  /**
   * This method returns an array in which each element indicates whether the candidate is correct.
   * 
   * @param questionId
   * @return list of booleans indicating the relevance of candidates.
   */
  boolean[] getCandidateCorrectness(String questionId) throws Exception;

  /**
   * This method returns an array in which each element indicates whether the final answer is
   * correct.
   * 
   * @param questionId
   * @return list of booleans indicating the relevance of final answer.
   */
  boolean[] getFinalAnswerCorrectness(String questionId) throws Exception;

  /**
   * This method returns the number of valid questions, which is the number of questions that have
   * answer keys.
   * 
   * @return
   * @throws Exception
   */
  List<String> getQuestionsWithAnswerKeys();
}
