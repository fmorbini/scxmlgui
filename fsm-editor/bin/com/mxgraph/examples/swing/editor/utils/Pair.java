package com.mxgraph.examples.swing.editor.utils;

import java.io.Serializable;

// taken from http://en.wikipedia.org/wiki/Generics_in_Java
public class Pair<T, S> implements Serializable {
	private static final long serialVersionUID = 1L;
	public Pair(T f, S s)
	{ 
		first = f;
		second = s;   
	}

	public T getFirst()
	{
		return first;
	}

	public S getSecond() 
	{
		return second;
	}
	
	public void setFirst(T f) {
		first=f;
	}
	public void setSecond(S s) {
		second=s;
	}

	public String toString()
	{ 
		return "(" + first.toString() + ", " + second.toString() + ")"; 
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Pair<?,?>) {
			return ((getFirst()!=null) && getFirst().equals(((Pair) other).getFirst()) &&
					(getSecond()!=null) && getSecond().equals(((Pair) other).getSecond()));
		} else return false;
	}
	
	@Override
	public int hashCode() {
		return (getFirst()+"||"+getSecond()).hashCode();
	}
	
	private T first;
	private S second;
}
