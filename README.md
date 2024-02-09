<img src="https://www.apec.fr/files/live/mounts/images/presentation_entreprise/416917/MIG_RENAME_06F162FF85ACD9785CB46C012D1AEFF21A5DF630.png" alt="Logo Avisto" width="270" height="173" />

# Generic-Spring-Search


TODO : Add JavaDoc and clean code
TODO : remove spring dependency by implementing Pageables ( already done ?)
TODO : add Unit Tests

[![SpringBoot](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white
)](https://hibernate.org/)

[//]: # (dispo entity graph, voir benchmark(limit entity graph qui n'existe plus)

***
## Description 🔍

Generic-Spring-Search simplifies specific entity searches with JPA, including :
- Modular filters
- Multiple filters
- Modular sorts
- Multiple sorts (following each other)
- Various operations (you can add yours)
- Complete pagination


This search is designed to be efficient, since it operates at a low level, as close as possible to SQL, thanks to the CriteriaBuilder provided by javax.persistence.

A search works with a configuration file where you define each possible filter, its associated operation (e.g.: name equals value) and where to fetch the field in DB. Once a filter has been defined, it is possible to filter by this ascending or descending field.

The search function takes as input a list of query params in the form of a `Map<String, String>` (also containing a `page` and `size` field for pagination), a `List<String>` sort list, the type of entity searched for `Class<T>` and the type of search configuration enumeration `Class<E>`. In return, this function returns a `Page<T>` containing the number of elements requested and the number of total elements.

### How to use it ? 🤔 <a name="how-to-use-it"></a>

To configure a search, you need to create a configuration criteria `enum` described like below:

```java
@Getter
@AllArgsConstructor
public enum ApiKeyCriteria implements ISearchCriteriaConfig<Apikey> {
    ID(FilterSorterConfig.of("id", ObjectFilterOperation.EQUAL, "id")),
    ACCOUNT_TYPE(FilterConfig.of("accountType", ObjectFilterOperation.EQUAL, "account.accountType.key")),
    PROFILE_TYPE(FilterConfig.of("profileType", ObjectFilterOperation.EQUAL, "account.accountType.profile.key")),
    ACCOUNT_FILES_NAME(FilterConfig.of("accountFilesName", ObjectFilterOperation.EQUAL, "account.files[name]")),
    ACCOUNT_LABEL(FilterConfig.of("accountLabel", ObjectFilterOperation.EQUAL, "account.label")),
    TYPE(FilterConfig.of("type", ObjectFilterOperation.EQUAL, "type")),
    EXTERNAL_ID(FilterConfig.of("externalId", StringFilterOperation.LIKE_IGNORE_CASE, "externalId")),
    CREATION_DATE(FilterConfig.of("creationDate", ListComparableFilterOperation.BETWEEN, "creationDate")),
    END_DATE(FilterConfig.of("endDate", ListComparableFilterOperation.BETWEEN, "endDate")),
    EXPIRATION_DATE(FilterConfig.of("expirationDate", ListComparableFilterOperation.BETWEEN, "expirationDate")),
    LABEL(FilterConfig.of("label", StringFilterOperation.LIKE_IGNORE_CASE_IGNORE_ACCENT, "label", "account.label")),
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

[//]: # (The idea here is to associate in `FilterConfig.of&#40;...&#41;` an operation, a query param name and an entity field name.)

Taking `ACCOUNT_TYPE` as an example, we're looking for an `ApiKey` that is associated with a particular account type, in this case equality.

[//]: # (The `FilterOperation` is used to describe the operation to be performed on the filter, many of which are already available and applicable to different types:)

#### Filter

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
| `LIKE`                                 | Checks that `field` contains part of the `filter`                          | `String`    |
| `LIKE_IGNORE_CASE`                     | Checks that the `field` contains part of the `filter`, case ignored        | `String`    |
| `LIKE_IGNORE_CASE_IGNORE_ACCENT`       | Checks for equality between `field` and `filter`, case and accents ignored | `String`    |
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
| `IN_LIKE`                            | Checks that `field` contains one of the fields in the `filter` list                              | `String[]`  |
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

You can easily create an other `enum` which inherits from `IFilterOperation`, like `ObjectFilterOperation.java`.

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
SEARCH(FilterConfig.of("search", StringFilterOperation.LIKE_IGNORE_CASE, "firstName", "lastName"));
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

### Known limitations 📈

- The generic search doesn't yet offer the option of navigating through several levels of ManyToMany dependencies, for example: Companies → Employees → Pets (the generic search doesn't allow you to filter on the pets owned by the employees of each company).
- Sorting on elements requiring a join is not possible (How to sort on a list of linked elements)
- Autogenerated swaggers can't identify fields in queryparams, since a `Map<String, String>` is passed.
- Types actually handled:
    - Comparable: Float, float, Integer, int, Long, long, Double, double, BigDecimal, LocalDate, LocalDateTime, ZonedDateTime;
    - String: String;
    - Object: Boolean, boolean, UUID;

## Installation ⬇️
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Getting started ✋

<details>
  <summary>1 - Configure your SpringBootApplication</summary>

  Firstly, you need to specify that you want Spring to scan your package and the library package, so in your SpringBootApplication add this line :
  ```java
  @SpringBootApplication(scanBasePackages = {"your_package", "com.avisto.genericspringsearch"})
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
  <summary>3 - Configure your DTO</summary>

Pas nécessaire je pense à voir
```java
  protected EntityDTO(Entity entity) {
    this.id = entity.getId();
    this.name = entity.getName();
  }

  @Getter
  public static class EntityInList extends EntityDTO {

  public EntityInList(Entity entity) {
    super(entity);
  }
}
  ```
</details>

<details>
  <summary>4 - Create Criteria Enum</summary>

To specify a filter for your search, you need to create an enum as described in [How to use it ? 🤔](#how-to-use-it).

</details>

<details>
  <summary>5 - Configure your Service</summary>

To integrate the library into your services, add the following lines :
```java
  private final SearchCriteriaRepository<Entity, EntityCriteria> searchCriteriaRepository;
  ```
</details>

<details>
  <summary>6 - Example of use</summary>

This example shows you how to search your entity with EntityCriteria Enum :
```java
  public Page<EntityDTO.EntityInList> getEntities(Map<String, String> params, List<String> sorts) {
    return searchCriteriaRepository.search(EntityCriteria.class, params, sorts, EntityDTO.EntityInList::new);
  }
```
</details>


## Contributing 👯

Gabriel Revelli\
Martin Rech

## License 📃
For open source projects, say how it is licensed.