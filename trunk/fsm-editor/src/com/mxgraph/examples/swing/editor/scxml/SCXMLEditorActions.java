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
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Document;

import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.SCXMLGraphEditor.AskToSaveIfRequired;
import com.mxgraph.examples.swing.editor.EditorRuler;
import com.mxgraph.examples.swing.editor.fileimportexport.IImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.ImportExportPicker;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLDatamodelEditor;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLEdgeEditor;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLNamespaceEditor;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLNodeEditor;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLOutEdgeOrderEditor;
import com.mxgraph.examples.swing.editor.scxml.eleditor.SCXMLOutsourcingEditor;
import com.mxgraph.examples.swing.editor.scxml.listener.SCXMLListener;
import com.mxgraph.examples.swing.editor.scxml.search.SCXMLSearchTool;
import com.mxgraph.io.mxCodec;
import com.mxgraph.layout.mxClusterLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
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
		private mxCell node;
		
		public EditDatamodelAction(mxCell c, Point pt) {
			pos=pt;
			node=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);
			Object nodeValue = node.getValue();
			if ((nodeValue!=null) && (nodeValue instanceof SCXMLNode)) {
				try {
					new SCXMLDatamodelEditor(frame,editor,node,(SCXMLNode) nodeValue,pos);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	public static class EditNamespaceAction extends AbstractAction
	{
		private Point pos;
		private mxCell node;

		public EditNamespaceAction(mxCell c, Point pt) {
			pos=pt;
			node=c;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);
			if (node!=null) {
				Object nodeValue = node.getValue();
				if ((nodeValue!=null) && (nodeValue instanceof SCXMLNode)) {
					new SCXMLNamespaceEditor(frame,editor,node,(SCXMLNode) nodeValue,pos);
				}
			}
		}
	}
	public static class EditEdgeOrderAction extends AbstractAction
	{
		private mxCell source;
		private Point pos;
		
		public EditEdgeOrderAction(mxCell s, Point pt) {
			source=s;
			pos=pt;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);			
			new SCXMLOutEdgeOrderEditor(frame,source,editor,pos);
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
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);
			new SCXMLEdgeEditor(frame,cell,(SCXMLEdge)cell.getValue(),editor,pos);
		}
	}

	public static class AddCornerToEdgeAction extends AbstractAction
	{
		private Point pos,scaledPos;
		private mxCell cell;
		private int index;
	
		public AddCornerToEdgeAction(mxCell c, Point pt, Point unscaledPt,int i) {
			this.cell=c;
			this.pos=pt;
			this.scaledPos=unscaledPt;
			this.index=i;
		}
		public void actionPerformed(ActionEvent e)
		{
			assert(cell.isEdge());
			SCXMLGraphEditor editor = getEditor(e);
			mxGraphComponent gc = editor.getGraphComponent();
			mxGraphView gv = gc.getGraph().getView();
			mxCellState cs = gv.getState(cell);
			//List<mxPoint> pts = cs.getAbsolutePoints();
			mxGeometry cg = cell.getGeometry();
			
			if (cg.isRelative()) {
				mxCellState ps = gv.getState(cell.getParent());
				pos=ps.relativizePointToThisState(scaledPos,gv.getScale(),gv.getTranslate());
			}
			
			List<mxPoint> ptsAlreadyThere = (cg.getPoints()==null)?new ArrayList<mxPoint>():new ArrayList<mxPoint>(cg.getPoints());
			if (index>=ptsAlreadyThere.size())
				ptsAlreadyThere.add(new mxPoint(pos.x, pos.y));
			else
				ptsAlreadyThere.add(index,new mxPoint(pos.x, pos.y));

			mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			mxGeometry geometry = (mxGeometry) cg.clone();
			geometry.setPoints(ptsAlreadyThere);
			model.setGeometry(cell, geometry);
			
			//cg.setPoints(ptsAlreadyThere);
			//mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			//model.execute(new mxGeometryChange(model, cell, cg));
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
			List<mxPoint> ptsAlreadyThere = new ArrayList<mxPoint>(cg.getPoints());
			
			ptsAlreadyThere.remove(index);

			mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			mxGeometry geometry = (mxGeometry) cg.clone();
			geometry.setPoints(ptsAlreadyThere);
			model.setGeometry(cell, geometry);
			
			//mxGraphModel model=(mxGraphModel) gc.getGraph().getModel();
			//model.execute(new mxGeometryChange(model, cell, cg));
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
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);
			new SCXMLNodeEditor(frame,cell,(SCXMLNode)cell.getValue(),editor,pos);
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

	public static class SetNodeAsOutsourced extends AbstractAction
	{
		private Point pos;
		private mxCell cell;
		
		public SetNodeAsOutsourced(mxCell c, Point pt) {
			cell=c;
			pos=pt;
		}
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(editor);
			SCXMLGraph graph = editor.getGraphComponent().getGraph();
			assert(cell.isVertex());
			SCXMLNode n=(SCXMLNode) cell.getValue();
			
			//edit outsourcing
			try {
				new SCXMLOutsourcingEditor(frame,editor,cell,n,pos);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if (n.isOutsourcedNode()) {
				graph.addToOutsourced(cell);
			}
			else graph.removeFromOutsourced(cell);
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
				} else {
					File file = editor.getCurrentFile();
					Long newDate = file.lastModified();
					Long prevDate=editor.getLastModifiedDate();
					if ((prevDate!=null) && (newDate>prevDate) && (JOptionPane.showConfirmDialog(editor, mxResources.get("fileModified")) != JOptionPane.YES_OPTION)) {
						return;
					}
				}

				try
				{
					fileIO.write(fc,editor);
					editor.getUndoManager().resetUnmodifiedState();
					editor.setLastModifiedDate();
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
				Collection<Object> modifiedObjects;
				if (undo)
				{
					modifiedObjects=editor.getUndoManager().undo();
				}
				else
				{
					modifiedObjects=editor.getUndoManager().redo();
				}
				if (modifiedObjects!=null) {
					for (Object o:modifiedObjects) {
						if ((o instanceof mxCell) && ((mxCell)o).isEdge()) {
							editor.getGraphComponent().getGraph().reOrderOutgoingEdges((mxCell) ((mxCell)o).getSource());
						}
					}
				}
				editor.updateUndoRedoActionState();
			}
		}

	}

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
					graph.setDefaultParent(null);

					SCXMLNode value=(SCXMLNode)editor.getCurrentFileIO().buildNodeValue();
					((SCXMLImportExport)editor.getCurrentFileIO()).setRoot(value);
					value.setID(SCXMLNode.ROOTID);
					value.setNAMESPACE("xmlns=\"http://www.w3.org/2005/07/scxml\"");
					value.setCluster(true);
					mxCell p = (mxCell) graph.insertVertex(null, value.getInternalID(), value, 0, 0, gc.getSize().width, gc.getSize().height, value.getStyle());
					p.setValue(value);

					graph.setDefaultParent(p);

					graph.setCellAsDeletable(p, false);
					editor.setModified(false);
					editor.getUndoManager().clear();
					editor.getUndoManager().resetUnmodifiedState();
					editor.updateUndoRedoActionState();
					editor.clearDisplayOutsourcedContentStatus();
				}
				editor.setStatus(SCXMLGraphEditor.EDITING);
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
		mxCell parentToLayout;
		
		public DoLayoutAction(mxGraph g, mxCell p) {
			graph=g;
			layout=new mxClusterLayout(g);
			parentToLayout=p;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			int oldStatus=editor.getStatus();
			editor.setStatus(SCXMLGraphEditor.LAYOUT);
			editor.getUndoManager().setCollectionMode(true);
			layout.execute((parentToLayout==null)?graph.getDefaultParent():parentToLayout);
			editor.getUndoManager().setCollectionMode(false);
			editor.setStatus(oldStatus);
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
		protected File file;

		public OpenAction(File file) {
			this.file=file;
		}
		
		public OpenAction() {
			this.file=null;
		}

		private JFileChooser getFileToOpen(SCXMLGraphEditor editor) {
			String wd=(lastDir!=null)?lastDir:((editor.getCurrentFile()!=null)?editor.getCurrentFile().getParent():System.getProperty("user.dir"));
			JFileChooser fc = new JFileChooser(wd);
			
			ImportExportPicker fileIO=editor.getIOPicker();
			fileIO.addImportFiltersToFileChooser(fc);

			if (file!=null) fc.setSelectedFile(file);
			else {
				int rc = fc.showDialog(null, mxResources.get("openFile"));
				if (rc != JFileChooser.APPROVE_OPTION) fc.setSelectedFile(null);
			}
			return fc;
		}
		
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			if (editor != null) {
				editor.setStatus(SCXMLGraphEditor.POPULATING);
				if (AskToSaveIfRequired.check(editor)) {
					SCXMLGraph graph = editor.getGraphComponent().getGraph();
					if (graph != null) {
						JFileChooser fc = getFileToOpen(editor);

						ImportExportPicker fileIO=editor.getIOPicker();

						if (fc.getSelectedFile()!=null)
						{
							lastDir = fc.getSelectedFile().getParent();
							try
							{
								editor.clearDisplayOutsourcedContentStatus();
								IImportExport fie=fileIO.read(fc,editor);

								// apply layout to each cluster from the leaves up:
								mxClusterLayout clusterLayout=new mxClusterLayout(graph);
								clusterLayout.execute(graph.getDefaultParent());
								
								editor.setModified(false);
								editor.setCurrentFile(fc.getSelectedFile(),fie);
								editor.getUndoManager().clear();
								editor.getUndoManager().resetUnmodifiedState();
								editor.updateUndoRedoActionState();
								editor.menuBar.updateRecentlyOpenedListWithFile(fc.getSelectedFile(), editor);
								editor.getSCXMLSearchTool().buildIndex();
							} catch (Exception ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(editor.getGraphComponent(),
										ex.toString(),
										mxResources.get("error"),
										JOptionPane.ERROR_MESSAGE);
							}

						}
					}
				}
				editor.setStatus(SCXMLGraphEditor.EDITING);
			}
		}
	}
	
	public static class ShowSCXMLFindTool extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			SCXMLSearchTool st=editor.getSCXMLSearchTool();
			st.showTool();
		}
	}
	
	@SuppressWarnings("serial")
	public static class ShowSCXMLListener extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			SCXMLListener scxmlListener=editor.getSCXMLListener();
			scxmlListener.showTool();
		}
	}

	public static class ToggleDisplayOutsourcedContentInNode extends AbstractAction {

		private mxCell node=null;
		
		public ToggleDisplayOutsourcedContentInNode(mxCell n) {
			this.node=n;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SCXMLGraphEditor editor = getEditor(e);
			SCXMLGraph graph = editor.getGraphComponent().getGraph();
			try {
				editor.getUndoManager().setCollectionMode(true);
				if (node.getChildCount()>0) {
					// disable
					editor.displayOutsourcedContentInNode(node,graph,false);
				} else {
					// enable
					editor.displayOutsourcedContentInNode(node,graph,true);
				}
				// apply layout to each cluster from the leaves up:
				mxClusterLayout clusterLayout=new mxClusterLayout(graph);
				clusterLayout.execute(graph.getDefaultParent());
				editor.setDisplayOfOutsourcedContentSelected(false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			editor.getUndoManager().setCollectionMode(false);
		}
	}
	
	public static class ToggleDisplayOutsourcedContent extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			SCXMLGraphEditor editor = getEditor(e);
			SCXMLGraph graph = editor.getGraphComponent().getGraph();
			mxIGraphModel model = graph.getModel();
			try {
				editor.getUndoManager().setCollectionMode(true);
				if (editor.isDisplayOfOutsourcedContentSelected()) {
					//disable
					editor.displayOutsourcedContent(graph, false,true);
				} else {
					// enable
					editor.displayOutsourcedContent(graph, true,true);
				}
				// apply layout to each cluster from the leaves up:
				mxClusterLayout clusterLayout=new mxClusterLayout(graph);
				clusterLayout.execute(graph.getDefaultParent());
				editor.setDisplayOfOutsourcedContentSelected(!editor.isDisplayOfOutsourcedContentSelected());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			editor.getUndoManager().setCollectionMode(false);
		}
	}
	
	public static class ZoomIN extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			editor.getGraphComponent().zoomIn();
		}
	}
	public static class ZoomOUT extends AbstractAction
	{
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e)
		{
			SCXMLGraphEditor editor = getEditor(e);
			editor.getGraphComponent().zoomOut();
		}
	}
}
