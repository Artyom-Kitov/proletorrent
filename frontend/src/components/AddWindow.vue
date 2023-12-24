<script>

import axios from "axios";
export default {
  data() {
    return {
      isReady: false,
      fileInfo: null
    }
  },
  methods: {
    send() {
      this.close();
      this.uploadFile();
      location.href = "#";
    },
    close() {
      this.isReady = false;
    },

    parseFile() {
      let fileInput = document.getElementById('fileInput');
      let file = fileInput.files[0];

      if (file) {
        let formData = new FormData();
        formData.append('torrent', file);

        axios.post('http://localhost:8081/api/info', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        })
            .then(response => {
              this.fileInfo = response.data;
              this.isReady = true;
            })
            .catch(response => {
              this.fileInfo = 'server error: '.concat(response.data());
              this.isReady = false;
            });
      } else {
        this.fileInfo = "no file"
        this.isReady = false;
      }
    },

    uploadFile() {
      let fileInput = document.getElementById('fileInput');
      let file = fileInput.files[0];

      if (file) {
        let formData = new FormData();
        formData.append('torrent', file);

        axios.post('http://localhost:8081/api/start-upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        })
            .then(response => {
              console.log(response.data);
              this.close()
            })
            .catch(error => {
              console.error(error);
            });
      } else {
        console.log('No file selected');
      }
    }
  }
}

</script>

<template>
  <a href="#" class="box-close" @click="close()">×</a>
  <h2 class="modal_header">{{ $t('add-modal.title') }}</h2>
  <div class="file_space">
    <input type="file" id="fileInput" accept=".torrent">
    <button class="file_button" @click="parseFile()"> {{ $t('add-modal.send-torrent') }} </button>
  </div>
  <div>{{ $t('add-modal.file-preview') }}</div>
  <div class="file_preview">
    {{fileInfo}}
  </div>
  <div class="start_container">
    <button class="start_button" :disabled="!this.isReady" @click="send()"> {{$t('add-modal.start')}} </button>
  </div>
</template>

<style>

.start_container {
  display: flex;
  justify-content: flex-end;
}

.file_space {
  margin-bottom: 10px;
}

.start_button, .file_button {
  text-align-all: center;
  border-radius: 10px;
  height: 30px;
  width: 120px;
  font-family: Courier New, serif;
  font-weight: bold;
  font-size: 16px;
}

.start_button {
  background-color: rgb(208, 0, 0);
  color: white;
  margin: 10px 0 0 0;
  border: 2px solid #8F0000;
}

.start_button:disabled {
  background-color: rgb(171, 171, 171);
  color: #393939;
  border-color: #7a7a7a;
  cursor: not-allowed;
}

.file_button {
  margin: 10px 20px 10px 0;
  background-color: white;
  border: 2px solid #ababab;
}

.start_button, .file_button:hover {
  cursor: pointer;
}

.file_preview {
  height: 50vh;
}

.file_preview, .file_space {
  background-color: #eaeaea;
  border-radius: 15px;
  border: 2px solid #e7e7e7;
}

.dark {
  .file_preview, .file_space {
    background-color: #2a2a2a;
    border: 2px solid #494949;
  }

}

</style>