package com.github.simulatan.countvotes.utils;

import com.github.simulatan.countvotes.Main;

import java.io.OutputStream;
import java.io.PrintStream;

public class LogPrintStream extends PrintStream {

	public LogPrintStream(OutputStream out) {
		super(out);
	}

	public void print(String s) {
		Main.log(s);
	}
}