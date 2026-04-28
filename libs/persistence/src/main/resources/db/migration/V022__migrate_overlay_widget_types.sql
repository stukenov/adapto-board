-- Normalize widget type codes in overlay profiles and states to match widget_templates.code
-- The only mismatch: "table" in JSON → "queue-table" in widget_templates

-- Fix overlay_profiles.definition_json
UPDATE overlay_profiles
SET definition_json = (
    SELECT jsonb_set(
        definition_json,
        '{widgets}',
        (
            SELECT COALESCE(jsonb_agg(
                CASE
                    WHEN w->>'type' = 'table' THEN jsonb_set(w, '{type}', '"queue-table"')
                    ELSE w
                END
            ), '[]'::jsonb)
            FROM jsonb_array_elements(definition_json->'widgets') AS w
        )
    )
)
WHERE definition_json->'widgets' @> '[{"type":"table"}]';

-- Fix overlay_states.state_json
UPDATE overlay_states
SET state_json = (
    SELECT jsonb_set(
        state_json,
        '{widgets}',
        (
            SELECT COALESCE(jsonb_agg(
                CASE
                    WHEN w->>'type' = 'table' THEN jsonb_set(w, '{type}', '"queue-table"')
                    ELSE w
                END
            ), '[]'::jsonb)
            FROM jsonb_array_elements(state_json->'widgets') AS w
        )
    )
)
WHERE state_json->'widgets' @> '[{"type":"table"}]';
