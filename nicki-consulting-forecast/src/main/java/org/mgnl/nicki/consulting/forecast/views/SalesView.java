package org.mgnl.nicki.consulting.forecast.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.consulting.forecast.model.ForecastSales;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SalesView extends EditParameterView<ForecastSales> implements View {

	public SalesView() {
	}

	protected void delete(Optional<ForecastSales> stageOptional ) {
		if (!stageOptional.isPresent()) {
			Notification.show("Bitte erst Stufe auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			ForecastSales stage = stageOptional.get();
			try {
				if (ForecastHelper.hasDealsWithSales(stage)) {
					Notification.show("Die Stufe wurde benutzt", Type.HUMANIZED_MESSAGE);
				} else {
					try {
						ForecastHelper.delete(stage);
						init();
					} catch (NotSupportedException e) {
						Notification.show("Löschen wird nicht unterstützt", Type.HUMANIZED_MESSAGE);
					}
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Die Stufe konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void addColumns(Grid<ForecastSales> grid) {
		grid.addColumn(ForecastSales::getName).setHeader("Vertrieb");
	}

	@Override
	protected String getItemName() {
		return "Vertrieb";
	}

	@Override
	protected Collection<ForecastSales> getAll() {
		return ForecastHelper.getAllSales();
	}

	@Override
	protected Class<ForecastSales> getGlazz() {
		return ForecastSales.class;
	}

}
