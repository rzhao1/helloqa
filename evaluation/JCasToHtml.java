package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.framework.JCasManipulator;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class JCasToHtml {

  public static String casToHtml( JCas jcas, EvaluationDAO dao ) throws Exception {
    StringBuilder sb = new StringBuilder();
    JCas questionView =  ViewManager.getQuestionView(jcas);
    JCas documentView =  ViewManager.getDocumentView(jcas);
    
    String qid = JCasManipulator.loadQuestion(questionView).getId();
    
    String answer = null;
    boolean[] passageRelevance = null;
    
    int [][][] positions = null;
    int [][] flags = null;
    if (dao != null) { // batch result browser mode
      answer = dao.getDataset().getAnswerKeys().get(qid);
      IEvaluationResult er = dao.getEvaluationResult();
      passageRelevance = er.getPassageRelevance(qid);
      
      positions = ((ExtendedOutputInterface) er).getAllPositions(qid);
      flags = ((ExtendedOutputInterface) er).getFlags(qid);
    }
    
    boolean[] finalJudgment = dao.getEvaluationResult().getFinalAnswerCorrectness(qid);
    boolean failed = (finalJudgment == null
            || finalJudgment.length == 0 
            || !finalJudgment[0]);
    sb.append( toQuestionHeader(dao, qid, answer, failed) );
    sb.append( "<table class=\"bordered\" width=\"100%\">\n" );
    sb.append( toQuestionAnalysisHtml(dao, qid) );
    sb.append( toKeytermsHtml(dao, qid) );
    sb.append( toDocumentHtml(dao, qid, documentView, passageRelevance, positions, flags) );
    sb.append( toAnswerHtml(dao, qid, "Answer Candidates", false) );
    sb.append( toAnswerHtml(dao, qid, "Final Answers", true) );
    sb.append( "</table>\n<br>\n<br>\n" );
    return sb.toString();
  }
  
  public static String toQuestionHeader( EvaluationDAO dao, String qid, String answerPattern, boolean failed ) {
    return "<h3><div class=\"answer_key\">"+
    (answerPattern!=null?("Answer Key: "+answerPattern):"Answer Key Not Defined")+"</div>"
    +qid+": "+dao.getDataset().getQuestions().get(qid)+" "+
    (answerPattern!=null&&failed?"<span style=\"color:#ff0000;\">(Failed)</span>":"")+
    "</h3>\n";
  }
  
  /**
   * Appends both Answer Type and IC event types
   */
  public static String toQuestionAnalysisHtml( EvaluationDAO dao, String qid ) {
    String atype = dao.getRunResult().getAnswerTypes().get(qid);
    String icType = dao.getRunResult().getICEventTypes().get(qid);
    
    return "<tr><td valign=\"top\" width=\"120\"><strong>Answer Type</strong></td><td>"+atype+"</td></tr>\n<tr><td valign=\"top\" width=\"120\"><strong>Focus IC Event</strong></td><td>"+icType+"</td></tr>\n";
  }
  
  public static String toKeytermsHtml( EvaluationDAO dao, String qid ) {
    List<String> keyterms = dao.getRunResult().getKeyterms().get(qid);
    return "<tr><td valign=\"top\"><strong>Keyterms</strong></td><td>"+keyterms+"</td></tr>\n";
  }
  
  public static String toDocumentHtml( JCas documentView, boolean[] relevance ) {
    boolean succeeded = false;
    if (relevance!=null && relevance.length>0) {
      for (boolean b : relevance) {
        if (b) {
          succeeded = true;
          break;
        }
      }
    } else {
      succeeded = true;
    }
    List<RetrievalResult> documents = JCasManipulator.loadDocuments(documentView);
    StringBuilder sb = new StringBuilder();
    String message = (succeeded?"":"<div class=\"small_warning\">(Failed to retrieve relevant document)</div>");
    sb.append("<tr><td valign=\"top\"><strong>Documents</strong>" +
       message+
       "</td><td>\n");
    
    sb.append("<div class=\"docs\"><ol>\n");
    for (int i=0;i<documents.size();i++) {
      String style = (relevance!=null && relevance[i])?"relevant doc":"doc";
      sb.append("<li class=\""+style+"\">");
      sb.append(HtmlUtil.escapeHtmlEntities(documents.get(i).getText()) );
      sb.append("</li>\n");
    }
    sb.append("</ol></div>\n");
    sb.append("</td></tr>\n");
    return sb.toString();
  }
  
  /**
   * 
   * @param view either candidate view or final answer view
   * @return
   */
  public static String toAnswerHtml( EvaluationDAO dao, String qid, 
          String title, boolean finalMode ) throws Exception {
    String answer = dao.getDataset().getAnswerKeys().get(qid);
    boolean[] judgment = finalMode?dao.getEvaluationResult().getFinalAnswerCorrectness(qid)
            :dao.getEvaluationResult().getCandidateCorrectness(qid);
    List<String> responses = finalMode?dao.getRunResult().getFinalAnswers().get(qid)
            :dao.getRunResult().getCandidates().get(qid);
    
    boolean extractionSucceeded = false;
    boolean rankingSucceeded = false;
    
    
    if (judgment!=null && judgment.length>0) {
      rankingSucceeded = judgment[0];
      for (boolean b:judgment) {
        if (b) {
          extractionSucceeded = true;
          break;
        }
      }
    } else {
      if (answer==null) {
        rankingSucceeded = true;
        extractionSucceeded = true;
      }
    }
    
    String message = "";
    if ( !extractionSucceeded ) {
      message = "<div class=\"small_warning\">(Failed to extract correct candidate)</div>";
    } else if ( !rankingSucceeded ) {
      message = "<div class=\"small_warning\">(Failed to rank correct candidate)</div>";
    } else {
      message = "";
    }
    
    //List<AnswerCandidate>
    StringBuilder sb = new StringBuilder();
    sb.append("<tr><td valign=\"top\"><strong>"+title+"</strong>" +
    message+
    "</td><td>");
    sb.append("<div class=\"candidates wrapper\"><ol>\n");
    if (responses!=null && responses.size()>0) {
      for (int i=0;i<responses.size();i++) {
        String style = (judgment!=null && judgment[i])?"relevant":"";
        sb.append("<li><span class=\""+style+"\">");
        sb.append(HtmlUtil.escapeHtmlEntities(responses.get(i)) );
        sb.append("</span></li>\n");
      }
    }
    sb.append("</ol></div>\n");
    sb.append("</td></tr>\n");
    return sb.toString();
  }
  
  public static String toDocumentHtml(EvaluationDAO dao, String qid,
          JCas documentView, boolean[] relevance, int[][][] positions,
          int[][] flags) {
    List<String> keyterms = dao.getRunResult().getKeyterms().get(qid);
    
    Pattern p = HtmlUtil.getPatternFromList(keyterms);
    
    boolean succeeded = false;
    if (relevance != null && relevance.length > 0) {
      for (boolean b : relevance) {
        if (b) {
          succeeded = true;
          break;
        }
      }
    } else {
      succeeded = true;
    }
    List<RetrievalResult> documents = JCasManipulator.loadDocuments(documentView);
    StringBuilder sb = new StringBuilder();
    String message = (succeeded ? ""
            : "<div class=\"small_warning\">(Failed to retrieve relevant document)</div>");
    sb.append("<tr><td valign=\"top\"><strong>Documents</strong>" + message + "</td><td>\n");

    sb.append("<div class=\"docs\"><ol>\n");
    for (int i = 0; i < documents.size(); i++) {
      String doc = documents.get(i).getText();
      
      String style = (relevance != null && relevance[i]) ? "relevant doc" : "doc";
      sb.append("<li class=\"" + style + "\">");

      if (positions == null || i >= positions.length || positions[i] == null || flags == null
              || i >= flags.length || flags[i] == null) {
        sb.append(HtmlUtil.escapeHtmlEntities(doc));
      } else {
        StringBuilder sb2 = new StringBuilder();

        int cur = 0;
        int jmax = Math.min(positions[i].length, flags[i].length);
        for (int j = 0; j < jmax; j++) {
          if (positions[i][j] == null || positions[i][j].length != 2) {
            continue;
          }
          sb2.append(HtmlUtil.escapeHtmlEntities(doc.substring(cur, positions[i][j][0])));
          if (flags[i][j] == 0) {
            sb2.append("<span style=\"background-color:#00FF00;border:1px solid #cccccc\">");
          } else if (flags[i][j] == 1) {
            sb2.append("<span style=\"background-color:#E799A3;border:1px solid #cccccc\">");
          } else {
            sb2.append("<span style=\"background-color:#C2DFFF;border:1px solid #cccccc\">");
          }
          sb2.append(HtmlUtil.escapeHtmlEntities(doc.substring(positions[i][j][0],
                  positions[i][j][1])));
          sb2.append("</span>");
          cur = positions[i][j][1];
        }
        sb2.append(HtmlUtil.escapeHtmlEntities(doc.substring(cur, doc.length())));
        sb.append(sb2.toString());
      }
      sb.append("</li>\n");
    }
    sb.append("</ol></div>\n");
    sb.append("</td></tr>\n");
    
    Matcher m = p.matcher( sb.toString() );
    StringBuffer b = new StringBuffer();
    while (m.find()) {
      String key = m.group(1);
      String value = "<u>"+key+"</u>";
      value = value.replaceAll("\\$","&#36;");
      m.appendReplacement(b, value);
    }
    m.appendTail(b);
    return b.toString();
  }
}
