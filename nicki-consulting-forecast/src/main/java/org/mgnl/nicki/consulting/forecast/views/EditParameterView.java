package org.mgnl.nicki.consulting.forecast.views;

import java.util.Collection;
import java.util.Optional;

import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;

@SuppressWarnings("serial")
public abstract class EditParameterView<T> extends VerticalLayout implements View {
	
	private SplitLayout horizontalSplitPanel;
	
	private Grid<T> grid;
	private VerticalLayout gridCanvas;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private DialogBase editWindow;


	public EditParameterView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			gridCanvas.removeAll();
			canvas.removeAll();
			
			grid = new Grid<>();
			addColumns(grid);
			grid.setSizeFull();
			grid.addSelectionListener(event -> {
				canvas.removeAll();
				Optional<T> itemOptional = event.getFirstSelectedItem();
				if (itemOptional.isPresent()) {
					T target = itemOptional.get();
					showEditView(canvas, target);
				}
			});
			Button newStageButton = new Button();
			newStageButton.setText("Neue " + getItemName());
			newStageButton.setWidth("-1px");
			newStageButton.setHeight("-1px");
			newStageButton.addClickListener(event -> showEditView(getGlazz(), Optional.empty(), "Neue " + getItemName(), getItemName() + " bearbeiten"));
			gridCanvas.add(newStageButton);
			
			gridCanvas.add(grid);
			gridCanvas.setFlexGrow(1, grid);

	        GridContextMenu<T> contextMenu = new GridContextMenu<>(grid);
	        // handle item right-click
	        contextMenu.setDynamicContentHandler(item -> {
	        	contextMenu.removeAll();
	            if (item != null) {
	            	grid.select(item);
	            	T target = item;
	           		contextMenu.addItem("Löschen", selectedItem -> delete(Optional.of((T) target)));
	            	return true;
	            } else {
	            	return false;
	            }
	        });
			
			isInit = true;
		}
		

		grid.setItems(getAll());
	}

	protected abstract Class<T> getGlazz();

	protected abstract Collection<T> getAll();

	protected abstract void delete(Optional<T> of);

	protected abstract String getItemName();

	protected abstract void addColumns(Grid<T> grid);

	private void showEditView(VerticalLayout layout, T object) {
		DbBeanCloseListener closeListener = new DbBeanCloseListener() {			
			@Override
			public void close(Component component) {
				init();
			}
		};
		DbBeanViewer beanViewer = new DbBeanViewer(closeListener);

		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidthFull();
		if (object != null) {
			beanViewer.setDbBean(object);
			layout.add(beanViewer);
		}
	}
	
	private void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption, Object... foreignObjects) {
		DbBeanCloseListener closeListener = new DbBeanCloseListener() {			
			@Override
			public void close(Component component) {
				editWindow.close();
				init();
			}
		};
		DbBeanViewer beanViewer = new DbBeanViewer(closeListener);
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidth("400px");
		String windowTitle;
		if (!object.isPresent()) {
			try {
				beanViewer.init(clazz, foreignObjects);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			windowTitle = newCaption;
		} else {
			beanViewer.setDbBean(object.get());
			windowTitle = editCaption;
		}
		editWindow = new DialogBase(windowTitle, beanViewer);
		editWindow.setModal(true);
		editWindow.setHeightFull();
		editWindow.open();
	}
	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		
		// horizontalSplitPanel
		horizontalSplitPanel = new SplitLayout();
		horizontalSplitPanel.setOrientation(Orientation.HORIZONTAL);
		horizontalSplitPanel.setSizeFull();
		add(horizontalSplitPanel);
		
		gridCanvas = new VerticalLayout();
		gridCanvas.setSizeFull();
		gridCanvas.setMargin(false);
		gridCanvas.setSpacing(true);
		horizontalSplitPanel.addToPrimary(gridCanvas);
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setMargin(false);
		canvas.setSpacing(false);
		horizontalSplitPanel.addToSecondary(canvas);
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setApplication(NickiApplication arg0) {
		// TODO Auto-generated method stub
		
	}

}
