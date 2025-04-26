<template>
  <div id="app">
    <header class="chat-header">
      <h1>WebSocket Chat</h1>
    </header>
    <main>
      <WebSocketTest />
    </main>
  </div>
</template>

<script>
import WebSocketTest from './components/WebSocketTest.vue';

export default {
  name: 'App',
  components: {
    WebSocketTest,
  },
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: #f0f2f5;
}

#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background-color: #1a73e8;
  color: white;
  padding: 1rem;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.chat-header h1 {
  font-size: 1.8rem;
  font-weight: 500;
}

main {
  flex: 1;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>