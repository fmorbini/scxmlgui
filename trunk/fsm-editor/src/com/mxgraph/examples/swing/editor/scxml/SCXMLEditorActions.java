/*
 * $Id: EditorActions.java,v 1.6 2009/12/08 19:52:50 gaudenz Exp $
 * Copyright (c) 2001-2009, JGraph Ltd
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Document;

import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.examples.swing.SCXMLEditor;
import com.mxgraph.examples.swing.editor.EditorRuler;
import com.mxgraph.examples.swing.editor.fileimportexport.IImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.ImportExportPicker;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphEditor.AskToSaveIfRequired;
import com.mxgraph.io.mxCodec;
import com.mxgraph.layout.mxClusterLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

/**
 * @author Administrator
 * 
 */
public class SCXMLEditorActions
{

	/**
	 * 
	 * @param e
	 * @return Returns the graph for the given action event.
	 */
	public static final SCXMLGraphEditor getEditor(ActionEvent e)
	{
		if (e.getSource() instanceof Component)
		{
			Component component = (Component) e.getSource();

			while (component != null
					&& !(component instanceof SCXMLGraphEditor))
			{
				component = component.getParent();
			}

			return (SCXMLGraphEditor) component;
		}

		return null;
	}

	public static class AddAction extends AbstractAction
	{
		private Point pos;
		mxCell parent;
		
