<#!
.SYNOPSIS
Sends a high-priority, data-only mock incoming-call message through FCM HTTP v1.

.EXAMPLE
.\scripts\send-incoming-call.ps1 -DeviceToken 'YOUR_FCM_TOKEN'

.EXAMPLE
.\scripts\send-incoming-call.ps1 -DeviceToken 'YOUR_FCM_TOKEN' -Caller 'Alice Example' -CallId 'demo-001'
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$DeviceToken,

    [ValidateNotNullOrEmpty()]
    [string]$ProjectId = 'mobile-1b887',

    [ValidateNotNullOrEmpty()]
    [string]$Caller = 'Alice Example',

    [ValidateNotNullOrEmpty()]
    [string]$CallId = "mock-call-$(Get-Date -Format 'yyyyMMddHHmmss')"
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command gcloud -ErrorAction SilentlyContinue)) {
    throw 'Google Cloud CLI (gcloud) is required. Install it, then run: gcloud auth application-default login'
}

$accessToken = (& gcloud auth application-default print-access-token).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($accessToken)) {
    throw 'Could not obtain an application-default access token. Run: gcloud auth application-default login'
}

$payload = @{
    message = @{
        token = $DeviceToken
        data = @{
            type    = 'incoming_call'
            call_id = $CallId
            caller  = $Caller
        }
        android = @{
            priority = 'high'
        }
    }
} | ConvertTo-Json -Depth 6 -Compress

$response = Invoke-RestMethod `
    -Method Post `
    -Uri "https://fcm.googleapis.com/v1/projects/$ProjectId/messages:send" `
    -Headers @{ Authorization = "Bearer $accessToken" } `
    -ContentType 'application/json' `
    -Body $payload

Write-Host "Incoming call push sent for '$Caller' (call ID: $CallId)." -ForegroundColor Green
$response
