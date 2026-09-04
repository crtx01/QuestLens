param(
    [ValidateSet('Open', 'Close', 'Status')][string]$Action = 'Status',
    [Parameter(Mandatory = $true)][string]$Device
)
$ErrorActionPreference = 'Stop'
switch ($Action) {
    'Open' {
        & adb -s $Device shell am broadcast --allow-background-activity-starts --user 0 -n dev.questlens/.DebugFlowReceiver -a dev.questlens.debug.OPEN
    }
    'Close' {
        & adb -s $Device shell am broadcast --user 0 -n dev.questlens/.DebugFlowReceiver -a dev.questlens.debug.CLOSE
    }
    'Status' {
        & adb -s $Device shell dumpsys media_projection
        & adb -s $Device shell dumpsys activity services dev.questlens
        & adb -s $Device logcat -d -t 1200 -v threadtime QuestLens:I AndroidRuntime:E '*:S'
    }
}
if ($LASTEXITCODE -ne 0) { throw "ADB failed ($LASTEXITCODE). Check the connection." }
