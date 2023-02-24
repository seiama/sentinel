<template>
  <v-data-table
    v-model:items-per-page="itemsPerPage"
    v-model:search="search"
    v-model:show-expand="showExpand"
    v-model:sort-by="sortBy"
    :headers="filteredHeaders"
    :items="filteredPunishments"
    item-value="_id"
    class="elevation-1"
  >
    <template v-slot:top>
      <div class='d-flex'>
        <v-text-field
          v-model='search'
          label='Search'
          variant='solo'
        ></v-text-field>
        <v-chip
          v-for='(value, type) in filters'
          :key='type'
          closable
          class='align-self-center mr-2'
          @click:close='removeFilter(type)'
        >{{ type + ': ' + value }}
        </v-chip>
      </div>
    </template>
    <template v-slot:item.date="{ item }">
      {{ formatDate(item.raw.date )}}
    </template>
    <template v-slot:item.type="{ item }">
      <v-chip
        small
        :color='typeColor[item.raw.type]'
        class='font-weight-medium'
      >
        {{ item.raw.type }}
      </v-chip>
      <v-icon class='float-end' @click='filter("type", item.raw.type)' icon="mdi-search-web"></v-icon>
    </template>
    <template v-slot:item.punished_username="{ item }">
      {{ item.raw.punished_username }}#{{ item.raw.punished_discriminator }}
      <v-icon class='float-end' @click='filter("punished_id", item.raw.punished_id)' icon="mdi-search-web">
      </v-icon>
    </template>
    <template v-slot:item.stale="{ item }">
      <v-switch
        :input-value='item.raw.stale || false'
        disabled
        inset
      ></v-switch>
    </template>
    <template v-slot:item.punisher_username="{ item }">
      {{ item.raw.punisher_username }}#{{ item.raw.punisher_discriminator }}
      <v-icon class='float-end' @click='filter("punisher_id", item.raw.punisher_id)' icon="mdi-search-web">
      </v-icon>
    </template>
    <template v-slot:expanded-row='{ columns, item }'>
      <td :colspan='columns.length'>
        <v-table class='flipped' density="compact">
          <tr v-for='key in Object.keys(item.raw)' :key='key'>
            <th>{{ key }}</th>
            <td>{{ item.raw[key] }}</td>
          </tr>
        </v-table>
      </td>
    </template>
  </v-data-table>
</template>

<script type="ts">
import { getPunishments } from '@/views/punishments/punishments'

export default {
  data () {
    return {
      itemsPerPage: 15,
      showExpand: true,
      sortBy: [
        { key: 'date', order: 'desc' }
      ],
      headers: [
        { title: 'ID', align: 'start', sortable: false, key: '_id' },
        { title: 'DATE', align: 'start', key: 'date' },
        { title: 'TYPE', align: 'start', key: 'type' },
        { title: 'PUNISHED', align: 'start', key: 'punished_username' },
        { title: 'REASON', align: 'start', key: 'reason' },
        { title: 'STALE', align: 'start', key: 'stale' },
        { title: 'PUNISHER', align: 'start', key: 'punisher_username' },
      ],
      punishments: [],
      typeColor: {
        'BAN': 'punishment-ban',
        'KICK': 'punishment-kick',
        'MUTE': 'punishment-mute',
        'NOTE': 'punishment-note',
        'WARN': 'punishment-warn',
      },
      search: '',
      filters: {},
    }
  },
  computed: {
    filteredHeaders() {
      return this.headers.filter((h) => !this.filters[h.key])
    },
    filteredPunishments() {
      return this.punishments.filter((p) => {
        for (const key of Object.keys(this.filters)) {
          if (p[key] !== this.filters[key]) {
            return false
          }
        }
        return true
      })
    },
  },
  async created() {
    try {
      this.punishments = await getPunishments()
    } catch (e) {
      this.punishments = []
    }
  },
  methods: {
    formatDate(date) {
      const dt = new Date(date)
      return dt.toLocaleDateString("en-SE") + " " + dt.toLocaleTimeString()
    },
    filter(field, value) {
      this.filters[field] = value
    },
    removeFilter(field) {
      delete this.filters[field]
    },
  },
}
</script>

<style>
.flipped td,
.flipped th {
  text-align: left;
  padding: 0 1rem 0 1rem;
}

.flipped table {
  width: fit-content !important;
}
</style>
