<template>
  <div>
    <div class="section-title">{{ $t('m.Parent_Binding') }}</div>
    <div class="section-main">
      <p class="desc">{{ $t('m.Bind_Code_Description') }}</p>

      <!-- 未生成：显示生成按钮 -->
      <el-button
        v-if="!bindCode"
        type="primary"
        :loading="loading"
        @click="generate"
        >{{ $t('m.Generate_Bind_Code') }}</el-button
      >

      <!-- 已生成：绑定码 + 复制/重新生成 + 倒计时 + 小程序码 -->
      <template v-else>
        <div class="bind-code">{{ bindCode }}</div>
        <div class="bind-actions">
          <el-button
            size="mini"
            icon="el-icon-document-copy"
            @click="doCopy"
            >{{ $t('m.Copy') }}</el-button
          >
          <el-button size="mini" :loading="loading" @click="generate"
            >{{ $t('m.Regenerate') }}</el-button
          >
        </div>
        <div v-if="remainingSeconds > 0" class="expire">
          {{ $t('m.Bind_Code_Expire_In') }}: {{ countdownText }}
        </div>
        <div v-if="qrCodeUrl" class="qrcode">
          <img :src="qrCodeUrl" alt="qrcode" />
          <p class="qrcode-tip">{{ $t('m.Bind_Code_Scan_Tip') }}</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';

export default {
  data() {
    return {
      loading: false,
      bindCode: '',
      qrCodeUrl: '',
      expireIn: 0,
      remainingSeconds: 0,
      timer: null,
    };
  },
  computed: {
    countdownText() {
      const m = Math.floor(this.remainingSeconds / 60);
      const s = this.remainingSeconds % 60;
      return `${m}:${String(s).padStart(2, '0')}`;
    },
  },
  beforeDestroy() {
    this.clearTimer();
  },
  methods: {
    generate() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      api.generateBindCode().then(
        (res) => {
          this.loading = false;
          const data = res.data.data || {};
          this.bindCode = data.bindCode || '';
          this.qrCodeUrl = data.qrCodeUrl || '';
          this.expireIn = data.expireIn || 0;
          this.startCountdown();
        },
        () => {
          this.loading = false;
        }
      );
    },
    startCountdown() {
      this.clearTimer();
      this.remainingSeconds = this.expireIn || 0;
      if (this.remainingSeconds <= 0) {
        return;
      }
      this.timer = setInterval(() => {
        this.remainingSeconds -= 1;
        if (this.remainingSeconds <= 0) {
          this.clearTimer();
          myMessage.warning(this.$i18n.t('m.Bind_Code_Expired'));
        }
      }, 1000);
    },
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    doCopy() {
      this.$copyText(this.bindCode).then(
        () => {
          myMessage.success(this.$i18n.t('m.Copied_successfully'));
        },
        () => {
          myMessage.success(this.$i18n.t('m.Copied_failed'));
        }
      );
    },
  },
};
</script>

<style scoped>
.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
  text-align: center;
}
.section-main {
  text-align: center;
}
.desc {
  color: #909399;
  font-size: 13px;
  margin-bottom: 20px;
}
.bind-code {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 6px;
  color: #409eff;
  margin: 10px 0;
  font-family: 'Courier New', monospace;
}
.bind-actions {
  margin: 10px 0;
}
.expire {
  color: #e6a23c;
  font-size: 13px;
  margin: 5px 0;
}
.qrcode {
  margin: 15px 0 10px;
}
.qrcode img {
  width: 220px;
  height: 220px;
  box-shadow: 0 0 1px 0;
}
.qrcode-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
}
</style>
