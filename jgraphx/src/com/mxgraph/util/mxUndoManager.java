/**
 * $Id: mxUndoManager.java,v 1.12 2009/12/01 14:15:59 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;

/**
 * Implements an undo history.
 * 
 * This class fires the following events:
 * 
 * mxEvent.CLEAR fires after clear was executed. The event has no properties.
 * 
 * mxEvent.UNDO fires afer a significant edit was undone in undo. The
 * <code>edit</code> property contains the mxUndoableEdit that was undone.
 * 
 * mxEvent.REDO fires afer a significant edit was redone in redo. The
 * <code>edit</code> property contains the mxUndoableEdit that was redone.
 * 
 * mxEvent.ADD fires after an undoable edit was added to the history. The
 * <code>edit</code> property contains the mxUndoableEdit that was added.
 */
public class mxUndoManager extends mxEventSource
{
	
	long timestampOfLastEdit=new Date().getTime();
	
	int unmodifiedPosition;

	/**
	 * Maximum command history size. 0 means unlimited history. Default is 100.
	 */
	protected int size;

	/**
	 * List that contains the steps of the command history.
	 */
	protected List<List<mxUndoableEdit>> history;

	/**
	 * Index of the element to be added next.
	 */
	protected int indexOfNextAdd;

	private boolean enabled=true;
	private boolean collection=false;
	private List<mxUndoableEdit> collected=new ArrayList<mxUndoableEdit>();

	/**
	 * Constructs a new undo manager with a default history size.
	 */
	public mxUndoManager()
	{
		this(100);
	}

	/**
	 * Constructs a new undo manager for the specified size.
	 */
	public mxUndoManager(int size)
	{
		this.size = size;
		clear();
		resetUnmodifiedState();
	}

	/**
	 * 
	 */
	public boolean isEmpty()
	{
		return history.isEmpty();
	}

	/**
	 * Clears the command history.
	 */
	public void clear()
	{
		history = new ArrayList<List<mxUndoableEdit>>(size);
		indexOfNextAdd = 0;
		fireEvent(new mxEventObject(mxEvent.CLEAR));
	}

	/**
	 * Returns true if an undo is possible.
	 */
	public boolean canUndo()
	{
		return indexOfNextAdd > 0;
	}

	/**
	 * Undoes the last change.
	 */
	public Collection<Object> undo()
	{
		HashSet<Object> modifiedObjects=null;
		boolean done=false;
		while ((indexOfNextAdd > 0) && !done)
		{
			List<mxUndoableEdit> edits = history.get(--indexOfNextAdd);
			for (int i=edits.size()-1;i>=0;i--) {
				mxUndoableEdit edit = edits.get(i);
				edit.undo();
				modifiedObjects=edit.getAffectedObjects();
	
				if (edit.isSignificant())
				{
					fireEvent(new mxEventObject(mxEvent.UNDO, "edit", edit));
					done=true;
				}
			}
		}
		return modifiedObjects;
	}

	/**
	 * Returns true if a redo is possible.
	 */
	public boolean canRedo()
	{
		return indexOfNextAdd < history.size();
	}

	/**
	 * Redoes the last change.
	 */
	public Collection<Object> redo()
	{
		HashSet<Object> modifiedObjects=new HashSet<Object>();
		int n = history.size();
		boolean done=false;

		while ((indexOfNextAdd < n) && !done)
		{
			List<mxUndoableEdit> edits = history.get(indexOfNextAdd++);
			for (mxUndoableEdit edit:edits) {
				edit.redo();
				for (mxUndoableChange c:edit.getChanges()) {
					if (c instanceof mxChildChange) {
						Object o = ((mxChildChange) c).getChild();
						if (o!=null) modifiedObjects.add(o);
					}
				}
				
				if (edit.isSignificant())
				{
					fireEvent(new mxEventObject(mxEvent.REDO, "edit", edit));
					done=true;
				}
			}
		}
		return modifiedObjects;
	}

	public void setEnabled(boolean e) {
		enabled=e;
	}
	public void setCollectionMode(boolean e) {
		collection=e;
		if (!collection && (collected.size()>0)) {
			addEventList();
		}
	}

	/**
	 * Method to be called to add new undoable edits to the history.
	 */
	public void undoableEditHappened(mxUndoableEdit undoableEdit)
	{
		if (enabled) {
			if (undoableEdit.getTransparent()) {}
			else if (!undoableEdit.getUndoable()) {
				notUndoableEditHappened();
			} else if (collection) {
				collected.add(undoableEdit);
				fireEvent(new mxEventObject(mxEvent.ADD, "edit", undoableEdit));
			} else {
				collected.add(undoableEdit);
				addEventList();
				fireEvent(new mxEventObject(mxEvent.ADD, "edit", undoableEdit));
			}			
		}
	}
	private void addEventList() {
		if (collected.size()>0) {
			timestampOfLastEdit=new Date().getTime();
			trim();
			
			if (size > 0 && size == history.size())
			{
				history.remove(0);
				unmodifiedPosition--;
			}
	
			history.add(collected);
			indexOfNextAdd = history.size();
			collected=new ArrayList<mxUndoableEdit>();
		}
	}
	private boolean notUndoableEdits=false;
	public void notUndoableEditHappened() {
		notUndoableEdits=true;
	}

	/**
	 * Removes all pending steps after indexOfNextAdd from the history,
	 * invoking die on each edit. This is called from undoableEditHappened.
	 */
	protected void trim()
	{
		while (history.size() > indexOfNextAdd)
		{
			List<mxUndoableEdit> edits = (List<mxUndoableEdit>) history.remove(indexOfNextAdd);
			for (mxUndoableEdit edit:edits)edit.die();
		}
	}

	public long getTimeOfMostRecentUndoEvent() {
		return timestampOfLastEdit;
	}
	
	public void resetUnmodifiedState() {
		//System.out.println("reset= "+indexOfNextAdd+" "+unmodifiedPosition);
		unmodifiedPosition=indexOfNextAdd;
		notUndoableEdits=false;
	}
	public boolean isUnmodifiedState() {
		//System.out.println("check= "+indexOfNextAdd+" "+unmodifiedPosition);
		return (!notUndoableEdits && (indexOfNextAdd==unmodifiedPosition));
	}
}
