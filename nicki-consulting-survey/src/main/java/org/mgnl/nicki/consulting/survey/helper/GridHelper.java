package org.mgnl.nicki.consulting.survey.helper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.survey.model.SurveyConfigWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.core.helper.NameValue;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.ValueProvider;

public class GridHelper {

	public static <T extends SurveyTopicWrapper> void addSurveyTopicWrapperColumns(Grid<T> grid, SurveyConfigWrapper survey) {
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 0, T::get0);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 1, T::get1);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 2, T::get2);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 3, T::get3);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 4, T::get4);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 5, T::get5);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 6, T::get6);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 7, T::get7);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 8, T::get8);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 9, T::get9);
		if (survey.getAddComment()) {
			addCommentColumn(grid, 10, T::get10);
		}
	}

	public static <T extends SurveyTopicWrapper> void addSummarySurveyTopicWrapperColumns(Grid<T> grid, SurveyConfigWrapper survey) {
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 0, T::getSummary0);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 1, T::getSummary1);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 2, T::getSummary2);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 3, T::getSummary3);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 4, T::getSummary4);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 5, T::getSummary5);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 6, T::getSummary6);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 7, T::getSummary7);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 8, T::getSummary8);
		addColumn(grid, survey.getTitles(), survey.getDescriptions(), 9, T::getSummary9);

	}

	public static <T>  void addColumn(Grid<T> grid, String[] titles, String[] descriptions, int i, ValueProvider<T, Component> fn) {
		if (titles.length > i) {
			grid.addComponentColumn(fn).setHeader(titles[i]).setKey("column" + i);
			if (StringUtils.isNotBlank(descriptions[i])) {
				if (grid.getHeaderRows() == null || grid.getHeaderRows().size() == 1) {
					grid.appendHeaderRow();
				}
				grid.getHeaderRows().get(1).getCell(grid.getColumnByKey("column" + i)).setComponent(new Span(descriptions[i]));
			}
		}
	}

	public static <T>  void addCommentColumn(Grid<T> grid, int i, ValueProvider<T, Component> fn) {
		grid.addComponentColumn(fn).setHeader("Anmerkung").setKey("column" + i);
	}
	

	
	public static Grid<NameValue> getDetails(List<NameValue> values) {
		
		Grid<NameValue> detailsGrid = new Grid<>();
		detailsGrid.addColumn(nv -> nv.getName());
		detailsGrid.addColumn(nv -> nv.getValue());
		detailsGrid.setItems(values);
		detailsGrid.setAllRowsVisible(true);
		
		return detailsGrid;

	}
}
