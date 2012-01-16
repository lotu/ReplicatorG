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

package replicatorg.model;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;

import replicatorg.app.Base;
import replicatorg.app.ui.MainWindow;

/**
 * Stores information about files in the current sketch
 */
public class Build {
	
	/** The editor window associated with this build.  We should remove this dependency or replace it with a
	 * buildupdatelistener or the like. */
	MainWindow editor;

	/** Name of sketch, which is the name of main file, sans extension. */
	private String name;

	public String getName() { return name; }
	/** Name of source file, used by load().  This may be an .STL file or a .gcode file. */
	String mainFilename;

	/**
	 * true if any of the files have been modified.
	 */
	public boolean modified;

	/**
	 * The folder which the base file is located in.
	 */
	public File folder;

	/**
	 * The STL model that this build is based on, if any.
	 */
	public BuildModel model = null;
	/**
	 * The gcode interpretation of the model.
	 */
	public BuildCode code;

	int currentIndex;

	/**
	 * The element that this build was opened as.
	 */
	private BuildElement openedElement;
	/**
	 * Retrieve the element that this build was opened as.  If a user opens a gcode file, it should 
	 * initially display that gcode rather than the model, etc.
	 */
	public BuildElement getOpenedElement() { return openedElement; }
	
	/**
	 * path is location of the main .gcode file, because this is also simplest
	 * to use when opening the file from the finder/explorer.
	 */
	public Build(MainWindow editor, String path) throws IOException {
		this.editor = editor;
		if (path == null) {
			mainFilename = null;
			name = "Untitled";
			folder = new File("~");
			code = new BuildCode(null,null);
			openedElement = code;
			code.modified = true;
		} else {
			File mainFile = new File(path);
			mainFilename = mainFile.getName();
			String suffix = "";
			int lastIdx = mainFilename.lastIndexOf('.');
			if (lastIdx > 0) {
				suffix = mainFilename.substring(lastIdx+1);
				name = mainFilename.substring(0,lastIdx);
			} else {
				name = mainFilename;
			}
			String parentPath = new File(path).getParent(); 
			if (parentPath == null) {
				parentPath = ".";
			}
			folder = new File(parentPath);
			loadCode();
			loadModel();
			if ("gcode".equals(suffix) && code != null) {
				openedElement = code;
			} else {
				openedElement = model;
			}
		}
	}

	public void loadCode() {
		File codeFile = new File(folder, name + ".gcode");
		if (codeFile.exists()) {
			code = new BuildCode(name, codeFile);
		}
	}
	
	public void loadModel() {
		final File modelFile = new File(folder, name + ".stl");
		if (modelFile.exists()) {
			model = new BuildModel() {
				public BuildElement.Type getType() {
					return BuildElement.Type.MODEL;
				}
				public String getSTLPath() {
					try {
						return modelFile.getCanonicalPath();
					} catch (IOException ioe) { return null; }
				}
			};
		}		
	}
	
	/**
	 * Sets the modified value for the code in the frontmost tab.
	 */
	public void setModified(boolean state) {
		code.modified = state;
		calcModified();
	}

	public void calcModified() {
		modified = code.modified;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// editor.getHeader().repaint(); TODO: fix
			}
		});
	}

	/**
	 * Save all code in the current sketch.
	 */
	public boolean save() throws IOException {
		if (mainFilename == null) {
			return saveAs();
		}
		if (!code.modified) { return true; }
		code.program = editor.getText();
		if (isReadOnly()) {
			// if the files are read-only, need to first do a "save as".
			Base.showMessage(
					"File is read-only",
					"This file is marked \"read-only\", so you'll\n"
					+ "need to re-save this file to another location.");
			// if the user cancels, give up on the save()
			if (!saveAs())
				return false;
		}
		code.save();
		calcModified();
		return true;
	}

	/**
	 * Handles 'Save As' for a sketch.
	 * <P>
	 * This basically just duplicates the current sketch folder to a new
	 * location, and then calls 'Save'. (needs to take the current state of the
	 * open files and save them to the new folder.. but not save over the old
	 * versions for the old sketch..)
	 * <P>
	 * Also removes the previously-generated .class and .jar files, because they
	 * can cause trouble.
	 */
	public boolean saveAs() throws IOException {
		// get new name for folder
		FileDialog fd = new FileDialog(editor, "Save file as...",
				FileDialog.SAVE);
		// default to the folder that this file is in
		fd.setDirectory(folder.getCanonicalPath());
		fd.setFile(mainFilename);

		fd.setVisible(true);
		String newParentDir = fd.getDirectory();
		String newName = fd.getFile();
		// user cancelled selection
		if (newName == null)
			return false;

		File newFolder = new File(newParentDir);

		if (!newName.endsWith(".gcode")) newName = newName + ".gcode";

		// grab the contents of the current tab before saving
		// first get the contents of the editor text area
		if (code.modified) {
			code.program = editor.getText();
		}

		File newFile = new File(newFolder, newName);
		code.saveAs(newFile);
		//editor.getHeader().rebuild(); TODO: fix
		calcModified();


		// TODO: update MRU?
		// let MainWindow know that the save was successful
		return true;
	}

	/**
	 * Return the gcode object.
	 */
	public BuildCode getCode() {
		return code;
	}
	
	/**
	 * Return the model object.
	 */
	public BuildModel getModel() {
		return model;
	}
	
	/**
	 * Cleanup temporary files used during a build/run.
	 */
	public void cleanup() {
		// if the java runtime is holding onto any files in the build dir, we
		// won't be able to delete them, so we need to force a gc here
		System.gc();

	}

	protected int countLines(String what) {
		char c[] = what.toCharArray();
		int count = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\n')
				count++;
		}
		return count;
	}

	static public String scrubComments(String what) {
		char p[] = what.toCharArray();

		int index = 0;
		while (index < p.length) {
			// for any double slash comments, ignore until the end of the line
			if ((p[index] == '/') && (index < p.length - 1)
					&& (p[index + 1] == '/')) {
				p[index++] = ' ';
				p[index++] = ' ';
				while ((index < p.length) && (p[index] != '\n')) {
					p[index++] = ' ';
				}

				// check to see if this is the start of a new multiline comment.
				// if it is, then make sure it's actually terminated somewhere.
			} else if ((p[index] == '/') && (index < p.length - 1)
					&& (p[index + 1] == '*')) {
				p[index++] = ' ';
				p[index++] = ' ';
				boolean endOfRainbow = false;
				while (index < p.length - 1) {
					if ((p[index] == '*') && (p[index + 1] == '/')) {
						p[index++] = ' ';
						p[index++] = ' ';
						endOfRainbow = true;
						break;

					} else {
						index++;
					}
				}
				if (!endOfRainbow) {
					throw new RuntimeException(
							"Missing the */ from the end of a "
									+ "/* comment */");
				}
			} else { // any old character, move along
				index++;
			}
		}
		return new String(p);
	}



	/**
	 * Returns true if this is a read-only sketch. Used for the examples
	 * directory, or when sketches are loaded from read-only volumes or folders
	 * without appropriate permissions.
	 */
	public boolean isReadOnly() {
		// check to see if each modified code file can be written to
		return (code.modified && 
				!code.file.canWrite() &&
				code.file.exists());
	}

	/**
	 * Returns path to the main .gcode file for this sketch.
	 */
	public String getMainFilePath() {
		if (code != null && code.file != null) {
			return code.file.getAbsolutePath();
		}
		return null;
	}

}
