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
	public static int getRows(String s) {
		if (s==null) return 0;
		else {
			int count=1;
			int length=s.length();
			for(int i=0;i<length;i++) if (s.charAt(i)=='\n') count++;
			System.out.println(count);
			return count;
		}
	}
	public static int getColumns(String s) {
		if (s==null) return 0;
		else {
			int count=0;
			int previ=0;
			int length=s.length();
			for(int i=0;i<length;i++)
				if (s.charAt(i)=='\n') {
					if ((i-previ)>count) {
						count=i-previ;
					}
					previ=i;
				}
			System.out.println(count);
			return count;
		}
	}
}
