import re
content = open('core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt').read()
content = re.sub(r'sessionStore = object : com.rtiqa.core.network.session.RestSessionStore \{.*?\}', '', content, flags=re.DOTALL)
# It's mangled. Let's look at the structure and just rebuild the setUp method.
