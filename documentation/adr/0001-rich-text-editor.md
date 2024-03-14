# Rich text editor

- Status: APPROVED
- Approved by: Chris Tasker, James Barnett, Will Jukes

## Problem statement

Users of the Field Consents system need a way to format text when editing document sections.
This includes (but is not limited to): aligning text, headings, bullet-pointed lists and tables.
The end result should be some HTML with styling that can be displayed on a page and within a PDF.

### Options

#### Option 1: [TipTap](https://tiptap.dev/) + Custom frontend

TipTap is an open source headless rich-text editor. It provides an API and can be used with pure JS, Vue, React etc.
It doesn't come with a frontend, so we would need to implement our own menu bar with actions for bolding, italicising,
aligning text, tables.

Using the pure JS implementation revealed many reactivity quirks. For example, when a user highlights a region of text,
the corresponding actions should update accordingly. The pure JS implementation uses events, which fire when stuff in
the editor changes, but this can get very complicated. So for this, it's a good idea to use Vue, and not worry about
covering all edge cases with a manual implementation.

Luckily, TipTap offers a Vue implementation. However, it does mean that the frontend build step will need enhancing.

##### Positives

- The UI can be styled to match the gov.uk design system
- Accessibility work has already been done on eCase
- TipTap is extensible and there are lots of available plugins for features like tables, links, images etc
- Users can see their changes in real time

##### Negatives

- Creating some of the menu bar actions (e.g. tables) will be non-trivial
- The entire thing will need to be styled from scratch to match the gov.uk design system
- Requires changes to the build pipeline due to using Vue components

#### Option 2: Markdown

Markdown is a common and widely used markup language to store rich text information.

##### Positives

- Very easy to integrate into the service
- Familiar to the users if they have used other markdown editors
- Doesn't require any changes to the build pipeline

##### Negatives

- Syntax is non-trivial and varies depending on the site
- Slow feedback loop (can be solved with a split view simiar to [stackedit](https://stackedit.io/app))
- Doesn't support aligning text
  - We will need to write custom Freemarker templates which the users won't be able to edit for things like right-aligned addresses.

#### Option 3: [TinyMCE](https://www.tiny.cloud/)

##### Positives

- Minimal effort to implement because the functionality and UI is provided out of the box
- Supports tables via a plugin

##### Negatives

- Has known accessibility issues
- Limited customisation and extendability options
- Unlicensed usage requires attribution

### Decision

Option 1

We will use TipTap, Vue3 and port the existing styling and features that have already been developed within
[ecase-sba](https://github.com/fivium/ecase-sba). The existing Field Consents frontend build pipeline will
need to be enhanced, but the impact is minimal because Vitejs provide a rollup plugin for Vue.

In future work, the design of the rich text editor can be updated to
be inline with govuk styling. However, this isn't MVP for the time being.
