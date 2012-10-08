package com.mxgraph.examples.swing.editor.fileimportexport;

import javax.swing.JFileChooser;

import com.mxgraph.examples.config.SCXMLConstraints;
import com.mxgraph.swing.mxGraphComponent;

public interface IImportExport {
	public abstract Boolean canImport();
	public abstract Boolean canExport();
	public abstract void read(String from,mxGraphComponent graphComponent, JFileChooser fc, SCXMLConstraints restrictedConstraints) throws Exception;
	public abstract void write(mxGraphComponent graphComponent,String into) throws Exception;
	public abstract Object buildNodeValue();
	public abstract Object buildEdgeValue();
	public abstract Object cloneValue(Object value);
	public void clearInternalID2NodesAndSCXMLID2Nodes();
}
