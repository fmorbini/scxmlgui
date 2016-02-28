package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import com.mxgraph.examples.config.SCXMLConstraints.RestrictedState.PossibleEvent;
import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.MyUndoManager;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class SCXMLEdgeEditor extends SCXMLElementEditor {

	private static final long serialVersionUID = 3563719047023065063L;
	
	private UndoJTextField eventTextPane;
	private JLabel eventDocumentationLabel;
	private UndoJTextField conditionTextPane;
	private UndoJTextPane exeTextPane;
	private UndoJTextPane commentsPane;
    private MyUndoManager undo;
    private Document doc;
    private SCXMLEdge edge;
    private JMenu editMenu;
    private JScrollPane scrollPane;
    private JRadioButton possibleEventRadioButton;
    private JPanel possibleEventsButtonGroupPanel;
    private ButtonGroup eventButtonGroup;
    private JScrollPane buttonGroupScrollPane;
    private JPanel restrictedEdgeEditorPanel;
    private JPanel possibleEventDetailsPanel;
    private SCXMLNode sourceNode;

    public SCXMLEdgeEditor(JFrame parent,mxCell en, SCXMLGraphEditor editor, Point pos) {
    	super(parent,editor,en);
    	setTitle(mxResources.get("titleEdgeEditor"));
    	setLocation(pos);

        edge=(SCXMLEdge) en.getValue();
        //we need 3 editors:
        // one for the event
        // one for the condition
        // one for the executable content
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(650, 300));

        DocumentChangeListener changeListener = new DocumentChangeListener(editor);

        undo=edge.getEventUndoManager();
        doc=edge.getEventDoc();
        eventTextPane=new UndoJTextField(edge.getEvent(), doc, undo);
        if (doc==null) {
        	edge.setEventDoc(doc=eventTextPane.getDocument());
        	edge.setEventUndoManager(undo=eventTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);
        eventDocumentationLabel = new JLabel();
        
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
        exeTextPane=new UndoJTextPane(edge.getExe(), doc, undo, keyboardHandler);
        if (doc==null) {
        	edge.setExeDoc(doc=exeTextPane.getDocument());
        	edge.setExeUndoManager(undo=exeTextPane.getUndoManager());
        }
        doc.addDocumentListener(changeListener);

		undo=edge.getCommentsUndoManager();
		doc=edge.getCommentsDoc();
		commentsPane=new UndoJTextPane(edge.getComments(), doc, undo, keyboardHandler);
		if (doc==null) {
			edge.setCommentsDoc(doc=commentsPane.getDocument());
			edge.setCommentsUndoManager(undo=commentsPane.getUndoManager());
		}
		doc.addDocumentListener(changeListener);

        eventTextPane.setCaretPosition(0);
        conditionTextPane.setCaretPosition(0);
        conditionTextPane.setMargin(new Insets(5,5,5,5));
        exeTextPane.setCaretPosition(0);
        exeTextPane.setMargin(new Insets(5,5,5,5));
		commentsPane.setCaretPosition(0);
		commentsPane.setMargin(new Insets(5,5,5,5));
		
        scrollPane = new JScrollPane(eventTextPane);
        eventTextPane.setScrollPane(scrollPane);
        
        sourceNode = (SCXMLNode) editor.getGraphComponent().getSCXMLNodeForID(edge.getSCXMLSource()).getValue();
        if (sourceNode.isRestricted()) {
            GridBagLayout gbl = new GridBagLayout();
            restrictedEdgeEditorPanel = new JPanel(gbl);

            possibleEventsButtonGroupPanel = new JPanel(new GridBagLayout());
            loadPossibleEventsButtonGroup(editor);
            
        	GridBagConstraints c = new GridBagConstraints();
        	buttonGroupScrollPane = new JScrollPane(possibleEventsButtonGroupPanel);
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.3;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 0;
			restrictedEdgeEditorPanel.add(buttonGroupScrollPane, c);
			possibleEventDetailsPanel = new JPanel(new GridBagLayout());
			eventTextPane.setEditable(false);
			JLabel eventNameTitleLabel = new JLabel(mxResources.get("eventNameTitle"));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 0;
			possibleEventDetailsPanel.add(eventNameTitleLabel, c);
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 0.25;
			c.gridx = 0;
			c.gridy = 1;
			possibleEventDetailsPanel.add(scrollPane, c);
			JLabel evenDocumentationTitleLabel = new JLabel(mxResources.get("eventDocumentationTitle"));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 2;
			possibleEventDetailsPanel.add(evenDocumentationTitleLabel, c);
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 3;
			JScrollPane documentationScrollPane = new JScrollPane(eventDocumentationLabel);
			//documentationScrollPane.setPreferredSize(new Dimension(400, 200));
			possibleEventDetailsPanel.add(documentationScrollPane, c);
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 0;
			restrictedEdgeEditorPanel.add(possibleEventDetailsPanel, c);
			
			gbl.columnWidths = new int[]{buttonGroupScrollPane.getPreferredSize().width + 100, 600};
			
			restrictedEdgeEditorPanel.validate();
			
			tabbedPane.addTab(mxResources.get("eventTAB"), restrictedEdgeEditorPanel);
		} else {
	        tabbedPane.addTab(mxResources.get("eventTAB"), scrollPane);
		}
        
        scrollPane = new JScrollPane(conditionTextPane);
        conditionTextPane.setScrollPane(scrollPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab(mxResources.get("conditionTAB"), scrollPane);
        scrollPane = new JScrollPane(exeTextPane);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        tabbedPane.addTab(mxResources.get("exeTAB"), scrollPane);
		scrollPane = new JScrollPane(commentsPane);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		tabbedPane.addTab(mxResources.get("commentsTAB"), scrollPane);
		
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
		
		SCXMLElementEditor.focusOnTextPanel(tabbedPane.getSelectedComponent());
    }
    
    private void loadPossibleEventsButtonGroup(SCXMLGraphEditor editor){
    	eventButtonGroup = new ButtonGroup();
    	//Get all outgoing event names
    	mxCell sourceCell = editor.getGraphComponent().getSCXMLNodeForID(edge.getSCXMLSource());
    	Object[] allOutgoingEdges = editor.getGraphComponent().getGraph().getAllOutgoingEdges(sourceCell);
    	List<String> existingEventsOnSourceNode = new LinkedList<String>();
    	for(Object object : allOutgoingEdges){
    		SCXMLEdge tempEdge = (SCXMLEdge)((mxCell)object).getValue();
    		existingEventsOnSourceNode.add(tempEdge.getEvent());
    	}
    	
    	String eventName;
    	int rowNumber = 0;
		for(PossibleEvent possibleEvent: sourceNode.getPossibleEvents()){
			eventName = possibleEvent.getName();
			possibleEventRadioButton = new JRadioButton(eventName);
			possibleEventRadioButton.setActionCommand(mxResources.get("changeEvent") + eventName + "#&@" + possibleEvent.getDocumentation());
			possibleEventRadioButton.addActionListener(this);
			//If the file is imported
			if (eventName.equals(edge.getEvent())) {
				setEventDocumentationLabel(possibleEvent.getDocumentation());
				possibleEventRadioButton.setSelected(true);
			} else {
				possibleEventRadioButton.setEnabled(!existingEventsOnSourceNode.contains(eventName));
			}
			eventButtonGroup.add(possibleEventRadioButton);
			//possibleEventRadioButton.setPreferredSize(new Dimension(180, 25));
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = rowNumber;
			possibleEventsButtonGroupPanel.add(possibleEventRadioButton, c);
			rowNumber++;
		}
		possibleEventsButtonGroupPanel.validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	String cmd=e.getActionCommand();
		if (cmd.startsWith(mxResources.get("changeEvent"))) {
			String[] eventProperties = cmd.split("#&@");
			String eventName = eventProperties[1];
			String eventDocumentation = eventProperties[2];
			setEventDocumentationLabel(eventDocumentation);
			eventTextPane.setText(eventName);
			this.pack();
		} else {
			super.actionPerformed(e);
		}
    }
    
    private void setEventDocumentationLabel(String eventDocumentation){
    	eventDocumentationLabel.setText(eventDocumentation.trim());
    }
    
}
