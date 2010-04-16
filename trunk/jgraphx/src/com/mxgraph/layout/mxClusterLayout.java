package com.mxgraph.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class mxClusterLayout extends mxGraphLayout {

	HashSet<String>internalClusterID2DoneLayout; // clsters already layed-out
	mxIGraphLayout[] layouts; // layout to apply to each cluster
	mxGraph graph;

	public mxClusterLayout(mxIGraphLayout[] ls,mxGraph g) {
		super(g);
		layouts=ls;
		internalClusterID2DoneLayout=new HashSet<String>();
		graph=g;
	}

	public mxClusterLayout(mxGraph g) {
		super(g);
		graph=g;
		mxIGraphLayout[] ls={new mxHierarchicalLayout(g),new mxParallelEdgeLayout(g),new mxEdgeLabelLayout(g)};
		layouts=ls;
		internalClusterID2DoneLayout=new HashSet<String>();
	}

	public HashSet<mxCell> findAllClustersRootedAt(mxGraph graph,mxCell parent) {
		return findAllClustersRootedAt(graph,parent,new HashSet<mxCell>());
	}
	public HashSet<mxCell> findAllClustersRootedAt(mxGraph graph,mxCell parent,HashSet<mxCell> result) {
		if (graph.isSwimlane(parent)) result.add(parent);
		int numChildren=parent.getChildCount();
		for(int i=0;i<numChildren;i++) {
			mxCell c=(mxCell) parent.getChildAt(i);
			findAllClustersRootedAt(graph, c, result);
		}
		return result;
	}
	
	@Override
	public void execute(Object parent) {
		mxCell root=(mxCell) parent;
		HashSet<mxCell> clusters = findAllClustersRootedAt(graph, root);
		for (mxCell cluster:clusters) {
			handleLayoutInThisCluster(cluster,clusters);
		}
	}
	
	private void handleLayoutInThisCluster(mxCell cluster, HashSet<mxCell> clusters) {
		String id=cluster.getId();
		//System.out.println("considering cluster: "+cluster.getValue());
		if (!internalClusterID2DoneLayout.contains(id)) {
			int numChildren=cluster.getChildCount();
			for(int i=0;i<numChildren;i++) {
				mxCell c=(mxCell) cluster.getChildAt(i);
				//System.out.println("  "+c.getValue());
				if (clusters.contains(c)) {
					handleLayoutInThisCluster(c,clusters);
				}
			}
			//System.out.println("doing cluster: "+cluster.getValue());
			internalClusterID2DoneLayout.add(id);
			// time to apply the layout to this cluster:
			// apply layout
			// exit cluster
			// resize the cluster container
			for (mxIGraphLayout layout:layouts) layout.execute(cluster);
			Object[] a=new Object[1];
			a[0]=cluster;
			graph.updateGroupBounds(a,2 * graph.getGridSize(),false);
		}
	}
}
