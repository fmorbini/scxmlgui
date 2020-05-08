package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;

import javax.swing.JFileChooser;

import com.mxgraph.examples.config.SCXMLConstraints;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class VMLImportExport implements IImportExport {

	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return false;
	}

	@Override
	public void read(String from, mxGraphComponent graphComponent,JFileChooser fc, SCXMLConstraints restrictedConstraints) throws IOException {
	}

	@Override
	public void write(mxGraphComponent from, String into) throws IOException {
		mxGraph graph=from.getGraph();
		mxUtils.writeFile(mxUtils.getXml(mxCellRenderer.createVmlDocument(graph, null, 1, null, null).getDocumentElement()), into);
	}

	@Override
	public Object buildNodeValue() {
		return null;
	}

	@Override
	public Object buildEdgeValue() {
		return null;
	}

	@Override
	public Object cloneValue(Object value) {
		return null;
	}

	@Override
	public void clearInternalID2NodesAndSCXMLID2Nodes() {
		// TODO Auto-generated method stub
		
	}
}
