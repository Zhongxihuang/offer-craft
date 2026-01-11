<template>
  <div class="welcome-screen">
    <div class="welcome-icon" aria-hidden="true">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M16 18l2-2-2-2" />
        <path d="M8 6l-2 2 2 2" />
        <path d="M14.5 4l-5 16" />
      </svg>
    </div>
    <h2>Welcome to Code Forge AI</h2>
    <p class="welcome-description">
      Your personal assistant for programming learning and career development.
      Ask me anything about:
    </p>
    <div class="topic-cards">
      <button 
        class="topic-card" 
        v-for="topic in topics" 
        :key="topic.title"
        @click="$emit('select-topic', topic.prompt)"
      >
        <div class="topic-icon" v-html="topic.icon"></div>
        <div class="topic-content">
          <h3>{{ topic.title }}</h3>
          <p>{{ topic.description }}</p>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
defineEmits(['select-topic'])

const topics = [
  {
    title: 'Programming Roadmaps',
    description: 'Get guidance on learning paths for different technologies',
    prompt: 'What is a good roadmap for learning full-stack web development?',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="4.5" r="2.5"/>
      <path d="M12 7v3"/>
      <circle cx="5" cy="19.5" r="2.5"/>
      <path d="M5 17v-3"/>
      <circle cx="19" cy="19.5" r="2.5"/>
      <path d="M19 17v-3"/>
      <path d="M12 10l-7 4"/>
      <path d="M12 10l7 4"/>
    </svg>`
  },
  {
    title: 'Interview Preparation',
    description: 'Practice technical and behavioral interview questions',
    prompt: 'Can you give me some common Java interview questions for senior developers?',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>`
  },
  {
    title: 'Code Review & Debug',
    description: 'Get help with code issues and best practices',
    prompt: 'Can you help me understand how to optimize a slow database query?',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 20h9"/>
      <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
    </svg>`
  },
  {
    title: 'Resume & Career Tips',
    description: 'Improve your resume and job search strategy',
    prompt: 'What should I include in my software developer resume to stand out?',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/>
      <path d="M14 2v4a2 2 0 0 0 2 2h4"/>
      <path d="M10 9H8"/>
      <path d="M16 13H8"/>
      <path d="M16 17H8"/>
    </svg>`
  }
]
</script>

<style scoped>
.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl) var(--spacing-lg);
  text-align: center;
  min-height: 400px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-lg);
}

.welcome-icon svg {
  width: 48px;
  height: 48px;
  color: white;
}

.welcome-screen h2 {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.welcome-description {
  font-size: 1rem;
  color: var(--color-text-muted);
  max-width: 500px;
  margin-bottom: var(--spacing-xl);
}

.topic-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--spacing-md);
  max-width: 700px;
  width: 100%;
}

.topic-card {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  text-align: left;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal), transform var(--transition-fast);
  cursor: pointer;
}

.topic-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.topic-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(37, 99, 235, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-primary);
}

.topic-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

.topic-content h3 {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.topic-content p {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .welcome-screen {
    padding: var(--spacing-xl) var(--spacing-md);
  }
  
  .welcome-icon {
    width: 64px;
    height: 64px;
  }
  
  .welcome-icon svg {
    width: 36px;
    height: 36px;
  }
  
  .welcome-screen h2 {
    font-size: 1.5rem;
  }
  
  .topic-cards {
    grid-template-columns: 1fr;
  }
}
</style>
