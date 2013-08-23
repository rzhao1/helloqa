package edu.cmu.lti.oaqa.framework.evaluation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedEvaluater extends EvaluationResultDAO implements
		ExtendedOutputInterface {

	public ExtendedEvaluater(IRunResult runResult, Dataset dataset) {
    super(runResult, dataset);
    
    try {
   // the number of candidates and final answers
      candidateNum = getSize(runResult.getCandidates());
      finalAnswerNum = getSize(runResult.getFinalAnswers());
      
      answerPositions = proceedPositions(super.answerKeys, runResult.getPassages());

      Map<String, Pattern> candidatePatterns = new HashMap<String, Pattern>();
      for (Entry<String, List<String>> entry : runResult.getCandidates().entrySet()) {
        candidatePatterns.put(entry.getKey(), HtmlUtil.getPatternFromList(
                entry.getValue()));
      }
      candidatePositions = proceedPositions(candidatePatterns, runResult.getPassages());
      
      Map<String, Pattern> finalPatterns = new HashMap<String, Pattern>();
      for(Entry<String, List<String>> entry : runResult.getFinalAnswers().entrySet()) {
        finalPatterns.put(entry.getKey(), 
                HtmlUtil.getPatternFromList(entry.getValue().subList(0, 1)));
      }
      
      Map<String, int[][][]> finalAnswerPositions = proceedPositions(finalPatterns, runResult.getPassages());

      proceedMerge(answerPositions, finalAnswerPositions, candidatePositions);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Map<String, int[][][]> candidatePositions;
	private Map<String, int[][][]> answerPositions;

	private Map<String, int[][][]> positions;
	private Map<String, int[][]> flags;
	
	private int candidateNum;
	private int finalAnswerNum;

	public int[][][] getCandidatePositions(String QuestionId) throws Exception {
		return candidatePositions.get(QuestionId);
	}

	public int[][][] getAnswerPositions(String QuestionId) throws Exception {
		return answerPositions.get(QuestionId);
	}

	public int[][][] getAllPositions(String QuestionId) throws Exception {
		return positions.get(QuestionId);
	}

	public int[][] getFlags(String QuestionId) throws Exception {
		return flags.get(QuestionId);
	}


	public int getCandidateAnswerNum() throws Exception {
		return candidateNum;
	}

	public int getFinalAnswerNum() throws Exception {
		return finalAnswerNum;
	}

	private Map<String, int[][][]> proceedPositions(
			Map<String, Pattern> patterns, Map<String, List<String>> passageList) {

		Map<String, int[][][]> map = new HashMap<String, int[][][]>();

		for (Entry<String, List<String>> entry : passageList.entrySet()) {

			String QuestionId = entry.getKey();
			Pattern pattern = patterns.get(QuestionId);
			
			if(pattern == null){
				continue;
			}

			List<String> passages = entry.getValue();

			int[][][] list = new int[passages.size()][][];

			int listCounter = 0;
			for (String passage : passages) {
				LinkedList<int[]> tempList = new LinkedList<int[]>();

				Matcher matcher = pattern.matcher(passage);
				while (matcher.find()) {
					int[] set = new int[2];
					set[0] = matcher.start();
					set[1] = matcher.end();
					tempList.add(set);
				}

				int[][] localList = new int[tempList.size()][];
				int localCounter = 0;
				for (int[] temp : tempList) {
					localList[localCounter++] = temp;
				}
				list[listCounter++] = localList;
			}
			map.put(QuestionId, list);
		}
		return map;
	}
	
	private void proceedMerge(Map<String, int[][][]> list1,
			Map<String, int[][][]> list2, Map<String, int[][][]> list3)throws Exception {
		
		Map<String, int[][][]> tempPos = new HashMap<String, int[][][]>();
		Map<String, boolean[][]> tempFlag = new HashMap<String, boolean[][]>();
		Map<String, int[][]> tempbackPointer = new HashMap<String, int[][]>();
		
		merge(list1, list2, tempPos, tempFlag, tempbackPointer);
		
		Map<String, int[][][]> tempPos2 = new HashMap<String, int[][][]>();
		Map<String, boolean[][]> tempFlag2 = new HashMap<String, boolean[][]>();
		
		tempbackPointer.clear();
		merge(tempPos, list3, tempPos2, tempFlag2, tempbackPointer);
		
		positions = tempPos2;
		flags = new HashMap<String, int[][]>();
		for(Entry<String, boolean[][]> entry : tempFlag2.entrySet()){
			boolean [][] flag1 = tempFlag.get(entry.getKey());
			boolean [][] flag2 = entry.getValue();
			int [][] backPointer = tempbackPointer.get(entry.getKey());
			
			int [][] flagList = new int[flag2.length][];
			
			for(int i = 0; i < flag2.length; i++){
				flagList[i] = new int[flag2[i].length];
				
				for(int j = 0; j < flag2[i].length; j++){
					if(!flag2[i][j]){
						flagList[i][j] = 2;
					} else {
						if(flag1[i][backPointer[i][j]]){
							flagList[i][j] = 0;
						} else {
							flagList[i][j] = 1;
						}
					}
				}
			}
			flags.put(entry.getKey(), flagList);
		}
		
	}
	
	private void merge(Map<String, int[][][]> answerList,
			Map<String, int[][][]> candidateList, Map<String, int[][][]> positions1, Map<String, boolean[][]> flags1, Map<String, int[][]> backPointers1) throws Exception {

		for (Entry<String, int[][][]> entry : candidateList.entrySet()) {
			String id = entry.getKey();
			int[][][] answers = answerList.get(id);
			int[][][] candidates = entry.getValue();
			
			if(answers == null){
				positions1.put(id, candidates.clone());
				boolean [][] falseFlags = new boolean[candidates.length][];
				int[][] zeroPointers = new int[candidates.length][];
				for(int i = 0; i < candidates.length; i++){
					falseFlags[i] = new boolean[candidates[i].length];
					Arrays.fill(falseFlags[i], false);
					
					zeroPointers[i] = new int[candidates[i].length];
					Arrays.fill(zeroPointers[i], 0);
				}
				flags1.put(id, falseFlags);
				backPointers1.put(id, zeroPointers);
				continue;
			}

			if (answers.length != candidates.length) {
				throw new Exception(
						"Answers and candidates have different passage numbers!");
			}

			int[][][] position2 = new int[answers.length][][];
			boolean[][] flag2 = new boolean[answers.length][];
			int[][] backPoiners2 = new int[answers.length][];

			for (int i = 0; i < position2.length; i++) {
				LinkedList<int[]> positionList = new LinkedList<int[]>();
				LinkedList<Boolean> flagList = new LinkedList<Boolean>();
				LinkedList<Integer> backPointerList = new LinkedList<Integer>();
				
				
				mergePossition(answers[i], candidates[i], positionList,
						flagList, backPointerList);

				int[][] positions3 = new int[positionList.size()][];
				boolean[] flags3 = new boolean[flagList.size()];
				int[] backPointers3 = new int[backPointerList.size()];

				int index = 0;
				for (int[] pointSet : positionList) {
					positions3[index++] = pointSet;
				}
				index = 0;
				for (boolean value : flagList) {
					flags3[index++] = value;
				}
				index = 0;
				for(int value : backPointerList){
					backPointers3[index ++] = value;
				}

				position2[i] = positions3;
				flag2[i] = flags3;
				backPoiners2[i] = backPointers3;
			}

			positions1.put(id, position2);
			flags1.put(id, flag2);
			backPointers1.put(id, backPoiners2);
		}
	}

	private void mergePossition(int[][] answers, int[][] candidates,
			LinkedList<int[]> positionList, LinkedList<Boolean> flagList, LinkedList<Integer> backPointerList) {
		int cur = 0;
		int candidateIndex = 0;
		for (int i = 0; i < answers.length; i++) {
			int next = answers[i][0];
			while (candidateIndex < candidates.length
					&& candidates[candidateIndex][0] < cur) {
				candidateIndex++;
			}
			while (candidateIndex < candidates.length
					&& candidates[candidateIndex][1] < next) {
				positionList.add(candidates[candidateIndex]);
				flagList.add(false);
				backPointerList.add(candidateIndex);
				candidateIndex++;
			}
			positionList.add(answers[i]);
			flagList.add(true);
			backPointerList.add(i);

			cur = answers[i][1];
		}

		while (candidateIndex < candidates.length
				&& candidates[candidateIndex][0] < cur) {
			candidateIndex++;
		}
		for (; candidateIndex < candidates.length; candidateIndex++) {
			positionList.add(candidates[candidateIndex]);
			flagList.add(false);
			backPointerList.add(candidateIndex);
		}
	}
	
	int getSize(Map<String, List<String>> list){
		int result = 0;
		for(Entry<String, List<String>> entry : list.entrySet()){
			result += entry.getValue().size();
		}
		return result;
	}
}
