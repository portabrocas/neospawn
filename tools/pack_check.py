"""Check a mods folder for jars that will not load on NeoForge 21.1.259 / Minecraft 1.21.1 / Java 21.

Reports Fabric- or Quilt-only jars (FABRIC), classes compiled for a newer Java (JAVA), missing required
dependencies (MISSING), and version ranges that look unmet (RANGE). Nested jar-in-jar mods count as installed.

Usage: python3 tools/pack_check.py <mods dir> [<more dirs>...]

RANGE lines are hints only: some mods declare minecraft "[1.21,1.21.1)" and still load on 1.21.1, and
versions like "1.21.1-2.2.0" are compared loosely. MISSING, JAVA and FABRIC lines are real problems.
"""
import io
import os
import re
import sys
import zipfile

PROVIDED = {"minecraft": "1.21.1", "neoforge": "21.1.259", "javafml": "4", "forge": "21.1.259"}
JAVA_21 = 65  # class file major version


def ver(s):
    return tuple(int(x) for x in re.findall(r"\d+", s.split("-")[0].split("+")[0])[:4]) or (0,)


def in_range(v, r):
    r = r.replace(" ", "")
    if r in ("", "*"):
        return True
    parts = re.findall(r"[\[(][^\])]*[\])]", r)
    if not parts:
        return ver(v) >= ver(r)  # a bare version means "at least"
    for part in parts:
        lo, comma, hi = part[1:-1].partition(",")
        if not comma:
            if ver(v) == ver(lo):
                return True
            continue
        ok = True
        if lo:
            ok &= ver(v) >= ver(lo) if part[0] == "[" else ver(v) > ver(lo)
        if hi:
            ok &= ver(v) <= ver(hi) if part[-1] == "]" else ver(v) < ver(hi)
        if ok:
            return True
    return False


def toml_text(z, name):
    lines = z.read(name).decode("utf8", "ignore").splitlines()
    return "\n".join("" if l.strip().startswith("#") else l.split(" #")[0] for l in lines)


def scan(z, jar, provided, deps, problems, nested=False):
    names = z.namelist()
    for n in ("META-INF/neoforge.mods.toml", "META-INF/mods.toml"):
        if n not in names:
            continue
        for block in re.split(r"^\s*\[\[", toml_text(z, n), flags=re.M):
            head = block.split("]]")[0].strip()
            mod_id = re.search(r'modId\s*=\s*"([^"]+)"', block)
            if head == "mods" and mod_id:
                v = re.search(r'version\s*=\s*"([^"]+)"', block)
                provided.setdefault(mod_id.group(1), v.group(1) if v and "${" not in v.group(1) else "999")
            elif head.startswith("dependencies.") and mod_id:
                typ = re.search(r'type\s*=\s*"([^"]+)"', block)
                mandatory = re.search(r"mandatory\s*=\s*(true|false)", block)
                required = typ.group(1).lower() == "required" if typ else bool(mandatory and mandatory.group(1) == "true")
                rng = re.search(r'versionRange\s*=\s*"([^"]*)"', block)
                if required:
                    deps.append((jar, head.split(".", 1)[1], mod_id.group(1), rng.group(1) if rng else ""))
    has_toml = any(n.endswith("mods.toml") for n in names)
    if not nested and not has_toml:
        if "fabric.mod.json" in names:
            problems.append(f"FABRIC  {jar}")
        elif "quilt.mod.json" in names:
            problems.append(f"QUILT   {jar}")
    newest = 0
    for n in names:
        if n.endswith(".class") and not n.startswith("META-INF/versions/"):
            head = z.read(n)[:8]
            if head[:4] == b"\xca\xfe\xba\xbe":
                newest = max(newest, int.from_bytes(head[6:8], "big"))
    if newest > JAVA_21:
        problems.append(f"JAVA{newest - 44}  {jar}")
    for n in names:
        if n.startswith("META-INF/jarjar/") and n.endswith(".jar"):
            try:
                scan(zipfile.ZipFile(io.BytesIO(z.read(n))), f"{jar} > {os.path.basename(n)}", provided, deps, problems, True)
            except zipfile.BadZipFile:
                pass


def main(dirs):
    provided, deps, problems = dict(PROVIDED), [], []
    for d in dirs:
        for f in sorted(os.listdir(d)):
            if f.endswith(".jar"):
                scan(zipfile.ZipFile(os.path.join(d, f)), f, provided, deps, problems)
    for jar, owner, dep, rng in deps:
        if dep not in provided:
            problems.append(f"MISSING {dep} {rng}  <- {owner} ({jar})")
        elif not in_range(provided[dep], rng):
            problems.append(f"RANGE   {dep} has {provided[dep]}, wants {rng}  <- {owner} ({jar})")
    print("\n".join(problems) or "no problems found")


if __name__ == "__main__":
    main(sys.argv[1:])
