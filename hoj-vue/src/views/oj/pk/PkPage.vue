<template>
  <div class="pk-page">
    <!-- 顶部栏: 倒计时 + 玩家信息 + 投降 -->
    <div class="pk-header">
      <div class="pk-player pk-player-left">
        <img :src="matchInfo.initiatorAvatar" class="pk-avatar" v-if="matchInfo.initiatorAvatar" />
        <span class="pk-player-name">{{ matchInfo.initiatorNickname || matchInfo.initiatorUsername }}</span>
        <span class="pk-player-score">PK积分: {{ matchInfo.initiatorPkScore || 0 }}</span>
      </div>
      <div class="pk-center">
        <div class="pk-countdown" :class="{ 'pk-countdown-warn': remainingSeconds <= 60 && pkStatus === 'running' }">
          <template v-if="pkStatus === 'waiting'">
            {{ $t('m.PK_Waiting') }}
          </template>
          <template v-else-if="pkStatus === 'running'">
            {{ formatTime(remainingSeconds) }}
          </template>
          <template v-else>
            {{ $t('m.PK_Match_Ended') }}
          </template>
        </div>
        <el-button
          type="danger"
          size="small"
          @click="surrender"
          :disabled="pkEnded"
          class="pk-surrender-btn"
        >
          {{ $t('m.PK_Surrender') }}
        </el-button>
      </div>
      <div class="pk-player pk-player-right">
        <img :src="matchInfo.opponentAvatar" class="pk-avatar" v-if="matchInfo.opponentAvatar" />
        <span class="pk-player-name">{{ matchInfo.opponentNickname || matchInfo.opponentUsername }}</span>
        <span class="pk-player-score">PK积分: {{ matchInfo.opponentPkScore || 0 }}</span>
      </div>
    </div>

    <!-- 主体区域: 题目 + 编辑器 -->
    <div class="pk-body" v-loading="loading">
      <div class="pk-problem-panel">
        <div class="pk-problem-title">{{ problemData.problem ? problemData.problem.title : '' }}</div>
        <div class="pk-problem-content" v-if="problemData.problem">
          <div class="pk-problem-section" v-if="problemData.problem.description">
            <h4>{{ $t('m.Problem_Description') }}</h4>
            <div v-katex v-html="problemData.problem.description"></div>
          </div>
          <div class="pk-problem-section" v-if="problemData.problem.input">
            <h4>{{ $t('m.Input_Description') }}</h4>
            <div v-katex v-html="problemData.problem.input"></div>
          </div>
          <div class="pk-problem-section" v-if="problemData.problem.output">
            <h4>{{ $t('m.Output_Description') }}</h4>
            <div v-katex v-html="problemData.problem.output"></div>
          </div>
          <div class="pk-problem-section" v-if="problemData.problem.examples && problemData.problem.examples.length > 0">
            <h4>{{ $t('m.Example') }}</h4>
            <div v-for="(example, index) in problemData.problem.examples" :key="index" class="pk-example">
              <div class="pk-example-item">
                <span class="pk-example-label">{{ $t('m.Input') }}:</span>
                <pre>{{ example.input }}</pre>
              </div>
              <div class="pk-example-item">
                <span class="pk-example-label">{{ $t('m.Output') }}:</span>
                <pre>{{ example.output }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="pk-editor-panel">
        <CodeMirror
          ref="pkEditor"
          :value.sync="code"
          :languages="problemData.languages || ['C', 'C++', 'Java', 'Python3', 'Python2']"
          :language.sync="language"
          :theme.sync="theme"
          :height="editorHeight"
          :fontSize.sync="fontSize"
          :tabSize.sync="tabSize"
          :isPkMode="true"
          :isAuthenticated="true"
          :pid="problemInternalId"
          :submitDisabled="pkStatus !== 'running'"
          :openTestCaseDrawer.sync="openTestCaseDrawer"
          :problemTestCase="problemData.problem ? problemData.problem.examples : []"
          @changeLang="onChangeLang"
          @changeTheme="onChangeTheme"
        ></CodeMirror>
        <div class="pk-editor-actions">
          <el-button
            type="primary"
            icon="el-icon-edit-outline"
            @click="submitCode"
            :loading="submitting"
            :disabled="pkStatus !== 'running'"
            size="small"
            class="fl-right"
          >
            <span v-if="submitting">{{ $t('m.Submitting') }}</span>
            <span v-else>{{ $t('m.Submit') }}</span>
          </el-button>
          <el-tag
            type="success"
            :class="openTestCaseDrawer ? 'tj-btn active' : 'tj-btn non-active'"
            @click.native="openTestJudgeDrawer"
            v-if="pkStatus === 'running'"
            effect="plain"
            class="fl-right"
            style="margin-right: 8px;"
          >
            <svg
              t="1653665263421"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="1656"
              width="12"
              height="12"
              style="vertical-align: middle;"
            >
              <path
                d="M1022.06544 583.40119c0 11.0558-4.034896 20.61962-12.111852 28.696576-8.077979 8.077979-17.639752 12.117992-28.690436 12.117992L838.446445 624.215758c0 72.690556-14.235213 134.320195-42.718941 184.89915l132.615367 133.26312c8.076956 8.065699 12.117992 17.634636 12.117992 28.690436 0 11.050684-4.034896 20.614503-12.117992 28.691459-7.653307 8.065699-17.209964 12.106736-28.690436 12.106736-11.475356 0-21.040199-4.041036-28.690436-12.106736L744.717737 874.15318c-2.124384 2.118244-5.308913 4.88424-9.558703 8.283664-4.259 3.3984-13.180184 9.463536-26.78504 18.171871-13.598716 8.715499-27.415396 16.473183-41.439808 23.276123-14.029528 6.797823-31.462572 12.966313-52.289923 18.49319-20.827351 5.517667-41.446971 8.28571-61.842487 8.28571L552.801776 379.38668l-81.611739 0 0 571.277058c-21.668509 0-43.250036-2.874467-64.707744-8.615215-21.473057-5.734608-39.960107-12.749372-55.476499-21.039175-15.518438-8.289804-29.541827-16.572444-42.077328-24.867364-12.541641-8.290827-21.781072-15.193027-27.739784-20.714787l-9.558703-8.93244L154.95056 998.479767c-8.500605 8.921183-18.699897 13.386892-30.606065 13.386892-10.201339 0-19.335371-3.40454-27.409257-10.202363-8.079002-7.652284-12.437264-17.10968-13.080923-28.372188-0.633427-11.263531 2.659573-21.143553 9.893324-29.647227l128.787178-144.727219c-24.650423-48.464805-36.980239-106.699114-36.980239-174.710091L42.738895 624.207571c-11.057847 0-20.61655-4.041036-28.690436-12.111852-8.079002-8.082072-12.120039-17.640776-12.120039-28.696576 0-11.050684 4.041036-20.61962 12.120039-28.689413 8.073886-8.072863 17.632589-12.107759 28.690436-12.107759l142.81466 0L185.553555 355.156836l-110.302175-110.302175c-8.074909-8.077979-12.113899-17.640776-12.113899-28.691459 0-11.04966 4.044106-20.61962 12.113899-28.690436 8.071839-8.076956 17.638729-12.123109 28.691459-12.123109 11.056823 0 20.612457 4.052293 28.692482 12.123109l110.302175 110.302175 538.128077 0 110.303198-110.302175c8.070816-8.076956 17.632589-12.123109 28.690436-12.123109 11.050684 0 20.617573 4.052293 28.689413 12.123109 8.077979 8.070816 12.119015 17.640776 12.119015 28.690436 0 11.050684-4.041036 20.614503-12.119015 28.691459l-110.302175 110.302175 0 187.448206 142.815683 0c11.0558 0 20.618597 4.034896 28.690436 12.113899 8.076956 8.069793 12.117992 17.638729 12.117992 28.683273l0 0L1022.06544 583.40119 1022.06544 583.40119zM716.021162 216.158085 307.968605 216.158085c0-56.526411 19.871583-104.667851 59.616796-144.414087 39.733956-39.746236 87.88256-59.611679 144.411017-59.611679 56.529481 0 104.678084 19.865443 144.413064 59.611679C696.156742 111.48921 716.021162 159.631674 716.021162 216.158085L716.021162 216.158085 716.021162 216.158085 716.021162 216.158085z"
                p-id="1657"
                :fill="openTestCaseDrawer ? '#ffffff' : '#67c23a'"
              >
              </path>
            </svg>
            <span style="vertical-align: middle;">
              {{ $t('m.Online_Test') }}
            </span>
          </el-tag>
          <div class="pk-status" v-if="statusVisible" style="clear: both; padding-top: 8px;">
            <template v-if="result.status == JUDGE_STATUS_RESERVE['sf']">
              <el-tag effect="dark" :color="submissionStatus.color" @click.native="reSubmit" style="cursor: pointer;">
                <i class="el-icon-refresh"></i> {{ submissionStatus.text }}
              </el-tag>
            </template>
            <template v-else-if="result.status == JUDGE_STATUS_RESERVE['snr']">
              <el-alert type="warning" show-icon effect="dark" :closable="false">{{ $t('m.Submitted_Not_Result') }}</el-alert>
            </template>
            <template v-else>
              <span style="font-size: 13px; font-weight: bolder;">{{ $t('m.Status') }}:</span>
              <el-tag effect="dark" :color="submissionStatus.color">
                <template v-if="result.status == JUDGE_STATUS_RESERVE['Pending']
                  || result.status == JUDGE_STATUS_RESERVE['Compiling']
                  || result.status == JUDGE_STATUS_RESERVE['Judging']
                  || result.status == JUDGE_STATUS_RESERVE['Submitting']">
                  <i class="el-icon-loading"></i> {{ submissionStatus.text }}
                </template>
                <template v-else-if="result.status == JUDGE_STATUS_RESERVE.ac">
                  <i class="el-icon-success"> {{ submissionStatus.text }}</i>
                </template>
                <template v-else-if="result.status == JUDGE_STATUS_RESERVE.pa">
                  <i class="el-icon-remove"> {{ submissionStatus.text }}</i>
                </template>
                <template v-else>
                  <i class="el-icon-error"> {{ submissionStatus.text }}</i>
                </template>
              </el-tag>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 结果弹窗 -->
    <el-dialog
      :visible.sync="resultVisible"
      :title="$t('m.PK_Result')"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="pk-result" v-if="resultInfo">
        <div class="pk-result-icon">
          <i v-if="resultInfo.isWin" class="el-icon-success" style="color: #67c23a; font-size: 48px;"></i>
          <i v-else-if="resultInfo.isDraw" class="el-icon-warning" style="color: #e6a23c; font-size: 48px;"></i>
          <i v-else class="el-icon-error" style="color: #f56c6c; font-size: 48px;"></i>
        </div>
        <div class="pk-result-text">
          <span v-if="resultInfo.isWin">{{ $t('m.PK_You_Win') }}</span>
          <span v-else-if="resultInfo.isDraw">{{ $t('m.PK_Draw') }}</span>
          <span v-else>{{ $t('m.PK_You_Lose') }}</span>
        </div>
        <div class="pk-result-score" v-if="!resultInfo.isDraw">
          <span>{{ $t('m.PK_Score_Change') }}: {{ resultInfo.scoreChange >= 0 ? '+' : '' }}{{ resultInfo.scoreChange }}</span>
        </div>
        <div class="pk-result-detail" v-if="resultInfo.reason">
          {{ resultInfo.reason }}
        </div>
      </div>
      <div slot="footer">
        <el-button type="primary" @click="backToProblem" size="small">{{ $t('m.PK_Back_To_Problem') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import myMessage from '@/common/message';
import CodeMirror from '@/components/oj/common/CodeMirror.vue';
import { JUDGE_STATUS, JUDGE_STATUS_RESERVE } from '@/common/constants';

export default {
  name: 'PkPage',
  components: {
    CodeMirror,
  },
  data() {
    return {
      matchId: null,
      problemId: null,
      problemInternalId: null,
      matchInfo: {},
      remainingSeconds: 1200,
      pkStatus: 'loading', // loading, waiting, running, ended
      pkEnded: false,
      openTestCaseDrawer: false,
      resultVisible: false,
      resultInfo: null,
      code: '',
      language: 'C++',
      theme: 'solarized',
      fontSize: '14px',
      tabSize: 4,
      editorHeight: 550,
      submitting: false,
      statusVisible: false,
      result: { status: 9 },
      submissionId: '',
      refreshStatus: null,
      pollingTimer: null,
      countdownTimer: null,
      loading: false,
      problemData: {
        problem: null,
        languages: ['C', 'C++', 'Java', 'Python3', 'Python2'],
      },
      JUDGE_STATUS: {},
      JUDGE_STATUS_RESERVE: {},
      lastOpponentSubmit: null,
      currentUserUid: null,
    };
  },
  created() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
    this.matchId = this.$route.params.matchId;
    this.currentUserUid = this.$store.getters.userInfo ? this.$store.getters.userInfo.uid : null;
    this.initMatch();
  },
  mounted() {
    window.addEventListener('resize', this.resizeEditor);
    this.resizeEditor();
  },
  beforeDestroy() {
    this.clearAllTimers();
    window.removeEventListener('resize', this.resizeEditor);
  },
  methods: {
    initMatch() {
      this.loading = true;
      api.getPkMatchStatus(this.matchId).then(res => {
        let data = res.data.data;
        this.matchInfo = data;
        this.problemId = data.problemDisplayId || data.problemId;
        this.problemInternalId = data.problemId;
        this.pkStatus = this.getStatusType(data.status);
        if (data.status === 0) {
          this.pkStatus = 'waiting';
        } else if (data.status === 1) {
          this.pkStatus = 'running';
          this.startCountdown(data.remainingSeconds);
        }
        // 加载题目
        this.loadProblem();
        // 启动轮询
        this.startPolling();
      }).catch(() => {
        this.loading = false;
        myMessage.error(this.$i18n.t('m.System_Error'));
      });
    },

    loadProblem() {
      if (!this.problemId) return;
      api.getProblem(this.problemId, 0, null, true).then(res => {
        let result = res.data.data;
        if (result.problem.examples) {
          result.problem.examples = utils.stringToExamples(result.problem.examples);
        }
        this.problemData = result;
        this.loading = false;

        // 设置默认语言
        if (result.languages && result.languages.length > 0) {
          if (!this.language || !result.languages.includes(this.language)) {
            this.language = result.languages[0];
          }
        }
        // 如果有代码模板且本地无缓存，加载模板
        if (result.codeTemplate && result.codeTemplate[this.language]) {
          if (!this.code) {
            this.code = result.codeTemplate[this.language];
          }
        }
      }).catch(() => {
        this.loading = false;
      });
    },

    openTestJudgeDrawer() {
      this.openTestCaseDrawer = !this.openTestCaseDrawer;
    },

    onChangeLang(newLang) {
      if (this.problemData.codeTemplate && this.code === this.problemData.codeTemplate[this.language]) {
        if (this.problemData.codeTemplate[newLang]) {
          this.code = this.problemData.codeTemplate[newLang];
        } else {
          this.code = '';
        }
      }
      this.language = newLang;
    },

    onChangeTheme(newTheme) {
      this.theme = newTheme;
    },

    getStatusType(status) {
      if (status === 0) return 'waiting';
      if (status === 1) return 'running';
      return 'ended';
    },

    startPolling() {
      this.pollingTimer = setInterval(() => {
        this.pollStatus();
      }, 2000);
    },

    pollStatus() {
      api.getPkMatchStatus(this.matchId).then(res => {
        let data = res.data.data;
        let oldStatus = this.pkStatus;
        this.matchInfo = data;

        // 等待中变为进行中
        if (oldStatus === 'waiting' && data.status === 1) {
          this.pkStatus = 'running';
          this.startCountdown(data.remainingSeconds);
        }

        // 进行中检查结束
        if (this.pkStatus === 'running') {
          if (data.status !== 1) {
            // 对战结束
            this.pkEnded = true;
            this.pkStatus = 'ended';
            this.stopCountdown();
            this.showResult(data);
          } else if (data.remainingSeconds !== undefined) {
            // 更新倒计时（服务器时间纠正）
            this.remainingSeconds = data.remainingSeconds;
          }
        }
      }).catch(() => {
        // 静默处理轮询错误
      });
    },

    startCountdown(initialSeconds) {
      this.remainingSeconds = initialSeconds || 1200;
      this.stopCountdown();
      this.countdownTimer = setInterval(() => {
        if (this.remainingSeconds > 0) {
          this.remainingSeconds--;
        }
        if (this.remainingSeconds <= 0 && !this.pkEnded) {
          this.stopCountdown();
          // 触发一次立即轮询以获取服务器确认的结束状态
          this.pollStatus();
        }
      }, 1000);
    },

    stopCountdown() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
        this.countdownTimer = null;
      }
    },

    clearAllTimers() {
      this.stopCountdown();
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer);
        this.pollingTimer = null;
      }
      if (this.refreshStatus) {
        clearTimeout(this.refreshStatus);
        this.refreshStatus = null;
      }
    },

    submitCode() {
      if (this.code.trim() === '') {
        myMessage.error(this.$i18n.t('m.Code_can_not_be_empty'));
        return;
      }

      if (this.code.length > 65535) {
        myMessage.error(this.$i18n.t('m.Code_Length_can_not_exceed_65535'));
        return;
      }

      this.submitting = true;
      this.submissionId = '';
      this.result = { status: 9 };
      this.statusVisible = true;
      let data = {
        pid: this.problemId,
        language: this.language,
        code: this.code,
        cid: 0,
        isRemote: false,
        pkId: this.matchId,
      };

      api.submitCode(data).then(res => {
        this.submitting = false;
        let submitId = res.data.data && res.data.data.submitId;
        if (submitId) {
          this.submissionId = submitId;
          this.checkSubmissionStatus();
        }
      }).catch(() => {
        this.submitting = false;
        myMessage.error(this.$i18n.t('m.System_Error'));
      });
    },

    checkSubmissionStatus() {
      if (this.refreshStatus) {
        clearTimeout(this.refreshStatus);
      }
      const checkStatus = () => {
        let submitId = this.submissionId;
        if (!submitId) return;
        api.getSubmission(submitId).then(res => {
          let judgeResult = res.data.data.submission;
          if (!judgeResult || Object.keys(judgeResult).length === 0) return;
          this.result.status = judgeResult.status;
          if (judgeResult.status !== this.JUDGE_STATUS_RESERVE['Pending'] &&
              judgeResult.status !== this.JUDGE_STATUS_RESERVE['Compiling'] &&
              judgeResult.status !== this.JUDGE_STATUS_RESERVE['Judging']) {
            // 判题完成
            clearTimeout(this.refreshStatus);
            if (judgeResult.status === this.JUDGE_STATUS_RESERVE['ac']) {
              this.pollStatus();
            }
          } else {
            this.refreshStatus = setTimeout(checkStatus, 2000);
          }
        }).catch(() => {});
      };
      this.refreshStatus = setTimeout(checkStatus, 1000);
    },

    reSubmit() {
      this.submitCode();
    },

    surrender() {
      this.$confirm(this.$i18n.t('m.PK_Surrender') + '?', this.$i18n.t('m.Confirm'), {
        confirmButtonText: this.$i18n.t('m.Confirm'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.surrenderPk({ matchId: this.matchId }).then(res => {
          let data = res.data.data;
          this.pkEnded = true;
          this.pkStatus = 'ended';
          this.stopCountdown();
          this.showResult(data);
        }).catch(() => {
          myMessage.error(this.$i18n.t('m.System_Error'));
        });
      }).catch(() => {});
    },

    showResult(data) {
      let isMeInitiator = this.currentUserUid === data.initiatorUid;
      let resultInfo = {
        isWin: false,
        isDraw: false,
        scoreChange: 0,
        reason: '',
      };

      if (data.status === 2) {
        // 发起者胜
        resultInfo.isWin = isMeInitiator;
        resultInfo.isDraw = false;
        resultInfo.scoreChange = isMeInitiator ? 10 : -2;
        if (data.surrenderUid) {
          resultInfo.reason = this.$i18n.t('m.PK_Opponent_Surrendered');
        } else {
          resultInfo.reason = this.$i18n.t('m.PK_First_To_AC');
        }
      } else if (data.status === 3) {
        // 对手胜
        resultInfo.isWin = !isMeInitiator;
        resultInfo.isDraw = false;
        resultInfo.scoreChange = !isMeInitiator ? 10 : -2;
        if (data.surrenderUid) {
          if (data.surrenderUid === this.currentUserUid) {
            resultInfo.reason = this.$i18n.t('m.PK_You_Surrendered');
          } else {
            resultInfo.reason = this.$i18n.t('m.PK_Opponent_Surrendered');
          }
        } else {
          resultInfo.reason = this.$i18n.t('m.PK_First_To_AC');
        }
      } else if (data.status === 4) {
        // 平局
        resultInfo.isDraw = true;
        resultInfo.scoreChange = 0;
        resultInfo.reason = this.$i18n.t('m.PK_Time_Up');
      }

      this.resultInfo = resultInfo;
      this.resultVisible = true;
    },

    backToProblem() {
      if (this.problemId) {
        this.$router.push({ name: 'ProblemDetails', params: { problemID: this.problemId } });
      } else {
        this.$router.push({ name: 'Home' });
      }
    },

    playAgain() {
      if (this.problemId) {
        this.$router.push({ name: 'ProblemDetails', params: { problemID: this.problemId } });
      }
    },

    formatTime(seconds) {
      if (seconds == null) return '--:--';
      let m = Math.floor(seconds / 60);
      let s = seconds % 60;
      return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
    },

    resizeEditor() {
      this.$nextTick(() => {
        let headerHeight = 80;
        try {
          let headerEl = document.querySelector('.pk-header');
          if (headerEl) headerHeight = headerEl.offsetHeight + 10;
        } catch (e) {}
        let actionsEl = document.querySelector('.pk-editor-actions');
        let actionsHeight = actionsEl ? actionsEl.offsetHeight + 5 : 55;
        let totalHeight = window.innerHeight;
        // Reserve space for: CodeMirror header(~40px) + margin(~15px) + actions bar
        this.editorHeight = Math.max(400, totalHeight - headerHeight - 55 - actionsHeight);
      });
    },
  },
  computed: {
    submissionStatus() {
      return {
        text: this.JUDGE_STATUS[this.result.status] ? this.JUDGE_STATUS[this.result.status].name : '',
        color: this.JUDGE_STATUS[this.result.status] ? this.JUDGE_STATUS[this.result.status].rgb : '',
      };
    },
  },
};
</script>

