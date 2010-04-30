package com.mxgraph.examples.swing.editor.scxml.eleditor;

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.Point;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextField;
import com.mxgraph.examples.swing.editor.scxml.UndoJTextPane;
import com.mxgraph.examples.swing.editor.utils.AbstractActionWrapper;
import com.mxgraph.util.mxResources;

public class SCXMLElementEditor extends JFrame {

	private static final long serialVersionUID = 3563719047023065063L;
	
	public static final String undoAction="Undo"; 
	public static final String redoAction="Redo"; 
	
    protected HashMap<Object, Action> actions=new HashMap<Object, Action>();;

	private SCXMLGraphEditor editor;

	protected EditorKeyboardHandler keyboardHandler=null;
    
	private AbstractActionWrapper externalUndoAction,externalRedoAction;

    public SCXMLElementEditor(SCXMLGraphEditor e) {
    	editor=e;
    	externalUndoAction = editor.bind(mxResources.get("undo"), null,"/com/mxgraph/examples/swing/images/undo.gif");
    	externalRedoAction = editor.bind(mxResources.get("redo"), null,"/com/mxgraph/examples/swing/images/redo.gif");
    	keyboardHandler=new EditorKeyboardHandler(this);
	}

	//The following two methods allow us to find an
    //action provided by the editor kit by its name.
    protected HashMap<Object, Action> updateActionTable(JTabbedPane tabbedPane,HashMap<Object, Action> actions) {
    	Object o=tabbedPane.getSelectedComponent();
    	if (o instanceof JScrollPane) {
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
    			if ((externalUndoAction!=null) && (externalRedoAction!=null)) {
    				if (u.getUndoAction().getExternalAction()==null) ua.setExternalAction(externalUndoAction);
    				if (u.getRedoAction().getExternalAction()==null) ra.setExternalAction(externalRedoAction);
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
    			if ((externalUndoAction!=null) && (externalRedoAction!=null)) {
    				if (u.getUndoAction().getExternalAction()==null) ua.setExternalAction(externalUndoAction);
    				if (u.getRedoAction().getExternalAction()==null) ra.setExternalAction(externalRedoAction);
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
    
    // any time a change is made to the document, the scxml editor "modified" flag is set 
    protected class DocumentChangeListener implements DocumentListener {
    	private SCXMLGraphEditor editor;
        public DocumentChangeListener(SCXMLGraphEditor e) {
    		this.editor=e;
		}
		public void insertUpdate(DocumentEvent e) {
			editor.setModified(true);
        }
        public void removeUpdate(DocumentEvent e) {
			editor.setModified(true);
        }
        public void changedUpdate(DocumentEvent e) {
			editor.setModified(true);
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

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     * @param editor 
     * @param pos 
     */
    public void showSCXMLElementEditor(Point pos) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocation(pos);
        //Display the window.
        pack();
        setVisible(true);
    }
}