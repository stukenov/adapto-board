# Ops & Monitoring — Tasks

## Health Endpoints

- [x] Create HealthRoutes.kt with /health/live and /health/ready
- [x] Add health check for database connection
- [x] Add health check for storage availability
- [x] Wire routes in Application.kt (public, no auth)

## Alerts System

- [x] AlertType and AlertStatus enums already exist in OpsEnums.kt
- [x] AlertEntity already exists in OpsEntities.kt
- [x] Create AlertRepository interface
- [x] Implement AlertRepositoryImpl
- [x] Create AlertService
- [x] Create AlertsRoutes.kt with admin endpoints
- [x] Wire alerts routes in Application.kt

## Verification

- [x] Verify build compiles
