package org.mgnl.nicki.consulting.forecast.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.consulting.forecast.model.ForecastCategory;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class CategoriesView extends EditParameterView<ForecastCategory> implements View {

	public CategoriesView() {
	}

	protected void delete(Optional<ForecastCategory> categoryOptional ) {
		if (!categoryOptional.isPresent()) {
			Notification.show("Bitte erst Kategorie auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			ForecastCategory category = categoryOptional.get();
			try {
				if (ForecastHelper.hasDealsWithCategory(category)) {
					Notification.show("Die Kategorie wurde benutzt", Type.HUMANIZED_MESSAGE);
				} else {
					try {
						ForecastHelper.delete(category);
						init();
					} catch (NotSupportedException e) {
						Notification.show("Löschen wird nicht unterstützt", Type.HUMANIZED_MESSAGE);
					}
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Die Kategorie konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void addColumns(Grid<ForecastCategory> grid) {
		grid.addColumn(ForecastCategory::getName).setHeader("Kategorie");
	}

	@Override
	protected String getItemName() {
		return "Kategorie";
	}

	@Override
	protected Collection<ForecastCategory> getAll() {
		return ForecastHelper.getAllCategories();
	}

	@Override
	protected Class<ForecastCategory> getGlazz() {
		return ForecastCategory.class;
	}

}
