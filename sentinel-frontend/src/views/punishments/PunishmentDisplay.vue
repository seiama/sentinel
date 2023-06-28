<template>
  <VCard>
    <VRow no-gutters>
      <VCol
        cols="12"
        sm="8"
        md="1"
        lg="10"
        order="2"
        order-lg="1"
      >
        <VCardItem>
          <VCardTitle>Punishment Details</VCardTitle>
        </VCardItem>

        <VCardText>
          <b>Reason:</b> {{ punishment.reason }}
        </VCardText>

        <VCardText>
          <VDivider />
        </VCardText>

        <VCardText class="d-flex justify-center">
          <div class="me-auto pe-4">
            <p class="d-flex align-center mb-6">
              <VIcon
                color="primary"
                icon="mdi-fingerprint"
              />
              <span class="ms-3"><b>ID:</b> {{ punishment._id }}</span>
            </p>

            <p class="d-flex align-center mb-6">
              <VIcon
                color="primary"
                icon="mdi-account"
              />
              <span class="ms-3"><b>User:</b> {{ punishment.punished_username }}{{ punishment.punished_discriminator ? `#${punishment.punished_discriminator}` : `` }} ({{ punishment.punished_id }})</span>
            </p>

            <p class="d-flex align-center mb-6">
              <VIcon
                color="primary"
                icon="mdi-calendar-clock"
              />
              <span class="ms-3"><b>Date:</b> {{ punishment.date }}</span>
            </p>

            <p class="d-flex align-center mb-0">
              <VIcon
                color="primary"
                :icon="staleIcon"
              />
              <span class="ms-3"><b>Stale:</b> {{ punishment.stale || "false" }}</span>
            </p>
          </div>
        </VCardText>
      </VCol>
    </VRow>
  </VCard>
</template>

<script type="ts">
import { getPunishment } from "@/views/punishments/punishments"
import { useRoute } from "vue-router"

export default {
  data () {
    return {
      punishment: {},
    }
  },
  computed: {
    staleIcon() {
      return this.punishment['stale'] ? "mdi-lock-open-outline" : "mdi-lock";
    },
  },
  async created() {
    try {
      const route = useRoute()
      this.punishment = await getPunishment(route.query['id'])
    } catch (e) {
      console.log(e);
      this.punishment = {_id: "no"}
    }
  }
}
</script>
