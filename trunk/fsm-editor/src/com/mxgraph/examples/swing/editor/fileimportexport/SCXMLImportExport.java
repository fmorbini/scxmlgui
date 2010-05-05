package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.utils.StringUtils;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.layout.mxClusterLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraphView;

public class SCXMLImportExport implements IImportExport {
	
	private SCXMLNode root=null;

	private HashMap<String,SCXMLNode> internalID2nodes=new HashMap<String, SCXMLNode>();
	private HashMap<String,ArrayList<SCXMLNode>> internalID2clusters=new HashMap<String, ArrayList<SCXMLNode>>();
	private HashMap<String,SCXMLNode> scxmlID2nodes=new HashMap<String, SCXMLNode>();
	private HashMap<String,HashMap<String,ArrayList<SCXMLEdge>>> fromToEdges=new HashMap<String, HashMap<String,ArrayList<SCXMLEdge>>>();
	private int internalIDcounter=11;

	private ArrayList<SCXMLEdge> getEdges(String SCXMLfromID,String SCXMLtoID) {
		assert(!SCXMLfromID.equals(""));
		HashMap<String, ArrayList<SCXMLEdge>> toEdge=fromToEdges.get(SCXMLfromID);
		return (toEdge==null)?null:toEdge.get(SCXMLtoID);
	}
	private int getNumEdgesFrom(String SCXMLfromID) {
		assert(!SCXMLfromID.equals(""));
		HashMap<String, ArrayList<SCXMLEdge>> toEdge=fromToEdges.get(SCXMLfromID);
		if (toEdge==null) return 0;
		else {
			int tot=0;
			for(ArrayList<SCXMLEdge> es:toEdge.values()) {
				tot+=es.size();
			}
			return tot;
		}
	}
	private void addEdge(HashMap<String,String> ec) {
		//System.out.println("add edge: "+ec.get(SCXMLEdge.SOURCE)+"->"+ec.get(SCXMLEdge.TARGET));
		addEdge(ec.get(SCXMLEdge.SOURCE),ec.get(SCXMLEdge.TARGET),ec.get(SCXMLEdge.CONDITION),ec.get(SCXMLEdge.EVENT),ec.get(SCXMLEdge.EDGEEXE));
	}
	private void addEdge(String SCXMLfromID,String SCXMLtoID,String cond,String event,String content) {
		ArrayList<SCXMLEdge> edges = getEdges(SCXMLfromID, SCXMLtoID);
		SCXMLEdge edge = new SCXMLEdge(SCXMLfromID,SCXMLtoID,cond, event, content);
		edge.setInternalID(getNextInternalID());
		if (edges==null) {
			edges=new ArrayList<SCXMLEdge>();			
			edges.add(edge);
			HashMap<String, ArrayList<SCXMLEdge>> toEdges=fromToEdges.get(SCXMLfromID);
			if (toEdges==null) {
				toEdges=new HashMap<String, ArrayList<SCXMLEdge>>();
				fromToEdges.put(SCXMLfromID, toEdges);
			}
			toEdges.put(SCXMLtoID,edges);
		} else {
			edges.add(edge);
		}
		int oe=getNumEdgesFrom(SCXMLfromID);
		edge.setOrder((oe<=0)?0:oe-1);
	}
	private void setNodeAsChildrenOf(SCXMLNode node,SCXMLNode pn) {
		if (pn!=null) {
			// make pn a cluster and add node to its children
			ArrayList<SCXMLNode> cluster=setThisNodeAsCluster(pn);
			addThisNodeAsChildrenOfCluster(node, cluster);
		}
	}
	public ArrayList<SCXMLNode> setThisNodeAsCluster(SCXMLNode node) {
		ArrayList<SCXMLNode> cluster=null;
		if (!isThisNodeACluster(node)) {
			node.setShape(SCXMLNode.CLUSTERSHAPE);
			internalID2clusters.put(node.getInternalID(), cluster=new ArrayList<SCXMLNode>());
		}
		else
			cluster=internalID2clusters.get(node.getInternalID());
		return cluster;
	}
	public void addThisNodeAsChildrenOfCluster(SCXMLNode node,ArrayList<SCXMLNode> cluster) {
		cluster.add(node);
	}
	public Boolean isThisNodeACluster(SCXMLNode node){
		String internalID=node.getInternalID();
		return internalID2clusters.containsKey(internalID);
	}
	public SCXMLNode getClusterNamed(String internalID){
		assert(isThisNodeACluster(internalID2nodes.get(internalID)));
		return internalID2nodes.get(internalID);
	}
	
