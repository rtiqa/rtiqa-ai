import sys

file_path = "/app/applet/core-data/src/main/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImpl.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'RtiqaError.AuthError("Validation error. Please check your inputs.", e)',
    'RtiqaError.AuthError("Validation error. Please check your inputs.", cause = e)'
)
content = content.replace(
    'RtiqaError.AuthError("Invalid credentials or session.", e)',
    'RtiqaError.AuthError("Invalid credentials or session.", cause = e)'
)
content = content.replace(
    'RtiqaError.AuthError("Authorization or tenant issue.", e)',
    'RtiqaError.AuthError("Authorization or tenant issue.", cause = e)'
)
content = content.replace(
    'RtiqaError.NetworkError("Too many requests. Please try again later.", e)',
    'RtiqaError.NetworkError("Too many requests. Please try again later.", cause = e)'
)
content = content.replace(
    'RtiqaError.NetworkError("Temporary server error.", e)',
    'RtiqaError.NetworkError("Temporary server error.", cause = e)'
)
content = content.replace(
    'RtiqaError.UnknownError(errorMessage, e)',
    'RtiqaError.UnknownError(errorMessage, cause = e)'
)

with open(file_path, "w") as f:
    f.write(content)
