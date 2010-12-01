package com.mxgraph.validation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.StringUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class Validator extends Thread {
	
	private mxGraph graph;
	private mxGraphComponent graphComponent;
	private mxGraphModel model;

	private LinkedHashSet<mxCell> requests=null;
	Hashtable<Object, Object> context=null;
	HashMap<Object,String> warnings=null;

	boolean keepGoing=true;
	
	public Validator(mxGraphComponent gc) {
		setName("Validator");
		graphComponent=gc;
		graph=gc.getGraph();
		model = (mxGraphModel) graph.getModel();
		requests=new LinkedHashSet<mxCell>();
		context=new Hashtable<Object, Object>();
		warnings=new HashMap<Object, String>();
		
		graph.getModel().addListener(mxEvent.REQUEST_VALIDATION, new mxIEventListener()
		{
			public void invoke(Object sender, mxEventObject evt)
			{
				mxCell rootCell=(mxCell) evt.getProperty("root");
				if (!requests.contains(rootCell)) {
					System.out.println("Graph validation requested starting from cell: "+rootCell);
					requests.add(rootCell);
				}
			}
		});
		start();
	}
	
	public void kill() {keepGoing=false;}
	
	@Override
	public void run() {
		while(keepGoing) {
			try {
				Thread.sleep(1000);
				if (!requests.isEmpty()) {
					mxCell cell = requests.iterator().next();
					requests.remove(cell);
					context.clear();
					warnings.clear();

					model.fireEvent(new mxEventObject(mxEvent.VALIDATION_PRE_START));

					validateGraph(cell, context, warnings);

					model.fireEvent(new mxEventObject(mxEvent.VALIDATION_DONE,"warnings",warnings));
				}
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Validates the graph by validating each descendant of the given cell or
	 * the root of the model. Context is an object that contains the validation
	 * state for the complete validation run. The validation errors are
	 * attached to their cells using <setWarning>. This function returns true
	 * if no validation errors exist in the graph.
	 * 
	 * @param cell Cell to start the validation recursion.
	 * @param context Object that represents the global validation state.
	 */
	public boolean validateGraph(Object cell, Hashtable<Object, Object> context,HashMap<Object,String> totalWarnings)
	{
		mxIGraphModel model = graph.getModel();
		mxGraphView view = graph.getView();
		boolean isValid = true;
		int childCount = model.getChildCount(cell);

		for (int i = 0; i < childCount; i++)
		{
			Object tmp = model.getChildAt(cell, i);
			Hashtable<Object, Object> ctx = context;

			if (graph.isValidRoot(tmp))
			{
				ctx = new Hashtable<Object, Object>();
			}

			boolean isThisCellValid=validateGraph(tmp, ctx,totalWarnings);
			isValid = isValid && isThisCellValid;

			String warn=totalWarnings.get(tmp);
			if (warn != null)
			{
				String html = warn.replaceAll("\n", "<br>");
				int len = html.length();
				graphComponent.setCellWarning(tmp, html.substring(0, Math.max(0, len - 4)));
			}
			else
			{
				graphComponent.setCellWarning(tmp, null);
			}
		}

		String warningsForCell=totalWarnings.get(cell);
		if (warningsForCell==null) warningsForCell="";
		
		// Adds error for invalid children if collapsed (children invisible)
		if (graph.isCellCollapsed(cell) && !isValid)
			warningsForCell+=mxResources.get("containsValidationErrors","Contains Validation Errors")+"\n";

		// Checks edges and cells using the defined multiplicities
		String tmp=null;
		if (model.isEdge(cell)) tmp = graph.getEdgeValidationError(cell, model.getTerminal(cell, true), model.getTerminal(cell, false));
		else tmp = graph.getCellValidationError(cell);
		if (tmp != null) warningsForCell+=tmp+"\n";

		// Checks custom validation rules
		tmp = graph.validateCell(cell, context);
		if (tmp != null) warningsForCell+=tmp+"\n";

		warningsForCell=StringUtils.cleanupSpaces(warningsForCell);
		if (!StringUtils.isEmptyString(warningsForCell)) totalWarnings.put(cell, warningsForCell);
		
		// Updates the display with the warning icons before any potential
		// alerts are displayed
		if (model.getParent(cell) == null) view.validate();
		
		return (warningsForCell.isEmpty() && isValid);
	}

}
