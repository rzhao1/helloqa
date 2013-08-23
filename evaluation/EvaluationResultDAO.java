package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements IEvaluationResult.
 * 
 * @author Yuanpeng_Li
 * @author Hideki Shima
 * 
 */
public class EvaluationResultDAO implements IEvaluationResult {

  protected Map<String, Pattern> answerKeys;

  private double answerTypeAccuracy;

  // For passage related values.
  private double passageRecall;

  private Map<String, boolean[]> passageJudgment;

  // For candidate related values.
  private double candidateAccuracy;

  private double averageCandidateNumber;

  private double candidateRecall;

  private Map<String, boolean[]> candidateJudgment;

  private double finalAnswerAccuracy;

  private double averageFinalAnswerNumber;

  private double finalAnswerRecall;

  private double meanAveragePrecision;

  private Map<String, boolean[]> finalAnswerJudgment;

  private List<String> questionsWithAnswerKeys;

  private int qSize;

  public EvaluationResultDAO(IRunResult runResult, Dataset dataset) {
    try {
      List<String> questions = runResult.getQuestionIds();
      Map<String, String> answerKeyList = dataset.getAnswerKeys();

      // compile the answer keys
      answerKeys = new LinkedHashMap<String, Pattern>();
      for (String questionId : questions) {
        // need boundary marker as it's used to identify answer-bearing passage
        Pattern p = answerKeyList.get(questionId) != null ? Pattern.compile(
                "\\b" + answerKeyList.get(questionId) + "\\b", Pattern.CASE_INSENSITIVE) : null;
        answerKeys.put(questionId, p);
      }

      // In trec dataset, there are many questions that have no answer.
      // int qSize = runResult.getAnswerTypes().size(); WRONG!
      questionsWithAnswerKeys = new ArrayList<String>();
      for (String qid : runResult.getQuestionIds()) {
        if (answerKeys.get(qid) != null) {
          questionsWithAnswerKeys.add(qid);
        }
      }
      qSize = questionsWithAnswerKeys.size();

      Map<String, String> ephyraGoldAnswerTypes = getEphyraAnswerTypes(dataset.getGoldAnswerTypes());
      answerTypeAccuracy = judgeAnswerTypeCorrectness(runResult.getAnswerTypes(), ephyraGoldAnswerTypes);
      passageJudgment = judgePassageRelevance(answerKeys, runResult.getPassages());
      passageRecall = countRecall(passageJudgment, qSize);

      candidateJudgment = judgeCandidateCorrectness(answerKeys, runResult.getCandidates());
      candidateAccuracy = countTop1Accuracy(candidateJudgment, qSize);
      averageCandidateNumber = countNumber(candidateJudgment, qSize);
      candidateRecall = countRecall(candidateJudgment, qSize);

      finalAnswerJudgment = judgeCandidateCorrectness(answerKeys, runResult.getFinalAnswers());
      finalAnswerAccuracy = countTop1Accuracy(finalAnswerJudgment, qSize);
      averageFinalAnswerNumber = countNumber(finalAnswerJudgment, qSize);
      finalAnswerRecall = countRecall(finalAnswerJudgment, qSize);
      meanAveragePrecision = calculateMeanAveragePrecision(finalAnswerJudgment, qSize);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public double getCandidateAccuracy() throws Exception {
    return candidateAccuracy;
  }

  public double getAverageCandidateNumber() throws Exception {
    return averageCandidateNumber;
  }

  public double getCandidateRecall() throws Exception {
    return candidateRecall;
  }

  public double getFinalAnswerAccuracy() throws Exception {
    return finalAnswerAccuracy;
  }

  public double getAverageFinalAnswerNumber() throws Exception {
    return averageFinalAnswerNumber;
  }

  public double getFinalAnswerRecall() throws Exception {
    return finalAnswerRecall;
  }

  public double getMeanReciprocalRank(int maxRank) throws Exception {
    return countReciprocalRank(finalAnswerJudgment, qSize, maxRank);
  }

  public double getMeanAveragePrecision() throws Exception {
    return meanAveragePrecision;
  }

  public boolean[] getCandidateCorrectness(String questionId) throws Exception {
    return candidateJudgment.get(questionId);
  }

  public boolean[] getFinalAnswerCorrectness(String questionId) throws Exception {
    return finalAnswerJudgment.get(questionId);
  }

  public double getPassageRecall() throws Exception {
    return passageRecall;
  }

  public double getAnswerTypeAccuracy() throws Exception {
    return answerTypeAccuracy;
  }

  public boolean[] getPassageRelevance(String questionId) throws Exception {
    System.out.println(questionId);
    return passageJudgment.get(questionId);
  }

  Map<String, String> getEphyraAnswerTypes(Map<String, String> answerTypes) {
    Map<String, String> ephyraAnswerTypes = new LinkedHashMap<String, String>();;
    for (String qid : answerTypes.keySet()) {
      ephyraAnswerTypes.put(qid, getEphyraAnswerType(answerTypes.get(qid)));
    }
    return ephyraAnswerTypes;
  }

  String getEphyraAnswerType(String answerType) {
    // map answer type(s) to Ephyra naming conventions
    // See edu.cmu.lti.oaqa.experimental_impl.question.analyzer.EphyraAnswerTypeExtractor.

    String ephyraAnswerType = answerType.toLowerCase().replaceAll("\\.", "->NE").replaceAll("^", "NE");
    StringBuilder sb = new StringBuilder(ephyraAnswerType);
    Matcher m = Pattern.compile("_(\\w)").matcher(ephyraAnswerType);
    while (m.find()) {
      sb.replace(m.start(), m.end(), m.group(1).toUpperCase());
      m = Pattern.compile("_(\\w)").matcher(sb.toString());
    }

    if (ephyraAnswerType.toLowerCase().contains("person")){ 
      ephyraAnswerType = "NEproperName->NEperson->NEterrorist";
    }
    if (ephyraAnswerType.toLowerCase().contains("organization")) {
      ephyraAnswerType = "NEproperName->NEorganization->NETerroristOrganization";
    }
    if (ephyraAnswerType.toLowerCase().contains("number")) {
      ephyraAnswerType = "NEnumber->NEquantity";
    }
    if(ephyraAnswerType.toLowerCase().contains("type")){
      ephyraAnswerType = "NEproperName->NEtype->NEattacktype";
    }

    return ephyraAnswerType;
  }

  private Map<String, boolean[]> judgePassageRelevance(Map<String, Pattern> patterns,
          Map<String, List<String>> passages) throws Exception {

    Map<String, boolean[]> relevance = new LinkedHashMap<String, boolean[]>();

    for (Entry<String, List<String>> entry : passages.entrySet()) {

      String questionId = entry.getKey();
      List<String> passageList = entry.getValue();
      Pattern pattern = patterns.get(questionId);

      if (pattern == null) {
        continue;
      }

      // True if there exists at least one relevant passage in the first
      // MAX_RELEVANT_PASSAGE_NUM passages.
      boolean[] passageRelevanceList = new boolean[passageList.size()];
      int index = 0;
      for (String passage : passageList) {
        boolean isRelevant = pattern.matcher(passage).find();
        if (!isRelevant) {
          String answerkey = pattern.toString();
          answerkey = answerkey.replace("\\b", "");
          if (passage.contains(answerkey)) {
            isRelevant = true;
          }
        }

        passageRelevanceList[index] = isRelevant;
        index++;
      }
      relevance.put(questionId, passageRelevanceList);
    }
    return relevance;
  }

  private double judgeAnswerTypeCorrectness(Map<String, String> answerTypes, Map<String, String> goldAnswerTypes) {
    int count = 0;
    for (String qid : answerTypes.keySet()) {
      String answerType = answerTypes.get(qid);
      String goldAnswerType = goldAnswerTypes.get(qid);

      // TREC11 does not have any gold answers type for some questions.
      if (answerType == null || goldAnswerType == null) {
        continue;
      }

      if (answerType.equals(goldAnswerType)) {
        count++;
      }
    }

    return (double) count / (double) qSize;
  }

  private double countRecall(Map<String, boolean[]> judgment, int qSize) {
    int count = 0;
    for (String qid : judgment.keySet()) {
      for (boolean relevant : judgment.get(qid)) {
        if (relevant) {
          count++;
          break;
        }
      }
    }
    return (double) count / (double) qSize;
  }

  /**
   * Count recall for structured
   * 
   * @return
   */
  public double countRecallStructured() {
    return 0.0;
  }

  private double countNumber(Map<String, boolean[]> judgment, int qSize) {
    int count = 0;
    for (String qid : judgment.keySet()) {
      count += judgment.get(qid).length;
    }
    return (double) count / (double) qSize;
  }

  private double countTop1Accuracy(Map<String, boolean[]> judgment, int qSize) {
    int count = 0;
    for (String qid : judgment.keySet()) {
      if (judgment.get(qid).length > 0 && judgment.get(qid)[0]) {
        count++;
      }
    }
    return (double) count / (double) qSize;
  }

  private double countReciprocalRank(Map<String, boolean[]> judgment, int qSize, int maxRank) {
    double count = 0;
    for (String qid : judgment.keySet()) {
      boolean[] jds = judgment.get(qid);
      if (jds == null) continue;

      for (int i = 0; i < jds.length; i++) {
        if (i >= maxRank) break;

        if (jds[i]) {
          count = count + (1.0 / (double) (i + 1));
          break;
        }
      }
    }
    return (double) count / (double) qSize;
  }

  private double calculateMeanAveragePrecision(Map<String, boolean[]> judgment, int qSize) {
    double avePrecision = 0;
    for (String qid : judgment.keySet()) {
      boolean[] jds = judgment.get(qid);
      if (jds == null) continue;

      int count = 0;
      double precision = 0;
      for (int i = 0; i < jds.length; i++) {
        if (jds[i]) {
          count++;
          double p = (double) count / (double) (i + 1);
          precision = precision + p;
        }
      }

//       int numOfCorrectAnswers = getCorrectAnswers(qid).size();
//       if (numOfCorrectAnswers > 0) {
//         avePrecision += precision / (double) numOfCorrectAnswers;
//       }
      // Assume the number of correct answers is 1.
      avePrecision += precision / (double) 1.0;
    }
    return (double) avePrecision / (double) qSize;
  }

  private Map<String, boolean[]> judgeCandidateCorrectness(Map<String, Pattern> patterns,
          Map<String, List<String>> candidates) throws Exception {
    Map<String, boolean[]> correctness = new LinkedHashMap<String, boolean[]>();

    for (String qid : candidates.keySet()) {
      List<String> candidateList = candidates.get(qid);
      Pattern pattern = patterns.get(qid);

      if (pattern == null) {
        continue;
      }

      boolean[] correctnessJudgment = new boolean[candidateList.size()];

      for (int i = 0; i < candidateList.size(); i++) {
        String candidate = candidateList.get(i);
        boolean isCorrect = pattern.matcher(candidate).matches();
        if (!isCorrect) {
          candidate = "\\b" + candidate + "\\b";
          String answerkey = pattern.toString();
          if (answerkey.equals(candidate)) {
            isCorrect = true;
          }
        }
        correctnessJudgment[i] = isCorrect;
      }

      correctness.put(qid, correctnessJudgment);
    }

    return correctness;
  }

  @Override
  public List<String> getQuestionsWithAnswerKeys() {
    return questionsWithAnswerKeys;
  }

  @Override
  public double getPassageRecallStructured() throws Exception {
    return countRecallStructured();
  }

}
