package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.model.mxCell;

public class SCXMLOutsourcingEditor extends SCXMLElementEditor {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextField undoTextField;
	private UndoManager undo;
	private Document doc;
    private SCXMLNode node;
    
    public SCXMLOutsourcingEditor(JFrame parent,SCXMLGraphEditor editor, mxCell nn,SCXMLNode n, Point pos) throws Exception {
    	super(parent,editor,nn);
    	setModal(true);
        setTitle("Set source file to fill the content of this node");
        setLocation(pos);

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        tabbedPane = new JTabbedPane();

        node=n;
        undo=node.getSRCUndoManager();
        doc=node.getSRCDoc();
        // undo and doc must be both either null or not null.
        assert(!((undo==null) ^ (doc==null)));
        undoTextField=new UndoJTextField(XMLUtils.prettyPrintXMLString(node.getSRC()," "), doc, undo);
        if (doc==null) {
        	node.setSRCDoc(doc=undoTextField.getDocument());
        	node.setSRCUndoManager(undo=undoTextField.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        // configure the undo text pane.
        undoTextField.setCaretPosition(0);
        undoTextField.setMargin(new Insets(5,5,5,5));

        JScrollPane scrollPane = new JScrollPane(undoTextField);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        tabbedPane.addTab("Source URL", scrollPane);
        
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
	public class CloseAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
}

