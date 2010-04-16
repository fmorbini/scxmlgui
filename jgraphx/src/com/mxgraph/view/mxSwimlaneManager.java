package com.mxgraph.view;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxRectangle;

public class mxSwimlaneManager extends mxEventSource
{

	/**
	 * Defines the type of the source or target terminal. The type is a string
	 * passed to mxCell.is to check if the rule applies to a cell.
	 */
	protected mxGraph graph;

	/**
	 * Optional string that specifies the value of the attribute to be passed
	 * to mxCell.is to check if the rule applies to a cell.
	 */
	protected boolean enabled;

	/**
	 * Optional string that specifies the attributename to be passed to
	 * mxCell.is to check if the rule applies to a cell.
	 */
	protected boolean horizontal;

	/**
	 * Optional string that specifies the attributename to be passed to
	 * mxCell.is to check if the rule applies to a cell.
	 */
	protected boolean siblings;

	/**
	 * Optional string that specifies the attributename to be passed to
	 * mxCell.is to check if the rule applies to a cell.
	 */
	protected boolean bubbling;

	/**
	 * 
	 */
	protected mxIEventListener addHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			if (isEnabled())
			{
				cellsAdded((Object[]) evt.getProperty("cells"));
			}
		}
	};

	/**
	 * 
	 */
	protected mxIEventListener resizeHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			if (isEnabled())
			{
				cellsResized((Object[]) evt.getProperty("cells"));
			}
		}
	};

	/**
	 * 
	 */
	public mxSwimlaneManager(mxGraph graph)
	{
		setGraph(graph);
	}

	/**
	 * 
	 */
	public boolean isSwimlaneIgnored(Object swimlane)
	{
		return !getGraph().isSwimlane(swimlane);
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param value the enabled to set
	 */
	public void setEnabled(boolean value)
	{
		enabled = value;
	}

	/**
	 * @return the bubbling
	 */
	public boolean isHorizontal()
	{
		return horizontal;
	}

	/**
	 * @param value the bubbling to set
	 */
	public void setHorizontal(boolean value)
	{
		horizontal = value;
	}

	/**
	 * @return the bubbling
	 */
	public boolean isSiblings()
	{
		return siblings;
	}

	/**
	 * @param value the bubbling to set
	 */
	public void setSiblings(boolean value)
	{
		siblings = value;
	}

	/**
	 * @return the bubbling
	 */
	public boolean isBubbling()
	{
		return bubbling;
	}

	/**
	 * @param value the bubbling to set
	 */
	public void setBubbling(boolean value)
	{
		bubbling = value;
	}

	/**
	 * @return the graph
	 */
	public mxGraph getGraph()
	{
		return graph;
	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(mxGraph graph)
	{
		if (this.graph != null)
		{
			this.graph.removeListener(addHandler);
			this.graph.removeListener(resizeHandler);
		}

		this.graph = graph;

		if (this.graph != null)
		{
			this.graph.addListener(mxEvent.ADD_CELLS, addHandler);
			this.graph.addListener(mxEvent.CELLS_RESIZED, resizeHandler);
		}
	}

	/**
	 * 
	 */
	protected void cellsAdded(Object[] cells)
	{
		if (cells != null)
		{
			mxIGraphModel model = getGraph().getModel();

			model.beginUpdate();
			try
			{
				for (int i = 0; i < cells.length; i++)
				{
					if (!isSwimlaneIgnored(cells[i]))
					{
						swimlaneAdded(cells[i]);
					}
				}
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

	/**
	 * 
	 */
	protected void swimlaneAdded(Object swimlane)
	{
		mxIGraphModel model = getGraph().getModel();

		// Tries to find existing swimlane for dimensions
		// TODO: Use parent geometry - header if inside
		// parent swimlane
		mxGeometry geo = null;
		Object parent = model.getParent(swimlane);
		int childCount = model.getChildCount(parent);

		for (int i = 0; i < childCount; i++)
		{
			Object child = model.getChildAt(parent, i);

			if (child != swimlane && !isSwimlaneIgnored(child))
			{
				geo = model.getGeometry(child);
				break;
			}
		}

		// Applies dimension to new child
		if (geo != null)
		{
			model.beginUpdate();
			try
			{
				resizeSwimlane(swimlane, geo.getWidth(), geo.getHeight());
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

	/**
	 * 
	 */
	protected void cellsResized(Object[] cells)
	{
		if (cells != null)
		{
			mxIGraphModel model = getGraph().getModel();

			model.beginUpdate();
			try
			{
				for (int i = 0; i < cells.length; i++)
				{
					if (!isSwimlaneIgnored(cells[i]))
					{
						swimlaneResized(cells[i]);
					}
				}
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

	/**
	 * 
	 */
	protected void swimlaneResized(Object swimlane)
	{
		mxIGraphModel model = getGraph().getModel();
		mxGeometry geo = model.getGeometry(swimlane);

		if (geo != null)
		{
			double w = geo.getWidth();
			double h = geo.getHeight();

			model.beginUpdate();
			try
			{
				Object parent = model.getParent(swimlane);

				if (isSiblings())
				{
					int childCount = model.getChildCount(parent);

					for (int i = 0; i < childCount; i++)
					{
						Object child = model.getChildAt(parent, i);

						if (child != swimlane && !isSwimlaneIgnored(child))
						{
							resizeSwimlane(child, w, h);
						}
					}
				}

				if (isBubbling() && !isSwimlaneIgnored(parent))
				{
					resizeParent(parent, w, h);
					swimlaneResized(parent);
				}
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

	/**
	 * 
	 */
	protected void resizeSwimlane(Object swimlane, double w, double h)
	{
		mxIGraphModel model = getGraph().getModel();
		mxGeometry geo = model.getGeometry(swimlane);

		if (geo != null)
		{
			geo = (mxGeometry) geo.clone();

			if (isHorizontal())
			{
				geo.setWidth(w);
			}
			else
			{
				geo.setHeight(h);
			}

			model.setGeometry(swimlane, geo);
		}
	}

	/**
	 * 
	 */
	protected void resizeParent(Object parent, double w, double h)
	{
		mxIGraphModel model = getGraph().getModel();
		mxGeometry geo = model.getGeometry(parent);

		if (geo != null)
		{
			geo = (mxGeometry) geo.clone();
			mxRectangle size = graph.getStartSize(parent);

			if (isHorizontal())
			{
				geo.setWidth(w + size.getWidth());
			}
			else
			{
				geo.setHeight(h + size.getHeight());
			}

			model.setGeometry(parent, geo);
		}
	}

	/**
	 * 
	 */
	public void destroy()
	{
		setGraph(null);
	}

}
