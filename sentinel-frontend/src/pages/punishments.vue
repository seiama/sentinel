<template>
  <v-row>
    <v-col cols='12'>
      <PunishmentsHeader />
    </v-col>

    <v-col cols='12'>
      <PunishmentsTable />
    </v-col>
  </v-row>
</template>

<script type="ts">
import PunishmentsHeader from '@/views/punishments/PunishmentsHeader.vue'
import PunishmentsTable from '@/views/punishments/PunishmentsTable.vue'

export default {
  components: { PunishmentsTable, PunishmentsHeader },
}
</script>

<style scoped>

</style>
