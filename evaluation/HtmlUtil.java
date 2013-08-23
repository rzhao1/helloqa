package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlUtil {

  public static String escapeHtmlEntities( String html ) {
    return html.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }
 
  public static String substituteVariables( String content, Map<String,String> nameValuePair ) {
    StringBuffer sb = new StringBuffer();
    Pattern p = Pattern.compile("<TMPL_VAR NAME=(.+?)>",Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(content);
    while (m.find()) {
      String key = m.group(1);
      String value = nameValuePair.get(key);
      //Replace problematic character that causes java.lang.IllegalArgumentException.
      value = value.replaceAll("\\$","&#36;");
      if (key==null||value==null) { 
        System.err.println("Invalid template: (key,value) = ("+key+","+value+")");
      } else {
        m.appendReplacement(sb, value);
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }
  
  public static Pattern getPatternFromList(List<String> words) {
    StringBuffer sb = new StringBuffer();
    sb.append("\\b(");
    for (int i=0; i<words.size(); i++) {
      String candidate = quote(words.get(i));
      sb.append(i>0?"|":"");
      sb.append(candidate);
    }
    sb.append(")\\b");
    return Pattern.compile(sb.toString(),Pattern.CASE_INSENSITIVE);
  }

  //somehow Pattern.quote didn't compile on linx jdk 1.6.0.16
  private static String quote(String s) {
    int slashEIndex = s.indexOf("\\E");
    if (slashEIndex == -1)
        return "\\Q" + s + "\\E";

    StringBuilder sb = new StringBuilder(s.length() * 2);
    sb.append("\\Q");
    slashEIndex = 0;
    int current = 0;
    while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
        sb.append(s.substring(current, slashEIndex));
        current = slashEIndex + 2;
        sb.append("\\E\\\\E\\Q");
    }
    sb.append(s.substring(current, s.length()));
    sb.append("\\E");
    return sb.toString();
  }
}
