import urllib.request
import json

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}/pulls/12"

req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
try:
    with urllib.request.urlopen(req) as response:
        pr_data = json.loads(response.read().decode())
        print(f"PR Number: {pr_data['number']}")
        print(f"State: {pr_data['state']}")
        print(f"Title: {pr_data['title']}")
        print(f"Target Branch: {pr_data['base']['ref']}")
        print(f"HEAD SHA: {pr_data['head']['sha']}")
        print(f"Mergeable: {pr_data.get('mergeable')}")
        
        sha = pr_data['head']['sha']
        checks_url = f"https://api.github.com/repos/{repo}/commits/{sha}/check-runs"
        req_checks = urllib.request.Request(checks_url, headers={'Accept': 'application/vnd.github.v3+json'})
        with urllib.request.urlopen(req_checks) as resp_checks:
            checks_data = json.loads(resp_checks.read().decode())
            print(f"Total Checks: {checks_data['total_count']}")
            for run in checks_data.get('check_runs', []):
                print(f"- {run['name']}: {run['status']} / {run['conclusion']}")
                
except Exception as e:
    print(f"Error: {e}")
