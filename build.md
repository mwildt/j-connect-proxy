# build instructions

### build maven package

```bash
mvn clean package
```

### Build container image

use podman to build the java app and corresponding container. 
```bash
podman build -f containerfile --tag java-connect-proxy .
```

the image can then be launched using PROXY_RULES-environment. 
```bash
podman run --rm \
  -e PROXY_RULE_SOURCE=env://PROXY_RULES \
  -e "PROXY_RULES=* direct" \
  -p 8022:8080 \
  java-connect-proxy .
```