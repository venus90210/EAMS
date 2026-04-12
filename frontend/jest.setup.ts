/// <reference types="jest" />
import '@testing-library/jest-dom'
import { server } from './src/mocks/server'

// Establish API mocking before all tests
beforeAll(() => {
  // MSW setup if needed
  // server.listen() is called in a separate setup if using MSW
})

// Reset any request handlers that we may add during the tests
afterEach(() => {
  // server.resetHandlers()
})

// Clean up after the tests are finished
afterAll(() => {
  // server.close()
})
