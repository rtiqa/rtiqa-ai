import urllib.request
import json
import sys

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}/pulls?state=open&per_page=100"

try:
    req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
    with urllib.request.urlopen(req) as response:
        prs = json.loads(response.read().decode())
        
        dependabot_prs = [pr for pr in prs if 'dependabot' in pr['user']['login'].lower()]
        print(f"Found {len(dependabot_prs)} dependabot PRs.")
        
        for pr in dependabot_prs:
            print(f"---")
            print(f"PR: {pr['number']}")
            print(f"Title: {pr['title']}")
            print(f"Branch: {pr['head']['ref']}")
            print(f"Base: {pr['base']['ref']}")
            
            # Fetch statuses (for commit)
            commit_url = f"https://api.github.com/repos/{repo}/commits/{pr['head']['sha']}/check-runs"
            req_checks = urllib.request.Request(commit_url, headers={'Accept': 'application/vnd.github.v3+json'})
            with urllib.request.urlopen(req_checks) as resp_checks:
                checks = json.loads(resp_checks.read().decode())
                for run in checks.get('check_runs', []):
                    print(f"Check: {run['name']} | Status: {run['status']} | Conclusion: {run['conclusion']}")
            
except Exception as e:
    print(f"Error: {e}")
