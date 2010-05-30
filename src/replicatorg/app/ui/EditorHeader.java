/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package replicatorg.app.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.model.Build;
import replicatorg.model.BuildElement;

/**
 * Sketch tabs at the top of the editor window.
 */
public class EditorHeader extends JComponent implements ActionListener {
	public enum Tab {
		MODEL,
		GCODE
	};
	
	public Tab getSelectedTab() {
		if (modelButton.isSelected()) return Tab.MODEL;
		return Tab.GCODE;
	}
	static Color backgroundColor;

	static Color textSelectedColor;
	static Color textUnselectedColor;
 
	private ButtonGroup tabGroup = new ButtonGroup();

	private ChangeListener changeListener;
	void setChangeListener(ChangeListener listener) {
		changeListener = listener;
	}
	
	private class TabButtonUI extends BasicButtonUI {
		public void paint(Graphics g,JComponent c) {
			initTabImages();
			TabButton b = (TabButton)c;
			BufferedImage img = b.isSelected()?selectedTabBg:regularTabBg;
			final int partWidth = img.getWidth()/3;
			int height = img.getHeight();
			final int x = 0;
			final int y = 0;
			final int w = c.getWidth();
			// Draw left side of tab
			g.drawImage(img, x, y, x+partWidth, y+height, 0, 0, partWidth, height, null);
			final int rightTabStart = img.getWidth()-partWidth;
			// Draw center of tab
			g.drawImage(img, x+partWidth, y, x+w-partWidth, y+height, partWidth, 0, rightTabStart, height, null);
			// Draw right side of tab
			g.drawImage(img, x+w-partWidth, y, x+w, y+height, rightTabStart, 0, img.getWidth(), height, null);
			b.setForeground(b.isSelected()?textSelectedColor:textUnselectedColor);
			super.paint(g,c);
		}
	}

	static BufferedImage selectedTabBg;
	static BufferedImage regularTabBg;
	
	protected void initTabImages() {
		if (selectedTabBg == null) {
			selectedTabBg = Base.getImage("images/tab-selected.png", this);
		}
		if (regularTabBg == null) {
			regularTabBg = Base.getImage("images/tab-regular.png", this);
		}
	}


	private class TabButton extends JToggleButton {
		
		public TabButton(String text) {
			super(text);
			setUI(new TabButtonUI());
			setBorder(new EmptyBorder(6,8,8,10));
			tabGroup.add(this);
			addActionListener(EditorHeader.this);
		}
	}
	
	JToggleButton codeButton = new TabButton("gcode");
	JToggleButton modelButton = new TabButton("model");
	JLabel titleLabel = new JLabel("Untitled");
	
	MainWindow editor;

	int fontAscent;

	int menuLeft;

	int menuRight;

	public EditorHeader(MainWindow mainWindow) {
		initTabImages();
		setLayout(new MigLayout("gap 15"));
		this.editor = mainWindow;

		add(titleLabel);
		add(modelButton);
		add(codeButton);
		codeButton.setSelected(true);
		backgroundColor = new Color(0x92, 0xA0, 0x6B);
		textSelectedColor = Base.getColorPref("header.text.selected.color","#1A1A00");
		textUnselectedColor = Base.getColorPref("header.text.unselected.color","#ffffff");
	}

	void setBuild(Build build) {
		codeButton.setVisible(build.getCode() != null);
		modelButton.setVisible(build.getModel() != null);
		titleLabel.setText(build.getName());
		if (build.getOpenedElement() != null) {
			if (build.getOpenedElement().getType() == BuildElement.Type.GCODE) {
				codeButton.doClick();
			} else {
				modelButton.doClick();
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		if (g == null)
			return;

		Build sketch = editor.build;
		if (sketch == null)
			return; // ??

		Dimension size = getSize();

		// set the background for the offscreen
		g.setColor(backgroundColor);
		g.fillRect(0, 0, size.width, size.height);

		super.paintComponent(g);
	}

	/**
	 * Called when a new sketch is opened.
	 */
	public void rebuild() {
		// System.out.println("rebuilding editor header");
		repaint();
	}

	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	final static int GRID_SIZE = 33;
	
	public Dimension getMinimumSize() {
		return new Dimension(0, GRID_SIZE);
	}

	public void actionPerformed(ActionEvent a) {
		ChangeEvent e = new ChangeEvent(this);
		if (changeListener != null) changeListener.stateChanged(e);
	}
}
