package com.mxgraph.examples.swing.editor.scxml;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

	@SuppressWarnings("serial")
	public SCXMLEditorMenuBar(final SCXMLGraphEditor editor)
	{
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		final mxGraph graph = graphComponent.getGraph();
		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		menu = add(new JMenu(mxResources.get("file")));

		menu.add(editor.bind(mxResources.get("newscxml"), new NewSCXMLAction(),"/com/mxgraph/examples/swing/images/new.gif"));
		menu.add(editor.bind(mxResources.get("openFile"), new OpenAction(),"/com/mxgraph/examples/swing/images/open.gif"));
		menu.addSeparator();
		menu.add(editor.bind(mxResources.get("save"), new SaveAction(false),"/com/mxgraph/examples/swing/images/save.gif"));
		menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(true),"/com/mxgraph/examples/swing/images/saveas.gif"));

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));

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
		menu.add(editor.bind(mxResources.get("showSCXMLListener"), new ShowSCXMLListener()));
		
		menu = add(new JMenu(mxResources.get("view")));
		internalAction=new ToggleDisplayOutsourcedContent();
		JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(externalAction=editor.bind(mxResources.get("toggleDisplayContentOutsourced"), internalAction));
		menu.add(menuItem);
		editor.setDisplayOutsourcedContentMenuItem(menuItem);
		editor.setDisplayOfOutsourcedContentSelected(editor.isDisplayOfOutsourcedContentSelected());

		add(Box.createHorizontalGlue());
		
		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));

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

}