<style scoped>
.pk-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

.pk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 2px solid #e6a23c;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-height: 60px;
}

.pk-player {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.pk-player-left {
  justify-content: flex-start;
}

.pk-player-right {
  justify-content: flex-end;
}

.pk-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #e6a23c;
}

.pk-player-name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
}

.pk-player-score {
  font-size: 12px;
  color: #999;
}

.pk-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 0 0 auto;
  min-width: 160px;
}

.pk-countdown {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  font-family: 'Courier New', monospace;
}

.pk-countdown-warn {
  color: #f56c6c;
  animation: pk-blink 1s ease-in-out infinite;
}

@keyframes pk-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pk-vs {
  font-size: 14px;
  color: #e6a23c;
  font-weight: bold;
}

.pk-surrender-btn {
  margin-top: 6px;
}

.pk-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.pk-problem-panel {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
}

.pk-problem-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.pk-problem-section {
  margin-bottom: 15px;
}

.pk-problem-section h4 {
  color: #555;
  margin-bottom: 8px;
  font-size: 14px;
}

.pk-example {
  margin-bottom: 10px;
}

.pk-example-item {
  margin-bottom: 5px;
}

.pk-example-label {
  font-weight: bold;
  color: #666;
}

.pk-example-item pre {
  background: #f8f8f8;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  overflow-x: auto;
}

