package org.mgnl.nicki.consulting.forecast.views;

import java.time.LocalDate;
import java.util.stream.IntStream;

import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SuppressWarnings("serial")
public class ForecastGraphDemoView extends VerticalLayout implements View {
	
	
	private boolean isInit;
	


	public ForecastGraphDemoView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
	        ApexCharts areaChart = ApexChartsBuilder.get()
	                .withChart(ChartBuilder.get()
	                        .withType(Type.line)
	                        .withZoom(ZoomBuilder.get()
	                                .withEnabled(false)
	                                .build())
	                        .build())
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
	        areaChart.setSizeFull();
	        add(areaChart);
	        setWidth("100%");

			
			isInit = true;
		}
		

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
