# Mini-Plateforme Sécurisée de Gestion d'Utilisateurs et de Rôles

Ce projet implémente une API REST sécurisée permettant de gérer des utilisateurs et leurs rôles, avec une authentification via JWT (JSON Web Token) et une gestion des autorisations basée sur les rôles.

## Fonctionnalités

### Entités
- **User** : id, nom, email, mot de passe (hashé), rôle
- **Role** : id, nom (ex : ADMIN, USER)

### Endpoints API

#### API Publique (sans authentification)
- **POST /api/auth/register** : Création d'un utilisateur avec rôle USER par défaut
- **POST /api/auth/login** : Authentification et obtention d'un token JWT

#### API Sécurisée (authentification requise)
- **GET /api/admin/users** : Consulter tous les utilisateurs (ADMIN uniquement)
- **PUT /api/admin/users/{id}/role** : Modifier le rôle d'un utilisateur (ADMIN uniquement)
- **GET /api/users/profile** : Consulter son propre profil (USER ou ADMIN)

## Technologies utilisées

- **Spring Boot** : Framework Java pour le développement d'applications
- **Spring Data JPA** : Pour la persistence des données avec Hibernate
- **Spring Security** : Pour la sécurisation de l'API
- **H2 Database** : Base de données en mémoire
- **JSON Web Token (JWT)** : Pour l'authentification sans état
- **BCrypt** : Pour le hachage sécurisé des mots de passe

## Prérequis

- Java 17 ou supérieur
- Gradle

## Installation et exécution

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-nom/gestion-utilisateurs-api.git
   cd gestion-utilisateurs-api
   ```

2. Construisez et lancez l'application avec Gradle :
   ```bash
   ./gradlew bootRun
   ```

3. L'application sera disponible à l'adresse : `http://localhost:8080`

4. La console H2 est accessible à : `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `password`

## Guide d'utilisation

### 1. Créer un utilisateur

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"nom\":\"User Test\",\"email\":\"user@test.com\",\"password\":\"password123\"}"
```

### 2. Se connecter et obtenir un token JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@test.com\",\"password\":\"password123\"}"
```

### 3. Accéder à son profil

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

### 4. Créer un administrateur

```bash
# 1. Créer un compte utilisateur normal
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"nom\":\"Admin\",\"email\":\"admin@test.com\",\"password\":\"adminpass\"}"

# 2. Modifier son rôle via la console H2
# Exécutez cette SQL dans la console H2 :
# UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN') 
# WHERE email = 'admin@test.com';

# 3. Se connecter en tant qu'admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@test.com\",\"password\":\"adminpass\"}"
```

### 5. Accéder à la liste des utilisateurs (admin uniquement)

```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer VOTRE_TOKEN_ADMIN"
```

### 6. Modifier le rôle d'un utilisateur (admin uniquement)

```bash
curl -X PUT http://localhost:8080/api/admin/users/1/role \
  -H "Authorization: Bearer VOTRE_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d "{\"roleName\":\"ROLE_ADMIN\"}"
```

## Structure du projet

```
src/main/java/com/example/demo/
├── controller/         # Contrôleurs REST
│   ├── AuthController.java
│   └── UserController.java
├── model/              # Entités JPA
│   ├── Role.java
│   └── User.java
├── repository/         # Interfaces Repository
│   ├── RoleRepository.java
│   └── UserRepository.java
├── security/           # Configuration de sécurité
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   ├── SecurityConfig.java
│   └── UserDetailsServiceImpl.java
├── service/            # Services métier
│   ├── InitService.java
│   └── UserService.java
└── DemoApplication.java  # Point d'entrée de l'application
```

## Tests

Le projet inclut des tests unitaires pour le service de gestion des utilisateurs.

Pour exécuter les tests :
```bash
./gradlew test
```

## Sécurité

- Les mots de passe sont hashés avec BCrypt avant d'être stockés en base
- L'authentification utilise JWT avec l'algorithme HS512
- Les autorisations sont basées sur les rôles (ROLE_USER, ROLE_ADMIN)
- Accès restreint aux endpoints selon les rôles

## Améliorations possibles

- Ajouter une validation des données d'entrée
- Implémenter un système de rafraîchissement des tokens
- Ajouter plus de tests (tests d'intégration, tests des contrôleurs)
- Ajouter une documentation OpenAPI/Swagger

---

*Développé dans le cadre d'un test technique.*