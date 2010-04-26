package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.utils.StringUtils;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class DOTImportExport implements IImportExport {

	@Override
	public Object buildEdgeValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object buildNodeValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return false;
	}

	@Override
	public Object cloneValue(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void read(String from, mxGraphComponent graphComponent) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(mxGraphComponent from, String into)	throws IOException {
		mxGraph graph = from.getGraph();
		mxGraphView view = graph.getView();
		mxIGraphModel model = graph.getModel();
		mxCell root=(mxCell) model.getRoot();
		if ((root!=null) && (root.getChildCount()==1)) root=(mxCell) root.getChildAt(0);
		if (root!=null) {
			String dot=mxVertex2DOTString(view,root,new StringBuffer(),true);
			System.out.println(dot);
			mxUtils.writeFile(dot, into);
		}
	}

	private String mxVertex2DOTString(mxGraphView view, mxCell n, StringBuffer transitions,boolean isRoot) {
		mxGraph graph=view.getGraph();
		String ret="",close="";		
		String label=graph.getLabel(n);
		String id=n.getId();
		
		if (isRoot) {
			ret += "digraph {";
			close="}";
		} else {
			if (n.hasAVertexAsChild()) {
				ret += "\nsubgraph cluster_"+id+" {\nlabel=\""+label+"\";";
				close = "}\n"+close; 
			} else {
				ret += "\n"+id+" [label=\""+label+"\"];";
			}
		}

		int nc=n.getChildCount();
		for(int i=0;i<nc;i++) {
			mxCell c=(mxCell) n.getChildAt(i);
			if (c.isVertex()) {
				ret+=mxVertex2DOTString(view,c,transitions,false);
			} else {
				String source=c.getSource().getId();
				String target=c.getTarget().getId();
				if (c.getSource().hasAVertexAsChild()) source="cluster_"+source;
				if (c.getTarget().hasAVertexAsChild()) target="cluster_"+target;
				String edgeLabel;
				if (graph instanceof SCXMLGraph) {
					edgeLabel=((SCXMLGraph) graph).getLabel(c);
				} else {
					edgeLabel=graph.getLabel(c);
				}
				transitions.append("\n"+source+" -> "+target+" [label=\""+edgeLabel+"\"];");
			}
		}
		if (isRoot) ret+=transitions.toString();
		ret+=close;
		return ret;
	}
}
