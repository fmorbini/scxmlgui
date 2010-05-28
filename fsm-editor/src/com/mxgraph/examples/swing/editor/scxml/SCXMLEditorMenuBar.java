package com.mxgraph.examples.swing.editor.scxml;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.HistoryAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.NewSCXMLAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.OpenAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SaveAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ShowSCXMLListener;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ToggleDisplayOutsourcedContent;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

public class SCXMLEditorMenuBar extends JMenuBar
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4060203894740766714L;
	private static final int MAX_RECENT_FILE_HISTORY=9;
	private ArrayList<String> recentlyOpenedFiles=new ArrayList<String>();
	private JMenu fileMenu;

	public void getRecentlyOpenedFiles(final SCXMLGraphEditor editor) {
		recentlyOpenedFiles.clear();
		String baseKey="FILE_";
		for (int i=0;i<MAX_RECENT_FILE_HISTORY;i++) {
			String fileName=editor.preferences.get(baseKey+i, null);
			if (fileName!=null) recentlyOpenedFiles.add(fileName); 
		}
	}
	public void updateRecentlyOpenedListWithFile(File file,SCXMLGraphEditor editor) {
		String fileName=file.getAbsolutePath();
		if (!recentlyOpenedFiles.contains(fileName)) {
			recentlyOpenedFiles.add(fileName);
			int size=recentlyOpenedFiles.size();
			for(int i=0;i<size-MAX_RECENT_FILE_HISTORY;i++) {
				recentlyOpenedFiles.remove(0);
			}
			String baseKey="FILE_";
			int i=0;
			for(String fn:recentlyOpenedFiles) {
				if (new File(fn).exists()) editor.preferences.put(baseKey+(i++), fn);
			}
			for (;i<MAX_RECENT_FILE_HISTORY;i++) {
				editor.preferences.remove(baseKey+(i++));
			}
			makeFileMenu(fileMenu,editor);
		}
	}

	@SuppressWarnings("serial")
	public SCXMLEditorMenuBar(final SCXMLGraphEditor editor)
	{
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		final mxGraph graph = graphComponent.getGraph();
		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		fileMenu = add(new JMenu(mxResources.get("file")));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		makeFileMenu(fileMenu,editor);

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));
		menu.setMnemonic(KeyEvent.VK_E);

		AbstractAction internalAction=new HistoryAction(true);
		Action externalAction=editor.bind(mxResources.get("undo"), internalAction,"/com/mxgraph/examples/swing/images/undo.gif");
		menu.add(externalAction);
		editor.setUndoMenuAction(externalAction);
		internalAction=new HistoryAction(false);
		externalAction=editor.bind(mxResources.get("redo"), internalAction,"/com/mxgraph/examples/swing/images/redo.gif");
		menu.add(externalAction);
		editor.setRedoMenuAction(externalAction);

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("cut"), TransferHandler.getCutAction(), "/com/mxgraph/examples/swing/images/cut.gif"));
		menu.add(editor.bind(mxResources.get("copy"), TransferHandler.getCopyAction(),"/com/mxgraph/examples/swing/images/copy.gif"));
		menu.add(editor.bind(mxResources.get("paste"), TransferHandler.getPasteAction(),"/com/mxgraph/examples/swing/images/paste.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("delete"), mxGraphActions.getDeleteAction(),"/com/mxgraph/examples/swing/images/delete.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("selectAll"), mxGraphActions.getSelectAllAction()));
		menu.add(editor.bind(mxResources.get("selectNone"), mxGraphActions.getSelectNoneAction()));

		menu = add(new JMenu(mxResources.get("tools")));
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(editor.bind(mxResources.get("showSCXMLListener"), new ShowSCXMLListener()));
		
		menu = add(new JMenu(mxResources.get("view")));
		menu.setMnemonic(KeyEvent.VK_V);
		internalAction=new ToggleDisplayOutsourcedContent();
		JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(externalAction=editor.bind(mxResources.get("toggleDisplayContentOutsourced"), internalAction));
		menu.add(menuItem);
		editor.setDisplayOutsourcedContentMenuItem(menuItem);
		editor.setDisplayOfOutsourcedContentSelected(editor.isDisplayOfOutsourcedContentSelected());

		add(Box.createHorizontalGlue());
		
		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));
		menu.setMnemonic(KeyEvent.VK_H);

		JMenuItem item = menu.add(new JMenuItem(mxResources.get("aboutGraphEditor")));
		item.addActionListener(new ActionListener()
		{
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				editor.about();
			}
		});
	}
	private void makeFileMenu(JMenu menu, SCXMLGraphEditor editor) {
		getRecentlyOpenedFiles(editor);
		menu.removeAll();
		menu.add(editor.bind(mxResources.get("newscxml"), new NewSCXMLAction(),"/com/mxgraph/examples/swing/images/new.gif"));
		menu.add(editor.bind(mxResources.get("openFile"), new OpenAction(),"/com/mxgraph/examples/swing/images/open.gif"));
		menu.addSeparator();
		menu.add(editor.bind(mxResources.get("save"), new SaveAction(false),"/com/mxgraph/examples/swing/images/save.gif"));
		menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(true),"/com/mxgraph/examples/swing/images/saveas.gif"));
		if (!recentlyOpenedFiles.isEmpty()) {
			menu.addSeparator();
			int i=0;
			for (String fileName:recentlyOpenedFiles) {
				File file=new File(fileName);
				if (file.exists()) {
					Action itemAction = editor.bind(fileName, new OpenAction(file));
					JMenuItem itemMenu= new JMenuItem(itemAction);
					itemMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1+(i++), ActionEvent.ALT_MASK));
					menu.add(itemMenu);
				}
			}
		}
	}
}
