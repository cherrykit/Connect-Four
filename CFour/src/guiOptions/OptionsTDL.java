package guiOptions;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import gui.GridLayout2;

public class OptionsTDL extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Label lTitle;
	
	Button ok;
	OptionsTDL m_par;

	Checkbox cbAgentX;
	Checkbox cbAgentO;
	Checkbox cbAgentEval;

	public OptionsTDL() {
		super("Parameters for TDL Agent");

		lTitle = new Label("Default: Train through self-play");
		lTitle.setFont(new Font("Times New Roman", Font.BOLD, 14));

		cbAgentX = new Checkbox("Train against Minimax instead");
		cbAgentX.setState(false);
		cbAgentX.setEnabled(true);
		
		ok = new Button("OK");
		m_par = this;
		//
		
		ok.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						m_par.setVisible(false);
					}
				}					
		);
		
		setLayout(new GridLayout2(13,1,10,10));

		add(lTitle);
		add(cbAgentX);
		add(ok);

		pack();
		setVisible(false);
		
	}
	
	public boolean playAgainstMinimax() {
		return cbAgentX.getState();
	}
	
}
