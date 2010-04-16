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
import com.mxgraph.examples.swing.editor.scxml.SCXMLEdgeEditor.DocumentChangeListener;
import com.mxgraph.examples.swing.editor.utils.*;

public class SCXMLNodeEditor extends JFrame {

	private static final long serialVersionUID = 3563719047023065063L;
	
	private static final String undoAction="Undo"; 
	private static final String redoAction="Redo"; 
	
	private UndoJTextPane SCXMLIDTextPane;
	private UndoJTextPane onentryTextPane;
	private UndoJTextPane onexitTextPane;
	private UndoJTextPane initialTextPane;
	private UndoJTextPane finalTextPane;
	private JTabbedPane tabbedPane;
    private HashMap<Object, Action> actions;
    private UndoManager undo;
    private AbstractDocument doc;
    private SCXMLNode node;
    private JMenu editMenu;

    public SCXMLNodeEditor(SCXMLNode n, SCXMLGraphEditor editor) {
        super();
        setTitle("SCXML edge editor");
        
        editMenu=new JMenu("Edit");
        
        node=n;
        //we need 3 editors:
        // one for the event
        // one for the condition
        // one for the executable content
        tabbedPane = new JTabbedPane();

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        undo=node.getSCXMLIDUndoManager();
        doc=node.getSCXMLIDDoc();
        SCXMLIDTextPane=new UndoJTextPane(node.getID(), doc, undo);
        if (doc==null) {
        	node.setSCXMLIDDoc(doc=(AbstractDocument) SCXMLIDTextPane.getStyledDocument());
        	node.setSCXMLIDUndoManager(undo=SCXMLIDTextPane.getUndoManager());
        }
        SCXMLIDTextPane.setCaretPosition(0);
        SCXMLIDTextPane.setMargin(new Insets(5,5,5,5));
        JScrollPane scrollPane = new JScrollPane(SCXMLIDTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("SCXML ID", scrollPane);
        doc.addDocumentListener(changeListener);

        undo=node.getOnEntryUndoManager();
        doc=node.getOnEntryDoc();
        onentryTextPane=new UndoJTextPane(node.getOnEntry(), doc, undo);
        if (doc==null) {
        	node.setOnEntryDoc(doc=(AbstractDocument) onentryTextPane.getStyledDocument());
        	node.setOnEntryUndoManager(undo=onentryTextPane.getUndoManager());
        }
        onentryTextPane.setCaretPosition(0);
        onentryTextPane.setMargin(new Insets(5,5,5,5));
        scrollPane = new JScrollPane(onentryTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("On entry", scrollPane);
        doc.addDocumentListener(changeListener);
        
        undo=node.getOnExitUndoManager();
        doc=node.getOnExitDoc();
        onexitTextPane=new UndoJTextPane(node.getOnExit(), doc, undo);
        if (doc==null) {
        	node.setOnExitDoc(doc=(AbstractDocument) onexitTextPane.getStyledDocument());
        	node.setOnExitUndoManager(undo=onexitTextPane.getUndoManager());
        }
        onexitTextPane.setCaretPosition(0);
        onexitTextPane.setMargin(new Insets(5,5,5,5));
        scrollPane = new JScrollPane(onexitTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("On exit", scrollPane);
        doc.addDocumentListener(changeListener);
        
        if (node.isFinal()) {
        	undo=node.getFinalUndoManager();
        	doc=node.getFinalDataDoc();
        	finalTextPane=new UndoJTextPane(node.getDoneData(), doc, undo);
        	if (doc==null) {
        		node.setFinalDataDoc(doc=(AbstractDocument) finalTextPane.getStyledDocument());
        		node.setFinalUndoManager(undo=finalTextPane.getUndoManager());
        	}
        	finalTextPane.setCaretPosition(0);
        	finalTextPane.setMargin(new Insets(5,5,5,5));
            scrollPane = new JScrollPane(finalTextPane);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            tabbedPane.addTab("Final event data", scrollPane);
            doc.addDocumentListener(changeListener);
       }
        if (node.isInitial()) {
        	undo=node.getInitialEntryUndoManager();
        	doc=node.getInitialEntryDoc();
        	initialTextPane=new UndoJTextPane(node.getOnInitialEntry(), doc, undo);
        	if (doc==null) {
        		node.setInitialEntryDoc(doc=(AbstractDocument) initialTextPane.getStyledDocument());
        		node.setInitialEntryUndoManager(undo=initialTextPane.getUndoManager());
        	}
        	initialTextPane.setCaretPosition(0);
        	initialTextPane.setMargin(new Insets(5,5,5,5));
            scrollPane = new JScrollPane(initialTextPane);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            tabbedPane.addTab("On initial entry", scrollPane);
            doc.addDocumentListener(changeListener);
       }

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
    public static void createAndShowSCXMLNodeEditor(SCXMLGraphEditor editor, SCXMLNode node, Point pos) {
        //Create and set up the window.
        final SCXMLNodeEditor frame = new SCXMLNodeEditor(node,editor);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(pos);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}