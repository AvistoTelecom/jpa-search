<img src="assets/logo_avisto.png" alt="Logo Avisto" width="270" height="173" />

# JPA-Search

JPA Search offers a solution that facilitates the search of database content in Java projects.

[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white
)](https://hibernate.org/)

***
## Description 🔍

Jpa-Search simplifies specific entity searches with JPA, including :
- Modular filters
- Multiple filters
- Modular sorts
- Multiple sorts (following each other)
- Various operations (you can add yours)
- Complete pagination


This search is designed to be efficient, since it operates at a low level, as close as possible to SQL, thanks to the CriteriaBuilder provided by javax.persistence.

A search works with a configuration file where you define each possible filter, its associated operation (e.g.: name equals value) and where to fetch the field in DB. Once a filter has been defined, it is possible to filter by this ascending or descending field.

The search function takes as input a list of query params in the form of a `Map<String, String>` (also containing a `page` and `size` field for pagination if it's not a key filter), a `List<String>` sort list, the type of entity searched for `Class<T>` and the type of search configuration enumeration `Class<E>`. In return, this function returns a `Page<T>` containing the number of elements requested and the number of total elements.

### How to use it ? 🤔 <a name="how-to-use-it"></a>

To configure a search, you need to create a configuration criteria `enum` described like below:

```java
@Getter
@AllArgsConstructor
public enum ApiKeyCriteria implements ISearchCriteriaConfig<Apikey> {
    ID(sorterConfig.of("id", ObjectFilterOperation.EQUAL, "id")),
    ACCOUNT_TYPE(FilterConfig.of("accountType", ObjectFilterOperation.EQUAL, "account.accountType.key")),
    PROFILE_TYPE(FilterSorterConfig.of("profileType", ObjectFilterOperation.EQUAL, "account.accountType.profile.key")),
    ACCOUNT_FILES_NAME(FilterConfig.of("accountFilesName", ObjectFilterOperation.EQUAL, "account.files[name]")),
    ACCOUNT_LABEL(FilterConfig.of("accountLabel", ObjectFilterOperation.EQUAL, "account.label")),
    TYPE(FilterConfig.of("type", ObjectFilterOperation.EQUAL, "type")),
    EXTERNAL_ID(FilterSorterConfig.of("externalId", StringFilterOperation.CONTAIN_IGNORE_CASE, "externalId")),
    CREATION_DATE(FilterConfig.of("creationDate", ListComparableFilterOperation.BETWEEN, "creationDate")),
    END_DATE(FilterSorterConfig.of("endDate", ListComparableFilterOperation.BETWEEN, "endDate")),
    EXPIRATION_DATE(FilterConfig.of("expirationDate", ListComparableFilterOperation.BETWEEN, "expirationDate")),
    LABEL(FilterConfig.of("label", StringFilterOperation.CONTAIN_IGNORE_CASE_IGNORE_ACCENT, "label", "account.label")),
    YEAR(GroupFilterConfig.of("year", CREATION_DATE.getFilterConfig(), END_DATE.getFilterConfig())),
    MULTI_ACCOUNT_LABEL(MultiFilterConfig.of("multiAccountLabel", ACCOUNT_LABEL.getFilterConfig(), "account"));

    final ISearchConfig<ApiKey> searchConfig;

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria(ID.getKey(), SortDirection.ASC);
    }
  
    @Override
    public Class<ApiKey> getRootClass() {
        return ApiKey.class;
    }

}
```

The idea here is to associate in `FilterConfig.of(...)` an operation, a query param name and an entity field name.

Taking `ACCOUNT_TYPE` as an example, we're looking for an `ApiKey` that is associated with a particular account type, in this case equality.

#### FilterOperation

The `FilterOperation` is used to describe the operation to be performed on the filter, many of which are already available and applicable to different types:

<details>
  <summary>ObjectFilterOperation</summary>

| Operator Name | Description                                      | Filter Type |
|---------------|--------------------------------------------------|-------------|
| `EQUAL`       | Checks for equality between `field` and `filter` | `Object`    |

</details>

<details>
  <summary>StringFilterOperation</summary>

| Operator Name                          | Description                                                                | Filter Type |
|----------------------------------------|----------------------------------------------------------------------------|-------------|
| `CONTAIN`                                 | Checks that `field` contains part of the `filter`                          | `String`    |
| `CONTAIN_IGNORE_CASE`                     | Checks that the `field` contains part of the `filter`, case ignored        | `String`    |
| `CONTAIN_IGNORE_CASE_IGNORE_ACCENT`       | Checks for equality between `field` and `filter`, case and accents ignored | `String`    |
| `START_WITH`                           | Checks that the `field` begins with the `filter`                           | `String`    |
| `START_WITH_IGNORE_CASE`               | Check that the `field` begins with the `filter`, case ignored              | `String`    |
| `START_WITH_IGNORE_CASE_IGNORE_ACCENT` | Check that the `field` starts with the `filter`, case and accents ignored  | `String`    |
| `EQUAL_IGNORE_CASE`                    | Checks for equality between `field` and `filter`, case                     | `String`    |
| `EQUAL_IGNORE_CASE_IGNORE_ACCENT`      | Checks for equality between `field` and `filter`, case and accents ignored | `String`    |

</details>

<details>
  <summary>ListComparableFilterOperation</summary>

| Operator Name | Description                                            | Filter Type     |
|---------------|--------------------------------------------------------|-----------------|
| `BETWEEN`     | Checks that `field` is between the two `filter` fields | `Comparable[2]` |

</details>

<details>
  <summary>ComparableFilterOperation</summary>

| Operator Name                   | Description                                                      | Filter Type  |
|---------------------------------|------------------------------------------------------------------|--------------|
| `GREATER_THAN_OR_EQUAL`         | Checks that `field` is greater than or equal to `filter`         | `Comparable` |
| `GREATER_THAN_OR_EQUAL_OR_NULL` | Checks that `field` is greater than or equal to `filter` or null | `Comparable` |
| `LESS_THAN_OR_EQUAL`            | Checks that `field` is smaller than or equal to `filter`         | `Comparable` |

</details>

<details>
  <summary>ListObjectFilterOperation</summary>

| Operator Name | Description                                                                | Filter Type |
|---------------|----------------------------------------------------------------------------|-------------|
| `IN_EQUAL`    | Checks the equality of `field` with one of the `fields` in the filter list | `Object[]`  |

</details>

<details>
  <summary>ListStringFilterOperation</summary>

| Operator Name                        | Description                                                                                      | Filter Type |
|--------------------------------------|--------------------------------------------------------------------------------------------------|-------------|
| `IN_CONTAIN`                            | Checks that `field` contains one of the fields in the `filter` list                              | `String[]`  |
| `IN_EQUAL_IGNORE_CASE_IGNORE_ACCENT` | Checks that `field` is equal to one of the fields in the `filter` list, case and accents ignored | `String[]`  |

</details>

<details>
  <summary>VoidFilterOperation</summary>

| Operator Name         | Description                | Filter Type |
|-----------------------|----------------------------|-------------|
| `NOT_NULL`            | Checks for `field` nullity | `Void`      |
| `NULL`                | Checks for `field` nullity | `Void`      |
| `COLLECTION_IS_EMPTY` | Checks for `field` nullity | `Void`      |

</details>

You can easily create an other `enum` which inherits from `IFilterOperation`, like :

<details>
  <summary>CustomFilterOperation</summary>

This example shows you how to create an operation filter that can be reused.

Example:
```java
public enum CustomFilterOperation implements IFilterOperation<Object> {
    NOT_EQUAL {
        public Predicate calculate(CriteriaBuilder cb, Expression<?> expression, Object value) {
            return value == null ? cb.isNotNull(expression) : cb.notEqual(expression, value);
        }
    };

    private CustomFilterOperation() {
    }

    public boolean needsMultipleValues() {
        return false;
    }

    public Class<Object> getOperationType() {
        return Object.class;
    }
}
```

</details>

#### Config

<details>
  <summary>FilterConfig</summary>

Configures the filter in relation to a field.

Parameters:

| Key                | Filter                        | pathFirst                                                  | paths (Optional)                                            |
|--------------------|-------------------------------|------------------------------------------------------------|-------------------------------------------------------------|
| Name of the filter | Filter that you want to apply | First path to the field where you want to apply the filter | Same function as for the path, but for the remaining fields |

Example:
```java
SEARCH(FilterConfig.of("search", StringFilterOperation.CONTAIN_IGNORE_CASE, "firstName", "lastName"));
```

</details>

<details>
  <summary>SorterConfig</summary>

You can add additional sorter like `id` in the example to sort your entities.
In this case if we set `sorts = {id,asc}`, the response will be the sort by id in ascending order.

Parameters:

| Key                | path                                                 | 
|--------------------|------------------------------------------------------|
| Name of the sorter | Path to the field where you want to apply the sorter |

Example:
```java
ID(FilterSorterConfig.of("id", ObjectFilterOperation.EQUAL, "id"));
```

</details>

<details>
  <summary>FilterSorterConfig</summary>

This filter groups FilterConfig and SorterConfig. So you can use this config like a sort or like a filter.

Parameters:

| Key                | Filter                        | pathFirst                                                  | paths (Optional)                                            |
|--------------------|-------------------------------|------------------------------------------------------------|-------------------------------------------------------------|
| Name of the sorter | Filter that you want to apply | First path to the field where you want to apply the filter | Same function as for the path, but for the remaining fields |

Example:
```java
ID(FilterSorterConfig.of("id", ObjectFilterOperation.EQUAL, "id"));
```

</details>

<details>
  <summary>GroupFilterConfig</summary>

The purpose of this filter is to group some FilterConfig.

Parameters:

| Key               | Filter                        | Filters (Optional) |
|-------------------|-------------------------------|--------------------|
| Name of the Group | Filter that you want to apply | Other filters      |

Example:
```java
PEOPLE(GroupFilterConfig.of("searchpeople", FIRSTNAME.getFilterConfig(), LASTNAME.getFilterConfig()));
```

</details>

<details>
  <summary>MultiFilterConfig</summary>

To understand this filter, let's take the example of a company: Avisto. Avisto has a `List<Employee>`.
 And if you want to get the employees whose names start with "M" or "N", you can use this filter to apply the name filter twice.

Parameters:

| Key                    | Filter                        | joinPath                                             |
|------------------------|-------------------------------|------------------------------------------------------|
| Name of the Multiplier | Filter that you want to apply | path to the field where you want to apply the filter |

Example:
```java
PEOPLE(GroupFilterConfig.of("searchpeople", FIRSTNAME.getFilterConfig(), LASTNAME.getFilterConfig()));
```

Parameters example:
`searchpeople="M","N"`

</details>

The library also supports EntityGraphs, so you can use the search function with an EntityGraph.

Example:
```java
Page<EntityInList> entityInList = searchCriteriaRepository.search(EntityCriteria.class, params, sorts, EntityInList::new, "NameOfTheEntityGraph");
```

<details>
  <summary>Test</summary>

If you want to test your ISearchCriteriaConfig, you can create tests like this:

```java
@Test
<R extends SearchableEntity, E extends Enum<E> & ISearchCriteriaConfig<R>> void ValidateCriteriaConfigurationsTest() {
    Reflections reflections = new Reflections("com.jpasearchimplem");
    Set<Class<? extends ISearchCriteriaConfig>> classes = reflections.getSubTypesOf(ISearchCriteriaConfig.class);
    classes.forEach(clazz -> {
        System.out.println("Checking for criteria : " + clazz.getSimpleName());
        assertTrue(clazz.isEnum());
        try {
            SearchUtils.checkCriteriaConfig((Class<E>) clazz);
        } catch (JpaSearchException e) {
            fail(e.getMessage());
        }
    });
}
```

</details>

### Benchmark 📈

If you've ever done research with CriteriaBuilder, you probably know that it's very tedious and time-consuming.

<details>
  <summary>Example of 1 filter and 1 sorter without JpaSearch</summary>

```java
public class EmployeeSpecification implements Specification<Employee> {

    private final EmployeeCompleteDTO filter;
    private final String scope;
    private final String order;

    public EmployeeSpecification(
        EmployeeCompleteDTO filter,
        String scope,
        String order
    ) {
        this.filter = filter;
        this.scope = scope;
        this.order = order;
    }

    @Override
    public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getFirstName() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.function("unaccent",
                    String.class,
                    criteriaBuilder.lower(root.get("firstName"))),
                "%" + StringUtils.stripAccents(filter.getFirstName()
                    .toLowerCase()) + "%"));
        }

        Order sortOrder;

        sortOrder = order.equals("asc")
            ? criteriaBuilder.asc(root.get(scope))
            : criteriaBuilder.desc(
                criteriaBuilder.coalesce(root.get(scope), 0));
        query.orderBy(sortOrder);

        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
```
</details>

<details>
  <summary>Example of 13 filters and 2 sorters with JpaSearch</summary>

```java
public enum EmployeeCriteria implements ISearchCriteriaConfig<Employee> {
    ID(FilterSorterConfig.of("id", ObjectFilterOperation.EQUAL, "id")),
    FIRSTNAME(FilterSorterConfig.of("firstname", StringFilterOperation.CONTAIN_IGNORE_CASE, "firstName")),
    LASTNAME(FilterConfig.of("lastname", StringFilterOperation.CONTAIN_IGNORE_CASE, "lastName")),
    LASTNAME_IGNORE_ACCENT(FilterConfig.of("lastnameIgnoreAccent", StringFilterOperation.CONTAIN_IGNORE_CASE_IGNORE_ACCENT, "lastName")),
    BIRTHDATE(FilterConfig.of("birthdate", ListComparableFilterOperation.BETWEEN, "birthDate")),
    MARRYING(FilterConfig.of("marrying", ObjectFilterOperation.EQUAL, "marriedEmployee.id")),
    COMPANY(FilterConfig.of("company", StringFilterOperation.START_WITH, "company.name")),
    FIRSTNAME_OR_LASTNAME(FilterConfig.of("firstnameLastname", StringFilterOperation.CONTAIN_IGNORE_CASE, "firstName", "lastName")),
    FIRSTNAME_AND_LASTNAME(GroupFilterConfig.of("searchName", FIRSTNAME.getFilterConfig(), LASTNAME.getFilterConfig())),
    SEARCH_MARRIED(MultiFilterConfig.of("search_married", FIRSTNAME_AND_LASTNAME.getFilterConfig(), "marriedEmployee.id")),
    PET_NAME(FilterConfig.of("petName", StringFilterOperation.CONTAIN_IGNORE_CASE, "pets[name]")),
    PET_SPECIES(FilterConfig.of("petSpecies", ObjectFilterOperation.EQUAL, "pets[species]")),
    PETS(GroupFilterConfig.of("searchPets", PET_NAME.getFilterConfig(), PET_SPECIES.getFilterConfig())),
    MULTI_PETS(MultiFilterConfig.of("multiPets", PET_SPECIES.getFilterConfig(), "pets"));

    final ISearchConfig<Employee> searchConfig;

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria(ID.getKey(), SortDirection.ASC);
    }

    @Override
    public Class<Employee> getRootClass() {
        return Employee.class;
    }
}
```

</details>


### Known limitations 🐧

- Jpa Search doesn't provide the option to filter through several *ToMany relations, for example: Companies → Employees → Pets (the generic search doesn't allow you to filter on the pets owned by the employees of each company).
- Sorting on elements requiring a join is not possible (How to sort on a list of linked elements)
- Autogenerated swaggers can't identify fields in queryparams, since a `Map<String, String>` is passed.
- Types actually handled:
    - Comparable: Float, float, Integer, int, Long, long, Double, double, BigDecimal, LocalDate, LocalDateTime, ZonedDateTime;
    - String: String;
    - Object: Boolean, boolean, UUID;
- Count take place each time.
- Page and Size names are not configurable.

To use the IGNORE_ACCENT operation you must install the function according to your database manager. After that you can specify the name of the function or the name of the schema where the function is located.

Function location (by default = "unaccent")
```
System.setProperty("UNACCENT_FUNCTION_PATH", "dbo.unaccent");
```

<details>
  <summary>MySQL</summary>

```sql
DROP FUNCTION IF EXISTS unaccent;
DELIMITER |
CREATE FUNCTION unaccent( textvalue VARCHAR(10000) ) RETURNS VARCHAR(10000)
DETERMINISTIC
NO SQL
    BEGIN

    SET @textvalue = textvalue COLLATE utf8mb4_general_ci;

    -- ACCENTS
    SET @withaccents = 'ŠšŽžÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸÞàáâãäåæçèéêëìíîïñòóôõöøùúûüýÿþƒ';
    SET @withoutaccents = 'SsZzAAAAAAACEEEEIIIINOOOOOOUUUUYYBaaaaaaaceeeeiiiinoooooouuuuyybf';
    SET @count = LENGTH(@withaccents);

    WHILE @count > 0 DO
        SET @textvalue = REPLACE(@textvalue, SUBSTRING(@withaccents, @count, 1), SUBSTRING(@withoutaccents, @count, 1));
        SET @count = @count - 1;
    END WHILE;

    -- SPECIAL CHARS

    SET @special = '«»’”“!@#$%¨&()_+=§¹²³£¢¬"`´{[^~}]<,>.:;?/°ºª+|\';
    SET @count = LENGTH(@special);

    WHILE @count > 0 DO
        SET @textvalue = REPLACE(@textvalue, SUBSTRING(@special, @count, 1), '');
        SET @count = @count - 1;
    END WHILE;

    RETURN @textvalue;
END
|
DELIMITER ;
```

</details>


<details>
  <summary>PostgreSQL</summary>

```sql
CREATE EXTENSION IF NOT EXISTS "unaccent";
```

</details>

<details>
  <summary>Microsoft SQL Server</summary>

```sql
IF OBJECT_ID('dbo.unaccent', 'FN') IS NOT NULL
    DROP FUNCTION dbo.unaccent;
GO
CREATE FUNCTION dbo.unaccent (@textvalue NVARCHAR(MAX))
RETURNS NVARCHAR(MAX)
AS
BEGIN
    DECLARE @withaccents NVARCHAR(MAX), @withoutaccents NVARCHAR(MAX), @special NVARCHAR(MAX);
    DECLARE @count INT;

    SET @textvalue = @textvalue COLLATE Latin1_General_BIN;

     -- ACCENTS
    SET @withaccents =    'ŠšŽžÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸàáâãäåæçèéêëìíîïñòóôõöøùúûüýÿƒ';
    SET @withoutaccents = 'SsZzAAAAAAACEEEEIIIINOOOOOOUUUUYYaaaaaaaceeeeiiiinoooooouuuuyyf';

    SET @count = LEN(@withaccents);
        
    WHILE @count > 0
    BEGIN
        SET @textvalue = REPLACE(@textvalue, SUBSTRING(@withaccents, @count, 1), SUBSTRING(@withoutaccents, @count, 1));
        SET @count = @count - 1;
    END;

    -- SPECIAL CHARS
    SET @special = '«»’”“!@#$%¨&()_+=§¹²³£¢¬"`´{[^~}]<,>.:;?/°ºª+|';
    SET @count = LEN(@special);

    WHILE @count > 0
    BEGIN
        SET @textvalue = REPLACE(@textvalue, SUBSTRING(@special, @count, 1), '');
        SET @count = @count - 1;
    END;

    RETURN @textvalue;
END;
```

</details>

## Requirements ⬇️

This library is compatible with Java17+, so you can install it simply by adding its dependencies to your build.gradle / pom.xml.

## Getting started ✋

<details>
  <summary>1 - Configure your SpringBootApplication</summary>

Firstly, you need to specify that you want Spring to scan your package and the library package to allow Spring bean detectiona and injection, so in your SpringBootApplication add this line :
  ```java
  @SpringBootApplication(scanBasePackages = {"your_package", "com.avisto.jpasearch"})
  ```
</details>

<details>
  <summary>2 - Configure your Entity</summary>

For the library to correctly analyze your entity, you must add SearchableEntity to your Entity :
```java
  public class Entity implements SearchableEntity {}
  ```
</details>

<details>
  <summary>3 - Create Criteria Enum</summary>

To specify a filter for your search, you need to create an enum as described in [How to use it ? 🤔](#how-to-use-it).

</details>

<details>
  <summary>4 - Configure your Service</summary>

After applying the previous steps, you can inject a SearchCriteriaRepository with the criteria and the entity you want to search with.
example with constructor injection :
```java
private final SearchCriteriaRepository<Entity, EntityCriteria> searchCriteriaRepository;
```
example with field injection :

```java
@Autowired private SearchCriteriaRepository<Entity, EntityCriteria> searchCriteriaRepository;
```
</details>

<details>
  <summary>5 - Example of use</summary>

This example shows you how to search your entity with EntityCriteria Enum :
```java
  public Page<EntityDTO.EntityInList> getEntities(Map<String, String> params, List<String> sorts) {
    return searchCriteriaRepository.search(EntityCriteria.class, params, sorts, EntityDTO.EntityInList::new);
  }
```

The result: Page<EntityDTO.EntityInList> contains the total number of elements and a list of elements.

</details>


## Contributing 👯

Gabriel Revelli\
Martin Rech

## License 📃
For open source projects, say how it is licensed.