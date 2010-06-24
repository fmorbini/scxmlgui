package com.mxgraph.examples.swing.editor.scxml;

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;

public class SCXMLAnalyzer extends Analyzer {

	Pattern p = Pattern.compile("[a-zA-Z0-9]");
	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		return new CharTokenizer(reader) {
			
			@Override
			protected boolean isTokenChar(char c) {
				String s=""+c;
				Matcher m = p.matcher(s);
				return m.matches();
			}
		};
	}
}

