<template>
  <main>
    <section aria-labelledby="title">
      <p class="eyebrow">CAPACITOR + NUXT</p>
      <h1 id="title">VoIP Prototype</h1>
      <p>Hello from the Capacitor WebView.</p>
      <div v-if="call.state === 'active'" class="active-call" aria-live="polite">
        <div>
          <p class="label">Active call</p>
          <p class="caller">{{ call.caller }}</p>
          <p class="status">Call in progress</p>
        </div>
        <button type="button" class="hang-up" :disabled="hangingUp" @click="hangUp">
          {{ hangingUp ? 'Ending call…' : 'Hang up' }}
        </button>
      </div>
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
import { Capacitor, registerPlugin, type PluginListenerHandle } from '@capacitor/core'

interface PushDebugPlugin {
  getRegistrationToken(): Promise<{ token: string }>
}

type CallState = 'ringing' | 'active' | 'ended'

interface CallStatus {
  state: CallState
  callId?: string
  caller?: string
}

interface CallPlugin {
  getCurrentCall(): Promise<CallStatus>
  hangUp(): Promise<void>
  addListener(eventName: 'callStateChanged', listenerFunc: (event: CallStatus) => void): Promise<PluginListenerHandle>
}

const PushDebug = registerPlugin<PushDebugPlugin>('PushDebug')
const Call = registerPlugin<CallPlugin>('Call')
const registrationToken = ref('')
const tokenStatus = ref('Firebase is not checked yet.')
const loadingToken = ref(false)
const call = ref<CallStatus>({ state: 'ended' })
const hangingUp = ref(false)
let callStateListener: PluginListenerHandle | undefined

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

async function loadCallState() {
  if (!Capacitor.isNativePlatform()) {
    return
  }

  try {
    call.value = await Call.getCurrentCall()
  } catch (error) {
    console.error('Could not read native call state.', error)
  }
}

async function hangUp() {
  if (!Capacitor.isNativePlatform()) {
    return
  }

  hangingUp.value = true
  try {
    await Call.hangUp()
    call.value = { state: 'ended' }
  } catch (error) {
    console.error('Could not hang up the native call.', error)
  } finally {
    hangingUp.value = false
  }
}

onMounted(async () => {
  await loadCallState()
  if (Capacitor.isNativePlatform()) {
    callStateListener = await Call.addListener('callStateChanged', (event) => {
      call.value = event
    })
  }
  await loadRegistrationToken()
})

onUnmounted(() => {
  callStateListener?.remove()
})
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

.active-call {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 28px;
  padding: 18px;
  border: 1px solid #405b9d;
  border-radius: 14px;
  background: #202b49;
}

.active-call p { margin: 0; }

.caller {
  margin-top: 4px !important;
  color: #f7f8ff;
  font-size: 1.125rem;
  font-weight: 700;
}

.active-call .status { margin-top: 4px; }

.hang-up {
  flex: 0 0 auto;
  background: #ff8c8c;
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
