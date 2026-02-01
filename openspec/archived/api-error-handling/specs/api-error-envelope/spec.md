## ADDED Requirements

### Requirement: Unified error response format
The system SHALL return all API errors in a consistent JSON envelope format containing: code (string), message (string), details (optional object), and requestId (string).

#### Scenario: Validation error response
- **WHEN** a request fails validation
- **THEN** the response body SHALL contain code "VALIDATION_ERROR", a human-readable message describing the validation failure, optional details with field-specific errors, and the requestId

#### Scenario: Not found error response
- **WHEN** a requested resource does not exist
- **THEN** the response body SHALL contain code "NOT_FOUND" or a domain-specific code like "ASSET_NOT_FOUND", a message describing what was not found, and the requestId

### Requirement: Error response content type
The system SHALL return error responses with Content-Type: application/json.

#### Scenario: JSON content type on error
- **WHEN** any API error occurs
- **THEN** the response Content-Type header SHALL be "application/json"

### Requirement: ApiError data class
The system SHALL provide an ApiError data class in libs/contracts that is serializable with kotlinx.serialization.

#### Scenario: Serialize error to JSON
- **WHEN** an ApiError instance is serialized
- **THEN** the output SHALL be valid JSON with code, message, requestId fields and optional details field

#### Scenario: Deserialize error from JSON
- **WHEN** valid error JSON is deserialized
- **THEN** an ApiError instance SHALL be created with all fields populated correctly

### Requirement: ApiException hierarchy
The system SHALL provide a sealed class ApiException with subclasses for each HTTP error category: BadRequest (400), Unauthorized (401), Forbidden (403), NotFound (404), Conflict (409), UnprocessableEntity (422), InternalError (500).

#### Scenario: Throw not found exception
- **WHEN** code throws ApiException.NotFound with an error code
- **THEN** the exception SHALL carry the error code, message, and optional details

#### Scenario: Exception to HTTP status mapping
- **WHEN** an ApiException subclass is thrown
- **THEN** the corresponding HTTP status code SHALL be determined by the exception type
