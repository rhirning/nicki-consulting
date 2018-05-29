package org.mgnl.nicki.consulting.data;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.model.Member;

import com.vaadin.data.Container.Sortable;
import com.vaadin.data.util.ItemSorter;

public class TimeWrapperItemSorter implements ItemSorter {
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
				return timeWrapper.getCustomerReport().getValue();
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
	PROPERTY property;
	String propertyId;
	int compFactor = 1;
	
	@Override
	public void setSortProperties(Sortable container, Object[] propertyIds, boolean[] ascendings) {
		if (propertyIds != null && propertyIds.length > 0) {
			this.propertyId = (String) propertyIds[0];
			property = PROPERTY.get(propertyId);
			this.compFactor = ascendings[0]?1:-1;
		}
	}

	@Override
	public int compare(Object itemId1, Object itemId2) {
		TimeWrapper timeWrapper1 = (TimeWrapper) itemId1;
		TimeWrapper timeWrapper2 = (TimeWrapper) itemId2;
		Object value1 = property.getValue(timeWrapper1);
		Object value2 = property.getValue(timeWrapper2);
		if (value1 != null && value2 != null) {
			return this.compFactor * ((Comparable) value1).compareTo(value2); 
		} else if (value1 != null) {
			return 1 * this.compFactor;
		} else if (value2 != null) {
			return -1 * this.compFactor;
		} else {
			return 0;
		}
	}

}