	public SCXMLNode getNodeFromSCXMLID(String scxmlID) {
		assert(!scxmlID.equals(""));
		return scxmlID2nodes.get(scxmlID);
	}
	
	public String getNextInternalID() {
		return ""+internalIDcounter++;
	}
	
	public void addSCXMLNode(SCXMLNode node) {
		//System.out.println("Adding node: "+node);
		String scxmlID=node.getID();
		String internalID=getNextInternalID();
		node.setInternalID(internalID);
		if (!scxmlID.equals("")) {
			assert(!scxmlID2nodes.containsKey(scxmlID));
			scxmlID2nodes.put(scxmlID, node);
		}
		internalID2nodes.put(internalID, node);
	}

	private SCXMLNode handleSCXMLNode(Node n, SCXMLNode pn, Boolean isParallel) {
		NamedNodeMap att = n.getAttributes();
		Node nodeID = att.getNamedItem("id");
		String nodeIDString=(nodeID==null)?"":StringUtils.cleanupSpaces(nodeID.getNodeValue());
		SCXMLNode node;
		if (nodeIDString.equals("") || ((node=scxmlID2nodes.get(nodeIDString))==null)) {
			node=new SCXMLNode();
			node.setID(nodeIDString);
			addSCXMLNode(node);
		}
		node.setParallel(isParallel);
		setNodeAsChildrenOf(node,pn);
		Node isInitial=null;
		Node isFinal=null;
		if (((isFinal=att.getNamedItem("final"))!=null) &&
			(isFinal.getNodeValue().equals("true"))) {
			node.setFinal(true);
		}
		if (((isInitial=att.getNamedItem("initial"))!=null)||
				((isInitial=att.getNamedItem("initialstate"))!=null)) {
			String[] initialStates=StringUtils.cleanupSpaces(isInitial.getNodeValue()).split("[\\s]");
			for (String initialStateID:initialStates) {
				SCXMLNode in =getNodeFromSCXMLID(initialStateID);
				if (in==null) in=new SCXMLNode();
				in.setID(initialStateID);
				in.setInitial(true);
				addSCXMLNode(in);
			}
		}
		return node;
	}

