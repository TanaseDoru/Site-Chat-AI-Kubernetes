<template>
  <div>
    <h2>Chat Room</h2>
    <ul>
      <li v-for="(msg, index) in messages" :key="index">
        {{ msg.text }} ({{ msg.timestamp }})
      </li>
    </ul>
    <input v-model="message" placeholder="Type a message" />
    <button @click="sendMessage">Send</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      message: '',
      messages: []
    };
  },
  methods: {
    sendMessage() {
      if (this.message) {
        const msg = {
          text: this.message,
          timestamp: new Date().toLocaleTimeString()
        };
        this.messages.push(msg);
        this.message = ''; // Clear the input
      }
    }
  }
};
</script>

<style scoped>
ul {
  list-style-type: none;
  padding: 0;
}
li {
  margin: 5px 0;
}
</style>
