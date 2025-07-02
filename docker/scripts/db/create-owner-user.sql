BEGIN;

WITH system_owner_audit AS (
  INSERT INTO users_audit (id, created_by)
  VALUES (gen_random_uuid(), 'system')
  RETURNING id
)
INSERT INTO users (id, email, name, password, role, audit_id)
SELECT
  gen_random_uuid(),
  :'APPLICATION_OWNER_EMAIL',
  :'APPLICATION_OWNER_NAME',
  :'APPLICATION_OWNER_PASSWORD_HASH',
  'OWNER',
  id
FROM system_owner_audit;

COMMIT;
