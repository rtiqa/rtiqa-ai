import urllib.request
import json
import sys
import os

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}/pulls?state=open&per_page=100"
req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
token = os.environ.get('GITHUB_TOKEN')
if token:
    req.add_header('Authorization', f'token {token}')

try:
    with urllib.request.urlopen(req) as response:
        prs = json.loads(response.read().decode())
        dependabot_prs = [pr for pr in prs if 'dependabot' in pr['user']['login'].lower()]
        
        for pr in dependabot_prs:
            print(f"PR: {pr['number']} | Base SHA: {pr['base']['sha'][:7]} | Head SHA: {pr['head']['sha'][:7]}")
            
except Exception as e:
    print(f"Error accessing PRs: {e}")
