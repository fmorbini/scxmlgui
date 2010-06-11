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
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;

public class SCXMLNamespaceEditor extends SCXMLElementEditor {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextPane namespaceTextPane;
	private UndoManager undo;
	private Document doc;
    private SCXMLNode node;
    
    public SCXMLNamespaceEditor(JFrame parent,SCXMLGraphEditor editor, SCXMLNode n, Point pos) {
    	super(parent,editor);
        setTitle("SCXML namespace editor");
        setLocation(pos);

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
        
		//Display the window.
		pack();
		setVisible(true);
		
		SCXMLElementEditor.focusOnTextPanel(tabbedPane.getSelectedComponent());
    }
}

