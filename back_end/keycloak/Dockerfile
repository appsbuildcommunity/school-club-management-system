FROM quay.io/keycloak/keycloak:26.7

ARG WEBHOOK_PLUGIN_VERSION=0.9.1

ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-core-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-core-all.jar
ADD https://github.com/vymalo/keycloak-webhook/releases/download/v${WEBHOOK_PLUGIN_VERSION}/keycloak-webhook-provider-http-${WEBHOOK_PLUGIN_VERSION}-all.jar \
    /opt/keycloak/providers/keycloak-webhook-provider-http-all.jar

USER root
RUN chown -R 1000:0 /opt/keycloak/providers
USER 1000

RUN /opt/keycloak/bin/kc.sh build
