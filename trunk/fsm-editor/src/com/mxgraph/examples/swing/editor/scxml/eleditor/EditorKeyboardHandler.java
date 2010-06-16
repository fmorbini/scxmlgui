package com.mxgraph.examples.swing.editor.scxml.eleditor;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

public class EditorKeyboardHandler {
	
	private SCXMLElementEditor editor;

	public EditorKeyboardHandler(SCXMLElementEditor e)
	{
		editor=e;
		updateInputMap(editor.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));
	}

	protected void updateInputMap(InputMap map)
	{
		map.put(KeyStroke.getKeyStroke("control Z"), "undo");
		map.put(KeyStroke.getKeyStroke("control Y"), "redo");			
		map.put(KeyStroke.getKeyStroke("control A"), "selectAll");
		map.put(KeyStroke.getKeyStroke("CUT"), "cut");
		map.put(KeyStroke.getKeyStroke("control C"), "copy");
		map.put(KeyStroke.getKeyStroke("COPY"), "copy");
		map.put(KeyStroke.getKeyStroke("control V"), "paste");
		map.put(KeyStroke.getKeyStroke("PASTE"), "paste");
		map.put(KeyStroke.getKeyStroke("ESCAPE"), "close");
	}

	ActionMap map=null;
	public void updateActionMap()
	{
		if (map==null) {
			map = new ActionMap();
			editor.getRootPane().setActionMap(map);
		}

		map.put("undo", editor.getActionByName(SCXMLElementEditor.undoAction));
		map.put("redo", editor.getActionByName(SCXMLElementEditor.redoAction));
		map.put("selectAll", editor.getActionByName(DefaultEditorKit.selectAllAction));
		map.put("cut", editor.getActionByName(DefaultEditorKit.cutAction));
		map.put("copy", editor.getActionByName(DefaultEditorKit.copyAction));
		map.put("paste", editor.getActionByName(DefaultEditorKit.pasteAction));
		map.put("close", editor.closeAction);
	}
}
