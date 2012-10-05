/**
 * $Id: mxGraphTransferHandler.java,v 1.13 2009/11/24 12:00:29 gaudenz Exp $
 * Copyright (c) 2008, Gaudenz Alder
 */
package com.mxgraph.swing.handler;

import java.awt.Color;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * 
 */
public class mxGraphTransferHandler extends TransferHandler
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6443287704811197675L;

	/**
	 * Boolean that specifies if an image of the cells should be created for
	 * each transferable. Default is true.
	 */
	public static boolean DEFAULT_TRANSFER_IMAGE_ENABLED = true;

	/**
	 * Specifies the background color of the transfer image. If no
	 * color is given here then the background color of the enclosing
	 * graph component is used. Default is Color.WHITE.
	 */
	public static Color DEFAULT_BACKGROUNDCOLOR = Color.WHITE;

	/**
	 * Reference to the original cells for removal after a move.
	 */
	protected Object[] originalCells;

	/**
	 * Reference to the last imported cell array.
	 */
	protected Object[] lastImported;

	/**
	 * Counter for the last imported cell array.
	 */
	protected int importCount = 0;

	/**
	 * Specifies if a transfer image should be created for the transferable.
	 * Default is DEFAULT_TRANSFER_IMAGE.
	 */
	protected boolean transferImageEnabled = DEFAULT_TRANSFER_IMAGE_ENABLED;

	/**
	 * Specifies the background color for the transfer image. Default is
	 * DEFAULT_BACKGROUNDCOLOR.
	 */
	protected Color transferImageBackground = DEFAULT_BACKGROUNDCOLOR;

	/**
	 * 
	 */
	protected Point location;

	/**
	 * 
	 */
	protected Point offset;

	/**
	 * 
	 */
	public void setTransferImageEnabled(boolean transferImageEnabled)
	{
		this.transferImageEnabled = transferImageEnabled;
	}

	/**
	 * 
	 */
	public boolean isTransferImageEnabled()
	{
		return this.transferImageEnabled;
	}

	/**
	 * 
	 */
	public void setTransferImageBackground(Color transferImageBackground)
	{
		this.transferImageBackground = transferImageBackground;
	}

	/**
	 * 
	 */
	public Color getTransferImageBackground()
	{
		return this.transferImageBackground;
	}

	/**
	 * Returns true if the DnD operation started from this handler.
	 */
	public boolean isLocalDrag()
	{
		return originalCells != null;
	}

	/**
	 * 
	 */
	public void setLocation(Point value)
	{
		location = value;
	}

	/**
	 * 
	 */
	public void setOffset(Point value)
	{
		offset = value;
	}

	/**
	 * 
	 */
	public boolean canImport(JComponent comp, DataFlavor[] flavors)
	{
		for (int i = 0; i < flavors.length; i++)
		{
			if (flavors[i] != null
					&& flavors[i].equals(mxGraphTransferable.dataFlavor))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	public Transferable createTransferable(JComponent c)
	{
		if (c instanceof mxGraphComponent)
		{
			mxGraphComponent graphComponent = (mxGraphComponent) c;
			mxGraph graph = graphComponent.getGraph();

			if (!graph.isSelectionEmpty())
			{
				originalCells = graphComponent.getExportableCells(graph
						.getSelectionCells());

				if (originalCells.length > 0)
				{
					ImageIcon icon = (transferImageEnabled) ? createTransferableImage(
							graphComponent, originalCells)
							: null;

					return createGraphTransferable(graphComponent,
							originalCells, icon);
				}
			}
		}

		return null;
	}

	/**
	 * 
	 */
	public mxGraphTransferable createGraphTransferable(
			mxGraphComponent graphComponent, Object[] cells, ImageIcon icon)
	{
		mxGraph graph = graphComponent.getGraph();
		mxPoint tr = graph.getView().getTranslate();
		double scale = graph.getView().getScale();

		mxRectangle bounds = graph.getPaintBounds(cells);

		// Removes the scale and translation from the bounds
		bounds.setX(bounds.getX() / scale - tr.getX());
		bounds.setY(bounds.getY() / scale - tr.getY());
		bounds.setWidth(bounds.getWidth() / scale);
		bounds.setHeight(bounds.getHeight() / scale);

		return new mxGraphTransferable(graph.cloneCells(cells), bounds, icon);
	}

	/**
	 * 
	 */
	public ImageIcon createTransferableImage(mxGraphComponent graphComponent,
			Object[] cells)
	{
		ImageIcon icon = null;
		Color bg = (transferImageBackground != null) ? transferImageBackground
				: graphComponent.getBackground();
		Image img = mxCellRenderer.createBufferedImage(graphComponent
				.getGraph(), cells, 1, bg, graphComponent.isAntiAlias(), null,
				graphComponent.getCanvas());

		if (img != null)
		{
			icon = new ImageIcon(img);
		}

		return icon;
	}

	/**
	 * 
	 */
	public void exportDone(JComponent c, Transferable data, int action)
	{
		if (c instanceof mxGraphComponent
				&& data instanceof mxGraphTransferable)
		{
			// Requires that the graph handler resets the location to null if the drag leaves the
			// component. This is the condition to identify a cross-component move.
			boolean isLocalDrop = location != null;

			if (action == TransferHandler.MOVE && !isLocalDrop)
			{
				removeCells((mxGraphComponent) c, originalCells);
			}
		}

		originalCells = null;
		location = null;
		offset = null;
	}

	/**
	 * 
	 */
	protected void removeCells(mxGraphComponent graphComponent, Object[] cells)
	{
		graphComponent.getGraph().removeCells(cells);
	}

	/**
	 * 
	 */
	public int getSourceActions(JComponent c)
	{
		return COPY_OR_MOVE;
	}

	/**
	 * 
	 */
	public boolean importData(JComponent c, Transferable t)
	{
		boolean result = false;

		if (isLocalDrag())
		{
			// Enables visual feedback on the Mac
			result = true;
		}
		else
		{
			try
			{
				if (c instanceof mxGraphComponent)
				{
					mxGraphComponent graphComponent = (mxGraphComponent) c;

					if (graphComponent.isEnabled()
							&& t
									.isDataFlavorSupported(mxGraphTransferable.dataFlavor))
					{
						mxGraphTransferable gt = (mxGraphTransferable) t
								.getTransferData(mxGraphTransferable.dataFlavor);

						if (gt.getCells() != null)
						{
							result = importCells(graphComponent, gt.getCells(),
									gt.getBounds());
						}

					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Returns true if the cells have been imported.
	 */
	protected boolean importCells(mxGraphComponent graphComponent,
			Object[] cells, mxRectangle bounds)
	{
		boolean result = false;

		try
		{
			mxGraph graph = graphComponent.getGraph();
			double scale = graph.getView().getScale();
			double dx = 0, dy = 0;

			Object target = null;

			if (location==null) {
				Point pointer = MouseInfo.getPointerInfo().getLocation();
				Rectangle graphScreenLocation=graphComponent.getScreenArea();
				Point mousePoint = new Point(pointer.x-graphScreenLocation.x, pointer.y-graphScreenLocation.y);
				if (graphScreenLocation.contains(pointer)) {
					setLocation(graphComponent.mouseCoordToGraphMouseCoord(mousePoint));
				}
			}
			
			// Finds the target cell at the given location and checks if the
			// target is not already the parent of the first imported cell
			if (location != null)
			{
				target = graph.getDropTarget(cells, location, graphComponent
						.getCellAt(location.x, location.y));

				if (cells.length > 0
						&& graph.getModel().getParent(cells[0]) == target)
				{
					target = null;
				}
			}

			// Computes the offset for the placement of the imported cells
			if (location != null && bounds != null)
			{
				mxPoint translate = graph.getView().getTranslate();

				dx = location.getX() - (bounds.getX() + translate.getX())
						* scale;
				dy = location.getY() - (bounds.getY() + translate.getY())
						* scale;

				// Keeps the cells aligned to the grid
				dx = graph.snap(dx / scale);
				dy = graph.snap(dy / scale);
			}
			else
			{
				// Counts the number of times the cells have been imported
				// via paste and increments the offset accordingly
				if (lastImported != cells)
				{
					importCount = 1;
				}
				else
				{
					importCount++;
				}

				int gs = graph.getGridSize();

				dx = importCount * gs;
				dy = importCount * gs;
			}

			if (offset != null)
			{
				dx += offset.x;
				dy += offset.y;
			}

			lastImported = cells;
			cells = graphComponent.getImportableCells(cells);

			if (graph.isSplitEnabled() && graph.isSplitTarget(target, cells))
			{
				graph.splitEdge(target, cells, dx, dy);
			}
			else
			{
				cells = graphComponent.importCells(cells, dx, dy, target,
						location);
				graph.setSelectionCells(cells);
			}

			location = null;
			offset = null;
			result = true;

			// Requests the focus after an import
			graphComponent.requestFocus();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

}
