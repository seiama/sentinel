<template>
  <div class="misc-wrapper">
    <ErrorHeader
      error-code="404"
      error-title="Page Not Found"
      error-description="We couldn't find the page you are looking for."
    />

    <div class="misc-avatar w-100 text-center">
      <VBtn
        to="/"
        class="mt-10"
      >
        Back to Home
      </VBtn>
    </div>
  </div>
</template>

<style lang="scss">
@use "@core/scss/pages/misc.scss";
</style>

<route lang="yaml">
meta:
  layout: blank
  action: read
  subject: Auth
</route>
