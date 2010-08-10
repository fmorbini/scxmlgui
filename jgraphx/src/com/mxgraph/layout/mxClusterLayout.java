package com.mxgraph.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class mxClusterLayout extends mxGraphLayout {

	HashSet<String>internalClusterID2DoneLayout; // clsters already layed-out
	mxIGraphLayout clusterLayout; // layout to apply to each cluster
	mxGraph graph;

	public mxClusterLayout(mxIGraphLayout cl,mxGraph g) {
		super(g);
		clusterLayout=cl;
		internalClusterID2DoneLayout=new HashSet<String>();
		graph=g;
	}

	public mxClusterLayout(mxGraph g) {
		super(g);
		graph=g;
		clusterLayout=new mxHierarchicalLayout(g);
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
		System.out.println("Starting cluster layout");
		mxCell root=(mxCell) parent;
		// first run the layout on the clusters
		HashSet<mxCell> clusters = findAllClustersRootedAt(graph, root);
		for (mxCell cluster:clusters) {
			handleLayoutInThisCluster(cluster,clusters);
		}
		// after run the graph layout (for edges and labels)
		mxIGraphLayout l = new mxParallelEdgeLayout(graph);
		l.execute(parent);
		l=new mxEdgeLabelLayout(graph);
		l.execute(parent);
		System.out.println("Done cluster layout");
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
					System.out.println("internal starting layout for cluster: "+c.getValue());
					handleLayoutInThisCluster(c,clusters);
				}
			}
			//System.out.println("doing cluster: "+cluster.getValue());
			internalClusterID2DoneLayout.add(id);
			// time to apply the layout to this cluster:
			// apply layout
			// exit cluster
			// resize the cluster container
			if (!cluster.isCollapsed()) {
				clusterLayout.execute(cluster);
				Object[] a=new Object[1];
				a[0]=cluster;
				graph.updateGroupBounds(a,2 * graph.getGridSize(),false);
			}
		}
	}
}
