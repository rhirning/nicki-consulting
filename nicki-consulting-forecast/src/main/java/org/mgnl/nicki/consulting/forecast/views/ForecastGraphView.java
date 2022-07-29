package org.mgnl.nicki.consulting.forecast.views;

import java.util.Date;

import org.mgnl.nicki.consulting.forecast.helper.ForecastDealCollector;
import org.mgnl.nicki.consulting.forecast.helper.ForecastDealCollector.UNIT;
import org.mgnl.nicki.consulting.forecast.helper.ForecastDealGraphWrapper;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SuppressWarnings("serial")
public class ForecastGraphView extends VerticalLayout implements View {

	private HorizontalLayout periodLayout;
	private HorizontalLayout filterLayout;
	private VerticalLayout chartLayout;
	private DatePicker periodStartDatePicker;
	private DatePicker periodEndDatePicker;
	private ComboBox<UNIT> unitComboBox;
	private Checkbox ignoreProbabilityCheckbox;
	private DatePicker timeDatePicker;
	private ApexCharts areaChart;
	
	
	private boolean isInit;
	


	public ForecastGraphView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			periodStartDatePicker.setValue(DataHelper.getLocalDate(Period.getFirstDayOfMonth().getTime()));
			periodEndDatePicker.setValue(DataHelper.getLocalDate(Period.getLastDayOfMonth().getTime()));
			timeDatePicker.setValue(DataHelper.getLocalDate(new Date()));
			unitComboBox.setValue(UNIT.Personen);

			periodStartDatePicker.addValueChangeListener(event -> redraw());
			periodEndDatePicker.addValueChangeListener(event -> redraw());
			timeDatePicker.addValueChangeListener(event -> redraw());
			unitComboBox.addValueChangeListener(event -> redraw());
			ignoreProbabilityCheckbox.addValueChangeListener(event -> redraw());
			

	        setWidth("100%");

	        redraw();
			
			isInit = true;
		}
		

	}
	
	private void redraw() {
		ForecastDealGraphWrapper dealGraphWrapper = ForecastDealCollector.get()
		.withStart(periodStartDatePicker.getValue())
		.withEnd(periodEndDatePicker.getValue())
		.withUnit(unitComboBox.getValue())
		.withIgnoreProbability(ignoreProbabilityCheckbox.getValue())
		.withTime(timeDatePicker.getValue())
		.getDealGraphWrapper();
		
		if (areaChart != null) {
			chartLayout.remove(areaChart);
		}
		
        areaChart = ApexChartsBuilder.get()
        		.withSeries(dealGraphWrapper.getSeries())
        		.withLabels(dealGraphWrapper.getDates().stream().map(ld -> ld.toString()).toArray(String[]::new))
                .withChart(ChartBuilder.get()
                        .withType(Type.bar)
                        .withStacked(true)
                        .withToolbar(ToolbarBuilder.get().withShow(true).build())
                        .withZoom(ZoomBuilder.get()
                                .withEnabled(true)
                                .build())
                        .build())
                .build();
        /*
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.smooth).build())
                .withSeries(new Series<>("STOCK ABC", 10.0, 41.0, 35.0, 51.0, 49.0, 62.0, 69.0, 91.0, 148.0))
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Fundamental Analysis of Stocks")
                        .withAlign(Align.left).build())
                .withSubtitle(TitleSubtitleBuilder.get()
                        .withText("Price Movements")
                        .withAlign(Align.left).build())
                .withLabels(IntStream.range(1, 10).boxed().map(day -> LocalDate.of(2000, 1, day).toString()).toArray(String[]::new))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(true).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
                */
        areaChart.setSizeFull();
        chartLayout.add(areaChart);


	}

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		setPadding(false);
		
		periodLayout = new HorizontalLayout();
		periodLayout.setPadding(false);
		periodStartDatePicker = new DatePicker("Start");
		periodEndDatePicker = new DatePicker("Ende");
		timeDatePicker = new DatePicker("Zeitpunkt");
		unitComboBox = new ComboBox<>("Anzeige", UNIT.values());
		
		periodLayout.add(periodStartDatePicker, periodEndDatePicker, unitComboBox, timeDatePicker);
		
		filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		ignoreProbabilityCheckbox = new Checkbox("ohne Wahrscheinlichkeit");
		filterLayout.add(ignoreProbabilityCheckbox);
		
		chartLayout = new VerticalLayout();
		chartLayout.setHeightFull();
		chartLayout.setMargin(false);
		chartLayout.setPadding(false);
		add(periodLayout, filterLayout, chartLayout);
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
