package gui;

import gui.C4Game.State;
import guiOptions.OptionsMinimax;
import guiOptions.OptionsTDL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import c4.Agent.AgentState;
import c4.AlphaBetaAgent;
import c4.Agent;

/**
 * Class containing all the Buttons on the right panel of the main-window
 * 
 * @author Markus Thill
 * 
 */
public class C4Buttons extends JPanel {

	private static final long serialVersionUID = 1L;

	StatusBar statusBar = new StatusBar();

	C4Game m_game;
	C4Buttons buttons;
	private final String agentList[] = { "Human", "Minimax", "TDLAgent", "Random" };

	// ==================================================================
	// Elements
	// ==================================================================
	private JLabel lPlayerX;
	private JLabel lPlayerO;
	private JLabel lEvaluation;
	private JLabel lChooseOpponents;
	private JLabel lGame;
	private JLabel lTraining;
	private JLabel lprintVals;
	private JLabel lEval;
	private JLabel lSetInitialBoard;
	private JLabel lCurrentAgents;
	private JLabel lAgent0;
	private JLabel lAgent1;
	private JLabel lAgent2;

	private JButton bPlay;
	private JButton bStopGame;
	private JButton bStep;
	private JButton bMoveBack;
	private JButton bNextMove;
	private JButton bParamsX;
	private JButton bParamsO;
	private JButton bParamsEval;
	private JButton bInitX;
	private JButton bInitO;
	private JButton bInitEval;
	private JButton bTrainX;
	private JButton bTrainO;
	private JButton bTrainEval;
	private JButton bMakeNextMoveEval;
	private JButton bSetInitialBoard;
	private JButton bResetBoard;

	protected JComboBox<String> cChooseX;
	protected JComboBox<String> cChooseO;
	protected JComboBox<String> cChooseEval;

	private JCheckBox cbShowGTV;
	private JCheckBox cbShowAgentV;
	private JCheckBox cbShowEvalV;
	protected JCheckBox cbAutostep;

	JProgressBar progress;

	JScrollPane listScrollPane;

