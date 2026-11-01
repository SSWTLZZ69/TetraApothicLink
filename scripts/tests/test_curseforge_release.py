import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

SCRIPT = Path(__file__).resolve().parents[1] / 'curseforge_release.py'
spec = importlib.util.spec_from_file_location('curseforge_release', SCRIPT)
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleasePreparationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        docs = self.root / 'docs/release'
        docs.mkdir(parents=True)
        (docs / 'curseforge-changelog-0.2.0-alpha.1.md').write_text('# Alpha\nTetrawear is optional.', encoding='utf-8')
        self.relations = docs / 'curseforge-relations.json'
        self.relations.write_text(json.dumps({'projects': [
            {'slug': slug, 'projectID': pid, 'type': kind}
            for pid, (slug, kind) in release.REVIEWED_PROJECTS.items()]}))
        self.jar = self.root / 'tetra_apothic_link-0.2.0-alpha.1.jar'
        self.make_jar()

    def make_jar(self, optional=True, version='0.2.0-alpha.1'):
        with zipfile.ZipFile(self.jar, 'w') as archive:
            archive.writestr('META-INF/mods.toml', f'version="{version}"\nmodId="tetrawear"\nmandatory={str(not optional).lower()}')
            archive.writestr('tetra_apothic_link.mixins.json', '{}')
            archive.writestr('tetra_apothic_link.refmap.json', '{}')

    def plan(self, **kwargs):
        with patch.object(release, 'ROOT', self.root), patch.object(release, 'RELATIONS', self.relations):
            return release.prepare(self.jar, '0.2.0-alpha.1', **kwargs)

    def test_auto_release_types(self):
        self.assertEqual('alpha', release.release_type('0.2.0-alpha.1'))
        self.assertEqual('beta', release.release_type('0.2.0-beta.1'))
        self.assertEqual('release', release.release_type('0.2.0'))

    def test_offline_plan_never_opens_network_or_requires_token(self):
        with patch.dict(release.os.environ, {}, clear=True), patch.object(release.urllib.request, 'build_opener', side_effect=AssertionError('network')):
            plan = self.plan()
        self.assertEqual('alpha', plan['metadata']['releaseType'])
        tetrawear = next(p for p in plan['metadata']['relations']['projects'] if p['projectID'] == '670545')
        self.assertEqual('optionalDependency', tetrawear['type'])
        self.assertEqual(['1.20.1', 'Forge', 'Java 17'], plan['metadata']['gameVersionNames'])
        self.assertEqual(64, len(plan['sha256']))

    def test_reject_prerelease_as_stable(self):
        with self.assertRaises(ValueError): self.plan(requested_type='release')

    def test_reject_wrong_actual_jar_version(self):
        self.make_jar(version='0.1.0')
        with self.assertRaises(ValueError): self.plan()

    def test_reject_required_tetrawear_in_artifact(self):
        self.make_jar(optional=False)
        with self.assertRaises(ValueError): self.plan()

    def test_reject_required_tetrawear_relation(self):
        self.relations.write_text(json.dumps({'projects': [{'slug': 'tetrawear', 'projectID': '670545', 'type': 'requiredDependency'}]}))
        with self.assertRaises(ValueError): self.plan()

    def test_reject_mismatched_slug_and_missing_required_project(self):
        original = json.loads(self.relations.read_text())
        for mutation in ('slug', 'missing', 'id'):
            data = json.loads(json.dumps(original))
            if mutation == 'slug':
                next(p for p in data['projects'] if p['projectID'] == '670545')['slug'] = 'wrong-project'
            elif mutation == 'missing':
                data['projects'] = [p for p in data['projects'] if p['projectID'] != '289712']
            else:
                data['projects'][0]['projectID'] = 'not-a-number'
            self.relations.write_text(json.dumps(data))
            with self.assertRaises(ValueError): self.plan()

    def test_execute_requires_matching_digest_before_network(self):
        with patch.object(release.urllib.request, 'build_opener', side_effect=AssertionError('network')):
            with self.assertRaises(ValueError): release.execute(self.plan(), 'wrong')

    def test_legacy_version_ids_not_type_ids(self):
        class Response:
            def __enter__(self): return self
            def __exit__(self, *args): pass
            def read(self):
                return json.dumps([{'id': 1, 'gameVersionTypeID': 999, 'name': '1.20.1'},
                                   {'id': 2, 'gameVersionTypeID': 998, 'name': 'Forge'},
                                   {'id': 3, 'gameVersionTypeID': 997, 'name': 'Java17'}]).encode()
        class Opener:
            def open(self, *args, **kwargs): return Response()
        self.assertEqual([1, 2, 3], release.resolve_versions(['1.20.1', 'Forge', 'Java 17'], 'FAKE', Opener()))
        with self.assertRaises(ValueError): release.resolve_versions(['Unknown'], 'FAKE', Opener())

    def test_all_authenticated_redirects_are_rejected(self):
        handler = release.SameHostRedirect()
        for target in ('http://minecraft.curseforge.com/api/game/versions',
                       'https://example.invalid/steal',
                       'https://minecraft.curseforge.com/another-endpoint'):
            for method in ('GET', 'POST'):
                request = release.urllib.request.Request(release.BASE + '/api/test', method=method)
                with self.assertRaises(ValueError):
                    handler.redirect_request(request, None, 302, 'redirect', {}, target)

    def test_duplicate_legacy_name_fails_closed(self):
        class Response:
            def __enter__(self): return self
            def __exit__(self, *args): pass
            def read(self): return b'[{"id":1,"name":"Forge"},{"id":2,"name":"Forge"}]'
        class Opener:
            def open(self, *args, **kwargs): return Response()
        with self.assertRaises(ValueError): release.resolve_versions(['Forge'], 'FAKE', Opener())


if __name__ == '__main__':
    unittest.main()
