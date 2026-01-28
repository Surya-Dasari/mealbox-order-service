FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

# ---- Build args ----
ARG NEXUS_URL
ARG NEXUS_REPO
ARG NEXUS_USER
ARG NEXUS_PASS
ARG VERSION=0.0.1-SNAPSHOT

WORKDIR /app

# ---- Download artifact (build-time only) ----
USER root
RUN curl -u ${NEXUS_USER}:${NEXUS_PASS} \
      ${NEXUS_URL}/repository/${NEXUS_REPO}/com/mealbox/order-service/${VERSION}/maven-metadata.xml \
      -o maven-metadata.xml && \
    BUILD_VER=$(sed -n 's:.*<value>\(.*\)</value>.*:\1:p' maven-metadata.xml | head -1) && \
    curl -u ${NEXUS_USER}:${NEXUS_PASS} \
      ${NEXUS_URL}/repository/${NEXUS_REPO}/com/mealbox/order-service/${VERSION}/order-service-${BUILD_VER}.jar \
      -o app.jar && \
    rm maven-metadata.xml && \
    chown -R 1001:0 /app

# ---- Runtime (secure) ----
USER 1001
EXPOSE 9104
ENTRYPOINT ["java","-jar","/app/app.jar"]

