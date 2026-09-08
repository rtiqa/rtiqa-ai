import urllib.request
import json
import sys
import os

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}"
req = urllib.request.Request(url, headers={'Accept': 'application/vnd.github.v3+json'})
token = os.environ.get('GITHUB_TOKEN')
if token:
    req.add_header('Authorization', f'token {token}')

try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        print(f"Repo found: {data.get('full_name')}")
except Exception as e:
    print(f"Error accessing repo (likely private or no token): {e}")
