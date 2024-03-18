package com.avisto.jpasearch.model;

import com.avisto.jpasearch.SearchableEntity;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class TestEntity implements SearchableEntity {

    @Id
    private Long id;

    private String field1;

    private Integer field2;

    @OneToOne
    private TestNested nestedEntity;

    @OneToMany
    private List<TestNested> nestedList;

    public TestEntity(TestEntity entity) {
        this.id = entity.id;
        this.field1 = entity.field1;
        this.field2 = entity.field2;
        this.nestedEntity = entity.nestedEntity;
        this.nestedList = entity.nestedList;
    }

    public TestEntity() {

    }

    public static class TestEntityInList extends TestEntity {

        public TestEntityInList(TestEntity entity) {
            super(entity);
        }
    }
}
