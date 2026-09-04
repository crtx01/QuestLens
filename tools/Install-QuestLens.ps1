param(
    [string]$Apk,
    [string]$Device,
    [switch]$SkipHeadsetShortcut
)
$ErrorActionPreference = 'Stop'

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCommand) { $questAdb = $adbCommand.Source }
else {
    $questAdb = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
    if (-not (Test-Path -LiteralPath $questAdb)) {
        throw 'Install Android platform-tools and add adb to PATH, then run this script again.'
    }
}
if (-not $Apk) {
    $candidates = @(
        (Join-Path $PSScriptRoot 'QuestLens-0.4.2-preview.apk'),
        (Join-Path $PSScriptRoot '../QuestLens-0.4.2-preview.apk'),
        (Join-Path $PSScriptRoot '../app/build/outputs/apk/debug/app-debug.apk')
    )
    $Apk = $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
}
if (-not $Apk -or -not (Test-Path -LiteralPath $Apk)) {
    throw 'APK not found. Run this script with -Apk followed by the APK path.'
}
if (-not $Device) {
    $connected = @(& $questAdb devices | ForEach-Object {
        if ($_ -match '^(\S+)\s+device$') { $Matches[1] }
    })
    if ($connected.Count -ne 1) {
        throw 'Connect and authorize one headset, or select it with -Device SERIAL. Run adb devices to check.'
    }
    $Device = $connected[0]
}
function Invoke-QuestAdb([string[]]$Arguments) {
    & $questAdb -s $Device @Arguments
    if ($LASTEXITCODE -ne 0) { throw "ADB failed (exit $LASTEXITCODE). Check the headset connection." }
}
Write-Host 'Installing QuestLens. Existing settings are kept when the signing key matches.'
Invoke-QuestAdb -Arguments @('install', '-r', (Resolve-Path -LiteralPath $Apk).Path)
if (-not $SkipHeadsetShortcut) {
    Write-Host 'Granting one-time setup permission for the optional local headset shortcut.'
    Invoke-QuestAdb -Arguments @('shell', 'pm', 'grant', 'dev.questlens', 'android.permission.WRITE_SECURE_SETTINGS')
    # Open the guide before restarting adbd, which can interrupt this transport.
    Invoke-QuestAdb -Arguments @('shell', 'am', 'start', '-n', 'dev.questlens/.MainActivity')
    Invoke-QuestAdb -Arguments @('tcpip', '5555')
    Write-Host 'On the headset: follow the first-run guide, allow window opening, then CONNECT.'
    Write-Host 'Accept the network and app-key prompts and select Always allow.'
} else {
    Invoke-QuestAdb -Arguments @('shell', 'am', 'start', '-n', 'dev.questlens/.MainActivity')
}
Write-Host 'Start and allow capture on the headset. See docs/INSTALL.md for details.'
