package gui;

public class Console {

	public static final String TITLE = "Connect Four";
	
	private C4Game t_Game;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final Console console = new Console();

		 console.init(args);

	}

	/**
	 * Initialize the frame and {@link #t_Game}.
	 */
	private void init(String[] args) {
		if (args.length >= 3)
			t_Game.epsilon = Double.parseDouble(args[2]);
		else
			t_Game.epsilon = 0.1;
		if (args.length >= 2)
			t_Game.alpha = Double.parseDouble(args[1]);
		else
			t_Game.alpha = 0.004;
		if (args.length >= 1)
			t_Game.players[0] = t_Game.initTDLAgent(Boolean.parseBoolean(args[0]));
		else
			t_Game.players[0] = t_Game.initTDLAgent(false);
		t_Game.trainTDLAgent(0);
		t_Game.init();
	}

	private Console() {
		t_Game = new C4Game();
	}
	
}
