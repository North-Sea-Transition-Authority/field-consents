INSERT INTO fcs.teams (type, display_name, organisation_group_id)
VALUES ('INDUSTRY', 'BP EXPLORATION OIL & GAS COMPANY', 50);

INSERT INTO fcs.teams (type, display_name, organisation_group_id)
VALUES ('INDUSTRY', 'ROYAL DUTCH SHELL', 116);

INSERT INTO fcs.team_member_roles (wua_id, team_id, role) VALUES
    (50586, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ACCESS_MANAGER'), -- opred-access-manager@field-consents.co.uk
    (50587, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'VIEWER'), -- opred-viewer1@field-consents.co.uk
    (50588, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'VIEWER'), -- opred-viewer2@field-consents.co.uk
    (50589, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ALLOCATOR'), -- opred-allocator1@field-consents.co.uk
    (50766, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'ALLOCATOR'), -- opred-allocator2@field-consents.co.uk
    (50590, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'RESPONDER'), -- opred-responder1@field-consents.co.uk
    (50591, (SELECT id FROM fcs.teams WHERE type = 'OPRED'), 'RESPONDER'), -- opred-responder2@field-consents.co.uk
    -- industry.bp@field-consents.co.uk
    (49347, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'EDITOR'),
    (49347, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'ACCESS_MANAGER'),
    (49347, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'VIEWER'),
    (49347, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'CREATOR'),
    (49347, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'SUBMITTER'),
    (50726, (SELECT id FROM fcs.teams WHERE organisation_group_id = 50), 'FINANCE_ADMINISTRATOR'), -- industry.bp.finance.admin@field-consents.co.uk
    -- industry.shell@field-consents.co.uk
    (49348, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'EDITOR'),
    (49348, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'ACCESS_MANAGER'),
    (49348, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'VIEWER'),
    (49348, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'CREATOR'),
    (49348, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'SUBMITTER'),
    (50727, (SELECT id FROM fcs.teams WHERE organisation_group_id = 116), 'FINANCE_ADMINISTRATOR'), -- industry.shell.finance.admin@field-consents.co.uk
    (50286, 1, 'CASE_OFFICER'), -- administrator.case.officer@field-consents.co.uk
    (50346, 1, 'CASE_OFFICER'), -- administrator.case.officer2@field-consents.co.uk
    (50347, 1, 'CASE_OFFICER'), -- administrator.case.officer3@field-consents.co.uk
    (50287, 1, 'ACCESS_MANAGER'), -- administrator.case.Manager@field-consents.co.uk
    (50287, 1, 'INDUSTRY_ACCESS_MANAGER'), -- administrator.case.Manager@field-consents.co.uk
    (50287, 1, 'CASE_MANAGER'), -- administrator.case.Manager@field-consents.co.uk
    (50348, 1, 'CASE_MANAGER'), -- administrator.case.manager2@field-consents.co.uk
    (50349, 1, 'CASE_MANAGER'), -- administrator.case.manager3@field-consents.co.uk
    (50686, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam1@field-consents.co.uk
    (50687, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam2@field-consents.co.uk
    (50688, 1, 'CONSENTS_AND_AUTHORISATIONS_MANAGER'), -- cam3@field-consents.co.uk
    (50406, 1, 'TECHNICAL_REVIEWER'), -- technical.reviewer1@field-consents.co.uk
    (50407, 1, 'TECHNICAL_REVIEWER'), -- technical.reviewer2@field-consents.co.uk
    (50408, 1, 'TECHNICAL_REVIEWER'), -- technical.reviewer3@field-consents.co.uk
    (50746, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager1@field-consents.co.uk
    (50747, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager2@field-consents.co.uk
    (50748, 1, 'DOCUMENT_TEMPLATE_MANAGER'), -- document.template.manager3@field-consents.co.uk
    (49346, 1, 'ACCESS_MANAGER'), -- administrator@field-consents.co.uk
    (49346, 1, 'INDUSTRY_ACCESS_MANAGER'), -- administrator@field-consents.co.uk
    (49346, 1, 'VIEWER'); -- administrator@field-consents.co.uk
