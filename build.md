# Build instructtions

### Build container image

```bash
podman build -f containerfile --tag java-connect-proxy .
```

```bash
podman run --rm \
  -e PROXY_RULE_SOURCE=env://PROXY_RULES \
  -e "PROXY_RULES=* direct" \
  -p 8022:8080 \
  java-connect-proxy .
```