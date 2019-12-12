- Prometheus

  java client: https://github.com/prometheus/client_java#javadocs

  ​

- Tracing:

  Tutorial walkthrough: https://github.com/opentracing-contrib/java-opentracing-walkthrough

  OpenTracing specification: https://github.com/opentracing/specification

  > OpenTracing can throw errors when an extract fails due to no span being present, so make sure to catch the errors that signify there was no injected span and not crash your server. This often just means that the request is coming from a third-party (or untraced) client, and the server should start a new trace.

  ​

  ​

