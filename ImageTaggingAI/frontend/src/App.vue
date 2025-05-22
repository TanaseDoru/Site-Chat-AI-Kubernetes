<template>
  <div id="app">
    <div class="container">
      <h1>Image Tagging</h1>

      <div class="upload-section">
        <input type="file" @change="handleFileChange" accept="image/*" />
        <button @click="uploadImage" :disabled="!file">Procesează</button>
        <button @click="fetchHistory">Vezi Istoric</button>
      </div>

      <div class="content">
        <div class="preview-card" v-if="currentImage.url">
          <img :src="currentImage.url" alt="Preview" />
          <div class="tags-list">
            <h2>Tag-uri găsite:</h2>
            <ul>
              <li v-for="(tag, i) in currentImage.tags" :key="i">
                {{ tag.name }} — {{ (tag.confidence * 100).toFixed(1) }}%
              </li>
            </ul>
          </div>
        </div>

        <div class="history-panel" v-if="showHistory">
          <h2>Istoric imagini</h2>
          <div v-if="history.length === 0">Nu există imagini procesate.</div>
          <div v-for="(item, i) in history" :key="i" class="history-card">
            <img :src="item.url" />
            <div class="history-tags">
              <ul>
                <li v-for="(tag, j) in item.tags" :key="j">
                  {{ tag.name }} — {{ (tag.confidence * 100).toFixed(1) }}%
                </li>
              </ul>
              <small>{{ new Date(item.uploadedAt).toLocaleString() }}</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      file: null,
      currentImage: { url: '', tags: [] },
      showHistory: false,
      history: []
    };
  },
  methods: {
    handleFileChange(e) {
      this.file = e.target.files[0];
      this.currentImage = { url: '', tags: [] };
    },
    async uploadImage() {
      if (!this.file) return;

      const form = new FormData();
      form.append('file', this.file);
      const api = `${window.location.protocol}//${window.location.hostname}:70/api`;

      try {
        const { data } = await axios.post(`${api}/upload`, form);
        this.currentImage.url = data.url;
        this.currentImage.tags = data.tags;
        this.file = null;
      } catch (err) {
        console.error('Upload error:', err);
      }
    },
    async fetchHistory() {
      const api = `${window.location.protocol}//${window.location.hostname}:70/api`;
      try {
        console.log('Fetching history from:', `${api}/history`);
        const { data } = await axios.get(`${api}/history`);
        this.history = data;
        this.showHistory = true;
        this.currentImage = { url: '', tags: [] }; // ← Ascunde poza curentă
      } catch (err) {
        console.error('Fetch history error:', err);
      }
    }

  }
};
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.container {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 1200px;
}

h1 {
  margin-bottom: 1.5rem;
  color: #333;
  text-align: center;
}

.upload-section {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 2rem;
}

.upload-section input[type='file'] {
  flex: 1 1 200px;
}

.upload-section button {
  background: #0078d7;
  color: #fff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.upload-section button:disabled {
  background: #a0a0a0;
  cursor: not-allowed;
}

.upload-section button:not(:disabled):hover {
  background: #005fa3;
}

.content {
  display: flex;
  flex-direction: row;
  gap: 2rem;
  flex-wrap: wrap;
}

.preview-card {
  flex: 1 1 400px;
}

.preview-card img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.tags-list h2 {
  margin-bottom: 0.5rem;
  color: #444;
}

.tags-list ul {
  list-style: none;
  padding: 0;
}

.tags-list li {
  background: #e1f5fe;
  margin-bottom: 0.4rem;
  padding: 0.5rem;
  border-radius: 4px;
  color: #0277bd;
}

.history-panel {
  flex: 1 1 400px;
  background: #f8f8f8f8;
  padding: 1rem;
  border-radius: 12px;
  overflow-y: auto;
  max-height: 600px;
  max-width: 400px;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.05);
  color: black
}

.history-card {
  margin-bottom: 1.5rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.history-card img {
  width: 80%;
  height: 80%;
  display: block;
  margin: 0 auto;
  object-fit: cover;
}

.history-tags {
  padding: 0.5rem 1rem;
}

.history-tags ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.history-tags li {
  font-size: 0.9rem;
  color: #555;
}

.history-tags small {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #999;
}

@media (max-width: 768px) {
  .content {
    flex-direction: column;
  }
}
</style>
