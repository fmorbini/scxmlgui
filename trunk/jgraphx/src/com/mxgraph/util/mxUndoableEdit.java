/**
 * $Id: mxUndoableEdit.java,v 1.2 2009/11/24 12:00:28 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mxgraph.model.mxGraphModel.mxChildChange;

/**
 * Implements a 2-dimensional rectangle with double precision coordinates.
 */
public class mxUndoableEdit
{

	/**
	 * Defines the requirements for an undoable change.
	 */
	public interface mxUndoableChange
	{

		/**
		 * Undoes or redoes the change depending on its undo state.
		 */
		void execute();

	}

	/**
	 * Holds the source of the undoable edit.
	 */
	protected Object source;

	/**
	 * Holds the list of changes that make up this undoable edit.
	 */
	protected List<mxUndoableChange> changes = new ArrayList<mxUndoableChange>();

	/**
	 * Specifies this undoable edit is significant. Default is true.
	 */
	protected boolean significant = true;

	/**
	 * Specifies the state of the undoable edit.
	 */
	protected boolean undone, redone;
	private boolean transparent;
	private boolean undoable=true;

	/**
	 * Constructs a new undoable edit for the given source.
	 */
	public mxUndoableEdit(Object source)
	{
		this(source, true);
	}

	/**
	 * Constructs a new undoable edit for the given source.
	 */
	public mxUndoableEdit(Object source, boolean significant)
	{
		this(source,significant,false);
	}
	public mxUndoableEdit(Object source, boolean significant,boolean transparent)
	{
		this.source = source;
		this.significant = significant;
		this.transparent=transparent;
	}

	/**
	 * Hook to notify any listeners of the changes after an undo or redo
	 * has been carried out. This implementation is empty.
	 */
	public void dispatch()
	{
		// empty
	}

	/**
	 * Hook to free resources after the edit has been removed from the command
	 * history. This implementation is empty.
	 */
	public void die()
	{
		// empty
	}

	/**
	 * @return the source
	 */
	public Object getSource()
	{
		return source;
	}

	/**
	 * @return the changes
	 */
	public List<mxUndoableChange> getChanges()
	{
		return changes;
	}

	/**
	 * @return the significant
	 */
	public boolean isSignificant()
	{
		return significant;
	}

	/**
	 * @return the undone
	 */
	public boolean isUndone()
	{
		return undone;
	}

	/**
	 * @return the redone
	 */
	public boolean isRedone()
	{
		return redone;
	}

	/**
	 * Returns true if the this edit contains no changes.
	 */
	public boolean isEmpty()
	{
		return changes.isEmpty();
	}

	/**
	 * Adds the specified change to this edit. The change is an object that is
	 * expected to either have an undo and redo, or an execute function.
	 */
	public void add(mxUndoableChange change)
	{
		changes.add(change);
	}

	/**
	 * 
	 */
	public void undo()
	{
		if (!undone && !transparent)
		{
			int count = changes.size();

			for (int i = count - 1; i >= 0; i--)
			{
				mxUndoableChange change = (mxUndoableChange) changes.get(i);
				change.execute();
			}

			undone = true;
			redone = false;
		}

		dispatch();
	}

	/**
	 * 
	 */
	public void redo()
	{
		if (!redone && !transparent)
		{
			int count = changes.size();

			for (int i = 0; i < count; i++)
			{
				mxUndoableChange change = (mxUndoableChange) changes.get(i);
				change.execute();
			}

			undone = false;
			redone = true;
		}

		dispatch();
	}

	public void setTransparent(boolean t) {
		transparent=t;
	}
	public boolean getTransparent() {
		return transparent;
	}
	public void setUndoable(boolean u) {
		undoable=u;
	}
	public boolean getUndoable() {
		return undoable;
	}

	public HashSet<Object> getAffectedObjects() {
		HashSet<Object> modifiedObjects=new HashSet<Object>();
		for (mxUndoableChange c:getChanges()) {
			if (c instanceof mxChildChange) {
				Object o = ((mxChildChange) c).getChild();
				if (o!=null) modifiedObjects.add(o);
			}
		}
		return modifiedObjects;
	}
	
}
