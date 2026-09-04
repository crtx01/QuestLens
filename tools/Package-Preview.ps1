param([string]$Version = '0.4.3-preview')
$ErrorActionPreference = 'Stop'
$questRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$questOutput = Join-Path $questRoot "artifacts/publication/$Version"
$questApk = Join-Path $questRoot 'app/build/outputs/apk/debug/app-debug.apk'
$questDependency = Join-Path $questRoot 'artifacts/dependency-source/spake2-java-2.2.1'
if ($Version -notmatch '^[0-9A-Za-z.-]+$') { throw 'Invalid version.' }
if (-not (Test-Path -LiteralPath $questApk)) { throw 'Build the preview APK first.' }
if (-not (Test-Path -LiteralPath (Join-Path $questDependency 'android/src/main/cpp/spake2-c/LICENSE'))) {
    throw 'Fetch spake2-java tag 2.2.1 recursively into artifacts/dependency-source/spake2-java-2.2.1 first.'
}
New-Item -ItemType Directory -Force -Path $questOutput | Out-Null
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Write-SourceZip([string]$Root, [string[]]$Files, [string]$Path, [string]$Prefix) {
    $archive = [IO.Compression.ZipArchive]::new([IO.File]::Open($Path, [IO.FileMode]::Create), [IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($relative in ($Files | Sort-Object -Unique)) {
            $relative = $relative.Replace('\', '/')
            if ($relative -match '(^|/)(\.git|\.gradle|build)(/|$)' -or $relative.Contains('..')) {
                throw "Unexpected source path: $relative"
            }
            $full = [IO.Path]::GetFullPath((Join-Path $Root $relative))
            if (-not $full.StartsWith($Root.TrimEnd('\') + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Source path escapes root.' }
            [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $full, "$Prefix/$relative", [IO.Compression.CompressionLevel]::Optimal) | Out-Null
        }
    } finally { $archive.Dispose() }
}

$projectFiles = @(& git -C $questRoot ls-files --cached --others --exclude-standard)
if ($LASTEXITCODE -ne 0) { throw 'Could not enumerate source files.' }
foreach ($relative in $projectFiles) {
    if ($relative -match '(^artifacts/|local\.properties$|\.(jks|keystore|p12|key)$|(^|/)\.env$|BUILD_REPORT\.md$|TEST_PLAN\.md$)') {
        throw "Private or generated file in source inventory: $relative"
    }
}
Write-SourceZip -Root $questRoot -Files $projectFiles -Path (Join-Path $questOutput "QuestLens-$Version-source.zip") -Prefix "QuestLens-$Version"

$dependencyFiles = @(Get-ChildItem -LiteralPath $questDependency -File -Recurse -Force |
    Where-Object { $_.FullName -notmatch '[\\/]\.git([\\/]|$)' } |
    ForEach-Object { $_.FullName.Substring($questDependency.Length + 1) })
Write-SourceZip -Root $questDependency -Files $dependencyFiles -Path (Join-Path $questOutput 'spake2-java-2.2.1-source.zip') -Prefix 'spake2-java-2.2.1'
Copy-Item -LiteralPath $questApk -Destination (Join-Path $questOutput "QuestLens-$Version.apk") -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Install-QuestLens.ps1') -Destination $questOutput -Force
Copy-Item -LiteralPath (Join-Path $questRoot 'docs/INSTALL.md') -Destination (Join-Path $questOutput 'INSTALL.md') -Force
$sumLines = Get-ChildItem -LiteralPath $questOutput -File |
    Where-Object Name -ne 'SHA256SUMS.txt' | Sort-Object Name |
    ForEach-Object { "{0}  {1}" -f (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant(), $_.Name }
[IO.File]::WriteAllLines((Join-Path $questOutput 'SHA256SUMS.txt'), [string[]]$sumLines, [Text.UTF8Encoding]::new($false))
Write-Host "Preview bundle prepared: $questOutput"
