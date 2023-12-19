package com.avisto.genericspringsearch.config;

import com.avisto.genericspringsearch.SearchableEntity;

public sealed interface ISearchConfig<R extends SearchableEntity> permits IFilterConfig, ISorterConfig {
    String getKey();

    boolean needMultipleValues();
}
