package com.playoutedge.server.views.settings

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*

fun HTML.apiDocsView(session: AdminClaims) {
    adminLayout(
        title = "API Documentation",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/settings"
    ) {
        breadcrumb("Settings" to "/admin/settings", "API Documentation" to null)
        pageHeader(title = "API Documentation", subtitle = "Reference for the Playout Edge REST API")

        // Authentication section
        div("card mb-4") {
            div("card-header") { h3 { +"Authentication" } }
            div("card-body") {
                p { +"All API requests require a Bearer token in the Authorization header. You can create API keys in the " }
                a(href = "/admin/settings/api-keys") { +"API Keys" }
                p { +" settings page." }

                h4("mt-3") { +"Header Format" }
                pre { code { +"Authorization: Bearer <your-api-key>" } }

                h4("mt-3") { +"Example: Authenticating with curl" }
                div("code-block") {
                    pre { code { +"""curl -H "Authorization: Bearer pk_abc123def456..." \
  https://your-domain.com/api/assets""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -H "Authorization: Bearer pk_abc123def456..." https://your-domain.com/api/assets'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }

                h4("mt-3") { +"Example: Authenticating with JavaScript" }
                div("code-block") {
                    pre { code { +"""const response = await fetch('/api/assets', {
  headers: {
    'Authorization': 'Bearer pk_abc123def456...',
    'Content-Type': 'application/json'
  }
});
const data = await response.json();""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText(`const response = await fetch('/api/assets', {\n  headers: {\n    'Authorization': 'Bearer pk_abc123def456...',\n    'Content-Type': 'application/json'\n  }\n});\nconst data = await response.json();`); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }

                h4("mt-3") { +"Error Responses" }
                p { +"All endpoints return standard HTTP status codes. Error responses include a JSON body:" }
                pre { code { +"""{ "error": "Unauthorized", "message": "Invalid or expired API key" }""" } }
            }
        }

        // Assets endpoints
        div("card mb-4") {
            div("card-header") { h3 { +"Assets" } }
            div("card-body") {
                table("table") {
                    thead { tr { th { +"Method" }; th { +"Path" }; th { +"Description" }; th { +"Scope" } } }
                    tbody {
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/assets" }; td { +"List all assets" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-primary") { +"POST" } }; td { +"/api/assets" }; td { +"Upload new asset" }; td { span("badge badge-gray badge-plain") { +"write" } } }
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/assets/:id" }; td { +"Get asset details" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-danger") { +"DELETE" } }; td { +"/api/assets/:id" }; td { +"Archive asset" }; td { span("badge badge-gray badge-plain") { +"write" } } }
                    }
                }

                h4("mt-3") { +"List Assets" }
                div("code-block") {
                    pre { code { +"""curl -H "Authorization: Bearer pk_..." \
  "https://your-domain.com/api/assets?page=1&limit=20"

# Response:
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "promo-video.mp4",
      "type": "VIDEO",
      "sizeBytes": 15728640,
      "duration": 30,
      "createdAt": "2025-01-15T10:30:00Z"
    }
  ],
  "total": 42,
  "page": 1,
  "limit": 20
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -H "Authorization: Bearer pk_..." "https://your-domain.com/api/assets?page=1&limit=20"'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }

                h4("mt-3") { +"Upload Asset" }
                div("code-block") {
                    pre { code { +"""curl -X POST \
  -H "Authorization: Bearer pk_..." \
  -F "file=@/path/to/video.mp4" \
  -F "name=My Video" \
  https://your-domain.com/api/assets

# Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Video",
  "type": "VIDEO",
  "status": "PROCESSING"
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -X POST -H "Authorization: Bearer pk_..." -F "file=@/path/to/video.mp4" -F "name=My Video" https://your-domain.com/api/assets'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }

        // Channels endpoints
        div("card mb-4") {
            div("card-header") { h3 { +"Channels" } }
            div("card-body") {
                table("table") {
                    thead { tr { th { +"Method" }; th { +"Path" }; th { +"Description" }; th { +"Scope" } } }
                    tbody {
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/channels" }; td { +"List all channels" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-primary") { +"POST" } }; td { +"/api/channels" }; td { +"Create channel" }; td { span("badge badge-gray badge-plain") { +"write" } } }
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/channels/:id" }; td { +"Get channel details" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                    }
                }

                h4("mt-3") { +"List Channels" }
                div("code-block") {
                    pre { code { +"""curl -H "Authorization: Bearer pk_..." \
  https://your-domain.com/api/channels

# Response:
{
  "items": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "name": "Lobby Display",
      "resolution": "1920x1080",
      "deviceCount": 3,
      "createdAt": "2025-01-10T08:00:00Z"
    }
  ]
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -H "Authorization: Bearer pk_..." https://your-domain.com/api/channels'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }

        // Devices endpoints
        div("card mb-4") {
            div("card-header") { h3 { +"Devices" } }
            div("card-body") {
                table("table") {
                    thead { tr { th { +"Method" }; th { +"Path" }; th { +"Description" }; th { +"Scope" } } }
                    tbody {
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/devices" }; td { +"List all devices" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/devices/:id" }; td { +"Get device details" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-warning") { +"PUT" } }; td { +"/api/devices/:id" }; td { +"Update device" }; td { span("badge badge-gray badge-plain") { +"write" } } }
                    }
                }

                h4("mt-3") { +"Get Device Status" }
                div("code-block") {
                    pre { code { +"""curl -H "Authorization: Bearer pk_..." \
  https://your-domain.com/api/devices/DEVICE_ID

# Response:
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "name": "Lobby Screen 1",
  "status": "ONLINE",
  "lastHeartbeat": "2025-01-20T14:30:00Z",
  "channelId": "660e8400-e29b-41d4-a716-446655440000",
  "firmwareVersion": "2.1.0"
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -H "Authorization: Bearer pk_..." https://your-domain.com/api/devices/DEVICE_ID'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }

        // Schedules endpoints
        div("card mb-4") {
            div("card-header") { h3 { +"Schedules" } }
            div("card-body") {
                table("table") {
                    thead { tr { th { +"Method" }; th { +"Path" }; th { +"Description" }; th { +"Scope" } } }
                    tbody {
                        tr { td { span("badge badge-success") { +"GET" } }; td { +"/api/schedules/:channelId" }; td { +"Get channel schedule" }; td { span("badge badge-gray badge-plain") { +"read" } } }
                        tr { td { span("badge badge-primary") { +"POST" } }; td { +"/api/schedules/:channelId" }; td { +"Create schedule version" }; td { span("badge badge-gray badge-plain") { +"write" } } }
                    }
                }

                h4("mt-3") { +"Get Channel Schedule" }
                div("code-block") {
                    pre { code { +"""curl -H "Authorization: Bearer pk_..." \
  https://your-domain.com/api/schedules/CHANNEL_ID

# Response:
{
  "channelId": "660e8400-e29b-41d4-a716-446655440000",
  "version": 5,
  "slots": [
    {
      "startTime": "08:00",
      "endTime": "12:00",
      "assetId": "550e8400-e29b-41d4-a716-446655440000",
      "assetName": "Morning Playlist"
    }
  ],
  "publishedAt": "2025-01-20T09:00:00Z"
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -H "Authorization: Bearer pk_..." https://your-domain.com/api/schedules/CHANNEL_ID'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }

        // Webhooks endpoints
        div("card mb-4") {
            div("card-header") { h3 { +"Webhooks" } }
            div("card-body") {
                table("table") {
                    thead { tr { th { +"Method" }; th { +"Path" }; th { +"Description" }; th { +"Scope" } } }
                    tbody {
                        tr { td { span("badge badge-primary") { +"POST" } }; td { +"/api/webhook/:bindingId" }; td { +"Send overlay webhook" }; td { span("badge badge-gray badge-plain") { +"overlay" } } }
                    }
                }

                h4("mt-3") { +"Push Overlay Data" }
                div("code-block") {
                    pre { code { +"""curl -X POST \
  -H "Authorization: Bearer pk_..." \
  -H "Content-Type: application/json" \
  -d '{"title": "Breaking News", "body": "Important update..."}' \
  https://your-domain.com/api/webhook/BINDING_ID

# Response:
{ "status": "ok", "devicesNotified": 3 }""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText('curl -X POST -H "Authorization: Bearer pk_..." -H "Content-Type: application/json" -d \'{"title": "Breaking News", "body": "Important update..."}\' https://your-domain.com/api/webhook/BINDING_ID'); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }

        // Webhook Signature Verification
        div("card mb-4") {
            div("card-header") { h3 { +"Webhook Signature Verification" } }
            div("card-body") {
                p { +"When Playout Edge sends outbound webhooks to your server, each request includes a signature header for verification." }

                h4("mt-3") { +"Headers Included" }
                table("table") {
                    thead { tr { th { +"Header" }; th { +"Description" } } }
                    tbody {
                        tr { td { code { +"X-PlayoutEdge-Signature" } }; td { +"HMAC-SHA256 signature of the request body" } }
                        tr { td { code { +"X-PlayoutEdge-Timestamp" } }; td { +"Unix timestamp of when the webhook was sent" } }
                    }
                }

                h4("mt-3") { +"Verification Steps" }
                ol {
                    li { +"Extract the timestamp and signature from the headers." }
                    li { +"Concatenate the timestamp and request body: timestamp + \".\" + body" }
                    li { +"Compute HMAC-SHA256 using your webhook secret as the key." }
                    li { +"Compare your computed signature with the received signature." }
                    li { +"Reject requests older than 5 minutes to prevent replay attacks." }
                }

                h4("mt-3") { +"Example: Node.js Verification" }
                div("code-block") {
                    pre { code { +"""const crypto = require('crypto');

function verifyWebhook(req, secret) {
  const signature = req.headers['x-playoutedge-signature'];
  const timestamp = req.headers['x-playoutedge-timestamp'];
  const body = JSON.stringify(req.body);

  // Check timestamp freshness (5 min window)
  const age = Math.abs(Date.now() / 1000 - parseInt(timestamp));
  if (age > 300) return false;

  const payload = timestamp + '.' + body;
  const expected = crypto
    .createHmac('sha256', secret)
    .update(payload)
    .digest('hex');

  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expected)
  );
}""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText(`const crypto = require('crypto');\n\nfunction verifyWebhook(req, secret) {\n  const signature = req.headers['x-playoutedge-signature'];\n  const timestamp = req.headers['x-playoutedge-timestamp'];\n  const body = JSON.stringify(req.body);\n  const age = Math.abs(Date.now() / 1000 - parseInt(timestamp));\n  if (age > 300) return false;\n  const payload = timestamp + '.' + body;\n  const expected = crypto.createHmac('sha256', secret).update(payload).digest('hex');\n  return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected));\n}`); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }

                h4("mt-3") { +"Example: Python Verification" }
                div("code-block") {
                    pre { code { +"""import hmac
import hashlib
import time

def verify_webhook(headers, body, secret):
    signature = headers.get('X-PlayoutEdge-Signature')
    timestamp = headers.get('X-PlayoutEdge-Timestamp')

    # Check timestamp freshness
    if abs(time.time() - int(timestamp)) > 300:
        return False

    payload = f"{timestamp}.{body}"
    expected = hmac.new(
        secret.encode(),
        payload.encode(),
        hashlib.sha256
    ).hexdigest()

    return hmac.compare_digest(signature, expected)""" } }
                    button(type = ButtonType.button, classes = "btn btn-sm btn-secondary copy-btn") {
                        attributes["onclick"] = """navigator.clipboard.writeText(`import hmac\nimport hashlib\nimport time\n\ndef verify_webhook(headers, body, secret):\n    signature = headers.get('X-PlayoutEdge-Signature')\n    timestamp = headers.get('X-PlayoutEdge-Timestamp')\n    if abs(time.time() - int(timestamp)) > 300:\n        return False\n    payload = f"{timestamp}.{body}"\n    expected = hmac.new(secret.encode(), payload.encode(), hashlib.sha256).hexdigest()\n    return hmac.compare_digest(signature, expected)`); this.textContent='Copied!'; setTimeout(() => this.textContent='Copy', 2000)"""
                        +"Copy"
                    }
                }
            }
        }
    }
}
