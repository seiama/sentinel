<script setup>
const branch = __GIT_BRANCH__
const commit = __GIT_COMMIT__
const commitShort = __GIT_COMMIT_SHORT__
</script>

<template>
  <div class="w-100 h-100 d-flex align-center justify-space-between">
    <!-- 👉 Footer: left content -->
    <span class="d-flex align-center">
      &copy; {{ new Date().getFullYear() }} Seiama
    </span>
    <span class="d-md-flex gap-x-4 text-primary d-none">
      <a
        href="https://github.com/seiama/sentinel/commit/{{ commit }}"
        target="noopener"
      >{{ commitShort }} on {{ branch }}</a>
    </span>
  </div>
</template>
