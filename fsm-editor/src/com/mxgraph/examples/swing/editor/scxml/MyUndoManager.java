package com.mxgraph.examples.swing.editor.scxml;

import java.util.Vector;

import javax.sql.rowset.spi.SyncResolver;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class MyUndoManager extends UndoManager {

	private static final long serialVersionUID = 1L;
	
	public Vector<UndoableEdit> getEdits() {		
		return edits;		
	}
	protected UndoableEdit getNextUndoableEdit() {
		int size = edits.size( );
		for (int i=size-1;i>=0;i--) {
			UndoableEdit u = (UndoableEdit)edits.elementAt(i);
			if (u.canUndo( ) && u.isSignificant( ))
				return u;
		}
		return null;
	}
	protected UndoableEdit getInitialEdit() {
		return (edits.isEmpty())?null:edits.elementAt(0);
	}

	public synchronized void undoTo(UndoableEdit to, boolean excludeTo) {
		int size = edits.size( );
		for (int i=size-1;i>=0;i--) {
			UndoableEdit u = (UndoableEdit)edits.elementAt(i);
			if (canUndo()) {
				if ((u==to) && excludeTo) return;
				else undo();
			}
			if (u==to) return;
		}
	}
	public synchronized void redoTo(UndoableEdit to, boolean excludeTo) {
		int size = edits.size( );
		for (int i=0;i<size;i++) {
			UndoableEdit u = (UndoableEdit)edits.elementAt(i);
			if (canRedo()) {
				if ((u==to) && excludeTo) return;
				else redo();
			}
			if (u==to) return;
		}
	}
	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
	}
}