	public void getNodeHier(Node el, SCXMLNode pn) {
		//addTransitionsToInitialNodes(el,pid);
		NodeList states = el.getChildNodes();
		for (int s = 0; s < states.getLength(); s++) {
			Node n = states.item(s);
			switch (n.getNodeType()) {
			case Node.ELEMENT_NODE:
				String name=n.getNodeName().toLowerCase();
				// STATE: normal or parallel
				Boolean isParallel=false;
				if (name.equals("state")||(isParallel=name.equals("parallel"))) {
					SCXMLNode node = handleSCXMLNode(n,pn,isParallel);
					// continue recursion on the children of this node
					getNodeHier(n, node);
				} else if (name.equals("transition")) {
					addEdge(processEdge(pn,n));
				} else if (name.equals("final")) {
					SCXMLNode node = handleSCXMLNode(n,pn,isParallel);
					node.setFinal(true);
					getNodeHier(n, node);
				} else if (name.equals("initial")) {
					//pn.setInitial(true);
					// only one child that is a transition
					NodeList cs = n.getChildNodes();
					for (int i = 0; i < cs.getLength(); i++) {
						Node c = cs.item(i);
						if ((c.getNodeType()==Node.ELEMENT_NODE) &&
								c.getNodeName().toLowerCase().equals("transition")) {
							HashMap<String, String> edgeContent = processEdge(pn,c);
							assert(!(edgeContent.containsKey(SCXMLEdge.EVENT) || edgeContent.containsKey(SCXMLEdge.CONDITION)) && edgeContent.containsKey(SCXMLEdge.EDGEEXE));
							pn.setOnInitialEntry(edgeContent.get(SCXMLEdge.EDGEEXE));
							String inName=edgeContent.get(SCXMLEdge.TARGET);
							if (inName!=null) {
								SCXMLNode in =getNodeFromSCXMLID(inName);
								if (in==null) in=new SCXMLNode();
								in.setID(inName);
								in.setInitial(true);
								addSCXMLNode(in);
							}
							break;
						}
					}
				} else if (name.equals("onentry")) {
					String content=collectAllChildrenInString(n);
					pn.setOnEntry(content);
				} else if (name.equals("onexit")) {
					String content=collectAllChildrenInString(n);
					pn.setOnExit(content);
				} else if (name.equals("donedata")) {
					String content=collectAllChildrenInString(n);
					pn.setDoneData(content);
				} else if (name.equals("datamodel")) {
					String content=collectAllChildrenInString(n);
					root.addToDataModel(content);
				}
				break;
			case Node.COMMENT_NODE:
				String positionString=n.getNodeValue();
				Pattern p = Pattern.compile("^[\\s]*x=([\\d]+) y=([\\d]+) w=([\\d]+) h=([\\d]+)[\\s]*$");
				Matcher m = p.matcher(positionString);
				int x,y,h,w;
				if (m.matches() && (m.groupCount()==4)) {
					try {
						x=Integer.parseInt(m.group(1));
						y=Integer.parseInt(m.group(2));
						w=Integer.parseInt(m.group(3));
						h=Integer.parseInt(m.group(4));
						pn.setGeometry(x,y,w,h);
					} catch (NumberFormatException e) {
					}
				}
				break;
			}
		}
	}

	private HashMap<String, String> processEdge(SCXMLNode pn, Node n) {
		HashMap<String,String> ret=new HashMap<String, String>();
		//event, cond and target attributes
		NamedNodeMap att = n.getAttributes();
		Node condNode = att.getNamedItem("cond");
		String cond=(condNode!=null)?StringUtils.removeLeadingAndTrailingSpaces(condNode.getNodeValue()):"";
		Node eventNode = att.getNamedItem("event");
		String event=(eventNode!=null)?StringUtils.removeLeadingAndTrailingSpaces(eventNode.getNodeValue()):"";
		Node targetNode = att.getNamedItem("target");					
		String target=(targetNode!=null)?StringUtils.removeLeadingAndTrailingSpaces(targetNode.getNodeValue()):pn.getID();
		String exe=collectAllChildrenInString(n);
		ret.put(SCXMLEdge.CONDITION,cond);
		ret.put(SCXMLEdge.EVENT,event);
		ret.put(SCXMLEdge.TARGET,target);
		ret.put(SCXMLEdge.SOURCE,pn.getID());
		ret.put(SCXMLEdge.EDGEEXE,exe);
		return ret;
	}
	private String collectAllChildrenInString(Node n) {
		String content="";
		NodeList list = n.getChildNodes();
		int listLength = list.getLength();
		for (int i=0;i<listLength;i++) {
			content+=XMLUtils.domNode2String(list.item(i));
		}
		return StringUtils.removeLeadingAndTrailingSpaces(content);
	}
	@Override
	public void read(String from, mxGraphComponent graphComponent) throws IOException {
		SCXMLGraphComponent gc=(SCXMLGraphComponent)graphComponent;
		// clean importer data-structures
		internalID2cell.clear();
		internalID2clusters.clear();
		internalID2nodes.clear();
		fromToEdges.clear();
		scxmlID2nodes.clear();
		internalIDcounter=11;
		
		Document doc = mxUtils.parse(mxUtils.readFile(from));
		doc.getDocumentElement().normalize();
		root=handleSCXMLNode(doc.getDocumentElement(),null,false);
		root.setID("SCXML");
		getNodeHier(doc.getDocumentElement(),root);

		SCXMLGraph graph = (SCXMLGraph) gc.getGraph();
		// empty the graph
		mxCell gr = new mxCell();
		gr.insert(new mxCell());
		graph.getModel().setRoot(gr);

		populateGraph(graph);
		// set the SCXML (this.root) mxCell as not deletable.
		gr=internalID2cell.get(root.getInternalID());
		graph.setCellAsDeletable(gr, false);
	}

