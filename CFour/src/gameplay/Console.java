package gameplay;

public class Console {

	public static final String TITLE = "Connect Four";
	
	private C4Game[] t_Game;
	private int numInstances;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final Console console = new Console();

		 console.init(args);

	}

	/**
	 * Initialize training and the game play framework
	 */
	private void init(String[] args) {
		for(int i = 0; i < numInstances; i++) {
			t_Game[i] = new C4Game();
			if (args.length >= 3)
				t_Game[i].epsilon = Double.parseDouble(args[2]);
			else
				t_Game[i].epsilon = 0.1;
			if (args.length >= 2)
				t_Game[i].alpha = Double.parseDouble(args[1]);
			else
				t_Game[i].alpha = 0.004;
			if (args.length >= 1)
				t_Game[i].players[0] = t_Game[i].initTDLAgent(Boolean.parseBoolean(args[0]));
			else
				t_Game[i].players[0] = t_Game[i].initTDLAgent(false);
			t_Game[i].FILE_NAME = "results_" + t_Game[i].trainAgainstMinimax + 
					"_" + t_Game[i].alpha + "_" + t_Game[i].epsilon + "_#" + i + ".csv";
			t_Game[i].trainTDLAgent(0);
			t_Game[i].init();
		}
	}

	private Console() {
		//numInstances = Runtime.getRuntime().availableProcessors();
		numInstances = 1;
		t_Game = new C4Game[numInstances];
	}
	
}
