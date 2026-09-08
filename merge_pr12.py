import urllib.request
import json
import os

repo = "rtiqa/rtiqa-ai"
url = f"https://api.github.com/repos/{repo}/pulls/12/merge"

token = os.environ.get("GITHUB_TOKEN")
if not token:
    print("FAILURE_NO_TOKEN: No GITHUB_TOKEN environment variable found.")
    exit(1)

data = json.dumps({
    "commit_title": "build(deps): bump com.google.devtools.ksp to 2.3.11 (#12)",
    "merge_method": "squash"
}).encode('utf-8')

req = urllib.request.Request(url, data=data, method="PUT")
req.add_header('Accept', 'application/vnd.github.v3+json')
req.add_header('Authorization', f'token {token}')
req.add_header('Content-Type', 'application/json')

try:
    with urllib.request.urlopen(req) as response:
        result = json.loads(response.read().decode())
        print(f"SUCCESS: {result['message']}")
        print(f"SHA: {result['sha']}")
except Exception as e:
    print(f"FAILURE_API_ERROR: {e}")
    exit(1)
