# BC01 : MISE EN PLACE PRATIQUE SUR ORDINATEUR DE PIPELINE CI/CD

## QUESTION 1 : CONCEPTION ARCHITECTURALE ET MODELISATION

### 1. Reformuler le besoin fonctionnel et identifier
**Acteurs du système**
Les acteurs sont les personnes qui utilisent le système.
- **Acteur principal :** Utilisateur (User)
- **Acteur secondaire :** Administrateur (Admin)

**Principales fonctionnalités**
L'application doit permettre :
- Création d'un compte utilisateur
- Connexion sécurisée
- Création d'une tâche
- Modification d'une tâche
- Suppression d'une tâche
- Consultation de la liste des tâches
- Pagination des tâches
- Gestion des rôles utilisateur

**Contraintes techniques**
Le système doit respecter :
- Architecture REST API
- Framework Spring Boot
- Authentification JWT
- Base de données MySQL
- Tests unitaires obligatoires
- Pipeline CI/CD
- Analyse de qualité SonarQube
- Déploiement avec Docker

### 2. Modélisation des données

| Table | Colonnes principales | Contraintes |
| :--- | :--- | :--- |
| **users** | id (PK), username, email, password, role, created_at | email UNIQUE, NOT NULL |
| **tasks** | id (PK), title, description, status, priority, due_date, user_id (FK), created_at, updated_at | title NOT NULL, status ENUM |
| **roles** | id (PK), name (ROLE_USER, ROLE_ADMIN) | name UNIQUE |

**Relations :**
- `users` → `tasks` : OneToMany (un user peut avoir plusieurs tâches)
- `tasks` → `users` : ManyToOne (chaque tâche appartient à un user)
- `Clé étrangère` : tasks.user_id REFERENCES users(id) ON DELETE CASCADE

### 3. Structure REST de l'API

| Méthode HTTP | Route | Description |
| :--- | :--- | :--- |
| POST | `/api/auth/register` | Inscription utilisateur |
| POST | `/api/auth/login` | Connexion → retourne JWT |
| GET | `/api/tasks` | Liste des tâches |
| POST | `/api/tasks` | Créer une tâche |
| GET | `/api/tasks/{id}` | Détail d'une tâche |
| PUT | `/api/tasks/{id}` | Modifier une tâche |
| DELETE | `/api/tasks/{id}` | Supprimer une tâche |
| GET | `/api/admin/users` | Liste des users (admin) |

**Codes de réponse HTTP**
- 200 : succès
- 201 : ressource créée
- 400 : mauvaise requête
- 401 : non autorisé
- 404 : non trouvé
- 500 : erreur serveur

**Gestion des erreurs**
Spring Boot utilisera :
- `GlobalExceptionHandler`
- `@ControllerAdvice`

*Exemple erreurs :*
- Tâche inexistante
- Utilisateur non authentifié
- Validation échouée

**Versioning API**
Pour maintenir la compatibilité :
- `/api/v1/tasks`
- `/api/v1/auth`

### 4. Architecture applicative Spring Boot
**Organisation des packages (Clean Architecture) :**
```text
src/main/java/com/taskmanager/
├── config/          → SecurityConfig, JwtConfig, SwaggerConfig
├── controller/      → AuthController, TaskController, AdminController
├── service/         → AuthService, TaskService, UserService
├── repository/      → UserRepository, TaskRepository (JPA)
├── model/entity/    → User.java, Task.java (entités JPA)
├── dto/             → TaskDTO, UserDTO, LoginRequest, JwtResponse
├── exception/       → GlobalExceptionHandler, ResourceNotFoundException
├── security/        → JwtFilter, JwtUtil, UserDetailsServiceImpl
└── mapper/          → TaskMapper, UserMapper (DTO ↔ Entity)
```

**Justification des choix :**
- **Séparation des responsabilités :** chaque couche a un rôle précis (Controller = HTTP, Service = logique métier, Repository = accès données)
- **Principe SOLID :** Single Responsabilité sur chaque classe, interfaces pour les services
- **Clean Code :** nommage explicite, méthodes courtes (<20 lignes), pas de duplication via les services partagés
- **DTOs :** séparation entre les entités JPA et les données exposées à l'API (sécurité et flexibilité)

---

## QUESTION 2 : Réalisation, Qualité et Sécurité
**Mettre en œuvre l’application springboot**
- Authentification JWT (Spring Security)
- CRUD des tâches
- Validation des données
- Gestion centralisée des exceptions
- Pagination des résultats

