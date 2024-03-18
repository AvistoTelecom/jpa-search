package com.avisto.jpasearch.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TestNested {
    @Id
    private Long id;
    private Integer nestedField;
}
