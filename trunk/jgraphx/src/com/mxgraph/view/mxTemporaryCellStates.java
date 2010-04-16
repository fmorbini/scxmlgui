package com.mxgraph.view;

import java.util.Hashtable;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxRectangle;

public class mxTemporaryCellStates
{
	/**
	 * 
	 */
	protected mxGraphView view;

	/**
	 * 
	 */
	protected Hashtable<Object, mxCellState> oldStates;

	/**
	 * 
	 */
	protected mxRectangle oldBounds;

	/**
	 * 
	 */
	protected double oldScale;

	/**
	 * Constructs a new temporary cell states instance.
	 */
	public mxTemporaryCellStates(mxGraphView view)
	{
		this(view, 1, null);
	}

	/**
	 * Constructs a new temporary cell states instance.
	 */
	public mxTemporaryCellStates(mxGraphView view, double scale)
	{
		this(view, scale, null);
	}

	/**
	 * Constructs a new temporary cell states instance.
	 */
	public mxTemporaryCellStates(mxGraphView view, double scale, Object[] cells)
	{
		this.view = view;

		// Stores the previous state
		oldBounds = view.getGraphBounds();
		oldStates = view.getStates();
		oldScale = view.getScale();

		// Creates space for the new states
		view.setStates(new Hashtable<Object, mxCellState>());
		view.setScale(scale);

		if (cells != null)
		{
			// Creates virtual parent state for validation
			mxCellState state = view.createState(new mxCell());

			// Validates the vertices and edges without adding them to
			// the model so that the original cells are not modified
			for (int i = 0; i < cells.length; i++)
			{
				view.validateBounds(state, cells[i]);
			}

			double minX = 0;
			double minY = 0;
			double maxX = 0;
			double maxY = 0;

			for (int i = 0; i < cells.length; i++)
			{
				mxRectangle bounds = view.validatePoints(state, cells[i]);

				// TODO: Fix initial 0 for minX, minY (should be null)
				minX = Math.min(minX, bounds.getX());
				minY = Math.min(minY, bounds.getY());
				maxX = Math.max(maxX, bounds.getX() + bounds.getWidth());
				maxY = Math.max(maxY, bounds.getY() + bounds.getHeight());
			}

			view.setGraphBounds(new mxRectangle(minX, minY, maxX - minX, maxY
					- minY));
		}
	}

	/**
	 * Destroys the cell states and restores the state of the graph view.
	 */
	public void destroy()
	{
		view.setScale(oldScale);
		view.setStates(oldStates);
		view.setGraphBounds(oldBounds);
	}

}
