package com.mxgraph.sharing;

import com.mxgraph.sharing.mxSharedDiagram.mxDiagramChangeListener;
import com.mxgraph.util.mxUtils;

/**
 * Implements a session that may be attached to a shared diagram.
 */
public class mxSession implements mxDiagramChangeListener
{
	/**
	 * Default timeout is 10000 ms.
	 */
	public static int DEFAULT_TIMEOUT = 10000;

	/**
	 * Holds the session ID.
	 */
	protected String id;

	/**
	 * Reference to the shared diagram.
	 */
	protected mxSharedDiagram diagram;

	/**
	 * Holds the send buffer for this session.
	 */
	protected StringBuffer buffer = new StringBuffer();

	/**
	 * Holds the last active time millis.
	 */
	protected long lastTimeMillis = 0;

	/**
	 * Constructs a new session with the given ID.
	 * 
	 * @param id Specifies the session ID to be used.
	 * @param diagram Reference to the shared diagram.
	 */
	public mxSession(String id, mxSharedDiagram diagram)
	{
		this.id = id;
		this.diagram = diagram;
		this.diagram.addDiagramChangeListener(this);
		
		lastTimeMillis = System.currentTimeMillis();
	}

	/**
	 * Returns the session ID.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Returns an XML string that represents the current state of the session
	 * and the shared diagram. A globally unique ID is used as the session's
	 * namespace, which is used on the client side to prefix IDs of newly
	 * created cells.
	 */
	public String getInitialState()
	{
		String ns = mxUtils.getMd5Hash(id);
		
		StringBuffer result = new StringBuffer("<state session-id=\"" + id
				+ "\" namespace=\"" + ns + "\">");
		result.append(diagram.getInitialState());
		result.append("<delta>");
		result.append(diagram.getDelta());
		result.append("</delta>");
		result.append("</state>");
		
		return result.toString();
	}

	/**
	 * Initializes the session buffer and returns a string that represents the
	 * state of the session.
	 *
	 * @return Returns the initial state of the session.
	 */
	public synchronized String init()
	{
		synchronized (this)
		{
			buffer = new StringBuffer();
			notify();
		}
		
		return getInitialState();
	}

	/**
	 * Posts the change represented by the given XML string to the shared diagram.
	 * 
	 * @param xml XML string that represents the change.
	 */
	public void post(String xml)
	{
		diagram.dispatch(this, xml);
	}

	/**
	 * Returns the changes received by other sessions for the shared diagram.
	 * The method returns an empty XML node if no change was received within
	 * 10 seconds.
	 * 
	 * @return Returns a string representing the changes to the shared diagram.
	 */
	public String poll() throws InterruptedException
	{
		return poll(DEFAULT_TIMEOUT);
	}

	/**
	 * Returns the changes received by other sessions for the shared diagram.
	 * The method returns an empty XML node if no change was received within
	 * the given timeout.
	 * 
	 * @param timeout Time in milliseconds to wait for changes.
	 * @return Returns a string representing the changes to the shared diagram.
	 */
	public String poll(long timeout) throws InterruptedException
	{
		lastTimeMillis = System.currentTimeMillis();
		String result = "<delta/>";
		
		synchronized (this)
		{
			if (buffer.length() == 0)
			{
				wait(timeout);
			}
			
			if (buffer.length() > 0)
			{
				result = "<delta>" + buffer.toString() + "</delta>";
				buffer = new StringBuffer();
			}
			
			notify();
		}
		
		return result;
	}

	/**
	 * Returns the number of milliseconds this session has been inactive.
	 */
	public long inactiveTimeMillis()
	{
		return System.currentTimeMillis() - lastTimeMillis;
	}

	/* (non-Javadoc)
	 * @see com.mxgraph.sharing.mxSharedDiagram.mxDiagramChangeListener#diagramChanged(java.lang.Object, java.lang.String)
	 */
	public synchronized void diagramChanged(Object sender, String xml)
	{
		if (sender != this)
		{
			synchronized (this)
			{
				buffer.append(xml);
				notify();
			}
		}
	}

	/**
	 * Destroys the session and removes its listener from the shared diagram.
	 */
	public void destroy()
	{
		diagram.removeDiagramChangeListener(this);
	}

}
