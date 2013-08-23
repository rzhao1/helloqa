package edu.cmu.lti.oaqa.framework.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import edu.cmu.lti.oaqa.framework.QuestionReader;

/**
 * This class implements getAnswerKeys() method in InputInterface.
 * 
 * @author Yuanpeng_Li
 * @author Hideki Shima
 */
public class DatasetLoader {

	private static final String questionDir = "dataset/questions";
	private static final String goldstandardDir = "dataset/goldstandard";
	private static final String goldstandardAtypeDir = "dataset/goldstandard-atype";

	// size() may be different
	private Map<String, String> questions = null;
	private Map<String, String> answerKeys = null;
	private Map<String, String> goldAnswerTypes = null;

	public DatasetLoader() {
		questions = loadMap(new File(questionDir));
		answerKeys = loadMap(new File(goldstandardDir));
		goldAnswerTypes = loadMap(new File(goldstandardAtypeDir));
	}

	private Map<String, String> loadMap(File resourceDir) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		File[] filenames = resourceDir.listFiles();

		// Proceed files one by one.
		for (int i = 0; i < filenames.length; i++) {
			if (filenames[i].getName().startsWith("."))
				continue;
			Map<String, List<String>> mapList = new HashMap<String, List<String>>();
			loadOneFile(mapList, filenames[i]);

			// if (!Collections.disjoint(map.keySet(), mapList.keySet())) {
			// throw new
			// RuntimeException("Duplicate question ID is defined across diffrent files");
			// }

			for (Entry<String, List<String>> entry : mapList.entrySet()) {
				map.put(entry.getKey(), concatAnswerKeys(entry.getValue()));
			}
		}
		return map;
	}

	/**
	 * This method load one pattern file. Note that map is shared by all files.
	 * 
	 * @param map
	 * @param filename
	 * @throws Exception
	 */
	private void loadOneFile(Map<String, List<String>> mapList, File file) {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			is = new FileInputStream(file);
			isr = new InputStreamReader(is, "utf8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0 || line.matches("^\\s*#.+")) {
					continue;
				}
				Matcher m = QuestionReader.pQuestionLine.matcher(line);
				if (!m.find())
					continue;
				String qid = m.group(1);
				String item = m.group(2);

				List<String> list = mapList.get(qid);
				if (list == null) {
					list = new LinkedList<String>();
					mapList.put(qid, list);
				}
				list.add(item);
			}
			br.close();
			isr.close();
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String concatAnswerKeys(List<String> keys) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size(); i++) {
			sb.append(i > 0 ? "|" : "");
			sb.append(keys.get(i));
		}
		return sb.toString();
	}

	/**
	 * @return the questions
	 */
	public Map<String, String> getQuestions() {
		return questions;
	}

	/**
	 * Get answer keys
	 * 
	 * @return answer keys as a map of (key, value) = (qid, answer pattern in
	 *         regex)
	 */
	public Map<String, String> getAnswerKeys() {
		return answerKeys;
	}

	/**
	 * Get gold answer types
	 * 
	 * @return the goldAnswerTypes
	 */
	public Map<String, String> getGoldAnswerTypes() {
		return goldAnswerTypes;
	}

}
