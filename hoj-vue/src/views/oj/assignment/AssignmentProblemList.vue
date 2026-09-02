<template>
  <el-card class="container" v-loading="loading">
    <div slot="header">
      <span class="panel-title home-title">{{ $t('m.Assignment_Problem_List') }}</span>
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
      <el-table-column :label="$t('m.Title')" min-width="220" show-overflow>
        <template v-slot="{ row }">
          <el-link type="primary" @click="toProblem(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('m.Level')" min-width="100" align="center">
        <template v-slot="{ row }">
          <span
            class="el-tag el-tag--small"
            :style="getLevelColor(row.difficulty)"
            >{{ getLevelName(row.difficulty) }}</span
          >
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && problemList.length === 0" class="empty">
      {{ $t('m.Assignment_Empty') }}
    </div>
  </el-card>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
export default {
  name: 'AssignmentProblemList',
  data() {
    return {
      problemList: [],
      loading: false,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      api.getAssignmentProblemList(this.$route.params.assignmentID).then(
        (res) => {
          this.problemList = res.data.data || [];
          this.loading = false;
        },
        () => {
          this.loading = false;
        }
      );
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
.container .empty {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}
</style>