	protected C4Buttons(C4Game game) {
		// ==============================================================
		// Inits
		// ==============================================================
		m_game = game;
		buttons = this;

		// ==============================================================
		// Init Elements
		// ==============================================================

		lPlayerX = new JLabel("Player X", JLabel.CENTER);
		lPlayerX.setForeground(Color.yellow);
		lPlayerX.setFont(new Font("Times New Roman", Font.BOLD, 18));

		lPlayerO = new JLabel("Player O", JLabel.CENTER);
		lPlayerO.setForeground(Color.red);
		lPlayerO.setFont(new Font("Times New Roman", Font.BOLD, 18));

		lEvaluation = new JLabel("Evaluation", JLabel.CENTER);
		lEvaluation.setForeground(Color.black);
		lEvaluation.setFont(new Font("Times New Roman", Font.BOLD, 18));

		lChooseOpponents = new JLabel("choose opponents:");

		cChooseX = new JComboBox<String>(agentList);
		cChooseX.setSelectedIndex(0);

		cChooseO = new JComboBox<String>(agentList);
		cChooseO.setSelectedIndex(0);

		cChooseEval = new JComboBox<String>(Arrays.copyOfRange(agentList, 0, 4));
		cChooseEval.setSelectedIndex(0);

		bPlay = new JButton("Start Game");
		bStopGame = new JButton("Stop Game");
		bStep = new JButton("Step");

		lGame = new JLabel("Game: ");
		bMoveBack = new JButton("Move Back");
		bNextMove = new JButton("Next Move");

		lTraining = new JLabel("Training:");
		bParamsX = new JButton("Params X");
		bParamsO = new JButton("Params O");
		bParamsEval = new JButton("Params Eval");

		bInitX = new JButton("Init X");
		bInitO = new JButton("Init O");
		bInitEval = new JButton("Init Eval");

		bTrainX = new JButton("Train X");
		bTrainO = new JButton("Train O");
		bTrainEval = new JButton("Train Eval");

		lCurrentAgents = new JLabel("Current Agents:");
		lAgent0 = new JLabel("None.");
		lAgent0.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lAgent1 = new JLabel("None.");
		lAgent1.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lAgent2 = new JLabel("None.");
		lAgent2.setFont(new Font("Times New Roman", Font.BOLD, 14));

		lEval = new JLabel("Evaluation: ");
		bMakeNextMoveEval = new JButton("Make Move");

		lprintVals = new JLabel("Print Value-Bar:");
		cbShowGTV = new JCheckBox("Theoretic Vals");

		cbShowGTV.setBorderPainted(false);
		// cbShowGTV.b

		cbShowAgentV = new JCheckBox("Agent Vals");
		cbShowEvalV = new JCheckBox("Eval Vals");

		cbAutostep = new JCheckBox("Autostep");

		lSetInitialBoard = new JLabel("Initial Board:");
		bSetInitialBoard = new JButton("Set");
		bResetBoard = new JButton("Reset");
		progress = new JProgressBar(0, 100);

		// ==============================================================
		// Add Tool-Tips
		// ==============================================================

		bPlay.setToolTipText("<html><body>Start a new Math between the two players. "
				+ "If no Agent is selected, the moves are made by a Human.<br> "
				+ "The Agents must be trained (Button Train) to work, even "
				+ "if it is a Minimax-agent and the like</body></html>");

		bStopGame
				.setToolTipText("<html><body>Stop the current game. The board is "
						+ "going to be reseted an a new match can be started</body></html>");
		bMoveBack
				.setToolTipText("<html><body>Take a move back during a game. If"
						+ "the opponents are two humans, then always ONE piece <br>"
						+ "will be taken from the Board. If one opponent is an "
						+ "agent, then always two pieces will be taken from the <br>"
						+ "board, so that it's the human players move. If two \n"
						+ "Agents are playing against each other, then this <br>"
						+ "Button has no function</body></html>");

		bNextMove.setToolTipText("<html><body>Opposite of \"Move Back\". If"
				+ "the opponents are two humans, then always ONE piece <br>"
				+ "will be put from the Board. If one opponent is an "
				+ "agent, then always two pieces will be put on the <br>"
				+ "board. If two \nAgents are playing against each other, "
				+ "then this <br>Button has no function</body></html>");

		bParamsX.setToolTipText("<html><body>Set the parameters for the selected agent. "
				+ "Changes will only then be updated if the Init-Button is <br>"
				+ "used to re-init the agent.</body></html>");

		bParamsO.setToolTipText("<html><body>Set the parameters for the selected agent. "
				+ "Changes will only then be updated if the Init-Button is <br>"
				+ "used to re-init the agent.</body></html>");

		bParamsEval
				.setToolTipText("<html><body>Set the parameters for the selected agent. "
						+ "Changes will only then be updated if the Init-Button is <br>"
						+ "used to re-init the agent.</body></html>");

		bInitX.setToolTipText("<html><body>Init the selected Agent. This has to "
				+ "be done for all agents. If Parameters like the used N-Tuples change, <br>"
				+ "then the agent also must be initialized again! </body></html>");

		bInitO.setToolTipText("<html><body>Init the selected Agent. This has to "
				+ "be done for all agents.  If Parameters like the used N-Tuples change, <br>"
				+ "then the agent also must be initialized again!</body></html>");

		bInitEval
				.setToolTipText("<html><body>Init the selected Agent. This has to "
						+ "be done for all agents.  If Parameters like the used N-Tuples change, <br>"
						+ "then the agent also must be initialized again!</body></html>");

		bMakeNextMoveEval
				.setToolTipText("Let the Evaluation make a move during a game. ");

		bSetInitialBoard
				.setToolTipText("Set a initial Board for Games and competitions.");

		bResetBoard
				.setToolTipText("Reset the initial Board for Games and competitions.");

		bTrainX.setToolTipText("<html><body>Train the selected Agent. This has to "
				+ "be done for all TD-agents with a training-routine <br>. If no training routine is "
				+ "found, then the agent will be re-initialized</body></html>");

		bTrainO.setToolTipText("<html><body>Train the selected Agent. This has to "
				+ "be done for all TD-agents with a training-routine <br>. If no training routine is "
				+ "found, then the agent will be re-initialized</body></html>");

		bTrainEval
				.setToolTipText("<html><body>Train the selected Agent. This has to "
						+ "be done for all TD-agents with a training-routine <br>. If no training routine is "
						+ "found, then the agent will be re-initialized</body></html>");

		bStep.setToolTipText("<html><body>Makes a move during a game for the current agent.</body></html>");

		// ==============================================================
		// Place Elements on the Panel
		// ==============================================================
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 4, 10, 10));
		p.setPreferredSize(new Dimension(480, 300));

		p.add(new JLabel(""));
		p.add(lPlayerX);
		p.add(lPlayerO);
		p.add(lEvaluation);

		p.add(lChooseOpponents);
		p.add(cChooseX);
		p.add(cChooseO);
		p.add(cChooseEval);

		p.add(lTraining);
		p.add(bParamsX);
		p.add(bParamsO);
		p.add(bParamsEval);

		p.add(new JLabel(""));
		p.add(bInitX);
		p.add(bInitO);
		p.add(bInitEval);

		p.add(new JLabel(""));
		p.add(bTrainX);
		p.add(bTrainO);
		p.add(bTrainEval);

		p.add(lCurrentAgents);
		p.add(lAgent0);
		p.add(lAgent1);
		p.add(lAgent2);

		p.add(lprintVals);
		p.add(cbShowGTV);
		p.add(cbShowAgentV);
		p.add(cbShowEvalV);

		p.add(lSetInitialBoard);
		p.add(bSetInitialBoard);
		p.add(bResetBoard);
		p.add(new JLabel(""));

		p.add(lGame);
		p.add(bPlay);
		p.add(bStopGame);
		p.add(cbAutostep);

		p.add(new JLabel(""));
		p.add(bMoveBack);
		p.add(bNextMove);
		p.add(bStep);

		p.add(lEval);
		p.add(bMakeNextMoveEval);
		p.add(new JLabel(""));
		p.add(new JLabel(""));

		// ==============================================================
		// Add Panel to Scroll-Pane
		// ==============================================================
		listScrollPane = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// =====================================

		// ==============================================================
		// Status-Bar
		// ==============================================================
		JPanel s = new JPanel();
		s.setLayout(new GridLayout2(0, 1, 10, 10));
		s.add(progress);
		s.add(statusBar);
		statusBar.setMessage("Init done.");

		setLayout(new BorderLayout(10, 10));

		add(listScrollPane, BorderLayout.CENTER);
		add(s, java.awt.BorderLayout.SOUTH);
	}

	protected boolean isToBeInitialized(int index) {
		Agent p = m_game.players[index];
		if (p != null) {
			if (p.getAgentState() == AgentState.INITIALIZED
					|| p.getAgentState() == AgentState.TRAINED) {
				int sel = JOptionPane
						.showConfirmDialog(
								null,
								"Agent is already initialzed/trained. Are you sure that you want to initialize again? This will reset the whole agent.",
								"Warning!", JOptionPane.YES_NO_OPTION);
				return (sel == JOptionPane.YES_OPTION);
			}
		}
		return true;
	}

	protected boolean isToBeTrained(int index) {
		Agent p = m_game.players[index];
		if (p != null) {
			if (p.getAgentState() == AgentState.TRAINED) {
				int sel = JOptionPane
						.showConfirmDialog(
								null,
								"Agent is already trained. Are you sure that you want to train again? This will not reset the agent, but the training will be continued based on the current parameter-settings.",
								"Warning!", JOptionPane.YES_NO_OPTION);
				return (sel == JOptionPane.YES_OPTION);
			}
		}
		return true;
	}

	protected StatusBar getStatusBar() {
		return statusBar;
	}

	protected void setWindowPos(Window obj) {
		obj.setVisible(!obj.isVisible());
		int x = m_game.getLocation().x + m_game.getWidth() + 8;
		int y = m_game.getLocation().y;
		obj.setLocation(x, y);
	}

	protected void initValueBar() {
	}

	protected void openParams(int choiceIndex, int agent) {
		for (int i = 0; i < 3; i++)
			if (m_game.params[i] != null && i != agent)
				m_game.params[i].setVisible(false);

		switch (choiceIndex) {
		case 1:
		case 4:
			if (m_game.params[agent] == null
					|| !m_game.params[agent].getClass().equals(
							OptionsMinimax.class))
				m_game.params[agent] = new OptionsMinimax(
						AlphaBetaAgent.TRANSPOSBYTES);
			break;
		case 2:
			if (m_game.params[agent] == null || !m_game.params[agent].getClass().equals(
					OptionsTDL.class))
				m_game.params[agent] = new OptionsTDL();
			break;
		case 0:
		default:
			m_game.params[agent] = null;
			break;

		}
		if (m_game.params[agent] != null) {
			setWindowPos(m_game.params[agent]);
		}
	}

	protected void enableItems(State st) {
		switch (st) {
		case TRAIN_EVAL:
		case TRAIN:
			cChooseEval.setEnabled(false);
			bParamsEval.setEnabled(false);
			bInitEval.setEnabled(false);
			bTrainEval.setEnabled(false);
			break;
		default:
			break;
		}
	}

	protected void setEnabledPlayStep(boolean enabled) {
		if (!cbAutostep.isSelected())
			bStep.setEnabled(enabled);
		else
			bStep.setEnabled(false);
	}

	protected void printCurAgents(Agent pa[]) {
		String none = "None / Human.";
		if (pa == null) {
			lAgent0.setText(none);
			lAgent1.setText(none);
			lAgent2.setText(none);
		} else {
			if (pa[0] != null)
				lAgent0.setText(pa[0].getName());
			else
				lAgent0.setText(none);
			if (pa[1] != null)
				lAgent1.setText(pa[1].getName());
			else
				lAgent1.setText(none);
			if (pa[2] != null)
				lAgent2.setText(pa[2].getName());
			else
				lAgent2.setText(none);
		}
	}

	protected void setProgressBar(int value) {
		progress.setValue(value);
	}

	public class StatusBar extends JTextArea {
		private static final long serialVersionUID = 1L;

		/** Creates a new instance of StatusBar */
		public StatusBar() {
			super();
			super.setPreferredSize(new Dimension(200, 46));
			setMessage("Ready");
		}

		public void setMessage(String message) {
			setText(" " + message);
		}
	}

	public void printStatus(String str) {
		System.out.println(str);
		statusBar.setMessage(str);
	}

}
