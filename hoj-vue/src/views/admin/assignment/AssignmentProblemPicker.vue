<template>
  <el-dialog
    :title="$t('m.Assignment_Add_Problem')"
    :visible.sync="dialogVisible"
    width="700px"
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <div style="margin-bottom:10px">
      <vxe-input
        v-model="keyword"
        :placeholder="$t('m.Enter_keyword')"
        type="search"
        size="medium"
        @search-click="searchProblem"
        @keyup.enter.native="searchProblem"
      ></vxe-input>
    </div>
    <vxe-table
      :loading="loading"
      ref="xTable"
      :data="problems"
      auto-resize
      stripe
      align="center"
      max-height="400"
      :checkbox-config="{ labelField: 'id', highlight: true, range: true }"
    >
      <vxe-table-column type="checkbox" width="60"></vxe-table-column>
      <vxe-table-column
        field="problemId"
        min-width="100"
        :title="$t('m.Assignment_Problem_Number')"
      ></vxe-table-column>
      <vxe-table-column
        field="title"
        min-width="200"
        :title="$t('m.Title')"
        show-overflow
      ></vxe-table-column>
      <vxe-table-column min-width="90" :title="$t('m.Difficulty')">
        <template v-slot="{ row }">
          <el-tag
            :style="getLevelColor(row.difficulty)"
            effect="dark"
            size="small"
          >
            {{ getLevelName(row.difficulty) }}
          </el-tag>
        </template>
      </vxe-table-column>
    </vxe-table>
    <div class="panel-options">
      <el-pagination
        class="page"
        layout="prev, pager, next"
        @current-change="currentChange"
        :page-size="limit"
        :current-page.sync="page"
        :total="total"
      >
      </el-pagination>
    </div>
    <div style="text-align:center;margin-top:10px">
      <el-button type="primary" @click="confirmSelection">{{
        $t('m.OK')
      }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
export default {
  name: 'AssignmentProblemPicker',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      page: 1,
      limit: 10,
      total: 0,
      loading: false,
      problems: [],
      keyword: '',
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit('update:visible', val);
      },
    },
  },
  methods: {
    getLevelColor(difficulty) {
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    onOpen() {
      this.page = 1;
      this.keyword = '';
      this.getProblemList(1);
    },
    getProblemList(page) {
      this.loading = true;
      let params = {
        keyword: this.keyword,
        currentPage: page,
        limit: this.limit,
      };
      api.admin_getProblemList(params).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.problems = res.data.data.records;
        },
        () => {
          this.loading = false;
        }
      );
    },
    searchProblem() {
      this.page = 1;
      this.getProblemList(1);
    },
    currentChange(page) {
      this.page = page;
      this.getProblemList(page);
    },
    confirmSelection() {
      let records = this.$refs.xTable.getCheckboxRecords();
      let selected = records.map((r) => ({
        pid: r.id,
        problemId: r.problemId,
        title: r.title,
        difficulty: r.difficulty,
      }));
      this.$emit('confirm', selected);
      this.$emit('update:visible', false);
    },
  },
};
</script>
<style scoped>
.panel-options {
  margin-top: 10px;
  text-align: right;
}
</style>
