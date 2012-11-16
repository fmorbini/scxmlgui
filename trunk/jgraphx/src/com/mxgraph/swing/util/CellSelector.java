package com.mxgraph.swing.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel.mxStyleChange;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class CellSelector {
	protected HashMap<mxCell,mxCellMarker> currentSelectedCells=new HashMap<mxCell, mxCellMarker>();
	protected mxGraphComponent gc;
	private mxGraph graph;
	private mxGraphView view;
	private mxIGraphModel model;
	private boolean withScroll;
	private boolean selectSetAsValid=true;

	public CellSelector(final mxGraphComponent gc,final boolean withScroll) {
		this.gc=gc;
		this.graph=gc.getGraph();
		this.view=graph.getView();
		this.model=graph.getModel();
		this.withScroll=withScroll;
		mxIEventListener updateListener=new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				//System.out.println("Updating marker because of event: "+evt.getName()+" change: "+evt.getProperties());
				
				Object changes=evt.getProperty("changes");
				if (changes!=null && changes instanceof List) {
					for(Object change:((List)changes)) {
						if (change !=null && change instanceof mxStyleChange) {
							Object cell=((mxStyleChange)change).getCell();
							mxCellState state = view.getState(cell, false);
							if (currentSelectedCells.containsKey(cell)) {
								mxCellMarker thisCellSelector = currentSelectedCells.get(cell);
								thisCellSelector.unmark();
								thisCellSelector.process(state, thisCellSelector.getMarkerColor(null, state, selectSetAsValid), selectSetAsValid);
								thisCellSelector.mark();
							}
						}
					}
				}
				for(Entry<mxCell,mxCellMarker> el:currentSelectedCells.entrySet()) {
					el.getValue().unmark();
					el.getValue().mark();
				}
			}
		};
		
		view.addListener(mxEvent.SCALE_AND_TRANSLATE, updateListener);
		view.addListener(mxEvent.SCALE, updateListener);
		view.addListener(mxEvent.TRANSLATE, updateListener);
		model.addListener(mxEvent.CHANGE, updateListener);
	}
	
	public CellSelector(mxGraphComponent gc) {
		this(gc,true);
	}
	
	public void selectCell(mxCell c) {
		if (c!=null) {
			mxCellMarker thisCellSelector=currentSelectedCells.get(c);
			mxCellState state=view.getState(c);
			if (thisCellSelector==null) {			
				thisCellSelector=new mxCellMarker(gc);
				currentSelectedCells.put(c,thisCellSelector);
				thisCellSelector.process(state, thisCellSelector.getMarkerColor(null, state, selectSetAsValid), selectSetAsValid);
				thisCellSelector.mark();
				if (withScroll) gc.scrollCellToVisible(c, true);
			} else {
				thisCellSelector.process(state, thisCellSelector.getMarkerColor(null, state, selectSetAsValid), selectSetAsValid);
				thisCellSelector.mark();
			}
		}
	}
	public void unselectCell(mxCell c) {
		mxCellMarker thisCellSelector=currentSelectedCells.get(c);
		if (thisCellSelector!=null) {
			thisCellSelector.unmark();
			currentSelectedCells.remove(c);
		}
	}
	public void toggleSelection(mxCell c) {
		if ((c!=null) && (!currentSelectedCells.containsKey(c))) selectCell(c);
		else unselectCell(c);
	}
	
	public void unselectAll() {
		for(Entry<mxCell,mxCellMarker> el:currentSelectedCells.entrySet()) {
			el.getValue().unmark();
		}
		currentSelectedCells.clear();
	}
}
