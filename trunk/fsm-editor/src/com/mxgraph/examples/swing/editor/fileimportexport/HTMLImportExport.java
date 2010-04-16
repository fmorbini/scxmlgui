package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class HTMLImportExport implements IImportExport {

	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return false;
	}

	@Override
	public void read(String from, mxGraphComponent graphComponent) throws IOException {
	}

	@Override
	public void write(mxGraphComponent from, String into) throws IOException {
		mxGraph graph=from.getGraph();
		mxUtils.writeFile(mxUtils.getXml(mxCellRenderer.createHtmlDocument(graph, null, 1, null, null).getDocumentElement()), into);
	}

	@Override
	public Object buildNodeValue() {
		return null;
	}

	@Override
	public Object cloneValue(Object value) {
		return null;
	}

	@Override
	public Object buildEdgeValue() {
		return null;
	}

}