	private HashMap<String,mxCell> internalID2cell=new HashMap<String, mxCell>();
	private void populateGraph(SCXMLGraph graph) {
		mxIGraphModel model=graph.getModel();
		model.beginUpdate();
		try{
			// first process the clusters
			for (String internalID: internalID2clusters.keySet()) {
				SCXMLNode cluster = internalID2nodes.get(internalID);
				addOrUpdateNode(graph,cluster,null);
				//mxCell pn = (mxCell) graph.insertVertex(clusterCell, getNextInternalID(), "aaaaaaa", 0, 0, 0, 0, "rounded=1");
				//pn.setVisible(false);
				for (SCXMLNode n:internalID2clusters.get(internalID)) {
					addOrUpdateNode(graph,n,cluster);
					//String id=getNextInternalID();
					//mxCell e=(mxCell) graph.insertEdge(internalID2cell.get(root.getInternalID()), id, "", pn,internalID2cell.get(n.getInternalID()) ,"");
					//e.setVisible(false);
				}
			}			
			// then go through all nodes and make sure all have been created
			for (String internalID:internalID2nodes.keySet()) {
				SCXMLNode n = internalID2nodes.get(internalID);				
				mxCell cn=addOrUpdateNode(graph,n,null);
				//System.out.println(n.getStyle());
                // set geometry and size
				cn.setStyle(n.getStyle());
				mxGeometry g=null;//n.getGeometry();
				if (g!=null) {
					graph.setCellAsMovable(cn, false);
					model.setGeometry(cn, g);
				} else if (!internalID2clusters.containsKey(internalID)) {
					graph.updateCellSize(internalID2cell.get(internalID));
				}
			}
			// then add the edges
			for (String fromSCXMLID:fromToEdges.keySet()) {
				HashMap<String, ArrayList<SCXMLEdge>> toEdge = fromToEdges.get(fromSCXMLID);
				for (String toSCXMLID:toEdge.keySet()) {
					ArrayList<SCXMLEdge> es=toEdge.get(toSCXMLID);
					for (SCXMLEdge e:es) {
						addOrUpdateEdge(graph,e);
					}
				}
			}
		} finally {
			model.endUpdate();
		}
		// apply layout to each cluster from the leaves up:
		mxClusterLayout clusterLayout=new mxClusterLayout(graph);
		clusterLayout.execute(graph.getDefaultParent());
	}
	
	private mxCell addOrUpdateEdge(SCXMLGraph graph, SCXMLEdge edge) {
		mxCell source=internalID2cell.get(scxmlID2nodes.get(edge.getSCXMLSource()).getInternalID());
		mxCell target=internalID2cell.get(scxmlID2nodes.get(edge.getSCXMLTarget()).getInternalID());
		//String label=(String)edge.edge.get(EVENT);
		mxCell e=(mxCell) graph.insertEdge(internalID2cell.get(root.getInternalID()), edge.getInternalID(),edge,source,target);
		internalID2cell.put(edge.getInternalID(),e);
		return e;
	}
	private mxCell addOrUpdateNode(SCXMLGraph graph, SCXMLNode node, SCXMLNode parent) {
		mxCell n=internalID2cell.get(node.getInternalID());
		mxCell p=null;
		if (parent!=null) {
			p=internalID2cell.get(parent.getInternalID());
			if (p==null) {
				p=(mxCell) graph.insertVertex(null, node.getInternalID(), node, 0, 0, 0, 0, "");
				internalID2cell.put(parent.getInternalID(), p);
			}
		}
		if (n==null) {
			n=(mxCell) graph.insertVertex((parent==null)?null:p, node.getInternalID(), node, 0, 0, 0, 0, "");
			internalID2cell.put(node.getInternalID(), n);
		}
		else
			if (parent!=null)  {
				n.removeFromParent();
				graph.addCell(n, p);
			}
		return n;
	}
	
