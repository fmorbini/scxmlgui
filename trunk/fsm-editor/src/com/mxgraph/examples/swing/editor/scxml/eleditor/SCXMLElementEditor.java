package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.examples.swing.editor.utils.AbstractActionWrapper;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.util.CellSelector;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

public class SCXMLElementEditor extends JDialog implements ActionListener {

	private static final long serialVersionUID = 3563719047023065063L;
	
	public static final String undoAction="Undo"; 
	public static final String redoAction="Redo"; 
	public final CloseAction closeAction; 
	protected JTabbedPane tabbedPane;
	
    protected HashMap<Object, Action> actions=new HashMap<Object, Action>();;

	private SCXMLGraphEditor editor;

	protected EditorKeyboardHandler keyboardHandler=null;
    
	private AbstractActionWrapper externalUndoAction=null,externalRedoAction=null;

	private mxCell cell=null;

	protected JButton idButton;
	private CellSelector cellSelector;
	
	public static enum Type {EDGE,NODE,OUTGOING_EDGE_ORDER, OUTSOURCING};
	
    public SCXMLElementEditor(JFrame parent, SCXMLGraphEditor e,mxCell cell) {    	
    	super(parent);
    	this.cell=cell;
    	closeAction=new CloseAction();
    	editor=e;
    	externalUndoAction = editor.bind(mxResources.get("undo"), null,"/com/mxgraph/examples/swing/images/undo.gif");
    	externalRedoAction = editor.bind(mxResources.get("redo"), null,"/com/mxgraph/examples/swing/images/redo.gif");
    	keyboardHandler=new EditorKeyboardHandler(this);
    	
    	cellSelector=new CellSelector(editor.getGraphComponent());
    	
		idButton = new JButton(mxResources.get("showCell"));
		idButton.setActionCommand("id");
		idButton.addActionListener(this);
		idButton.setEnabled(true);
		
		getContentPane().add(idButton, BorderLayout.SOUTH);
    }

    protected HashMap<Object, Action> updateActionTable(JTabbedPane tabbedPane,HashMap<Object, Action> actions) {
    	Component o=tabbedPane.getSelectedComponent();
    	if (o instanceof JScrollPane) {
    		// put focus on text field
            focusOnTextPanel(o);

    		JScrollPane scrollPane=(JScrollPane) o;
    		o=scrollPane.getViewport().getComponent(0);
    		//o=scrollPane.getComponent(0);
    		if (o instanceof UndoJTextPane) {
    	    	UndoJTextPane u;
    			u=(UndoJTextPane) o;
    			ActionMap actionMap = u.getActionMap();
    			actions.put(DefaultEditorKit.copyAction,actionMap.get(DefaultEditorKit.copyAction));
    			actions.put(DefaultEditorKit.cutAction,actionMap.get(DefaultEditorKit.cutAction));
    			actions.put(DefaultEditorKit.pasteAction,actionMap.get(DefaultEditorKit.pasteAction));
    			actions.put(DefaultEditorKit.selectAllAction,actionMap.get(DefaultEditorKit.selectAllAction));
    			UndoJTextPane.UndoAction ua=u.getUndoAction();
    			UndoJTextPane.RedoAction ra=u.getRedoAction();
    			actions.put(undoAction,ua);
    			actions.put(redoAction,ra);
    			//System.out.println("update actions, pane side, before");
    			if ((externalUndoAction!=null) && (externalRedoAction!=null)) {
        			//System.out.println("update actions, pane side, after");
    				if (ua.getExternalAction()==null) ua.setExternalAction(externalUndoAction);
    				if (ra.getExternalAction()==null) ra.setExternalAction(externalRedoAction);
    		    	externalUndoAction.setInternalAction(ua);
    		    	externalRedoAction.setInternalAction(ra);
    		    	ua.updateUndoState();
    		    	ra.updateRedoState();
    			}
    			if (keyboardHandler!=null) keyboardHandler.updateActionMap();
    			return actions;
    		} else if (o instanceof UndoJTextField) {
    	    	UndoJTextField u;
    			u=(UndoJTextField) o;
    			ActionMap actionMap = u.getActionMap();
    			actions.put(DefaultEditorKit.copyAction,actionMap.get(DefaultEditorKit.copyAction));
    			actions.put(DefaultEditorKit.cutAction,actionMap.get(DefaultEditorKit.cutAction));
    			actions.put(DefaultEditorKit.pasteAction,actionMap.get(DefaultEditorKit.pasteAction));
    			actions.put(DefaultEditorKit.selectAllAction,actionMap.get(DefaultEditorKit.selectAllAction));
    			UndoJTextField.UndoAction ua=u.getUndoAction();
    			UndoJTextField.RedoAction ra=u.getRedoAction();
    			actions.put(undoAction,ua);
    			actions.put(redoAction,ra);
    			//System.out.println("update actions, field side, before");
    			if ((externalUndoAction!=null) && (externalRedoAction!=null)) {
        			//System.out.println("update actions, field side, after");
    				if (ua.getExternalAction()==null) ua.setExternalAction(externalUndoAction);
    				if (ra.getExternalAction()==null) ra.setExternalAction(externalRedoAction);
    		    	externalUndoAction.setInternalAction(ua);    	
    		    	externalRedoAction.setInternalAction(ra);
    		    	ua.updateUndoState();
    		    	ra.updateRedoState();
    			}
    			if (keyboardHandler!=null) keyboardHandler.updateActionMap();
    			return actions;
    		}
    	}
    	return null;
    }
    
