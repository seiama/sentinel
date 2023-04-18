<template>
  <v-row>
    <v-col cols='12'>
      <FactoidsHeader />
    </v-col>

    <v-col cols='12'>
      <FactoidsTable />
    </v-col>
  </v-row>
</template>

<script type="ts">
import FactoidsHeader from "@/views/factoids/FactoidsHeader.vue";
import FactoidsTable from "@/views/factoids/FactoidsTable.vue";

export default {
  components: { FactoidsHeader, FactoidsTable },
}
</script>
