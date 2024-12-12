INSERT INTO fcs.teams (id, type, name, scope_type, scope_id)
VALUES (gen_random_uuid(), 'INDUSTRY', 'BP EXPLORATION OIL & GAS COMPANY', 'ORGANISATION_GROUP_ID', '50');

INSERT INTO fcs.team_roles (id, wua_id, team_id, role) VALUES
    (gen_random_uuid(), 53644, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'ACCESS_MANAGER'), -- opred-access-manager@field-consents.co.uk
    (gen_random_uuid(), 53645, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'VIEWER'), -- opred-viewer1@field-consents.co.uk
    (gen_random_uuid(), 53646, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'VIEWER'), -- opred-viewer2@field-consents.co.uk
    (gen_random_uuid(), 53647, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'ALLOCATOR'), -- opred-allocator1@field-consents.co.uk
    (gen_random_uuid(), 53648, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'ALLOCATOR'), -- opred-allocator2@field-consents.co.uk
    (gen_random_uuid(), 53649, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'RESPONDER'), -- opred-responder1@field-consents.co.uk
    (gen_random_uuid(), 53650, (SELECT id FROM fcs.teams WHERE type = 'CONSULTEE'), 'RESPONDER'), -- opred-responder2@field-consents.co.uk
    (gen_random_uuid(), 38138, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'EDITOR'),
    (gen_random_uuid(), 38138, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'ACCESS_MANAGER'),
    (gen_random_uuid(), 38138, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'VIEWER'),
    (gen_random_uuid(), 38138, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'CREATOR'),
    (gen_random_uuid(), 38138, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'SUBMITTER'),
    (gen_random_uuid(), 53712, (SELECT id FROM fcs.teams WHERE scope_id = '50'), 'FINANCE_ADMINISTRATOR'),
    (gen_random_uuid(), 53453, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_OFFICER'),
    (gen_random_uuid(), 53552, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_OFFICER'),
    (gen_random_uuid(), 53553, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_OFFICER'),
    (gen_random_uuid(), 53554, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_MANAGER'),
    (gen_random_uuid(), 53555, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_MANAGER'),
    (gen_random_uuid(), 53452, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CASE_MANAGER'),
    (gen_random_uuid(), 53696, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam1@field-consents.co.uk
    (gen_random_uuid(), 53697, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam2@field-consents.co.uk
    (gen_random_uuid(), 53698, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam3@field-consents.co.uk
    (gen_random_uuid(), 53512, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'TECHNICAL_REVIEWER'),
    (gen_random_uuid(), 53513, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'TECHNICAL_REVIEWER'),
    (gen_random_uuid(), 53514, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'TECHNICAL_REVIEWER'),
    (gen_random_uuid(), 38137, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'ACCESS_MANAGER'),
    (gen_random_uuid(), 38137, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'INDUSTRY_ACCESS_MANAGER'),
    (gen_random_uuid(), 53752, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager1@field-consents.co.uk
    (gen_random_uuid(), 53753, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager2@field-consents.co.uk
    (gen_random_uuid(), 53754, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager3@field-consents.co.uk
    (gen_random_uuid(), 38137, (SELECT id FROM fcs.teams WHERE type = 'REGULATOR'), 'VIEWER');