<script setup>
  import { useI18n } from 'vue-i18n'

  const { t, locale } = useI18n({useScope: 'global'});

  const switchLang = () => {
    let e = document.getElementById('lang');
    let val = e.options[e.selectedIndex].text;
    console.log(val);
    val === t('language.ru') ? locale.value = 'ru' : locale.value = 'en'
  }

  function onChange(event) {
    switchLang();
  }
</script>
<script>
  import HeaderComponent from "@/components/HeaderComponent.vue";
  import Tab from "@/components/Tab.vue"
  import Table from "@/components/Table.vue"

  export default {
    components: {
      HeaderComponent, Tab, Table
    },
    data() {
      return {
        selectedArr: [true, false, false, false],
        theme: true,
        files: [
          {id:44, name:"bible", size:68198, progress:100, status:"sharing", date:"06/06/2023"}
        ]
      }
    },
    methods: {
      select(num){
        this.selectedArr = this.selectedArr.map(e => false);
        this.selectedArr[num] = true;
      },
      changeTheme(){
        this.theme = !this.theme;
      }
    }
  }
</script>

<template>
  <header >
    <HeaderComponent/>
  </header>

  <main>
    <div class="container">
      <div class="sidebar">
        <div class="add_btn_container">
          <button class="add_btn_light">
          <img class="add_pic" src="../imgs/plus_icon.svg" alt="">
            {{ $t('add-button') }}
        </button>
        </div>
        <Tab @click="select(0)" v-bind:is-selected="selectedArr[0]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/all.svg" alt="" :class="[this.selectedArr[0] ? 'img_clicked' : 'img']">
          {{ $t('tabs.all') }}
        </Tab>
        <Tab @click="select(1)" v-bind:is-selected="selectedArr[1]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/down.svg" alt="" :class="[this.selectedArr[1] ? 'img_clicked' : 'img']">
          {{ $t('tabs.downloading') }}
        </Tab>
        <Tab @click="select(2)" v-bind:is-selected="selectedArr[2]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/share.svg" alt="" :class="[this.selectedArr[2] ? 'img_clicked' : 'img']">
          {{ $t('tabs.sharing') }}
        </Tab>
        <Tab @click="select(3)" v-bind:is-selected="selectedArr[3]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/paused.svg" alt="" :class="[this.selectedArr[3] ? 'img_clicked' : 'img']">
          {{ $t('tabs.paused') }}
        </Tab>
        <div class="pic_container"><img class="big_logo" src="../imgs/logo512q.svg" alt=""></div>
        <a href="#settings" class="settings_btn">
          <img class="settings_pic" src="../imgs/customize.svg" alt="">
          <div class="settings_text">
            {{ $t('settings-button') }}
          </div>
        </a>
      </div>

      <div class="main_light">
        <div id="about" class="modal">
          <div class="content">
            <img class="star" src="../imgs/star.png" alt="star">
            <h2 class="modal_title">☆{{ $t('git-modal.message') }}☆</h2>
            <a class="git_link" href="https://github.com/Artyom-Kitov/proletorrent" onClick="window.open('https://ru.wikipedia.org/wiki/SOCKS'); return true;" target="_blank">
              {{ $t('git-modal.git-link') }}
            </a>
            <a href="#" class="box-close">×</a>
          </div>
        </div>


        <div id="settings" class="modal">
          <div class="settings_content">
            <h2 class="settings_header">{{ $t('settings-modal.settings-title') }}</h2>
            <div class="theme_container">
              <img class="settings_icon" src="../imgs/brush.svg" alt="">
              {{ $t('settings-modal.theme-toggle') }}:
              <div>
                *toggle*
              </div>
            </div>
            <div class="theme_container">
              <img class="settings_icon" src="../imgs/globe.svg" alt="">
              {{ $t('settings-modal.lang-toggle') }}:
              <div>
                <select id="lang" class="lang_select" @change="switchLang">
                  <option>{{ $t('language.ru') }}</option>
                  <option>{{ $t('language.en') }}</option>
                </select>
              </div>
            </div>
            <div class="feedback">
              <a class="feedback" href="mailto:e.syroezhkin@g.nsu.ru;a.kitov@g.nsu.ru" >
                {{ $t('settings-modal.feedback-link') }}
              </a>
            </div>

            <a href="#" class="box-close">×</a>
          </div>
        </div>

        <div v-if="selectedArr[0]" class="container">
          <Table :files="files" :filter="'all'"></Table>
        </div>
        <div v-if="selectedArr[1]" class="container">
          <Table :files="files" :filter="'downloading'"></Table>
        </div>
        <div v-if="selectedArr[2]" class="container">
          <Table :files="files" :filter="'sharing'"></Table>
        </div>
        <div v-if="selectedArr[3]" class="container">
          <Table :files="files" :filter="'paused'"></Table>
        </div>


      </div>
    </div>
  </main>


</template>

<style>
@import "/css/head_styles.css";
@import "/css/git_window.css";
@import "/css/settings_window.css";
@import "/css/git_window_dark.css";
@import "/css/side.css";
@import "/css/main.css";

.body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #cf0000 9.57%, #960000 100%) fixed;
}

.container,.container_dark{
  display: flex;
  flex-direction: row;
  height: 86.8vh;

  font-family: Courier New,serif;
  font-size: 16px;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  
}

.container {
  color: black;
}

.container_dark {
  color: white;
}

.img,.img_clicked {
  padding-right: 5px;
}

.img {
  filter: invert(86%) sepia(38%) saturate(3641%) hue-rotate(358deg) brightness(106%) contrast(105%);
}

.img_clicked {
  filter: invert(9%) sepia(100%) saturate(6831%) hue-rotate(8deg) brightness(93%) contrast(113%);
}

</style>