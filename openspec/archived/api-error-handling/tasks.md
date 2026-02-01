## 1. Error Codes and Types (libs/contracts)

- [x] 1.1 Create ErrorCode enum with all error codes grouped by domain
- [x] 1.2 Create ApiError data class with kotlinx.serialization support

## 2. Exception Hierarchy (libs/contracts)

- [x] 2.1 Create ApiException sealed class with subclasses for each HTTP status category
- [x] 2.2 Add helper functions to create exceptions from ErrorCode

## 3. Request ID Plugin (apps/server)

- [x] 3.1 Create RequestIdPlugin Ktor plugin for generating/extracting request IDs
- [x] 3.2 Add call.requestId extension property for accessing request ID in handlers
- [x] 3.3 Configure MDC context with request ID for logging

## 4. Error Handling Plugin (apps/server)

- [x] 4.1 Create StatusPages configuration for handling ApiException
- [x] 4.2 Map each ApiException subclass to appropriate HTTP status code
- [x] 4.3 Handle unexpected exceptions as 500 Internal Error
- [x] 4.4 Ensure all error responses include requestId

## 5. Integration

- [x] 5.1 Install RequestIdPlugin in Application.module()
- [x] 5.2 Install StatusPages error handling in Application.module()
- [x] 5.3 Verify build compiles with new error handling infrastructure
