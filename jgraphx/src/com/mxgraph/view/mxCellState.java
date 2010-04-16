/**
 * $Id: mxCellState.java,v 1.31 2010/01/13 10:43:46 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package com.mxgraph.view;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;

/**
 * Represents the current state of a cell in a given graph view.
 */
public class mxCellState extends mxRectangle
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7588335615324083354L;

	/**
	 * Reference to the enclosing graph view.
	 */
	protected mxGraphView view;

	/**
	 * Reference to the cell that is represented by this state.
	 */
	protected Object cell;

	/**
	 * Contains an array of key, value pairs that represent the style of the
	 * cell.
	 */
	protected Map<String, Object> style;

	/**
	 * Holds the origin for all child cells.
	 */
	protected mxPoint origin = new mxPoint();

	/**
	 * List of mxPoints that represent the absolute points of an edge.
	 */
	protected List<mxPoint> absolutePoints;

	/**
	 * Holds the absolute offset. For edges, this is the absolute coordinates
	 * of the label position. For vertices, this is the offset of the label
	 * relative to the top, left corner of the vertex.
	 */
	protected mxPoint absoluteOffset = new mxPoint();

	/**
	 * Caches the distance between the end points and the length of an edge.
	 */
	protected double terminalDistance, length;

	/**
	 * Array of numbers that represent the cached length of each segment of the
	 * edge.
	 */
	protected double[] segments;

	/**
	 * Holds the rectangle which contains the label.
	 */
	protected mxRectangle labelBounds;

	/**
	 * Holds the largest rectangle which contains all rendering for this cell.
	 */
	protected mxRectangle boundingBox;

	/**
	 * Specifies if the state is invalid. Default is true.
	 */
	protected boolean invalid = true;

	/**
	 * Constructs an empty cell state.
	 */
	public mxCellState()
	{
		this(null, null, null);
	}

	/**
	 * Constructs a new object that represents the current state of the given
	 * cell in the specified view.
	 * 
	 * @param view Graph view that contains the state.
	 * @param cell Cell that this state represents.
	 * @param style Array of key, value pairs that constitute the style.
	 */
	public mxCellState(mxGraphView view, Object cell, Map<String, Object> style)
	{
		setView(view);
		setCell(cell);
		setStyle(style);
	}

	/**
	 * Returns true if the state is invalid.
	 */
	public boolean isInvalid()
	{
		return invalid;
	}

	/**
	 * Sets the invalid state.
	 */
	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
	}

	/**
	 * Returns the enclosing graph view.
	 * 
	 * @return the view
	 */
	public mxGraphView getView()
	{
		return view;
	}

	/**
	 * Sets the enclosing graph view. 
	 *
	 * @param view the view to set
	 */
	public void setView(mxGraphView view)
	{
		this.view = view;
	}

	/**
	 * Returns the cell that is represented by this state.
	 * 
	 * @return the cell
	 */
	public Object getCell()
	{
		return cell;
	}

	/**
	 * Sets the cell that this state represents.
	 * 
	 * @param cell the cell to set
	 */
	public void setCell(Object cell)
	{
		this.cell = cell;
	}

	/**
	 * Returns the cell style as a map of key, value pairs.
	 * 
	 * @return the style
	 */
	public Map<String, Object> getStyle()
	{
		return style;
	}

	/**
	 * Sets the cell style as a map of key, value pairs.
	 * 
	 * @param style the style to set
	 */
	public void setStyle(Map<String, Object> style)
	{
		this.style = style;
	}

	/**
	 * Returns the origin for the children.
	 * 
	 * @return the origin
	 */
	public mxPoint getOrigin()
	{
		return origin;
	}

	/**
	 * Sets the origin for the children.
	 * 
	 * @param origin the origin to set
	 */
	public void setOrigin(mxPoint origin)
	{
		this.origin = origin;
	}

	/**
	 * Returns the absolute point at the given index.
	 * 
	 * @return the mxPoint at the given index
	 */
	public mxPoint getAbsolutePoint(int index)
	{
		return absolutePoints.get(index);
	}

	/**
	 * Returns the absolute point at the given index.
	 * 
	 * @return the mxPoint at the given index
	 */
	public mxPoint setAbsolutePoint(int index, mxPoint point)
	{
		return absolutePoints.set(index, point);
	}

	/**
	 * Returns the number of absolute points.
	 * 
	 * @return the absolutePoints
	 */
	public int getAbsolutePointCount()
	{
		return (absolutePoints != null) ? absolutePoints.size() : 0;
	}

	/**
	 * Returns the absolute points.
	 * 
	 * @return the absolutePoints
	 */
	public List<mxPoint> getAbsolutePoints()
	{
		return absolutePoints;
	}

	/**
	 * Returns the absolute points.
	 * 
	 * @param absolutePoints the absolutePoints to set
	 */
	public void setAbsolutePoints(List<mxPoint> absolutePoints)
	{
		this.absolutePoints = absolutePoints;
	}

	/**
	 * Returns the absolute offset.
	 * 
	 * @return the absoluteOffset
	 */
	public mxPoint getAbsoluteOffset()
	{
		return absoluteOffset;
	}

	/**
	 * Returns the absolute offset.
	 * 
	 * @param absoluteOffset the absoluteOffset to set
	 */
	public void setAbsoluteOffset(mxPoint absoluteOffset)
	{
		this.absoluteOffset = absoluteOffset;
	}

	/**
	 * Returns the terminal distance.
	 * 
	 * @return the terminalDistance
	 */
	public double getTerminalDistance()
	{
		return terminalDistance;
	}

	/**
	 * Sets the terminal distance.
	 * 
	 * @param terminalDistance the terminalDistance to set
	 */
	public void setTerminalDistance(double terminalDistance)
	{
		this.terminalDistance = terminalDistance;
	}

	/**
	 * Returns the length.
	 * 
	 * @return the length
	 */
	public double getLength()
	{
		return length;
	}

	/**
	 * Sets the length.
	 * 
	 * @param length the length to set
	 */
	public void setLength(double length)
	{
		this.length = length;
	}

	/**
	 * Returns the length of the segments.
	 * 
	 * @return the segments
	 */
	public double[] getSegments()
	{
		return segments;
	}

	/**
	 * Sets the length of the segments.
	 * 
	 * @param segments the segments to set
	 */
	public void setSegments(double[] segments)
	{
		this.segments = segments;
	}

	/**
	 * Returns the label bounds.
	 * 
	 * @return Returns the label bounds for this state.
	 */
	public mxRectangle getLabelBounds()
	{
		return labelBounds;
	}

	/**
	 * Sets the label bounds.
	 * 
	 * @param labelBounds
	 */
	public void setLabelBounds(mxRectangle labelBounds)
	{
		this.labelBounds = labelBounds;
	}

	/**
	 * Returns the bounding box.
	 * 
	 * @return Returns the bounding box for this state.
	 */
	public mxRectangle getBoundingBox()
	{
		return boundingBox;
	}

	/**
	 * Sets the bounding box.
	 * 
	 * @param boundingBox
	 */
	public void setBoundingBox(mxRectangle boundingBox)
	{
		this.boundingBox = boundingBox;
	}

	/**
	 * Returns the rectangle that should be used as the perimeter of the cell.
	 * This implementation adds the perimeter spacing to the rectangle
	 * defined by this cell state.
	 * 
	 * @return Returns the rectangle that defines the perimeter.
	 */
	public mxRectangle getPerimeterBounds()
	{
		return getPerimeterBounds(0);
	}

	/**
	 * Returns the rectangle that should be used as the perimeter of the cell.
	 * 
	 * @return Returns the rectangle that defines the perimeter.
	 */
	public mxRectangle getPerimeterBounds(double border)
	{
		mxRectangle bounds = new mxRectangle(getRectangle());

		if (border != 0)
		{
			bounds.grow(border);
		}

		return bounds;
	}

	/**
	 * Sets the first or last point in the list of points depending on isSource.
	 * 
	 * @param point Point that represents the terminal point.
	 * @param isSource Boolean that specifies if the first or last point should
	 * be assigned.
	 */
	public void setAbsoluteTerminalPoint(mxPoint point, boolean isSource)
	{
		if (isSource)
		{
			if (absolutePoints == null)
			{
				absolutePoints = new ArrayList<mxPoint>();
			}

			if (absolutePoints.size() == 0)
			{
				absolutePoints.add(point);
			}
			else
			{
				absolutePoints.set(0, point);
			}
		}
		else
		{
			if (absolutePoints == null)
			{
				absolutePoints = new ArrayList<mxPoint>();
				absolutePoints.add(null);
				absolutePoints.add(point);
			}
			else if (absolutePoints.size() == 1)
			{
				absolutePoints.add(point);
			}
			else
			{
				absolutePoints.set(absolutePoints.size() - 1, point);
			}
		}
	}

	/**
	 * Returns a clone of this state where all members are deeply cloned
	 * except the view and cell references, which are copied with no
	 * cloning to the new instance.
	 */
	public Object clone()
	{
		mxCellState clone = new mxCellState(view, cell, style);

		if (absolutePoints != null)
		{
			clone.absolutePoints = new ArrayList<mxPoint>();

			for (int i = 0; i < absolutePoints.size(); i++)
			{
				clone.absolutePoints.add((mxPoint) absolutePoints.get(i)
						.clone());
			}
		}

		if (origin != null)
		{
			clone.origin = (mxPoint) origin.clone();
		}

		if (absoluteOffset != null)
		{
			clone.absoluteOffset = (mxPoint) absoluteOffset.clone();
		}

		if (boundingBox != null)
		{
			clone.boundingBox = (mxRectangle) boundingBox.clone();
		}

		clone.terminalDistance = terminalDistance;
		clone.segments = segments;
		clone.length = length;
		clone.x = x;
		clone.y = y;
		clone.width = width;
		clone.height = height;

		return clone;
	}

	public int getIndexOfEdgePointAt(int x, int y,int tol) {
		Rectangle rect = new Rectangle(x - tol / 2, y - tol / 2, tol, tol);
		List<mxPoint> pts = getAbsolutePoints();

		int i=0;
		for (mxPoint p:pts) {
			//System.out.println("point="+p.getX()+" "+p.getY());
			if (rect.contains(p.getPoint())) return i;
			i++;
		}
		return -1;
	}
	public int getIndexOfNewPoint(int x, int y,int tol) {
		Rectangle rect = new Rectangle(x - tol / 2, y - tol / 2, tol, tol);
		List<mxPoint> pts = getAbsolutePoints();

		int length=pts.size();
		mxPoint start,end;
		start=pts.get(0);
		for(int i=1;i<length;i++) {
			end=pts.get(i);
			if (rect.intersectsLine(start.getX(),start.getY(),end.getX(),end.getY())) return i;
			start=end;
		}
		return -1;
	}


}
