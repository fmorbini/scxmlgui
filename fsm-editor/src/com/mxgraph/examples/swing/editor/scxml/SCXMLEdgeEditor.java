package com.mxgraph.examples.swing.editor.scxml;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.utils.*;

public class SCXMLEdgeEditor extends JFrame {

	private static final long serialVersionUID = 3563719047023065063L;
	
	private static final String undoAction="Undo"; 
	private static final String redoAction="Redo"; 
	
	private UndoJTextPane eventTextPane;
	private UndoJTextPane conditionTextPane;
	private UndoJTextPane exeTextPane;
	private JTabbedPane tabbedPane;
    private HashMap<Object, Action> actions;
    private UndoManager undo;
    private AbstractDocument doc;
    private SCXMLEdge edge;
    private JMenu editMenu;

    public SCXMLEdgeEditor(SCXMLEdge e, SCXMLGraphEditor editor) {
        super();
        setTitle("SCXML edge editor");
        
        editMenu=new JMenu("Edit");
        
        edge=e;
        //we need 3 editors:
        // one for the event
        // one for the condition
        // one for the executable content
        tabbedPane = new JTabbedPane();

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        undo=edge.getEventUndoManager();
        doc=edge.getEventDoc();
        eventTextPane=new UndoJTextPane(edge.getEvent(), doc, undo);
        if (doc==null) {
        	edge.setEventDoc(doc=(AbstractDocument) eventTextPane.getStyledDocument());
        	edge.setEventUndoManager(undo=eventTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        
        undo=edge.getConditionUndoManager();
        doc=edge.getConditionDoc();
        conditionTextPane=new UndoJTextPane(edge.getCondition(), doc, undo);
        if (doc==null) {
        	edge.setConditionDoc(doc=(AbstractDocument) conditionTextPane.getStyledDocument());
        	edge.setConditionUndoManager(undo=conditionTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);

        undo=edge.getExeUndoManager();
        doc=edge.getExeDoc();
        exeTextPane=new UndoJTextPane(edge.getExe(), doc, undo);
        if (doc==null) {
        	edge.setExeDoc(doc=(AbstractDocument) exeTextPane.getStyledDocument());
        	edge.setExeUndoManager(undo=exeTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        
        eventTextPane.setCaretPosition(0);
        eventTextPane.setMargin(new Insets(5,5,5,5));
        conditionTextPane.setCaretPosition(0);
        conditionTextPane.setMargin(new Insets(5,5,5,5));
        exeTextPane.setCaretPosition(0);
        exeTextPane.setMargin(new Insets(5,5,5,5));

        JScrollPane scrollPane = new JScrollPane(eventTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("Event", scrollPane);
        scrollPane = new JScrollPane(conditionTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("Condition", scrollPane);
        scrollPane = new JScrollPane(exeTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("Executable content", scrollPane);

        tabbedPane.setSelectedIndex(0);
        actions=createActionTable(tabbedPane);
        tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
              actions=createActionTable(tabbedPane);
              updateEditMenu(editMenu);
            }
          });
        
        //Add the components.
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        //Set up the menu bar.
        //actions=createActionTable(textPane);
        updateEditMenu(editMenu);
        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
    }
    //The following two methods allow us to find an
    //action provided by the editor kit by its name.
    private HashMap<Object, Action> createActionTable(JTabbedPane tabbedPane) {
    	Object o=tabbedPane.getSelectedComponent();
    	UndoJTextPane u;
    	if (o instanceof JScrollPane) {
    		JScrollPane scrollPane=(JScrollPane) o;
    		o=scrollPane.getViewport().getComponent(0);
    		//o=scrollPane.getComponent(0);
    		if (o instanceof UndoJTextPane) {
    			u=(UndoJTextPane) o;
    			HashMap<Object, Action> actions = new HashMap<Object, Action>();
    			ActionMap actionMap = u.getActionMap();
    			actions.put(DefaultEditorKit.copyAction,actionMap.get(DefaultEditorKit.copyAction));
    			actions.put(DefaultEditorKit.cutAction,actionMap.get(DefaultEditorKit.cutAction));
    			actions.put(DefaultEditorKit.pasteAction,actionMap.get(DefaultEditorKit.pasteAction));
    			actions.put(DefaultEditorKit.selectAllAction,actionMap.get(DefaultEditorKit.selectAllAction));
    			actions.put(undoAction,u.getUndoAction());
    			actions.put(redoAction,u.getRedoAction());
    			return actions;
    		}
    	}
    	return null;
    }

    // any time a change is made to the document, the scxml editor "modified" flag is set 
    protected class DocumentChangeListener implements DocumentListener {
    	private SCXMLGraphEditor editor;
        public DocumentChangeListener(SCXMLGraphEditor e) {
    		this.editor=e;
		}
		public void insertUpdate(DocumentEvent e) {
			editor.setModified(true);
        }
        public void removeUpdate(DocumentEvent e) {
			editor.setModified(true);
        }
        public void changedUpdate(DocumentEvent e) {
			editor.setModified(true);
        }
    }

    private Action getActionByName(String name) {
        return actions.get(name);
    }
    
    protected JMenu updateEditMenu(JMenu menu) {
    	menu.removeAll();
        //Undo and redo are actions of our own creation.
        menu.add(getActionByName(undoAction));
        menu.add(getActionByName(redoAction));

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
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     * @param editor 
     * @param pos 
     */
    public static void createAndShowSCXMLEdgeEditor(SCXMLGraphEditor editor, SCXMLEdge edge, Point pos) {
        //Create and set up the window.
        final SCXMLEdgeEditor frame = new SCXMLEdgeEditor(edge,editor);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(pos);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}