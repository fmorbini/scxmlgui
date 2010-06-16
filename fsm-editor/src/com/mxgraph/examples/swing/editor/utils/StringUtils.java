package com.mxgraph.examples.swing.editor.utils;


public class StringUtils {
	
	public static boolean isEmptyString(String s) {
		return ((s==null) || (s.length()==0));
	}
	
	public static String removeLeadingSpaces(String in) {
		return in.replaceAll("^[\\s]+", "");
	}
	public static String removeTrailingSpaces(String in) {
		return in.replaceAll("[\\s]+$", "");
	}
	public static String removeMultiSpaces(String in) {
		return in.replaceAll("[\\s]{2,}", " ");
	}
	public static String removeLeadingAndTrailingSpaces(String in) {
		return removeLeadingSpaces(removeTrailingSpaces(in));
	}
	public static String cleanupSpaces(String in) {
		return removeLeadingAndTrailingSpaces(removeMultiSpaces(in));
	}
}
