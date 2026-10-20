<#
Installs the latest NeoSpawn release into every Prism Launcher instance that has NeoSpawn,
and moves older neospawn-*.jar files out of the way (into mods\.old-neospawn, not deleted).

Run in PowerShell with Minecraft closed:
  irm https://raw.githubusercontent.com/portabrocas/neospawn/main/tools/install-windows.ps1 | iex

Or from a clone:  powershell -ExecutionPolicy Bypass -File tools\install-windows.ps1
Prism installed somewhere else (portable/scoop)? Set $env:PRISM_INSTANCES to its instances folder first.
#>
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$instancesDir = if ($env:PRISM_INSTANCES) { $env:PRISM_INSTANCES } else { Join-Path $env:APPDATA 'PrismLauncher\instances' }
if (-not (Test-Path $instancesDir)) { throw "No Prism instances folder at $instancesDir. Set `$env:PRISM_INSTANCES and run again." }

if (Get-Process -Name javaw, java -ErrorAction SilentlyContinue) {
    throw 'Java is running. Close Minecraft (and Prism''s game window) first, then run this again.'
}

$release = Invoke-RestMethod 'https://api.github.com/repos/portabrocas/neospawn/releases/latest' -Headers @{ 'User-Agent' = 'neospawn-installer' }
$asset = $release.assets | Where-Object { $_.name -like 'neospawn-*.jar' } | Select-Object -First 1
if (-not $asset) { throw "Release $($release.tag_name) has no jar yet. Wait for the GitHub build to finish." }
$jar = Join-Path $env:TEMP $asset.name
Invoke-WebRequest $asset.browser_download_url -OutFile $jar -UseBasicParsing -Headers @{ 'User-Agent' = 'neospawn-installer' }
Write-Host "Downloaded $($asset.name) ($($release.tag_name))"

$installed = 0
Write-Host ''
Write-Host 'Prism instances:'
foreach ($inst in Get-ChildItem $instancesDir -Directory | Where-Object { $_.Name -notlike '_*' }) {
    $mods = @('minecraft\mods', '.minecraft\mods') | ForEach-Object { Join-Path $inst.FullName $_ } |
            Where-Object { Test-Path $_ } | Select-Object -First 1
    $size = '{0:N0} MB' -f ((Get-ChildItem $inst.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object Length -Sum).Sum / 1MB)
    $old = if ($mods) { @(Get-ChildItem $mods -Filter 'neospawn-*.jar') } else { @() }
    if ($old.Count -eq 0) { Write-Host "  $($inst.Name)  [$size]  no NeoSpawn"; continue }

    $backup = Join-Path $mods '.old-neospawn'
    New-Item -ItemType Directory -Force $backup | Out-Null
    foreach ($o in $old) { Move-Item $o.FullName (Join-Path $backup $o.Name) -Force }
    Copy-Item $jar (Join-Path $mods $asset.name) -Force
    $installed++

    $gecko = Get-ChildItem $mods -Filter 'geckolib-neoforge-1.21.1-*.jar' | Select-Object -First 1
    $note = if ($gecko) { "GeckoLib: $($gecko.Name)" } else { 'MISSING GeckoLib: add GeckoLib 4.9+ for NeoForge 1.21.1 to this instance' }
    Write-Host "  $($inst.Name)  [$size]  installed $($asset.name) (moved $($old.Count) old jar(s) to mods\.old-neospawn). $note"
}

if ($installed -eq 0) {
    Write-Host ''
    Write-Host "No instance had NeoSpawn yet. Copy $jar into your instance's minecraft\mods folder."
}
Write-Host ''
Write-Host 'Instances you no longer want: delete them in Prism (right-click > Delete), which also cleans its shortcuts.'
Remove-Item $jar -Force
