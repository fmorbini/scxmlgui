package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
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

    public SCXMLEdgeEditor(JFrame parent,mxCell en,SCXMLEdge e, SCXMLGraphEditor editor) {
    	super(parent,editor,en);
    	setTitle(mxResources.get("titleEdgeEditor"));

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
        	possibleEventsButtonGroupPanel = new JPanel(new GridBagLayout());
        	buttonGroupScrollPane = new JScrollPane(possibleEventsButtonGroupPanel);
			buttonGroupScrollPane.setPreferredSize(new Dimension(200, 200));
			restrictedEdgeEditorPanel = new JPanel(new GridLayout(0, 2));
			restrictedEdgeEditorPanel.add(buttonGroupScrollPane);
			possibleEventDetailsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			eventTextPane.setEditable(false);
			JLabel eventNameTitleLabel = new JLabel(mxResources.get("eventNameTitle"));
			eventNameTitleLabel.setPreferredSize(new Dimension(200, 15));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.1;
			c.gridx = 0;
			c.gridy = 0;
			possibleEventDetailsPanel.add(eventNameTitleLabel, c);
			scrollPane.setPreferredSize(new Dimension(200, 60));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.5;
			c.gridx = 0;
			c.gridy = 1;
			possibleEventDetailsPanel.add(scrollPane, c);
			JLabel evenDocumentationTitleLabel = new JLabel(mxResources.get("eventDocumentationTitle"));
			evenDocumentationTitleLabel.setPreferredSize(new Dimension(200, 10));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.1;
			c.gridx = 0;
			c.gridy = 2;
			possibleEventDetailsPanel.add(evenDocumentationTitleLabel, c);
			eventDocumentationLabel.setPreferredSize(new Dimension(200, 60));
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.5;
			c.gridx = 0;
			c.gridy = 3;
			possibleEventDetailsPanel.add(eventDocumentationLabel, c);
			restrictedEdgeEditorPanel.add(possibleEventDetailsPanel);
			
			loadPossibleEventsButtonGroup();
			
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
    
    private void loadPossibleEventsButtonGroup(){
    	eventButtonGroup = new ButtonGroup();
    	String eventName;
    	int rowNumber = 0;
		for(PossibleEvent possibleEvent: sourceNode.getPossibleEvents()){
			eventName = possibleEvent.getName();
			possibleEventRadioButton = new JRadioButton(eventName);
			possibleEventRadioButton.setActionCommand(mxResources.get("changeEvent") + eventName + ":" + possibleEvent.getDocumentation());
			possibleEventRadioButton.addActionListener(this);
			//If the file is imported
			if (eventName.equals(edge.getEvent())) {
				setEventDocumentationLabel(possibleEvent.getDocumentation());
				possibleEventRadioButton.setSelected(true);
			}
			eventButtonGroup.add(possibleEventRadioButton);
			possibleEventRadioButton.setPreferredSize(new Dimension(180, 25));
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = rowNumber;
			possibleEventsButtonGroupPanel.add(possibleEventRadioButton, c);
			rowNumber++;
		}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	String cmd=e.getActionCommand();
		if (cmd.startsWith(mxResources.get("changeEvent"))) {
			String[] eventProperties = cmd.split(":");
			String eventName = eventProperties[1];
			String eventDocumentation = eventProperties[2];
			setEventDocumentationLabel(eventDocumentation);
			eventTextPane.setText(eventName);
		} else {
			super.actionPerformed(e);
		}
    }
    
    private void setEventDocumentationLabel(String eventDocumentation){
    	//html formatted text usage because of auto wrap
    	eventDocumentationLabel.setText("<html><p>" + eventDocumentation + "</p></html>");
    }
    
}
