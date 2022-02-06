package com.github.simulatan.countvotes.utils;

import jcurses.util.Rectangle;
import jcurses.widgets.MenuList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

import static com.github.simulatan.countvotes.Main.log;

public class PopUpMenu extends jcurses.widgets.PopUpMenu {

	private final int initialX;
	private final int initialY;
	private final boolean center;

	/**
	 * The constructor
	 *
	 * @param x     the x coordinate of the dialog window's top left corner
	 * @param y     the y coordinate of the dialog window's top left corner
	 * @param title window's title
	 */
	public PopUpMenu(int x, int y, String title) {
		this(x, y, title, true);
	}

	/**
	 * The constructor
	 *
	 * @param x     the x coordinate of the dialog window's top left corner
	 * @param y     the y coordinate of the dialog window's top left corner
	 * @param title window's title
	 */
	public PopUpMenu(int x, int y, String title, boolean center) {
		super(x, y, title);
		this.initialX = x;
		this.initialY = y;
		this.center = center;
		if (center)
			center();
	}

	private final HashMap<String, Runnable> actions = new HashMap<>();
	public void add(String item, Runnable action) {
		this.add(item);
		actions.put(item, action);
	}

	private static final Field _x;
	private static final Field _y;
	private static final Field _menuListField;
	private static final Method getPreferredSize;

	static {
		Field _x1, _y1, _menuList1;
		Method getPreferredSize1;
		try {
			_x1 = jcurses.widgets.PopUpMenu.class.getDeclaredField("_x");
			_x1.setAccessible(true);
		} catch (NoSuchFieldException e) {
			log(e.getMessage());
			_x1 = null;
		}
		try {
			_y1 = jcurses.widgets.PopUpMenu.class.getDeclaredField("_y");
			_y1.setAccessible(true);
		} catch (NoSuchFieldException e) {
			log(e.getMessage());
			_y1 = null;
		}
		try {
			_menuList1 = jcurses.widgets.PopUpMenu.class.getDeclaredField("_menuList");
			_menuList1.setAccessible(true);
		} catch (NoSuchFieldException e) {
			log(e.getMessage());
			_menuList1 = null;
		}
		try {
			getPreferredSize1 = jcurses.widgets.MenuList.class.getDeclaredMethod("getPreferredSize");
			getPreferredSize1.setAccessible(true);
		} catch (NoSuchMethodException e) {
			log(e.getMessage());
			getPreferredSize1 = null;
		}
		_x = _x1;
		_y = _y1;
		_menuListField = _menuList1;
		getPreferredSize = getPreferredSize1;
	}

	private MenuList _menuList;

	{
		try {
			_menuList = (MenuList) _menuListField.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void center() {
		try {
			Rectangle preferredSize = (Rectangle) getPreferredSize.invoke(_menuList);
			int width = preferredSize.getWidth();
			int height = preferredSize.getHeight();
			_x.set(this, initialX - width / 2 + 6); // 6 is the width of the border
			_y.set(this, initialY - height / 2 + 4);
		} catch (Exception e) {
			log(e.getMessage());
		}
	}

	@Override
	public void show() {
		super.show();
		Optional.ofNullable(this.actions.get(this.getSelectedItem())).ifPresent(Runnable::run);
	}

	@Override
	public void add(String item) {
		super.add(item);
		if (center)
			center();
	}

	@Override
	public void add(int pos, String item) {
		super.add(pos, item);
		if (center)
			center();
	}

	@Override
	public void remove(int pos) {
		super.remove(pos);
		if (center)
			center();
	}

	@Override
	public void remove(String item) {
		super.remove(item);
		if (center)
			center();
	}
}