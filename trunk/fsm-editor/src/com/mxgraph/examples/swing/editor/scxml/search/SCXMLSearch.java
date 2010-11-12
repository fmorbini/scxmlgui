package com.mxgraph.examples.swing.editor.scxml.search;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLGraph;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;

public class SCXMLSearch {

	
	private static final String INDEXID = "LUCENECELLID";
	private RAMDirectory idx=new RAMDirectory();
	private IndexSearcher searcher;
	private int defaultNumResults;
	private Class analyzer=null;
	private SCXMLGraphEditor editor;

	public SCXMLSearch(SCXMLGraphEditor editor,int numResults) {
		this.editor=editor;
        this.defaultNumResults=numResults;

        SCXMLGraph graph = editor.getGraphComponent().getGraph();
		graph.addListener(mxEvent.CELLS_REMOVED, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				Object[] arg=(Object[]) evt.getProperty("cells");
				if (arg!=null) {
					ArrayList<mxCell> cells=new ArrayList<mxCell>();
					for (Object o:arg) {
						if (o instanceof mxCell) {
							cells.add((mxCell) o);
						}
					}
					try {
						updateIndex(cells, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		graph.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				Object[] arg=(Object[]) evt.getProperty("cells");
				if (arg!=null) {
					ArrayList<mxCell> cells=new ArrayList<mxCell>();
					for (Object o:arg) {
						if (o instanceof mxCell) {
							cells.add((mxCell) o);
						}
					}
					try {
						updateIndex(cells, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void buildIndex() throws CorruptIndexException, LockObtainFailedException, IOException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		SCXMLGraph graph = editor.getGraphComponent().getGraph();
		analyzer=Class.forName("com.mxgraph.examples.swing.editor.scxml.SCXMLAnalyzer");
		Constructor constructor = analyzer.getConstructor();
		IndexWriter writer = new IndexWriter(idx, (Analyzer) constructor.newInstance(), IndexWriter.MaxFieldLength.UNLIMITED);
        writer.deleteAll();
        writer.commit();
        
		mxIGraphModel model = graph.getModel();
		mxCell root=(mxCell) model.getRoot();
		root.getChildCount();

		addCellsToIndex(root,writer,true);
        //writer.optimize();
        writer.close();

        searcher = new IndexSearcher(idx);
	}
	
	HashSet<String> cellsAlreadySeen=new HashSet<String>();
	HashMap<String,mxCell> doc2mxCell=new HashMap<String, mxCell>();
	private void addCellsToIndex(mxCell c,IndexWriter writer,boolean isRoot) throws CorruptIndexException, IOException {
		if (isRoot) {
			cellsAlreadySeen.clear();
			doc2mxCell.clear();
		}
		if (c!=null) {
			String cName=c.getId();
			if (!cellsAlreadySeen.contains(cName)) {
				cellsAlreadySeen.add(cName);
				if (c.getValue()!=null) {
					Document doc=createDocumentForCell(c);
					writer.addDocument(doc);
					doc2mxCell.put(doc.get(INDEXID), c);
				}
				int numChildren=c.getChildCount();
				for(int i=0;i<numChildren;i++) {
					mxCell child=(mxCell) c.getChildAt(i);
					addCellsToIndex(child,writer,false);
				}
			}
		}
	}
	private Document createDocumentForSCXMLEdge(SCXMLEdge v,String cellID) {
        Document doc = new Document();
        doc.add(new Field(INDEXID, cellID,Field.Store.YES,Field.Index.ANALYZED));
        doc.add(new Field("source", new StringReader(v.getSCXMLSource().toLowerCase())));
        doc.add(new Field("target", new StringReader(v.getSCXMLTargets().toString().toLowerCase())));
        doc.add(new Field("eve", new StringReader(v.getEvent().toLowerCase())));
        doc.add(new Field("cnd", new StringReader(v.getCondition().toLowerCase())));
        doc.add(new Field("eexe", new StringReader(v.getExe().toLowerCase())));
        doc.add(new Field("com", new StringReader(v.getComments().toLowerCase())));
        doc.add(new Field("all", new StringReader((v.getSCXMLSource()+" "+v.getSCXMLTargets().toString()+" "+v.getEvent()+" "+v.getCondition()+" "+v.getExe()).toLowerCase())));
        return doc;
	}
	private Document createDocumentForSCXMLNode(SCXMLNode v,String cellID) {
        Document doc = new Document();
        doc.add(new Field(INDEXID, cellID,Field.Store.YES,Field.Index.ANALYZED));
        doc.add(new Field("id", new StringReader(v.getID().toLowerCase())));
        doc.add(new Field("inc", new StringReader(v.getOutsourcedLocation().toLowerCase())));
        doc.add(new Field("dm", new StringReader(v.getDatamodel().toLowerCase())));
        doc.add(new Field("ns", new StringReader(v.getNamespace().toLowerCase())));
        doc.add(new Field("entry", new StringReader(v.getOnEntry().toLowerCase())));
        doc.add(new Field("exit", new StringReader(v.getOnExit().toLowerCase())));
        doc.add(new Field("init", new StringReader(v.getOnInitialEntry().toLowerCase())));
        doc.add(new Field("dd", new StringReader(v.getDoneData().toLowerCase())));
        doc.add(new Field("com", new StringReader(v.getComments().toLowerCase())));
        doc.add(new Field("all", new StringReader((v.getID()+" "+v.getSRC().getLocation()+" "+v.getDatamodel()+" "+v.getNamespace()+" "+v.getOnEntry()+" "+v.getOnExit()+" "+v.getOnInitialEntry()+" "+v.getDoneData()).toLowerCase())));
		return doc;
	}
	
	private Document createDocumentForCell(mxCell c) {
		String cellID=c.getId();
		Object v=c.getValue();
		Document doc=(c.isVertex())?createDocumentForSCXMLNode((SCXMLNode)v,cellID):createDocumentForSCXMLEdge((SCXMLEdge)v,cellID);
		return doc;
	}
	public void updateIndex(Collection<mxCell> cs,boolean add) throws CorruptIndexException, IOException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Constructor constructor = analyzer.getConstructor();
		IndexWriter writer = new IndexWriter(idx, (Analyzer) constructor.newInstance(), IndexWriter.MaxFieldLength.UNLIMITED);
		try {
			for(mxCell c:cs) {
				Query q=getQueryForGettingDocumentOfCell(c);
				//System.out.println("query: "+q.getClass()+" "+q);
				writer.deleteDocuments(q);

				TopDocs result = searcher.search(q, this.defaultNumResults);
				//printResult(result);
	        	int numHits=result.totalHits;

	        	for(int i=0;i<numHits;i++) {
	        		int docPos=result.scoreDocs[i].doc;
	        		Document doc = searcher.doc(docPos);
	        		doc2mxCell.remove(doc.get(INDEXID));
	        	}

	        	writer.commit();
				if (add) {
					Document doc=createDocumentForCell(c);
					doc2mxCell.put(doc.get(INDEXID), c);
					writer.addDocument(doc);
				}
			}
			//writer.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
        writer.close();        
        searcher.close();
        searcher = new IndexSearcher(idx);
    }
	
	public Query getQueryForGettingDocumentOfCell(mxCell c) throws ParseException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		String cellID=c.getId();
        Query q=new TermQuery(new Term(INDEXID, cellID));
        return q;
	}
	
	public ArrayList<mxCell> find(String query) throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Constructor constructor = analyzer.getConstructor();
        QueryParser queryParser = new QueryParser(Version.LUCENE_30,"all",(Analyzer) constructor.newInstance());
        try {
        	Query q = queryParser.parse(query);
			//System.out.println("query: "+q.getClass()+" "+q);
        	TopDocs result = searcher.search(q, this.defaultNumResults);
        	int numHits=result.totalHits;

        	ArrayList<mxCell>ret=new ArrayList<mxCell>();
        	for(int i=0;i<numHits;i++) {
        		int docPos=result.scoreDocs[i].doc;
        		Document doc = searcher.doc(docPos);
        		mxCell matchingCell=doc2mxCell.get(doc.get(INDEXID));
        		if (matchingCell!=null) {
        			ret.add(matchingCell);
        		}
        	}
        	return ret;
        } catch (ParseException e) {
        }
		return null;
	}

	public void printResult(TopDocs result) throws CorruptIndexException, IOException {
		int numHits=result.totalHits;

		for(int i=0;i<numHits;i++) {
			int docPos=result.scoreDocs[i].doc;
			Document doc = searcher.doc(docPos);
			mxCell matchingCell=doc2mxCell.get(doc.get(INDEXID));
			if (matchingCell!=null) {
				System.out.println(" search result: "+matchingCell.getValue());
			}
		}
	}
}
