<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Student_Group_Admin')
        }}</span>
        <div class="filter">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="openGroupDialog('add', null)"
              icon="el-icon-plus"
              >{{ $t('m.Student_Group_Add') }}
            </el-button>
          </span>
        </div>
      </div>

      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="groupList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"></vxe-table-column>
        <vxe-table-column
          field="name"
          min-width="150"
          :title="$t('m.Student_Group_Name')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="description"
          min-width="200"
          :title="$t('m.Student_Group_Description')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="memberCount"
          width="90"
          :title="$t('m.Student_Group_Member_Count')"
        ></vxe-table-column>
        <vxe-table-column
          field="ownerUid"
          min-width="120"
          :title="$t('m.Creator')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column min-width="140" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Created Time: {{ row.gmtCreate | localtime }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="200" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <el-tooltip
              effect="dark"
              :content="$t('m.Student_Group_Members')"
              placement="top"
            >
              <el-button
                icon="el-icon-user"
                size="mini"
                @click.native="openMemberDialog(row)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('m.Edit')" placement="top">
              <el-button
                icon="el-icon-edit"
                size="mini"
                @click.native="openGroupDialog('update', row)"
                type="success"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('m.Delete')" placement="top">
              <el-button
                icon="el-icon-delete"
                size="mini"
                @click.native="deleteGroup(row)"
                type="danger"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-card>

    <el-dialog
      :title="$t('m.' + groupDialogTitle)"
      :visible.sync="groupDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Student_Group_Name')" required>
          <el-input v-model="groupForm.name" size="small" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Student_Group_Description')">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            :rows="3"
            maxlength="500"
          ></el-input>
        </el-form-item>
        <el-form-item style="text-align:center">
          <el-button type="primary" @click="saveGroup" :loading="saveGroupLoading">{{
            $t('m.' + groupDialogBtn)
          }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      :title="$t('m.Student_Group_Members')"
      :visible.sync="memberDialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom:10px;text-align:right">
        <el-button
          type="primary"
          size="small"
          icon="el-icon-plus"
          @click="openAddMemberDialog"
          >{{ $t('m.Student_Group_Add_Member') }}
        </el-button>
      </div>
      <vxe-table
        :loading="memberLoading"
        :data="memberList"
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
          field="nickname"
          min-width="120"
          :title="$t('m.Nickname')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="number"
          min-width="120"
          :title="$t('m.Number')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column min-width="80" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <el-button
              icon="el-icon-delete"
              size="mini"
              type="danger"
              @click.native="removeMember(row)"
            ></el-button>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-dialog>

    <el-dialog
      :title="$t('m.Student_Group_Add_Member')"
      :visible.sync="addMemberDialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom:10px">
        <vxe-input
          v-model="memberKeyword"
          :placeholder="$t('m.Enter_keyword')"
          type="search"
          size="medium"
          @search-click="searchMember"
          @keyup.enter.native="searchMember"
        ></vxe-input>
      </div>
      <vxe-table
        :loading="addMemberLoading"
        ref="memberTable"
        :data="userList"
        auto-resize
        stripe
        align="center"
        max-height="400"
        :checkbox-config="{ labelField: 'id', highlight: true, range: true }"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column
          field="username"
          min-width="140"
          :title="$t('m.User')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="realname"
          min-width="130"
          :title="$t('m.RealName')"
          show-overflow
        ></vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="memberPageChange"
          :page-size="memberLimit"
          :total="memberTotal"
        >
        </el-pagination>
      </div>
      <div style="text-align:center;margin-top:10px">
        <el-button type="primary" @click="addMembers" :loading="addMemberLoading">{{
          $t('m.Add')
        }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
export default {
  name: 'AdminStudentGroup',
  data() {
    return {
      loading: false,
      groupList: [],
      groupDialogVisible: false,
      groupDialogTitle: 'Student_Group_Add',
      groupDialogBtn: 'To_Add',
      saveGroupLoading: false,
      groupForm: {
        id: null,
        name: '',
        description: '',
      },
      memberDialogVisible: false,
      memberLoading: false,
      memberList: [],
      currentGroup: null,
      addMemberDialogVisible: false,
      addMemberLoading: false,
      userList: [],
      memberKeyword: '',
      memberPage: 1,
      memberLimit: 10,
      memberTotal: 0,
    };
  },
  computed: {
    ...mapGetters(['isSuperAdmin', 'isAdminRole', 'isProblemAdmin']),
    isWritable() {
      return this.isSuperAdmin || (this.isAdminRole && !this.isProblemAdmin);
    },
  },
  mounted() {
    this.getGroupList();
  },
  methods: {
    getGroupList() {
      this.loading = true;
      api.admin_getStudentGroupList().then(
        (res) => {
          this.loading = false;
          this.groupList = res.data.data || [];
        },
        () => {
          this.loading = false;
        }
      );
    },
    openGroupDialog(action, group) {
      if (action === 'add') {
        this.groupDialogTitle = 'Student_Group_Add';
        this.groupDialogBtn = 'To_Add';
        this.groupForm = { id: null, name: '', description: '' };
      } else {
        this.groupDialogTitle = 'Student_Group_Edit';
        this.groupDialogBtn = 'To_Update';
        this.groupForm = { id: group.id, name: group.name, description: group.description };
      }
      this.groupDialogVisible = true;
    },
    saveGroup() {
      if (!this.groupForm.name) {
        myMessage.error(
          this.$i18n.t('m.Student_Group_Name') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      let data = {
        name: this.groupForm.name,
        description: this.groupForm.description,
      };
      this.saveGroupLoading = true;
      let func = this.groupForm.id ? 'admin_updateStudentGroup' : 'admin_createStudentGroup';
      if (this.groupForm.id) {
        data.id = this.groupForm.id;
      }
      api[func](data).then(
        (res) => {
          this.saveGroupLoading = false;
          myMessage.success(
            this.groupForm.id
              ? this.$i18n.t('m.Update_Successfully')
              : this.$i18n.t('m.Add_Successfully')
          );
          this.groupDialogVisible = false;
          this.getGroupList();
        },
        () => {
          this.saveGroupLoading = false;
        }
      );
    },
    deleteGroup(group) {
      this.$confirm(this.$i18n.t('m.Student_Group_Delete_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.admin_deleteStudentGroup(group.id).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.getGroupList();
        });
      });
    },
    openMemberDialog(group) {
      this.currentGroup = group;
      this.memberDialogVisible = true;
      this.getMemberList(group.id);
    },
    getMemberList(gid) {
      this.memberLoading = true;
      api.admin_getStudentGroupUserList(gid).then(
        (res) => {
          this.memberLoading = false;
          this.memberList = res.data.data || [];
        },
        () => {
          this.memberLoading = false;
        }
      );
    },
    openAddMemberDialog() {
      this.memberKeyword = '';
      this.memberPage = 1;
      this.addMemberDialogVisible = true;
      this.getUserList(1);
    },
    getUserList(page) {
      this.addMemberLoading = true;
      api.admin_getUserList(page, this.memberLimit, this.memberKeyword, false).then(
        (res) => {
          this.addMemberLoading = false;
          this.memberTotal = res.data.data.total;
          this.userList = res.data.data.records;
        },
        () => {
          this.addMemberLoading = false;
        }
      );
    },
    searchMember() {
      this.memberPage = 1;
      this.getUserList(1);
    },
    memberPageChange(page) {
      this.memberPage = page;
      this.getUserList(page);
    },
    addMembers() {
      let records = this.$refs.memberTable.getCheckboxRecords();
      if (!records || records.length === 0) {
        myMessage.warning(this.$i18n.t('m.The_number_of_users_selected_cannot_be_empty'));
        return;
      }
      let uidList = records.map((r) => r.uid);
      api.admin_addStudentGroupUser(this.currentGroup.id, uidList).then((res) => {
        myMessage.success(this.$i18n.t('m.Add_Successfully'));
        this.addMemberDialogVisible = false;
        this.getMemberList(this.currentGroup.id);
        this.getGroupList();
      });
    },
    removeMember(row) {
      this.$confirm(this.$i18n.t('m.Student_Group_Remove_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.admin_removeStudentGroupUser(this.currentGroup.id, row.uid).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.getMemberList(this.currentGroup.id);
          this.getGroupList();
        });
      });
    },
  },
};
</script>
<style scoped>
.filter {
  margin-top: 10px;
}
.filter span {
  margin-right: 10px;
}
.panel-options {
  margin-top: 10px;
  text-align: right;
}
</style>
