package com.mxgraph.examples.swing.editor.scxml;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.utils.*;

public class SCXMLDatamodelEditor extends JFrame {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextPane undoTextPane;
	private UndoManager undo;
	private AbstractDocument doc;
    private SCXMLNode root;
	private HashMap<Object, Action> actions;
    
    public SCXMLDatamodelEditor(SCXMLNode r) {
        super();
        setTitle("SCXML datamodel editor");

        root=r;        
        undo=root.getDatamodelUndoManager();
        doc=root.getDatamodelDoc();
        // undo and doc must be both either null or not null.
        assert(!((undo==null) ^ (doc==null)));
        undoTextPane=new UndoJTextPane(XMLUtils.prettyPrintXMLString(root.getDataModel()," "), doc, undo);
        if (doc==null) {
        	root.setDatamodelDoc(doc=(AbstractDocument) undoTextPane.getStyledDocument());
        	root.setDatamodelUndoManager(undo=undoTextPane.getUndoManager());
        }
        actions=createActionTable(undoTextPane);
        
        // configure the undo text pane.
        undoTextPane.setCaretPosition(0);
        undoTextPane.setMargin(new Insets(5,5,5,5));

        JScrollPane scrollPane = new JScrollPane(undoTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        //Add the components.
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        //Set up the menu bar.
        //actions=createActionTable(textPane);
        JMenu editMenu = createEditMenu();
        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
    }

    //Create the edit menu.
    protected JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");

        //Undo and redo are actions of our own creation.
        menu.add(undoTextPane.getUndoAction());
        menu.add(undoTextPane.getRedoAction());

        menu.addSeparator();

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
        menu.add(getActionByName(DefaultEditorKit.cutAction));
        menu.add(getActionByName(DefaultEditorKit.copyAction));
        menu.add(getActionByName(DefaultEditorKit.pasteAction));

        menu.addSeparator();

        menu.add(getActionByName(DefaultEditorKit.selectAllAction));
        return menu;
    }

    //The following two methods allow us to find an
    //action provided by the editor kit by its name.
    private HashMap<Object, Action> createActionTable(UndoJTextPane textComponent) {
        HashMap<Object, Action> actions = new HashMap<Object, Action>();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
	return actions;
    }

    private Action getActionByName(String name) {
        return actions.get(name);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     * @param pos 
     */
    public static void createAndShowSCXMLDatamodelEditor(SCXMLNode root, Point pos) {
        //Create and set up the window.
        final SCXMLDatamodelEditor frame = new SCXMLDatamodelEditor(root);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(pos);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}

