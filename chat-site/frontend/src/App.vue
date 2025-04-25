<template>
  <div id="app">
    <h1>WebSocket Chat Test</h1>
    <WebSocketTest />
  </div>
</template>

<script>
import WebSocketTest from './components/WebSocketTest.vue'

export default {
  name: 'App',
  components: {
    WebSocketTest
  }
}
</script>

<style>
#app {
  font-family: Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>