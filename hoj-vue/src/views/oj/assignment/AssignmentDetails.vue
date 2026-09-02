<template>
  <div>
    <el-card class="container" v-loading="loading">
      <div slot="header" class="header">
        <span class="panel-title home-title">{{ $t('m.Assignment_Detail') }}</span>
        <el-button type="primary" size="small" @click="toProblemList">{{
          $t('m.Assignment_Problem_List')
        }}</el-button>
      </div>

      <template v-if="assignment.id">
        <div class="info-block">
          <h3 class="title">
            {{ assignment.title }}
            <el-tag
              :type="assignment.isRequired ? 'danger' : 'info'"
              size="mini"
              style="margin-left: 8px"
              >{{
                assignment.isRequired
                  ? $t('m.Assignment_Required')
                  : $t('m.Assignment_Optional')
              }}</el-tag
            >
          </h3>
          <div class="meta">
            <span>{{ $t('m.Assignment_Creator') }}：{{ assignment.creatorUsername }}</span>
            <span v-if="assignment.startTime">
              {{ $t('m.Assignment_Start_Time') }}：{{ assignment.startTime | localtime }}
            </span>
            <span v-if="assignment.endTime">
              {{ $t('m.Assignment_Deadline') }}：{{ assignment.endTime | localtime }}
            </span>
            <span>{{ $t('m.Assignment_Status') }}：{{ timeStatusText }}</span>
          </div>
          <div class="progress">
            <el-progress
              :text-inside="true"
              :stroke-width="22"
              :percentage="progressPercent"
              :status="assignment.completed ? 'success' : null"
            ></el-progress>
            <span class="progress-text"
              >{{ assignment.acceptedCount }} / {{ assignment.problemCount }}</span
            >
          </div>
        </div>

        <div class="description" v-if="assignment.description">
          <p v-html="assignment.description"></p>
        </div>

        <el-table :data="problemList" style="width: 100%">
          <el-table-column label="" width="50" align="center">
            <template v-slot="{ row }">
              <el-tooltip
                :content="row.status === 1 ? $t('m.Assignment_AC') : $t('m.Assignment_Not_AC')"
                placement="top"
              >
                <i
                  class="el-icon-check"
                  :style="row.status === 1 ? 'color:#19be6b;font-weight:600' : 'color:#c0c4cc'"
                ></i>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column :label="$t('m.Problem_ID')" min-width="90">
            <template v-slot="{ row }">{{ row.displayId }}</template>
          </el-table-column>
          <el-table-column :label="$t('m.Title')" min-width="200" show-overflow>
            <template v-slot="{ row }">
              <el-link type="primary" @click="toProblem(row)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column :label="$t('m.Level')" min-width="90" align="center">
            <template v-slot="{ row }">
              <span
                class="el-tag el-tag--small"
                :style="getLevelColor(row.difficulty)"
                >{{ getLevelName(row.difficulty) }}</span
              >
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
export default {
  name: 'AssignmentDetails',
  data() {
    return {
      assignment: {},
      problemList: [],
      loading: false,
    };
  },
  created() {
    this.getDetail();
  },
  computed: {
    progressPercent() {
      if (!this.assignment.problemCount) {
        return 0;
      }
      return Math.round(
        ((this.assignment.acceptedCount || 0) / this.assignment.problemCount) * 100
      );
    },
    timeStatusText() {
      if (this.assignment.isRunning) {
        return this.$t('m.Assignment_Running');
      }
      if (this.assignment.isEnded) {
        return this.$t('m.Assignment_Ended');
      }
      return this.$t('m.Assignment_Not_Started');
    },
  },
  methods: {
    getDetail() {
      this.loading = true;
      api.getAssignmentDetail(this.$route.params.assignmentID).then(
        (res) => {
          let data = res.data.data;
          this.assignment = data.assignment || {};
          this.problemList = data.problemList || [];
          this.loading = false;
        },
        () => {
          this.loading = false;
        }
      );
    },
    toProblemList() {
      this.$router.push({
        name: 'AssignmentProblemList',
        params: { assignmentID: this.$route.params.assignmentID },
      });
    },
    toProblem(row) {
      this.$router.push({
        name: 'AssignmentFullProblemDetails',
        params: {
          assignmentID: this.$route.params.assignmentID,
          problemID: row.displayId,
        },
      });
    },
    getLevelColor(difficulty) {
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
  },
};
</script>

<style scoped>
.container {
  margin-bottom: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.info-block .title {
  margin: 0 0 12px 0;
}
.meta span {
  margin-right: 20px;
  color: #909399;
  font-size: 13px;
}
.progress {
  display: flex;
  align-items: center;
  margin: 16px 0;
  max-width: 500px;
}
.progress-text {
  margin-left: 12px;
  color: #606266;
  white-space: nowrap;
}
.description {
  margin: 16px 0;
}
</style>
