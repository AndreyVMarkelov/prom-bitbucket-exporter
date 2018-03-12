# Prometheus Exporter For Bitbucket [![Build Status](https://travis-ci.org/AndreyVMarkelov/prom-bitbucket-exporter.svg?branch=master)](https://travis-ci.org/AndreyVMarkelov/prom-bitbucket-exporter)

This is Bitbucket plugin which provides endpoint to expose Bitbucket metrics to Prometheus.

For more information the documentation [Prometheus Exporter For Bitbucket](https://github.com/AndreyVMarkelov/prom-bitbucket-exporter/wiki/Prometheus-Exporter-For-Bitbucket).

On Atlassian Marketplace [Prometheus Exporter For Bitbucket](https://marketplace.atlassian.com/plugins/ru.andreymarkelov.atlas.plugins.prom-bitbucket-exporter/server/overview)


## Building
```
cd prom-bitbucket-exporter
docker run -v $(pwd):/buildzone -it maven:alpine sh -c 'cd buildzone && mvn clean package -f pom.xml -s settings.xml'

```


## Grafana examples 

* [Example grafana dashborad](./grafana/dashboard-example.json)

![image](./grafana/img/grafana-bitbucket-dashboard.png)
