<script setup>
import { reactive, ref } from 'vue'

const emptyPhone = () => ({ number: '', citycode: '', contrycode: '' })

const form = reactive({ name: '', email: '', password: '', phones: [emptyPhone()] })
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const result = ref(null)
const copied = ref(false)

const addPhone = () => form.phones.push(emptyPhone())
const removePhone = (i) => form.phones.splice(i, 1)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const payload = {
      name: form.name,
      email: form.email,
      password: form.password,
      phones: form.phones.filter((p) => p.number || p.citycode || p.contrycode)
    }
    const res = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (res.ok) result.value = data
    else error.value = data.mensaje
  } catch (e) {
    error.value = 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.'
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(form, { name: '', email: '', password: '', phones: [emptyPhone()] })
  result.value = null
  error.value = ''
  copied.value = false
  showPassword.value = false
}

const formatDate = (iso) => new Date(iso).toLocaleString('es-CL')

async function copyToken() {
  try {
    await navigator.clipboard.writeText(result.value.token)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
  } catch (e) {
    copied.value = false
  }
}
</script>

<template>
  <div class="page">
    <section class="shell">
      <aside class="intro">
        <h1>Gestión de Usuarios</h1>
        <p class="lead">Completa tus datos para obtener acceso a la plataforma.</p>
      </aside>

      <div class="panel">
        <form v-if="!result" @submit.prevent="submit" novalidate>
          <div class="field">
            <label for="name">Nombre completo</label>
            <input id="name" v-model="form.name" autocomplete="name" placeholder="Juan Rodríguez" required />
          </div>

          <div class="field">
            <label for="email">Correo electrónico</label>
            <input id="email" v-model="form.email" type="email" autocomplete="email" placeholder="nombre@empresa.cl" required />
          </div>

          <div class="field">
            <label for="password">Contraseña</label>
            <div class="password">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                required
              />
              <button type="button" class="link" @click="showPassword = !showPassword">
                {{ showPassword ? 'Ocultar' : 'Mostrar' }}
              </button>
            </div>
          </div>

          <fieldset class="phones">
            <legend>Teléfonos</legend>
            <div v-for="(p, i) in form.phones" :key="i" class="phone">
              <input v-model="p.number" class="phone-number" inputmode="tel" placeholder="Número" aria-label="Número de teléfono" />
              <input v-model="p.citycode" inputmode="numeric" placeholder="Ciudad" aria-label="Código de ciudad" />
              <input v-model="p.contrycode" inputmode="numeric" placeholder="País" aria-label="Código de país" />
              <button type="button" class="icon" @click="removePhone(i)" aria-label="Quitar teléfono">×</button>
            </div>
            <button type="button" class="ghost" @click="addPhone">Agregar teléfono</button>
          </fieldset>

          <p v-if="error" class="error" role="alert">{{ error }}</p>

          <button type="submit" class="primary" :disabled="loading">
            {{ loading ? 'Registrando…' : 'Registrar usuario' }}
          </button>
        </form>

        <div v-else class="success">
          <h2>Usuario registrado</h2>
          <p class="muted">La cuenta de {{ result.name }} quedó activa.</p>

          <dl>
            <div><dt>Correo</dt><dd>{{ result.email }}</dd></div>
            <div><dt>Identificador</dt><dd class="wrap">{{ result.id }}</dd></div>
            <div><dt>Creado</dt><dd>{{ formatDate(result.created) }}</dd></div>
            <div><dt>Último ingreso</dt><dd>{{ formatDate(result.last_login) }}</dd></div>
            <div><dt>Estado</dt><dd>{{ result.isactive ? 'Activo' : 'Inactivo' }}</dd></div>
            <div><dt>Token</dt><dd class="token">{{ result.token }}</dd></div>
          </dl>

          <div class="actions">
            <button type="button" class="ghost" @click="copyToken">{{ copied ? 'Token copiado' : 'Copiar token' }}</button>
            <button type="button" class="primary" @click="reset">Registrar otro usuario</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style>
:root {
  --ink: #17303a;
  --lagoon: #1f7a80;
  --lagoon-dark: #16595e;
  --mist: #edf2f2;
  --paper: #ffffff;
  --line: #d3dddf;
  --text: #1b2b30;
  --muted: #5b6e73;
  --error: #a8261d;
  --error-bg: #fbeceb;
}

*, *::before, *::after { box-sizing: border-box; }

body {
  margin: 0;
  background: var(--mist);
  color: var(--text);
  font-family: 'Segoe UI Variable', 'Segoe UI', system-ui, -apple-system, Roboto, sans-serif;
  line-height: 1.5;
}

