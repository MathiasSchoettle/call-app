#!/usr/bin/env bash

set -Eeuo pipefail

usage() {
    cat <<'EOF'
Usage:
  scripts/send-incoming-call.sh --device-token TOKEN --project-id PROJECT_ID [options]

Options:
  --device-token TOKEN  FCM registration token from the Android app (required)
  --project-id ID       Firebase/Google Cloud project ID (required unless
                        FIREBASE_PROJECT_ID is set)
  --caller NAME         Caller name (default: Alice Example)
  --call-id ID           Mock call ID (default: mock-call-YYYYMMDDHHMMSS)
  -h, --help             Show this help

The script uses Google Cloud application-default credentials. Authenticate once
with:

  gcloud auth application-default login
EOF
}

device_token=''
project_id="${FIREBASE_PROJECT_ID:-}"
caller='Alice Example'
call_id="mock-call-$(date '+%Y%m%d%H%M%S')"

while (($# > 0)); do
    case "$1" in
        --device-token)
            [[ $# -ge 2 ]] || { printf '%s\n' 'Missing value for --device-token.' >&2; usage >&2; exit 2; }
            device_token=$2
            shift 2
            ;;
        --project-id)
            [[ $# -ge 2 ]] || { printf '%s\n' 'Missing value for --project-id.' >&2; usage >&2; exit 2; }
            project_id=$2
            shift 2
            ;;
        --caller)
            [[ $# -ge 2 ]] || { printf '%s\n' 'Missing value for --caller.' >&2; usage >&2; exit 2; }
            caller=$2
            shift 2
            ;;
        --call-id)
            [[ $# -ge 2 ]] || { printf '%s\n' 'Missing value for --call-id.' >&2; usage >&2; exit 2; }
            call_id=$2
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            printf 'Unknown option: %s\n\n' "$1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

if [[ -z "$device_token" ]]; then
    printf '%s\n\n' 'A device token is required.' >&2
    usage >&2
    exit 2
fi

if [[ -z "$project_id" ]]; then
    printf '%s\n\n' 'A project ID is required. Pass --project-id or set FIREBASE_PROJECT_ID.' >&2
    usage >&2
    exit 2
fi

if ! command -v gcloud >/dev/null 2>&1; then
    printf '%s\n' 'Google Cloud CLI (gcloud) is required.' >&2
    printf '%s\n' 'Install it with: brew install --cask google-cloud-sdk' >&2
    printf '%s\n' 'Then authenticate with: gcloud auth application-default login' >&2
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    printf '%s\n' 'curl is required but was not found.' >&2
    exit 1
fi

if ! command -v node >/dev/null 2>&1; then
    printf '%s\n' 'Node.js is required but was not found.' >&2
    printf '%s\n' 'Install Node.js 20 or later before running this project.' >&2
    exit 1
fi

if ! access_token=$(gcloud auth application-default print-access-token); then
    printf '%s\n' 'Could not obtain an application-default access token.' >&2
    printf '%s\n' 'Run: gcloud auth application-default login' >&2
    exit 1
fi
if [[ -z "$access_token" ]]; then
    printf '%s\n' 'Could not obtain an application-default access token.' >&2
    printf '%s\n' 'Run: gcloud auth application-default login' >&2
    exit 1
fi

# Node is already required by this Capacitor project and avoids a jq/python
# dependency while safely escaping caller-provided values for JSON.
payload=$(DEVICE_TOKEN="$device_token" CALLER="$caller" CALL_ID="$call_id" node <<'NODE'
const payload = {
  message: {
    token: process.env.DEVICE_TOKEN,
    data: {
      type: 'incoming_call',
      call_id: process.env.CALL_ID,
      caller: process.env.CALLER,
    },
    android: {
      priority: 'high',
    },
  },
}

process.stdout.write(JSON.stringify(payload))
NODE
)

curl --fail --silent --show-error \
    --request POST \
    --url "https://fcm.googleapis.com/v1/projects/${project_id}/messages:send" \
    --header "Authorization: Bearer ${access_token}" \
    --header 'Content-Type: application/json' \
    --data "$payload"

printf "Incoming call push sent for '%s' (call ID: %s).\n" "$caller" "$call_id" >&2
