package org.mgnl.nicki.consulting.views;

import java.util.List;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class MyProjectsView extends BaseView implements View {

	private static final long serialVersionUID = -517846258408073414L;
	
	private HorizontalLayout dataLayout;
	
	private HorizontalLayout buttonsLayout;
	
	private Select<PERIOD> timeComboBox;
	
	private Select<Member> memberComboBox;
	
	private boolean isInit;

	private VerticalLayout verticalLayout;

	public MyProjectsView() {
		buildMainLayout();

	}

	@Override
	public void init() {
		if (!isInit) {
			initTimeComboBox(this.timeComboBox);
		}
		try {
			loadMember();
		} catch (NoValidPersonException | NoApplicationContextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!isInit) {
			memberComboBox.addValueChangeListener(event -> {
				Member m = event.getValue();
				showMember(m);
			});
			//memberComboBox.setReadOnly(true);
			timeComboBox.addValueChangeListener(event -> {try {
				loadMember();
			} catch (NoValidPersonException | NoApplicationContextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}});
			isInit = true;
		}
	}

	private void showMember(Member member) {
		dataLayout.removeAll();
		if (member != null) {
			Project project = TimeHelper.getProject(member.getProjectId());
			Customer customer = TimeHelper.getCustomer(project.getCustomerId());
			dataLayout.add(getDbBeanViewer(customer));
			addComponent(dataLayout, TimeHelper.getHoursForProject(project), project, "rate");
			addComponent(dataLayout, TimeHelper.getHoursForMember(member), member, "rate");
		}
	}
	
	

	private void addComponent(HorizontalLayout dataLayout, float hours, Object bean, String... hiddenAttributes) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		StringBuilder sb =  new StringBuilder();
		sb.append("gebucht: ");
		sb.append(TimeHelper.formatHours(hours));
		sb.append(" Stunden - ");
		sb.append(TimeHelper.formatHours(hours/8));
		sb.append(" Tage");
		layout.add(new Label(sb.toString()));
		layout.add(getDbBeanViewer(bean, hiddenAttributes));
		dataLayout.add(layout);
	}

	private DbBeanViewer getDbBeanViewer(Object bean, String...hiddenAttributes) {

		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
			}
		});
		beanViewer.setReadOnly(true);
		beanViewer.setDbContextName("projects");
		beanViewer.setWidth("400px");
		beanViewer.setDbBean(bean, hiddenAttributes);
		return beanViewer;
	}

	private void loadMember() throws NoValidPersonException, NoApplicationContextException {
		List<Member> members = getMembers(getPerson(), ((PERIOD) timeComboBox.getValue()).getPeriod());
		memberComboBox.setItems(members);
		memberComboBox.setItemLabelGenerator(Member::getDisplayName);
	}

	
	private void buildMainLayout() {
		setWidth("100%");
		setHeight("100%");

		verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setPadding(false);
		
		add(verticalLayout);
		
		// buttonsLayout
		buttonsLayout = buildButtonsLayout();
		verticalLayout.add(buttonsLayout);
		
		// dataLayout
		dataLayout = new HorizontalLayout();
		dataLayout.setWidth("-1px");
		dataLayout.setHeight("-1px");
		dataLayout.setMargin(false);
		dataLayout.setSpacing(true);
		dataLayout.setPadding(false);
		verticalLayout.add(dataLayout);
		verticalLayout.setFlexGrow(1, dataLayout);
	}

	
	private HorizontalLayout buildButtonsLayout() {
		// common part: create layout
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("-1px");
		buttonsLayout.setHeight("-1px");
		buttonsLayout.setMargin(true);
		buttonsLayout.setSpacing(true);
		
		// timeComboBox
		timeComboBox = new Select<PERIOD>();
		timeComboBox.setLabel("Zeitraum");
		timeComboBox.setWidth("-1px");
		timeComboBox.setHeight("-1px");
		buttonsLayout.add(timeComboBox);
		
		// memberComboBox
		memberComboBox = new Select<Member>();
		memberComboBox.setLabel("Projekt");
		memberComboBox.setWidth("-1px");
		memberComboBox.setHeight("-1px");
		buttonsLayout.add(memberComboBox);
		
		return buttonsLayout;
	}

}
