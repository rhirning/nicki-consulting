package org.mgnl.nicki.consulting.forecast.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.consulting.forecast.model.ForecastStage;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class StagesView extends EditParameterView<ForecastStage> implements View {

	public StagesView() {
	}

	protected void delete(Optional<ForecastStage> stageOptional ) {
		if (!stageOptional.isPresent()) {
			Notification.show("Bitte erst Stufe auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			ForecastStage stage = stageOptional.get();
			try {
				if (ForecastHelper.hasDealsWithStage(stage)) {
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
	protected void addColumns(Grid<ForecastStage> grid) {
		grid.addColumn(ForecastStage::getName).setHeader("Stufe");
		grid.addColumn(ForecastStage::getMinProb).setHeader("Mindestwahrscheinlichkeit (%)");
		grid.addColumn(ForecastStage::getMaxProb).setHeader("Höchstwahrscheinlichkeit (%)");
		grid.addColumn(ForecastStage::getPosition).setHeader("Position");
	}

	@Override
	protected String getItemName() {
		return "Stufe";
	}

	@Override
	protected Collection<ForecastStage> getAll() {
		return ForecastHelper.getAllStages();
	}

	@Override
	protected Class<ForecastStage> getGlazz() {
		return ForecastStage.class;
	}

}
