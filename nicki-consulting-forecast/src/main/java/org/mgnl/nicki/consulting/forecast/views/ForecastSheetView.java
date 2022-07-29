package org.mgnl.nicki.consulting.forecast.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.consulting.forecast.model.ForecastDeal;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.helper.NameValue;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

@SuppressWarnings("serial")
public class ForecastSheetView extends VerticalLayout implements View {
	
	
	private Grid<ForecastDeal> grid;
	private boolean isInit;
	
	private DialogBase editWindow;


	public ForecastSheetView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			
			grid = new Grid<>();
			grid.addColumn(d -> d.getCustomer().getName()).setHeader("Kunde");
			grid.addColumn(ForecastDeal::getName).setHeader("Deal");
			grid.addColumn(d -> d.getStage().getName()).setHeader("Stufe");
			grid.addColumn(ForecastDeal::getProbability).setHeader("Wahrscheinlichkeit(%)");
			grid.addColumn(ForecastDeal::getTeamSize).setHeader("Teamgröße");
			grid.addColumn(d -> DataHelper.getDisplayDay(d.getStartDate())).setHeader("Start");
			grid.addColumn(d -> DataHelper.getDisplayDay(d.getStartDate())).setHeader("Ende");
			grid.setSizeFull();
			grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

			grid.setItemDetailsRenderer(new ComponentRenderer<>(d -> getDetails(d)));
			
			add(grid);

	        GridContextMenu<ForecastDeal> contextMenu = new GridContextMenu<>(grid);
	        // handle item right-click
	        contextMenu.setDynamicContentHandler(item -> {
	        	contextMenu.removeAll();
	            if (item != null) {
	            	grid.select(item);
	            	contextMenu.addItem("Deal bearbeiten", selectedItem -> showEditView(ForecastDeal.class, Optional.of(item), "Neuer Deal", "Deal bearbeiten"));
	            	return true;
	            } else {
	            	return false;
	            }
	        });
			
			isInit = true;
		}
		

		grid.setItems(ForecastHelper.getAllDeals());
	}


	private Grid<NameValue> getDetails(ForecastDeal deal) {
		List<NameValue> values = new ArrayList<>();
		values.add(new NameValue("Kunde", ForecastHelper.getCustomer(deal.getCustomerId()).getName()));
		values.add(new NameValue("Opportunity", deal.getName()));
		if (StringUtils.isNotBlank(deal.getDescription())) {
			values.add(new NameValue("Beschreibung", deal.getDescription()));
		}
		values.add(new NameValue("Stufe", ForecastHelper.getStage(deal.getStageId()).getName()));
		values.add(new NameValue("Wahrscheinlichkeit", "" + deal.getProbability() + "%"));
		if (deal.getSalesId() != null) {
			values.add(new NameValue("Vertrieb", ForecastHelper.getSales(deal.getSalesId()).getName()));
		}
		values.add(new NameValue("Start", DataHelper.getDisplayDay(deal.getStartDate())));
		values.add(new NameValue("Ende", DataHelper.getDisplayDay(deal.getEndDate())));
		return ForecastHelper.getDetails(values);
	}
	
	private <T> void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption, Object... foreignObjects) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				editWindow.close();
				init();
			}
		});
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
