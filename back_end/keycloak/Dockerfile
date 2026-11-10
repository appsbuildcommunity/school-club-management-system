FROM quay.io/keycloak/keycloak:26.8.1@sha256:a78d6b79572168cb25886b80a8b5b8d4ed4390743e3eecb810538683e209dd6b

ARG WEBHOOK_PLUGIN_VERSION=0.9.1
ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-core-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-core-all.jar
ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-http-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-http-all.jar

USER root

RUN chown -R 1000:0 /opt/keycloak/providers

USER 1000
RUN /opt/keycloak/bin/kc.sh build
