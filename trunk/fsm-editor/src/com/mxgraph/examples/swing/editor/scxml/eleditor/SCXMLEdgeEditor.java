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
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;

public class SCXMLEdgeEditor extends SCXMLElementEditor {

	private static final long serialVersionUID = 3563719047023065063L;
	
	private static final String undoAction="Undo"; 
	private static final String redoAction="Redo"; 
	
	private UndoJTextField eventTextPane;
	private UndoJTextField conditionTextPane;
	private UndoJTextPane exeTextPane;
    private UndoManager undo;
    private Document doc;
    private SCXMLEdge edge;
    private JMenu editMenu;

    public SCXMLEdgeEditor(JFrame parent,SCXMLEdge e, SCXMLGraphEditor editor, Point pos) {
    	super(parent,editor);
    	setTitle("SCXML edge editor");
    	setLocation(pos);

        edge=e;
        //we need 3 editors:
        // one for the event
        // one for the condition
        // one for the executable content
        tabbedPane = new JTabbedPane();

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        undo=edge.getEventUndoManager();
        doc=edge.getEventDoc();
        eventTextPane=new UndoJTextField(edge.getEvent(), doc, undo);
        if (doc==null) {
        	edge.setEventDoc(doc=eventTextPane.getDocument());
        	edge.setEventUndoManager(undo=eventTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        
        undo=edge.getConditionUndoManager();
        doc=edge.getConditionDoc();
        conditionTextPane=new UndoJTextField(edge.getCondition(), doc, undo);
        if (doc==null) {
        	edge.setConditionDoc(doc=conditionTextPane.getDocument());
        	edge.setConditionUndoManager(undo=conditionTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);

        undo=edge.getExeUndoManager();
        doc=edge.getExeDoc();
        exeTextPane=new UndoJTextPane(edge.getExe(), doc, undo);
        if (doc==null) {
        	edge.setExeDoc(doc=exeTextPane.getDocument());
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
        updateActionTable(tabbedPane,actions);
        editMenu=createEditMenu();
        tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
              updateActionTable(tabbedPane,actions);
            }
          });
        
        //Add the components.
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        //Set up the menu bar.
        //actions=createActionTable(textPane);
        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
        
		//Display the window.
		pack();
		setVisible(true);
    }
}