<script setup lang="ts">
import logo from '@/assets/images/logo.png'
import { VerticalNavLink, VerticalNavSectionTitle } from '@layouts'
import { useTheme } from 'vuetify'
</script>

<template>
  <!-- 👉 Nav header -->
  <div class="nav-header">
    <RouterLink
      to="/"
      class="app-logo d-flex align-center gap-x-3 app-title-wrapper"
    >
      <div>
        <VImg
          :src="logo"
          class="logo"
          width="50px"
          height="50px"
        />
      </div>

      <Transition name="vertical-nav-app-title">
        <h1 class="font-weight-semibold leading-normal text-xl">
          Sentinel
        </h1>
      </Transition>
    </RouterLink>
  </div>

  <!-- 👉 Nav items -->
  <ul>
    <VerticalNavLink
      :item="{
        title: 'Home',
        to: 'index',
        icon: { icon: 'mdi-home-outline' }
      }"
    />
    <VerticalNavSectionTitle :item="{ heading: 'Features' }" />
    <VerticalNavLink
      :item="{
        title: 'Punishments',
        to: 'punishments',
        icon: { icon: 'mdi-gavel' }
      }"
    />
    <VerticalNavLink
      :item="{
        title: 'Factoids',
        to: 'factoids',
        icon: { icon: 'mdi-message-question' }
      }"
    />
  </ul>
</template>

<style lang="scss">
// .v-navigation-drawer {
//   height: 100%;

//   .v-navigation-drawer__content {
//     display: flex;
//     flex-direction: column;

//     > ul {
//       flex-grow: 1;
//     }
//   }
// }

.upgrade-banner {
  margin-top: auto;
  // position: absolute;
  // bottom: 13px;
  // left: 50%;
  // transform: translateX(-50%);
}
</style>
