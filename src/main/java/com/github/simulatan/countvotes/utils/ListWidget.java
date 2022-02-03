package com.github.simulatan.countvotes.utils;

import jcurses.widgets.List;

public class ListWidget extends List {

	private boolean isFocusable = true;

	public void setFocusable(boolean focusable) {
		isFocusable = focusable;
	}

	@Override
	protected boolean isFocusable() {
		return isFocusable;
	}

	public void addAll(java.util.List<String> elements) {
		elements.forEach(this::add);
	}
}