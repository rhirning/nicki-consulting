package org.mgnl.nicki.consulting.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mgnl.nicki.core.util.Classes;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.data.DataType;
import org.mgnl.nicki.db.helper.BeanHelper;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.editor.templates.export.GridExport;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public abstract class EditParameterView<T> extends VerticalLayout implements View {
		
	private Grid<T> grid;
	private GridExport<T> gridExport;
	private Anchor exportAnchor;
	private Button importButton;

	private Upload upload;
	private MemoryBuffer memoryBuffer;
	
	private VerticalLayout gridCanvas;
	private HorizontalLayout buttonsLayout;
	private Button newButton;
	private boolean isInit;
	
	private Collection<T> items;

	private DialogBase editDialog;
	private DialogBase importDialog;
	private @Setter @Getter boolean admin;
	private @Setter @Getter IMPORT importType = IMPORT.CSV;
	
	public enum IMPORT {XLSX, CSV}


	public EditParameterView() {
	}

	public void init() {
		if (!isInit) {
			buildMainLayout();

			addColumns(grid);
			grid.setItemDetailsRenderer(new ComponentRenderer<>(d -> getDetailsComponent(d)));
			gridExport = new GridExport<T>();
			addExportColumns(gridExport);
			if (isAdmin()) {
				importButton.addClickListener(e -> showImport());
			}
			newButton.addClickListener(event -> showEditView(getClazz(), Optional.empty(), getNewItemString(), getItemName() + " bearbeiten"));
			
			isInit = true;
		}
		
		items = getAll();
		grid.setItems(items);
		exportAnchor.setHref(gridExport.getXlsStreamResource(getExportFilename(), items));
	}

	protected abstract String getExportFilename();

	private VerticalLayout getDetailsComponent(T data) {
		VerticalLayout layout = new VerticalLayout();
		layout.add(getDetails(data));
		HorizontalLayout buttons = new HorizontalLayout();
		Button editButton = new Button("Bearbeiten");
		editButton.addClickListener(event -> showEditView(getClazz(), Optional.of(data), getNewItemString(), getItemName() + " bearbeiten"));
		Button deleteButton = new Button("Löschen");
		deleteButton.addClickListener(e -> delete(Optional.of(data)));
		buttons.add(editButton, deleteButton);
		layout.add(buttons);
		return layout;
	}

	protected abstract Component getDetails(T data);

	private void showImport() {
		memoryBuffer = new MemoryBuffer();
		upload = new Upload(memoryBuffer);
		upload.addSucceededListener(event -> {
		    InputStream fileData = memoryBuffer.getInputStream();
		    try {
		    	if (importType == IMPORT.XLSX) {
					XSSFWorkbook wb = readFile(fileData.readAllBytes());
					importExcel(wb);
		    	} else {
		    		importCsv(fileData);
		    	}
				importDialog.close();
				init();
			} catch (InstantiationException | IllegalAccessException | IOException | SQLException | InitProfileException | NotSupportedException e) {
				Notification.show("Fehler beim Import: " + e.getMessage());
			}
		});
		importDialog = new DialogBase("Import", upload);
		importDialog.setModal(true);
		importDialog.open();
	}


	private void importCsv(InputStream input) throws SQLException, InitProfileException, NotSupportedException {
		List<String> lines = IOUtils.readLines(input, Charset.defaultCharset());
		List<T> beans = BeanHelper.importCsv(getClazz(), lines, ";");
		try (DBContext dbContext = DBContextManager.getContext(getDbContextName())) {
			for (T bean : beans) {
				dbContext.create(bean);
			}
		}
	}

	protected abstract Collection<T> getAll();
	protected abstract Class<T> getClazz();

	protected abstract void delete(Optional<T> of);

	protected abstract String getItemName();
	protected abstract String getNewItemString();

	protected abstract void addColumns(Grid<T> grid);
	protected abstract void addExportColumns(GridExport<T> gridExport);
	
	public void importExcel(XSSFWorkbook wb) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException, NotSupportedException {

		try (DBContext dbContext = DBContextManager.getContext(getDbContextName())) {
			for (int k = 0; k < wb.getNumberOfSheets(); k++) {
				XSSFSheet sheet = wb.getSheetAt(k);
				List<T> beans = importSheet(sheet);
				for (T bean : beans) {
					dbContext.create(bean);
				}
			}
		}
	}


	public List<T> importSheet(XSSFSheet sheet) throws InstantiationException, IllegalAccessException {
		XSSFRow row = sheet.getRow(0);
		List<T> beans = new ArrayList<T>();
		int numberColumns = row.getLastCellNum();
		int numberRows = sheet.getLastRowNum();
		String[] titles = new String[numberColumns];		
		DataType[] types = new DataType[numberColumns];
		for (int i = 0; i < numberColumns; i++) {
			String title = row.getCell(i).getStringCellValue();
				try {
					Field field = getClazz().getDeclaredField(title);
					titles[i] = title;
					types[i] = DataType.getTypeOfField(field);
				} catch (NoSuchFieldException | SecurityException e) {
					log.error("Invalid field: " + title);
				}
		}
		
		for (int rowNum = 1 ; rowNum < numberRows; rowNum++) {
			T bean = Classes.newInstance(getClazz());
			row = sheet.getRow(rowNum);
			if (row != null) {
				for (int i = 0; i < numberColumns; i++) {
					if (titles[i] != null) {
						Cell cell = row.getCell(i);
						if (cell != null) {
							String cellValue = getStringValue(cell);
							BeanHelper.setValue(bean, titles[i], types[i].getValue(cellValue));
						}
					}
				}
			}
			beans.add(bean);
		}

		return beans;
	}

	private String getStringValue(Cell cell) {
		if (cell.getCellType() == CellType.BOOLEAN) {
			return Boolean.toString(cell.getBooleanCellValue());
		} else if (cell.getCellType() == CellType.NUMERIC) {
			return Double.toString(cell.getNumericCellValue());
		} else if (cell.getCellType() == CellType.STRING) {
			return cell.getStringCellValue();
		}
		return null;
	}

	/**
	 * creates an {@link XSSFWorkbook} with the specified OS filename.
	 */
	public static XSSFWorkbook readFile(byte[] bytes) throws IOException {
	    InputStream fis = new ByteArrayInputStream(bytes);
	    try {
			return new XSSFWorkbook(fis);		// NOSONAR - should not be closed here
	    } finally {
	        fis.close();
	    }
	}

	private void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption, Object... foreignObjects) {
		DbBeanCloseListener closeListener = new DbBeanCloseListener() {			
			@Override
			public void close(Component component) {
				editDialog.close();
				init();
			}
		};
		DbBeanViewer beanViewer = new DbBeanViewer(closeListener);
		beanViewer.setDbContextName(getDbContextName());
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
		editDialog = new DialogBase(windowTitle, beanViewer);
		editDialog.setModal(true);
		editDialog.setHeightFull();
		editDialog.open();
	}
	
	protected abstract String getDbContextName();

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		
		gridCanvas = new VerticalLayout();
		gridCanvas.setSizeFull();
		gridCanvas.setMargin(false);
		gridCanvas.setSpacing(true);
		
		
		buttonsLayout = new HorizontalLayout();
		gridCanvas.add(buttonsLayout);

		grid = new Grid<>();
		grid.setSizeFull();
		gridCanvas.add(grid);
		gridCanvas.setFlexGrow(1, grid);
		
		newButton = new Button();
		newButton.setText(getNewItemString());
		newButton.setWidth("-1px");
		newButton.setHeight("-1px");
		
		exportAnchor = new Anchor();
		exportAnchor.setText("Export");
		exportAnchor.setWidth("-1px");
		exportAnchor.setHeight("-1px");
		
		importButton = new Button();
		importButton.setText("Import");
		importButton.setWidth("-1px");
		importButton.setHeight("-1px");
		if (isAdmin()) {
			buttonsLayout.add(newButton, importButton, exportAnchor);
		} else {
			buttonsLayout.add(newButton, exportAnchor);
		}
		buttonsLayout.setDefaultVerticalComponentAlignment(Alignment.END);
						
		add(gridCanvas);

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
