#!/usr/bin/env python3
"""Validate/plan by default. Only --execute may read a token or POST a release.

API schema: https://support.curseforge.com/support/solutions/articles/9000197321
Version IDs are resolved from the SAME legacy API used for uploading, never Core IDs.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import urllib.error
import urllib.request
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BASE = "https://minecraft.curseforge.com"
PROJECT = "1653941"
RELATIONS = ROOT / "docs/release/curseforge-relations.json"
# IDs from the project's recorded dependencies; slug/ID pairs cross-checked
# against CFWidget's public mirror. Official pages require separate dashboard review.
REVIEWED_PROJECTS = {
    "289712": ("tetra", "requiredDependency"),
    "313970": ("apotheosis", "requiredDependency"),
    "283644": ("placebo", "requiredDependency"),
    "898963": ("apothic-attributes", "requiredDependency"),
    "670545": ("tetrawear", "optionalDependency"),
    "688768": ("create-enchantment-industry", "optionalDependency"),
    "929534": ("tetracelium", "optionalDependency"),
    "1613955": ("tetra-insight", "optionalDependency"),
}


def release_type(version):
    lower = version.lower()
    return "alpha" if "alpha" in lower else "beta" if "beta" in lower else "release"


def prepare(jar, version, requested_type="auto", game_names=None):
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:[-.][A-Za-z0-9]+)*", version):
        raise ValueError("Invalid version")
    jar = Path(jar)
    if not game_names and game_names is not None:
        raise ValueError("At least one game-version name is required")
    if jar.name != f"tetra_apothic_link-{version}.jar":
        raise ValueError("Artifact filename must match the requested version (not sources)")
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    with zipfile.ZipFile(jar) as archive:
        metadata = archive.read("META-INF/mods.toml").decode("utf-8")
        if f'version="{version}"' not in metadata:
            raise ValueError("Requested version differs from the actual JAR")
        if not re.search(r'modId="tetrawear"\s+mandatory=false', metadata):
            raise ValueError("Tetrawear must be optional in the actual JAR")
        for entry in ("tetra_apothic_link.mixins.json", "tetra_apothic_link.refmap.json"):
            json.loads(archive.read(entry))
        if any(n.endswith(".jar") for n in archive.namelist()):
            raise ValueError("Unexpected bundled dependency JAR")
    kind = release_type(version) if requested_type == "auto" else requested_type
    if kind not in ("alpha", "beta", "release"):
        raise ValueError("Invalid release type")
    if release_type(version) != "release" and kind == "release":
        raise ValueError("A prerelease must not be uploaded as a stable release")
    changelog = ROOT / f"docs/release/curseforge-changelog-{version}.md"
    text = changelog.read_text(encoding="utf-8")
    relations = json.loads(RELATIONS.read_text(encoding="utf-8"))
    projects = relations["projects"]
    allowed = {"requiredDependency", "optionalDependency", "embeddedLibrary", "incompatible", "tool"}
    ids = set()
    for dependency in projects:
        if not dependency.get("slug") or dependency.get("type") not in allowed:
            raise ValueError("Invalid project relation")
        pid = str(dependency["projectID"])
        if pid in ids:
            raise ValueError("Duplicate project relation")
        if not pid.isdigit() or REVIEWED_PROJECTS.get(pid) != (dependency["slug"], dependency["type"]):
            raise ValueError("Unreviewed or mismatched project slug/ID/type")
        ids.add(pid)
    required_ids = {pid for pid, pair in REVIEWED_PROJECTS.items() if pair[1] == "requiredDependency"}
    if not required_ids.issubset(ids):
        raise ValueError("Required project relations are missing")
    tetrawear = [p for p in projects if str(p["projectID"]) == "670545"]
    if len(tetrawear) != 1 or tetrawear[0]["type"] != "optionalDependency":
        raise ValueError("CurseForge Tetrawear relation must be optional")
    payload = {"changelog": text, "changelogType": "markdown",
               "displayName": f"Tetra Apothic Link {version}",
               "gameVersionNames": game_names or ["1.20.1", "Forge", "Java 17"],
               "releaseType": kind, "relations": relations}
    return {"projectID": PROJECT, "artifact": str(jar.resolve()), "size": jar.stat().st_size,
            "sha256": digest, "metadata": payload}


class SameHostRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        # Never forward publishing credentials, change POST semantics or follow
        # an HTTPS downgrade. A changed endpoint needs explicit human review.
        raise ValueError("Publishing API redirects are refused; verify the endpoint before retrying")


def resolve_versions(names, token, opener):
    request = urllib.request.Request(BASE + "/api/game/versions", headers={"X-Api-Token": token, "Accept": "application/json"})
    with opener.open(request, timeout=60) as response:
        versions = json.loads(response.read())
    if not isinstance(versions, list):
        raise ValueError("Unexpected legacy game-version response")
    def normalized(value):
        return re.sub(r"[\s_-]", "", value).lower()
    resolved = []
    for name in names:
        matches = {int(v["id"]) for v in versions if normalized(str(v.get("name", ""))) == normalized(name)}
        if len(matches) != 1:
            raise ValueError(f"Legacy game version {name!r} is missing/ambiguous; refusing upload")
        resolved.append(matches.pop())
    return resolved


def execute(plan, expected_sha256):
    if not expected_sha256 or plan["sha256"].lower() != expected_sha256.lower():
        raise ValueError("--execute requires a matching --expected-sha256")
    token = os.environ.get("CURSEFORGE_TOKEN", "")
    if not token:
        raise ValueError("CURSEFORGE_TOKEN is required only for an actual upload")
    opener = urllib.request.build_opener(SameHostRedirect())
    payload = dict(plan["metadata"])
    payload["gameVersions"] = resolve_versions(payload.pop("gameVersionNames"), token, opener)
    print("Resolved legacy gameVersions:", payload["gameVersions"])
    boundary = uuid.uuid4().hex
    crlf = b"\r\n"
    body = b"--" + boundary.encode() + crlf
    body += b'Content-Disposition: form-data; name="metadata"' + crlf + crlf
    body += json.dumps(payload).encode() + crlf
    body += b"--" + boundary.encode() + crlf
    jar = Path(plan["artifact"])
    data = jar.read_bytes()
    if hashlib.sha256(data).hexdigest() != plan["sha256"]:
        raise ValueError("Artifact changed after validation")
    body += f'Content-Disposition: form-data; name="file"; filename="{jar.name}"'.encode() + crlf
    body += b"Content-Type: application/java-archive" + crlf + crlf + data + crlf
    body += b"--" + boundary.encode() + b"--" + crlf
    request = urllib.request.Request(BASE + f"/api/projects/{PROJECT}/upload-file", data=body,
        headers={"X-Api-Token": token, "Content-Type": f"multipart/form-data; boundary={boundary}"}, method="POST")
    # Upload is non-idempotent: deliberately do not retry POST on timeouts/failures.
    with opener.open(request, timeout=180) as response:
        result = json.loads(response.read())
    if not result.get("id"):
        raise ValueError("Upload response did not contain a file ID; check the author dashboard before retrying")
    receipt = {"projectID": PROJECT, "fileID": result["id"], "sha256": plan["sha256"],
               "displayName": payload["displayName"], "releaseType": payload["releaseType"]}
    receipt_path = ROOT / "build/release/curseforge-upload-receipt.json"
    receipt_path.parent.mkdir(parents=True, exist_ok=True)
    receipt_path.write_text(json.dumps(receipt, indent=2) + "\n", encoding="utf-8")
    print("UPLOAD OK fileId=" + str(result["id"]))
    print("Do not rerun this upload; verify the file ID in the author dashboard first.")
    return result


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", required=True)
    parser.add_argument("--jar", required=True)
    parser.add_argument("--release-type", default="auto", choices=("auto", "alpha", "beta", "release"))
    parser.add_argument("--game-versions", default="1.20.1,Forge,Java 17", help="Legacy version names, not IDs")
    parser.add_argument("--output", default="build/release/curseforge-plan.json")
    parser.add_argument("--execute", action="store_true", help="Actually upload; default is an offline plan")
    parser.add_argument("--expected-sha256")
    args = parser.parse_args(argv)
    plan = prepare(args.jar, args.version, args.release_type, [n.strip() for n in args.game_versions.split(",") if n.strip()])
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(plan, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{'EXECUTE' if args.execute else 'OFFLINE PLAN ONLY'} project={PROJECT} releaseType={plan['metadata']['releaseType']} size={plan['size']} sha256={plan['sha256']}")
    print("Plan:", output)
    if args.execute:
        execute(plan, args.expected_sha256)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (ValueError, OSError, KeyError, zipfile.BadZipFile) as exc:
        # Never dump request headers, token values, remote error bodies or upload bytes.
        print("Release preparation failed:", type(exc).__name__, str(exc), file=sys.stderr)
        raise SystemExit(1)
