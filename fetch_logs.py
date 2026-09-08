import urllib.request
import json
import sys

repo = "rtiqa/rtiqa-ai"

# Let's fetch the failing jobs for PR 14 (Lint failure)
# We can use the check-runs API and get the text/annotations.
def get_failures(pr_num, sha):
    url = f"https://api.github.com/repos/{repo}/commits/{sha}/check-runs"
    req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            for run in data.get('check_runs', []):
                if run['conclusion'] == 'failure':
                    print(f"--- PR {pr_num} FAILED JOB: {run['name']} ---")
                    # Check for annotations
                    run_id = run['id']
                    annot_url = f"https://api.github.com/repos/{repo}/check-runs/{run_id}/annotations"
                    req_annot = urllib.request.Request(annot_url, headers={'Accept': 'application/vnd.github.v3+json'})
                    with urllib.request.urlopen(req_annot) as resp_annot:
                        annots = json.loads(resp_annot.read().decode())
                        for ann in annots:
                            print(f"Annotation: {ann.get('message')}")
                    # Also print the 'output' summary/text
                    output = run.get('output', {})
                    if output.get('summary'):
                        print(f"Summary: {output.get('summary')[:200]}")
                    if output.get('text'):
                        print(f"Text: {output.get('text')[:200]}")
    except Exception as e:
        print(f"Error fetching for PR {pr_num}: {e}")

prs_to_check = [14, 13, 11, 10, 9, 6, 5, 4, 3, 2, 1]
# We need to map PR number to SHA. Let's re-fetch the PRs.
url = f"https://api.github.com/repos/{repo}/pulls?state=open&per_page=100"
req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
with urllib.request.urlopen(req) as response:
    prs = json.loads(response.read().decode())
    for pr in prs:
        if pr['number'] in prs_to_check:
            get_failures(pr['number'], pr['head']['sha'])

