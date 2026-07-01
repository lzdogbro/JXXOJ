<template>
  <div class="pk-history-container">
    <div class="pk-history-header">
      <h2>
        <svg
          class="icon"
          viewBox="0 0 24 24"
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          style="vertical-align: middle; margin-right: 8px;"
        >
          <rect x="11" y="2" width="2" height="13" fill="currentColor"/>
          <polygon points="12,2 9,5 15,5" fill="currentColor"/>
          <rect x="5" y="10" width="14" height="2" rx="0.5" fill="currentColor"/>
          <rect x="11" y="14" width="2" height="7" fill="currentColor"/>
          <rect x="9" y="20" width="6" height="2" rx="1" fill="currentColor"/>
        </svg>
        {{ $t('m.PK_History_Title') }}
      </h2>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <i class="el-icon-loading"></i> {{ $t('m.Loading') }}
    </div>

    <div v-else-if="records.length === 0" class="empty-wrapper">
      <i class="el-icon-s-data"></i>
      <p>{{ $t('m.PK_History_Empty') }}</p>
    </div>

    <div v-else class="pk-history-list">
      <div
        v-for="record in records"
        :key="record.id"
        class="pk-record-card"
        :class="getResultClass(record)"
      >
        <div class="record-main">
          <div class="record-players">
            <div class="player" :class="{ 'is-winner': isWinner(record, 'initiator') }">
              <avatar
                :username="record.initiatorUsername"
                :size="40"
                :src="record.initiatorAvatar"
                :inline="true"
                color="#FFF"
              ></avatar>
              <div class="player-info">
                <span class="player-name">{{ record.initiatorNickname || record.initiatorUsername }}</span>
                <span class="player-score">PK {{ record.initiatorPkScore || 0 }}</span>
              </div>
              <i v-if="isWinner(record, 'initiator')" class="el-icon-medal winner-badge"></i>
            </div>

            <div class="vs-divider">
              <span class="vs-text">VS</span>
            </div>

            <div class="player" :class="{ 'is-winner': isWinner(record, 'opponent') }">
              <avatar
                :username="record.opponentUsername"
                :size="40"
                :src="record.opponentAvatar"
                :inline="true"
                color="#FFF"
              ></avatar>
              <div class="player-info">
                <span class="player-name">{{ record.opponentNickname || record.opponentUsername }}</span>
                <span class="player-score">PK {{ record.opponentPkScore || 0 }}</span>
              </div>
              <i v-if="isWinner(record, 'opponent')" class="el-icon-medal winner-badge"></i>
            </div>
          </div>

          <div class="record-meta">
            <span class="record-problem" @click="goToProblem(record.problemDisplayId)">
              <i class="el-icon-document"></i>
              {{ record.problemDisplayId }} {{ record.problemTitle }}
            </span>
            <span class="record-result" :style="{ color: getResultColor(record) }">
              {{ getResultText(record) }}
            </span>
            <span class="record-time">
              <i class="el-icon-time"></i>
              {{ record.startTime | localtime }}
            </span>
            <span v-if="record.initiatorScoreChange !== null && record.initiatorScoreChange !== undefined" class="record-score-change" :class="getScoreChangeClass(record)">
              {{ getScoreChangeText(record) }}
            </span>
          </div>
        </div>
        <div class="record-status-badge" :style="{ background: getStatusBg(record.status) }">
          {{ getStatusText(record.status) }}
        </div>
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="limit"
          :current-page.sync="currentPage"
          @current-change="loadRecords"
        >
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import Avatar from 'vue-avatar';
import api from '@/common/api';