	@Override
	public Boolean canExport() {
		return true;
	}

	@Override
	public Boolean canImport() {
		return true;
	}
	@Override
	public Object buildNodeValue() {
		SCXMLNode n=new SCXMLNode();
		String internalID=getNextInternalID();
		n.setID("new node"+getNextInternalID());
		n.setInternalID(internalID);
		return n;
	}
	@Override
	public Object buildEdgeValue() {
		SCXMLEdge e=new SCXMLEdge();
		String internalID=getNextInternalID();
		e.setInternalID(internalID);
		return e;
	}
	public SCXMLNode getRoot() {
		return root;
	}
	public void setRoot(SCXMLNode r) {
		root=r;
	}
	@Override
	public Object cloneValue(Object value) {
		if (value instanceof SCXMLNode) {
			return ((SCXMLNode) value).cloneNode(this);
		} else if (value instanceof SCXMLEdge) {
			return ((SCXMLEdge) value).cloneEdge(this);
		} else return null;
	}

	private final static String scxml1="<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\"";
	@Override
	public void write(mxGraphComponent from, String into) throws IOException {
		// find the starting point: root. as the last descendant from the root of the model (single line descendant) and the first with a value that is an SCXMLNode.
		// for root: get datamodel and write that
		// for any state/node: check that there is a children marked as initial, see if it has oninitialentry data. if yes add an initial node, otherwise add an initial attribute.
		// for any state/node: get all outgoing edges: add a transition for each of them
		//  for any transition: print event/condition and exe content.
		// for any state/node: print the on-entry/on-exit/donedata
		// for any state/node: add the children states, repeat process recursively
		SCXMLGraph graph=(SCXMLGraph) from.getGraph();
		mxGraphView view = graph.getView();
		mxIGraphModel model = graph.getModel();
		mxCell root=followUniqueDescendantLineTillSCXMLValueIsFound(model);
		if (root!=null) {
			String scxml=mxVertex2SCXMLString(view,root,true);
			System.out.println(scxml);
			scxml=XMLUtils.prettyPrintXMLString(scxml, " ");			
			System.out.println(scxml);
			mxUtils.writeFile(scxml, into);
		}
	}
	
