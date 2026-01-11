# Code Forge AI - Frontend

A Vue 3 chat application for programming learning and career development assistance.

## Features

- 💬 Real-time streaming chat with AI assistant
- 📚 Programming roadmaps and learning guidance
- 💼 Interview preparation and career tips
- 🔄 Session persistence across page refreshes
- ⚡ SSE (Server-Sent Events) for streaming responses
- 📱 Responsive design (desktop & mobile)

## Tech Stack

- **Vue 3** with Composition API
- **Vite** for fast development and builds
- **Axios** for HTTP configuration
- **EventSource** for SSE streaming

## Prerequisites

- Node.js 18+ installed
- npm or yarn package manager
- Backend server running at `http://localhost:8081`

## Setup Instructions (Windows)

### 1. Navigate to the frontend directory

```powershell
cd c:\project_workspace\code-forge-ai\frontend
```

### 2. Install dependencies

```powershell
npm install
```

### 3. Start the development server

```powershell
npm run dev
```

The application will be available at `http://localhost:5173`

### 4. Build for production

```powershell
npm run build
```

### 5. Preview production build

```powershell
npm run preview
```

## Project Structure

```
frontend/
├── public/
│   └── vite.svg              # Favicon
├── src/
│   ├── api/
│   │   ├── index.js          # Axios configuration
│   │   └── chat.js           # SSE chat stream handling
│   ├── assets/
│   │   └── styles/
│   │       └── main.css      # Global styles & CSS variables
│   ├── components/
│   │   ├── ChatHeader.vue    # App header with logo & new chat button
│   │   ├── ChatInput.vue     # Message input with send button
│   │   ├── ChatMessage.vue   # Individual message bubble
│   │   └── WelcomeScreen.vue # Welcome screen with topic suggestions
│   ├── App.vue               # Main application component
│   └── main.js               # Application entry point
├── index.html                # HTML template
├── package.json              # Dependencies & scripts
├── vite.config.js            # Vite configuration
└── README.md                 # This file
```

## API Configuration

The application connects to the backend at `http://localhost:8081/api`.

### SSE Endpoint

- **URL**: `GET /ai/chat`
- **Query Parameters**:
  - `memoryId` (int): Session identifier for conversation context
  - `message` (string): User's message (URL-encoded)

### Example Request

```
GET http://localhost:8081/api/ai/chat?memoryId=123456789&message=Hello%20world
```

## Key Features

### Session Management

- `memoryId` is auto-generated on first visit
- Stored in `localStorage` for persistence
- "New Chat" button creates a fresh session

### Message Input

- Press `Enter` to send message
- Press `Shift + Enter` for new line
- Input disabled during AI response streaming

### Streaming Response

- Real-time character-by-character rendering
- Visual indicator while streaming
- Graceful error handling with retry option

### Error Handling

- Connection errors shown in chat
- Retry button for failed messages
- Automatic cleanup of failed streams

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## Development Notes

### Adding New API Endpoints

1. Add the endpoint configuration in `src/api/index.js`
2. Create a new service file if needed
3. Import and use in components

### Styling Customization

All CSS variables are defined in `src/assets/styles/main.css`:

```css
:root {
  --color-primary: #2563EB;
  --color-secondary: #3B82F6;
  --color-background: #F8FAFC;
  /* ... more variables */
}
```

### Proxy Configuration

For development, API requests are proxied through Vite to avoid CORS issues:

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
}
```

## Troubleshooting

### "Connection lost" error

1. Ensure the backend server is running at `http://localhost:8081`
2. Check browser console for network errors
3. Verify CORS is configured on the backend

### Messages not persisting

The app uses `localStorage` for `memoryId` only. Message history is managed by the backend through the `memoryId` session.

### Styling issues

Clear browser cache or try incognito mode to ensure latest CSS is loaded.

## License

MIT
