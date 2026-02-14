#!/usr/bin/env bash
set -uo pipefail

BASE_URL="${PE_URL:-https://tv.adapto.kz}"
EMAIL="${PE_EMAIL:-saken@tukenov.kz}"
PASSWORD="${PE_PASSWORD:-Test1234}"
MCP="$BASE_URL/api/mcp"

PASS=0; FAIL=0; SKIP=0; REQ_ID=0
LAST_BODY=""
CLEANUP_CHANNELS=()
TS=$(date +%s)

# ── Helpers ───────────────────────────────
rpc() {
  local method="$1" params="${2:-null}"
  ((REQ_ID++))
  local payload
  if [ "$method" = "notifications/initialized" ]; then
    payload="{\"jsonrpc\":\"2.0\",\"method\":\"$method\"}"
  else
    payload="{\"jsonrpc\":\"2.0\",\"id\":$REQ_ID,\"method\":\"$method\",\"params\":$params}"
  fi
  curl -s -w "\n%{http_code}" -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" -d "$payload" "$MCP"
}

check_rpc() {
  local name="$1" method="$2" params="${3:-null}" expect="${4:-result}"
  local resp code body
  resp=$(rpc "$method" "$params")
  code=$(echo "$resp" | tail -1)
  body=$(echo "$resp" | sed '$d')
  if [ "$code" = "200" ] && echo "$body" | grep -q "\"$expect\""; then
    echo "  ✓ $name"
    ((PASS++))
  else
    echo "  ✗ $name (HTTP $code)"
    echo "    $(echo "$body" | head -c 200)"
    ((FAIL++))
  fi
  LAST_BODY="$body"
}

check_tool() {
  local tool="$1"
  local empty_obj='{}'
  local args="${2:-$empty_obj}"
  local resp code body
  resp=$(rpc "tools/call" "{\"name\":\"$tool\",\"arguments\":$args}")
  code=$(echo "$resp" | tail -1)
  body=$(echo "$resp" | sed '$d')
  if [ "$code" = "200" ] && echo "$body" | grep -q '"result"'; then
    local is_err
    is_err=$(echo "$body" | grep -o '"isError":true' || true)
    if [ -n "$is_err" ]; then
      echo "  ✗ tools/call $tool → isError"
      ((FAIL++))
    else
      echo "  ✓ tools/call $tool"
      ((PASS++))
    fi
  else
    echo "  ✗ tools/call $tool (HTTP $code)"
    echo "    $(echo "$body" | head -c 200)"
    ((FAIL++))
  fi
  LAST_BODY="$body"
}

json_val() {
  echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | cut -d'"' -f4
}

