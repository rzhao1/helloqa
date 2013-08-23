package edu.cmu.lti.oaqa.framework.evaluation;

/**
 * This interface extends the OutputInterface. A new method is added to indicate
 * the position of answer.
 * 
 * @author Yuanpeng_Li
 * 
 */
public interface ExtendedOutputInterface extends IEvaluationResult {

	/**
	 * This method returns the positions of the matched answers. The fist
	 * dimension is for the passage. The second dimension is for the match
	 * numbers. The length of the third dimension is two. The zero and the one
	 * elements the start and the end positions of the answer.
	 * 
	 * @param QuestionId
	 * @return
	 * @throws Exception
	 */
	int[][][] getCandidatePositions(String QuestionId) throws Exception;
	
	int[][][] getAnswerPositions(String QuestionId) throws Exception;
	
	int[][][] getAllPositions(String QuestionId)throws Exception;
	
	int [][] getFlags(String QuestionId)throws Exception;
	
	int getCandidateAnswerNum() throws Exception;
	
	int getFinalAnswerNum() throws Exception;
}
