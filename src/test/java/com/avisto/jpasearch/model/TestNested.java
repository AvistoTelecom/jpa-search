package com.avisto.jpasearch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TestNested {
    @Id
    private Long id;
    private Integer nestedField;
}
