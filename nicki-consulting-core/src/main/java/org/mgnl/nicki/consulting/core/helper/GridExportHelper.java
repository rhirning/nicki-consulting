package org.mgnl.nicki.consulting.core.helper;

import java.lang.reflect.Field;
import java.util.Date;

import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.helper.BeanHelper;
import org.mgnl.nicki.db.helper.Type;
import org.mgnl.nicki.editor.templates.export.GridExport;
import org.mgnl.nicki.editor.templates.export.GridExportColumn;

import com.vaadin.flow.component.grid.Grid;

public class GridExportHelper {
	
	public static <T> void addExportColumns(GridExport<T> gridExport, Class<T> clazz, String... hiddenAttributes) {
		for (Field field : BeanHelper.getFields(clazz)) {
			if (hiddenAttributes == null || !DataHelper.contains(hiddenAttributes, field.getName())) {
				gridExport.addColumn(createExportColumn(clazz, field.getName()));
			}
		}

	}

	private static <T> GridExportColumn<T> createExportColumn(Class<T> clazz, String attributeName) {
		Field field;
		try {
			field = clazz.getDeclaredField(attributeName);
		} catch (NoSuchFieldException | SecurityException e) {
			field = null;
		}
		if (field != null) {
			Attribute dbAttribute = BeanHelper.getBeanAttribute(clazz, attributeName);
			if (dbAttribute != null) {
				GridExportColumn<T> gridExportColumn; 
				Type type = BeanHelper.getTypeOfField(clazz, field.getName());
				if (type == Type.DATE) {
					gridExportColumn =  new GridExportColumn<>(t -> {
						Date date = (Date) BeanHelper.getValue(t, attributeName);
						if (date != null) {
							return DataHelper.getDisplayDay(date);
						} else {
							return "";
						}
					});
				} else if (type == Type.TIMESTAMP) {
					gridExportColumn = new GridExportColumn<>(t -> {
						Date date = (Date) BeanHelper.getValue(t, attributeName);
						if (date != null) {
							return DataHelper.getMilli(date);
						} else {
							return "";
						}
					});
				} else if (type == Type.INT) {
					gridExportColumn = new GridExportColumn<>(t -> (int) BeanHelper.getValue(t, attributeName));
				} else if (type == Type.LONG) {
					gridExportColumn = new GridExportColumn<>(t -> (long) BeanHelper.getValue(t, attributeName));
				} else if (type == Type.FLOAT) {
					gridExportColumn = new GridExportColumn<>(t -> (float) BeanHelper.getValue(t, attributeName));
				} else if (type == Type.BOOLEAN) {
					gridExportColumn = new GridExportColumn<>(t -> DataHelper.booleanOf((String) BeanHelper.getValue(t, attributeName)));
				} else {
					gridExportColumn = new GridExportColumn<>(t -> (String) BeanHelper.getValue(t, attributeName));
				}
				return gridExportColumn.setHeader(attributeName);
			}
		}
		return null;
	}

	public static <T> void addColumns(Grid<T> grid, Class<T> clazz, String... hiddenAttributes) {
		for (Field field : BeanHelper.getFields(clazz)) {
			if (hiddenAttributes == null || !DataHelper.contains(hiddenAttributes, field.getName())) {
				grid.addColumn(d -> {
					return d != null ? BeanHelper.getValue(d, field.getName()) : null;
				}).setSortable(true).setHeader(BeanHelper.getName(clazz, field.getName()));
			}
		}
	}

}
