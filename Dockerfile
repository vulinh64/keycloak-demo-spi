FROM quay.io/keycloak/keycloak:21.1.2
COPY target/keycloak-custom-authentication-1.0.0.jar /opt/keycloak/providers
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]