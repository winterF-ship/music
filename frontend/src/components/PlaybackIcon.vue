<script setup>
defineProps({ playing: { type: Boolean, default: false } })
</script>

<template>
  <svg class="playback-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <template v-if="playing">
      <rect x="5" y="3" width="5.4" height="18" rx="2.7" fill="currentColor" />
      <rect x="13.6" y="3" width="5.4" height="18" rx="2.7" fill="currentColor" />
    </template>
    <path v-else d="M6 3.8C6 2.84 7.06 2.27 7.86 2.79L21.11 11.04C21.83 11.49 21.83 12.51 21.11 12.96L7.86 21.21C7.06 21.73 6 21.16 6 20.2V3.8Z" fill="currentColor" />
  </svg>
</template>
