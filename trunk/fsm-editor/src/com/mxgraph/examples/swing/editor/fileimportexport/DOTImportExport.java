package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFileChooser;

import com.mxgraph.examples.config.SCXMLConstraints;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
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
	public void read(String from, mxGraphComponent graphComponent,JFileChooser fc, SCXMLConstraints restrictedConstraints) throws IOException {
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

	public mxCell getSimpleChildOf(mxCell n) {
		Queue<mxCell> list=new LinkedList<mxCell>();
		list.add(n);
		while(!list.isEmpty()) {
			mxCell x=list.poll();
			int l=x.getChildCount();
			for (int i=0;i<l;i++) {
				mxCell c = (mxCell)x.getChildAt(i);
				if (c.isVertex()) {
					if (c.hasAVertexAsChild()) list.add(c);
					else return c;
				}
			}
		}
		return n;
	}
	
	private String mxVertex2DOTString(mxGraphView view, mxCell n, StringBuffer transitions,boolean isRoot) {
		mxGraph graph=view.getGraph();
		String ret="",close="";		
		String label=graph.getLabel(n);
		String id=n.getId();
		
		if (isRoot) {
			ret += "digraph {\ncompound=true;";
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
				mxCell nSource=(mxCell)c.getSource(),nTarget=(mxCell)c.getTarget();
				boolean sourceIsCluster=nSource.hasAVertexAsChild();
				boolean targetIsCluster=nTarget.hasAVertexAsChild();
				String source=(sourceIsCluster)?"cluster_"+nSource.getId():null;
				String target=(targetIsCluster)?"cluster_"+nTarget.getId():null;
				String realStart=(sourceIsCluster)? realStart=getSimpleChildOf(nSource).getId():nSource.getId();
				String realEnd=(targetIsCluster)? realEnd=getSimpleChildOf(nTarget).getId():nTarget.getId();
				String edgeLabel;
				if (graph instanceof SCXMLGraph) {
					edgeLabel=((SCXMLGraph) graph).getLabel(c);
				} else {
					edgeLabel=graph.getLabel(c);
				}
				String ltail=(source!=null)?"ltail=\""+source+"\"":null;
				String lhead=(target!=null)?"lhead=\""+target+"\"":null;
				transitions.append("\n"+realStart+" -> "+realEnd+" ["+((ltail!=null)?ltail+",":"")+((lhead!=null)?lhead+",":"")+" label=\""+edgeLabel+"\"];");
			}
		}
		if (isRoot) ret+=transitions.toString();
		ret+=close;
		return ret;
	}

	@Override
	public void clearInternalID2NodesAndSCXMLID2Nodes() {
		// TODO Auto-generated method stub
		
	}
}
