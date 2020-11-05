package c4;


public class TDLAgent extends ConnectFour implements Agent {
	
	// Add parameters we will use
	
	// save weights as field here

	// Returns whether we should stop training the agent
	// Evaluates agent and writes the result of each evaluation to a file
	// Ethan: implement this
	public boolean evaluateAgent(TDLAgent agent) {
		return false;
	}
	
	// one iteration of TDL
	// Darrel: implement this
	// maybe the agent needs to know if it's the first or second player
	// can use methods of TDLAgent to retrieve the best move at one point.
	public void oneTDLIteration(TDLAgent agent) {
		
	}	
	
	/**
	 * Generate an empty Board
	 */
	public TDLAgent() {
		super();
	}

	/**
	 * @param field
	 *            a 7x6 Connect-Four Board: 1 -> Player 1 (Beginner) 2 -> Player
	 *            2
	 */
	public TDLAgent(int field[][]) {
		super(field);
	}

	/**
	 * Create a new Board
	 * 
	 * @param fieldP1
	 *            BitBoard of Player1
	 * @param fieldP2
	 *            BitBoard of Player2
	 */
	public TDLAgent(long fieldP1, long fieldP2) {
		super(fieldP1, fieldP2);
	}


	public String getName() {
		return new String("TDL-Agent");
	}

	// Don't need this yet
	@Override
	public AgentState getAgentState() {
		// TODO Auto-generated method stub
		return null;
	}

	// Sophia: Need to implement this
	@Override
	public int getBestMove(int[][] table) {
		// TODO Auto-generated method stub
		return 0;
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
