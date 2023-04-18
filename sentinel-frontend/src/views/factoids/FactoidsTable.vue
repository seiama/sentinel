<template>
<v-data-table
  v-model:items-per-page="itemsPerPage"
  v-model:search="search"
  v-model:show-expand="showExpand"
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
  <template v-slot:item.name="{ item }">
    {{ item.raw.name }}
  </template>
  <template v-slot:item.description="{ item }">
    {{ item.raw.description }}
  </template>
  <template v-slot:item.embed="{ item }">
    <v-switch
      :model-value='item.raw.response.content === null'
      disabled
      inset
    ></v-switch>
  </template>
  <template v-slot:expanded-row="{ item }">
    <div v-if="!item.raw.response.content" class="embed-container">
      <div class="discord-embed">
        <div v-for="embed in item.raw.response.embeds" :key="embed.title" class="embed">
          <div class="embed-header">
            <h4 class="embed-title">{{ embed.title }}</h4>
            <img v-if="embed.footer && embed.footer.icon_url" :src="embed.footer.icon_url" class="embed-icon" />
          </div>
          <p class="embed-description">{{ embed.description }}</p>
          <div class="embed-footer">
            <p class="embed-text">{{ embed.footer && embed.footer.text }}</p>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="content-container">
      <p class="content">{{ item.raw.response.content }}</p>
    </div>
  </template>
</v-data-table>
</template>

<script>
import { getFactoids} from "@/views/factoids/factoids";

export default {
  data() {
    return {
      itemsPerPage: 15,
      showExpand: true,
      factoids: [],
      headers: [
        { title: 'ID', align: 'start', sortable: false, key: '_id' },
        { title: 'NAME', align: 'start', key: 'name' },
        { title: 'DESCRIPTION', align: 'start', key: 'description' },
        { title: 'EMBED', align: 'start', key: 'embed' },
      ],
      search: '',
      filters: {},
    }
  },
  computed: {
    filteredHeaders() {
      return this.headers.filter((h) => !this.filters[h.key])
    },
    filteredPunishments() {
      return this.factoids.filter((p) => {
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
      this.factoids = await getFactoids();
    } catch (e) {
      this.factoids = [];
    }
  },
  methods: {
    filter(field, value) {
      this.filters[field] = value
    },
    removeFilter(field) {
      delete this.filters[field]
    },
  }
}
</script>

<style scoped>
.discord-embed {
  background-color: #36393f;
  color: #ffffff;
  border-radius: 5px;
  padding: 10px;
  max-width: 600px;
}

.discord-message {
  margin-bottom: 20px;
}

.description {
  margin-top: 0;
  font-weight: bold;
}

.embed-container {
  border-radius: 5px;
  padding: 10px;
}

.embed {
  border-left: 4px solid #7289da;
  padding-left: 10px;
  margin-bottom: 10px;
}

.embed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 5px;
}

.embed-title {
  margin: 0;
}

.embed-icon {
  width: 16px;
  height: 16px;
}

.embed-description {
  margin-top: 0;
  margin-bottom: 10px;
}

.embed-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.embed-text {
  margin: 0;
  font-size: 12px;
}

.content-container {
  margin-top: 10px;
  padding: 10px;
  border-radius: 5px;
  background-color: #2f3136;
}

.content {
  margin-top: 0;
}
</style>
