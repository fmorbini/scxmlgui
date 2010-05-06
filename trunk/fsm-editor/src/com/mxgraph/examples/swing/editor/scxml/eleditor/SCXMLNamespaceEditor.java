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

public class SCXMLNamespaceEditor extends SCXMLElementEditor {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextPane namespaceTextPane;
	private UndoManager undo;
	private Document doc;
    private SCXMLNode node;
	private JTabbedPane tabbedPane;
    
    public SCXMLNamespaceEditor(SCXMLGraphEditor editor, SCXMLNode n) {
    	super(editor);
        setTitle("SCXML namespace editor");

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        tabbedPane = new JTabbedPane();

        node=n;
        
    	undo=node.getNAMESPACEUndoManager();
    	doc=node.getNAMESPACEDoc();
    	namespaceTextPane=new UndoJTextPane(node.getNAMESPACE(), doc, undo);
    	if (doc==null) {
    		node.setNAMESPACEDoc(doc=namespaceTextPane.getDocument());
    		node.setNAMESPACEUndoManager(undo=namespaceTextPane.getUndoManager());
    	}
    	doc.addDocumentListener(changeListener);
    	namespaceTextPane.setCaretPosition(0);
    	namespaceTextPane.setMargin(new Insets(5,5,5,5));
    	JScrollPane scrollPane = new JScrollPane(namespaceTextPane);
    	scrollPane.setPreferredSize(new Dimension(400, 200));
    	tabbedPane.addTab("Namespace", scrollPane);
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
    public static void createAndShowSCXMLNamespaceEditor(SCXMLGraphEditor editor, SCXMLNode root, Point pos) {
        //Create and set up the window.
        final SCXMLNamespaceEditor frame = new SCXMLNamespaceEditor(editor,root);
        frame.showSCXMLElementEditor(pos);
    }
}

