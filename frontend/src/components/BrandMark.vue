<script setup>
defineProps({ size: { type: Number, default: 38 } })
</script>

<template>
  <svg class="brand-mark-svg" :width="size" :height="size" viewBox="0 0 48 48" fill="none" aria-hidden="true">
    <rect width="48" height="48" rx="16" fill="currentColor" />
    <circle cx="21" cy="25" r="10" stroke="#F3FEE7" stroke-width="2.6" />
    <circle cx="21" cy="25" r="2.6" fill="#6ED3CF" />
    <path d="M32.5 14.5V25.5C32.5 29.6 29.1 33 25 33" stroke="#6ED3CF" stroke-width="2.6" stroke-linecap="round" />
    <circle cx="32.5" cy="14.5" r="2.2" fill="#F3FEE7" />
  </svg>
</template>