.pk-editor-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pk-editor-actions {
  padding: 10px 15px;
  background: #fff;
  border-top: 1px solid #e0e0e0;
  flex-shrink: 0;
}

/* 结果弹窗 */
.pk-result {
  text-align: center;
}

.pk-result-icon {
  margin-bottom: 15px;
}

.pk-result-text {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.pk-result-score {
  font-size: 16px;
  color: #e6a23c;
  margin-bottom: 8px;
}

.pk-result-detail {
  font-size: 14px;
  color: #999;
}

/* PK邀请弹窗 */
.pk-invite-dialog {
  padding: 10px 0;
}

.pk-invite-problem, .pk-invite-search {
  margin-bottom: 15px;
}

.pk-label {
  font-weight: bold;
  color: #555;
  margin-right: 5px;
}

.pk-user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pk-user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.pk-user-nickname {
  color: #999;
  font-size: 12px;
}

.pk-selected-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: #f8f8f8;
  border-radius: 4px;
  margin-top: 5px;
}

.pk-user-name {
  font-weight: bold;
}

.pk-user-score {
  margin-left: auto;
  font-size: 12px;
  color: #e6a23c;
}

.tj-btn {
  margin-right: 15px;
  float: right;
  cursor: pointer;
}
.tj-btn.non-active {
  border: 1px solid #32ca99;
}
.tj-btn.non-active:hover {
  background-color: #d5f1eb;
}
.tj-btn.active {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

@media screen and (max-width: 768px) {
  .pk-header {
    flex-wrap: wrap;
    padding: 8px 10px;
    gap: 5px;
  }

  .pk-player {
    flex: 0 0 auto;
    font-size: 12px;
  }

  .pk-avatar {
    width: 28px;
    height: 28px;
  }

  .pk-countdown {
    font-size: 22px;
  }

  .pk-surrender-btn {
    margin-top: 4px;
  }

  .pk-body {
    flex-direction: column;
  }

  .pk-problem-panel {
    max-height: 40%;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }

  .pk-editor-panel {
    max-height: 60%;
  }
}
</style>
