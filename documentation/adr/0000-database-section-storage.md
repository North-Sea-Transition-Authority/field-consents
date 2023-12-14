# Database section storage

* Status: APPROVED
* Approved by: James Barnett

## Problem statement

We need to store sections for document templates and document instances in the database. Sections can have nested child
sections.

### Options

#### Option 1: Store sections as JSON

With this option we would store the sections in the database as JSON. 

The template/instance entities would have a List of sections which would be stored as JSON using `@JdbcTypeCode`, e.g:

```java
@JdbcTypeCode(SqlTypes.JSON)
private List<DocumentTemplateSection> sections;
```

The `DocumentTemplateSection` record would look something like the following:

```java
public record DocumentTemplateSection(
    UUID id,
    DocumentTemplate documentTemplate,
    String title,
    String content,
    List<DocumentTemplateSection> children
) {
}
```

##### Positives
- No need to write any code to build up a hierarchical section tree as you would already have a fully hydrated list of
sections.
- No need for a display order column as the sections would be shown in the order of the list at each level.

##### Negatives
- There would be no database constraints, so you could end up with invalid data (e.g duplicate IDs or null columns).
- All Envers audit history data would be stored against the template/instance instead of per section. It would be
difficult to extract when each individual section was edited and who edited it.

#### Option 2: Store sections relationally

With this approach, we would have tables for template/instance sections and store the sections relationally. 

The tables would contain a `parent_id` column so you can tell if the section is a child. They would also contain a
`display_order` column to store the order of the sections relative to other sibling sections at the same level.

For example:

```sql
CREATE TABLE document_library_document_template_sections(
  id UUID PRIMARY KEY NOT NULL
, document_template_id UUID NOT NULL
, parent_id UUID
, title TEXT NOT NULL
, content TEXT
, display_order INTEGER NOT NULL
);
```

##### Positives
- You have database constraints to ensure valid data. 
- Envers audit history data would be per-section.
- Existing known working implementation in PWA uses this approach.

##### Negatives
- You have to write some code to build up a hierarchical section tree in the library.
- You need a display order column and code in the library to shift the display order of existing sections if someone
wants to add section before an existing section.

### Decision

Option 2 will be used as it has fewer trade-offs than option 1 and requires minimal additional code which would only be
written once in the library.