.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: clamp(1rem, 4vw, 3rem);
}

.shell {
  width: 100%;
  max-width: 560px;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  background: var(--paper);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(23, 48, 58, 0.14);
}

/* Panel lateral */
.intro {
  background: var(--ink);
  color: #e8f0f1;
  padding: 1.75rem 1.75rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.6rem;
}
.intro h1 {
  margin: 0;
  font-family: 'Iowan Old Style', 'Palatino Linotype', Palatino, Georgia, serif;
  font-weight: 600;
  font-size: clamp(1.6rem, 6vw, 2rem);
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.lead { margin: 0; color: #b9ccd0; font-size: 1.05rem; }
/* Formulario */
.panel { padding: clamp(1.25rem, 5vw, 2rem); min-width: 0; }
form { display: grid; gap: 1.1rem; }

.field { display: grid; gap: 0.35rem; min-width: 0; }
label, legend { font-size: 0.9rem; font-weight: 600; color: var(--text); }

input {
  width: 100%;
  min-width: 0;
  padding: 0.7rem 0.85rem;
  font: inherit;
  color: var(--text);
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 8px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input::placeholder { color: #93a4a8; }
input:hover { border-color: #b5c4c7; }
input:focus-visible,
button:focus-visible {
  outline: 3px solid rgba(31, 122, 128, 0.35);
  outline-offset: 1px;
  border-color: var(--lagoon);
}

.password { position: relative; }
.password input { padding-right: 5.5rem; }
.password .link {
  position: absolute;
  top: 50%;
  right: 0.5rem;
  transform: translateY(-50%);
}

/* Teléfonos */
.phones { border: 0; padding: 0; margin: 0.25rem 0 0; display: grid; gap: 0.6rem; min-width: 0; }
.phones legend { padding: 0; margin-bottom: 0.35rem; }
.phone {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr) minmax(0, 1fr) auto;
  gap: 0.5rem;
  align-items: center;
}

/* Botones */
button { font: inherit; cursor: pointer; border-radius: 8px; }
.primary {
  padding: 0.8rem 1.25rem;
  font-weight: 600;
  color: #fff;
  background: var(--lagoon);
  border: 1px solid var(--lagoon);
  transition: background 0.15s;
}
.primary:hover:not(:disabled) { background: var(--lagoon-dark); }
.primary:disabled { opacity: 0.6; cursor: progress; }

.ghost {
  padding: 0.65rem 1rem;
  color: var(--lagoon-dark);
  background: transparent;
  border: 1px dashed #9db9bc;
  justify-self: start;
}
.ghost:hover { background: #f1f7f7; }

.icon {
  width: 2.4rem;
  height: 2.4rem;
  font-size: 1.25rem;
  line-height: 1;
  color: var(--muted);
  background: transparent;
  border: 1px solid var(--line);
}
.icon:hover { color: var(--error); border-color: var(--error); }

.link {
  padding: 0.25rem 0.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--lagoon-dark);
  background: transparent;
  border: 0;
}

.error {
  margin: 0;
  padding: 0.75rem 0.9rem;
  color: var(--error);
  background: var(--error-bg);
  border-left: 3px solid var(--error);
  border-radius: 6px;
  font-size: 0.92rem;
}

/* Resultado */
.success h2 {
  margin: 0;
  font-family: 'Iowan Old Style', 'Palatino Linotype', Palatino, Georgia, serif;
  font-size: 1.75rem;
}
.muted { color: var(--muted); margin: 0.25rem 0 1.25rem; }
dl { margin: 0 0 1.5rem; display: grid; }
dl > div {
  display: grid;
  grid-template-columns: minmax(0, 9rem) minmax(0, 1fr);
  gap: 0.75rem;
  padding: 0.65rem 0;
  border-bottom: 1px solid var(--line);
}
dt { color: var(--muted); font-size: 0.9rem; }
dd { margin: 0; min-width: 0; overflow-wrap: anywhere; }
.token {
  font-family: ui-monospace, 'Cascadia Mono', Consolas, monospace;
  font-size: 0.8rem;
  max-height: 4.5rem;
  overflow: auto;
}
.actions { display: flex; flex-wrap: wrap; gap: 0.75rem; }

/* Responsivo */
@media (max-width: 480px) {
  .phone { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto; }
  .phone-number { grid-column: 1 / -1; }
  .phone .icon { grid-column: 3; }
  dl > div { grid-template-columns: minmax(0, 1fr); gap: 0.15rem; }
  .actions > button { width: 100%; }
}

@media (prefers-reduced-motion: reduce) {
  * { transition: none !important; }
}
</style>