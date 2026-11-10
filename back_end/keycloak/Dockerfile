FROM quay.io/keycloak/keycloak:26.8.2-1@sha256:723b1d6e254c0fc96d5cffec860c7e0744d6a3277822b865bb538cf891575e41

ARG WEBHOOK_PLUGIN_VERSION=0.9.1
ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-core-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-core-all.jar
ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-http-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-http-all.jar

USER root

RUN chown -R 1000:0 /opt/keycloak/providers

USER 1000
RUN /opt/keycloak/bin/kc.sh build
