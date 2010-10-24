package com.mxgraph.examples.swing.editor.fileimportexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.OutSource.OUTSOURCETYPE;
import com.mxgraph.examples.swing.editor.scxml.SCXMLFileChoser;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraphComponent;
import com.mxgraph.examples.swing.editor.utils.StringUtils;
import com.mxgraph.examples.swing.editor.utils.XMLUtils;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraphView;

public class SCXMLImportExport implements IImportExport {
	
	private SCXMLNode root=null;
	private HashMap<String,SCXMLNode> internalID2nodes=new HashMap<String, SCXMLNode>();
	private HashMap<String,ArrayList<SCXMLNode>> internalID2clusters=new HashMap<String, ArrayList<SCXMLNode>>();
	private HashMap<String,SCXMLNode> scxmlID2nodes=new HashMap<String, SCXMLNode>();
	private HashMap<String,HashMap<String,HashSet<SCXMLEdge>>> fromToEdges=new HashMap<String, HashMap<String,HashSet<SCXMLEdge>>>();
	private int internalIDcounter=11;

	private HashSet<SCXMLEdge> getEdges(String SCXMLfromID,String SCXMLtoID) {
		assert(!SCXMLfromID.equals(""));
		HashMap<String, HashSet<SCXMLEdge>> toEdge=fromToEdges.get(SCXMLfromID);
		return (toEdge==null)?null:toEdge.get(SCXMLtoID);
	}
	private int getNumEdgesFrom(String SCXMLfromID) {
		assert(!SCXMLfromID.equals(""));
		HashMap<String, HashSet<SCXMLEdge>> toEdge=fromToEdges.get(SCXMLfromID);
		if (toEdge==null) return 0;
		else {
			int tot=0;
			for(HashSet<SCXMLEdge> es:toEdge.values()) {
				tot+=es.size();
			}
			return tot;
		}
	}
	private void addEdge(HashMap<String,Object> ec) throws Exception {
		System.out.println("add edge: "+ec.get(SCXMLEdge.SOURCE)+"->"+ec.get(SCXMLEdge.TARGETS));
		addEdge((String)ec.get(SCXMLEdge.SOURCE),(ArrayList<String>) ec.get(SCXMLEdge.TARGETS),(String)ec.get(SCXMLEdge.CONDITION),(String)ec.get(SCXMLEdge.EVENT),(String)ec.get(SCXMLEdge.EDGEEXE),(HashMap<String,String>)ec.get(SCXMLEdge.EDGEGEO));
	}
	private void addEdge(String SCXMLfromID,ArrayList<String> targets,String cond,String event,String content, HashMap<String, String> geometry) throws Exception {
		SCXMLEdge edge = new SCXMLEdge(SCXMLfromID,targets,cond, event, content,geometry);
		edge.setInternalID(getNextInternalID());
		int oe=getNumEdgesFrom(SCXMLfromID);
		edge.setOrder((oe<=0)?0:oe);

		if (targets==null) {
			targets=new ArrayList<String>();
			targets.add(SCXMLfromID);
		}
		for(String target:targets) {
			HashSet<SCXMLEdge> edges = getEdges(SCXMLfromID, target);
			if (edges==null) {
				edges=new HashSet<SCXMLEdge>();
				edges.add(edge);
				HashMap<String, HashSet<SCXMLEdge>> toEdges=fromToEdges.get(SCXMLfromID);
				if (toEdges==null) {
					toEdges=new HashMap<String, HashSet<SCXMLEdge>>();
					fromToEdges.put(SCXMLfromID, toEdges);
				}
				toEdges.put(target,edges);
			} else {
				edges.add(edge);
			}
		}
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
	public mxCell getCellFromInternalID(String internalID) {
		assert(!internalID.equals(""));
		return internalID2cell.get(internalID);
	}
	
	public String getNextInternalID() {
		return ""+internalIDcounter++;
	}
	
	public void addSCXMLNode(SCXMLNode node) {
		//System.out.println("Adding node: "+node);
		String scxmlID=node.getID();
		String internalID=getNextInternalID();
		node.setInternalID(internalID);
		if (!StringUtils.isEmptyString(scxmlID)) scxmlID2nodes.put(scxmlID, node);
		internalID2nodes.put(internalID, node);
	}

	private SCXMLNode handleSCXMLNode(Node n, SCXMLNode pn, Boolean isParallel, Boolean isHistory) throws Exception {
		NamedNodeMap att = n.getAttributes();
		Node nodeID = att.getNamedItem("id");
		String nodeIDString=(nodeID==null)?(n.getNodeName().toLowerCase().equals(SCXMLNode.ROOTID.toLowerCase())?SCXMLNode.ROOTID:""):StringUtils.cleanupSpaces(nodeID.getNodeValue());
		Node nodeHistoryType = att.getNamedItem("type");
		String nodeHistoryTypeString=(nodeHistoryType==null)?"shallow":StringUtils.cleanupSpaces(nodeHistoryType.getNodeValue());
		SCXMLNode.HISTORYTYPE historyType=null;
		try {
			historyType=SCXMLNode.HISTORYTYPE.valueOf(nodeHistoryTypeString.toUpperCase());
		} catch (Exception e) {
			e.printStackTrace();
		}
		SCXMLNode node;
		if (nodeIDString.equals("") || ((node=scxmlID2nodes.get(nodeIDString))==null)) {
			node=new SCXMLNode();
			node.setID(nodeIDString);
			addSCXMLNode(node);
		}
		// see issue 7 in google code website
		if (node!=pn) setNodeAsChildrenOf(node,pn);
		if ((!isHistory) || (historyType==null)) {
			node.setParallel(isParallel);
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
			// set namespace attribute
			int na=att.getLength();
			String namespace="";
			for(int i=0;i<na;i++) {
				Node a=att.item(i);
				String name=a.getNodeName().toLowerCase();
				if (name.startsWith("xmlns")) {
					namespace+=a.getNodeName()+"=\""+a.getNodeValue()+"\"\n";
				} else if (name.equals("src")) node.addToOutsourcingChildren(new OutSource(OUTSOURCETYPE.SRC,a.getNodeValue()));
			}
			if (!StringUtils.isEmptyString(namespace)) node.setNamespace(namespace);
		} else {
			node.setAsHistory(historyType);
		}
		// set src attribute
		return node;
	}

	public void scanChildrenOf(Node el,SCXMLNode pn, File pwd) throws Exception {
		NodeList states = el.getChildNodes();
		for (int s = 0; s < states.getLength(); s++) {
			Node n = states.item(s);
			getNodeHier(n, pn,pwd);
		}
	}
	public SCXMLNode getNodeHier(Node n, SCXMLNode pn, File pwd) throws Exception {
		SCXMLNode root=null;
		switch (n.getNodeType()) {
		case Node.ELEMENT_NODE:
			String name=n.getNodeName().toLowerCase();
			// STATE: normal or parallel
			Boolean isParallel=false;
			boolean isHistory=false;
			if (name.equals(SCXMLNode.ROOTID.toLowerCase())||name.equals("state")||(isParallel=name.equals("parallel"))||(isHistory=name.equals("history"))) {
				root = handleSCXMLNode(n,pn,isParallel,isHistory);
				// continue recursion on the children of this node
				scanChildrenOf(n, root,pwd);
				processOutsourcingChildrenForNode(root, pwd);
			} else if (name.equals("transition")) {
				addEdge(processEdge(pn,n));
			} else if (name.equals("final")) {
				SCXMLNode node = handleSCXMLNode(n,pn,isParallel,false);
				node.setFinal(true);
				scanChildrenOf(n, node,pwd);
			} else if (name.equals("initial")) {
				//pn.setInitial(true);
				// only one child that is a transition
				NodeList cs = n.getChildNodes();
				for (int i = 0; i < cs.getLength(); i++) {
					Node c = cs.item(i);
					if ((c.getNodeType()==Node.ELEMENT_NODE) &&
							c.getNodeName().toLowerCase().equals("transition")) {
						HashMap<String, Object> edgeContent = processEdge(pn,c);
						//pn.setOnInitialEntry(edgeContent.get(SCXMLEdge.EDGEEXE));
						ArrayList<String> inNames=(ArrayList<String>) edgeContent.get(SCXMLEdge.TARGETS);
						if (inNames.size()>1) throw new Exception("Unhandled multiple initial states. Report test case.");
						for(String inName:inNames) {
							if (inName!=null) {
								SCXMLNode in =getNodeFromSCXMLID(inName);
								if (in==null) in=new SCXMLNode();
								in.setID(inName);
								in.setInitial(true);
								addSCXMLNode(in);
								in.setOnInitialEntry((String) edgeContent.get(SCXMLEdge.EDGEEXE));
							}
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
				pn.addToDataModel(content);
			} else if (name.equals("xi:include")) {
				NamedNodeMap att = n.getAttributes();
				Node nodeLocation = att.getNamedItem("href");
				String location=(nodeLocation==null)?"":StringUtils.cleanupSpaces(nodeLocation.getNodeValue());
				location=StringUtils.cleanupSpaces(location);
				System.out.println(location);
				if (!StringUtils.isEmptyString(location)) {
					pn.addToOutsourcingChildren(new OutSource(OUTSOURCETYPE.XINC,location));
				}
			}
			break;
		case Node.COMMENT_NODE:
			String positionString=n.getNodeValue();
			readNodeGeometry(pn,positionString);
			break;
		}
		return root;
	}

	private void processOutsourcingChildrenForNode(SCXMLNode node,File pwd) throws Exception {
		if (node!=null) {
			HashSet<OutSource> outSources = node.getOutsourcingChildren();
			if (outSources!=null) {
				if ((outSources.size()==1) && !node.isClusterNode()) {
					node.setSRC(outSources.iterator().next());
				} else {
					for(OutSource source:outSources) {
						if (source.getType()==OUTSOURCETYPE.XINC) {
							saveProblematicNodes.add(node);
							String fileLocation=new File(pwd, source.getLocation()).getAbsolutePath();
							readSCXMLFileContentAndAttachAsChildrenOf(fileLocation, node);
						} else {
							throw new Exception("Bug: multiple SRC inclusions.");
						}
					}
				}
			}
		}
	}
	
	private HashMap<String, Object> processEdge(SCXMLNode pn, Node n) throws Exception {
		HashMap<String,Object> ret=new HashMap<String, Object>();
		//event, cond and target attributes
		NamedNodeMap att = n.getAttributes();
		Node condNode = att.getNamedItem("cond");
		String cond=(condNode!=null)?StringUtils.removeLeadingAndTrailingSpaces(condNode.getNodeValue()):"";
		Node eventNode = att.getNamedItem("event");
		String event=(eventNode!=null)?StringUtils.removeLeadingAndTrailingSpaces(eventNode.getNodeValue()):"";
		Node targetNode = att.getNamedItem("target");
		ArrayList<String> targets=null;
		if (targetNode!=null)
			targets=new ArrayList<String>(Arrays.asList(StringUtils.cleanupSpaces(targetNode.getNodeValue()).split("[\\s]")));
		//if ((targets!=null) && (targets.size()>1)) throw new Exception("multiple targets not supported.");
		HashMap<String,String> edgeGeometry=readEdgeGeometry(n);
		String exe=collectAllChildrenInString(n);
		ret.put(SCXMLEdge.CONDITION,cond);
		ret.put(SCXMLEdge.EVENT,event);
		ret.put(SCXMLEdge.TARGETS,targets);
		ret.put(SCXMLEdge.SOURCE,pn.getID());
		ret.put(SCXMLEdge.EDGEEXE,exe);
		ret.put(SCXMLEdge.EDGEGEO,edgeGeometry);
		return ret;
	}
	private String collectAllChildrenInString(Node n) {
		String content="";
		NodeList list = n.getChildNodes();
		int listLength = list.getLength();
		for (int i=0;i<listLength;i++) {
			content+=XMLUtils.domNode2String(list.item(i),true);
		}
		return StringUtils.removeLeadingAndTrailingSpaces(content);
	}
	public SCXMLNode readSCXMLFileContentAndAttachAsChildrenOf(String filename,SCXMLNode parent) throws Exception {
		System.out.println("Parsing file: "+filename);
		File file=new File(filename);
		Document doc = mxUtils.parseXMLFile(file,false,false);
		doc.getDocumentElement().normalize();
		SCXMLNode rootNode=getNodeHier(doc.getDocumentElement(),parent,file.getParentFile());
		System.out.println("Done reading file");
		return rootNode;
	}
	public void readInGraph(SCXMLGraph graph, String filename, boolean ignoreStoredLayout) throws Exception {
		// clean importer data-structures
		internalID2cell.clear();
		internalID2clusters.clear();
		internalID2nodes.clear();
		fromToEdges.clear();
		scxmlID2nodes.clear();
		internalIDcounter=11;

		root=readSCXMLFileContentAndAttachAsChildrenOf(filename, null);
		if (root!=scxmlID2nodes.get(SCXMLNode.ROOTID)) {
			SCXMLNode firstChild=root;
			root=new SCXMLNode();
			root.setID(SCXMLNode.ROOTID);
			addSCXMLNode(root);
			setNodeAsChildrenOf(firstChild, root);
			root.setSaveThisRoot(false);
		}
		
		// empty the graph
		mxCell gr = new mxCell();
		gr.insert(new mxCell());
		graph.getModel().setRoot(gr);
		graph.setDefaultParent(null);
		graph.clearOutsourcedIndex();

		System.out.println("Populating graph."); 
		populateGraph(graph,ignoreStoredLayout);
		System.out.println("Done populating graph."); 
		// set the SCXML (this.root) mxCell as not deletable.
		gr=internalID2cell.get(root.getInternalID());
		graph.setCellAsDeletable(gr, false);

		graph.setDefaultParent(gr);
	}
	private ArrayList<SCXMLNode> saveProblematicNodes=new ArrayList<SCXMLNode>();
	public boolean hasUnhandledXIncludeUsage() { return !saveProblematicNodes.isEmpty(); }
	public int displayWarningAboutUnhandledXIncludeUsage(SCXMLGraphEditor editor,boolean asQuestion) {
		String message=mxResources.get("xincludeSaveProblem")+"\n";
		for (SCXMLNode n:saveProblematicNodes) message+=n.getID()+"\n";
		if (asQuestion) {
			String[] options={mxResources.get("continue"),mxResources.get("cancel")};
			return JOptionPane.showOptionDialog(editor,message,"XInclude problem",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]);
		}
		else JOptionPane.showMessageDialog(editor,message,"XInclude problem",JOptionPane.WARNING_MESSAGE);
		return -1;
	}
	@Override
	public void read(String from, mxGraphComponent graphComponent,JFileChooser fc) throws Exception {
		SCXMLGraphComponent gc=(SCXMLGraphComponent)graphComponent;
		SCXMLGraphEditor editor = gc.getGraph().getEditor();
		saveProblematicNodes.clear();
		SCXMLGraph graph = (SCXMLGraph) gc.getGraph();
		readInGraph(graph,from,((SCXMLFileChoser)fc).ignoreStoredLayout());
		if (hasUnhandledXIncludeUsage()) displayWarningAboutUnhandledXIncludeUsage(editor,false);
	}

	private HashMap<String,mxCell> internalID2cell=new HashMap<String, mxCell>();
	private void populateGraph(SCXMLGraph graph,boolean ignoreStoredLayout) {
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
				mxCell cn=internalID2cell.get(internalID);
				//mxCell cn=addOrUpdateNode(graph,n,null);
				//System.out.println(n.getStyle());
                // set geometry and size
				if (cn!=null) {
					cn.setStyle(n.getStyle());
					mxGeometry g=n.getGeometry();
					if ((g!=null) && !ignoreStoredLayout) {
						//graph.setCellAsMovable(cn, false);
						model.setGeometry(cn, g);
					} else if (!internalID2clusters.containsKey(internalID)) {
						graph.updateCellSize(internalID2cell.get(internalID));
					}
				}
			}
			// then add the edges
			for (String fromSCXMLID:fromToEdges.keySet()) {
				HashMap<String, HashSet<SCXMLEdge>> toEdge = fromToEdges.get(fromSCXMLID);
				for (String toSCXMLID:toEdge.keySet()) {
					HashSet<SCXMLEdge> es=toEdge.get(toSCXMLID);
					for (SCXMLEdge e:es) {
						ArrayList<mxCell> ces = addOrUpdateEdge(graph,e,toSCXMLID,ignoreStoredLayout);
						for (mxCell ce:ces) ce.setStyle(e.getStyle());
					}
				}
			}
		} finally {
			model.endUpdate();
		}
	}
	
	private ArrayList<mxCell> addOrUpdateEdge(SCXMLGraph graph, SCXMLEdge edge, String toSCXMLID, boolean ignoreStoredLayout) {
		ArrayList<mxCell> ret=new ArrayList<mxCell>();
		mxCell source=internalID2cell.get(scxmlID2nodes.get(edge.getSCXMLSource()).getInternalID());
		for(String targetSCXMLID:edge.getSCXMLTargets()) {
			if (targetSCXMLID.equals(toSCXMLID)) {
				mxCell target=internalID2cell.get(scxmlID2nodes.get(targetSCXMLID).getInternalID());
				System.out.println("add edge ("+source+"->"+target+")to graph: "+edge);
				mxCell e=(mxCell) graph.insertEdge(internalID2cell.get(root.getInternalID()), edge.getInternalID(),edge,source,target);
				if (!ignoreStoredLayout) {
					mxGeometry geo=edge.getEdgeGeometry(toSCXMLID);
					if (geo!=null) e.setGeometry(geo);
				}
				internalID2cell.put(edge.getInternalID(),e);
				ret.add(e);
			}
		}
		return ret;
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
		if (node.isOutsourcedNode()) graph.addToOutsourced(n);
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
		n.setID("new_node"+getNextInternalID());
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
			return ((SCXMLNode) value).cloneNode();
		} else if (value instanceof SCXMLEdge) {
			return ((SCXMLEdge) value).cloneEdge();
		} else return null;
	}

