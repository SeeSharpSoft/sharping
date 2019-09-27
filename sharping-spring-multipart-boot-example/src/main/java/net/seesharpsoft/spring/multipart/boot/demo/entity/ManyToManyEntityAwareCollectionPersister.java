package net.seesharpsoft.spring.multipart.boot.demo.entity;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;

import javax.persistence.JoinTable;
import java.io.Serializable;
import java.lang.reflect.Field;

public class ManyToManyEntityAwareCollectionPersister extends BasicCollectionPersister {
    public ManyToManyEntityAwareCollectionPersister(Collection collectionBinding, CollectionDataAccess cacheAccessStrategy, PersisterCreationContext creationContext) throws MappingException, CacheException {
        super(collectionBinding, cacheAccessStrategy, creationContext);
    }

    private AbstractEntityPersister manyToManyRelationPersister;
    protected AbstractEntityPersister getManyToManyRelationPersister() {
        if (manyToManyRelationPersister == null) {
            int methodIndex = this.getName().lastIndexOf('.');
            try {
                Class<?> definedClass = Class.forName(this.getName().substring(0, methodIndex));
                Field field = definedClass.getDeclaredField(this.getName().substring(methodIndex + 1));
                JoinTable joinTable = field.getAnnotation(JoinTable.class);
                String targetName = joinTable.name();
                final String needle = '.' + targetName;
                String foundKey = this.getFactory().getAllClassMetadata().keySet().stream().filter(key -> key.endsWith(needle)).findFirst().orElse(null);
                if (foundKey == null) {
                    throw new ClassNotFoundException(targetName);
                }
                manyToManyRelationPersister = (AbstractEntityPersister)this.getFactory().getEntityPersister(foundKey);
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return manyToManyRelationPersister;
    }

    @Override
    protected boolean isRowInsertEnabled() {
        return false;
    }

    @Override
    protected boolean isRowDeleteEnabled() {
        return false;
    }

    @Override
    protected int doUpdateRows(Serializable id, PersistentCollection collection, SharedSessionContractImplementor session) throws HibernateException {
        return 0;
    }
}
