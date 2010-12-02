package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.TransferHandler;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class SCXMLGraphHandler extends mxGraphHandler {

	private static final long serialVersionUID = 1L;

	public SCXMLGraphHandler(mxGraphComponent graphComponent) {
		super(graphComponent);
	}
	
	@Override
	protected mxCellMarker createMarker()
	{
		mxCellMarker marker = new mxCellMarker(graphComponent, Color.BLUE)
		{
			HashSet<Object> localHashSetOfCells=new HashSet<Object>();
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8451338653189373347L;

			/**
			 * 
			 */
			public boolean isEnabled()
			{
				return graphComponent.getGraph().isDropEnabled();
			}

			@Override
			public void reset() {
				super.reset();
				localHashSetOfCells.clear();
			}
			
			@Override
			protected boolean isValidState(mxCellState state) {
				if (cells!=null) {
					if (localHashSetOfCells.isEmpty() && (cells.length>0)) localHashSetOfCells.addAll(Arrays.asList(cells));
					mxCell targetCell=(mxCell) state.getCell();
					while(targetCell!=null) {
						// avoid cycles. return invalid if the drop target is a child of one of the moved nodes.
						if (localHashSetOfCells.contains(targetCell)) return false;
						else targetCell=(mxCell) targetCell.getParent();
					}
				}
				return true;
			}
			
			/**
			 * 
			 */
			public Object getCell(MouseEvent e)
			{
				TransferHandler th = graphComponent.getTransferHandler();
				boolean isLocal = th instanceof mxGraphTransferHandler
						&& ((mxGraphTransferHandler) th).isLocalDrag();

				mxGraph graph = graphComponent.getGraph();
				Object cell = super.getCell(e);
				Object[] cells = (isLocal) ? graph.getSelectionCells()
						: dragCells;
				cell = graph.getDropTarget(cells, e.getPoint(), cell);
				boolean clone = graphComponent.isCloneEvent(e) && cloneEnabled;

				if (isLocal && cell != null && cells.length > 0 && !clone
						&& graph.getModel().getParent(cells[0]) == cell)
				{
					cell = null;
				}

				return cell;
			}

		};

		// Swimlane content area will not be transparent drop targets
		marker.setSwimlaneContentEnabled(true);

		return marker;
	}
}