	private String mxVertex2SCXMLString(mxGraphView view, mxCell n, boolean isRoot) {
		String ret="";
		String ID=null;
		String datamodel=null;
		String onentry=null;
		String onexit=null;
		String oninitialentry=null;
		String donedata=null;
		String transitions=null;
		assert(n.isVertex());
		SCXMLNode value=(SCXMLNode) n.getValue();
		assert(value==root);
		ID=value.getID();
		if (isRoot)	datamodel=value.getDataModel();
		if (value.isFinal()) donedata=value.getDoneData();
		onentry=value.getOnEntry();
		onexit=value.getOnExit();
		transitions=edgesOfmxVertex2SCXMLString(n,value);
		SCXMLNode initialChild=getInitialChildOfmxCell(n);
		if (initialChild!=null) oninitialentry=initialChild.getOnInitialEntry();
		String close;
		if (isRoot) {
			ret=scxml1;
			close="</scxml>";
		} else if (value.isParallel()) {
			ret="<parallel";
			close="</parallel>";
		} else if (value.isFinal()) {
			ret="<final";
			close="</final>";
		} else {
			ret="<state";
			close="</state>";
		}
		if (!StringUtils.isEmptyString(ID))
			ret+=" id=\""+ID+"\"";
		if (StringUtils.isEmptyString(oninitialentry) && (initialChild!=null))
			ret+=" initial=\""+initialChild.getID()+"\"";
		ret+=">";

		// save the geometric information of this node:
		ret+="<!-- "+getGeometryString(view,n)+" -->";
		
		if (!StringUtils.isEmptyString(datamodel))
			ret+="<datamodel>"+datamodel+"</datamodel>";
		if (value.isInitial() && !StringUtils.isEmptyString(oninitialentry=value.getOnInitialEntry()))
			ret+="<initial><transition>"+oninitialentry+"</transition></initial>";
		if (!StringUtils.isEmptyString(donedata))
			ret+="<donedata>"+donedata+"</donedata>";
		if (!StringUtils.isEmptyString(onentry))
			ret+="<onentry>"+onentry+"</onentry>";
		if (!StringUtils.isEmptyString(onexit))
			ret+="<onexit>"+onexit+"</onexit>";
		if (!StringUtils.isEmptyString(transitions))
			ret+=transitions;
		int nc=n.getChildCount();
		for(int i=0;i<nc;i++) {
			mxCell c=(mxCell) n.getChildAt(i);
			if (c.isVertex())
				ret+=mxVertex2SCXMLString(view,c,false);
		}
		ret+=close;
		return ret;
	}
	private String getGeometryString(mxGraphView view, mxCell n) {
		mxCellState ns=view.getState(n);
		if (ns!=null)
			return "x="+(int)ns.getX()+" y="+(int)ns.getY()+" w="+(int)ns.getWidth()+" h="+(int)ns.getHeight();
		else return "";
	}
	private SCXMLNode getInitialChildOfmxCell(mxCell n) {
		int nc=n.getChildCount();
		for(int i=0;i<nc;i++) {
			mxCell c=(mxCell) n.getChildAt(i);
			if (c.isVertex()) {
				SCXMLNode value=(SCXMLNode) c.getValue();
				assert(value!=null);
				if (value.isInitial())
					return value;
			}
		}
		return null;
	}
	private String edgesOfmxVertex2SCXMLString(mxCell n, SCXMLNode value) {
		int ec=n.getEdgeCount();
		String[] sortedEdges=new String[ec];
		int maxOutgoingEdge=-1;
		for(int i=0;i<ec;i++) {
			mxCell e=(mxCell) n.getEdgeAt(i);
			mxCell source=(mxCell) e.getSource();
			mxCell target=(mxCell) e.getTarget();
			if (source==n) {
				String ret="";
				SCXMLNode targetValue=(SCXMLNode) target.getValue();
				SCXMLEdge edgeValue=(SCXMLEdge) e.getValue();
				assert((edgeValue!=null) && (targetValue!=null));
				assert(!targetValue.getID().equals(""));
				String cond=edgeValue.getCondition();
				String event=edgeValue.getEvent();
				String exe=edgeValue.getExe();
				ret="<transition";
				if (!StringUtils.isEmptyString(event))
					ret+=" event=\""+event+"\"";
				if (!StringUtils.isEmptyString(cond))
					ret+=" cond=\""+cond+"\"";
				ret+=" target=\""+targetValue.getID()+"\"";
				if (!StringUtils.isEmptyString(exe))
					ret+=">"+exe+"</transition>";
				else
					ret+="/>";
				int order=edgeValue.getOrder();
				if (maxOutgoingEdge<order) maxOutgoingEdge=order;
				sortedEdges[order]=ret;
			}
		}
		String ret="";
		for (int i=0;i<=maxOutgoingEdge;i++) {
			ret+=sortedEdges[i];
		}
		return ret;
	}
	private String mxEdge2SCXMLString(mxCell e) {
		assert(e.isEdge());
		return null;
	}
	public static mxCell followUniqueDescendantLineTillSCXMLValueIsFound(mxIGraphModel model) {
		mxCell n=(mxCell) model.getRoot();
		while(true) {
			Object v=n.getValue();
			if (v instanceof SCXMLNode) {
				return n;
			} else {
				if (n.getChildCount()==1) {
					n=(mxCell) n.getChildAt(0);
				} else {
					return null;
				}
			}
		}
	}
}
