<script>
export default {
  props: {
    files: {
      type: Array,
      //required: true
    },
    filter: {
      type: Number,
      //required: true
    }
  },
  methods: {
    formatBytes(bytes, decimals) {
      if (!+bytes) return '0 Bytes'

      const k = 1024
      const dm = decimals < 0 ? 0 : decimals
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']

      const i = Math.floor(Math.log(bytes) / Math.log(k))

      return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`
    }
  }
}
</script>

<template>
<table class="tbl">
  <tr><th>№№</th>
    <th class="name_cell">
    {{ $t('table-header.name') }}
    </th>
    <th>
      {{ $t('table-header.size') }}
    </th>
    <th>
      {{ $t('table-header.progress') }}
    </th>
    <th>
      {{ $t('table-header.status') }}
    </th>
    <th>
      {{ $t('table-header.date') }}
    </th>
  </tr>
  <tr :class="(index % 2 === 0) ? 'tr_odd_light' : ''" v-for="(file, index) in files" v-show="(file.status === this.filter) || (this.filter === -1)" >
    <td>
      {{ index+1 }}
    </td>
    <td class="name_cell">
      {{ file.name }}
    </td>
    <td>
      {{ this.formatBytes(file.size, 2) }}
    </td>
    <td>
      {{Math.round(file.progress * 100) / 100}}%<progress class="progress" max="100" :value="file.progress"></progress>
    </td>
    <td>
      {{ $t('status.'.concat(file.status)) }}
    </td>
    <td>
      {{ file.date }}
    </td>
  </tr>
</table>
</template>

<style>
.tbl {
  width: 100%;
  height: max-content;
  border-collapse: collapse;
}

th {
  border: 1px solid #5b5b5b;
}

td,.name_cell {
  text-align: center;
  vertical-align: middle;
  word-wrap: break-word;
  height: 25px;
}

.name_cell {
  width: 200px;
}

progress {
  accent-color: #c70000;
}

.tr_odd_light {
  background-color: #eaeaea;
}

.dark {
  .tr_odd_light {
    background-color: #2a2a2a;
  }

  .th {
    border: 1px solid;
  }
}

</style>