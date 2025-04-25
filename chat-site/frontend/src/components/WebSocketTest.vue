<template>
    <div class="websocket-test">
      <h2>WebSocket Test</h2>
      <div class="status" :class="{ connected: isConnected }">
        Status: {{ isConnected ? 'Connected' : 'Disconnected' }}
      </div>
      <div class="debug-log">
        <h3>Debug Log:</h3>
        <div class="log-entries">
          <div v-for="(log, index) in logs" :key="index" class="log-entry">
            {{ log }}
          </div>
        </div>
      </div>
      <div class="controls">
        <input v-model="message" placeholder="Type a message..." />
        <button @click="sendMessage" :disabled="!isConnected">Send</button>
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
        message: '',
        logs: []
      }
    },
    methods: {
      addLog(message) {
        const timestamp = new Date().toISOString()
        this.logs.unshift(`[${timestamp}] ${message}`)
      },
      connect() {
        this.addLog('Attempting to connect...')
        try {
          this.socket = new WebSocket('ws://localhost:8080/chat')
          
          this.socket.onopen = () => {
            this.isConnected = true
            this.addLog('Connection established!')
          }
          
          this.socket.onmessage = (event) => {
            this.addLog(`Received message: ${event.data}`)
          }
          
          this.socket.onclose = () => {
            this.isConnected = false
            this.addLog('Connection closed')
            // Try to reconnect after 5 seconds
            setTimeout(() => this.connect(), 5000)
          }
          
          this.socket.onerror = (error) => {
            this.addLog(`WebSocket error: ${error}`)
          }
        } catch (error) {
          this.addLog(`Failed to connect: ${error.message}`)
        }
      },
      sendMessage() {
        if (this.isConnected && this.message.trim()) {
          this.socket.send(this.message)
          this.addLog(`Sent message: ${this.message}`)
          this.message = ''
        }
      }
    },
    mounted() {
      this.connect()
    },
    beforeUnmount() {
      if (this.socket) {
        this.socket.close()
      }
    }
  }
  </script>
  
  <style scoped>
  .websocket-test {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
  }
  
  .status {
    padding: 10px;
    margin: 10px 0;
    background-color: #ff4444;
    color: white;
    border-radius: 4px;
  }
  
  .status.connected {
    background-color: #44ff44;
  }
  
  .debug-log {
    margin: 20px 0;
    border: 1px solid #ccc;
    padding: 10px;
    border-radius: 4px;
  }
  
  .log-entries {
    height: 300px;
    overflow-y: auto;
    background: #f5f5f5;
    padding: 10px;
  }
  
  .log-entry {
    font-family: monospace;
    margin: 5px 0;
    white-space: pre-wrap;
  }
  
  .controls {
    display: flex;
    gap: 10px;
    margin-top: 20px;
  }
  
  input {
    flex: 1;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  button {
    padding: 8px 16px;
    background-color: #4444ff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  button:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
  </style>