2. Mettre en place les tests automatises
3. Intégrer les bonnes pratiques de sécurité
4. Appliquer les principes de clean code

---

## QUESTION 3 : CONCEPTION DU PIPELINE CI/CD

### 1. Analyse et strategie ci/cd
**Stratégie de branches**

| Branche | Rôle | Déclencheur pipeline |
| :--- | :--- | :--- |
| **main** | Code stable, prêt pour production | Push → pipeline complet + déploiement prod |
| **develop** | Intégration des fonctionnalités | Push → build, tests, analyse qualité |
| **feature/\*** | Développement d'une fonctionnalité | Push → build + tests unitaires |
| **hotfix/\*** | Correction urgente en production | Push → pipeline complet accéléré |

**Déclencheurs du pipeline**
- `push` : sur toutes les branches → exécute au minimum build + tests
- `pull_request` (merge request) : vers develop ou main → pipeline complet obligatoire
- `tag` : déploiement automatique en production

**Étapes du pipeline**
```text
Developer
   ↓
Push GitHub
   ↓
Pipeline CI
   ↓
Build
   ↓
Lint
   ↓
Tests
   ↓
SonarQube
   ↓
Security Scan
   ↓
Docker Build
   ↓
Deploy
```

### 2. Définition des etapes CI
- **Etape 1: Build & Lint:** Compile le code et vérifie le respect des normes de codage (Maintainabilité).
- **Etape 2: Tests Unitaires:** Garantit le bon fonctionnement des composants isolés.
- **Etape 3: Analyse SonarQube:** Détecte la dette technique, les bugs et les vulnérabilités (Traçabilité).
- **Etape 4: Security Scan:** Vérifie les failles de sécurité dans les dépendances et l'image Docker.
- **Etape 5: Build image Docker:** Empaquette l'application dans un conteneur standardisé.
- **Etape 6: Déploiement:** Met en ligne l'application automatiquement (Observabilité).

**Maintenabilité**
Le pipeline garantit : code propre, tests automatisés, qualité continue.

**Traçabilité**
Chaque build garde : logs, rapport test, analyse SonarQube.

### 3. Proposer un schema conception
```text
Code Push
    │
    ▼
[1] BUILD  →  mvn clean package -DskipTests
     si build OK
[2] LINT  →  Checkstyle + SpotBugs
     si aucune violation critique
[3] TESTS UNITAIRES  →  mvn test + JaCoCo
     ✓ si coverage >= 60%
[4] SONARQUBE  →  analyse qualité + Quality Gate
     ✓ si Quality Gate = PASSED
[5] SCAN SÉCURITÉ  →  Trivy + OWASP Dependency-Check
     ✓ si aucune vulnérabilité CRITICAL
[6] BUILD DOCKER  →  docker build + push registry
      tag = git commit SHA
[7] DÉPLOIEMENT  →  docker-compose up -d (si branche main)
```

---

## Question 4 Implementation du pipeline

**4.1 Intégration Continue — Fichier YAML**
*(Voir le fichier `.github/workflows/ci-cd.yml` pour le pipeline complet)*

**Critères d'évaluation atteints :**
- Automatisation complète : aucune intervention humaine nécessaire
- Cache Maven : réduit le temps de téléchargement des dépendances de ~2 minutes
- Rapport JaCoCo archivé : visible dans les artifacts GitHub Actions

**4.2 Intégration SonarQube**
Scan configuré avec `sonar:sonar` et Quality Gate.

**4.3 Scan de Sécurité (DevSecOps)**
- Scan dépendances Maven (OWASP)
- Scan image Docker (Trivy)
- Détection de secrets (Gitleaks)

**Vulnérabilités typiquement détectées et corrections :**
- CVE dans une vieille version de Spring Boot → mise à jour vers la version stable la plus récente
- Token/mot de passe hardcodé dans le code → utilisation de GitHub Secrets et variables d'environnement
- Vulnérabilité dans l'image de base Docker → utiliser `eclipse-temurin:17-jre-alpine` au lieu de `openjdk:17`

---

## QUESTION 5 — Déploiement Automatique

**5.1 Dockerfile Spring Boot**
*(Voir le fichier `Dockerfile` principal)*
Génère une image optimisée avec un utilisateur non-root.

**5.2 docker-compose.yml**
*(Voir le fichier `docker-compose.yml` principal)*
Orchestration de l'application avec une base de données MySQL.

