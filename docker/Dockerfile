FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

# -------- Build arguments --------
ARG NEXUS_URL
ARG NEXUS_REPO
ARG NEXUS_USER
ARG NEXUS_PASS
ARG GROUP_PATH=com/mealbox
ARG ARTIFACT_ID=order-service
ARG VERSION=0.0.1-SNAPSHOT

# -------- Runtime config --------
WORKDIR /app
USER 1001

# -------- Download artifact from Nexus --------
RUN curl -u ${NEXUS_USER}:${NEXUS_PASS} \
    ${NEXUS_URL}/repository/${NEXUS_REPO}/${GROUP_PATH}/${ARTIFACT_ID}/${VERSION}/maven-metadata.xml \
    -o metadata.xml && \
    BUILD_VERSION=$(grep -oPm1 "(?<=<value>)[^<]+" metadata.xml) && \
    curl -u ${NEXUS_USER}:${NEXUS_PASS} \
    ${NEXUS_URL}/repository/${NEXUS_REPO}/${GROUP_PATH}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${BUILD_VERSION}.jar \
    -o app.jar && \
    rm -f metadata.xml

EXPOSE 9104
ENTRYPOINT ["java","-jar","/app/app.jar"]

