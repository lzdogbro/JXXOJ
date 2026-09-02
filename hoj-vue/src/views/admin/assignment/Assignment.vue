<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ title }}</span>
      </div>
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item :label="$t('m.Title')" required>
              <el-input
                v-model="form.title"
                :placeholder="$t('m.Title')"
                maxlength="200"
                show-word-limit
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Assignment_Description')">
              <Editor :value.sync="form.description"></Editor>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Assignment_Status')">
              <el-radio-group v-model="form.isRequired" :disabled="isPublished">
                <el-radio :label="1">{{ $t('m.Assignment_Required') }}</el-radio>
                <el-radio :label="0">{{ $t('m.Assignment_Optional') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Assignment_Start_Time')" required>
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                :disabled="isPublished"
                style="width:100%"
                :placeholder="$t('m.Assignment_Start_Time')"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Assignment_Deadline')" required>
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                :disabled="isPublished"
                style="width:100%"
                :placeholder="$t('m.Assignment_Deadline')"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Assignment_Problem_List')">
              <div style="margin-bottom:10px">
                <el-button
                  type="primary"
                  size="small"
                  icon="el-icon-plus"
                  :disabled="isPublished"
                  @click="pickerVisible = true"
                  >{{ $t('m.Assignment_Add_Problem') }}
                </el-button>
              </div>
              <vxe-table
                :data="problemList"
                auto-resize
                stripe
                align="center"
                border
              >
                <vxe-table-column
                  min-width="100"
                  :title="$t('m.Assignment_Display_ID')"
                >
                  <template v-slot="{ row }">
                    <el-input
                      v-model="row.displayId"
                      size="mini"
                      :disabled="isPublished"
                    ></el-input>
                  </template>
                </vxe-table-column>
                <vxe-table-column
                  min-width="100"
                  :title="$t('m.Assignment_Problem_Number')"
                >
                  <template v-slot="{ row }">
                    {{ row.problemId }}
                  </template>
                </vxe-table-column>
                <vxe-table-column min-width="220" :title="$t('m.Title')" show-overflow>
                  <template v-slot="{ row }">
                    {{ row.title }}
                  </template>
                </vxe-table-column>
                <vxe-table-column min-width="90" :title="$t('m.Difficulty')">
                  <template v-slot="{ row }">
                    <el-tag
                      v-if="row.difficulty != null"
                      :style="getLevelColor(row.difficulty)"
                      effect="dark"
                      size="small"
                    >
                      {{ getLevelName(row.difficulty) }}
                    </el-tag>
                  </template>
                </vxe-table-column>
                <vxe-table-column min-width="80" :title="$t('m.Option')">
                  <template v-slot="{ row }">
                    <el-button
                      icon="el-icon-delete"
                      size="mini"
                      type="danger"
                      :disabled="isPublished"
                      @click.native="removeProblem(row)"
                    ></el-button>
                  </template>
                </vxe-table-column>
              </vxe-table>
            </el-form-item>
          </el-col>
          <template v-if="!isPublished">
            <el-col :md="12" :xs="24">
              <el-form-item :label="$t('m.Assignment_Student_Group')">
                <el-select
                  v-model="groupIdList"
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
            </el-col>
            <el-col :md="12" :xs="24">
              <el-form-item :label="$t('m.Assignment_Extra_User')">
                <el-select
                  v-model="extraUidList"
                  multiple
                  filterable
                  remote
                  style="width:100%"
                  :remote-method="searchExtraUser"
                  :loading="extraUserLoading"
                  :placeholder="$t('m.Assignment_Extra_User_Placeholder')"
                >
                  <el-option
                    v-for="u in extraUserOptions"
                    :key="u.uid"
                    :label="u.username"
                    :value="u.uid"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>
      <div style="text-align:center;margin-top:20px">
        <el-button
          v-if="!isPublished"
          type="primary"
          @click.native="saveAssignment(false)"
          >{{ $t('m.Assignment_Save_Draft') }}
        </el-button>
        <el-button
          v-if="!isPublished"
          type="success"
          @click.native="saveAssignment(true)"
          >{{ $t('m.Assignment_Save_Publish') }}
        </el-button>
        <el-button v-if="isPublished" type="primary" @click.native="saveAssignment(false)">{{
          $t('m.Save')
        }}</el-button>
      </div>
    </el-card>

    <assignment-problem-picker
      :visible.sync="pickerVisible"
      @confirm="onProblemsSelected"
    ></assignment-problem-picker>
  </div>
</template>

<script>
import api from '@/common/api';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
import time from '@/common/time';
import utils from '@/common/utils';
const Editor = () => import('@/components/admin/Editor.vue');
const AssignmentProblemPicker = () =>
  import('@/views/admin/assignment/AssignmentProblemPicker.vue');
export default {
  name: 'AdminAssignment',
  components: {
    Editor,
    AssignmentProblemPicker,
  },
  data() {
    return {
      title: 'Create Assignment',
      form: {
        id: null,
        title: '',
        description: '',
        isRequired: 1,
        startTime: null,
        endTime: null,
        status: 0,
      },
      problemList: [],
      groupIdList: [],
      extraUidList: [],
      groupList: [],
      extraUserOptions: [],
      extraUserLoading: false,
      pickerVisible: false,
      isPublished: false,
    };
  },
  computed: {
    ...mapGetters(['isSuperAdmin', 'isAdminRole', 'isProblemAdmin']),
    isEdit() {
      return this.$route.name === 'admin-edit-assignment';
    },
  },
  mounted() {
    this.init();
  },
  watch: {
    $route() {
      this.resetForm();
      this.init();
    },
  },
  methods: {
    getLevelColor(difficulty) {
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    displayIdOf(index) {
      if (index < 26) {
        return String.fromCharCode(65 + index);
      }
      return (
        String.fromCharCode(65 + Math.floor(index / 26) - 1) +
        String.fromCharCode(65 + (index % 26))
      );
    },
    resetForm() {
      this.form = {
        id: null,
        title: '',
        description: '',
        isRequired: 1,
        startTime: null,
        endTime: null,
        status: 0,
      };
      this.problemList = [];
      this.groupIdList = [];
      this.extraUidList = [];
      this.extraUserOptions = [];
      this.isPublished = false;
      this.pickerVisible = false;
    },
    init() {
      api.admin_getStudentGroupList().then((res) => {
        this.groupList = res.data.data || [];
      });
      if (this.isEdit) {
        this.title = this.$i18n.t('m.Edit_Assignment');
        this.getAssignment();
      } else {
        this.title = this.$i18n.t('m.Create_Assignment');
      }
    },
    getAssignment() {
      api
        .admin_getAssignment(this.$route.params.assignmentId)
        .then((res) => {
          let data = res.data.data;
          let assignment = data.assignment || {};
          this.form = {
            id: assignment.id,
            title: assignment.title,
            description: assignment.description,
            isRequired: assignment.isRequired,
            startTime: assignment.startTime,
            endTime: assignment.endTime,
            status: assignment.status,
          };
          this.isPublished = assignment.status === 1;
          this.enrichProblemList(data.problemList || []);
        })
        .catch(() => {});
    },
    enrichProblemList(list) {
      let tasks = list.map((p) =>
        api
          .admin_getProblem(p.pid)
          .then(
            (res) => {
              let prob = res.data.data;
              return Object.assign({}, p, {
                problemId: prob.problemId,
                title: prob.title,
                difficulty: prob.difficulty,
              });
            },
            () => {
              return Object.assign({}, p, {
                problemId: null,
                title: null,
                difficulty: null,
              });
            }
          )
      );
      Promise.all(tasks).then((result) => {
        this.problemList = result;
      });
    },
    onProblemsSelected(selected) {
      let next = this.problemList.length;
      selected.forEach((p) => {
        // 去重：已存在的题目不再重复添加
        if (this.problemList.some((item) => item.pid === p.pid)) {
          return;
        }
        this.problemList.push({
          pid: p.pid,
          problemId: p.problemId,
          title: p.title,
          difficulty: p.difficulty,
          displayId: this.displayIdOf(next++),
        });
      });
    },
    removeProblem(row) {
      let idx = this.problemList.findIndex((item) => item.pid === row.pid);
      if (idx >= 0) {
        this.problemList.splice(idx, 1);
      }
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
    validateForm(publish) {
      if (!this.form.title) {
        myMessage.error(this.$i18n.t('m.Title') + ' ' + this.$i18n.t('m.is_required'));
        return false;
      }
      if (!this.form.startTime || !this.form.endTime) {
        myMessage.error(
          this.$i18n.t('m.Assignment_Start_Time') +
            '/' +
            this.$i18n.t('m.Assignment_Deadline') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return false;
      }
      if (time.durationMs(this.form.startTime, this.form.endTime) < 0) {
        myMessage.error(this.$i18n.t('m.Assignment_Time_Check'));
        return false;
      }
      if (this.problemList.length === 0) {
        myMessage.error(this.$i18n.t('m.Assignment_Problem_Empty'));
        return false;
      }
      if (publish) {
        if (
          (!this.groupIdList || this.groupIdList.length === 0) &&
          (!this.extraUidList || this.extraUidList.length === 0)
        ) {
          myMessage.error(this.$i18n.t('m.Assignment_No_Target'));
          return false;
        }
      }
      return true;
    },
    buildProblemList() {
      return this.problemList.map((p) => ({ pid: p.pid, displayId: p.displayId }));
    },
    buildPayload() {
      return {
        id: this.form.id,
        title: this.form.title,
        description: this.form.description,
        isRequired: this.form.isRequired,
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        problemList: this.buildProblemList(),
      };
    },
    saveAssignment(publish) {
      if (!this.validateForm(publish)) {
        return;
      }
      if (this.isPublished) {
        // 已发布仅可改标题/说明
        api
          .admin_updateAssignment({
            id: this.form.id,
            title: this.form.title,
            description: this.form.description,
          })
          .then((res) => {
            myMessage.success(this.$i18n.t('m.Update_Successfully'));
            this.goList();
          })
          .catch(() => {});
        return;
      }

      let payload = this.buildPayload();
      let isCreate = !this.form.id;
      let func = isCreate ? 'admin_createAssignment' : 'admin_updateAssignment';

      if (publish && isCreate) {
        // 新建并发布：一步到位
        payload.status = 1;
        payload.groupIdList = this.groupIdList;
        payload.extraUidList = this.extraUidList;
        api[func](payload)
          .then((res) => {
            myMessage.success(this.$i18n.t('m.Assignment_Publish'));
            this.goList();
          })
          .catch(() => {});
      } else if (publish && !isCreate) {
        // 编辑草稿并发布：先保存草稿，再发布
        payload.status = 0;
        api[func](payload)
          .then((res) => {
            return api.admin_publishAssignment({
              id: this.form.id,
              groupIdList: this.groupIdList,
              extraUidList: this.extraUidList,
            });
          })
          .then((res) => {
            myMessage.success(this.$i18n.t('m.Assignment_Publish'));
            this.goList();
          })
          .catch(() => {});
      } else {
        // 保存草稿
        payload.status = 0;
        api[func](payload)
          .then((res) => {
            myMessage.success(this.$i18n.t('m.Assignment_Save_Draft'));
            this.goList();
          })
          .catch(() => {});
      }
    },
    goList() {
      this.$router.push({
        name: 'admin-assignment-list',
        query: { refresh: 'true' },
      });
    },
  },
};
</script>
<style scoped>
.view {
  padding: 10px;
}
</style>
