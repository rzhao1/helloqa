package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class implements getAnswerKeys() method in InputInterface.
 * 
 * @author Yuanpeng_Li
 * 
 */
public class AnswerKeyLoader {

	private static final String goldstandardDir = "dataset/goldstandard";
	
	private Map<String, String> answerKeys = null;

	/**
	 * Get answer keys
	 * @return answer keys as a map of (key, value) = (qid, answer pattern in regex)
	 */
	public Map<String, String> getAnswerKeys() {
		
		if(answerKeys != null){
			return answerKeys;
		}

		Map<String, String> map = new HashMap<String, String>();

		File[] filenames = new File(goldstandardDir).listFiles();
		
		// Proceed files one by one.
		for (int i = 0; i < filenames.length; i++) {
		  if (filenames[i].getName().startsWith(".")) continue; 
			Map<String, List<String>> mapList = new HashMap<String, List<String>>();
			loadOneFile(mapList, filenames[i]);

			if (!Collections.disjoint(map.keySet(), mapList.keySet())) {
				throw new RuntimeException(
						"Duplicate question ID is defined across diffrent files");
			}

			for (Entry<String, List<String>> entry : mapList.entrySet()) {
				map.put(entry.getKey(), concatAnswerKeys(entry.getValue()));
			}
		}

		answerKeys = map;
		
		return map;
	}

	/**
	 * This method load one pattern file. Note that map is shared by all files.
	 * 
	 * @param map
	 * @param filename
	 * @throws Exception
	 * @throws Exception
	 */
	private void loadOneFile(Map<String, List<String>> mapList, File file) {

		// Open a file.
//		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
//			is = cl.getResourceAsStream(filename);
		  is = new FileInputStream(file);
			isr = new InputStreamReader(is, "utf8");
			br = new BufferedReader(isr);

			while (br.ready()) {
				// Read the file line by line.
				String strLine = br.readLine();
				if (strLine.isEmpty()) {
					throw new Exception("Line is empty.");
				}

				// split one line to question ID and answer type by space.
				int splitIndex = strLine.indexOf(' ');

				// if the space does not exit or the first occurrence is the
				// head or tail, the format is incorrect.
				if (splitIndex <= 0 || splitIndex >= strLine.length() - 1) {
					throw new Exception("Wrong AnswerKey file format.");
				}

				String questionId = strLine.substring(0, splitIndex);
				if (questionId.isEmpty()) {
					throw new Exception("Question ID is empty.");
				}
				String answerKey = strLine.substring(splitIndex + 1);
				if (answerKey.isEmpty()) {
					throw new Exception("Answer key is empty.");
				}

				// Put the pair to map. Question ID should not duplicate across
				// all pattern files.

				List<String> list = mapList.get(questionId);
				if (list == null) {
					list = new LinkedList<String>();
					mapList.put(questionId, list);
				}
				list.add(answerKey);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

			// the order of closing is the opposite as the opening.
			// First in, last out.
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}

			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private String concatAnswerKeys(List<String> keys) {
		// (?!) : ignoring case, \b : word boundary.
		String result = "(?i)(\\b(";

		Iterator<String> iterator = keys.iterator();
		result += iterator.next();

		while (iterator.hasNext()) {
			result += "|" + iterator.next();
		}

		result += ")\\b)";

		return result;
	}

}
