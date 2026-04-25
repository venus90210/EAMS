import { setupServer } from 'msw/node'

// This configures a request mocking server with the given request handlers.
// All requests to the API will be mocked - you can define custom handlers
// for each test or test suite.

export const server = setupServer(
  // Define default handlers here - they will apply to all tests
  // unless overridden in individual test files
)
