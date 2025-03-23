# Simple HTTP-Connect Proxy written in Plain-Java

at the moment this proxy only supports HTTP-Connect-Requests (via HTTP)

## Proxy-Rules

Incoming Connections can be handled in 2 different ways:  
* block: Connection is blocked. Proxy returns HTTP 403.
* proxy: Connection will be established via another connect proxy,
* direct: Connection will be established directly.

The configuration source can be read from file or environment. Source can be switched via ENV:PROXY_RULE_SOURCE.

### Read proxy rules from environment
```
PROXY_RULE_SOURCE = env://PROXY_RULES
PROXY_RULES = * direct
```

### Read proxy rules from file
```
PROXY_RULE_SOURCE = file://path/to/file
```

### Rule Syntax
Rules can be chained via PIPE. This can be usefully in container environment for small configurations:
```
github.com direct | www.google.com proxy localhost:8888 | * block 
```
.. or can be read from file:
```
github.com direct
www.google.com proxy localhost:8888
* block 
```
