## ADDED Requirements

### Requirement: Request ID generation
The system SHALL generate a unique requestId (UUID) for each incoming HTTP request if not provided by the client.

#### Scenario: Generate request ID when not provided
- **WHEN** a request arrives without X-Request-Id header
- **THEN** the system SHALL generate a new UUID as the requestId

#### Scenario: Use client-provided request ID
- **WHEN** a request arrives with X-Request-Id header
- **THEN** the system SHALL use the provided value as the requestId

### Requirement: Request ID in response
The system SHALL include the requestId in every API response, both in the response body (for errors) and as X-Request-Id response header.

#### Scenario: Request ID in error response body
- **WHEN** an API error occurs
- **THEN** the error response body SHALL contain the requestId field

#### Scenario: Request ID in response header
- **WHEN** any API response is sent (success or error)
- **THEN** the response SHALL include X-Request-Id header with the requestId value

### Requirement: Request ID in logs
The system SHALL include the requestId in all log entries produced while handling a request via MDC (Mapped Diagnostic Context).

#### Scenario: Log entry contains request ID
- **WHEN** a log statement is executed during request processing
- **THEN** the log entry SHALL include the requestId in the MDC context

### Requirement: Request ID plugin for Ktor
The system SHALL implement request ID functionality as a Ktor plugin named RequestIdPlugin in apps/server.

#### Scenario: Plugin intercepts all requests
- **WHEN** RequestIdPlugin is installed
- **THEN** every incoming request SHALL have a requestId assigned to call attributes

#### Scenario: Plugin sets MDC context
- **WHEN** a request is processed with RequestIdPlugin
- **THEN** the MDC context SHALL be set with requestId before route handling and cleared after

### Requirement: Request ID accessible in route handlers
The system SHALL provide an extension property or function to access the current requestId from ApplicationCall.

#### Scenario: Access request ID in handler
- **WHEN** a route handler needs the requestId
- **THEN** it SHALL be accessible via call.requestId extension property
