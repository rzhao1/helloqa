package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import org.apache.uima.util.FileUtils;

public class CsvGenerator {

  private static NumberFormat nf;

  static {
    nf = NumberFormat.getNumberInstance();
    nf.setMinimumFractionDigits(4);
    nf.setMaximumFractionDigits(4);
  }

  public static void createCsvForPerformance(File target, EvaluationDAO dao) throws Exception {
    StringBuffer buf = new StringBuffer();

    appendCsvHeader("Passage Recall", buf, true);
    appendCsvHeader("Candidate Recall", buf, false);
    appendCsvHeader("Candidate Accuracy", buf, false);
    appendCsvHeader("Answer Recall", buf, false);
    appendCsvHeader("Answer Accuracy", buf, false);
    appendCsvHeader("Mean Reciprocal Rank (MRR@5)", buf, false);
    appendCsvHeader("Mean Average Precision", buf, false);
    appendCsvHeader("Answer Type Accuracy", buf, false);  // the first column

    appendLineBreak(buf);

    IEvaluationResult er = dao.getEvaluationResult();
    appendCsvData(nf.format(er.getPassageRecall()), buf, true);  // the first column
    appendCsvData(nf.format(er.getCandidateRecall()), buf, false);
    appendCsvData(nf.format(er.getCandidateAccuracy()), buf, false);
    appendCsvData(nf.format(er.getFinalAnswerRecall()), buf, false);
    appendCsvData(nf.format(er.getFinalAnswerAccuracy()), buf, false);
    appendCsvData(nf.format(er.getMeanReciprocalRank(5)), buf, false);
    appendCsvData(nf.format(er.getMeanAveragePrecision()), buf, false);
    appendCsvData(nf.format(er.getAnswerTypeAccuracy()), buf, false);

    FileUtils.saveString2File(buf.toString(), target, "utf-8");
  }

  public static void createCsvForRawResults(File target, EvaluationDAO dao) throws Exception {
    StringBuffer buf = new StringBuffer();

    appendCsvHeader("QID", buf, true);
    appendCsvHeader("\"Answer Type\"", buf, false);
    appendCsvHeader("\"Whether retrieved passages contain a correct answer (passage recall)\"", buf, false);
    appendCsvHeader("\"Whether extracted candidates contain a correct answer (candidate recall)\"", buf, false);
    appendCsvHeader("\"Whether the top-ranked candidate is a correct answer (candidate accuracy)\"", buf, false);
    appendCsvHeader("\"Whether generated answers contain a correct answer (answer recall)\"", buf, false);
    appendCsvHeader("\"Whether the top-ranked answer is a correct answer (answer accuracy)\"", buf, false);

    appendLineBreak(buf);

    IRunResult rr = dao.getRunResult();
    List<String> qids = rr.getQuestionIds();
    IEvaluationResult er = dao.getEvaluationResult();

    for (int i = 0; i < qids.size(); i++) {
      String qid = qids.get(i);
      appendCsvData(qid, buf, true);

      String atype = rr.getAnswerTypes().get(qid);
      appendCsvData(atype, buf, false);

      String judgementPassagesContainCorrectAnswer = checkJudgments(er.getPassageRelevance(qid));
      appendCsvData(judgementPassagesContainCorrectAnswer, buf, false);

      String judgementCandidatesContainCorrectAnswer = checkJudgments(er.getCandidateCorrectness(qid));
      appendCsvData(judgementCandidatesContainCorrectAnswer, buf, false);

      String judgementTopCandidateIsCorrectAnswer = checkTopJudgment(er.getCandidateCorrectness(qid));
      appendCsvData(judgementTopCandidateIsCorrectAnswer, buf, false);

      String judgementAnswersContainCorrectAnswer = checkJudgments(er.getFinalAnswerCorrectness(qid));
      appendCsvData(judgementAnswersContainCorrectAnswer, buf, false);

      String judgementTopAnswerIsCorrectAnswer = checkTopJudgment(er.getFinalAnswerCorrectness(qid));
      appendCsvData(judgementTopAnswerIsCorrectAnswer, buf, false);

      appendLineBreak(buf);
    }

    FileUtils.saveString2File(buf.toString(), target, "utf-8");
  }

  private static void appendCsvHeader(String header, StringBuffer buf, boolean isInFirstColumn) {
    if (!isInFirstColumn) {
      buf.append(",");
    }
    buf.append(header);
  }

  private static void appendLineBreak(StringBuffer buf) {
    buf.append("\n");
  }

  private static void appendCsvData(String data, StringBuffer buf, boolean isInFirstColumn) {
    if (!isInFirstColumn) {
      buf.append(",");
    }
    buf.append(data);
  }

  private static String checkJudgments( boolean[] judgments ) {
    if (judgments == null) return "0";
    for ( int i = 0; i < judgments.length; i++ ) {
      if (judgments[i]) {
        return "1";
      }
    }
    return "0";
  }

  private static String checkTopJudgment( boolean[] judgments ) {
    if (judgments == null) return "0";
    if (judgments[0]) {
      return "1";
    }
    return "0";
  }
}
