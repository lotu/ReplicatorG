package replicatorg.app.ui.controlpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.MachineController;
import replicatorg.drivers.Driver;

public class ScriptPanel extends JPanel implements ActionListener
{
	protected MachineController machine;
	protected Driver driver;

	/**
	 * Create a Script-style button with the given name and tooltip.  By default, the
	 * action name is the same as the text of the button.  The button will emit an
	 * action event to the jog panel when it is clicked.
	 * @param text the text to display on the button.
	 * @param tooltip the text to display when the mouse hovers over the button.
	 * @return the generated button.
	 */
	protected JButton createScriptButton(String text, String tooltip) {
		final int buttonSize = 60;
		JButton b = new JButton(text);
		b.setToolTipText(tooltip);
		b.setMaximumSize(new Dimension(buttonSize, buttonSize));
		b.setPreferredSize(new Dimension(buttonSize, buttonSize));
		b.setMinimumSize(new Dimension(buttonSize, buttonSize));
		b.addActionListener(this);
		return b;
	}

	/**
	 * Create a Script-style button with the given name and tooltip.  The action
	 * name is specified by the caller.  The button will emit an
	 * action event to the jog panel when it is clicked.
	 * @param text the text to display on the button.
	 * @param tooltip the text to display when the mouse hovers over the button.
	 * @param action the string representing the action.
	 * @return the generated button.
	 */
	protected JButton createScriptButton(String text, String tooltip, String action) {
		JButton button = createScriptButton(text,tooltip);
		button.setActionCommand(action);
		return button;
	}

	/**
	 * The Gcode that will be executed for the various buttons.
	 */
	private ArrayList<ArrayList<String>> comands = new ArrayList<ArrayList<String>>();

	public ScriptPanel(MachineController machine) {
		this.machine = machine;
		this.driver = machine.getDriver();
		setLayout(new MigLayout());
		
		JPanel buttonPanel = new JPanel(new MigLayout("","[]0[]","[]0[]"));
		
		try {
			BufferedReader script = new BufferedReader( new FileReader("/home/hbullen/.replicator_scripts"));
			String line;
			int count = 0;
			while ((line = script.readLine()) != null ) {
				String fmt = "";  // default button format
				if ( (count + 1) % 4 == 0 ) // every 4th button but not the first
					fmt = "wrap";
				buttonPanel.add( createScriptButton( line, script.readLine(), String.valueOf(count) ), fmt);
				String cmd; 
				int i = 0;
				comands.add( new ArrayList<String>() );
				while ((cmd = script.readLine()) != null ){
					if ( cmd.equalsIgnoreCase("(END)") )
						break;
					comands.get(count).add(cmd);
					i++;
				}
				count++;
			}
		} catch ( IOException e )
		{
			// file not found or something
		}

		// add it all to our jog panel
		add(buttonPanel);

		// add jog panel border and stuff.
		setBorder(BorderFactory.createTitledBorder("Script Controls"));
	
	}
	
	public void actionPerformed(ActionEvent e) {
		Point3d current = driver.getCurrentPosition();
		String s = e.getActionCommand();
		int c = Integer.parseInt( s );

		if (comands.size() > c) {
			try {
				// run thru comands and execute one by one
				for( int i = 0 ; i < comands.get(c).size(); i++) {
					driver.parse( comands.get(c).get(i) );
					driver.execute();
				}
			} catch (Exception exe )
			{
				Base.logger.warning("Failed doing: " + s);
			}
		} else
			Base.logger.warning("Unknown Action Event: " + s);
		
	}
}
