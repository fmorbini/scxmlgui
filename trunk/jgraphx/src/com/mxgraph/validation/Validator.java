package com.mxgraph.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
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
	StringBuffer warnings=null;

	boolean keepGoing=true;
	
	public Validator(mxGraphComponent gc) {
		graphComponent=gc;
		graph=gc.getGraph();
		model = (mxGraphModel) graph.getModel();
		requests=new LinkedHashSet<mxCell>();
		context=new Hashtable<Object, Object>();
		warnings=new StringBuffer();
		
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
					warnings.delete(0, warnings.length());

					model.fireEvent(new mxEventObject(mxEvent.VALIDATION_PRE_START));

					validateGraph(cell, context, warnings);

					model.fireEvent(new mxEventObject(mxEvent.VALIDATION_DONE,"warnings",warnings.toString()));
				}
			} catch (InterruptedException e) {}
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
	public String validateGraph(Object cell, Hashtable<Object, Object> context,StringBuffer totalWarnings)
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

			String warn = validateGraph(tmp, ctx,totalWarnings);

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

			isValid = isValid && warn == null;
		}

		StringBuffer warning = new StringBuffer();

		// Adds error for invalid children if collapsed (children invisible)
		if (graph.isCellCollapsed(cell) && !isValid)
		{
			warning.append(mxResources.get("containsValidationErrors",
					"Contains Validation Errors")
					+ "\n");
		}

		// Checks edges and cells using the defined multiplicities
		if (model.isEdge(cell))
		{
			String tmp = graph.getEdgeValidationError(cell, model.getTerminal(
					cell, true), model.getTerminal(cell, false));

			if (tmp != null)
			{
				warning.append(tmp);
			}
		}
		else
		{
			String tmp = graph.getCellValidationError(cell);

			if (tmp != null)
			{
				warning.append(tmp);
			}
		}

		// Checks custom validation rules
		String err = graph.validateCell(cell, context);

		if (err != null)
		{
			warning.append(err);
		}

		// Updates the display with the warning icons before any potential
		// alerts are displayed
		if (model.getParent(cell) == null)
		{
			view.validate();
		}
		if ((totalWarnings!=null) && (warning.length() > 0))
			totalWarnings.append(warning);
		return (warning.length() > 0 || !isValid) ? warning.toString() : null;
	}

}
