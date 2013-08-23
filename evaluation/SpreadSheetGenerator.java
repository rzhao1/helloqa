package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class SpreadSheetGenerator {

  public static void createSpreadSheet( File target, EvaluationDAO dao ) throws Exception {
    
    WorkbookSettings ws = new WorkbookSettings();
//    ws.setLocale(new Locale("ja", "JP"));
//    ws.setEncoding("Windows-31J");
    ws.setLocale(new Locale("en", "EN"));
    ws.setEncoding("ISO-8859-1");
    OutputStream os = new FileOutputStream(target);
    WritableWorkbook workbook = Workbook.createWorkbook(os, ws);
    WritableSheet sheet = workbook.createSheet("analysis", 0);
    createSheet(sheet, dao);
    workbook.write();
    workbook.close();
  }

  private static void createSheet( WritableSheet sheet, EvaluationDAO dao )
  throws Exception {
    String[] legend = {"QID","Question", "Answer key", 
            "Expected answer type", "Answer type", "Keyterms", 
            "# of passages retrieved", "Ranks of passages which contain the answer key",
            "# of candidate answers extracted", "Ranks of candidate answers which match the answer key",
            "Candidate answers (top 5)",
            "# of final answers", "Ranks of final answers which match the answer key",
            "Final answers (top 5)"};

    Dataset d = dao.getDataset();
    IRunResult rr = dao.getRunResult();
    IEvaluationResult er = dao.getEvaluationResult();
    for (int i=0; i<legend.length; i++) {
      sheet.addCell( new Label(i,0,legend[i]) );
    }
    List<String> qids = dao.getRunResult().getQuestionIds();
    for ( int i=0; i<qids.size(); i++ ) {
      String qid = qids.get(i);
      sheet.addCell( new Label(0,i+1, qid) );
     
      String q = d.getQuestions().get(qid);
      sheet.addCell( new Label(1,i+1, q) );
      
      String answerKey = d.getAnswerKeys().get(qid);
      if (answerKey==null) answerKey = "-";
      sheet.addCell( new Label(2,i+1, answerKey) );
      
      String goldAtype = d.getGoldAnswerTypes().get(qid);
      if (goldAtype==null) goldAtype = "-";
      sheet.addCell( new Label(3,i+1, canonicalizeGoldAtype(goldAtype)) );
      
      String atype = rr.getAnswerTypes().get(qid);      
      sheet.addCell( new Label(4,i+1, canonicalizeEphyraAtype(atype)) );

      List<String> keyTerms = rr.getKeyterms().get(qid);
      sheet.addCell( new Label(5,i+1, keyTerms.toString()) );
      
      sheet.addCell( new Number(6,i+1, rr.getPassages().get(qid).size()) );
      sheet.addCell( new Label(7,i+1, judgmentsToString(er.getPassageRelevance(qid))));
      
      List<String> c = rr.getCandidates().get(qid);
      int candSize = c!=null?c.size():0;
      sheet.addCell( new Number(8,i+1, candSize) );
      sheet.addCell( new Label(9,i+1, judgmentsToString(er.getCandidateCorrectness(qid))));
      sheet.addCell( new Label(10,i+1, topNToString(rr.getCandidates().get(qid), 5)));
      
      List<String> a = rr.getFinalAnswers().get(qid);
      int finalSize = a!=null?a.size():0;
      sheet.addCell( new Number(11,i+1, finalSize) );
      sheet.addCell( new Label(12,i+1, judgmentsToString(er.getFinalAnswerCorrectness(qid))));
      sheet.addCell( new Label(13,i+1, topNToString(rr.getFinalAnswers().get(qid), 5)));
    }
  }
  
  private static String judgmentsToString( boolean[] judgments ) {
    if (judgments==null) return "-";
    StringBuilder sb = new StringBuilder();
    for ( int i=0; i<judgments.length; i++ ) {
      if (judgments[i]) {
        sb.append((sb.length()>0?", ":"")+(i+1));
      }
    }
    return sb.length()>0?"["+sb.toString()+"]":"-";
  }
  
  private static String topNToString( List<String> rankedList, int N ) {
    if (rankedList==null) return "-";
    StringBuilder sb = new StringBuilder();
    for ( int i=0; i<Math.min(N,rankedList.size()); i++ ) {
      sb.append((i>0?", ":"")+(rankedList.get(i)));
    }
    return sb.length()>0?"["+sb.toString()+"]":"-";
  }
  
  private static String canonicalizeEphyraAtype( String atype ) {
    atype = atype.replaceAll("->", ".");
    atype = atype.replaceFirst("^NE", "");
    atype = atype.replaceFirst("\\.NE", ".");
    atype = atype.toUpperCase();
    return atype;
  }
  
  private static String canonicalizeGoldAtype( String atype ) {
    atype = atype.replaceAll("_", "");
    return atype;
  }
}
