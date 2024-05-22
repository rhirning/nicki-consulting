package org.mgnl.nicki.consulting.members.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.GridExportHelper;
import org.mgnl.nicki.consulting.members.helper.MembersHelper;
import org.mgnl.nicki.consulting.members.model.Member;
import org.mgnl.nicki.consulting.views.EditParameterView;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.editor.templates.export.GridExport;
import org.mgnl.nicki.vaadin.base.helper.ConfirmHelper;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class MembersView extends EditParameterView<Member> implements View {

	public MembersView() {
		super();
	}

	protected void delete(Optional<Member> memberOptional ) {
		if (!memberOptional.isPresent()) {
			Notification.show("Bitte erst Person auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			Member member = memberOptional.get();
			ConfirmHelper.confirm("Mitglied löschen", member, m -> {
				try {
					MembersHelper.delete(m);
					init();
				} catch (NotSupportedException e) {
					Notification.show("Löschen wird nicht unterstützt", Type.HUMANIZED_MESSAGE);
				} catch (SQLException | InitProfileException e) {
					log.error("Error accessing db", e);
					Notification.show("Das Mitglied konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
				}
			});
		}
	}

	@Override
	protected void addColumns(Grid<Member> grid) {
//		grid.addColumn(Member::getDisplayName).setHeader("Mitglied").setSortable(true);
		GridExportHelper.addColumns(grid, Member.class);
	}

	@Override
	protected void addExportColumns(GridExport<Member> gridExport) {
		GridExportHelper.addExportColumns(gridExport, Member.class);
	}

	@Override
	protected String getItemName() {
		return "Mitglied";
	}

	@Override
	protected String getExportFilename() {
		return "Mitglieder";
	}

	@Override
	protected String getNewItemString() {
		return "Neues Mitglied";
	}

	@Override
	protected Collection<Member> getAll() {
		return MembersHelper.getAllMembers();
	}

	@Override
	protected String getDbContextName() {
		return "projects";
	}

	@Override
	protected Class<Member> getClazz() {
		return Member.class;
	}

	@Override
	protected Component getDetails(Member member) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		add(sb, member.getPersonalTitle());
		add(sb, member.getGivenname(), member.getSurname());
		add(sb, member.getStreet());
		add(sb, member.getPostCode(), member.getCity());
		add(sb, "Telefon", member.getPhone());
		add(sb, "Mobil", member.getMobile());
		add(sb, "Eintritt", member.getEntryDate());
		sb.append("</table>");
		return new Html(sb.toString());
	}

	private void add(StringBuilder sb, String value1, Date date) {
		if (date != null) {
			add(sb, value1, DataHelper.getDisplayDay(date));
		} else {
			add(sb, value1);			
		}
	}

	private void add(StringBuilder sb, String value1, String value2) {
		sb.append("<tr><td>");
		if (value1 != null) {
			sb.append(value1);
		}
		sb.append("</td><td>");
		if (value2 != null) {
			sb.append(value2);
		}
		sb.append("</td></tr>");		
	}

	private void add(StringBuilder sb, String value) {
		if (value != null) {
			sb.append("<tr><td>").append(value).append("</td>");
		}
	}
}
