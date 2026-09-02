<template>
  <el-card class="container">
    <div slot="header">
      <span class="panel-title home-title">{{ $t('m.Assignment_My_List') }}</span>
    </div>
    <el-table :data="assignmentList" v-loading="loading" style="width: 100%">
      <el-table-column :label="$t('m.Assignment_Title')" min-width="220">
        <template v-slot="{ row }">
          <el-link type="primary" @click="toDetail(row.id)">{{ row.title }}</el-link>
          <el-tag
            v-if="isRequiredUnfinished(row)"
            type="danger"
            size="mini"
            effect="dark"
            style="margin-left: 8px"
            >{{ $t('m.Assignment_Required_Badge') }}</el-tag
          >
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Status')"
        min-width="110"
        align="center"
      >
        <template v-slot="{ row }">
          <el-tag v-if="row.completed" type="success">{{
            $t('m.Assignment_Completed')
          }}</el-tag>
          <el-tag v-else type="warning">{{
            $t('m.Assignment_Unfinished')
          }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Required')"
        min-width="90"
        align="center"
      >
        <template v-slot="{ row }">
          <el-tag :type="row.isRequired ? 'danger' : 'info'" size="mini">{{
            row.isRequired
              ? $t('m.Assignment_Required')
              : $t('m.Assignment_Optional')
          }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Progress')"
        min-width="150"
        align="center"
      >
        <template v-slot="{ row }">
          <el-progress
            :text-inside="true"
            :stroke-width="20"
            :percentage="progressPercent(row)"
          ></el-progress>
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Problem_Count')"
        prop="problemCount"
        min-width="80"
        align="center"
      ></el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Deadline')"
        min-width="150"
        align="center"
      >
        <template v-slot="{ row }">
          <el-tooltip v-if="row.endTime" :content="row.endTime | localtime" placement="top">
            <span>{{ row.endTime | fromNow }}</span>
          </el-tooltip>
          <span v-else>—</span>
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('m.Assignment_Creator')"
        prop="creatorUsername"
        min-width="100"
        align="center"
      ></el-table-column>
    </el-table>

    <div v-if="!loading && assignmentList.length === 0" class="empty">
      {{ $t('m.Assignment_Empty') }}
    </div>

    <Pagination
      :total="total"
      :pageSize="limit"
      @on-change="changePage"
      :current.sync="currentPage"
    ></Pagination>
  </el-card>
</template>

<script>
import api from '@/common/api';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'AssignmentList',
  components: {
    Pagination,
  },
  data() {
    return {
      assignmentList: [],
      total: 0,
      currentPage: 1,
      limit: 15,
      loading: false,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      api.getAssignmentList(this.currentPage, this.limit).then(
        (res) => {
          this.assignmentList = res.data.data.records;
          this.total = res.data.data.total;
          this.loading = false;
        },
        () => {
          this.loading = false;
        }
      );
    },
    changePage(page) {
      this.currentPage = page;
      this.getList();
    },
    toDetail(aid) {
      this.$router.push({
        name: 'AssignmentDetails',
        params: { assignmentID: aid },
      });
    },
    isRequiredUnfinished(row) {
      return row.isRequired === 1 && !row.completed;
    },
    progressPercent(row) {
      if (!row.problemCount) {
        return 0;
      }
      return Math.round(((row.acceptedCount || 0) / row.problemCount) * 100);
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
