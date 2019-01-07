package net.seesharpsoft.spring.multipart.boot.demo.util;

import net.seesharpsoft.spring.multipart.boot.demo.entity.ManyToManyEntityAwareCollectionPersister;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.internal.StandardPersisterClassResolver;

public class CustomPersisterClassResolver extends StandardPersisterClassResolver {

    public Class<? extends CollectionPersister> getCollectionPersisterClass(Collection metadata) {
        if (!metadata.isOneToMany()) {
            return ManyToManyEntityAwareCollectionPersister.class;
        }
        return super.getCollectionPersisterClass(metadata);
    }

}
