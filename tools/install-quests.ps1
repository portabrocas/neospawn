<#
Installs the NeoSpawnPack quest book (modpack\ftbquests\quests, from tools\gen_quests.py) into a Prism instance's
config\ftbquests\quests. The instance's old quest book is moved to config\ftbquests\quests.bak-<time>, not deleted.
Needs FTB Quests, FTB Library and FTB Teams in the instance. Run with Minecraft closed:

  powershell -ExecutionPolicy Bypass -File tools\install-quests.ps1 [-Instance NeoSpawnPack]
#>
param([string]$Instance = 'NeoSpawnPack')
$ErrorActionPreference = 'Stop'

$instancesDir = if ($env:PRISM_INSTANCES) { $env:PRISM_INSTANCES } else { Join-Path $env:APPDATA 'PrismLauncher\instances' }
$game = Join-Path $instancesDir "$Instance\minecraft"
if (-not (Test-Path $game)) { throw "No Prism instance at $game. Pass -Instance <name>." }
if (Get-Process -Name javaw, java -ErrorAction SilentlyContinue) {
    throw 'Java is running. Close Minecraft first: FTB Quests rewrites its files when the game saves.'
}
foreach ($mod in 'ftb-quests', 'ftb-library', 'ftb-teams') {
    if (-not (Get-ChildItem (Join-Path $game 'mods') -Filter "$mod-*.jar" -ErrorAction SilentlyContinue)) {
        Write-Warning "$mod is not in $Instance's mods folder; add it in Prism (Edit > Mods > Download mods)."
    }
}

$source = Join-Path $PSScriptRoot '..\modpack\ftbquests\quests'
if (-not (Test-Path (Join-Path $source 'data.snbt'))) { throw 'No quest book yet: run  py tools\gen_quests.py  first.' }
$target = Join-Path $game 'config\ftbquests\quests'
if (Test-Path $target) {
    $backup = "$target.bak-$(Get-Date -Format yyyyMMdd-HHmmss)"
    Move-Item $target $backup
    Write-Host "Old quest book moved to $backup"
}
New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
Copy-Item $source $target -Recurse
$chapters = (Get-ChildItem (Join-Path $target 'chapters') -Filter *.snbt).Count
Write-Host "Installed the NeoSpawnPack quest book ($chapters chapters) into $Instance."
