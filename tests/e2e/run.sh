#!/usr/bin/env bash
# End-to-end test of the FT-Engrave factory pipeline.
#
# Drives the full stack through two real production flows:
#   - QC ACCEPT: order → insert → manufacturing → Pass QC → item shipped
#   - QC REJECT: order → insert → manufacturing → Reject QC →
#                Remove Item From Factory → item gone
#
# All API calls go through the worker-facing proxies on the
# frontend-springboot service (port 8082 by default). The factory
# simulator and the QC Camunda engine are queried directly only for
# verification.

set -euo pipefail

FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
SIMULATOR_URL="${SIMULATOR_URL:-http://localhost:8081}"
QC_ENGINE_URL="${QC_ENGINE_URL:-http://localhost:8100}"
ORDER_ENGINE_URL="${ORDER_ENGINE_URL:-http://localhost:8101}"

STACK_READY_TIMEOUT="${STACK_READY_TIMEOUT:-180}"
PIPELINE_TIMEOUT="${PIPELINE_TIMEOUT:-180}"
SORTER_TIMEOUT="${SORTER_TIMEOUT:-30}"

PASS_COUNT=0
FAIL_COUNT=0

log()  { printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*"; }
pass() { PASS_COUNT=$((PASS_COUNT + 1)); printf '  \033[32m✓\033[0m %s\n' "$*"; }
fail() { FAIL_COUNT=$((FAIL_COUNT + 1)); printf '  \033[31m✗\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[31mFATAL\033[0m %s\n' "$*" >&2; exit 1; }

# ----- generic helpers ---------------------------------------------------

wait_until() {
  local desc="$1"; local timeout="$2"; shift 2
  local deadline=$(( $(date +%s) + timeout ))
  local last_output=""
  while [ "$(date +%s)" -lt "$deadline" ]; do
    if last_output=$("$@" 2>&1); then return 0; fi
    sleep 1
  done
  printf '  last attempt output: %s\n' "$last_output" >&2
  die "timeout waiting ${timeout}s for: $desc"
}

http_ok() {
  local url="$1"
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' "$url" || echo "000")
  [ "$code" = "200" ] || [ "$code" = "201" ] || [ "$code" = "204" ]
}

json() { python3 -c "$@"; }

# ----- stack readiness ---------------------------------------------------

wait_for_stack() {
  log "Waiting up to ${STACK_READY_TIMEOUT}s for the stack to come up..."
  wait_until "frontend orders API" "$STACK_READY_TIMEOUT" http_ok "$FRONTEND_URL/api/orders"
  wait_until "simulator items API" "$STACK_READY_TIMEOUT" http_ok "$SIMULATOR_URL/api/items"
  wait_until "QC engine REST"      "$STACK_READY_TIMEOUT" http_ok "$QC_ENGINE_URL/engine-rest/task"
  wait_until "order-orch engine REST" "$STACK_READY_TIMEOUT" http_ok "$ORDER_ENGINE_URL/engine-rest/task"
  wait_until "dashboard metrics"   "$STACK_READY_TIMEOUT" http_ok "$FRONTEND_URL/api/dashboard/metrics"
  log "Stack is ready"
}

# ----- workflow helpers --------------------------------------------------

create_order() {
  local id="$1"; local color="$2"; local engraved="${3:-}"
  local body
  body=$(printf '{"id":"%s","color":"%s","engravedText":"%s","createdAt":"%s"}' \
    "$id" "$color" "$engraved" "$(date -u +%Y-%m-%dT%H:%M:%SZ)")
  curl -fsS -X POST "$FRONTEND_URL/api/orders" \
    -H 'Content-Type: application/json' -d "$body" >/dev/null
}

insert_item() {
  local id="$1"; local color="$2"
  local response
  response=$(curl -fsS -X POST \
    "$FRONTEND_URL/api/simulator/items?itemId=${id}&color=${color}") \
    || die "insert proxy call failed for $id"
  local action
  action=$(echo "$response" | json "
import sys, json
print(json.load(sys.stdin).get('action', ''))
")
  [ -n "$action" ] || die "insert response missing action: $response"
}

item_sink() {
  local id="$1"
  curl -fsS "$SIMULATOR_URL/api/items" | json "
import sys, json
items = [i for i in json.load(sys.stdin) if i['id'] == '$id']
print(items[0]['sinkId'] if items else '')
"
}

open_task_names_for() {
  local id="$1"
  curl -fsS "$FRONTEND_URL/api/user-tasks" | json "
import sys, json
print(','.join(t.get('taskName','') for t in json.load(sys.stdin)
                if t.get('itemIdentifier') == '$id'))
"
}

wait_for_user_task() {
  local id="$1"; local task_name="$2"; local timeout="$3"
  local deadline=$(( $(date +%s) + timeout ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    local tasks
    tasks=$(open_task_names_for "$id")
    case ",$tasks," in *",$task_name,"*) return 0 ;; esac
    sleep 2
  done
  fail "Timed out (${timeout}s) waiting for user task '$task_name' on $id (saw: ${tasks:-none})"
  return 1
}

complete_qc() {
  local id="$1"; local passed="$2"
  curl -fsS -X POST \
    "$FRONTEND_URL/api/qc/check-quality/complete?itemId=${id}&passed=${passed}" \
    >/dev/null
}

mark_removed() {
  local id="$1"
  curl -fsS -X DELETE "$FRONTEND_URL/api/simulator/items/${id}" >/dev/null
  curl -fsS -X POST \
    "$FRONTEND_URL/api/manual-task/complete?itemId=${id}&taskName=Remove+Item+From+Factory" \
    >/dev/null
}

wait_for_sink_prefix() {
  local id="$1"; local prefix="$2"; local timeout="$3"
  local deadline=$(( $(date +%s) + timeout ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    local sink
    sink=$(item_sink "$id")
    case "$sink" in ${prefix}*) return 0 ;; esac
    sleep 2
  done
  fail "Timed out (${timeout}s) waiting for $id to reach ${prefix}* (last seen: ${sink:-gone})"
  return 1
}

wait_for_item_gone() {
  local id="$1"; local timeout="$2"
  local deadline=$(( $(date +%s) + timeout ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    local sink
    sink=$(item_sink "$id")
    [ -z "$sink" ] && return 0
    sleep 2
  done
  fail "Timed out (${timeout}s) waiting for $id to disappear from simulator (still at $sink)"
  return 1
}

camunda_open_task_count() {
  local engine_url="$1"; local item_id="$2"
  curl -fsS "${engine_url}/engine-rest/task?processInstanceBusinessKey=${item_id}" \
    | json "import sys, json; print(len(json.load(sys.stdin)))"
}

dashboard_value() {
  local jpath="$1"
  curl -fsS "$FRONTEND_URL/api/dashboard/metrics" \
    | json "import sys, json; m=json.load(sys.stdin); print($jpath)"
}

# ----- test cases --------------------------------------------------------

accept_path() {
  local id="e2e-accept-$(date +%s)"
  log "=== ACCEPT path: $id (BLUE) ==="

  log "  [1] Create order"
  create_order "$id" "blue" "PASS"
  wait_for_user_task "$id" "Insert Item" 30 \
    && pass "Insert Item user task issued"

  log "  [2] Insert into intake via /api/simulator/items"
  insert_item "$id" "BLUE"
  [ "$(item_sink "$id")" = "SINK-I1" ] \
    && pass "Item landed in SINK-I1" \
    || fail "Item not at SINK-I1 after insert (at: $(item_sink "$id"))"

  log "  [3] Wait for Check Quality user task (≤${PIPELINE_TIMEOUT}s)"
  wait_for_user_task "$id" "Check Quality" "$PIPELINE_TIMEOUT" \
    && pass "Check Quality user task issued"
  [ "$(item_sink "$id")" = "SM-I" ] \
    && pass "Item at SM-I waiting for QC" \
    || fail "Item not at SM-I (at: $(item_sink "$id"))"

  log "  [4] Click Pass via /api/qc/check-quality/complete"
  complete_qc "$id" "true"
  wait_for_sink_prefix "$id" "SINK-S" "$SORTER_TIMEOUT" \
    && pass "Item sorted to a SINK-S* shipping bin ($(item_sink "$id"))"

  log "  [5] No open QC Camunda tasks should remain for this item"
  local n; n=$(camunda_open_task_count "$QC_ENGINE_URL" "$id")
  [ "$n" = "0" ] \
    && pass "qc-service has 0 open tasks for $id" \
    || fail "qc-service still has $n task(s) for $id"
}

reject_path() {
  local id="e2e-reject-$(date +%s)"
  log "=== REJECT path: $id (RED) ==="

  log "  [1] Create order"
  create_order "$id" "red" "FAIL"
  wait_for_user_task "$id" "Insert Item" 30 \
    && pass "Insert Item user task issued"

  log "  [2] Insert into intake"
  insert_item "$id" "RED"

  log "  [3] Wait for Check Quality (≤${PIPELINE_TIMEOUT}s)"
  wait_for_user_task "$id" "Check Quality" "$PIPELINE_TIMEOUT" \
    && pass "Check Quality user task issued"

  log "  [4] Click Reject"
  complete_qc "$id" "false"
  wait_for_user_task "$id" "Remove Item From Factory" 60 \
    && pass "Remove Item From Factory task issued by order-orchestrator"

  log "  [5] Click 'Mark as removed'"
  mark_removed "$id"
  wait_for_item_gone "$id" 10 \
    && pass "Item removed from simulator"

  local n; n=$(camunda_open_task_count "$ORDER_ENGINE_URL" "$id")
  [ "$n" = "0" ] \
    && pass "order-orchestrator has 0 open tasks for $id" \
    || fail "order-orchestrator still has $n task(s) for $id"
}

verify_dashboard() {
  log "=== Dashboard verification ==="
  local qc_total qc_passed qc_rejected manuf_total
  qc_total=$(dashboard_value "m['qcRejectedRate']['totalCount']")
  qc_passed=$(dashboard_value "m['qcRejectedRate']['passedCount']")
  qc_rejected=$(dashboard_value "m['qcRejectedRate']['rejectedCount']")
  manuf_total=$(dashboard_value "m['manufacturingFailureRate']['totalCount']")

  log "  QC total=$qc_total passed=$qc_passed rejected=$qc_rejected, manuf total=$manuf_total"

  [ "$qc_total" -ge 2 ] \
    && pass "Dashboard QC totalCount ≥ 2 ($qc_total)" \
    || fail "Dashboard QC totalCount < 2 ($qc_total)"
  [ "$qc_passed" -ge 1 ] \
    && pass "Dashboard QC passedCount ≥ 1 ($qc_passed)" \
    || fail "Dashboard QC passedCount < 1 ($qc_passed)"
  [ "$qc_rejected" -ge 1 ] \
    && pass "Dashboard QC rejectedCount ≥ 1 ($qc_rejected)" \
    || fail "Dashboard QC rejectedCount < 1 ($qc_rejected)"
  [ "$manuf_total" -ge 2 ] \
    && pass "Dashboard manuf totalCount ≥ 2 ($manuf_total)" \
    || fail "Dashboard manuf totalCount < 2 ($manuf_total)"
}

# ----- main --------------------------------------------------------------

main() {
  wait_for_stack
  accept_path
  reject_path
  verify_dashboard

  echo
  log "Results: ${PASS_COUNT} passed, ${FAIL_COUNT} failed"
  [ "$FAIL_COUNT" -eq 0 ] || exit 1
}

main "$@"
