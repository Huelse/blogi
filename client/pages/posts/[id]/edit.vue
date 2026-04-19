<script setup lang="ts">
const route = useRoute()

await navigateTo(`/admin/posts/${route.params.id}/edit`, { replace: true })
</script>
