import { createVuetify } from 'vuetify'
import { VDataTable } from 'vuetify/labs/VDataTable'
import defaults from './defaults'
import { icons } from './icons'
import theme from './theme'

// Styles
import '@core/scss/libs/vuetify/index.scss'
import 'vuetify/styles'

export default createVuetify({
  components: {
    VDataTable,
  },
  defaults,
  icons,
  theme,
})
