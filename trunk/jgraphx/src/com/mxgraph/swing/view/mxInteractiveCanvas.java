package com.mxgraph.swing.view;

import java.awt.Polygon;
import java.awt.Rectangle;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class mxInteractiveCanvas extends mxGraphics2DCanvas
{
	/**
	 * 
	 */
	public boolean contains(mxGraphComponent graphComponent, Rectangle rect,
			mxCellState state)
	{
		return state != null && state.getX() >= rect.x
				&& state.getY() >= rect.y
				&& state.getX() + state.getWidth() <= rect.x + rect.width
				&& state.getY() + state.getHeight() <= rect.y + rect.height;
	}

	/**
	 * 
	 */
	public boolean intersects(mxGraphComponent graphComponent, Rectangle rect,
			mxCellState state)
	{
		if (state != null)
		{
			// Checks if the label intersects
			if (state.getLabelBounds() != null
					&& state.getLabelBounds().getRectangle().intersects(rect))
			{
				return true;
			}

			int pointCount = state.getAbsolutePointCount();

			// Checks if the segments of the edge intersect
			if (pointCount > 0)
			{
				rect = (Rectangle) rect.clone();
				int tolerance = graphComponent.getTolerance();
				rect.grow(tolerance, tolerance);
				mxPoint p0 = state.getAbsolutePoint(0);

				// Handles the special arrow line shape
				if (mxUtils.getString(state.getStyle(),
						mxConstants.STYLE_SHAPE, "").equals(
						mxConstants.SHAPE_ARROW))
				{
					mxPoint pe = state.getAbsolutePoint(state
							.getAbsolutePointCount() - 1);
					Polygon poly = createArrow(p0, pe);

					return poly.intersects(rect);
				}
				else
				{
					for (int i = 0; i < pointCount; i++)
					{
						mxPoint p1 = state.getAbsolutePoint(i);

						if (rect.intersectsLine(p0.getX(), p0.getY(),
								p1.getX(), p1.getY()))
						{
							return true;
						}

						p0 = p1;
					}
				}
			}
			else
			{
				// Checks if the bounds of the shape intersect
				return state.getRectangle().intersects(rect);
			}
		}

		return false;
	}

	/**
	 * Returns true if the given point is inside the content area of the given
	 * swimlane. (The content area of swimlanes is transparent to events.) This
	 * implementation does not check if the given state is a swimlane, it is
	 * assumed that the caller has checked this before using this method.
	 */
	public boolean hitSwimlaneContent(mxGraphComponent graphComponent,
			mxCellState swimlane, int x, int y)
	{
		if (swimlane != null)
		{
			int start = (int) Math.max(2, Math.round(mxUtils.getInt(swimlane.getStyle(),
					mxConstants.STYLE_STARTSIZE, mxConstants.DEFAULT_STARTSIZE)
					* graphComponent.getGraph().getView().getScale()));
			Rectangle rect = swimlane.getRectangle();

			if (mxUtils.isTrue(swimlane.getStyle(),
					mxConstants.STYLE_HORIZONTAL, true))
			{
				rect.y += start;
				rect.height -= start;
			}
			else
			{
				rect.x += start;
				rect.width -= start;
			}

			return rect.contains(x, y);
		}

		return false;
	}

}