	@Override
	public void write(mxGraphComponent from, String into) throws Exception {
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
			scxml=XMLUtils.prettyPrintXMLString(scxml, " ",true);			
			System.out.println(scxml);
			mxUtils.writeFile(scxml, into);
		}
	}
	
	private String mxVertex2SCXMLString(mxGraphView view, mxCell n, boolean isRoot) throws Exception {
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
		ID=StringUtils.removeLeadingAndTrailingSpaces(value.getID());
		datamodel=StringUtils.removeLeadingAndTrailingSpaces(value.getDatamodel());
		if (value.isFinal()) donedata=StringUtils.removeLeadingAndTrailingSpaces(value.getDoneData());
		onentry=StringUtils.removeLeadingAndTrailingSpaces(value.getOnEntry());
		onexit=StringUtils.removeLeadingAndTrailingSpaces(value.getOnExit());

		transitions=edgesOfmxVertex2SCXMLString(n,value,view);

		SCXMLNode initialChild=getInitialChildOfmxCell(n);
		if (initialChild!=null) oninitialentry=StringUtils.removeLeadingAndTrailingSpaces(initialChild.getOnInitialEntry());
		String close="";
		if (!isRoot || value.shouldThisRootBeSaved()) {
			if (isRoot) {
				ret="<scxml";
				close="</scxml>";
			} else if (value.isParallel()) {
				ret="<parallel";
				close="</parallel>";
			} else if (value.isFinal()) {
				ret="<final";
				close="</final>";
			} else if (value.isHistoryNode()) {
				ret="<history type=\""+((value.isDeepHistory())?"deep":"shallow")+"\"";
				close="</history>";
			} else {
				ret="<state";
				close="</state>";
			}
			String namespace=StringUtils.removeLeadingAndTrailingSpaces(value.getNamespace().replace("\n", " "));
			if (!StringUtils.isEmptyString(namespace))
				ret+=" "+namespace;
			if (value.isOutsourcedNode() && value.isOutsourcedNodeUsingSRC()) {
				String src=value.getSRC().getLocation();
				ret+=" src=\""+src+"\"";
			}
			if (!StringUtils.isEmptyString(ID))
				ret+=" id=\""+ID+"\"";
			if (StringUtils.isEmptyString(oninitialentry) && (initialChild!=null))
				ret+=" initial=\""+initialChild.getID()+"\"";
			ret+=">";
	
			if (value.isOutsourcedNode() && value.isOutsourcedNodeUsingXInclude()) {
				String src=value.getSRC().getLocation();
				ret+="<xi:include href=\""+src+"\" parse=\"xml\"/>";
			}
			
			// save the geometric information of this node:
			String nodeGeometry=getGeometryString(view,n);
			if (!StringUtils.isEmptyString(nodeGeometry))
				ret+="<!-- "+nodeGeometry+" -->";
			if (!StringUtils.isEmptyString(datamodel))
				ret+="<datamodel>"+datamodel+"</datamodel>";
			if ((!StringUtils.isEmptyString(oninitialentry)) && (initialChild!=null))
				ret+="<initial><transition target=\""+initialChild.getID()+"\">"+oninitialentry+"</transition></initial>";
			if (!StringUtils.isEmptyString(donedata))
				ret+="<donedata>"+donedata+"</donedata>";
			if (!StringUtils.isEmptyString(onentry))
				ret+="<onentry>"+onentry+"</onentry>";
			if (!StringUtils.isEmptyString(onexit))
				ret+="<onexit>"+onexit+"</onexit>";
			if (!StringUtils.isEmptyString(transitions))
				ret+=transitions;
		}
		// add the children only if the node is not outsourced
		if (!value.isOutsourcedNode()) {
			int nc=n.getChildCount();
			for(int i=0;i<nc;i++) {
				mxCell c=(mxCell) n.getChildAt(i);
				if (c.isVertex())
					ret+=mxVertex2SCXMLString(view,c,false);
			}
		}
		ret+=close;
		return ret;
	}
	private String getGeometryString(mxGraphView view, mxCell n) {
		double scale = view.getScale();
		mxCellState ns=view.getState(n);
		if (n.isVertex()) {
			mxICell p = n.getParent();
			double xp=0;
			double yp=0;
			if (p!=null) {
				mxCellState ps=view.getState(p);
				if (ps!=null) {
					xp=ps.getX();
					yp=ps.getY();
				}
			}
			if (ns!=null) return " node-size-and-position x="+((ns.getX()-xp)/scale)+" y="+((ns.getY()-yp)/scale)+" w="+(ns.getWidth()/scale)+" h="+(ns.getHeight()/scale);
			else return null;
		} else if (n.isEdge()) {			
			String target=getIDOfThisEdgeTarget(n);
			mxGeometry geo = n.getGeometry();
			mxPoint offset = geo.getOffset();
			List<mxPoint> points = geo.getPoints();			
			String ret=null;
			if ((points!=null) || (offset!=null)) {
				ret=" edge-path ["+((StringUtils.isEmptyString(target))?"":target)+"] ";
				if (points!=null) {
					for (mxPoint p:points) {
						ret+=" x="+p.getX()+" y="+p.getY();
					}
				}
				if (offset!=null) {
					mxPoint pt = view.getPoint(ns, geo);
					ret+=" pointx="+geo.getX()+" pointy="+geo.getY()+" offsetx="+(offset.getX())+" offsety="+(offset.getY());
				}
			}
			return ret;
		} else return null;
	}
	private String getIDOfThisEdgeTarget(mxCell n) {
		String targetID=((SCXMLNode) n.getTarget().getValue()).getID();
		assert(!StringUtils.isEmptyString(targetID));
		return targetID;
	}
	private static final String numberPattern="[\\deE\\+\\-\\.]+";
	public static final String xyPatternString="[\\s]*x=("+numberPattern+")[\\s]*y=("+numberPattern+")[\\s]*";
	public static final String offsetPatternString="[\\s]*pointx=("+numberPattern+")[\\s]*pointy=("+numberPattern+")[\\s]*offsetx=("+numberPattern+")[\\s]*offsety=("+numberPattern+")[\\s]*";
	public static final Pattern xyPattern=Pattern.compile(xyPatternString);
	public static final Pattern offsetPattern=Pattern.compile(offsetPatternString);
	private static final Pattern nodesizePattern = Pattern.compile("[\\s]*node-size-and-position[\\s]*"+xyPatternString+"[\\s]*w=("+numberPattern+")[\\s]*h=("+numberPattern+")[\\s]*");
	public static final Pattern edgepathPattern = Pattern.compile("[\\s]*edge-path[\\s]*\\[(.*)\\](("+xyPatternString+")*[\\s]*("+offsetPatternString+")*)[\\s]*");
	private void readNodeGeometry(SCXMLNode pn, String positionString) {
		Matcher m = nodesizePattern.matcher(positionString);		
		if (m.matches() && (m.groupCount()==4)) {
			try {
				double x=Double.parseDouble(m.group(1));
				double y=Double.parseDouble(m.group(2));
				double w=Double.parseDouble(m.group(3));
				double h=Double.parseDouble(m.group(4));
				((SCXMLNode) pn).setGeometry(x,y,w,h);
			} catch (Exception e) {
			}
		}
	}
	private HashMap<String,String> readEdgeGeometry(Node root) {
		// this will search for all geometries available (one geometry for each edge target)
		HashMap<String,String> ret=new HashMap<String, String>();
		NodeList states = root.getChildNodes();
		for (int s = 0; s < states.getLength(); s++) {
			Node n = states.item(s);
			switch (n.getNodeType()) {
			case Node.COMMENT_NODE:
				String comment=n.getNodeValue();
				Matcher m = edgepathPattern.matcher(comment);
				if (m.matches()) {
					ret.put(m.group(1),m.group(2));
				}
				break;
			}
		}
		return (ret.isEmpty())?null:ret;
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
	private String edgesOfmxVertex2SCXMLString(mxCell n, SCXMLNode value, mxGraphView view) throws Exception {
		HashMap<Integer,ArrayList<mxCell>> edges=buildListSortedEdges(n);
		int maxOutgoingEdge=-1;
		for(Integer order:edges.keySet()) if (order>maxOutgoingEdge) maxOutgoingEdge=order;
		if (!edges.isEmpty() && (maxOutgoingEdge>=0)) {
			String[] sortedEdges=new String[maxOutgoingEdge+1];
			for(int order=0;order<=maxOutgoingEdge;order++) {
				ArrayList<mxCell> edges4order = edges.get(order);
				if ((edges4order!=null) && !edges4order.isEmpty()) {
					mxCell e=edges4order.get(0);
					mxCell source=(mxCell) e.getSource();
					mxCell target=(mxCell) e.getTarget();
					SCXMLNode targetValue=(SCXMLNode) target.getValue();
					SCXMLEdge edgeValue=(SCXMLEdge) e.getValue();
					String ret="";
					String cond=XMLUtils.escapeStringForXML(StringUtils.removeLeadingAndTrailingSpaces(edgeValue.getCondition()));
					String event=XMLUtils.escapeStringForXML(StringUtils.removeLeadingAndTrailingSpaces(edgeValue.getEvent()));
					String exe=StringUtils.removeLeadingAndTrailingSpaces(edgeValue.getExe());
					ret="<transition";
					if (!StringUtils.isEmptyString(event))
						ret+=" event=\""+event+"\"";
					if (!StringUtils.isEmptyString(cond))
						ret+=" cond=\""+cond+"\"";
					if ((!edgeValue.isCycle()) || edgeValue.isCycleWithTarget()) {
						ret+=" target=\"";
						boolean first=true;
						for(mxCell edge:edges4order) {
							if (first) first=false;
							else ret+=" ";
							ret+=((SCXMLNode)edge.getTarget().getValue()).getID();
						}
						ret+="\"";
					}
					ret+=">";
					if (!StringUtils.isEmptyString(exe))
						ret+=exe;
					for(mxCell edge:edges4order) {
						String edgeGeometry=getGeometryString(view,edge);
						if (!StringUtils.isEmptyString(edgeGeometry))
							ret+="<!-- "+edgeGeometry+" -->";
					}
					ret+="</transition>";
					if (maxOutgoingEdge<order) maxOutgoingEdge=order;
					sortedEdges[order]=ret;
				}
			}
			String ret="";
			for (int i=0;i<=maxOutgoingEdge;i++) {
				ret+=sortedEdges[i];
			}
			return ret;
		} else return null;
	}
	private HashMap<Integer, ArrayList<mxCell>> buildListSortedEdges(mxCell n) throws Exception {
		HashMap<Integer, ArrayList<mxCell>> ret=new HashMap<Integer, ArrayList<mxCell>>();
		int ec=n.getEdgeCount();
		for(int i=0;i<ec;i++) {
			mxCell e=(mxCell) n.getEdgeAt(i);
			mxCell source=(mxCell) e.getSource();
			mxCell target=(mxCell) e.getTarget();
			if (source==n) {
				SCXMLEdge edgeValue=(SCXMLEdge) e.getValue();
				int order=edgeValue.getOrder();
				ArrayList<mxCell> edges4order = ret.get(order);
				if (edges4order==null) ret.put(order, edges4order=new ArrayList<mxCell>());				
				edges4order.add(e);				
			}
		}
		for(ArrayList<mxCell>edges:ret.values()) {
			if (edges.size()>1) {
				SCXMLEdge first=(SCXMLEdge)edges.get(0).getValue();
				for (mxCell edge:edges) {
					if (edge.getValue()!=first) throw new Exception("Error in multitarget edges.");
				}
			}
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
