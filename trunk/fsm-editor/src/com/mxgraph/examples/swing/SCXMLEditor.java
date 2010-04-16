package com.mxgraph.examples.swing;

import java.awt.Color;
import java.net.URL;

import javax.swing.Action;
import javax.swing.UIManager;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxMultiplicity;

public class SCXMLEditor extends SCXMLGraphEditor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4601740824088314699L;

	/**
	 * Holds the URL for the icon to be used as a handle for creating new
	 * connections. This is currently unused.
	 */
	public static URL url = null;

	//GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/connector.gif");

	public SCXMLEditor()
	{
		this("FSM Editor", new SCXMLGraphComponent(new SCXMLGraph()));		
	}

	/**
	 * 
	 */
	public SCXMLEditor(String appTitle, SCXMLGraphComponent component)
	{
		super(appTitle, component);
		final SCXMLGraph graph = (SCXMLGraph) graphComponent.getGraph();
		graph.setAutoSizeCells(true);
		graph.setEditor(this);
		graph.setMultigraph(true);
		graph.setAllowDanglingEdges(false);
		graph.setConnectableEdges(false);
		// the following 2 lines are required by the graph validation routines,
		// otherwise a null pointer exception is generated.
		mxMultiplicity[] m={};
		graph.setMultiplicities(m);
	}

	/**
	 * main of the editor application.
	 * creates the FSMEditor that is a CustomGraphComponent (JScrollPane)
	 *  contains an instance of CustomGraph (mxGraph that is mxEventSourcE))
	 * create the interface containing the CustomGraphComponent: FSMEditor (FSMGraphEditor (JPanel))
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		mxConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		SCXMLEditor editor = new SCXMLEditor();
		editor.createFrame(editor).setVisible(true);
		//editor.createFrame(new FSMEditorMenuBar(editor)).setVisible(true);
	}
}
