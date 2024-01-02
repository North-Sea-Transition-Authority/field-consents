-- This patch adds all field consents users to 'FCS_ACCESS_TEAM' to allow access to new Field Consents service from the
-- Energy Portal workbasket via LHS link.
DECLARE
  l_res_id NUMBER(12);
  l_rp_id_list BPMMGR.NUMBER_LIST_TYPE;

BEGIN

  SELECT wua.resource_person_id
  BULK COLLECT INTO l_rp_id_list
  FROM securemgr.web_user_accounts wua
  WHERE wua.login_id LIKE '%@field-consents.co.uk';

  SELECT xr.res_id
  INTO l_res_id
  FROM decmgr.xview_resources xr
  WHERE xr.res_type = 'FCS_ACCESS_TEAM';

  FOR i IN l_rp_id_list.FIRST..l_rp_id_list.LAST LOOP
    decmgr.contact.add_member(
      p_res_id => l_res_id
    , p_role_name => 'SERVICE_ACCESS'
    , p_person_id => l_rp_id_list(i)
    , p_requesting_wua_id => 1
    );
  END LOOP;

END;
/

COMMIT
/