	public class CloseAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			cellSelector.unselectAll();
			dispose();
			try {
				editor.getSCXMLSearchTool().updateCellInIndex(cell,true);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}    
	
	// any time a change is made to the document, the scxml editor "modified" flag is set 
    protected class DocumentChangeListener implements DocumentListener {
    	private SCXMLGraphEditor editor;
    	private final List<mxUndoableChange> changes=new ArrayList<mxUndoableEdit.mxUndoableChange>();
        public DocumentChangeListener(SCXMLGraphEditor e) {
    		this.editor=e;
		}
		public void insertUpdate(DocumentEvent e) {
			mxGraphModel model = (mxGraphModel) editor.getGraphComponent().getGraph().getModel();
			model.fireEvent(new mxEventObject(mxEvent.CHANGE,"changes",changes,"revalidate",false));
        }
        public void removeUpdate(DocumentEvent e) {
			mxGraphModel model = (mxGraphModel) editor.getGraphComponent().getGraph().getModel();
			model.fireEvent(new mxEventObject(mxEvent.CHANGE,"changes",changes,"revalidate",false));
        }
        public void changedUpdate(DocumentEvent e) {
			mxGraphModel model = (mxGraphModel) editor.getGraphComponent().getGraph().getModel();
			model.fireEvent(new mxEventObject(mxEvent.CHANGE,"changes",changes,"revalidate",false));
        }
    }

    public Action getActionByName(String name) {
        return actions.get(name);
    }
    
    
    protected JMenu createEditMenu() {
    	JMenu menu = new JMenu(mxResources.get("edit"));
    	menu.removeAll();

        menu.add(externalUndoAction);
        menu.add(externalRedoAction);

        menu.addSeparator();

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
 		menu.add(editor.bind(mxResources.get("cut"), getActionByName(DefaultEditorKit.cutAction), "/com/mxgraph/examples/swing/images/cut.gif"));
		menu.add(editor.bind(mxResources.get("copy"), getActionByName(DefaultEditorKit.copyAction),"/com/mxgraph/examples/swing/images/copy.gif"));
		menu.add(editor.bind(mxResources.get("paste"), getActionByName(DefaultEditorKit.pasteAction),"/com/mxgraph/examples/swing/images/paste.gif"));

        menu.addSeparator();

        menu.add(editor.bind(mxResources.get("selectAll"),getActionByName(DefaultEditorKit.selectAllAction),null));
        return menu;
    }

	public static void focusOnTextPanel(Component component) {
		if (component instanceof JScrollPane) {
			JScrollPane scrollPane=(JScrollPane) component;
			component=scrollPane.getViewport().getComponent(0);
			component.requestFocus();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd=e.getActionCommand();
		if (cmd.equals("id")) {
			cellSelector.updateSelection(cell);
		}
	}
}