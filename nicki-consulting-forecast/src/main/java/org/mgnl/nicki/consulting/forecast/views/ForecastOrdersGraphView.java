package org.mgnl.nicki.consulting.forecast.views;

import org.mgnl.nicki.consulting.forecast.helper.ForecastDealCollector;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SuppressWarnings("serial")
public class ForecastOrdersGraphView extends VerticalLayout implements View {

	private HorizontalLayout periodLayout;
	private VerticalLayout chartLayout;
	private DatePicker periodStartDatePicker;
	private DatePicker periodEndDatePicker;
	private ApexCharts areaChart;
	
	
	private boolean isInit;
	


	public ForecastOrdersGraphView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {

			periodStartDatePicker.addValueChangeListener(event -> redraw());
			periodEndDatePicker.addValueChangeListener(event -> redraw());
			
	        setWidth("100%");
			
			isInit = true;
		}
		
		periodStartDatePicker.setValue(DataHelper.getLocalDate(Period.getFirstDayOfMonth().getTime()));
		periodEndDatePicker.setValue(DataHelper.getLocalDate(Period.getLastDayOfMonth().getTime()));

		redraw();
		

	}
	
	private void redraw() {
		ForecastDealGraphWrapper dealGraphWrapper = ForecastDealCollector.get()
		.withStart(periodStartDatePicker.getValue())
		.withEnd(periodEndDatePicker.getValue())
		.withOrdersOnly(true)
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
		
		periodLayout.add(periodStartDatePicker, periodEndDatePicker);

		
		chartLayout = new VerticalLayout();
		chartLayout.setHeightFull();
		chartLayout.setMargin(false);
		chartLayout.setPadding(false);
		add(periodLayout, chartLayout);
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
