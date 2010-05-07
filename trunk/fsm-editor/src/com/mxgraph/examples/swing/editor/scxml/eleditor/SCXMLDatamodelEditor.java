package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLElementEditor.DocumentChangeListener;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.util.mxResources;

public class SCXMLDatamodelEditor extends SCXMLElementEditor {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextPane undoTextPane;
	private UndoManager undo;
	private Document doc;
    private SCXMLNode root;
	private JTabbedPane tabbedPane;
    
    public SCXMLDatamodelEditor(SCXMLGraphEditor editor, SCXMLNode r) throws Exception {
    	super(editor);
        setTitle("SCXML datamodel editor");

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        tabbedPane = new JTabbedPane();

        root=r;        
        undo=root.getDatamodelUndoManager();
        doc=root.getDatamodelDoc();
        // undo and doc must be both either null or not null.
        assert(!((undo==null) ^ (doc==null)));
        undoTextPane=new UndoJTextPane(XMLUtils.prettyPrintXMLString(root.getDataModel()," "), doc, undo);
        if (doc==null) {
        	root.setDatamodelDoc(doc=undoTextPane.getDocument());
        	root.setDatamodelUndoManager(undo=undoTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        // configure the undo text pane.
        undoTextPane.setCaretPosition(0);
        undoTextPane.setMargin(new Insets(5,5,5,5));

        JScrollPane scrollPane = new JScrollPane(undoTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        tabbedPane.addTab("Data model", scrollPane);
        
        tabbedPane.setSelectedIndex(0);
        updateActionTable(tabbedPane,actions);
        
        //Add the components.
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        //Set up the menu bar.
        //actions=createActionTable(textPane);
        JMenu editMenu=createEditMenu();
        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
    }
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     * @param editor 
     * @param pos 
     */
    public static void createAndShowSCXMLDatamodelEditor(SCXMLGraphEditor editor, SCXMLNode root, Point pos) {
        //Create and set up the window.
        SCXMLDatamodelEditor frame;
		try {
			frame = new SCXMLDatamodelEditor(editor,root);
	        frame.showSCXMLElementEditor(pos);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

