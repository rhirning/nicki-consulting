package org.mgnl.nicki.consulting.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SaveOrIgnoreDialog extends VerticalLayout {
	private static final long serialVersionUID = 3551076231334629551L;
	
	private HorizontalLayout buttonsLayout;
	
	private Button ignoreButton;
	
	private Button saveButton;
	
	private Label introLabel;
	
	public enum DECISION {SAVE, IGNORE};

	public SaveOrIgnoreDialog(SaveOrIgnoreHandler handler) {
		buildMainLayout();
		
		saveButton.addClickListener(event -> {handler.handle(DECISION.SAVE);});
		ignoreButton.addClickListener(event -> {handler.handle(DECISION.IGNORE);});
	}

	
	private void buildMainLayout() {
		setWidth("-1px");
		setHeight("-1px");
		setMargin(true);
		setSpacing(true);
		
		// introLabel
		introLabel = new Label();
		introLabel.setWidth("-1px");
		introLabel.setHeight("-1px");
		introLabel.setText("Die Daten wurden verändert");
		add(introLabel);
		
		// horizontalLayout_1
		buttonsLayout = buildHorizontalLayout_1();
		add(buttonsLayout);
	}

	
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("-1px");
		buttonsLayout.setHeight("-1px");
		buttonsLayout.setMargin(false);
		buttonsLayout.setSpacing(true);
		
		// saveButton
		saveButton = new Button();
		saveButton.setText("Speichern");
		saveButton.setWidth("-1px");
		saveButton.setHeight("-1px");
		buttonsLayout.add(saveButton);
		
		// ignoreButton
		ignoreButton = new Button();
		ignoreButton.setText("Verwerfen");
		ignoreButton.setWidth("-1px");
		ignoreButton.setHeight("-1px");
		buttonsLayout.add(ignoreButton);
		
		return buttonsLayout;
	}
	
}
