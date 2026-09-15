<script setup>
import { computed } from 'vue'
import { assetUrl } from '../api/catalog'
const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  label: { type: String, default: '♪' },
  round: { type: Boolean, default: false },
})
const url = computed(() => assetUrl(props.src))
</script>
<template><div class="media-cover" :class="{ 'is-round': round }"><img v-if="url" :src="url" :alt="alt" loading="lazy" /><span v-else aria-hidden="true">{{ label?.slice(0, 2) || '♪' }}</span></div></template>
