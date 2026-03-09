# ── Stage 1: Build ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Installer Maven
RUN apk add --no-cache maven

# Copier pom.xml et télécharger les dépendances (couche cachée)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et builder
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Métadonnées
LABEL maintainer="DevOps Team"
LABEL description="TaskManager API - Devops Exercise"
LABEL version="1.0.0"

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S taskmanager && adduser -S taskmanager -G taskmanager

# Copier le JAR depuis le stage builder
COPY --from=builder /app/target/taskmanager-1.0.0-SNAPSHOT.jar app.jar

# Créer le dossier de logs
RUN mkdir -p /app/logs && chown -R taskmanager:taskmanager /app

USER taskmanager

# Port exposé
EXPOSE 8080

# Variables d'environnement par défaut (surchargeables)
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="prod"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
