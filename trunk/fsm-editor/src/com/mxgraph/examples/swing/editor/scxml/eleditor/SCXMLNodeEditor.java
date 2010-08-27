package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.model.mxCell;

public class SCXMLNodeEditor extends SCXMLElementEditor {

	private static final long serialVersionUID = 3563719047023065063L;
	
	private static final String undoAction="Undo"; 
	private static final String redoAction="Redo"; 
	
	private UndoJTextField SCXMLIDTextPane,scrTextPane;
	private UndoJTextPane onentryTextPane;
	private UndoJTextPane onexitTextPane;
	private UndoJTextPane initialTextPane;
	private UndoJTextPane finalTextPane;
    private UndoManager undo;
    private Document doc;
    private SCXMLNode node;
    private JMenu editMenu;

    public SCXMLNodeEditor(JFrame parent,mxCell nn,SCXMLNode n, SCXMLGraphEditor editor, Point pos) {
    	super(parent,editor,nn);
        setTitle("SCXML node editor");
        setLocation(pos);

        node=n;
        //we need 3 editors:
        // one for the event
        // one for the condition
        // one for the executable content
        tabbedPane = new JTabbedPane();

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        undo=node.getSCXMLIDUndoManager();
        doc=node.getSCXMLIDDoc();
        SCXMLIDTextPane=new UndoJTextField(node.getID(), doc, undo);
        if (doc==null) {
        	node.setSCXMLIDDoc(doc=SCXMLIDTextPane.getDocument());
        	node.setSCXMLIDUndoManager(undo=SCXMLIDTextPane.getUndoManager());
        }
        SCXMLIDTextPane.setCaretPosition(0);
        SCXMLIDTextPane.setMargin(new Insets(5,5,5,5));
        JScrollPane scrollPane = new JScrollPane(SCXMLIDTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab("SCXML ID", scrollPane);
        doc.addDocumentListener(changeListener);

        if (!node.isHistoryNode()) {
	        undo=node.getOnEntryUndoManager();
	        doc=node.getOnEntryDoc();
	        onentryTextPane=new UndoJTextPane(node.getOnEntry(), doc, undo);
	        if (doc==null) {
	        	node.setOnEntryDoc(doc=onentryTextPane.getDocument());
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
	        	node.setOnExitDoc(doc=onexitTextPane.getDocument());
	        	node.setOnExitUndoManager(undo=onexitTextPane.getUndoManager());
	        }
	        onexitTextPane.setCaretPosition(0);
	        onexitTextPane.setMargin(new Insets(5,5,5,5));
	        scrollPane = new JScrollPane(onexitTextPane);
	        scrollPane.setPreferredSize(new Dimension(400, 200));
	        tabbedPane.addTab("On exit", scrollPane);
	        doc.addDocumentListener(changeListener);
        }
        
        if (node.isFinal()) {
        	undo=node.getFinalUndoManager();
        	doc=node.getFinalDataDoc();
        	finalTextPane=new UndoJTextPane(node.getDoneData(), doc, undo);
        	if (doc==null) {
        		node.setFinalDataDoc(doc=finalTextPane.getDocument());
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
        		node.setInitialEntryDoc(doc=initialTextPane.getDocument());
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
        updateActionTable(tabbedPane,actions);
        editMenu=createEditMenu();
        tabbedPane.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent changeEvent) {
        		//System.out.println("stateChanged in scxmlnodeeditor");
        		updateActionTable(tabbedPane,actions);        		
        	}

        });

        //Add the components.
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
        
		//Display the window.
		pack();
		setVisible(true);
		
		SCXMLElementEditor.focusOnTextPanel(tabbedPane.getSelectedComponent());
    }
}