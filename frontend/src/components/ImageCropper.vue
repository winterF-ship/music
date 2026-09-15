<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, required: true },
  aspectRatio: { type: Number, default: 2.4 },
  outputWidth: { type: Number, default: 1200 },
})
const emit = defineEmits(['confirm', 'cancel'])
const canvas = ref()
const image = ref()
const zoom = ref(1)
const offset = ref({ x: 0, y: 0 })
const dragging = ref(false)
const pointerStart = ref({ x: 0, y: 0 })
const offsetStart = ref({ x: 0, y: 0 })
const stageWidth = 520
const stageHeight = computed(() => Math.round(stageWidth / props.aspectRatio))
const displayScale = ref(1)

function draw() {
  const target = canvas.value
  const source = image.value
  if (!target || !source) return
  const context = target.getContext('2d')
  if (!context) return
  target.width = stageWidth
  target.height = stageHeight.value
  const baseScale = Math.max(stageWidth / source.naturalWidth, stageHeight.value / source.naturalHeight)
  displayScale.value = baseScale * zoom.value
  const width = source.naturalWidth * displayScale.value
  const height = source.naturalHeight * displayScale.value
  const maxX = Math.max(0, (width - stageWidth) / 2)
  const maxY = Math.max(0, (height - stageHeight.value) / 2)
  offset.value.x = Math.min(maxX, Math.max(-maxX, offset.value.x))
  offset.value.y = Math.min(maxY, Math.max(-maxY, offset.value.y))
  context.clearRect(0, 0, stageWidth, stageHeight.value)
  context.fillStyle = '#dfe9e0'
  context.fillRect(0, 0, stageWidth, stageHeight.value)
  context.drawImage(source, (stageWidth - width) / 2 + offset.value.x, (stageHeight.value - height) / 2 + offset.value.y, width, height)
}

function loadImage() {
  const next = new Image()
  next.onload = () => { image.value = next; zoom.value = 1; offset.value = { x: 0, y: 0 }; draw() }
  next.src = props.src
}
function startDrag(event) { dragging.value = true; pointerStart.value = { x: event.clientX, y: event.clientY }; offsetStart.value = { ...offset.value }; event.currentTarget?.setPointerCapture?.(event.pointerId) }
function moveDrag(event) { if (!dragging.value) return; offset.value = { x: offsetStart.value.x + event.clientX - pointerStart.value.x, y: offsetStart.value.y + event.clientY - pointerStart.value.y }; draw() }
function endDrag() { dragging.value = false }
function confirm() {
  const source = image.value
  if (!source) return
  const output = document.createElement('canvas')
  output.width = props.outputWidth
  output.height = Math.round(props.outputWidth / props.aspectRatio)
  const context = output.getContext('2d')
  if (!context) return
  const scale = Math.max(output.width / source.naturalWidth, output.height / source.naturalHeight) * zoom.value
  const width = source.naturalWidth * scale
  const height = source.naturalHeight * scale
  const ratio = output.width / stageWidth
  context.drawImage(source, (output.width - width) / 2 + offset.value.x * ratio, (output.height - height) / 2 + offset.value.y * ratio, width, height)
  output.toBlob((blob) => { if (blob) emit('confirm', blob) }, 'image/jpeg', .9)
}
watch(() => props.src, loadImage)
watch(zoom, draw)
onMounted(loadImage)
onBeforeUnmount(() => { image.value = undefined })
</script>

<template>
  <div class="image-cropper">
    <div class="crop-stage" :style="{ aspectRatio: String(aspectRatio) }" @pointerdown="startDrag" @pointermove="moveDrag" @pointerup="endDrag" @pointercancel="endDrag" @pointerleave="endDrag"><canvas ref="canvas" /><div class="crop-frame" aria-hidden="true" /></div>
    <div class="crop-toolbar"><span>拖动图片调整位置</span><el-slider v-model="zoom" :min="1" :max="3" :step=".05" :show-tooltip="false" aria-label="图片缩放" /><span>{{ Math.round(zoom * 100) }}%</span></div>
    <div class="crop-actions"><el-button @click="emit('cancel')">取消</el-button><el-button type="primary" @click="confirm">裁剪并上传</el-button></div>
  </div>
</template>
