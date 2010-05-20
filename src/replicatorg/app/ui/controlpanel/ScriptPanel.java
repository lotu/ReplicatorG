package replicatorg.app.ui.controlpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public ScriptPanel(MachineController machine) {
		this.machine = machine;
		this.driver = machine.getDriver();
		setLayout(new MigLayout());
		
		JButton xPlusButton = createScriptButton("X+", "Script X axis in positive direction");
		JButton xMinusButton = createScriptButton("X-", "Script X axis in negative direction");
		JButton xCenterButton = createScriptButton("<html><center>Center<br/>X", "Script X axis to the origin","Center X");
		JButton yPlusButton = createScriptButton("Y+", "Script Y axis in positive direction");
		JButton yMinusButton = createScriptButton("Y-", "Script Y axis in negative direction");
		JButton yCenterButton = createScriptButton("<html><center>Center<br/>Y", "Script Y axis to the origin","Center Y");
		JButton zPlusButton = createScriptButton("Z+", "Script Z axis in positive direction");
		JButton zMinusButton = createScriptButton("Z-", "Script Z axis in negative direction");
		JButton zCenterButton = createScriptButton("<html><center>Center<br/>Z", "Script Z axis to the origin","Center Z");
		JButton zeroButton = createScriptButton("<html><center>Set<br/>zero","Mark Current Position as Zero (0,0,0)","Zero");

		JPanel xyzPanel = new JPanel(new MigLayout("","[]0[]","[]0[]"));
        xyzPanel.add(zCenterButton, "split 3,flowy,gap 0 0 0 0");
		xyzPanel.add(xMinusButton, "gap 0 0 0 0");
        xyzPanel.add(yCenterButton);
		xyzPanel.add(yPlusButton, "split 3,flowy,gap 0 0 0 0");
		xyzPanel.add(zeroButton,"gap 0 0 0 0");
		xyzPanel.add(yMinusButton);
		xyzPanel.add(xPlusButton,"split 2, flowy, aligny bottom, gap 0 0 0 0, gapafter 10");
        xyzPanel.add(xCenterButton);
		xyzPanel.add(zPlusButton, "split 2,flowy,gap 0 0 0 0");
		xyzPanel.add(zMinusButton);

		// add it all to our jog panel
		add(xyzPanel);

		// add jog panel border and stuff.
		setBorder(BorderFactory.createTitledBorder("Script Controls"));
	
	}
	
	public void actionPerformed(ActionEvent e) {
		Point3d current = driver.getCurrentPosition();
		String s = e.getActionCommand();

		if (s.equals("X+")) {
			try {
				driver.parse( "G21" );
				driver.execute();
				driver.parse( "G90" );
				driver.execute();
				driver.parse( "G0 X10 Z10");
				driver.execute();
				driver.parse( "G1 X20 Y10");
				driver.execute();
			} catch (Exception exe )
			{
				Base.logger.warning("Failed doing: " + s);
			}
		} else if (s.equals("X-")) {
			current.x -= 10;

			driver.setFeedrate(580);
			driver.queuePoint(current);
		} else
			Base.logger.warning("Unknown Action Event: " + s);
		
	}
}
