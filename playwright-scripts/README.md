# playwright-scripts

## `create-applications`

Will create a lot of flare, vent and production applications which are ready to grant and issue

> **Important**: Before running this script, ensure your fee lines are all set to 0 so that submitting applications is
> free. We want to avoid spamming gov.uk pay

## bun

```bash
bun install && bun create-applications
```

## npm

```bash
npm install && npm create-applications
```

## optional environment variables

These should be set when running the scripts against an environment where the base url or passwords differ. They should be put into a new file called `.env` in this directory

| Environment variable         | Default value                         |
|------------------------------|---------------------------------------|
| `BASE_URL`                   | `http://localhost:8080/fcs/work-area` |
| `INDUSTRY_USER_PASSWORD`     | `dev1`                                |
| `CASE_OFFICER_USER_PASSWORD` | `dev1`                                |
| `CAM_USER_PASSWORD`          | `dev1`                                |