**5.3 Job de déploiement dans le pipeline**
Déploiement via SSH sur un serveur distant, zéro-downtime, redémarrage automatique.

**Option Bonus — Environnement Staging**
- Ajouter un job deploy-staging qui s'exécute sur la branche develop
- Utiliser un docker-compose.staging.yml avec port 8081
- Les tests de smoke (test de santé via /actuator/health) s'exécutent avant la promotion vers main

---

## QUESTION 6 Optimisation & Clean Code

### 4.1 Structuration Modularité
**Comment le projet respecte les principes de modularité :**
- Chaque couche (Controller, Service, Repository) est dans son propre package
- Les DTOs découplent le modèle de données de l'API
- Spring `@Component`, `@Service`, `@Repository` permettent l'injection de dépendances → testabilité par Mockito
- `GlobalExceptionHandler` centralisé → une seule classe gère toutes les erreurs de l'application

**Améliorations apportées au code initial :**
- Suppression du code de validation dupliqué dans plusieurs controllers → déplacé dans des classes Validator dédiées ou annotations
- Extraction de la logique JWT dans `JwtUtil` → le SecurityFilter ne fait qu'appeler des méthodes nommées clairement
- Utilisation de records ou DTOs explicites pour le mapping et validations strictes
- (Optionnel) Utilisation de MapStruct pour le mapping Entity↔DTO → élimine le code boilerplate de conversion

### 4.2 Optimisation du pipeline

| Optimisation | Implémentation | Gain estimé |
| :--- | :--- | :--- |
| **Cache Maven** | `actions/setup-java` avec `cache: 'maven'` | -2 min par exécution |
| **Parallélisation** | `sonarqube-analysis` et `security-scan` tournent en parallèle | -3 min au total |
| **Cache Docker layers** | `docker/build-push-action` avec cache | -1 min sur le build |
| **Exécution conditionnelle** | `if: github.ref == 'refs/heads/main'` | Pas de déploiement accidentel |
| **Tests en parallèle** | `mvn -T 4 test` (4 threads Maven) | -30 sec sur les tests |

### 4.3 Guide pratique

**Comment exécuter le pipeline**
- **Automatique :** tout push ou pull_request déclenche le pipeline (onglet Actions sur GitHub)
- **Manuel :** GitHub Actions > workflow > 'Run workflow'
- **Local :** utiliser maven pour simuler les étapes build/test/qualité

**Comment corriger un échec de pipeline**

| Symptôme | Cause probable | Correction |
| :--- | :--- | :--- |
| BUILD FAILED - compilation | Erreur Java, dépendance manquante | Lire le log Maven, corriger l'erreur, relancer |
| Tests échoués | Assertion fausse, mock mal configuré | Lire le test raté, corriger la logique ou le mock |
| JaCoCo coverage < 60% | Trop peu de tests | Ajouter des tests unitaires sur les services non couverts |
| SonarQube Quality Gate FAILED | Trop de bugs/code smells/duplication | Aller sur SonarQube, corriger les issues bloquantes |
| Trivy CRITICAL CVE | Dépendance vulnérable | Mettre à jour la version dans pom.xml |
| Déploiement SSH échoue | Clé SSH expirée ou serveur indisponible | Vérifier le secret SSH_PRIVATE_KEY et l'état du serveur |

**Comment interpréter les rapports SonarQube**
- **Bugs :** problèmes qui peuvent causer des erreurs runtime → corriger en priorité (icône rouge)
- **Vulnerabilities :** failles de sécurité détectées dans le code → corriger avant tout déploiement
- **Code Smells :** mauvaises pratiques qui réduisent la maintenabilité → à corriger progressivement
- **Coverage :** % de lignes couvertes par les tests → doit rester >= 60%
- **Duplications :** % de code copié-collé → réduire en extrayant des méthodes communes
- **Quality Gate :** résumé PASSED/FAILED basé sur vos seuils configurés → doit être PASSED pour déployer

**Comment corriger une vulnérabilité détectée**
- **Étape 1 Identifier :** dans le rapport Trivy ou OWASP
- **Étape 2 Analyser :** chercher le CVE pour comprendre le risque et la version fixée
- **Étape 3 Corriger :** dans pom.xml, mettre à jour la dépendance vulnérable vers la version corrigée
- **Étape 4 Vérifier :** relancer le pipeline pour confirmer que le scan ne détecte plus la vulnérabilité
- **Étape 5 Documenter**
