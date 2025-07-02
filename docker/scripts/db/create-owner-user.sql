-- Replace fields with valid values before executing
BEGIN;

WITH system_owner_audit AS (
  INSERT INTO users_audit (id, created_by)
  VALUES (gen_random_uuid(), 'system')
  RETURNING id
)
INSERT INTO users (id, email, name, password, role, audit_id)
SELECT
  gen_random_uuid(),
  'APPLICATION_OWNER_EMAIL_VALUE',
  'APPLICATION_OWNER_NAME_VALUE',
  'APPLICATION_OWNER_PASSWORD_HASH_VALUE',
  'OWNER',
  id
FROM system_owner_audit;

COMMIT;
