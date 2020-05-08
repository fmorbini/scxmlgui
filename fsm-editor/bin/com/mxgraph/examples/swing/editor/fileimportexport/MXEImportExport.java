package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;

import com.mxgraph.examples.config.SCXMLConstraints;
import com.mxgraph.io.mxCodec;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class MXEImportExport implements IImportExport {

	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return true;
	}

	@Override
	public void read(String from, mxGraphComponent graphComponent,JFileChooser fc, SCXMLConstraints restrictedConstraints) throws Exception {
		Document document = mxUtils.parseXMLString(mxUtils.readFile(from),false,false);
		mxCodec codec = new mxCodec(document);
		codec.decode(document.getDocumentElement(),graphComponent.getGraph().getModel());
	}

	@Override
	public void write(mxGraphComponent from, String into) throws IOException {
		mxGraph graph=from.getGraph();
		mxCodec codec = new mxCodec();
		String xml = mxUtils.getXml(codec.encode(graph.getModel()));
		mxUtils.writeFile(xml, into);
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
		return value;
	}

	@Override
	public void clearInternalID2NodesAndSCXMLID2Nodes() {
		// TODO Auto-generated method stub
		
	}
}
