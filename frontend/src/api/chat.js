import { API_BASE_URL } from './index'

/**
 * Create an SSE connection for chat streaming
 * @param {number} memoryId - The session memory ID
 * @param {string} message - The user's message
 * @param {Object} callbacks - Callback functions for handling events
 * @param {Function} callbacks.onMessage - Called when a new chunk is received
 * @param {Function} callbacks.onError - Called when an error occurs
 * @param {Function} callbacks.onComplete - Called when the stream ends
 * @returns {EventSource} - The EventSource instance for cleanup
 */
export function createChatStream(memoryId, message, callbacks) {
  const { onMessage, onError, onComplete } = callbacks
  
  // Properly encode the message for URL
  const encodedMessage = encodeURIComponent(message)
  const url = `${API_BASE_URL}/ai/chat?memoryId=${memoryId}&message=${encodedMessage}`
  
  const eventSource = new EventSource(url)
  
  // Handle incoming messages
  eventSource.onmessage = (event) => {
    if (event.data) {
      onMessage(event.data)
    }
  }
  
  // Handle errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    eventSource.close()
    
    // Check if it was a normal close (readyState === 2) after receiving data
    // or an actual error
    if (eventSource.readyState === EventSource.CLOSED) {
      // Stream ended - this is normal when server closes connection
      onComplete()
    } else {
      // Actual error occurred
      onError(new Error('Connection lost, please retry.'))
    }
  }
  
  // Handle connection open
  eventSource.onopen = () => {
    console.log('SSE connection opened')
  }
  
  return eventSource
}

/**
 * Close an active SSE connection
 * @param {EventSource} eventSource - The EventSource to close
 */
export function closeChatStream(eventSource) {
  if (eventSource && eventSource.readyState !== EventSource.CLOSED) {
    eventSource.close()
  }
}
