package org.mgnl.nicki.consulting.core.helper;

import java.util.function.Consumer;

import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ViewHelper {


	public static <T> void showEditView(VerticalLayout layout, T object, Consumer<Component> consumer) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				consumer.accept(component);
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidthFull();
		if (object != null) {
			beanViewer.setDbBean(object);
			layout.add(beanViewer);
		}
	}

}
