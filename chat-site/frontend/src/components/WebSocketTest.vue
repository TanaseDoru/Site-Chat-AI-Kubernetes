<template>
  <div class="chat-container">
    <!-- Username Prompt -->
    <div v-if="!username" class="username-prompt">
      <h2>Welcome to the Chat!</h2>
      <input
        v-model="usernameInput"
        placeholder="Enter your username..."
        @keyup.enter="setUsername"
      />
      <button @click="setUsername" :disabled="!usernameInput.trim()">Join Chat</button>
    </div>

    <!-- Chat Interface -->
    <div v-else class="chat-interface">
      <div class="chat-header">
        <span>Chat as {{ username }}</span>
        <span class="status" :class="{ connected: isConnected }">
          {{ isConnected ? 'Online' : 'Offline' }}
        </span>
      </div>

      <div class="chat-layout">
        <div class="user-list">
          <h3>Connected Users</h3>
          <div class="user-list-content">
            <div v-for="(user, index) in connectedUsers" :key="index" class="user-item">
              {{ user }}
            </div>
          </div>
        </div>

        <div class="chat-content">
      <div class="chat-messages">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message"
              :class="{ 
                'self-message': message.isSelf, 
                'system-message': message.isSystem,
                'leave-message': message.isLeave
              }"
        >
          {{ message.text }}
        </div>
      </div>

      <div class="chat-input">
            <div class="input-container">
        <input
          v-model="message"
          placeholder="Type a message..."
          @keyup.enter="sendMessage"
                @input="handleInput"
                :class="{ 'max-letters': letterCount >= 50 }"
        />
              <div class="letter-counter" :class="{ 'max-letters': letterCount >= 50 }">
                {{ letterCount }}/50
              </div>
            </div>
            <button 
              @click="sendMessage" 
              :disabled="!isConnected || !message.trim() || letterCount >= 50"
            >
          Send
        </button>
          </div>
        </div>
      </div>

      <!-- Debug Panel -->
      <button class="debug-toggle" @click="toggleDebug">
        {{ showDebug ? 'Hide Debug' : 'Show Debug' }}
      </button>
      <div class="debug-panel" :class="{ 'debug-panel-open': showDebug }">
        <h3>Debug Logs</h3>
        <div class="debug-logs">
          <div v-for="(log, index) in logs" :key="index" class="debug-entry">
            {{ log }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WebSocketTest',
  data() {
    return {
      socket: null,
      isConnected: false,
      username: '',
      usernameInput: '',
      message: '',
      messages: [],
      logs: [],
      showDebug: false,
      connectedUsers: [],
    };
  },
  computed: {
    letterCount() {
      return this.message.length;
    }
  },
  methods: {
    addLog(message) {
      this.logs.unshift(message);
    },
    scrollToBottom() {
      const chatMessages = this.$el.querySelector('.chat-messages');
      if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
      }
    },
    setUsername() {
      if (this.usernameInput.trim()) {
        this.username = this.usernameInput.trim();
        this.connect();
      }
    },
    connect() {
      this.addLog('Attempting to connect...');
      try {
        this.socket = new WebSocket(`ws://localhost:8080/chat?username=${encodeURIComponent(this.username)}`);

        this.socket.onopen = () => {
          this.isConnected = true;
          this.addLog('Connection established!');
          this.$nextTick(() => {
            this.scrollToBottom();
          });
        };

        this.socket.onmessage = (event) => {
          const messageText = event.data;
          let formattedMessage;
          let isSystem = false;
          let isLeave = false;

          // Handle user list updates
          if (messageText.startsWith('USERLIST:')) {
            this.connectedUsers = messageText.substring(9).split(',').filter(user => user);
            return;
          }

          // Check if the message is a system message (e.g., "user joined" or "user left")
          if (messageText.includes('joined the chat') || messageText.includes('left the chat')) {
            formattedMessage = messageText;
            isSystem = true;
            isLeave = messageText.includes('left the chat');
          } else {
            formattedMessage = messageText;
          }

          this.messages.push({ 
            text: formattedMessage, 
            isSelf: false, 
            isSystem,
            isLeave 
          });

          this.$nextTick(() => {
            this.scrollToBottom();
          });
        };

        this.socket.onclose = (event) => {
          this.isConnected = false;
          this.addLog(`Connection closed: ${event.code} - ${event.reason || 'No reason provided'}`);
          // Only attempt to reconnect if the connection was not closed by the user
          if (event.code !== 1000) {
            setTimeout(() => this.connect(), 5000);
          }
        };

        this.socket.onerror = (error) => {
          this.addLog(`WebSocket error: ${error}`);
        };
      } catch (error) {
        this.addLog(`Failed to connect: ${error.message}`);
        setTimeout(() => this.connect(), 5000);
      }
    },
    sendMessage() {
      if (this.isConnected && this.message.trim()) {
        // Truncate message to 50 letters
        const truncatedMessage = this.message.slice(0, 50);
        if (this.message.length > 50) {
          this.addLog('Message was truncated to 50 letters');
        }
        
        // Add local timestamp for sender's view
        const now = new Date();
        const timestamp = now.toISOString().replace('T', ' ').substring(0, 19);
        const localMessage = `[${timestamp}] ${this.username}: ${truncatedMessage}`;
        
        // Send message without timestamp to server
        const serverMessage = `${this.username}: ${truncatedMessage}`;
        this.socket.send(serverMessage);
        
        // Add message to local view with timestamp
        this.messages.push({
          text: localMessage,
          isSelf: true,
          isSystem: false,
        });
        this.addLog(`Sent message: ${localMessage}`);
        this.message = '';
        
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    toggleDebug() {
      this.showDebug = !this.showDebug;
    },
    handleInput(event) {
      if (this.message.length >= 50 && event.inputType !== 'deleteContentBackward' && event.inputType !== 'deleteContentForward') {
        event.preventDefault();
        return;
      }
      this.message = event.target.value;
    },
  },
  beforeUnmount() {
    if (this.socket) {
      this.socket.close();
    }
  },
};
</script>

<style scoped>
.chat-container {
  width: 100%;
  max-width: 100%;
  margin: 0;
  background: white;
  border-radius: 0;      /* flush to edges on small */
  box-shadow: none;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Username Prompt */
.username-prompt {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.username-prompt h2 {
  margin-bottom: 20px;
  color: #333;
}

.username-prompt input {
  padding: 10px;
  width: 100%;
  max-width: 300px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1rem;
}

.username-prompt button {
  padding: 10px 20px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
}

.username-prompt button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Chat Interface */
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.chat-header {
  padding: 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header span {
  font-size: 1rem;
  color: #555;
}

.status {
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 0.9rem;
  background-color: #ff4444;
  color: white;
}

.status.connected {
  background-color: #44ff44;
}

.chat-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.user-list {
  width: 200px;
  min-width: 150px;
  background-color: #f8f9fa;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.user-list h3 {
  padding: 15px;
  margin: 0;
  border-bottom: 1px solid #ddd;
  font-size: 1rem;
  color: #333;
}

.user-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.user-item {
  padding: 8px 12px;
  margin: 4px 0;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0; /* This helps with text overflow */
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message {
  padding: 10px 15px;
  border-radius: 15px;
  max-width: 80%;
  word-wrap: break-word;
  background-color: #e9ecef;
  align-self: flex-start;
  margin: 5px 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.self-message {
  background-color: #1a73e8;
  color: white;
  align-self: flex-end;
}

.system-message {
  background-color: #d4edda;
  color: #155724;
  align-self: center;
  max-width: 100%;
  text-align: center;
}

.system-message.leave-message {
  background-color: #f8d7da;
  color: #721c24;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #ddd;
  display: flex;
  gap: 10px;
  background: white;
}

.input-container {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  min-width: 0; /* This helps with text overflow */
}

.chat-input input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1rem;
  padding-right: 50px;
  min-width: 0; /* This helps with text overflow */
}

.chat-input input.max-letters {
  border-color: #dc3545;
  background-color: #fff8f8;
}

.letter-counter {
  position: absolute;
  right: 10px;
  color: #666;
  font-size: 0.9rem;
  pointer-events: none;
}

.letter-counter.max-letters {
  color: #dc3545;
  font-weight: bold;
}

.chat-input button {
  padding: 10px 20px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.chat-input button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Debug Panel */
.debug-toggle {
  margin: 15px;
  padding: 8px 16px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.debug-toggle:hover {
  background-color: #5a6268;
}

.debug-panel {
  height: 0;
  background: #f8f9fa;
  border-top: 1px solid #ddd;
  overflow: hidden;
  transition: height 0.3s ease;
}

.debug-panel-open {
  height: 200px;
}

.debug-panel h3 {
  padding: 10px 15px;
  font-size: 1rem;
  color: #333;
}

.debug-logs {
  height: 150px;
  overflow-y: auto;
  padding: 0 15px 15px;
  background: #f5f5f5;
}

.debug-entry {
  font-family: monospace;
  margin: 5px 0;
  white-space: pre-wrap;
  font-size: 0.9rem;
  color: #555;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .chat-container {
    max-width: 100%;
    border-radius: 0;
  }

  .chat-interface {
    height: 100vh;
  }

  .user-list {
    width: 150px;
    min-width: 120px;
  }

  .message {
    max-width: 90%;
  }
}

@media (max-width: 480px) {
  .user-list {
    display: none;
  }

  .chat-input {
    flex-direction: column;
  }

  .chat-input button {
    width: 100%;
  }
}
</style>