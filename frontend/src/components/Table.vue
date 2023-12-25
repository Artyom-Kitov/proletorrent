<script>
export default {
  data() {
    return {
      options: { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric', hour: "numeric", minute:"numeric" }
    }
  },
  props: {
    files: {
      type: Array,
    },
    filter: {
      type: Number,
    },
    server: {
      type: String
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
  <colgroup>
    <col span="1" style="width: 5%;">
    <col span="1" style="width: 37%;">
    <col span="1" style="width: 10%;">
    <col span="1" style="width: 16%;">
    <col span="1" style="width: 16%;">
    <col span="1" style="width: 16%;">
  </colgroup>
  <tr><th class="th_edge">№№</th>
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
    <th class="th_edge">
      {{ $t('table-header.created-at') }}
    </th>
  </tr>
  <tr :class="(index % 2 === 0) ? 'tr_odd_light' : ''" v-for="(file, index) in files" v-show="(file.status === this.filter) || (this.filter === -1)" >
    <td>
      {{ index+1 }}
    </td>
    <td class="name_cell">
      {{ file.name }}
      <a v-show="file.status === 1" class="download_file" :href="'http://localhost:8081/api/download/'.concat(file.name)">🡻</a>
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
      {{ new Date(file.createdAt).toLocaleString('en-GB', { timeZone: 'UTC' })}}
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
  border-top: none;
}

.th_edge {
  border-left: none;
  border-right: none;
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

  .download_file {
    border: #ffbf1c solid 2px;
    color: #ffbf1c;
  }

  progress {
    accent-color: #ffbf1c;
  }
}

.download_file {
  border-radius: 10px;
  border: #960000 solid 2px;
  color: #960000;
  width: 30px;
  text-decoration: none;
}

</style>