/**
 * $Id: mxSubHandler.java,v 1.4 2010/02/01 10:14:27 gaudenz Exp $
 * Copyright (c) 2008, Gaudenz Alder
 * 
 * Known issue: Drag image size depends on the initial position and may sometimes
 * not align with the grid when dragging. This is because the rounding of the width
 * and height at the initial position may be different than that at the current
 * position as the left and bottom side of the shape must align to the grid lines.
 */
package com.mxgraph.swing.handler;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxMouseRedirector;
import com.mxgraph.swing.util.mxMouseControl;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class mxSubHandler extends mxMouseControl
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -882368002120921842L;

	/**
	 * Defines the default value for maxHandlers. Default is 100.
	 */
	public static int DEFAULT_MAX_HANDLERS = 100;

	/**
	 * Reference to the enclosing graph component.
	 */
	protected mxGraphComponent graphComponent;

	/**
	 * Defines the maximum number of handlers to paint individually.
	 * Default is DEFAULT_MAX_HANDLES.
	 */
	protected int maxHandlers = DEFAULT_MAX_HANDLERS;

	/**
	 * Maps from cells to handlers in the order of the selection cells.
	 */
	protected transient Map<Object, mxCellHandler> handlers = new LinkedHashMap<Object, mxCellHandler>();

	/**
	 * 
	 */
	protected transient mxIEventListener refreshHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			refresh();
		}
	};

	/**
	 * 
	 * @param graphComponent
	 */
	public mxSubHandler(final mxGraphComponent graphComponent)
	{
		this.graphComponent = graphComponent;

		// Adds component for rendering the handles (preview is separate)
		graphComponent.getGraphControl().add(this, 0);

		// Listens to all mouse events on the rendering control
		graphComponent.getGraphControl().addMouseListener(this);
		graphComponent.getGraphControl().addMouseMotionListener(this);

		// Refreshes the handles after any changes
		graphComponent.getGraph().getSelectionModel().addListener(
				mxEvent.CHANGE, refreshHandler);
		graphComponent.getGraph().addListener(mxEvent.REPAINT, refreshHandler);

		// Refreshes the handles if moveVertexLabels or moveEdgeLabels changes
		graphComponent.getGraph().addPropertyChangeListener(
				new PropertyChangeListener()
				{

					/*
					 * (non-Javadoc)
					 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
					 */
					public void propertyChange(PropertyChangeEvent evt)
					{
						if (evt.getPropertyName().equals("vertexLabelsMovable")
								|| evt.getPropertyName().equals(
										"edgeLabelsMovable"))
						{
							refresh();
						}
					}

				});

		// Redirects events from component to rendering control so
		// that the event handling order is maintained if other controls
		// such as overlays are added to the component hierarchy and
		// consume events before they reach the rendering control
		mxMouseRedirector redirector = new mxMouseRedirector(graphComponent);
		addMouseMotionListener(redirector);
		addMouseListener(redirector);
	}

	/**
	 * 
	 */
	public mxGraphComponent getGraphComponent()
	{
		return graphComponent;
	}

	/**
	 * 
	 */
	public int getMaxHandlers()
	{
		return maxHandlers;
	}

	/**
	 * 
	 */
	public void setMaxHandlers(int value)
	{
		maxHandlers = value;
	}

	/**
	 * 
	 */
	public mxCellHandler getHandler(Object cell)
	{
		return (mxCellHandler) handlers.get(cell);
	}

	/**
	 * Dispatches the mousepressed event to the subhandles. This is
	 * called from the connection handler as subhandles have precedence
	 * over the connection handler.
	 */
	public void mousePressed(MouseEvent e)
	{
		if (graphComponent.isEnabled()
				&& !graphComponent.isForceMarqueeEvent(e) && isEnabled())
		{
			Iterator<mxCellHandler> it = handlers.values().iterator();

			while (it.hasNext() && !e.isConsumed())
			{
				it.next().mousePressed(e);
			}
		}
	}

	/**
	 * 
	 */
	public void mouseMoved(MouseEvent e)
	{
		if (graphComponent.isEnabled() && isEnabled())
		{
			//System.out.println("mouse moved in mxSubHandler");
			
			Iterator<mxCellHandler> it = handlers.values().iterator();

			while (it.hasNext() && !e.isConsumed())
			{
				it.next().mouseMoved(e);
			}
		}
	}

	/**
	 * 
	 */
	public void mouseDragged(MouseEvent e)
	{
		if (graphComponent.isEnabled() && isEnabled())
		{
			//System.out.println("mouse dragged in mxSubHandler");

			Iterator<mxCellHandler> it = handlers.values().iterator();

			while (it.hasNext() && !e.isConsumed())
			{
				it.next().mouseDragged(e);
			}
		}
	}

	/**
	 * 
	 */
	public void mouseReleased(MouseEvent e)
	{
		if (graphComponent.isEnabled() && isEnabled())
		{
			Iterator<mxCellHandler> it = handlers.values().iterator();

			while (it.hasNext() && !e.isConsumed())
			{
				it.next().mouseReleased(e);
			}
		}

		reset();
	}

	/**
	 * Redirects the tooltip handling of the JComponent to the graph
	 * component, which in turn may use getHandleToolTipText in this class to
	 * find a tooltip associated with a handle.
	 */
	public String getToolTipText(MouseEvent e)
	{
		MouseEvent tmp = SwingUtilities.convertMouseEvent(e.getComponent(), e,
				graphComponent.getGraphControl());
		Iterator<mxCellHandler> it = handlers.values().iterator();
		String tip = null;

		while (it.hasNext() && tip == null)
		{
			tip = it.next().getToolTipText(tmp);
		}

		// Redirects tooltips to main graph control
		if (tip == null || tip.length() == 0)
		{
			tip = graphComponent.getGraphControl().getToolTipText(tmp);
		}

		return tip;
	}

	/**
	 * 
	 */
	public void reset()
	{
		Iterator<mxCellHandler> it = handlers.values().iterator();

		while (it.hasNext())
		{
			it.next().reset();
		}
	}

	/**
	 * 
	 */
	public void refresh()
	{
		mxGraph graph = graphComponent.getGraph();

		// Creates a new map for the handlers and tries to
		// to reuse existing handlers from the old map
		Map<Object, mxCellHandler> oldHandlers = handlers;
		handlers = new LinkedHashMap<Object, mxCellHandler>();

		// Creates handles for all selection cells
		Object[] tmp = graph.getSelectionCells();
		boolean handlesVisible = tmp.length <= getMaxHandlers();
		Rectangle handleBounds = null;

		for (int i = 0; i < tmp.length; i++)
		{
			mxCellState state = graph.getView().getState(tmp[i]);

			if (state != null)
			{
				mxCellHandler handler = (mxCellHandler) oldHandlers.get(tmp[i]);

				if (handler != null)
				{
					handler.refresh(state);
				}
				else
				{
					handler = graphComponent.createHandler(state);
				}

				if (handler != null)
				{
					handler.setHandlesVisible(handlesVisible);
					handlers.put(tmp[i], handler);

					if (handleBounds == null)
					{
						handleBounds = handler.getBounds();
					}
					else
					{
						handleBounds.add(handler.getBounds());
					}
				}
			}
		}

		// Constructs an array with cells that are indeed movable
		setVisible(!handlers.isEmpty());

		if (isVisible())
		{
			// Updates the bounds of the component to draw the subhandlers
			if (handleBounds != null)
			{
				handleBounds.grow(1, 1);
				setBounds(handleBounds);
			}
			else
			{
				setBounds(graphComponent.getViewport().getVisibleRect());
			}

			// Repaints only if the handler is visible
			repaint();
		}
	}

	/**
	 * 
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (isVisible())
		{
			g.translate(-getX(), -getY());
			Iterator<mxCellHandler> it = handlers.values().iterator();

			while (it.hasNext())
			{
				it.next().paint(g);
			}

			g.translate(getX(), getY());
		}
	}

}
