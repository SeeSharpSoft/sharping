package net.seesharpsoft.spring.multipart.boot.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;

@Entity
@IdClass(ArticleTags.CompoundId.class)
public class ArticleTags {

    public static class CompoundId implements Serializable {

        private Long articlId;
        private Long tagId;
    }

    @Id
    private Long articlId;

    @Id
    private Long tagId;

    private boolean deleted;
}
