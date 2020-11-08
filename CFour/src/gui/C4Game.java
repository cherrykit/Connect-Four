package gui;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.swing.JPanel;

import openingBook.BookSum;

import c4.AlphaBetaAgent;
import c4.ConnectFour;
import c4.MoveList;
import c4.TDLAgent;
import c4.Agent;

/**
 * Contains the "Logic" of the GUI
 * 
 * @author Markus Thill
 * 
 */
public class C4Game extends JPanel implements Runnable, ListOperation {

	private static final long serialVersionUID = -59357720973949917L;

	// the game buttons and text fields on the right of the window
	public C4Buttons c4Buttons;

	private Thread playThread = null;

	// Needed, for performing standard connect-four operations (finding
	// win-rows, check if legal move, etc.)
	protected ConnectFour c4 = new ConnectFour();

	// All moves during a game are stored in a movelist, so that moves can be
	// taken back or repeated again
	private MoveList mvList = new MoveList();

	// Because the process creates parallel running threads, sometime a
	// semaphore is needed to guarantee exclusive access on some objects
	private Semaphore mutex = new Semaphore(1);

	// The opening-books are loaded only once to save memory. All agents, that
	// need them, use the same books.
	private final BookSum books = new BookSum();

	// Possible states
	protected enum State {
		TRAIN, TRAIN_EVAL
	};

	protected State state;

	// Standard Alpha-Beta-Agent
	public AlphaBetaAgent alphaBetaStd = null;

	// Players
	protected Agent[] players = new Agent[3];
	private int curPlayer;
	private int winner;
	public boolean trainAgainstMinimax;

	// Evaluation settings, can change these later on
	private double prevScore1 = 0;
	private double prevScore2 = 0;
	private final int targetScore = 80;
	private final int evalInterval = 20000;
	private final int evalGames = 100;
	public String FILE_NAME = "results.csv";
	
	public double alpha;
	public double epsilon;

	
	private int numTrainingGames;

	// Flag that is set, when a game is won by a player or drawn.
	private boolean gameOver = false;

	public C4Game() {
		initGame();
	}

	public void init() {
		// Start a thread
		playThread = new Thread(this);
		playThread.start();
	}

	private void initGame() {
		c4Buttons = new C4Buttons(this);

		// Init the Standard Alpha-Beta-Agent
		// Until yet, there were no changes of the Options
		alphaBetaStd = new AlphaBetaAgent(books);
		alphaBetaStd.resetBoard();
		alphaBetaStd.setTransPosSize(3);
		alphaBetaStd.setBooks(true, false, true);
		alphaBetaStd.setDifficulty(100);
		alphaBetaStd.randomizeEqualMoves(true);
		
		winner = -1;
	}

	private boolean makeCompleteMove(int x, int player, String sPlayer) {
		int colheight = c4.getColHeight(x);
		if (!gameOver && colheight != 6) {
			checkWin(x, sPlayer);
			setPiece(x);
			swapPlayer();
			if (c4.isDraw() && !gameOver) {
				int col = mvList.readPrevMove();
				if (!c4.canWin(2, col, c4.getColHeight(col) - 1)) {
					gameOver = true;
				}
			}
			return true;
		}
		return false; // error
	}

	protected boolean makeCompleteMove(int x, String sPlayer) {
		return makeCompleteMove(x, curPlayer, sPlayer);
	}

	private void setPiece(int x, int player) {
		putPiece(x, player);
		mvList.putMove(x);
	}

	private void setPiece(int x) {
		setPiece(x, curPlayer);
	}

	private void putPiece(int x, int player) {
		c4.putPiece(player + 1, x);
	}

	protected void resetBoard() {
		c4.resetBoard();
		mvList.reset();
		gameOver = false;
		curPlayer = 0;
	}

	private void checkWin(int x, String sPlayer) {
		checkWin(curPlayer, x, sPlayer);
	}

