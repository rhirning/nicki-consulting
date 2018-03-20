package org.mgnl.nicki.consulting.data;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.util.AbstractBeanContainer;

@SuppressWarnings("serial")
public class BeanContainerDataSource<T> extends AbstractBeanContainer<T, T> implements Container.Filterable {

    public BeanContainerDataSource(Class<? super T> type) {
        super(type);
        super.setBeanIdResolver(new IdentityBeanIdResolver<T>());
    }
	private static class IdentityBeanIdResolver<BT> implements
            BeanIdResolver<BT, BT> {

        @Override
        public BT getIdForBean(BT bean) {
            return bean;
        }

    }
	@Override
	public void addAll(Collection<? extends T> collection) throws IllegalStateException, IllegalArgumentException {
		super.addAll(collection);
	}
	
}