cleanup() {
  echo
  echo "▸ Cleanup"
  [ ${#CLEANUP_CHANNELS[@]} -eq 0 ] && return
  for ch in "${CLEANUP_CHANNELS[@]}"; do
    [ -z "$ch" ] && continue
    rpc "tools/call" "{\"name\":\"channels_delete\",\"arguments\":{\"id\":\"$ch\"}}" > /dev/null 2>&1
    echo "  → deleted channel $ch"
  done
}
trap cleanup EXIT

echo "════════════════════════════════════════════"
echo " Playout Edge — MCP E2E Test Suite"
echo " $MCP"
echo " $(date '+%Y-%m-%d %H:%M:%S')"
echo "════════════════════════════════════════════"
echo

# ══════════════════════════════════════════
# 1. AUTH
# ══════════════════════════════════════════
echo "▸ Auth"
resp=$(curl -s -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  "$BASE_URL/api/auth/login")
TOKEN=$(echo "$resp" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then echo "  ✗ Login failed"; exit 1; fi
echo "  ✓ Login (token obtained)"
((PASS++))

# ══════════════════════════════════════════
# 2. PROTOCOL HANDSHAKE
# ══════════════════════════════════════════
echo "▸ Protocol Handshake"
check_rpc "initialize" "initialize" \
  '{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"e2e-test","version":"1.0"}}'

# Verify serverInfo
if echo "$LAST_BODY" | grep -q "playout-edge-mcp"; then
  echo "    ✓ serverInfo.name = playout-edge-mcp"
else
  echo "    ✗ serverInfo.name missing"
fi

# Verify protocolVersion echoed back
if echo "$LAST_BODY" | grep -q "2025-03-26"; then
  echo "    ✓ protocolVersion = 2025-03-26"
else
  echo "    ✗ protocolVersion mismatch"
fi

# Notification
resp=$(rpc "notifications/initialized")
code=$(echo "$resp" | tail -1)
if [ "$code" = "200" ] || [ "$code" = "204" ]; then
  echo "  ✓ notifications/initialized ($code)"; ((PASS++))
else
  echo "  ✗ notifications/initialized ($code)"; ((FAIL++))
fi

# ══════════════════════════════════════════
# 3. TOOLS LIST
# ══════════════════════════════════════════
echo "▸ Tools Discovery"
check_rpc "tools/list" "tools/list"
TOOL_COUNT=$(echo "$LAST_BODY" | grep -o '"name"' | wc -l | tr -d ' ')
echo "    tool count = $TOOL_COUNT"
if [ "$TOOL_COUNT" -ge 24 ]; then
  echo "    ✓ ≥ 24 tools registered"
else
  echo "    ✗ expected ≥ 24 tools"; ((FAIL++))
fi

# Verify key tools exist
for tool in channels_list channels_create assets_list devices_list schedules_list \
  overlay_profiles_list audit_logs asrun_events alerts_list; do
  if echo "$LAST_BODY" | grep -q "\"$tool\""; then
    echo "    ✓ $tool registered"
  else
    echo "    ✗ $tool missing"; ((FAIL++))
  fi
done

# ══════════════════════════════════════════
# 4. CHANNELS — full CRUD via MCP
# ══════════════════════════════════════════
echo "▸ Channels: CRUD lifecycle via MCP"

check_tool "channels_list"

# Create 3 channels
check_tool "channels_create" "{\"name\":\"MCP Alpha $TS\"}"
MCP_CH1=$(json_val "$LAST_BODY" "id")
[ -n "$MCP_CH1" ] && CLEANUP_CHANNELS+=("$MCP_CH1")
echo "    ch1=$MCP_CH1"

check_tool "channels_create" "{\"name\":\"MCP Beta $TS\"}"
MCP_CH2=$(json_val "$LAST_BODY" "id")
[ -n "$MCP_CH2" ] && CLEANUP_CHANNELS+=("$MCP_CH2")

check_tool "channels_create" "{\"name\":\"MCP Gamma $TS\"}"
MCP_CH3=$(json_val "$LAST_BODY" "id")
[ -n "$MCP_CH3" ] && CLEANUP_CHANNELS+=("$MCP_CH3")

# Get each channel
if [ -n "$MCP_CH1" ]; then
  check_tool "channels_get" "{\"id\":\"$MCP_CH1\"}"
fi
if [ -n "$MCP_CH2" ]; then
  check_tool "channels_get" "{\"id\":\"$MCP_CH2\"}"
fi

# Update channel name
if [ -n "$MCP_CH1" ]; then
  check_tool "channels_update" "{\"id\":\"$MCP_CH1\",\"name\":\"MCP Ren $TS\"}"
  # Verify update stuck
  check_tool "channels_get" "{\"id\":\"$MCP_CH1\"}"
  if echo "$LAST_BODY" | grep -q "MCP Ren"; then
    echo "    ✓ name updated correctly"
  else
    echo "    ✗ name not updated"
  fi
fi

# Delete channel #3
if [ -n "$MCP_CH3" ]; then
  check_tool "channels_delete" "{\"id\":\"$MCP_CH3\"}"
  CLEANUP_CHANNELS=("${CLEANUP_CHANNELS[@]/$MCP_CH3/}")
fi

# List again to verify
check_tool "channels_list"

# ══════════════════════════════════════════
# 5. ASSETS via MCP
# ══════════════════════════════════════════
echo "▸ Assets via MCP"
check_tool "assets_list"
MCP_ASSET=$(json_val "$LAST_BODY" "id")
if [ -n "$MCP_ASSET" ]; then
  check_tool "assets_get" "{\"id\":\"$MCP_ASSET\"}"
else
  echo "  ⊘ SKIP assets_get (none exist)"; ((SKIP++))
fi

# ══════════════════════════════════════════
# 6. DEVICES via MCP
# ══════════════════════════════════════════
echo "▸ Devices via MCP"
check_tool "devices_list"
MCP_DEV=$(json_val "$LAST_BODY" "id")
if [ -n "$MCP_DEV" ]; then
  check_tool "devices_get" "{\"id\":\"$MCP_DEV\"}"
  check_tool "devices_update" "{\"id\":\"$MCP_DEV\",\"displayName\":\"MCP Test Device\"}"
else
  echo "  ⊘ SKIP device tools (none exist)"; ((SKIP+=2))
fi

# ══════════════════════════════════════════
# 7. SCHEDULES via MCP
# ══════════════════════════════════════════
echo "▸ Schedules via MCP"
if [ -n "$MCP_CH1" ]; then
  check_tool "schedules_list" "{\"channelId\":\"$MCP_CH1\"}"

  # Note: schedule_draft creation is via REST only, so we test list/get
else
  echo "  ⊘ SKIP schedules (no channel)"; ((SKIP++))
fi

# ══════════════════════════════════════════
# 8. OVERLAYS via MCP
# ══════════════════════════════════════════
echo "▸ Overlays via MCP"
check_tool "overlay_profiles_list"

check_tool "overlay_profiles_create" \
  "{\"name\":\"MCP Overlay $TS\",\"definition\":{\"layers\":[{\"type\":\"ticker\",\"text\":\"MCP test\"}]}}"
MCP_PROF=$(json_val "$LAST_BODY" "id")
echo "    profile=$MCP_PROF"

# Overlay state via MCP
if [ -n "$MCP_CH1" ]; then
  # PUT state first to create it
  check_tool "overlay_state_update" \
    "{\"channelId\":\"$MCP_CH1\",\"state\":{\"layers\":[{\"type\":\"ticker\",\"visible\":true}]}}"

  check_tool "overlay_state_get" "{\"channelId\":\"$MCP_CH1\"}"
fi

# ══════════════════════════════════════════
# 9. REPORTS via MCP
# ══════════════════════════════════════════
echo "▸ Reports via MCP"
check_tool "audit_logs"
check_tool "audit_logs" '{"limit":5}'
check_tool "asrun_events"
check_tool "asrun_events" '{"limit":5}'
check_tool "alerts_list"
check_tool "alerts_list" '{"status":"OPEN"}'

# ══════════════════════════════════════════
# 10. ERROR CASES
# ══════════════════════════════════════════
echo "▸ Error Cases"

# Unknown tool
resp=$(rpc "tools/call" '{"name":"nonexistent_tool","arguments":{}}')
body=$(echo "$resp" | sed '$d')
if echo "$body" | grep -q '"isError"\|"error"'; then
  echo "  ✓ unknown tool → error"; ((PASS++))
else
  echo "  ✗ unknown tool did not return error"; ((FAIL++))
fi

# Unknown method
resp=$(rpc "unknown/method")
body=$(echo "$resp" | sed '$d')
if echo "$body" | grep -q '"error"'; then
  echo "  ✓ unknown method → error"; ((PASS++))
else
  echo "  ✗ unknown method did not return error"; ((FAIL++))
fi

# Get non-existent channel
check_tool_error() {
  local tool="$1" args="$2"
  local resp code body
  resp=$(rpc "tools/call" "{\"name\":\"$tool\",\"arguments\":$args}")
  code=$(echo "$resp" | tail -1)
  body=$(echo "$resp" | sed '$d')
  if echo "$body" | grep -q '"isError":true'; then
    echo "  ✓ $tool → isError (expected)"; ((PASS++))
  else
    echo "  ✗ $tool should have returned isError"; ((FAIL++))
  fi
}

# channels_get with non-existent ID returns "not found" (graceful, no isError)
check_tool "channels_get" '{"id":"00000000-0000-0000-0000-000000000000"}'
if echo "$LAST_BODY" | grep -qi "not found"; then
  echo "    ✓ returned 'not found' message"
else
  echo "    ✗ expected 'not found' in response"
fi

# ══════════════════════════════════════════
# Summary
# ══════════════════════════════════════════
echo
echo "════════════════════════════════════════════"
printf " Results: ✓ %d  ✗ %d  ⊘ %d\n" "$PASS" "$FAIL" "$SKIP"
echo "════════════════════════════════════════════"

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