	private void checkWin(int player, int x, String sPlayer) {
		if (c4.canWin(player + 1, x)) {
				winner = player;
			gameOver = true;
		}
	}

	// TODO: is there anywhere else we need to add support for players[2]?
	private void swapPlayer() {
		curPlayer = (state == State.TRAIN_EVAL ? 2 : 1) - curPlayer;
	}

	protected void setInitialBoard() {
		mvList.reset();
		resetBoard();
		curPlayer = 0;
	}

	private void initPlay() {
		curPlayer = 0;
		gameOver = false;
	}

	protected Agent initAlphaBetaAgent(int player) {
		return alphaBetaStd;
	}
	
	protected Agent initTDLAgent(boolean trainAgainstMinimax) {
		this.trainAgainstMinimax = trainAgainstMinimax;
		return new TDLAgent(trainAgainstMinimax, false, 0, alpha, epsilon);
	}
	
	protected String trainTDLAgent(int player) {
		TDLAgent curAgent = (TDLAgent) players[player];
		// Init opponent
		if (curAgent.trainAgainstMinimax) {
			players[1] = alphaBetaStd;
		} else {
			players[1] = new TDLAgent(false, true, 1, alpha, epsilon);
		}
		curAgent.isTraining = true;
		changeState(State.TRAIN);
		winner = -1;
		c4Buttons.cbAutostep.setSelected(true);
		numTrainingGames = 0;

		return "Started training";
	}

	// Evaluates agent in players[0] and returns the score
	public double evaluateAgent() {
		// TODO: do we need this?
		players[2] = alphaBetaStd;
		((TDLAgent)players[0]).isTraining = false;

		double score = 0;

		for(int i = 0; i < evalGames; ++i){
			winner = -1;
			resetBoard();
			playGame(true);
			switch(winner){
				case 0:
					score += 1;
					break;
				case 1:
					break;
				case -1:
					// TODO: what is winner in case of draw?
					score += 0.5;
					break;
			}
		}
		
		((TDLAgent)players[0]).isTraining = true;
		
		return score;
	}

	// ==============================================================
	// Button: Start Game
	// ==============================================================
	private void playGame(boolean newGame) {
		if (newGame) {
			initPlay();
		}
		while (true) {
			if (!gameOver) {
				int x = players[curPlayer].getBestMove(c4.getBoard());

				String[] color = { "Yellow", "Red", "Evaluation" };
				String sPlayer = players[curPlayer].getName() + " ("
						+ color[curPlayer] + ")";
				makeCompleteMove(x, sPlayer);
			}
			else {
				return;
			}
		}
	}

	protected void changeState(State st) {
		state = st;
	}

	public void setPlayStep(boolean value) {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mutex.release();
	}

	public void run() {
		System.out.println("Started training!");
		while (true) {
			state = State.TRAIN;
			numTrainingGames += 1;
			while (numTrainingGames % evalInterval != 0) {
				resetBoard();
				if (numTrainingGames % 1000 == 0) {
					System.out.println("Starting training game " + numTrainingGames);
				}
				playGame(true);
				((TDLAgent)players[0]).updateAlpha();
				if (!trainAgainstMinimax) {
					((TDLAgent)players[1]).updateAlpha();
				}
				numTrainingGames += 1;
			}
			System.out.println("Evaluating after game " + numTrainingGames);
			state = State.TRAIN_EVAL;
			
			double score = evaluateAgent();
			// write the output of each evaluation to a file
			FileWriter out;
			try {
				out = new FileWriter(FILE_NAME, true);
				out.append(numTrainingGames + "," + score + "\n");
				out.flush();
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			if (score >= targetScore && score <= prevScore1 && score <= prevScore2){
				System.out.println("Finished training!");
				return;
			}
			prevScore1 = prevScore2;
			prevScore2 = score;
		}
	}

	@Override
	public void indexChanged(int newIndex) {
		// not needed in this class yet
	}

	@Override
	public void playerChanged(Player player) {
		// not needed in this class yet
	}
}
