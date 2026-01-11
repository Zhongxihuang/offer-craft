<template>
  <div class="app-container">
    <!-- Header -->
    <ChatHeader 
      :is-streaming="isStreaming"
      @new-chat="handleNewChat"
    />
    
    <!-- Main Chat Area -->
    <main class="chat-main">
      <div class="chat-container" ref="chatContainerRef">
        <!-- Welcome Screen (when no messages) -->
        <WelcomeScreen 
          v-if="messages.length === 0"
          @select-topic="handleTopicSelect"
        />
        
        <!-- Messages List -->
        <div v-else class="messages-list">
          <ChatMessage
            v-for="(message, index) in messages"
            :key="message.id"
            :content="message.content"
            :is-user="message.role === 'user'"
            :timestamp="message.timestamp"
            :is-streaming="message.isStreaming"
            :is-error="message.isError"
            :show-retry="message.isError && index === messages.length - 1"
            @retry="handleRetry"
          />
        </div>
      </div>
    </main>
    
    <!-- Input Area -->
    <ChatInput
      ref="chatInputRef"
      :disabled="isStreaming"
      :is-loading="isStreaming"
      @send="handleSendMessage"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import ChatHeader from './components/ChatHeader.vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import WelcomeScreen from './components/WelcomeScreen.vue'
import { createChatStream, closeChatStream } from './api/chat'

// Refs
const chatContainerRef = ref(null)
const chatInputRef = ref(null)

// State
const messages = ref([])
const isStreaming = ref(false)
const memoryId = ref(null)
const currentEventSource = ref(null)
const lastUserMessage = ref('')

// Generate a unique memory ID
function generateMemoryId() {
  return Math.floor(Math.random() * 1000000000)
}

// Initialize or restore memoryId from localStorage
function initMemoryId() {
  const stored = localStorage.getItem('codeforge_memoryId')
  if (stored) {
    memoryId.value = parseInt(stored, 10)
  } else {
    memoryId.value = generateMemoryId()
    localStorage.setItem('codeforge_memoryId', memoryId.value.toString())
  }
}

// Create a new chat session
function handleNewChat() {
  // Close any active stream
  if (currentEventSource.value) {
    closeChatStream(currentEventSource.value)
    currentEventSource.value = null
  }
  
  // Clear messages
  messages.value = []
  isStreaming.value = false
  
  // Generate new memoryId
  memoryId.value = generateMemoryId()
  localStorage.setItem('codeforge_memoryId', memoryId.value.toString())
  
  // Focus input
  nextTick(() => {
    chatInputRef.value?.focus()
  })
}

// Handle topic selection from welcome screen
function handleTopicSelect(prompt) {
  handleSendMessage(prompt)
}

// Send a message
async function handleSendMessage(messageText) {
  if (!messageText.trim() || isStreaming.value) return
  
  lastUserMessage.value = messageText
  
  // Add user message
  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: messageText,
    timestamp: new Date(),
    isStreaming: false,
    isError: false
  }
  messages.value.push(userMessage)
  
  // Add empty AI message for streaming
  const aiMessage = {
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isStreaming: true,
    isError: false
  }
  messages.value.push(aiMessage)
  
  // Scroll to bottom
  await scrollToBottom()
  
  // Start streaming
  isStreaming.value = true
  
  try {
    currentEventSource.value = createChatStream(
      memoryId.value,
      messageText,
      {
        onMessage: (chunk) => {
          // Find the AI message and append chunk
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.content += chunk
            scrollToBottom()
          }
        },
        onError: (error) => {
          console.error('Stream error:', error)
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.isStreaming = false
            lastMessage.isError = true
            lastMessage.content = error.message || 'Connection lost, please retry.'
          }
          isStreaming.value = false
          currentEventSource.value = null
        },
        onComplete: () => {
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.isStreaming = false
          }
          isStreaming.value = false
          currentEventSource.value = null
          
          // Focus input after completion
          nextTick(() => {
            chatInputRef.value?.focus()
          })
        }
      }
    )
  } catch (error) {
    console.error('Failed to create stream:', error)
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant') {
      lastMessage.isStreaming = false
      lastMessage.isError = true
      lastMessage.content = 'Failed to connect. Please try again.'
    }
    isStreaming.value = false
  }
}

// Retry last failed message
function handleRetry() {
  if (lastUserMessage.value) {
    // Remove the error message
    if (messages.value.length > 0) {
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage.isError) {
        messages.value.pop()
      }
    }
    // Remove the last user message to re-add it
    if (messages.value.length > 0) {
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage.role === 'user') {
        messages.value.pop()
      }
    }
    // Resend
    handleSendMessage(lastUserMessage.value)
  }
}

// Smooth scroll to bottom
async function scrollToBottom() {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTo({
      top: chatContainerRef.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

// Watch for new messages and scroll
watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

// Lifecycle
onMounted(() => {
  initMemoryId()
  chatInputRef.value?.focus()
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--color-background);
}

.chat-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md) 0;
}

.messages-list {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .messages-list {
    padding: 0 var(--spacing-md);
    gap: var(--spacing-sm);
  }
}
</style>
