package c4;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class TDLAgent extends ConnectFour implements Agent {
	
	// Add parameters we will use
	public int player; // 0 for first player, 1 for second player
	public boolean trainAgainstMinimax;
	public boolean isTraining;
	private double epsilon = 0.1;
	private double alpha = 0.004;
	private int numGames = 0;
	
	// n-tuples
	private int[][] nTuples = {
			{0,6,7,12,13,14,19,21},
			{1,3,4,6,7,8,9,10},
			{3,8,9,10,11,15,16,17},
			{25,26,27,28,33,39},
			{2,3,4,8,9,14,15,20},
			{3,4,8,9,10,14,15,16},
			{4,10,15,20,21,22,27,28},
			{11,17,21,23,27,28,33,39},
			{22,25,26,27,30,32,33,37},
			{0,6,7,8,12,13,14,15},
			{27,32,33,34,37,38,39,40},
			{0,6,7,13,14,15,20},
			{13,14,15,18,21,22,23,24},
			{15,16,17,20,22,23,25,31},
			{24,26,30,31,32,33,36,37},
			{1,2,3,8,9,13,14,21},
			{14,15,16,21,26,31,38,39},
			{4,5,10,11,17,22,23,29},
			{5,9,10,11,15,16,21,27},
			{1,2,8,9,14,15,21,26},
			{13,18,19,24,25,26,31,32},
			{10,14,15,16,17,20,21,23},
			{13,20,25,26,27,32,34,41},
			{11,16,23,28,34,35,40,41},
			{15,20,21,22,26,32,33,39},
			{4,9,11,15,16,22,23,29},
			{20,27,28,33,34,35,40,41},
			{8,9,11,15,16,22,23,29},
			{1,2,3,6,7,8,9,13},
			{0,1,2,6,7,8,13,14},
			{8,14,20,25,26,31,33,38},
			{13,18,19,20,21,26,27,33},
			{7,8,9,12,15,19,25,30},
			{1,2,3,8,9,10,16,17},
			{0,1,2,6,8,12,13,18},
			{3,4,8,9,11,14,15,21},
			{18,19,24,30,31,32,36,37},
			{5,10,11,16,17,21,22,27},
			{18,24,25,30,31,32,36,37},
			{21,25,26,27,32,24,35,41},
			{4,10,11,16,17,21,22,27},
			{17,23,28,29,32,33,34,35},
			{2,3,4,5,8,9,10,11},
			{4,10,16,21,26,32,33,38},
			{0,6,12,19,25,31,32,33},
			{1,2,5,8,11,15,16,17},
			{2,3,9,10,11,16,17,22},
			{15,16,17,21,22,23,28,29},
			{12,13,18,19,20,26,27,33},
			{13,14,18,20,24,25,31,37},
			{1,2,6,7,12,13,14,20},
			{2,4,5,7,9,10,14,19},
			{1,2,3,7,8,13,14,20},
			{22,23,29,31,32,33,37,38},
			{27,28,29,31,32,33,37,38},
			{4,5,9,10,15,20,21,22},
			{30,31,33,34,36,37,38,39},
			{3,4,10,11,14,15,16,17},
			{18,19,25,26,31,32,34,39},
			{1,2,7,14,20,27,28,29},
			{9,14,15,20,21,22,27,32},
			{10,14,15,20,21,22,27,32},
			{1,6,7,12,13,20,26,27},
			{20,21,26,27,28,33,35,40}
	};
	// save weights as field here
	// we have 65536 weights for each tuple and 68 tuples
	// total of 4,456,448 weights
	private double[] weights = new double[4456448];
	
	private int[] getIndices(int[][] state1, int[][] state2) {
		int[] indices = new int[weights.length];
		Arrays.fill(indices, 0);
		for (int i = 0; i < nTuples.length; i++) {
			int[] tuple = nTuples[i];
			int index1 = 0;
			int index2 = 0;
			
			for (int j = 0; j < tuple.length; j++) {
				int col = (41 - tuple[j]) % 7;
				int row = (41 - tuple[j]) / 7;
				// determine what the value of that board space is in both states
				if (state1[col][row] != 0) {
					index1 += Math.pow(4, j) * state1[col][row];
				} else if (getColHeight(col) + 1 == row) {
					index1 += 3 * Math.pow(4, j);
				}
				
				if (state2[col][row] != 0) {
					index2 += Math.pow(4, j) * state2[col][row];
				} else if (getColHeight(6 - col) + 1 == row) {
					index2 += 3 * Math.pow(4, j);
				}
			}
			
			indices[index1] = 1;
			indices[index2] = 1;
		}
		return indices;
	}
	
	public void updateAlpha() {
		numGames += 1;
		alpha = 0.001 + 0.003 * Math.exp(-0.0000005*numGames);
	}

	//  moved this to C4Game.java!
	//	public boolean evaluateAgent(TDLAgent agent) {
	//		return false;
	//	}
	
	// one iteration of TDL
	// Darrel: implement this
	// bestMove is the column that is best according to the current weights
	// activeWeights is the vector x_t in the pseudocode when the next move is bestMove
	// bestMoveValue is the value (r(s_{t+1}) + V(s_{t+1})) of the state resulting from bestMove
	// check the getBestMove method for how to get the value of the current state 
	// (between my start and end comments, the correct state should already be initialized)
	// should modify the weights
	public int oneTDLIteration(int bestMove, double bestMoveValue, int[] activeWeights) {
		//epsilon greedy
		double e = ThreadLocalRandom.current().nextDouble();
		if (e < epsilon){
			int[] possibleMoves = generateMoves(false);
			int randomMove = ThreadLocalRandom.current().nextInt(0,possibleMoves.length);
			return possibleMoves[randomMove];
		}

		//initializing values
		double curValue = 0;

		/*double currentValue = 0;
		//getting the value for the current board state
		for (int i = 0; i < weights.length; i++) {
			currentValue += activeWeights[i] * weights[i];
		}
		currentValue = Math.tanh(currentValue);
		*/

		//getting the indices array for the current board state
		int[][] boardState = getBoard();
		int[][] mirroredState = getMirroredField(boardState);
		int[] nextActiveWeights = getIndices(boardState, mirroredState);

		//getting the value for the current board state
		for (int i = 0; i < nextActiveWeights.length; i++) {
			curValue += nextActiveWeights[i] * weights[i];
		}
		curValue = Math.tanh(curValue);

		//update weight array
		double delta_t = bestMoveValue - curValue;
		for (int i = 0; i < weights.length; i++){
			weights[i] += alpha * delta_t * (1 - Math.pow(curValue, 2)) * activeWeights[i];
		}

		return bestMove;
	}	
	
	/**
	 * Generate an empty Board
	 */
	public TDLAgent(boolean againstMinimax, boolean isTraining, int player) {
		super();
		trainAgainstMinimax = againstMinimax;
		this.isTraining = isTraining;
		this.player = player;
		Arrays.fill(weights, 0);
	}


	public String getName() {
		return new String("TDL-Agent");
	}

	// Sophia: Need to implement this
	// first index of table is the row, from bottom of board to the top
	// second index of table is the column, from left to right
	// value is 1 for yellow (first) player, 2 for red (second) player and 0 for empty spaces
	@Override
	public int getBestMove(int[][] table) {
		setBoard(table);
		int[] possibleMoves = generateMoves(false);
		int[][] moveActiveWeights = new int[possibleMoves.length][weights.length];
		double[] values = new double[possibleMoves.length];
		for (int i = 0; i < possibleMoves.length; i++) {
			// calculate vector x for each move
			
			putPiece(possibleMoves[i]);
			
			// start using this part
			int[][] boardState = getBoard();
			int[][] mirroredState = getMirroredField(boardState);
			moveActiveWeights[i] = getIndices(boardState, mirroredState);
			
			// calculate dot product for each
			if (hasWin(player))
				values[i] = 1;
			else if (isDraw())
				values[i] = 0;
			else {
				for (int j = 0; j < weights.length; j++) {
					values[i] += moveActiveWeights[i][j] * weights[j];
				}
				values[i] = Math.tanh(values[i]);
			}
			// end using this part
			
			removePiece(player, possibleMoves[i]);
			
		}
		// return best one or call TDL iteration with the best one
		int maxAt = 0;

		for (int i = 0; i < values.length; i++) {
		    maxAt = values[i] > values[maxAt] ? i : maxAt;
		}
		// call TDL instead when training, pass in the active weights vector as well
		if (isTraining)
			return oneTDLIteration(possibleMoves[maxAt], values[maxAt], moveActiveWeights[maxAt]);
		return possibleMoves[maxAt];
	}
	
	// Don't need this yet
		@Override
		public AgentState getAgentState() {
			// TODO Auto-generated method stub
			return null;
		}

	// Don't need this
	@Override
	public double getScore(int[][] table, boolean putInRange) {
		// TODO Auto-generated method stub
		return 0;
	}

	// Don't need this
	@Override
	public double[] getNextVTable(int[][] table, boolean putInRange) {
		// TODO Auto-generated method stub
		return null;
	}
	

	// Don't need this
	@Override
	public void semOpDown() {
		// TODO Auto-generated method stub
		
	}

	// Don't need this
	@Override
	public void semOpUp() {
		// TODO Auto-generated method stub
		
	}
	

}
