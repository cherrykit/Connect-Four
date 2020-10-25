package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Main Menu
 * 
 * @author Markus Thill
 * 
 */
public class C4Menu extends JMenuBar {

	private static final String TIPEVALUATE = "";

	private static final long serialVersionUID = -7247378225730684090L;

	private C4Game c4Game;

	// private JRadioButtonMenuItem rbMenuItem;
	// private JCheckBoxMenuItem cbMenuItem;
	private JFrame m_frame;
	private int selectedAgent = 0;

	C4Menu(C4Game game, JFrame frame) {
		c4Game = game;
		m_frame = frame;

		generateFileMenu();
		generateOptionsMenu();
		generateHelpMenu();

	}

	private void generateFileMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File Options");

		// ==============================================================
		// Quit Program
		// ==============================================================
		menuItem = new JMenuItem("Quit Program", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_frame.setVisible(false);
				m_frame.dispose();
				System.exit(0);
			}
		});
		menuItem.setToolTipText(TIPEVALUATE);
		menu.add(menuItem);

		add(menu);
	}

	private void generateOptionsMenu() {
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
				"Options for Competition");

		// ==============================================================
		// Options for Game-Theoretic Values
		// ==============================================================
		menuItem = new JMenuItem("Game-Theoretic Values");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				c4Game.c4Buttons.setWindowPos(c4Game.winOptionsGTV);
			}
		});
		menuItem.setToolTipText("<html><body>1. Set the Size of the Hash-Table "
				+ "for the Agent that determines the game-theoretic <br>"
				+ "values in a lot of situations. If the Hash-Size is made "
				+ "smaller more time is needed to calculate the score <br>"
				+ "for a position. <br> "
				+ "2.Choose which databases shall be used "
				+ "for the Agent that determines the game-theoretic values in a lot of <br>"
				+ "Situations. The usage of the databases needes more memory but"
				+ " fastens the agent a lot <br>"
				+ "3. Set the Search Depth for the Agent "
				+ "that determines the game-theoretic values. The Search-depth <br>"
				+ "should be predefined value</body></html>");
		menu.add(menuItem);

		add(menu);
	}

	private void generateHelpMenu() {
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Help");

		// ==============================================================
		// Options for Game-Theoretic Values
		// ==============================================================
		menuItem = new JMenuItem("Show Help-File");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.net.URL url = getClass().getResource("/doc/index.htm");
				ShowBrowser.openURL(url);
			}
		});
		menuItem.setToolTipText("<html><body>Show Help-File</body></html>");
		menu.add(menuItem);

		add(menu);
	}

	public int getSelectedAgent() {
		return selectedAgent;
	}

	public void setEnabledMenus(int[] menuList, boolean enable) {
		for (int i : menuList) {
			JMenu men = getMenu(i);
			men.setEnabled(enable);
		}
	}

}
