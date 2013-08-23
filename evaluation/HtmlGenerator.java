package edu.cmu.lti.oaqa.framework.evaluation;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.framework.JCasManipulator;
import edu.cmu.lti.oaqa.framework.ViewManager;

public class HtmlGenerator {

  private static NumberFormat nf;
  
  static {
    nf = NumberFormat.getNumberInstance();
    nf.setMinimumFractionDigits(4);
    nf.setMaximumFractionDigits(4);
  }

  public static Map<String, String> xmiToHtmlTables(EvaluationDAO dao) throws Exception {
    //map from qid to html table
    //problem: 1,2,10 -> 1,10,2 in text sorting
    SortedMap<String, String> map = new TreeMap<String, String>();
    CollectionReader cr = dao.getCollectionReader();
    CAS cas = dao.getCas();
    cr.reconfigure();
    
    while (cr.hasNext()) {
      cr.getNext(cas);
      JCas questionView = ViewManager.getQuestionView(cas.getJCas());
      String qid = JCasManipulator.loadQuestion(questionView).getId();
      try {
        int qidNum = Integer.parseInt(qid);
        qid = String.format("%06d", qidNum);
        // Fires when qid is a number.
      } catch (Exception e) {}
      
      try {       
        String perQuestionHtml = JCasToHtml.casToHtml(cas.getJCas(), dao);
        map.put(qid, perQuestionHtml);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Map<String, String> sortedMap = new LinkedHashMap<String, String>(map.size());
    for ( String key : map.keySet() ) {
      String newKey = key.replaceFirst("^0+", "");
      sortedMap.put(newKey, map.get(key));
    }
    return sortedMap;
  }
  
  /**
   * Convert xmi zip into html.
   * @param is xmi zip file
   * @param summaryHtml output for summary
   * @param detailHtml output for detailed result
   * @return summary of the evaluation in plain text
   * @throws Exception
   */
  public static String generateSummaryHtml(EvaluationDAO dao) throws Exception {
    StringBuilder sb = new StringBuilder();
    IEvaluationResult er = dao.getEvaluationResult();
    ExtendedOutputInterface extended = (ExtendedOutputInterface) er;
    IRunResult rr = dao.getRunResult();
    
    sb.append("<div class=\"summary\">");
    sb.append("<h4 style=\"margin:0px\">Evaluation Summary</h4><table>");
    summaryRow(sb,"Final Answer Accuracy*",nf.format(er.getFinalAnswerAccuracy()));
    summaryRow(sb,"Candidate Answer Accuracy",nf.format(er.getCandidateAccuracy()));    
    summaryRow(sb,"Final Answer Recall", nf.format(er.getFinalAnswerRecall()) );
    summaryRow(sb,"Candidate Answer Recall*", nf.format(er.getCandidateRecall()) );
    summaryRow(sb,"Passage Recall*", nf.format(er.getPassageRecall()) );
    //summaryRow(sb,"Passage Recall* (With Structured Candidates)", nf.format(er.getPassageRecallStructured()));
    summaryRow(sb,"Number of candidate answers", extended.getCandidateAnswerNum()+"" );
    summaryRow(sb,"Number of final answers", extended.getFinalAnswerNum()+"" );
    summaryRow(sb,"Number of questions with answer keys", er.getQuestionsWithAnswerKeys().size() + "");
    summaryRow(sb,"Number of questions", rr.getQuestionIds().size() + "");
    sb.append("</table></div><br>* = most important metrics<br>\n");
    return sb.toString();
  }
  
  private static void summaryRow( StringBuilder sb, String name, String value ) {
    sb.append( "<tr><td>"+name+"</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td>" +
    		"<td align=\"right\">" + value + "</td></tr>\n" );
  }
  
  public static String generateSummaryText(EvaluationDAO em,  
          Map<String, String> qidOutputMap) throws Exception {

    StringBuilder summaryText = new StringBuilder();

    IEvaluationResult er = em.getEvaluationResult();
    
    summaryText.append("Final Answer Accuracy:   "
            + nf.format(er.getFinalAnswerAccuracy()) + "\n");
    summaryText.append("Candidate Answer Recall: "
            + nf.format(er.getCandidateRecall())+ "\n");
    summaryText.append("Passage Recall:          "
            + nf.format(er.getPassageRecall())+ "\n");

    return summaryText.toString();
  }

  public static String generateDetailPage( List<String> qids, 
          Map<String, String> qidOutputMap, Map<String,String> qidToFileName, 
          int start, int end ) throws Exception {

    StringBuilder tables = new StringBuilder();
    tables.append("<h3 style=\"margin:0px\">Detailed system output</h3>\n");
    
    if (end==-1) end = start;
    for (int i = start; i <= end; i++) {
      tables.append("<hr size=\"1\"><a name=\"" + qids.get(i) + "\" />");
      tables.append("<a href=\"#top\">[TOP]</a>&nbsp;&nbsp;\n");
      if (i - 1 >= 0) {
        String qid = qids.get(i - 1);
        tables.append("<a href=\""+(i==start?qidToFileName.get(qid):"")+"#" + qid + "\">[PREV]</a>&nbsp;&nbsp;\n");
      } 
      if (i + 1 < qids.size()) {
        String qid = qids.get(i + 1);
        tables.append("<a href=\""+(i==end?qidToFileName.get(qid):"")+"#" + qid + "\">[NEXT]</a>\n");
      }
      tables.append(qidOutputMap.get(qids.get(i)));
    }
    return tables.toString();
  }
  
}
