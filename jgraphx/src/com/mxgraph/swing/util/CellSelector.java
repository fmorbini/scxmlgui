package com.mxgraph.swing.util;

import java.util.HashMap;
import java.util.Map.Entry;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class CellSelector {
	protected HashMap<mxCell,mxCellMarker> currentSelectedCells=new HashMap<mxCell, mxCellMarker>();
	protected mxGraphComponent gc;
	private mxGraph graph;
	private mxGraphView view;
	private boolean withScroll;
	
	public CellSelector(mxGraphComponent gc,boolean withScroll) {
		this.gc=gc;
		this.graph=gc.getGraph();
		this.view=graph.getView();
		this.withScroll=withScroll;
	}
	public CellSelector(mxGraphComponent gc) {
		this(gc,true);
	}
	
	public void selectCell(mxCell c) {
		if ((c!=null) && (!currentSelectedCells.containsKey(c))) {			
			mxCellMarker thisCellSelector;
			currentSelectedCells.put(c,thisCellSelector=new mxCellMarker(gc));
			boolean selectSetAsValid=true;
			mxCellState state=view.getState(c);
			thisCellSelector.process(state, thisCellSelector.getMarkerColor(null, state, selectSetAsValid), selectSetAsValid);
			thisCellSelector.mark();
			if (withScroll) gc.scrollCellToVisible(c, true);
		} else unselectAll();
	}
	public void unselectCell(mxCell c) {
		mxCellMarker thisCellSelector=currentSelectedCells.get(c);
		if (thisCellSelector!=null) {
			thisCellSelector.unmark();
			currentSelectedCells.remove(c);
		}
	}
	public void updateSelection(mxCell c) {
		unselectAll();
		selectCell(c);
	}
	
	public void unselectAll() {
		for(Entry<mxCell,mxCellMarker> el:currentSelectedCells.entrySet()) {
			el.getValue().unmark();
		}
		currentSelectedCells.clear();
	}
}
