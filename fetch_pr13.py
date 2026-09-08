import urllib.request
import json
import sys

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}/pulls/13"
req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
with urllib.request.urlopen(req) as resp:
    pr = json.loads(resp.read().decode())
    sha = pr['head']['sha']
    
    commit_url = f"https://api.github.com/repos/{repo}/commits/{sha}/check-runs"
    req_checks = urllib.request.Request(commit_url, headers={'Accept': 'application/vnd.github.v3+json'})
    with urllib.request.urlopen(req_checks) as resp_checks:
        data = json.loads(resp_checks.read().decode())
        for run in data.get('check_runs', []):
            if run['conclusion'] == 'failure':
                run_id = run['id']
                annot_url = f"https://api.github.com/repos/{repo}/check-runs/{run_id}/annotations"
                req_annot = urllib.request.Request(annot_url, headers={'Accept': 'application/vnd.github.v3+json'})
                with urllib.request.urlopen(req_annot) as resp_annot:
                    annots = json.loads(resp_annot.read().decode())
                    for ann in annots:
                        print(f"Annotation: {ann.get('message')}")
