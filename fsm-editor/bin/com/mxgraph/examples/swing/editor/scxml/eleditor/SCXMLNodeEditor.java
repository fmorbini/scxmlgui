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

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.MyUndoManager;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class SCXMLNodeEditor extends SCXMLElementEditor {

	private static final long serialVersionUID = 3563719047023065063L;

	private UndoJTextField scxmlIDTextPane;
	private UndoJTextField nameTextPane;
	private UndoJTextPane onentryTextPane;
	private UndoJTextPane onexitTextPane;
	private UndoJTextPane initialTextPane;
	private UndoJTextPane finalTextPane;
	private UndoJTextPane namespacePane;
	private UndoJTextPane datamodelPane;
	private UndoJTextPane commentsPane;
	private UndoJTextPane scriptPane;
	private MyUndoManager undo;
	private Document doc;
	private final SCXMLNode node;
	private JMenu editMenu;

	public SCXMLNodeEditor(JFrame parent,mxCell nn,mxCell rootOfGraph, SCXMLGraphEditor editor, Point pos) {
		super(parent,editor,nn);
		setTitle(mxResources.get("titleNodeEditor"));
		setLocation(pos);

		node=(SCXMLNode) nn.getValue();
		//we need 3 editors:
		// one for the event
		// one for the condition
		// one for the executable content
		tabbedPane = new JTabbedPane();

		DocumentChangeListener changeListener = new DocumentChangeListener(editor);
		mxCell nnParent=(mxCell) nn.getParent();
		if (nn!=rootOfGraph) {
			undo=node.getIDUndoManager();
			doc=node.getIDDoc();
			scxmlIDTextPane=new UndoJTextField(node.getID(), doc, undo);
			if (doc==null) {
				node.setIDDoc(doc=scxmlIDTextPane.getDocument());
				node.setIDUndoManager(undo=scxmlIDTextPane.getUndoManager());
			}
			scxmlIDTextPane.setCaretPosition(0);
			scxmlIDTextPane.setMargin(new Insets(5,5,5,5));
			JScrollPane scrollPane = new JScrollPane(scxmlIDTextPane);
			scxmlIDTextPane.setScrollPane(scrollPane);
			scrollPane.setPreferredSize(new Dimension(400, 200));
			tabbedPane.addTab(mxResources.get("nodeIDTAB"), scrollPane);
			doc.addDocumentListener(changeListener);

			if (!node.isHistoryNode() && !node.getFake()) {
				undo=node.getOnEntryUndoManager();
				doc=node.getOnEntryDoc();
				onentryTextPane=new UndoJTextPane(node.getOnEntry(), doc, undo,keyboardHandler);
				if (doc==null) {
					node.setOnEntryDoc(doc=onentryTextPane.getDocument());
					node.setOnEntryUndoManager(undo=onentryTextPane.getUndoManager());
				}
				onentryTextPane.setCaretPosition(0);
				onentryTextPane.setMargin(new Insets(5,5,5,5));
				scrollPane = new JScrollPane(onentryTextPane);
				scrollPane.setPreferredSize(new Dimension(400, 200));
				tabbedPane.addTab(mxResources.get("onEntryTAB"), scrollPane);
				doc.addDocumentListener(changeListener);

				undo=node.getOnExitUndoManager();
				doc=node.getOnExitDoc();
				onexitTextPane=new UndoJTextPane(node.getOnExit(), doc, undo, keyboardHandler);
				if (doc==null) {
					node.setOnExitDoc(doc=onexitTextPane.getDocument());
					node.setOnExitUndoManager(undo=onexitTextPane.getUndoManager());
				}
				onexitTextPane.setCaretPosition(0);
				onexitTextPane.setMargin(new Insets(5,5,5,5));
				scrollPane = new JScrollPane(onexitTextPane);
				scrollPane.setPreferredSize(new Dimension(400, 200));
				tabbedPane.addTab(mxResources.get("onExitTAB"), scrollPane);
				doc.addDocumentListener(changeListener);
			}

			if (node.isFinal() && !node.getFake()) {
				undo=node.getDoneDataUndoManager();
				doc=node.getDoneDataDoc();
				finalTextPane=new UndoJTextPane(node.getDoneData(), doc, undo, keyboardHandler);
				if (doc==null) {
					node.setDoneDataDoc(doc=finalTextPane.getDocument());
					node.setDoneDataUndoManager(undo=finalTextPane.getUndoManager());
				}
				finalTextPane.setCaretPosition(0);
				finalTextPane.setMargin(new Insets(5,5,5,5));
				scrollPane = new JScrollPane(finalTextPane);
				scrollPane.setPreferredSize(new Dimension(400, 200));
				tabbedPane.addTab(mxResources.get("finalDataTAB"), scrollPane);
				doc.addDocumentListener(changeListener);
			}
			if (node.isInitial() && !node.getFake() && (nnParent!=rootOfGraph)) {
				undo=node.getOnInitialEntryUndoManager();
				doc=node.getOnInitialEntryDoc();
				initialTextPane=new UndoJTextPane(node.getOnInitialEntry(), doc, undo, keyboardHandler);
				if (doc==null) {
					node.setOnInitialEntryDoc(doc=initialTextPane.getDocument());
					node.setOnInitialEntryUndoManager(undo=initialTextPane.getUndoManager());
				}
				initialTextPane.setCaretPosition(0);
				initialTextPane.setMargin(new Insets(5,5,5,5));
				scrollPane = new JScrollPane(initialTextPane);
				scrollPane.setPreferredSize(new Dimension(400, 200));
				tabbedPane.addTab(mxResources.get("initialEntryTAB"), scrollPane);
				doc.addDocumentListener(changeListener);
			}
		} else {
			undo=node.getNameUndoManager();
			doc=node.getNameDoc();
			nameTextPane=new UndoJTextField(node.getName(), doc, undo);
			if (doc==null) {
				node.setNameDoc(doc=nameTextPane.getDocument());
				node.setNameUndoManager(undo=nameTextPane.getUndoManager());
			}
			nameTextPane.setCaretPosition(0);
			nameTextPane.setMargin(new Insets(5,5,5,5));
			JScrollPane scrollPane = new JScrollPane(nameTextPane);
			nameTextPane.setScrollPane(scrollPane);
			scrollPane.setPreferredSize(new Dimension(400, 200));
			tabbedPane.addTab(mxResources.get("nodeNameTAB"), scrollPane);
			doc.addDocumentListener(changeListener);
		}
        if (!node.isHistoryNode() && !node.getFake()) {
	        undo=node.getDatamodelUndoManager();
	        doc=node.getDatamodelDoc();
	        datamodelPane=new UndoJTextPane(node.getDatamodel(), doc, undo,keyboardHandler);
	        if (doc==null) {
	        	node.setDatamodelDoc(doc=datamodelPane.getDocument());
	        	node.setDatamodelUndoManager(undo=datamodelPane.getUndoManager());
	        }
	        datamodelPane.setCaretPosition(0);
	        datamodelPane.setMargin(new Insets(5,5,5,5));
	        JScrollPane scrollPane = new JScrollPane(datamodelPane);
	        scrollPane.setPreferredSize(new Dimension(400, 200));
	        tabbedPane.addTab(mxResources.get("datamodelTAB"), scrollPane);
	        doc.addDocumentListener(changeListener);
	        
	        undo=node.getNamespaceUndoManager();
	        doc=node.getNamespaceDoc();
	        namespacePane=new UndoJTextPane(node.getNamespace(), doc, undo, keyboardHandler);
	        if (doc==null) {
	        	node.setNamespaceDoc(doc=namespacePane.getDocument());
	        	node.setNamespaceUndoManager(undo=namespacePane.getUndoManager());
	        }
	        namespacePane.setCaretPosition(0);
	        namespacePane.setMargin(new Insets(5,5,5,5));
	        scrollPane = new JScrollPane(namespacePane);
	        scrollPane.setPreferredSize(new Dimension(400, 200));
	        tabbedPane.addTab(mxResources.get("namespaceTAB"), scrollPane);
	        doc.addDocumentListener(changeListener);
        }
		if (nn!=rootOfGraph) {
			undo=node.getCommentsUndoManager();
			doc=node.getCommentsDoc();
			commentsPane=new UndoJTextPane(node.getComments(), doc, undo, keyboardHandler);
			if (doc==null) {
				node.setCommentsDoc(doc=commentsPane.getDocument());
				node.setCommentsUndoManager(undo=commentsPane.getUndoManager());
			}
			commentsPane.setCaretPosition(0);
			commentsPane.setMargin(new Insets(5,5,5,5));
			JScrollPane scrollPane = new JScrollPane(commentsPane);
			scrollPane.setPreferredSize(new Dimension(400, 200));
			tabbedPane.addTab(mxResources.get("commentsTAB"), scrollPane);
			doc.addDocumentListener(changeListener);
		}
		undo=node.getScriptUndoManager();
		doc=node.getScriptDoc();
		scriptPane=new UndoJTextPane(node.getScript(), doc, undo, keyboardHandler);
		if (doc==null) {
			node.setScriptDoc(doc=scriptPane.getDocument());
			node.setScriptUndoManager(undo=scriptPane.getUndoManager());
		}
		scriptPane.setCaretPosition(0);
		scriptPane.setMargin(new Insets(5,5,5,5));
		JScrollPane scrollPane = new JScrollPane(scriptPane);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		tabbedPane.addTab(mxResources.get("scriptTAB"), scrollPane);
		doc.addDocumentListener(changeListener);

		
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