export default {
  name: 'PkHistory',
  components: {
    Avatar,
  },
  data() {
    return {
      records: [],
      loading: false,
      currentPage: 1,
      limit: 10,
      total: 0,
    };
  },
  mounted() {
    this.loadRecords();
  },
  methods: {
    loadRecords() {
      this.loading = true;
      api.getPkHistory(this.limit, this.currentPage).then((res) => {
        this.records = res.data.data.records || [];
        this.total = res.data.data.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    isWinner(record, role) {
      if (!record.winnerUid) return false;
      if (role === 'initiator') {
        return record.winnerUid === record.initiatorUid;
      } else {
        return record.winnerUid === record.opponentUid;
      }
    },
    getResultClass(record) {
      const uid = this.$store.getters.userInfo.uid;
      if (!record.winnerUid) return 'record-draw';
      if (record.winnerUid === uid) return 'record-win';
      return 'record-lose';
    },
    getResultColor(record) {
      const uid = this.$store.getters.userInfo.uid;
      if (!record.winnerUid) return '#909399';
      if (record.winnerUid === uid) return '#67c23a';
      return '#f56c6c';
    },
    getResultText(record) {
      const uid = this.$store.getters.userInfo.uid;
      if (record.status === 5) return this.$t('m.PK_Status_Rejected');
      if (record.status === 6) return this.$t('m.PK_Status_Cancelled');
      if (record.surrenderUid) {
        return record.surrenderUid === uid ? this.$t('m.PK_Result_Surrendered') : this.$t('m.PK_Result_OpponentSurrendered');
      }
      if (!record.winnerUid) return this.$t('m.PK_Result_Draw');
      if (record.winnerUid === uid) return this.$t('m.PK_Result_Win');
      return this.$t('m.PK_Result_Lose');
    },
    getScoreChangeText(record) {
      const uid = this.$store.getters.userInfo.uid;
      if (record.initiatorUid === uid) {
        const change = record.initiatorScoreChange || 0;
        return change > 0 ? `+${change}` : `${change}`;
      } else {
        const change = record.opponentScoreChange || 0;
        return change > 0 ? `+${change}` : `${change}`;
      }
    },
    getScoreChangeClass(record) {
      const uid = this.$store.getters.userInfo.uid;
      let change = 0;
      if (record.initiatorUid === uid) {
        change = record.initiatorScoreChange || 0;
      } else {
        change = record.opponentScoreChange || 0;
      }
      return change > 0 ? 'score-up' : change < 0 ? 'score-down' : '';
    },
    getStatusBg(status) {
      const map = {
        2: '#67c23a',
        3: '#67c23a',
        4: '#e6a23c',
        5: '#f56c6c',
        6: '#909399',
      };
      return map[status] || '#909399';
    },
    getStatusText(status) {
      const map = {
        2: this.$t('m.PK_Status_Finished'),
        3: this.$t('m.PK_Status_Finished'),
        4: this.$t('m.PK_Status_Draw'),
        5: this.$t('m.PK_Status_Rejected'),
        6: this.$t('m.PK_Status_Cancelled'),
      };
      return map[status] || '';
    },
    goToProblem(problemDisplayId) {
      if (problemDisplayId) {
        this.$router.push(`/problem/${problemDisplayId}`);
      }
    },
  },
};
</script>

<style scoped>
.pk-history-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.pk-history-header h2 {
  display: flex;
  align-items: center;
  font-size: 22px;
  color: #303133;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e6a23c;
}

.loading-wrapper,
.empty-wrapper {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 16px;
}

.empty-wrapper .el-icon-s-data {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.pk-record-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid #909399;
}

.pk-record-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.pk-record-card.record-win {
  border-left-color: #67c23a;
}

.pk-record-card.record-lose {
  border-left-color: #f56c6c;
}

.pk-record-card.record-draw {
  border-left-color: #e6a23c;
}

.record-main {
  flex: 1;
  padding: 16px 20px;
}

.record-players {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 12px;
}

.player {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  min-width: 180px;
  position: relative;
}

.player.is-winner {
  background: #f0f9eb;
}

.player-info {
  display: flex;
  flex-direction: column;
}

.player-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.player-score {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.winner-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 20px;
  color: #e6a23c;
}

.vs-divider {
  display: flex;
  align-items: center;
  justify-content: center;
}

.vs-text {
  font-size: 18px;
  font-weight: 700;
  color: #c0c4cc;
  background: #fff;
  border: 2px solid #e4e7ed;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}

.record-problem {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
}

.record-problem:hover {
  text-decoration: underline;
}

.record-result {
  font-weight: 600;
  font-size: 14px;
}

.record-time {
  color: #909399;
}

.record-score-change {
  font-weight: 600;
  font-size: 13px;
  padding: 2px 8px;
  border-radius: 4px;
}

.record-score-change.score-up {
  color: #67c23a;
  background: #f0f9eb;
}

.record-score-change.score-down {
  color: #f56c6c;
  background: #fef0f0;
}

.record-status-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-lr;
  padding: 8px 6px;
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 2px;
  min-width: 28px;
}

.pagination-wrapper {
  text-align: center;
  margin-top: 24px;
}

@media screen and (max-width: 768px) {
  .pk-history-container {
    padding: 12px;
  }

  .record-players {
    flex-direction: column;
    gap: 10px;
  }

  .vs-divider {
    transform: rotate(90deg);
  }

  .player {
    min-width: auto;
    width: 100%;
    justify-content: center;
  }

  .record-meta {
    flex-direction: column;
    gap: 6px;
    text-align: center;
  }

  .record-status-badge {
    writing-mode: horizontal-tb;
    min-width: auto;
    padding: 4px 12px;
  }

  .pk-record-card {
    flex-direction: column;
    border-left: none;
    border-top: 4px solid #909399;
  }

  .pk-record-card.record-win {
    border-top-color: #67c23a;
  }

  .pk-record-card.record-lose {
    border-top-color: #f56c6c;
  }

  .pk-record-card.record-draw {
    border-top-color: #e6a23c;
  }
}
</style>
