package gui;

import guiOptions.OptionsComp;
import guiOptions.OptionsMinimax;
import guiOptions.OptionsMultiTrain;
import guiOptions.OptionsTDL;
import guiOptions.OptionsValueFunc;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	public C4Menu c4Menu;
	protected C4Frame_v2_14 c4Frame;
	private Thread playThread = null;

	// The Panel including the board and value-bar on the left of the window
	private JPanel boardPanel;

	// The 42 fields of the connect-four board
	private JPanel playingBoardPanel;
	private ImgShowComponent playingBoard[][];

	// The value-panel
	private JLabel[][] valueBoard;

	// Needed, for performing standard connect-four operations (finding
	// win-rows, check if legal move, etc.)
	protected ConnectFour c4 = new ConnectFour();

	// All moves during a game are stored in a movelist, so that moves can be
	// taken back or repeated again
	private MoveList mvList = new MoveList();

	// Because the process creates parallel running threads, sometime a
	// semaphore is needed to guarantee exclusive access on some objects
	private Semaphore mutex = new Semaphore(1);

	// Labels for the Values
	JLabel lValueTitle;
	JLabel lValueGTV;
	JLabel lValueAgent;
	JLabel lValueEval;

	// The opening-books are loaded only once to save memory. All agents, that
	// need them, use the same books.
	private final BookSum books = new BookSum();

	// Possible states
	protected enum State {
		TRAIN, TRAIN_EVAL, PLAY, COMPETE, MULTICOMPETE, IDLE, SETBOARD, TESTVALUEFUNC, TESTBESTMOVE, SHOWNTUPLE /* unused */, SETNTUPLE, INSPNTUPLE, MULTITRAIN, EVALUATE, SAVE_X, SAVE_O, SAVE_EVAL, LOAD_X, LOAD_O, LOAD_EVAL, SAVE_WEIGHTS_X, SAVE_WEIGHTS_O, SAVE_WEIGHTS_EVAL, LOAD_WEIGHTS_X, LOAD_WEIGHTS_O, LOAD_WEIGHTS_EVAL
	};

	protected State state = State.IDLE;

	// Possible Actions
	protected enum Action {
		NOACTION, MOVEBACK, NEXTMOVE, DELETE, CHANGE
	};

	protected Action action = Action.NOACTION;

	// Standard Alpha-Beta-Agent
	public AlphaBetaAgent alphaBetaStd = null;

	// Players
	protected Agent[] players = new Agent[3];
	private int curPlayer;
	private int winner;
	private boolean trainAgainstMinimax;

	// Evaluation settings, can change these later on
	private double prevScore1 = 0;
	private double prevScore2 = 0;
	private final int targetScore = 80;
	private final int evalInterval = 50000;
	private final int evalGames = 100;
	private final String FILE_NAME = "results.csv";

	
	private int numTrainingGames;

	// Agent for the game-theoretic-values (perfect minimax-agent)
	private Agent GTVab = null;

	// Flag that is set, when a game is won by a player or drawn.
	private boolean gameOver = false;

	// Show gameTheoretic Values
	protected boolean showGTV = false;
	protected boolean showAgentV = false;
	protected boolean showAgentEvalV = false;

	// Other Windows
	protected OptionsMinimax winOptionsGTV = new OptionsMinimax(
			AlphaBetaAgent.TRANSPOSBYTES);
	protected OptionsComp winCompOptions = new OptionsComp();
	protected OptionsValueFunc winValueFuncOptions = new OptionsValueFunc();
	// Not needed anymore: protected ShowNTuples winNTupleWindow = new
	// ShowNTuples(this);
	protected OptionsMultiTrain winMultiTrainOptions = new OptionsMultiTrain();

	// Options-Windows for the current agent-type
	// params[0]:Player X
	// params[1]:Player O
	// params[2]:Player Eval
	protected final JFrame params[] = new JFrame[3];

	// For Setting N-Tuples manually
	protected ArrayList<ArrayList<Integer>> nTupleList = new ArrayList<ArrayList<Integer>>();

	// If the status-message was changed, this flag will be set, to indicate,
	// that the statusbar has to be updated
	protected boolean syncStatusBar = false;

	// Flag, that is set, when the Step-Button is selected. This causes the
	// current agent to make a move
	private boolean playStep = false;

	public C4Game() {
		initGame();
	}

	public C4Game(C4Frame_v2_14 frame) {
		c4Frame = frame;
		initGame();
	}

	public void init() {
		// Start a thread
		playThread = new Thread(this);
		playThread.start();
	}

	private void initGame() {
		playingBoard = new ImgShowComponent[7][6];
		valueBoard = new JLabel[3][7];
		c4Buttons = new C4Buttons(this);
		c4Menu = new C4Menu(this, c4Frame);

		playingBoardPanel = initPlayingBoard();

		lValueTitle = new JLabel("Overall Result of the Value-Function");
		lValueTitle.setFont(new Font("Times New Roman", 1, 18));
		lValueGTV = new JLabel("Hallo");
		lValueAgent = new JLabel("Hallo");
		lValueEval = new JLabel("Hallo");

		lValueGTV.setToolTipText("Value for the game-theoretic value");
		lValueAgent.setToolTipText("Value for the selected Agent");
		lValueEval.setToolTipText("Value for the Evaluation");

		JLabel Title;
		Title = new JLabel(C4Frame_v2_14.TITLE,
				JLabel.CENTER);
		Title.setForeground(Color.black);
		Font font = new Font("Times New Roman", 1, 18);
		Title.setFont(font);

		setLayout(new BorderLayout(10, 10));
		setBackground(Color.white);
		boardPanel = new JPanel();
		boardPanel.add(playingBoardPanel);

		boardPanel.setLayout(new GridBagLayout());
		JPanel z = initValuePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		boardPanel.add(z, c);

		c.gridy++;
		boardPanel.add(lValueTitle, c);

		c.gridy++;
		boardPanel.add(lValueGTV, c);

		c.gridy++;
		boardPanel.add(lValueAgent, c);

		c.gridy++;
		boardPanel.add(lValueEval, c);

		add(c4Buttons, BorderLayout.EAST);
		add(boardPanel, BorderLayout.CENTER);
		add(Title, BorderLayout.NORTH);

		changeState(State.IDLE);

		printCurAgents();

		// Init the Standard Alpha-Beta-Agent
		// Until yet, there were no changes of the Options
		OptionsMinimax min = winOptionsGTV;
		alphaBetaStd = new AlphaBetaAgent(books);
		alphaBetaStd.resetBoard();
		alphaBetaStd.setTransPosSize(min.getTableIndex());
		alphaBetaStd.setBooks(min.useNormalBook(), min.useDeepBook(),
				min.useDeepBookDist());
		alphaBetaStd.setDifficulty(min.getSearchDepth());
		alphaBetaStd.randomizeEqualMoves(min.randomizeEqualMoves());
		
		winner = -1;
	}

	private JPanel initValuePanel() {
		JPanel z = new JPanel();
		z.setLayout(new GridLayout(1, 7, 2, 2));
		z.setBackground(Color.black);
		for (int i = 0; i < 7; i++) {
			JPanel vb = new JPanel();
			vb.setLayout(new GridLayout(3, 0, 2, 2));
			vb.setBackground(Color.orange);
			for (int j = 0; j < 3; j++) {
				valueBoard[j][i] = new JLabel("0.0", JLabel.CENTER);
				valueBoard[j][i].setBackground(Color.orange);
				vb.add(valueBoard[j][i]);
			}
			valueBoard[0][i].setToolTipText("Values for the single columns "
					+ "of the current board (game-theoretic value)");
			valueBoard[1][i].setToolTipText("Values for the single columns "
					+ "of the current board (selected Agent)");
			valueBoard[2][i].setToolTipText("Values for the single columns "
					+ "of the current board (Evaluation)");
			z.add(vb);
		}
		return z;
	}

	private JPanel initPlayingBoard() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 7, 2, 2));
		panel.setBackground(Color.BLACK);

		for (int i = 0; i < 42; i++)
			panel.add(new Canvas());

		// Add Playing Field
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				playingBoard[i][j] = replaceImage(playingBoard[i][j],
						ImgShowComponent.EMPTY0, i, j);
				panel.remove((5 - j) * 7 + i);
				panel.add(playingBoard[i][j], (5 - j) * 7 + i);
			}
		}

		return panel;
	}

	private ImgShowComponent replaceImage(ImgShowComponent oldImg,
			int imgIndex, int num1, int num2) {
		ImgShowComponent imgShComp = ImgShowComponent.replaceImg(oldImg,
				imgIndex);
		if (oldImg != imgShComp)
			imgShComp.addMouseListener(new MouseHandler(num1, num2) {
				public void mouseClicked(MouseEvent e) {
					handleMouseClick(x, y);
				}
			});
		return imgShComp;
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
					new MessageBox("Draw!!!       ", "Game Over");
				}
			}
			if (!gameOver)
				printValueBar();
			return true;
		}
		return false; // error
	}

	protected boolean makeCompleteMove(int x, String sPlayer) {
		return makeCompleteMove(x, curPlayer, sPlayer);
	}

	private void setPiece(int x, int player) {
		putPiece(x, player);
		int last = mvList.readPrevMove();
		if (last != -1) {
			int i = last;
			int j = c4.getColHeight(i) - 1;
			if (x == i)
				j--;
			unMarkMove(i, j, player);
		}

		mvList.putMove(x);
	}

	private void setPiece(int x) {
		setPiece(x, curPlayer);
	}

	private void putPiece(int x, int player) {
		int y = c4.getColHeight(x);
		c4.putPiece(player + 1, x);
		markMove(x, y, player);
	}

	private void markMove(int x, int y, int player) {
		int imgIndex = (player == 0 ? ImgShowComponent.YELLOW_M
				: ImgShowComponent.RED_M);
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);

		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	private void unMarkMove(int x, int y, int player) {
		int imgIndex = (player == 0 ? ImgShowComponent.RED
				: ImgShowComponent.YELLOW);
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);
		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	private void removePiece(int x, int player) {
		int y = c4.getColHeight(x) - 1;
		c4.removePiece(player + 1, x);
		removePiece(x, y, player);
	}

	private void removePiece(int x, int y, int player) {
		int imgIndex = ImgShowComponent.EMPTY0;
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);
		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	protected void resetBoard() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				int imgIndex = ImgShowComponent.EMPTY0;
				ImgShowComponent newImg = replaceImage(playingBoard[i][j],
						imgIndex, i, j);
				if (newImg != playingBoard[i][j]) {
					playingBoardPanel.remove((5 - j) * 7 + i);
					playingBoard[i][j] = newImg;
					playingBoardPanel.add(playingBoard[i][j], (5 - j) * 7 + i);
					playingBoardPanel.invalidate();
					playingBoardPanel.validate();
				}
			}
		}
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
			if (sPlayer != null && state == State.PLAY)
				new MessageBox(sPlayer + " Win!!       ", "Game Over");
			else if (state == State.PLAY)
				new MessageBox("Game Over!!!", "Game Over");
			else
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
		curPlayer = c4.countPieces() % 2;
		gameOver = false;
	}

	@SuppressWarnings("unused")
	private void printColValues(Agent pa) {
		double[] vals = pa.getNextVTable(c4.getBoard(), false);
		for (int i = 0; i < vals.length; i++)
			valueBoard[0][i].setText(vals[i] + "");
	}

	private double[] getGTV() {
		if (GTVab == null)
			GTVab = initGTVAgent();
		double[] vals = GTVab.getNextVTable(c4.getBoard(), true);
		return vals;
	}

	private double getSingleGTV() {
		if (GTVab == null)
			GTVab = initGTVAgent();
		return GTVab.getScore(c4.getBoard(), true);
	}

	protected void printValueBar() {
		new Thread("") {
			public void run() {
				double[] realVals = new double[7];
				double[] agentvals = new double[7];
				double[] evalVals = new double[7];

				// Reset Labels
				lValueAgent.setText("Agent: ");
				lValueEval.setText("Eval: ");
				lValueGTV.setText("GTV: ");

				Agent pa = null;
				if (showAgentV)
					pa = players[curPlayer];

				boolean useSigmoid = false;

				if (pa != null) {
					agentvals = pa.getNextVTable(c4.getBoard(), false);
					int val = (int) (pa.getScore(c4.getBoard(), true) * 100);
					lValueAgent.setText("Agent:     " + val);
				}

				if (agentvals != null)
					for (int i = 0; i < agentvals.length; i++)
						if (Math.abs(agentvals[i]) > 1.0) {
							useSigmoid = true;
							break;
						}

				if (useSigmoid && agentvals != null)
					for (int i = 0; i < agentvals.length; i++)
						agentvals[i] = Math.tanh(agentvals[i]);
				if (showGTV) {
					realVals = getGTV();
					String valGTV = "GTV:        "
							+ (int) (getSingleGTV() * 100) + "";
					lValueGTV.setText(valGTV);
					lValueGTV.setPreferredSize(getMaximumSize());
				}
				if (showAgentEvalV && players[2] != null) {
					evalVals = players[2].getNextVTable(c4.getBoard(), true);
					int val = (int) (players[2].getScore(c4.getBoard(), true) * 100);
					lValueEval.setText("Eval:        " + val + "");
				}
				for (int i = 0; i < 7; i++) {
					if (realVals != null)
						valueBoard[0][i]
								.setText((int) (realVals[i] * 100) + "");
					if (agentvals != null)
						valueBoard[1][i].setText((int) (agentvals[i] * 100)
								+ "");
					if (evalVals != null)
						valueBoard[2][i]
								.setText((int) (evalVals[i] * 100) + "");
				}
				System.gc();
			}
		}.start();
	}

	private void printCurAgents() {
		new Thread("") {
			public void run() {
				int x = 0;
				while (true) {
					c4Buttons.printCurAgents(players);
					if (syncStatusBar) {
						x++;
						if (x == 7) {
							syncStatusBar = false;
							x = 0;
						}
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!syncStatusBar) {
						c4Buttons.setProgressBar(0);
					}
				}
			}
		}.start();
	}

	private Agent initGTVAgent() {
		if (!winOptionsGTV.usePresetting()) {
			AlphaBetaAgent ab = new AlphaBetaAgent(books);
			ab.resetBoard();
			// New
			OptionsMinimax min = winOptionsGTV;
			ab.setTransPosSize(min.getTableIndex());
			ab.setBooks(min.useNormalBook(), min.useDeepBook(),
					min.useDeepBookDist());
			ab.setDifficulty(min.getSearchDepth());
			ab.randomizeEqualMoves(min.randomizeEqualMoves());
			return ab;
		}
		return alphaBetaStd;
	}

	protected Agent initAlphaBetaAgent(int player) {
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsMinimax.class))
			params[player] = new OptionsMinimax(AlphaBetaAgent.TRANSPOSBYTES);
		OptionsMinimax min = (OptionsMinimax) params[player];
		if (!min.usePresetting()) {
			AlphaBetaAgent ab = new AlphaBetaAgent(books);
			ab.resetBoard();

			ab.setTransPosSize(min.getTableIndex());
			ab.setBooks(min.useNormalBook(), min.useDeepBook(),
					min.useDeepBookDist());
			ab.setDifficulty(min.getSearchDepth());
			ab.randomizeEqualMoves(min.randomizeEqualMoves());

			// Using N-Tuple-System for move-Ordering
			// ab.setTDAgent((TDSAgent) players[2]);
			return ab;
		}
		return alphaBetaStd;
	}
	
	protected Agent initTDLAgent(int player) {
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsTDL.class))
			params[player] = new OptionsTDL();
		OptionsTDL min = (OptionsTDL) params[player];
		trainAgainstMinimax = min.playAgainstMinimax();
		
		return new TDLAgent(trainAgainstMinimax, false, 0, 0.004, 0.1);
	}
	
	protected String trainTDLAgent(int player) {
		TDLAgent curAgent = (TDLAgent) players[player];
		// Init opponent
		if (curAgent.trainAgainstMinimax) {
			players[1] = alphaBetaStd;
		} else {
			players[1] = new TDLAgent(false, true, 1, 0.004, 0.1);
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
		//printValueBar();

		while (state == State.PLAY || state == State.TRAIN || state == State.TRAIN_EVAL) {
			// Check for Actions
			handleAction();

			if (players[curPlayer] == null && !gameOver) {

			} else if (!gameOver) {
				c4Buttons.setEnabledPlayStep(players[curPlayer] != null);
				boolean autoMode = c4Buttons.cbAutostep.isSelected();
				if (playStep || autoMode) {
					setPlayStep(false);
					c4Buttons.setEnabledPlayStep(false);

					long startTime = System.currentTimeMillis();
					int x = players[curPlayer].getBestMove(c4.getBoard());

					float timeS = (float) ((System.currentTimeMillis() - startTime) / 1000.0);
					if (state == State.PLAY)
						c4Buttons.printStatus("Time needed for move: " + timeS
							+ "s");

					String[] color = { "Yellow", "Red", "Evaluation" };
					String sPlayer = players[curPlayer].getName() + " ("
							+ color[curPlayer] + ")";
					makeCompleteMove(x, sPlayer);
					if (state == State.PLAY) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
					
					//printValueBar();
				}
			}
			else if (gameOver && (state == State.TRAIN || state == State.TRAIN_EVAL)) {
				return;
			}
			if (state == State.PLAY) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}

		}
		if (state == State.PLAY)
			changeState(State.IDLE);
	}

	// ==============================================================
	// Button: Move Back
	// ==============================================================
	private void moveBack() {
		if (!mvList.isEmpty()) {
			gameOver = false;
			int col = mvList.getPrevMove();
			swapPlayer();
			removePiece(col, curPlayer);
			col = mvList.readPrevMove();
			if (col != -1)
				markMove(col, c4.getColHeight(col) - 1, 1 - curPlayer);
			c4Buttons.setEnabledPlayStep(players[curPlayer] != null);
			printValueBar();
		}
	}

	// ==============================================================
	// Button: Next Move
	// ==============================================================
	private void nextMove() {
		if (mvList.isNextMove()) {
			int prevCol = mvList.readPrevMove();
			if (prevCol != -1)
				unMarkMove(prevCol, c4.getColHeight(prevCol) - 1, curPlayer);
			int col = mvList.getNextMove();
			if (c4.canWin(curPlayer + 1, col))
				gameOver = true;
			putPiece(col, curPlayer);
			swapPlayer();
			c4Buttons.setEnabledPlayStep(players[curPlayer] != null);
			printValueBar();
		}

	}

	protected void changeState(State st) {
		c4Buttons.enableItems(st);
		state = st;
	}

	public void setPlayStep(boolean value) {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		playStep = value;
		mutex.release();
	}

	public void run() {
		while (true) {
			// Deactivate most menu-items (except File and Help) for the
			// different states
			c4Menu.setEnabledMenus(new int[] { 1 }, false);
			switch (state) {
			case IDLE:
				c4Menu.setEnabledMenus(new int[] { 1 }, true);
				action = Action.NOACTION;
				break;
			case PLAY:
				// compareTime();
				playGame(true);
				break;
			case TRAIN:
				resetBoard();
				numTrainingGames += 1;
				c4Buttons.printStatus("Training game " + numTrainingGames);
				playGame(true);
				((TDLAgent)players[0]).updateAlpha();
				if (!trainAgainstMinimax) {
					((TDLAgent)players[1]).updateAlpha();
				}
				if(numTrainingGames % evalInterval == 0){
					c4Buttons.printStatus("Evaluating after game " + numTrainingGames);
					changeState(State.TRAIN_EVAL);
				}
				break;
			case TRAIN_EVAL:
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
					c4Buttons.printStatus("Finished training!");
					changeState(State.IDLE);
				} else {
					prevScore1 = prevScore2;
					prevScore2 = score;
					changeState(State.TRAIN);
				}
				break;
			default:
				break;
			}
			handleAction();

			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
		}
	}

	private void handleAction() {
		switch (action) {
		case MOVEBACK:
			moveBack();
			action = Action.NOACTION;
			break;
		case NEXTMOVE:
			nextMove();
			action = Action.NOACTION;
			break;
		default:
			break;
		}
	}

	private void handleMouseClick(int x, int y) {
		switch (state) {
		case PLAY:
			if (players[curPlayer] == null && !gameOver)
				makeCompleteMove(x, "You");
			break;
		case SETBOARD:
			makeCompleteMove(x, "HuiBuh");
			break;
		default:
			break;
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

	private class MouseHandler implements MouseListener {
		int x, y;

		MouseHandler(int num1, int num2) {
			x = num1;
			y = num2;
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
}
