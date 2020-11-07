package gui;

public class Console {

	public static final String TITLE = "Connect Four";
	
	private C4Game t_Game;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final Console console = new Console();

		 console.init();

	}

	/**
	 * Initialize the frame and {@link #t_Game}.
	 */
	private void init() {
		t_Game.players[0] = t_Game.initTDLAgent(0);
		t_Game.trainTDLAgent(0);
		t_Game.init();
	}

	private Console() {
		t_Game = new C4Game();
	}
	
}
