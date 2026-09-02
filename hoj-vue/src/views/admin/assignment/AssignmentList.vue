<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Assignment_List') }}</span>
        <div class="filter-row">
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateAssignment"
              icon="el-icon-plus"
              >{{ $t('m.Create_Assignment') }}
            </el-button>
          </span>
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="assignmentList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"></vxe-table-column>
        <vxe-table-column min-width="180" :title="$t('m.Title')">
          <template v-slot="{ row }">
            <span>{{ row.title }}</span>
            <el-tag
              size="mini"
              effect="plain"
              :type="row.isRequired == 1 ? 'danger' : 'info'"
              style="margin-left:4px"
            >
              {{
                row.isRequired == 1
                  ? $t('m.Assignment_Required')
                  : $t('m.Assignment_Optional')
              }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column width="140" :title="$t('m.Assignment_Status')">
          <template v-slot="{ row }">
            <el-tag
              effect="dark"
              :type="row.status == ASSIGNMENT_STATUS.DRAFT ? 'info' : 'success'"
            >
              {{
                row.status == ASSIGNMENT_STATUS.DRAFT
                  ? $t('m.Assignment_Draft')
                  : $t('m.Assignment_Published')
              }}
            </el-tag>
            <el-tag
              v-if="row.status == ASSIGNMENT_STATUS.PUBLISHED"
              size="mini"
              effect="plain"
              :type="row.isEnded ? 'danger' : row.isRunning ? 'success' : 'info'"
              style="margin-left:4px"
            >
              {{
                row.isEnded
                  ? $t('m.Assignment_Ended')
                  : row.isRunning
                  ? $t('m.Assignment_Running')
                  : $t('m.Assignment_Not_Started')
              }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="problemCount"
          width="90"
          :title="$t('m.Assignment_Problem_Count')"
        ></vxe-table-column>
        <vxe-table-column
          field="creatorUsername"
          min-width="100"
          :title="$t('m.Assignment_Creator')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column min-width="220" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>{{ $t('m.Assignment_Start_Time') }}: {{ row.startTime | localtime }}</p>
            <p>{{ $t('m.Assignment_Deadline') }}: {{ row.endTime | localtime }}</p>
            <p>Created: {{ row.gmtCreate | localtime }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="230" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <el-tooltip effect="dark" :content="$t('m.Edit')" placement="top">
              <el-button
                icon="el-icon-edit"
                size="mini"
                @click.native="goEdit(row.id)"
                type="primary"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('m.Assignment_Completion_Stats')"
              placement="top"
            >
              <el-button
                icon="el-icon-data-analysis"
                size="mini"
                @click.native="openStatsDialog(row)"
                type="success"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              v-if="row.status == ASSIGNMENT_STATUS.DRAFT"
              effect="dark"
              :content="$t('m.Assignment_Publish')"
              placement="top"
            >
              <el-button
                icon="el-icon-position"
                size="mini"
                @click.native="openPublishDialog(row)"
                type="warning"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              v-if="row.status == ASSIGNMENT_STATUS.PUBLISHED"
              effect="dark"
              :content="$t('m.Assignment_Extend')"
              placement="top"
            >
              <el-button
                icon="el-icon-time"
                size="mini"
                @click.native="openExtendDialog(row)"
                type="info"
              ></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('m.Delete')" placement="top">
              <el-button
                icon="el-icon-delete"
                size="mini"
                @click.native="deleteAssignment(row.id)"
                type="danger"
              ></el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      :title="$t('m.Assignment_Publish')"
      :visible.sync="publishDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Assignment_Student_Group')">
          <el-select
            v-model="publishForm.groupIdList"
            multiple
            style="width:100%"
            :placeholder="$t('m.Assignment_Student_Group')"
          >
            <el-option
              v-for="group in groupList"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('m.Assignment_Extra_User')">
          <el-select
            v-model="publishForm.extraUidList"
            multiple
            filterable
            remote
            style="width:100%"
            :remote-method="searchExtraUser"
            :loading="extraUserLoading"
            :placeholder="$t('m.Assignment_Extra_User')"
          >
            <el-option
              v-for="u in extraUserOptions"
              :key="u.uid"
              :label="u.username"
              :value="u.uid"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item style="text-align:center">
          <el-button type="primary" @click="confirmPublish" :loading="publishLoading">{{
            $t('m.Assignment_Publish')
          }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      :title="$t('m.Assignment_Extend')"
      :visible.sync="extendDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Assignment_Deadline')" required>
          <el-date-picker
            v-model="extendForm.endTime"
            type="datetime"
            style="width:100%"
            :placeholder="$t('m.Assignment_Extend_Tips')"
          ></el-date-picker>
        </el-form-item>
        <el-form-item style="text-align:center">
          <el-button type="primary" @click="confirmExtend" :loading="extendLoading">{{
            $t('m.OK')
          }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      :title="$t('m.Assignment_Completion_Stats')"
      :visible.sync="statsDialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <vxe-table
        :loading="statsLoading"
        :data="studentList"
        auto-resize
        stripe
        align="center"
        max-height="400"
      >
        <vxe-table-column
          field="username"
          min-width="130"
          :title="$t('m.User')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="realname"
          min-width="110"
          :title="$t('m.RealName')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="acceptedCount"
          width="110"
          :title="$t('m.Assignment_Accepted_Count')"
        ></vxe-table-column>
        <vxe-table-column width="100" :title="$t('m.Assignment_Status')">
          <template v-slot="{ row }">
            <el-tag
              effect="dark"
              size="mini"
              :type="row.status == 1 ? 'success' : 'info'"
            >
              {{
                row.status == 1
                  ? $t('m.Assignment_Completed')
                  : $t('m.Assignment_Unfinished')
              }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Assignment_Finish_Time')">
          <template v-slot="{ row }">
            <span v-if="row.gmtFinish">{{ row.gmtFinish | localtime }}</span>
            <span v-else>-</span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import { ASSIGNMENT_STATUS } from '@/common/constants';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
export default {
  name: 'AdminAssignmentList',
  data() {
    return {
      pageSize: 10,
      total: 0,
      assignmentList: [],
      keyword: '',
      loading: false,
      currentPage: 1,
      ASSIGNMENT_STATUS: {},
      publishDialogVisible: false,
      publishLoading: false,
      publishForm: {
        id: null,
        groupIdList: [],
        extraUidList: [],
      },
      groupList: [],
      extraUserOptions: [],
      extraUserLoading: false,
      extendDialogVisible: false,
      extendLoading: false,
      extendForm: {
        id: null,
        endTime: null,
      },
      statsDialogVisible: false,
      statsLoading: false,
      studentList: [],
    };
  },
  computed: {
    ...mapGetters(['isSuperAdmin', 'isAdminRole', 'isProblemAdmin']),
    isWritable() {
      return this.isSuperAdmin || (this.isAdminRole && !this.isProblemAdmin);
    },
  },
  mounted() {
    this.ASSIGNMENT_STATUS = Object.assign({}, ASSIGNMENT_STATUS);
    this.getAssignmentList(this.currentPage);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == 'true' ? true : false;
      if (refresh) {
        this.getAssignmentList(1);
      }
    },
  },
  methods: {
    currentChange(page) {
      this.currentPage = page;
      this.getAssignmentList(page);
    },
    getAssignmentList(page) {
      this.loading = true;
      api.admin_getAssignmentList(page, this.pageSize, this.keyword).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.assignmentList = res.data.data.records;
        },
        () => {
          this.loading = false;
        }
      );
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateAssignment() {
      this.$router.push({ name: 'admin-create-assignment' });
    },
    goEdit(id) {
      this.$router.push({ name: 'admin-edit-assignment', params: { assignmentId: id } });
    },
    deleteAssignment(id) {
      this.$confirm(this.$i18n.t('m.Assignment_Delete_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.admin_deleteAssignment(id).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.currentChange(1);
        });
      });
    },
    openPublishDialog(row) {
      this.publishForm = {
        id: row.id,
        groupIdList: [],
        extraUidList: [],
      };
      this.extraUserOptions = [];
      this.publishDialogVisible = true;
      this.getGroupList();
    },
    getGroupList() {
      api.admin_getStudentGroupList().then((res) => {
        this.groupList = res.data.data || [];
      });
    },
    searchExtraUser(query) {
      if (!query) {
        this.extraUserOptions = [];
        return;
      }
      this.extraUserLoading = true;
      api.admin_getUserList(1, 20, query, false).then(
        (res) => {
          this.extraUserLoading = false;
          this.extraUserOptions = res.data.data.records || [];
        },
        () => {
          this.extraUserLoading = false;
        }
      );
    },
    confirmPublish() {
      if (
        (!this.publishForm.groupIdList || this.publishForm.groupIdList.length === 0) &&
        (!this.publishForm.extraUidList || this.publishForm.extraUidList.length === 0)
      ) {
        myMessage.warning(this.$i18n.t('m.Assignment_No_Target'));
        return;
      }
      this.publishLoading = true;
      api
        .admin_publishAssignment({
          id: this.publishForm.id,
          groupIdList: this.publishForm.groupIdList,
          extraUidList: this.publishForm.extraUidList,
        })
        .then((res) => {
          this.publishLoading = false;
          myMessage.success(this.$i18n.t('m.Assignment_Publish'));
          this.publishDialogVisible = false;
          this.currentChange(1);
        })
        .catch(() => {
          this.publishLoading = false;
        });
    },
    openExtendDialog(row) {
      this.extendForm = { id: row.id, endTime: null };
      this.extendDialogVisible = true;
    },
    confirmExtend() {
      if (!this.extendForm.endTime) {
        myMessage.warning(this.$i18n.t('m.Assignment_Extend_Tips'));
        return;
      }
      this.extendLoading = true;
      api
        .admin_extendAssignment({
          id: this.extendForm.id,
          endTime: this.extendForm.endTime,
        })
        .then((res) => {
          this.extendLoading = false;
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.extendDialogVisible = false;
          this.currentChange(1);
        })
        .catch(() => {
          this.extendLoading = false;
        });
    },
    openStatsDialog(row) {
      this.statsDialogVisible = true;
      this.statsLoading = true;
      this.studentList = [];
      api.admin_getAssignment(row.id).then(
        (res) => {
          this.statsLoading = false;
          this.studentList = res.data.data.studentList || [];
        },
        () => {
          this.statsLoading = false;
        }
      );
    },
  },
};
</script>
<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
  .filter-row span div {
    width: 80% !important;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
.panel-options {
  margin-top: 10px;
}
</style>
