package org.mgnl.nicki.consulting.data;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.model.Member;

import com.vaadin.ui.CheckBox;

public class TimeWrapperItemSorter implements Serializable {
	enum PROPERTY {
		PERSON("person") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return timeWrapper.getPerson().getValue();
			}
		},
		DELETE("delete") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return timeWrapper.getDelete().getValue();
			}
		},
		MEMBER("member") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				Member member = (Member) timeWrapper.getMember().getValue();
				if (member != null) {
					return member.getDisplayName();
				} else {
					return null;
				}
			}
		},
		DAY("day") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return timeWrapper.getDay().getValue();
			}
		},
		START("start") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return StringUtils.stripToNull(timeWrapper.getStart().getValue());
			}
		},
		END("end") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return StringUtils.stripToNull(timeWrapper.getEnd().getValue());
			}
		},
		PAUSE("pause") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return timeWrapper.getPause().getValue();
			}
		},
		HOURS("hours") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return StringUtils.stripToNull(timeWrapper.getHours());
			}
		},
		CUSTOMER_REPORT("customerReport") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				if (timeWrapper.getCustomerReport() instanceof CheckBox) {
					return ((CheckBox) timeWrapper.getCustomerReport()).getValue();
				} else {
					return false;
				}
			}
		},
		TEXT("text") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return StringUtils.stripToNull(timeWrapper.getText().getValue());
			}
		}, UNKNOWN("unknown") {
			@Override
			Object getValue(TimeWrapper timeWrapper) {
				return "";
			}
		};
		
		String property;
		PROPERTY(String property) {
			this.property= property;
		}
		
		abstract Object getValue(TimeWrapper timeWrapper);
		
		static PROPERTY get(String property) {
			for (PROPERTY p : PROPERTY.values()) {
				if (StringUtils.equalsIgnoreCase(p.property, property)) {
					return p;
				}
			}
			return PROPERTY.UNKNOWN;
		}
		

	}

	private static final long serialVersionUID = 338272978626155074L;

}
