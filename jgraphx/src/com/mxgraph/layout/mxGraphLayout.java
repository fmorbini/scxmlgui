/**
 * $Id: mxGraphLayout.java,v 1.19 2010/01/13 10:43:46 gaudenz Exp $
 * Copyright (c) 2008-2009, JGraph Ltd
 */
package com.mxgraph.layout;

import java.util.List;
import java.util.Map;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * Abstract bass class for layouts
 */
public abstract class mxGraphLayout implements mxIGraphLayout
{

	/**
	 * Holds the enclosing graph.
	 */
	protected mxGraph graph;

	/**
	 * Boolean indicating if the bounding box of the label should be used if
	 * its available. Default is true.
	 */
	protected boolean useBoundingBox = true;

	/**
	 * Constructs a new fast organic layout for the specified graph.
	 */
	public mxGraphLayout(mxGraph graph)
	{
		this.graph = graph;
	}

	/* (non-Javadoc)
	 * @see com.mxgraph.layout.mxIGraphLayout#move(java.lang.Object, double, double)
	 */
	public void moveCell(Object cell, double x, double y)
	{
		// TODO: Map the position to a child index for
		// the cell to be placed closest to the position
	}

	/**
	 * Returns the associated graph.
	 */
	public mxGraph getGraph()
	{
		return graph;
	}

	/**
	 * Returns the constraint for the given key and cell. This implementation
	 * always returns the value for the given key in the style of the given
	 * cell.
	 * 
	 * @param key Key of the constraint to be returned.
	 * @param cell Cell whose constraint should be returned.
	 */
	public Object getConstraint(Object key, Object cell)
	{
		return getConstraint(key, cell, null, false);
	}

	/**
	 * Returns the constraint for the given key and cell. The optional edge and
	 * source arguments are used to return inbound and outgoing routing-
	 * constraints for the given edge and vertex. This implementation always
	 * returns the value for the given key in the style of the given cell.
	 * 
	 * @param key Key of the constraint to be returned.
	 * @param cell Cell whose constraint should be returned.
	 * @param edge Optional cell that represents the connection whose constraint
	 * should be returned. Default is null.
	 * @param source Optional boolean that specifies if the connection is incoming
	 * or outgoing. Default is false.
	 */
	public Object getConstraint(Object key, Object cell, Object edge,
			boolean source)
	{
		mxCellState state = graph.getView().getState(cell);
		Map<String, Object> style = (state != null) ? state.getStyle() : graph
				.getCellStyle(cell);

		return (style != null) ? style.get(key) : null;
	}

	/**
	 * @return the useBoundingBox
	 */
	public boolean isUseBoundingBox()
	{
		return useBoundingBox;
	}

	/**
	 * @param useBoundingBox the useBoundingBox to set
	 */
	public void setUseBoundingBox(boolean useBoundingBox)
	{
		this.useBoundingBox = useBoundingBox;
	}

	/**
	 * Returns true if the given vertex may be moved by the layout.
	 * 
	 * @param vertex Object that represents the vertex to be tested.
	 * @return Returns true if the vertex can be moved.
	 */
	public boolean isVertexMovable(Object vertex)
	{
		return graph.isCellMovable(vertex);
	}

	/**
	 * Returns true if the given vertex has no connected edges.
	 * 
	 * @param vertex Object that represents the vertex to be tested.
	 * @return Returns true if the vertex should be ignored.
	 */
	public boolean isVertexIgnored(Object vertex)
	{
		return !graph.getModel().isVertex(vertex)
				|| !graph.isCellVisible(vertex);
	}

	/**
	 * Returns true if the given edge has no source or target terminal.
	 * 
	 * @param edge Object that represents the edge to be tested.
	 * @return Returns true if the edge should be ignored.
	 */
	public boolean isEdgeIgnored(Object edge)
	{
		mxIGraphModel model = graph.getModel();

		return !model.isEdge(edge) || !graph.isCellVisible(edge)
				|| model.getTerminal(edge, true) == null
				|| model.getTerminal(edge, false) == null;
	}

	/**
	 * Disables or enables the edge style of the given edge.
	 */
	public void setEdgeStyleEnabled(Object edge, boolean value)
	{
		graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, (value) ? "0" : "1",
				new Object[] { edge });
	}

	/**
	 * Sets the control points of the given edge to the given
	 * list of mxPoints. Set the points to null to remove all
	 * existing points for an edge.
	 */
	public void setEdgePoints(Object edge, List<mxPoint> points)
	{
		mxIGraphModel model = graph.getModel();
		mxGeometry geometry = model.getGeometry(edge);

		if (geometry == null)
		{
			geometry = new mxGeometry();
			geometry.setRelative(true);
		}
		else
		{
			geometry = (mxGeometry) geometry.clone();
		}

		geometry.setPoints(points);
		model.setGeometry(edge, geometry);
	}

	/**
	 * Returns an <mxRectangle> that defines the bounds of the given cell
	 * or the bounding box if <useBoundingBox> is true.
	 */
	public mxRectangle getVertexBounds(Object vertex)
	{
		mxRectangle geo = graph.getModel().getGeometry(vertex);

		// Checks for oversize label bounding box and corrects
		// the return value accordingly
		if (useBoundingBox)
		{
			mxCellState state = graph.getView().getState(vertex);

			if (state != null)
			{
				double scale = graph.getView().getScale();
				mxRectangle tmp = state.getBoundingBox();

				double dx0 = (tmp.getX() - state.getX()) / scale;
				double dy0 = (tmp.getY() - state.getY()) / scale;
				double dx1 = (tmp.getX() + tmp.getWidth() - state.getX() - state
						.getWidth())
						/ scale;
				double dy1 = (tmp.getY() + tmp.getHeight() - state.getY() - state
						.getHeight())
						/ scale;

				geo = new mxRectangle(geo.getX() + dx0, geo.getY() + dy0, geo
						.getWidth()
						- dx0 + dx1, geo.getHeight() + -dy0 + dy1);
			}
		}

		return new mxRectangle(geo);
	}

	/**
	 * Sets the new position of the given cell taking into account the size of
	 * the bounding box if <useBoundingBox> is true. The change is only carried
	 * out if the new location is not equal to the existing location, otherwise
	 * the geometry is not replaced with an updated instance. The new or old
	 * bounds are returned (including overlapping labels).
	 * 
	 * Parameters:
	 * 
	 * cell - <mxCell> whose geometry is to be set.
	 * x - Integer that defines the x-coordinate of the new location.
	 * y - Integer that defines the y-coordinate of the new location.
	 */
	public mxRectangle setVertexLocation(Object vertex, double x, double y)
	{
		mxIGraphModel model = graph.getModel();
		mxGeometry geometry = model.getGeometry(vertex);
		mxRectangle result = null;

		if (geometry != null)
		{
			result = new mxRectangle(x, y, geometry.getWidth(), geometry
					.getHeight());

			// Checks for oversize labels and offset the result
			if (useBoundingBox)
			{
				mxCellState state = graph.getView().getState(vertex);

				if (state != null
						&& state.getBoundingBox().getX() < state.getX())
				{
					double scale = graph.getView().getScale();
					mxRectangle box = state.getBoundingBox();
					x += (state.getX() - box.getX()) / scale;
					result.setWidth(box.getWidth());
				}
			}

			if (geometry.getX() != x || geometry.getY() != y)
			{
				geometry = (mxGeometry) geometry.clone();
				geometry.setX(x);
				geometry.setY(y);

				model.setGeometry(vertex, geometry);
			}
		}

		return result;
	}

}
