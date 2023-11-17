<script>
  import HeaderComponent from "@/components/HeaderComponent.vue";
  import Tab from "@/components/Tab.vue"
  export default {
    components: {
      HeaderComponent, Tab
    },
    data() {
      return {
        selectedArr: [true, false, false, false],
        theme: true
      }
    },
    methods: {
      falseAll() {
        this.selectedArr = this.selectedArr.map(e => false);
      },
      select(num){
        this.falseAll();
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
        <div class="add_btn_container"><button class="add_btn_light">
          <img class="add_pic" src="../imgs/plus_icon.svg" alt="">
          добавить
        </button>
        </div>
        <Tab @click="select(0)" v-bind:is-selected="selectedArr[0]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/all.svg" alt="" :class="[this.selectedArr[0] ? 'img_clicked' : 'img']">
          все
        </Tab>
        <Tab @click="select(1)" v-bind:is-selected="selectedArr[1]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/down.svg" alt="" :class="[this.selectedArr[1] ? 'img_clicked' : 'img']">
          скачиваются
        </Tab>
        <Tab @click="select(2)" v-bind:is-selected="selectedArr[2]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/share.svg" alt="" :class="[this.selectedArr[2] ? 'img_clicked' : 'img']">
          раздаются
        </Tab>
        <Tab @click="select(3)" v-bind:is-selected="selectedArr[3]" v-bind:theme="this.theme">
          <img class="img" src="../imgs/tab_icons/paused.svg" alt="" :class="[this.selectedArr[3] ? 'img_clicked' : 'img']">
          простаивают
        </Tab>
        <div class="pic_container"><img class="big_logo" src="../imgs/logo512q.svg" alt=""></div>
        <a href="#settings" class="settings_btn">
          <img class="settings_pic" src="../imgs/customize.svg" alt="">
          <div class="settings_text">настройки</div>
        </a>
      </div>

      <div class="main_light">
        <div id="about" class="modal">
          <div class="content">
            <img class="star" src="../imgs/star.png" alt="star">
            <h2 class="modal_title">☆Оцените труд работяг звездой на Github!☆</h2>
            <a class="git_link" href="https://github.com/Artyom-Kitov/proletorrent" target="_blank">
              Посеить репозиторий
            </a>
            <a href="#" class="box-close">×</a>
          </div>
        </div>

        <div id="settings" class="modal">
          <div class="settings_content">
            <h2 class="settings_header">Параметры</h2>
            <div class="theme_container">
              Тема приложения:
              <div>
                *toggle*
              </div>
            </div>
            <div class="theme_container">
              Язык приложения:
              <div>
                *toggle*
              </div>
            </div>
            <div class="feedback">
              <a class="feedback" href="mailto:e.syroezhkin@g.nsu.ru">Сообщите о проблемах или предложениях!</a>
            </div>

            <a href="#" class="box-close">×</a>
          </div>
        </div>

        <div class="container">
          {{selectedArr}}
          <div v-if="selectedArr[0]">табличка со всем</div>
          <div v-if="selectedArr[1]">табличка с скачками</div>
          <div v-if="selectedArr[2]">табличка с раздачами</div>
          <div v-if="selectedArr[3]">табличка с простойкой</div>
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