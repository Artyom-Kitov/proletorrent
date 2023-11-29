<script>
export default {
  props: {
    isSelected: {
      type: Boolean,
      required: true,
    }
  }
}
</script>

<template>
  <div :class="[this.isSelected ? 'tab_clicked' : 'tab']">
  <div class="tab_padding_light" v-if="this.isSelected"></div>
  <slot></slot>
  </div>
</template>

<style>
.tab,.tab_clicked{

  margin: 5px 0 5px 10px;
  padding-left: 10px;

  font-family: Arial, serif;
  font-size: 16px;
  font-style: normal;
  font-weight: 700;
  line-height: 100%; /* 24.22px */
  display: flex;
  align-items: center;
}

.tab {
  color: #FFD600;
  height: 30px;
}

.tab_clicked{
  margin: 10px 0 10px 10px;
  color: #DB0000;
  cursor: default;
}

.tab:hover {
  background: rgba(255, 250, 151, 0.20);
  border-radius: 10px;
  cursor: pointer;
}

.tab_padding_light{
  position: absolute;
  z-index: -1;
  height: 50px;
  width: 300px;
  border-radius: 20px;
  left: 20px;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.25);
  background-color: white;
}

.dark {
  .tab_padding_light {
    background-color: #393939;
  }

  .tab_clicked {
    color: #ff3c3c;
  }
}


</style>