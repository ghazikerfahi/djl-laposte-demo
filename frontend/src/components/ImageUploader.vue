<template>
  <div class="container">
    <h2>Reconnaissance d'image</h2>
    <input type="file" @change="onFileChange" />
    <div class="grid-buttons">
      <button @click="uploadImage" :disabled="!file">Analyser</button>
      <button @click="customUploadImage" :disabled="!file">Analyser avec custom DJL</button>
    </div>
    <p v-if="result">Résultat : {{ result }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const file = ref(null)
const result = ref('')

function onFileChange(e) {
  file.value = e.target.files[0]
}

async function uploadImage() {
  const formData = new FormData()
  formData.append('file', file.value)

  try {
    const res = await axios.post('http://localhost:8080/api/image/classify', formData)
    result.value = res.data
  } catch (error) {
    result.value = 'Erreur lors de la classification'
    console.error(error)
  }
}

async function customUploadImage() {
  const formData = new FormData()
  formData.append('file', file.value)

  try {
    const res = await axios.post('http://localhost:8080/api/image/customClassify', formData)
    result.value = res.data
  } catch (error) {
    result.value = 'Erreur lors de la classification'
    console.error(error)
  }
}
</script>

<style>
body {
    background-color: #d5c008;
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 20px;
  }

  h2 {
    color: #003366; /* Bleu foncé La Poste */
    text-align: center;
  }

  .container {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    max-width: 800px;
    margin: auto;
    box-shadow: 0 0 10px rgba(0,0,0,0.1);
  }

  .grid-buttons {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 20px;
  }

  button {
    font-size: 14px;
    padding: 8px 16px;
    background-color: #003366;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    min-width: 200px
  }

  button:hover {
    background-color: #002244;
  }

  p {
    margin-top: 20px;
    font-weight: bold;
    text-align: center;
  }
</style>