package c4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class TDLAgent extends ConnectFour implements Agent {
	
	// Fields
	
	// What player is this agent for
	public int player; // 1 for first player, 2 for second player
	// Is this agent training against minimax?
	public boolean trainAgainstMinimax;
	// Is agent currently training?
	public boolean isTraining;
	// Initial and current value of epsilon
	private double epsilonInit = 0.1;
	public double epsilon = 0.1;
	// Initial and current value of alpha
	private double alphaInit;
	public double alpha;
	// Number of training games played
	private int numGames = 0;
	// Last best value found by agent
	public double lastBestValue;
	// Agent corresponding to the other player in self-play
	public TDLAgent other;
	
	// n-tuples used
	private int[][] nTuples = {
			{0,6,7,12,13,14,19,21},
			{1,3,4,6,7,8,9,10},
			{4,5,9,10,11,15,16,17},
			{3,8,9,10,11,15,16,17},
			{25,26,27,28,33,39},
			{2,3,4,8,9,14,15,20},
			{3,4,8,9,10,14,15,16},
			{4,10,15,20,21,22,27,28},
			{11,17,21,23,27,28,33,39},
			{22,25,26,27,30,32,33,37},
			{0,6,7,8,12,13,14,15},
			{0,6,7,12,18,15,32,38},
			{27,32,33,34,37,38,39,40},
			{0,6,7,12,13,14,15,20},
			{1,2,6,7,13,14,15,20},
			{13,14,16,18,21,22,23,24},
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
			{26,27,31,32,33,38,38,39},
			{1,2,7,14,20,27,28,29},
			{9,14,15,20,21,22,27,32},
			{10,14,15,20,21,22,27,32},
			{1,6,7,12,13,20,26,27},
			{20,21,26,27,28,33,35,40}
	};
	
	// agent weights
	// we have 65536 weights for each tuple and 68 tuples
	// total of 4,456,448 weights
	private int weightsPerTuple = 65536;
	private int numWeights = 4456448;
	public double[] weights;
	
	
	
	/**
	 * Constructor
	 */
	public TDLAgent(boolean againstMinimax, boolean isTraining, int player, double alphaInit, double epsilon) {
		super();
		trainAgainstMinimax = againstMinimax;
		this.isTraining = isTraining;
		this.player = player == 0 ? 1 : 2;
		this.alphaInit = alphaInit;
		this.alpha = alphaInit;
		this.epsilonInit = epsilon;
		this.epsilon = epsilon;
		
		this.weights = new double[numWeights];
		Arrays.fill(weights, 0);
	}
	
	
	// will modify the indices field
	public int[] getIndices(int[][] state1, int[][] state2) {
		int[] indices = new int[nTuples.length * 2];
		int curIndex = 0;
		
		for (int i = 0; i < nTuples.length; i++) {
			int[] tuple = nTuples[i];
			int index1 = weightsPerTuple * i;
			int index2 = weightsPerTuple * i;
			
			for (int j = 0; j < tuple.length; j++) {
				int col = (41 - tuple[j]) % 7;
				int row = (41 - tuple[j]) / 7;
				// determine what the value of that board space is in both states
				if (state1[col][row] != 0) {
					index1 += Math.pow(4, j) * state1[col][row];
				} else if (colHeight[col] == row) {
					index1 += 3 * Math.pow(4, j);
				}
				
				if (state2[col][row] != 0) {
					index2 += Math.pow(4, j) * state2[col][row];
				} else if (colHeight[6 - col] == row) {
					index2 += 3 * Math.pow(4, j);
				}
			}
			indices[curIndex] = index1;
			indices[curIndex + 1] = index2;
			curIndex += 2;
		}
		
		return indices;
	}
	
	public void updateAlpha() {
		numGames += 1;
		alpha = 0.001 + (alphaInit - 0.001) * Math.exp(-0.000005*numGames);
		epsilon = 0.1 + (epsilonInit - 0.1) * Math.exp(-0.000005*numGames);
	}
	
	// one iteration of TDL. Updates current agents weights
	public int oneTDLIteration(int bestMove, double bestMoveValue) {
		
		//already checked that do not take random move
		double curValue = 0;

		//getting the indices array for the current board state
		int[][] boardState = getBoard();
		int[][] mirroredState = getMirroredField(boardState);
		int[] indices = getIndices(boardState, mirroredState);

		//getting the value for the current board state
		curValue = 0;
		for (int i = 0; i < indices.length; i++) {
			curValue += weights[indices[i]];
		}
		curValue = Math.tanh(curValue);

		//update weight array
		double delta_t = bestMoveValue - curValue;
		
		double change = alpha * delta_t * (1.0 - Math.pow(curValue, 2));
		
		for (int i = 0; i < indices.length; i++) {
			weights[indices[i]] += change;
		}

		return bestMove;
	}	
	
	public String getName() {
		return new String("TDL-Agent");
	}

	// Determines the best move for the agent based on the current weights of both TDL agents
	@Override
	public int getBestMove(int[][] table) {
		setBoard(table);//initializing values
		int[] possibleMoves = generateMoves(player, false);
		
		if (isTraining) {
			double e = ThreadLocalRandom.current().nextDouble();
			// take random move
			if (e < epsilon){
				int randomMove = ThreadLocalRandom.current().nextInt(0,possibleMoves.length);
				return possibleMoves[randomMove];
			}
		}
		
		double bestValue = -100;
		int bestIndex = -1;
		
		for (int i = 0; i < possibleMoves.length; i++) {
			// calculate vector x for each move
			
			double value = 0;
			
			if (canWin(possibleMoves[i])) {
				value = 1 /*- countPieces()/100.0*/;
			}
			
			putPiece(player, possibleMoves[i]);
			
			if (value == 0 && !isDraw()) {
				if (trainAgainstMinimax) {
					other.getBestMove(getBoard());
					value = -1 * other.lastBestValue;
				} else {
					// start using this part
					int[][] boardState = getBoard();
					int[][] mirroredState = getMirroredField(boardState);
					int[] indices = getIndices(boardState, mirroredState);
					
					// calculate dot product for each
					for (int j = 0; j < indices.length; j++) {
						value -= other.weights[indices[j]];
					}
					value = Math.tanh(value);
				}
			}
			
			if (value > bestValue) {
				bestValue = value;
				bestIndex = i;
			}
			
			
			removePiece(player, possibleMoves[i]);
			
		}
		
		// last best value
		lastBestValue = bestValue;
		
		// call TDL when training, otherwise return best move directly
		if (isTraining)
			return oneTDLIteration(possibleMoves[bestIndex], bestValue);
		return possibleMoves[bestIndex];
	}
	
	public void saveAgent(int fileEnd) {
		try {
			FileWriter out = new FileWriter("weights" + fileEnd + ".txt", true);
			out.append(numWeights + "\n");
			for(int i = 0; i < numWeights; i++) {
				out.append(weights[i] + "\n");
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
