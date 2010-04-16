package com.mxgraph.sharing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a diagram that may be shared among multiple sessions.
 */
public class mxSharedDiagram
{

	/**
	 * Defines the requirements for an object that listens to changes on the
	 * shared diagram.
	 */
	public interface mxDiagramChangeListener
	{

		/**
		 * Fires when the shared diagram was changed.
		 * 
		 * @param sender Session where the change was received from.
		 * @param xml XML string that represents the change.
		 */
		void diagramChanged(Object sender, String xml);
	}

	/**
	 * Holds a list of diagram change listeners.
	 */
	protected List<mxDiagramChangeListener> diagramChangeListeners;

	/**
	 * Holds the initial state of the diagram.
	 */
	protected String initialState;

	/**
	 * Holds the history of all changes of initial state.
	 */
	protected StringBuffer history = new StringBuffer();

	/**
	 * Constructs a new diagram with the given initial state.
	 * 
	 * @param initialState Initial state of the diagram.
	 */
	public mxSharedDiagram(String initialState)
	{
		this.initialState = initialState;
	}

	/**
	 * Returns the initial state of the diagram.
	 */
	public String getInitialState()
	{
		return initialState;
	}

	/**
	 * Clears the history of all changes.
	 */
	public synchronized void clearHistory()
	{
		history = new StringBuffer();
	}

	/**
	 * Returns the history of all changes as a string.
	 */
	public synchronized String getDelta()
	{
		return history.toString();
	}

	/**
	 * Appends the given string to the history and dispatches the change to all
	 * sessions that are listening to this shared diagram.
	 * 
	 * @param sender Session where the change originated from.
	 * @param xml XML string that represents the change.
	 */
	public void dispatch(Object sender, String xml)
	{
		synchronized (this)
		{
			history.append(xml);
		}

		dispatchDiagramChangeEvent(sender, xml);
	}

	/**
	 * Adds the given listener to the list of diagram change listeners.
	 * 
	 * @param listener Diagram change listener to be added.
	 */
	public void addDiagramChangeListener(mxDiagramChangeListener listener)
	{
		if (diagramChangeListeners == null)
		{
			diagramChangeListeners = new ArrayList<mxDiagramChangeListener>();
		}

		diagramChangeListeners.add(listener);
	}

	/**
	 * Removes the given listener from the list of diagram change listeners.
	 * 
	 * @param listener Diagram change listener to be removed.
	 */
	public void removeDiagramChangeListener(mxDiagramChangeListener listener)
	{
		if (diagramChangeListeners != null)
		{
			diagramChangeListeners.remove(listener);
		}
	}

	/**
	 * Dispatches the given event information to all diagram change listeners.
	 * 
	 * @param sender Session where the change was received from.
	 * @param xml XML string that represents the change.
	 */
	void dispatchDiagramChangeEvent(Object sender, String xml)
	{
		if (diagramChangeListeners != null)
		{
			Iterator<mxDiagramChangeListener> it = diagramChangeListeners
					.iterator();

			while (it.hasNext())
			{
				mxDiagramChangeListener listener = it.next();
				listener.diagramChanged(sender, xml);
			}
		}
	}

}
