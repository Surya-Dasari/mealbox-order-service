FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

LABEL app="mealbox-order-service" runtime="java17"

WORKDIR /app
RUN mkdir -p /app && chmod -R g+rwX /app

RUN set -eux; \
    META_URL="http://host.docker.internal:8082/repository/mealbox-maven-snapshots/com/mealbox/order-service/0.0.1-SNAPSHOT/maven-metadata.xml"; \
    SNAPSHOT=$(curl -fsSL ${META_URL} | grep '<value>' | tail -n 1 | sed -E 's/.*>(.*)<.*/\1/'); \
    curl -fsSL \
      "http://host.docker.internal:8082/repository/mealbox-maven-snapshots/com/mealbox/order-service/0.0.1-SNAPSHOT/order-service-${SNAPSHOT}.jar" \
      -o app.jar

EXPOSE 9104
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
