package com.mxgraph.examples.swing.editor.scxml;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;

public class SCXMLAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		return new CharTokenizer(reader) {
			
			@Override
			protected boolean isTokenChar(char c) {
				switch (c) {
				case '-':
				case '.':
				case '_':
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					return false;
				default:
					return true;
				}
			}
		};
	}
}

