INSERT INTO fcs.teams (type, display_name, organisation_group_id)
VALUES ('INDUSTRY', 'BP EXPLORATION', 50);

INSERT INTO fcs.team_member_roles (wua_id, team_id, role) VALUES
    (53644, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ACCESS_MANAGER'), -- opred-access-manager@field-consents.co.uk
    (53645, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'VIEWER'), -- opred-viewer1@field-consents.co.uk
    (53646, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'VIEWER'), -- opred-viewer2@field-consents.co.uk
    (53647, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ALLOCATOR'), -- opred-allocator1@field-consents.co.uk
    (53648, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ALLOCATOR'), -- opred-allocator2@field-consents.co.uk
    (53649, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'RESPONDER'), -- opred-responder1@field-consents.co.uk
    (53650, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'RESPONDER'), -- opred-responder2@field-consents.co.uk
    (38138, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'EDITOR'),
    (38138, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'ACCESS_MANAGER'),
    (38138, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'VIEWER'),
    (38138, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'CREATOR'),
    (38138, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'SUBMITTER'),
    (53712, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'FINANCE_ADMINISTRATOR'),
    (53453, 1, 'CASE_OFFICER'),
    (53552, 1, 'CASE_OFFICER'),
    (53553, 1, 'CASE_OFFICER'),
    (53554, 1, 'CASE_MANAGER'),
    (53555, 1, 'CASE_MANAGER'),
    (53452, 1, 'CASE_MANAGER'),
    (53696, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam1@field-consents.co.uk
    (53697, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam2@field-consents.co.uk
    (53698, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam3@field-consents.co.uk
    (53512, 1, 'TECHNICAL_REVIEWER'),
    (53513, 1, 'TECHNICAL_REVIEWER'),
    (53514, 1, 'TECHNICAL_REVIEWER'),
    (38137, 1, 'ACCESS_MANAGER'),
    (38137, 1, 'INDUSTRY_ACCESS_MANAGER'),
    (53752, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager1@field-consents.co.uk
    (53753, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager2@field-consents.co.uk
    (53754, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager3@field-consents.co.uk
    (38137, 1, 'VIEWER');
