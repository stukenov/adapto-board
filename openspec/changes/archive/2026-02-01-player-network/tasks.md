# Player Network Layer — Tasks

## HTTP Client
- [x] Create ApiClient with Ktor
- [x] Configure timeouts and retries
- [x] Add token injection interceptor

## Authentication
- [x] Create TokenStorage (encrypted DataStore)
- [x] Implement token refresh flow
- [x] Handle 401 → refresh → retry

## API Interfaces
- [x] Create PlayerApiClient interface
- [x] Implement config API calls
- [x] Implement playlist API calls
- [x] Implement heartbeat API calls
- [x] Implement asrun batch API calls
- [x] Implement enroll API calls

## Network State
- [x] Create NetworkMonitor
- [x] Detect online/offline
- [x] Monitor connectivity changes

## Verification
- [x] Verify build compiles
