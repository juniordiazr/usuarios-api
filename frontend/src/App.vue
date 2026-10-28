<script setup>
import { reactive, ref } from 'vue'

const form = reactive({
  name: '',
  email: '',
  password: '',
  phones: [{ number: '', citycode: '', contrycode: '' }]
})
const loading = ref(false)
const error = ref('')
const result = ref(null)

const addPhone = () => form.phones.push({ number: '', citycode: '', contrycode: '' })
const removePhone = (i) => form.phones.splice(i, 1)

async function submit() {
  error.value = ''
  result.value = null
  loading.value = true
  try {
    const res = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form)
    })
    const data = await res.json()
    if (res.ok) result.value = data
    else error.value = data.mensaje
  } catch (e) {
    error.value = 'No se pudo conectar con el servidor'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="card">
    <h1>Registro de usuario</h1>
    <form @submit.prevent="submit">
      <label>Nombre <input v-model="form.name" required /></label>
      <label>Correo <input v-model="form.email" type="email" placeholder="aaaaaaa@dominio.cl" required /></label>
      <label>Contraseña <input v-model="form.password" type="password" required /></label>

      <h2>Teléfonos</h2>
      <div v-for="(p, i) in form.phones" :key="i" class="phone">
        <input v-model="p.number" placeholder="Número" />
        <input v-model="p.citycode" placeholder="Cód. ciudad" />
        <input v-model="p.contrycode" placeholder="Cód. país" />
        <button type="button" @click="removePhone(i)">✕</button>
      </div>
      <button type="button" @click="addPhone">+ Agregar teléfono</button>

      <button type="submit" :disabled="loading">{{ loading ? 'Enviando...' : 'Registrar' }}</button>
    </form>

    <p v-if="error" class="error">{{ error }}</p>
    <pre v-if="result" class="ok">{{ JSON.stringify(result, null, 2) }}</pre>
  </main>
</template>

<style>
body { font-family: system-ui, sans-serif; background: #f4f5f7; margin: 0; }
.card { max-width: 520px; margin: 2rem auto; background: #fff; padding: 1.5rem; border-radius: 8px; }
form { display: flex; flex-direction: column; gap: .75rem; }
label { display: flex; flex-direction: column; gap: .25rem; font-size: .9rem; }
input { padding: .5rem; border: 1px solid #ccc; border-radius: 4px; }
.phone { display: flex; gap: .5rem; }
button { padding: .5rem 1rem; cursor: pointer; }
.error { color: #b00020; }
.ok { background: #eef7ee; padding: .75rem; overflow: auto; }
</style>
