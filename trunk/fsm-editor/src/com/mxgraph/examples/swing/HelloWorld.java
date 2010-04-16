package com.mxgraph.examples.swing;

import javax.swing.JFrame;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class HelloWorld extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	public HelloWorld()
	{
		super("Hello, World!");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try
		{
			Object v1 = graph.insertVertex(parent, null, "1", 10, 10, 10,10);
			Object v2 = graph.insertVertex(parent, null, "2", 20, 20,90, 90);
			Object v3 = graph.insertVertex(parent, null, "3", 30, 30,90, 90);
			Object v4 = graph.insertVertex(parent, null, "4", 40, 40,90, 90);
			Object v5 = graph.insertVertex(parent, null, "5", 50, 50,90, 90);
			Object v6 = graph.insertVertex(parent, null, "6", 60, 60,90, 90);
			graph.insertEdge(parent, null, "Edge", v1, v2);
			graph.insertEdge(parent, null, "Edge", v1, v3);
			graph.insertEdge(parent, null, "Edge", v2, v4);
			graph.insertEdge(parent, null, "Edge", v1, v5);
			graph.insertEdge(parent, null, "Edge", v3, v6);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
		Object cell = graphComponent.getGraph().getSelectionCell();
		if (cell == null
				|| graphComponent.getGraph().getModel()
						.getChildCount(cell) == 0)
		{
			cell = graphComponent.getGraph().getDefaultParent();
		}

		long t0 = System.currentTimeMillis();
		layout.execute(cell);
		System.out.println("Layout: " + (System.currentTimeMillis() - t0)+ " ms");
		getContentPane().repaint();
	}

	public static void main(String[] args)
	{
		HelloWorld frame = new HelloWorld();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}

}
