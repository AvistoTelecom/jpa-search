# Generic-Spring-Search


TODO : Add JavaDoc and clean code
TODO : remove spring dependency by implementing Pageables
TODO : add Unit Tests



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://versioning.advans-group.com/avisto-tooling/generic-spring-search.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://versioning.advans-group.com/avisto-tooling/generic-spring-search/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Automatically merge when pipeline succeeds](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing(SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thank you to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README
Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Generic JPA Search

## Description
Generic search simplifies specific entity searches with JPA, including :
- Modular filters
- Multiple filters
- Modular sorts
- Multiple sorts (following each other)
- Various operations (you can add yours)
- Complete pagination


This search is designed to be efficient, since it operates at a low level, as close as possible to SQL, thanks to the CriteriaBuilder provided by jakarta.persistence.

A search works with a configuration file where you define each possible filter, its associated operation (e.g.: name equals value) and where to fetch the field in DB. Once a filter has been defined, it is possible to filter by this ascending or descending field.

The search function takes as input a list of query params in the form of a `Map<String, String>` (also containing a `page` and `size` field for pagination), a `List<String>` sort list, the type of entity searched for `Class<T>` and the type of search configuration enumeration `Class<E>`. In return, this function returns a `Page<T>` containing the number of elements requested and the number of total elements.

### How to use it ?

To configure a search, you need to create a configuration criteria `enum` described like below:

```java
@Getter
@AllArgsConstructor
@SearchEntity(target = ApiKey.class)
public enum ApiKeyCriteria implements SearchConfigInterface {
    ACCOUNT_TYPE(FilterConfig.of(FilterOperation.EQUAL, "accountType", "account.accountType.key")),
    PROFILE_TYPE(FilterConfig.of(FilterOperation.EQUAL, "profileType", "account.accountType.profile.key")),
    ACCOUNT_FILES_NAME(FilterConfig.of(FilterOperation.EQUAL, "accountFilesName", "account.files[name]")),
    ACCOUNT_LABEL(FilterConfig.of(FilterOperation.EQUAL, "accountLabel", "account.label")),
    TYPE(FilterConfig.of(FilterOperation.EQUAL, "type", "type")),
    EXTERNAL_ID(FilterConfig.of(FilterOperation.LIKE_IGNORE_CASE, "externalId", "externalId")),
    CREATION_DATE(FilterConfig.of(FilterOperation.BETWEEN, "creationDate", "creationDate")),
    END_DATE(FilterConfig.of(FilterOperation.BETWEEN, "endDate", "endDate")),
    EXPIRATION_DATE(FilterConfig.of(FilterOperation.BETWEEN, "expirationDate", "expirationDate")),
    LABEL(FilterConfig.of(FilterOperation.LIKE_IGNORE_CASE_IGNORE_ACCENT, "label", "label", "account.label"));

    FilterConfig filterConfig;

    @Override
    public OrderCriteria getDefaultOrderCriteria() {
        return new OrderCriteria(ACCOUNT_LABEL.getFilterKey(), SortDirection.ASC);
    }

}
```
The idea here is to associate in `FilterConfig.of(...)` an operation, a query param name and an entity field name.

Taking `ACCOUNT_TYPE` as an example, we're looking for an `ApiKey` that is associated with a particular account type, in this case equality.

The `FilterOperation` is used to describe the operation to be performed on the filter, many of which are already available and applicable to different types:

| Operator Name | Description | Filter Type |
| ------------- | ----------- | ---- |
| `LIKE_IGNORE_CASE` | Checks that the `field` contains part of the `filter`, case ignored | `String` |
| `START_WITH` | Checks that the `field` begins with the `filter` | `String` |
| `START_WITH_IGNORE_CASE` | Check that the `field` begins with the `filter`, case ignored | `String` |
| `START_WITH_IGNORE_CASE_IGNORE_ACCENT` | Check that the `field` starts with the `filter`, case and accents ignored | `String` |
| `EQUAL` | Checks for equality between `field` and `filter` | `Object` |
| `EQUAL_IGNORE_CASE_IGNORE_ACCENT` | Checks for equality between `field` and `filter`, case and accents ignored | `String` |
| `NOT_NULL` | Checks for `field` nullity | `Object` |
| `IN_EQUAL` | Checks the equality of `field` with one of the `fields` in the filter lis | `Object[]` |
| `IN_LIKE` | Checks that `field` contains one of the fields in the `filter` list | `String[]` |
| `IN_EQUAL_IGNORE_CASE_IGNORE_ACCENT` | Checks that `field` is equal to one of the fields in the `filter` list, case and accents ignored | `String[]` |
| `BETWEEN` | Checks that `field` is between the two `filter` fields | `Comparable[2]` |
| `GREATER_THAN_OR_EQUAL` | Checks that `field` is greater than or equal to `filter` | `Comparable` |
| `LESS_THAN_OR_EQUAL` | Checks that `field` is smaller than or equal to `filter` | `Comparable` |

You can easily create an other `enum` file like `FilterOperation.java`.

### Known limitations

- The generic search doesn't yet offer the option of navigating through several levels of ManyToMany dependencies, for example: Companies → Employees → Pets (the generic search doesn't allow you to filter on the pets owned by the employees of each company).
- Sorting on elements requiring a join is not possible (How to sort on a list of linked elements)
- Autogenerated swaggers can't identify fields in queryparams, since a `Map<String, String>` is passed.
- Types actually handled:
    - Comparable: Float, float, Integer, int, Long, long, Double, double, BigDecimal, LocalDate, LocalDateTime, ZonedDateTime;
    - String: String;
    - Object: Boolean, boolean, UUID;

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