		// p must be a swimlane
		public AddAction(Point pt, mxCell p) {
			pos=pt;
			parent=p;
		}

		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			mxGraph graph = editor.getGraphComponent().getGraph();
			SCXMLGraphComponent c = (SCXMLGraphComponent) editor.getGraphComponent();
			SCXMLNode value=(SCXMLNode)editor.getCurrentFileIO().buildNodeValue();
			pos=c.mouseCoordToGraphCoord(pos);
			// the state contains the absolute coordinate
			mxGraphView view = graph.getView();
			double scale = view.getScale();
			mxCellState parentState = view.getState(parent);
			double parentX=parentState.getX()/scale;
			double parentY=parentState.getY()/scale;
			mxCell p = (mxCell) graph.insertVertex(parent, value.getInternalID(), value, pos.x-parentX, pos.y-parentY, 100, 100, value.getStyle());
		}
	}
	public static class EditDatamodelAction extends AbstractAction
	{
		private Point pos;
		
		public EditDatamodelAction(Point pt) {
			pos=pt;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			IImportExport fio = editor.getCurrentFileIO();
			if (fio instanceof SCXMLImportExport) {
				SCXMLNode root = ((SCXMLImportExport)fio).getRoot();
				com.mxgraph.examples.swing.editor.scxml.SCXMLDatamodelEditor.createAndShowSCXMLDatamodelEditor(root,pos);
			}
		}
	}

	public static class EditEdgeAction extends AbstractAction
	{
		private Point pos;
		private mxCell cell;
		
		public EditEdgeAction(mxCell c, Point pt) {
			cell=c;
			pos=pt;
		}
		public void actionPerformed(ActionEvent e)
		{
			assert(cell.isEdge());
			SCXMLGraphEditor editor = getEditor(e);
			com.mxgraph.examples.swing.editor.scxml.SCXMLEdgeEditor.createAndShowSCXMLEdgeEditor(editor,(SCXMLEdge)cell.getValue(),pos);
		}
	}

	public static class AddCornerToEdgeAction extends AbstractAction
	{
		private Point pos;
		private mxCell cell;
		private int index;
	
		public AddCornerToEdgeAction(mxCell c, Point pt, int i) {
			cell=c;
			pos=pt;
			index=i;
		}
		public void actionPerformed(ActionEvent e)
		{
			assert(cell.isEdge());
			SCXMLGraphEditor editor = getEditor(e);
			mxGraphComponent gc = editor.getGraphComponent();
			mxCellState cs = gc.getGraph().getView().getState(cell);
			//List<mxPoint> pts = cs.getAbsolutePoints();
			mxGeometry cg = cell.getGeometry();
			List<mxPoint> ptsAlreadyThere = cg.getPoints();
			if (ptsAlreadyThere==null) ptsAlreadyThere=new ArrayList<mxPoint>();
			if (index>=ptsAlreadyThere.size())
				ptsAlreadyThere.add(new mxPoint(pos.x, pos.y));
			else
				ptsAlreadyThere.add(index,new mxPoint(pos.x, pos.y));
			cg.setPoints(ptsAlreadyThere);
			mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			model.execute(new mxGeometryChange(model, cell, cg));
		}
	}
	public static class RemoveCornerToEdgeAction extends AbstractAction
	{
		private int index;
		private mxCell cell;
	
		public RemoveCornerToEdgeAction(mxCell c, int i) {
			cell=c;
			index=i;
		}
		public void actionPerformed(ActionEvent e)
		{
			assert(cell.isEdge());
			SCXMLGraphEditor editor = getEditor(e);
			mxGraphComponent gc = editor.getGraphComponent();
			mxCellState cs = gc.getGraph().getView().getState(cell);
			//List<mxPoint> pts = cs.getAbsolutePoints();
			mxGeometry cg = cell.getGeometry();
			List<mxPoint> ptsAlreadyThere = cg.getPoints();
			ptsAlreadyThere.remove(index);
			mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			model.execute(new mxGeometryChange(model, cell, cg));
		}
	}

	public static class EditNodeAction extends AbstractAction
	{
		private Point pos;
		private mxCell cell;
		
		public EditNodeAction(mxCell c, Point pt) {
			cell=c;
			pos=pt;
		}
		public void actionPerformed(ActionEvent e)
		{
			assert(cell.isVertex());
			SCXMLGraphEditor editor = getEditor(e);
			com.mxgraph.examples.swing.editor.scxml.SCXMLNodeEditor.createAndShowSCXMLNodeEditor(editor,(SCXMLNode)cell.getValue(),pos);
		}
	}
	
	public static class SetNodeAsInitial extends AbstractAction
	{
		private mxCell cell;
		
		public SetNodeAsInitial(mxCell c) {
			cell=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			mxGraph graph = editor.getGraphComponent().getGraph();
			assert(cell.isVertex());
			SCXMLNode n=(SCXMLNode) cell.getValue();
			n.setInitial(!n.isInitial());
			graph.setCellStyle(n.getStyle(),cell);
		}
	}

	public static class SetNodeAsFinal extends AbstractAction
	{
		private mxCell cell;
		
		public SetNodeAsFinal(mxCell c) {
			cell=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			mxGraph graph = editor.getGraphComponent().getGraph();
			assert(cell.isVertex());
			SCXMLNode n=(SCXMLNode) cell.getValue();
			n.setFinal(!n.isFinal());
			graph.setCellStyle(n.getStyle(),cell);
		}
	}

	public static class SetNodeAsCluster extends AbstractAction
	{
		private mxCell cell;
		
		public SetNodeAsCluster(mxCell c) {
			cell=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			mxGraph graph = editor.getGraphComponent().getGraph();
			assert(cell.isVertex());
			SCXMLNode n=(SCXMLNode) cell.getValue();
			n.setCluster(!n.isClusterNode());
			graph.setCellStyle(n.getStyle(),cell);
		}
	}

	public static class SetNodeAsParallel extends AbstractAction
	{
		private mxCell cell;
		
		public SetNodeAsParallel(mxCell c) {
			cell=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			mxGraph graph = editor.getGraphComponent().getGraph();
			assert(cell.isVertex());
			SCXMLNode n=(SCXMLNode) cell.getValue();
			n.setParallel(!n.isParallel());
			graph.setCellStyle(n.getStyle(),cell);
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleRulersItem extends JCheckBoxMenuItem
	{
		/**
		 * 
		 */
		public ToggleRulersItem(final SCXMLGraphEditor editor, String name)
		{
			super(name);
			setSelected(editor.getGraphComponent().getColumnHeader() != null);

			addActionListener(new ActionListener()
			{
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();

					if (graphComponent.getColumnHeader() != null)
					{
						graphComponent.setColumnHeader(null);
						graphComponent.setRowHeader(null);
					}
					else
					{
						graphComponent.setColumnHeaderView(new EditorRuler(
								graphComponent,
								EditorRuler.ORIENTATION_HORIZONTAL));
						graphComponent.setRowHeaderView(new EditorRuler(
								graphComponent,
								EditorRuler.ORIENTATION_VERTICAL));
					}
				}
			});
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleGridItem extends JCheckBoxMenuItem
	{
		/**
		 * 
		 */
		public ToggleGridItem(final SCXMLGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();
					mxGraph graph = graphComponent.getGraph();
					boolean enabled = !graph.isGridEnabled();

					graph.setGridEnabled(enabled);
					graphComponent.setGridVisible(enabled);
					graphComponent.repaint();
					setSelected(enabled);
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleOutlineItem extends JCheckBoxMenuItem
	{
		/**
		 * 
		 */
		public ToggleOutlineItem(final SCXMLGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e)
				{
					final mxGraphOutline outline = editor.getGraphOutline();
					outline.setVisible(!outline.isVisible());
					outline.revalidate();

					SwingUtilities.invokeLater(new Runnable()
					{
						/*
						 * (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						public void run()
						{
							if (outline.getParent() instanceof JSplitPane)
							{
								if (outline.isVisible())
								{
									((JSplitPane) outline.getParent())
											.setDividerLocation(editor
													.getHeight() - 300);
									((JSplitPane) outline.getParent())
											.setDividerSize(6);
								}
								else
								{
									((JSplitPane) outline.getParent())
											.setDividerSize(0);
								}
							}
						}
					});
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ExitAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				editor.exit();
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StylesheetAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String stylesheet;

		/**
		 * 
		 */
		public StylesheetAction(String stylesheet)
		{
			this.stylesheet = stylesheet;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxCodec codec = new mxCodec();
				Document doc = mxUtils.loadDocument(SCXMLEditorActions.class
						.getResource(stylesheet).toString());

				if (doc != null)
				{
					codec.decode(doc.getDocumentElement(), graph
							.getStylesheet());
					graph.refresh();
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ZoomPolicyAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected int zoomPolicy;

		/**
		 * 
		 */
		public ZoomPolicyAction(int zoomPolicy)
		{
			this.zoomPolicy = zoomPolicy;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.setPageVisible(true);
				graphComponent.setZoomPolicy(zoomPolicy);
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class GridStyleAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected int style;

		/**
		 * 
		 */
		public GridStyleAction(int style)
		{
			this.style = style;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.setGridStyle(style);
				graphComponent.repaint();
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class GridColorAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent,
						mxResources.get("gridColor"), graphComponent
								.getGridColor());

				if (newColor != null)
				{
					graphComponent.setGridColor(newColor);
					graphComponent.repaint();
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ScaleAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected double scale;

		/**
		 * 
		 */
		public ScaleAction(double scale)
		{
			this.scale = scale;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				double scale = this.scale;

				if (scale == 0)
				{
					String value = (String) JOptionPane.showInputDialog(
							graphComponent, mxResources.get("value"),
							mxResources.get("scale") + " (%)",
							JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null)
					{
						scale = Double.parseDouble(value.replace("%", "")) / 100;
					}
				}

				if (scale > 0)
				{
					graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageSetupAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();
				PageFormat format = pj.pageDialog(graphComponent
						.getPageFormat());

				if (format != null)
				{
					graphComponent.setPageFormat(format);
					graphComponent.zoomAndCenter();
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PrintAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();

				if (pj.printDialog())
				{
					PageFormat pf = graphComponent.getPageFormat();
					Paper paper = new Paper();
					double margin = 36;
					paper.setImageableArea(margin, margin, paper.getWidth()
							- margin * 2, paper.getHeight() - margin * 2);
					pf.setPaper(paper);
					pj.setPrintable(graphComponent, pf);

					try
					{
						pj.print();
					}
					catch (PrinterException e2)
					{
						System.out.println(e2);
					}
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SaveAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected boolean showDialog=false;

		/**
		 * 
		 */
		protected String lastDir = null;

		/**
		 * 
		 */
		public SaveAction(boolean showDialog)
		{
			this.showDialog = showDialog;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				ImportExportPicker fileIO=editor.getIOPicker();
				JFileChooser fc=null;

				if (showDialog || (editor.getCurrentFile() == null))
				{
					String wd=(lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():System.getProperty("user.dir"));
					fc = new JFileChooser(wd);
					fileIO.addExportFiltersToFileChooser(fc);
					int rc = fc.showDialog(null, mxResources.get("save"));

					if (rc != JFileChooser.APPROVE_OPTION)
					{
						return;
					}
					else
					{
						lastDir = fc.getSelectedFile().getParent();
					}
				}

				try
				{
					fileIO.write(fc,editor);
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(editor.getGraphComponent(),
							ex.toString(),
							mxResources.get("error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SelectShortestPathAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected boolean directed;

		/**
		 * 
		 */
		public SelectShortestPathAction(boolean directed)
		{
			this.directed = directed;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object source = null;
				Object target = null;

				Object[] cells = graph.getSelectionCells();

				for (int i = 0; i < cells.length; i++)
				{
					if (model.isVertex(cells[i]))
					{
						if (source == null)
						{
							source = cells[i];
						}
						else if (target == null)
						{
							target = cells[i];
						}
					}

					if (source != null && target != null)
					{
						break;
					}
				}

				if (source != null && target != null)
				{
					int steps = graph.getChildEdges(graph.getDefaultParent()).length;
					Object[] path = mxGraphAnalysis.getInstance()
							.getShortestPath(graph, source, target,
									new mxDistanceCostFunction(), steps,
									directed);
					graph.setSelectionCells(path);
				}
				else
				{
					JOptionPane.showMessageDialog(graphComponent, mxResources
							.get("noSourceAndTargetSelected"));
				}
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SelectSpanningTreeAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected boolean directed;

		/**
		 * 
		 */
		public SelectSpanningTreeAction(boolean directed)
		{
			this.directed = directed;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object parent = graph.getDefaultParent();
				Object[] cells = graph.getSelectionCells();

				for (int i = 0; i < cells.length; i++)
				{
					if (model.getChildCount(cells[i]) > 0)
					{
						parent = cells[i];
						break;
					}
				}

				Object[] v = graph.getChildVertices(parent);
				Object[] mst = mxGraphAnalysis.getInstance()
						.getMinimumSpanningTree(graph, v,
								new mxDistanceCostFunction(), directed);
				graph.setSelectionCells(mst);
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleDirtyAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.showDirtyRectangle = !graphComponent.showDirtyRectangle;
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleImagePreviewAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.getGraphHandler().setImagePreview(
						!graphComponent.getGraphHandler().isImagePreview());
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleConnectModeAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxConnectionHandler handler = graphComponent
						.getConnectionHandler();
				handler.setHandleEnabled(!handler.isHandleEnabled());
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleCreateTargetItem extends JCheckBoxMenuItem
	{
		/**
		 * 
		 */
		public ToggleCreateTargetItem(final SCXMLGraphEditor editor, String name)
		{
			super(name);
			setSelected(true);

			addActionListener(new ActionListener()
			{
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e)
				{
					mxGraphComponent graphComponent = editor
							.getGraphComponent();

					if (graphComponent != null)
					{
						mxConnectionHandler handler = graphComponent
								.getConnectionHandler();
						handler.setCreateTarget(!handler.isCreateTarget());
						setSelected(handler.isCreateTarget());
					}
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptPropertyAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected Object target;

		/**
		 * 
		 */
		protected String fieldname, message;

		/**
		 * 
		 */
		public PromptPropertyAction(Object target, String message)
		{
			this(target, message, message);
		}

		/**
		 * 
		 */
		public PromptPropertyAction(Object target, String message,
				String fieldname)
		{
			this.target = target;
			this.message = message;
			this.fieldname = fieldname;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof Component)
			{
				try
				{
					Method getter = target.getClass().getMethod(
							"get" + fieldname);
					Object current = getter.invoke(target);

					// TODO: Support other atomic types
					if (current instanceof Integer)
					{
						Method setter = target.getClass().getMethod(
								"set" + fieldname, new Class[] { int.class });

						String value = (String) JOptionPane.showInputDialog(
								(Component) e.getSource(), "Value", message,
								JOptionPane.PLAIN_MESSAGE, null, null, current);

						if (value != null)
						{
							setter.invoke(target, Integer.parseInt(value));
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

			// Repaints the graph component
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				graphComponent.repaint();
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class TogglePropertyItem extends JCheckBoxMenuItem
	{

		/**
		 * 
		 */
		public TogglePropertyItem(Object target, String name, String fieldname)
		{
			this(target, name, fieldname, false);
		}

		/**
		 * 
		 */
		public TogglePropertyItem(Object target, String name, String fieldname,
				boolean refresh)
		{
			this(target, name, fieldname, refresh, null);
		}

		/**
		 * 
		 */
		public TogglePropertyItem(final Object target, String name,
				final String fieldname, final boolean refresh,
				ActionListener listener)
		{
			super(name);

			// Since action listeners are processed last to first we add the given
			// listener here which means it will be processed after the one below
			if (listener != null)
			{
				addActionListener(listener);
			}

			addActionListener(new ActionListener()
			{
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e)
				{
					execute(target, fieldname, refresh);
				}
			});

			PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
			{

				/*
				 * (non-Javadoc)
				 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
				 */
				public void propertyChange(PropertyChangeEvent evt)
				{
					if (evt.getPropertyName().equalsIgnoreCase(fieldname))
					{
						update(target, fieldname);
					}
				}

			};

			if (target instanceof mxGraphComponent)
			{
				((mxGraphComponent) target)
						.addPropertyChangeListener(propertyChangeListener);
			}
			else if (target instanceof mxGraph)
			{
				((mxGraph) target)
						.addPropertyChangeListener(propertyChangeListener);
			}

			update(target, fieldname);
		}

		/**
		 * 
		 */
		public void update(Object target, String fieldname)
		{
			try
			{
				Method getter = target.getClass().getMethod("is" + fieldname);
				Object current = getter.invoke(target);

				if (current instanceof Boolean)
				{
					setSelected(((Boolean) current).booleanValue());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		/**
		 * 
		 */
		public void execute(Object target, String fieldname, boolean refresh)
		{
			try
			{
				Method getter = target.getClass().getMethod("is" + fieldname);
				Method setter = target.getClass().getMethod("set" + fieldname,
						new Class[] { boolean.class });

				Object current = getter.invoke(target);

				if (current instanceof Boolean)
				{
					boolean value = !((Boolean) current).booleanValue();
					setter.invoke(target, value);
					setSelected(value);
				}

				if (refresh)
				{
					mxGraph graph = null;

					if (target instanceof mxGraph)
					{
						graph = (mxGraph) target;
					}
					else if (target instanceof mxGraphComponent)
					{
						graph = ((mxGraphComponent) target).getGraph();
					}

					graph.refresh();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class HistoryAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected boolean undo;
		
		/**
		 * 
		 */
		public HistoryAction(boolean undo)
		{
			this.undo = undo;
			//setEnabled(shouldBeEnabled(undo));
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (undo)
				{
					editor.getUndoManager().undo();
				}
				else
				{
					editor.getUndoManager().redo();
				}
				editor.updateUndoRedoActionState();
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class FontStyleAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected boolean bold;

		/**
		 * 
		 */
		public FontStyleAction(boolean bold)
		{
			this.bold = bold;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Component editorComponent = null;

				if (graphComponent.getCellEditor() instanceof mxCellEditor)
				{
					editorComponent = ((mxCellEditor) graphComponent
							.getCellEditor()).getEditor();
				}

				if (editorComponent instanceof JEditorPane)
				{
					JEditorPane editorPane = (JEditorPane) editorComponent;
					int start = editorPane.getSelectionStart();
					int ende = editorPane.getSelectionEnd();
					String text = editorPane.getSelectedText();

					if (text == null)
					{
						text = "";
					}

					try
					{
						HTMLEditorKit editorKit = new HTMLEditorKit();
						HTMLDocument document = (HTMLDocument) editorPane
								.getDocument();
						document.remove(start, (ende - start));
						editorKit.insertHTML(document, start, ((bold) ? "<b>"
								: "<i>")
								+ text + ((bold) ? "</b>" : "</i>"), 0, 0,
								(bold) ? HTML.Tag.B : HTML.Tag.I);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

					editorPane.requestFocus();
					editorPane.select(start, ende);
				}
				else
				{
					mxIGraphModel model = graphComponent.getGraph().getModel();
					model.beginUpdate();
					try
					{
						graphComponent.stopEditing(false);
						graphComponent.getGraph().toggleCellStyleFlags(
								mxConstants.STYLE_FONTSTYLE,
								(bold) ? mxConstants.FONT_BOLD
										: mxConstants.FONT_ITALIC);
					}
					finally
					{
						model.endUpdate();
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class WarningAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Object[] cells = graphComponent.getGraph().getSelectionCells();

				if (cells != null && cells.length > 0)
				{
					String warning = JOptionPane.showInputDialog(mxResources
							.get("enterWarningMessage"));

					for (int i = 0; i < cells.length; i++)
					{
						graphComponent.setCellWarning(cells[i], warning);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(graphComponent, mxResources
							.get("noCellSelected"));
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class NewAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (AskToSaveIfRequired.check(editor)) {
					mxGraph graph = editor.getGraphComponent().getGraph();
					// Check modified flag and display save dialog
					mxCell root = new mxCell();
					root.insert(new mxCell());
					graph.getModel().setRoot(root);

					editor.setModified(false);
					editor.setCurrentFile(null,null);
				}
			}
		}
	}
	@SuppressWarnings("serial")
	public static class NewSCXMLAction extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				// Check modified flag and display save dialog
				if (AskToSaveIfRequired.check(editor)) {
					editor.setCurrentFile(null,new SCXMLImportExport());
					
					mxGraphComponent gc = editor.getGraphComponent();
					SCXMLGraph graph = (SCXMLGraph) gc.getGraph();

					mxCell root = new mxCell();
					root.insert(new mxCell());
					graph.getModel().setRoot(root);
					
					SCXMLNode value=(SCXMLNode)editor.getCurrentFileIO().buildNodeValue();
					((SCXMLImportExport)editor.getCurrentFileIO()).setRoot(value);
					value.setID("SCXML");
					value.setCluster(true);
					mxCell p = (mxCell) graph.insertVertex(null, value.getInternalID(), value, 0, 0, gc.getSize().width, gc.getSize().height, value.getStyle());
					p.setValue(value);
					graph.setCellAsMovable(p, false);
					editor.setModified(false);
					editor.undoManager.clear();
					editor.updateUndoRedoActionState();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class DoLayoutAction extends AbstractAction
	{
		mxGraph graph;
		mxClusterLayout layout;
		public DoLayoutAction(mxGraph g) {
			graph=g;
			layout=new mxClusterLayout(g);
		}
		public DoLayoutAction(mxGraph g,mxIGraphLayout[] ls) {
			graph=g;
			layout=(ls==null)?new mxClusterLayout(g):new mxClusterLayout(ls, g);
		}
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			layout.execute(graph.getDefaultParent());
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class OpenAction extends AbstractAction
	{
		/**
		 * 
		 */
		protected String lastDir;

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);

			if (editor != null)
			{
				if (AskToSaveIfRequired.check(editor)) {
					mxGraph graph = mxGraphActions.getGraph(e);

					if (graph != null)
					{
						String wd=(lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():System.getProperty("user.dir"));
						JFileChooser fc = new JFileChooser(wd);

						ImportExportPicker fileIO=editor.getIOPicker();
						fileIO.addImportFiltersToFileChooser(fc);

						int rc = fc.showDialog(null, mxResources.get("openFile"));

						if (rc == JFileChooser.APPROVE_OPTION)
						{
							lastDir = fc.getSelectedFile().getParent();
							try
							{
								IImportExport fie=fileIO.read(fc,editor);
								editor.setModified(false);
								editor.setCurrentFile(fc.getSelectedFile(),fie);
								editor.undoManager.clear();
								editor.updateUndoRedoActionState();
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}

						}
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String key;

		/**
		 * 
		 */
		protected boolean defaultValue;

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key)
		{
			this(key, false);
		}

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key, boolean defaultValue)
		{
			this.key = key;
			this.defaultValue = defaultValue;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null)
			{
				graph.toggleCellStyles(key, defaultValue);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetLabelPositionAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String labelPosition, alignment;

		/**
		 * 
		 * @param key
		 */
		public SetLabelPositionAction(String labelPosition, String alignment)
		{
			this.labelPosition = labelPosition;
			this.alignment = alignment;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.getModel().beginUpdate();
				try
				{
					// Checks the orientation of the alignment to use the correct constants
					if (labelPosition.equals(mxConstants.ALIGN_LEFT)
							|| labelPosition.equals(mxConstants.ALIGN_CENTER)
							|| labelPosition.equals(mxConstants.ALIGN_RIGHT))
					{
						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION,
								labelPosition);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, alignment);
					}
					else
					{
						graph.setCellStyles(
								mxConstants.STYLE_VERTICAL_LABEL_POSITION,
								labelPosition);
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN,
								alignment);
					}
				}
				finally
				{
					graph.getModel().endUpdate();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetStyleAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String value;

		/**
		 * 
		 * @param key
		 */
		public SetStyleAction(String value)
		{
			this.value = value;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.setCellStyle(value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class KeyValueAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String key, value;

		/**
		 * 
		 * @param key
		 */
		public KeyValueAction(String key)
		{
			this(key, null);
		}

		/**
		 * 
		 * @param key
		 */
		public KeyValueAction(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.setCellStyles(key, value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptValueAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String key, message;

		/**
		 * 
		 * @param key
		 */
		public PromptValueAction(String key, String message)
		{
			this.key = key;
			this.message = message;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof Component)
			{
				mxGraph graph = mxGraphActions.getGraph(e);

				if (graph != null && !graph.isSelectionEmpty())
				{
					String value = (String) JOptionPane.showInputDialog(
							(Component) e.getSource(),
							mxResources.get("value"), message,
							JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null)
					{
						if (value.equals(mxConstants.NONE))
						{
							value = null;
						}

						graph.setCellStyles(key, value);
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AlignCellsAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String align;

		/**
		 * 
		 * @param key
		 */
		public AlignCellsAction(String align)
		{
			this.align = align;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.alignCells(align);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AutosizeAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty())
			{
				graph.updateCellSize(graph.getSelectionCell());
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ColorAction extends AbstractAction
	{

		/**
		 * 
		 */
		protected String name, key;

		/**
		 * 
		 * @param key
		 */
		public ColorAction(String name, String key)
		{
			this.name = name;
			this.key = key;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty())
				{
					Color newColor = JColorChooser.showDialog(graphComponent,
							name, null);

					if (newColor != null)
					{
						graph.setCellStyles(key, mxUtils.hexString(newColor));
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundImageAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				String value = (String) JOptionPane.showInputDialog(
						graphComponent, mxResources.get("backgroundImage"),
						"URL", JOptionPane.PLAIN_MESSAGE, null, null,
						"http://www.callatecs.com/images/background2.JPG");

				if (value != null)
				{
					if (value.length() == 0)
					{
						graphComponent.setBackgroundImage(null);
					}
					else
					{
						graphComponent.setBackgroundImage(new ImageIcon(mxUtils
								.loadImage(value)));
					}

					// Forces a repaint of the outline
					graphComponent.getGraph().repaint();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent,
						mxResources.get("background"), null);

				if (newColor != null)
				{
					graphComponent.getViewport().setOpaque(false);
					graphComponent.setBackground(newColor);
				}

				// Forces a repaint of the outline
				graphComponent.getGraph().repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageBackgroundAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent,
						mxResources.get("pageBackground"), null);

				if (newColor != null)
				{
					graphComponent.setPageBackgroundColor(newColor);
				}

				// Forces a repaint of the component
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StyleAction extends AbstractAction
	{

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e
						.getSource();
				mxGraph graph = graphComponent.getGraph();
				String initial = graph.getModel().getStyle(
						graph.getSelectionCell());
				String value = (String) JOptionPane.showInputDialog(
						graphComponent, mxResources.get("style"), mxResources
								.get("style"), JOptionPane.PLAIN_MESSAGE, null,
						null, initial);

				if (value != null)
				{
					graph.setCellStyle(value);
				}
			}
		}
	}

}
