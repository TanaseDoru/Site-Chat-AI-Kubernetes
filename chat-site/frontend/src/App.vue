<template>
  <div id="app">
    <ChatForm />
  </div>
</template>

<script>
import ChatForm from './components/ChatForm.vue';

export default {
  components: {
    ChatForm
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
