<template>
  <main>
    <section aria-labelledby="title">
      <p class="eyebrow">CAPACITOR + NUXT</p>
      <h1 id="title">VoIP Prototype</h1>
      <p>Hello from the Capacitor WebView.</p>
      <div class="push-debug">
        <p class="label">Firebase test-device token</p>
        <p class="status">{{ tokenStatus }}</p>
        <code v-if="registrationToken">{{ registrationToken }}</code>
        <button type="button" :disabled="loadingToken" @click="loadRegistrationToken">
          {{ loadingToken ? 'Loading…' : 'Refresh FCM token' }}
        </button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { Capacitor, registerPlugin } from '@capacitor/core'

interface PushDebugPlugin {
  getRegistrationToken(): Promise<{ token: string }>
}

const PushDebug = registerPlugin<PushDebugPlugin>('PushDebug')
const registrationToken = ref('')
const tokenStatus = ref('Firebase is not checked yet.')
const loadingToken = ref(false)

async function loadRegistrationToken() {
  if (!Capacitor.isNativePlatform()) {
    tokenStatus.value = 'FCM tokens are available only in the installed Android app.'
    return
  }

  loadingToken.value = true
  tokenStatus.value = 'Requesting the Android FCM registration token…'

  try {
    const { token } = await PushDebug.getRegistrationToken()
    registrationToken.value = token
    tokenStatus.value = 'Copy this token into the test-send command.'
  } catch (error) {
    registrationToken.value = ''
    tokenStatus.value = error instanceof Error ? error.message : 'FCM token request failed.'
  } finally {
    loadingToken.value = false
  }
}

onMounted(loadRegistrationToken)
</script>

<style>
:root {
  color: #f7f8ff;
  background: #10131a;
  font-family: Inter, Roboto, system-ui, sans-serif;
}

* { box-sizing: border-box; }

body { margin: 0; }

main {
  display: grid;
  min-height: 100dvh;
  place-items: center;
  padding: 24px;
}

section {
  width: min(100%, 420px);
  padding: 32px;
  border: 1px solid #303748;
  border-radius: 20px;
  background: #191e29;
}

.eyebrow {
  margin: 0 0 8px;
  color: #9bb3ff;
  font-size: .75rem;
  font-weight: 700;
  letter-spacing: .12em;
}

h1 { margin: 0 0 12px; font-size: 2rem; }
p { color: #c6cad6; line-height: 1.5; }

.push-debug {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #303748;
}

.label {
  margin: 0;
  color: #f7f8ff;
  font-size: .875rem;
  font-weight: 700;
}

.status { margin: 8px 0; font-size: .875rem; }

code {
  display: block;
  overflow-wrap: anywhere;
  padding: 10px;
  border-radius: 8px;
  background: #10131a;
  color: #9bb3ff;
  font-size: .75rem;
}

button {
  margin-top: 12px;
  padding: 10px 14px;
  border: 0;
  border-radius: 8px;
  background: #9bb3ff;
  color: #10131a;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
}

button:disabled { cursor: wait; opacity: .6; }
</style>
