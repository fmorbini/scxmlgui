package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.Document;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.OutSource.OUTSOURCETYPE;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.MyUndoManager;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class SCXMLOutsourcingEditor extends SCXMLElementEditor implements ActionListener {
	private static final long serialVersionUID = 5819456701848767139L;
	private UndoJTextField undoTextField;
	private MyUndoManager undo;
	private Document doc;
    private SCXMLNode node;
	private JRadioButton srcButton,xincludeButton;
    
    public SCXMLOutsourcingEditor(JFrame parent,SCXMLGraphEditor editor, mxCell nn,SCXMLNode n, Point pos) throws Exception {
    	super(parent,editor,nn);
    	setModal(true);
        setTitle(mxResources.get("titleOutsourceEditor"));
        setLocation(pos);

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        tabbedPane = new JTabbedPane();

        node=n;
        undo=node.getSRCUndoManager();
        doc=node.getSRCDoc();
        // undo and doc must be both either null or not null.
        assert(!((undo==null) ^ (doc==null)));
        undoTextField=new UndoJTextField(node.getOutsourcedLocation(), doc, undo);
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
        
        srcButton = new JRadioButton(mxResources.get("SCXMLsrc"));
        srcButton.setActionCommand(mxResources.get("SCXMLsrc"));
        srcButton.setSelected(node.isOutsourcedNodeUsingSRC());

        xincludeButton = new JRadioButton(mxResources.get("SCXMLxinclude"));
        xincludeButton.setActionCommand(mxResources.get("SCXMLxinclude"));
        xincludeButton.setSelected(node.isOutsourcedNodeUsingXInclude());

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(srcButton);
        buttonGroup.add(xincludeButton);

        srcButton.addActionListener(this);
        xincludeButton.addActionListener(this);

        tabbedPane.addTab(mxResources.get("outsourceTAB"), scrollPane);
        tabbedPane.setSelectedIndex(0);
        
		JPanel contentPane=new JPanel();
		contentPane.setLayout(new GridBagLayout());

		//Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(tabbedPane, c);

        contentPane.add(srcButton);
        contentPane.add(xincludeButton);
        
        updateActionTable(tabbedPane,actions);
        
        //Add the components.
        getContentPane().add(contentPane, BorderLayout.CENTER);

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
	public void selectRadioButtonForType() {
		srcButton.setSelected(node.isOutsourcedNodeUsingSRC());
		xincludeButton.setSelected(node.isOutsourcedNodeUsingXInclude());
	}
	@Override
	public void actionPerformed(ActionEvent a) {
		String cmd=a.getActionCommand();
		if (cmd.equals(mxResources.get("SCXMLsrc"))) {
			node.getSRC().setType(OUTSOURCETYPE.SRC);
			selectRadioButtonForType();
		} else if (cmd.equals(mxResources.get("SCXMLxinclude"))) {
			node.getSRC().setType(OUTSOURCETYPE.XINC);
			selectRadioButtonForType();
		}
	}
}

