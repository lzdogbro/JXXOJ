<template>
  <div>
    <template v-if="!mobileNar">
      <div id="header">
        <el-menu
          ref="navMenu"
          :default-active="activeMenuName"
          mode="horizontal"
          router
          active-text-color="#2196f3"
          text-color="#495060"
        >
          <div ref="navLogo" class="logo">
            <el-tooltip
              :content="$t('m.Click_To_Change_Web_Language')"
              placement="bottom"
              effect="dark"
            >
              <el-image
                style="width: 139px; height: 50px"
                :src="imgUrl"
                fit="scale-down"
                @click="changeWebLanguage"
              ></el-image>
            </el-tooltip>
          </div>
          <template v-if="mode == 'defalut'">
            <template v-for="item in visiblePrimary">
              <el-menu-item
                v-if="item.type === 'item'"
                :key="item.key"
                :index="item.index"
                :ref="'nav-' + item.key"
              >
                <i :class="item.icon"></i>{{ $t(item.i18n) }}
              </el-menu-item>
              <el-submenu
                v-else
                :key="item.key"
                :index="item.index"
                :ref="'nav-' + item.key"
                :class="item.key === 'practice' && hasUnfinishedAssignment ? 'nav-assignment-flash' : ''"
              >
                <template slot="title">
                  <i :class="item.icon"></i>{{ $t(item.i18n) }}
                  <span
                    v-if="item.key === 'practice' && unfinishedBadgeCount > 0"
                    class="nav-assignment-badge"
                    >{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span
                  >
                </template>
                <el-menu-item
                  v-for="c in item.children"
                  :key="c.index"
                  :index="c.index"
                  :class="c.index === '/assignment' && hasUnfinishedAssignment ? 'nav-assignment-flash' : ''"
                >
                  {{ $t(c.i18n) }}
                  <span
                    v-if="c.index === '/assignment' && unfinishedBadgeCount > 0"
                    class="nav-assignment-badge"
                    >{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span
                  >
                </el-menu-item>
              </el-submenu>
            </template>
            <el-submenu index="more" ref="nav-more"
              :class="hasUnfinishedAssignment && assignmentParentOverflowed ? 'nav-assignment-flash' : ''"
            >
              <template slot="title">
                <i class="el-icon-more-outline"></i>{{ $t('m.NavBar_More') }}
                <span
                  v-if="assignmentParentOverflowed && unfinishedBadgeCount > 0"
                  class="nav-assignment-badge"
                  >{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span
                >
              </template>
              <template v-for="item in overflowPrimary">
                <el-menu-item
                  v-if="item.type === 'item'"
                  :key="'o-' + item.key"
                  :index="item.index"
                  >{{ $t(item.i18n) }}</el-menu-item
                >
                <template v-else>
                  <el-menu-item
                    v-for="c in item.children"
                    :key="'oc-' + c.index"
                    :index="c.index"
                    :class="c.index === '/assignment' && hasUnfinishedAssignment ? 'nav-assignment-flash' : ''"
                  >
                    {{ $t(c.i18n) }}
                    <span
                      v-if="c.index === '/assignment' && unfinishedBadgeCount > 0"
                      class="nav-assignment-badge"
                      >{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span
                    >
                  </el-menu-item>
                </template>
              </template>
              <el-menu-item
                v-for="m in moreFixed"
                :key="'m-' + m.index"
                :index="m.index"
                :class="m.deprecated ? 'nav-deprecated' : ''"
                >{{ $t(m.i18n) }}</el-menu-item
              >
            </el-submenu>
        </template>
        <template v-else-if="mode == 'training'">
          <el-menu-item index="/home"
              ><i class="el-icon-s-home"></i>{{ $t('m.NavBar_Back_Home') }}</el-menu-item
            >
            <template v-if="$route.params.groupID">
              <el-menu-item :index="'/group/' + $route.params.groupID"
              ><i
                class="fa fa-users navbar-icon"
              ></i
              >{{ $t('m.NavBar_Group_Home') }}</el-menu-item>
            </template>
            <el-menu-item :index="getTrainingHomePath()"
              ><i class="el-icon-s-claim"></i>{{ $t('m.NavBar_Training_Home') }}</el-menu-item
            >
            <el-menu-item :index="getTrainingProblemListPath()"
              ><i class="fa fa-list navbar-icon"></i>{{ $t('m.Problem_List') }}</el-menu-item
            >
        </template>
        <template v-else-if="mode == 'contest'">
          <el-menu-item index="/home"
              ><i class="el-icon-s-home"></i>{{ $t('m.NavBar_Back_Home') }}</el-menu-item
            >
            <el-menu-item :index="'/contest/' + $route.params.contestID"
              ><i class="el-icon-trophy"></i>{{ $t('m.NavBar_Contest_Home') }}</el-menu-item
            >
            <el-menu-item :index="'/contest/' + $route.params.contestID + '/problems'"
              ><i class="fa fa-list navbar-icon"></i>{{ $t('m.Problem_List') }}</el-menu-item
            >
            <el-menu-item :index="'/contest/' + $route.params.contestID + '/submissions?onlyMine=true'"
              ><i class="el-icon-menu"></i>{{ $t('m.NavBar_Contest_Own_Submission') }}</el-menu-item
            >
            <el-menu-item :index="'/contest/' + $route.params.contestID + '/rank'"
              ><i class="fa fa-bar-chart navbar-icon"></i>{{ $t('m.NavBar_Contest_Rank') }}</el-menu-item
            >
        </template>
        <template v-else-if="mode == 'group'">
          <el-menu-item index="/home"
              ><i class="el-icon-s-home"></i>{{ $t('m.NavBar_Back_Home') }}</el-menu-item
            >
            <template v-if="$route.params.groupID">
              <el-menu-item :index="'/group/' + $route.params.groupID"
              ><i
                class="fa fa-users navbar-icon"
              ></i
              >{{ $t('m.NavBar_Group_Home') }}</el-menu-item>
            </template>
            <el-menu-item :index="'/group/' + $route.params.groupID + '/problem'"
              ><i class="fa fa-list navbar-icon"></i>{{ $t('m.Problem_List') }}</el-menu-item
            >
        </template>

          <template v-if="!isAuthenticated">
            <div class="btn-menu">
              <el-button 
                type="primary" 
                size="medium" 
                round
                @click="handleBtnClick('Login')"
                >{{ $t('m.NavBar_Login') }}
              </el-button>
              <el-button
                v-if="websiteConfig.register"
                size="medium"
                round
                @click="handleBtnClick('Register')"
                style="margin-left: 5px"
                >{{ $t('m.NavBar_Register') }}
              </el-button>
            </div>
          </template>
          <template v-else>
            <el-dropdown
              class="drop-menu"
              @command="handleRoute"
              placement="bottom"
              trigger="hover"
            >
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-caret-bottom"></i>
              </span>

              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="/user-home">{{
                  $t('m.NavBar_UserHome')
                }}</el-dropdown-item>
                <el-dropdown-item command="/status?onlyMine=true">{{
                  $t('m.NavBar_Submissions')
                }}</el-dropdown-item>
                <el-dropdown-item command="/pk-history">{{
                  $t('m.NavBar_PK')
                }}</el-dropdown-item>
                <el-dropdown-item command="/setting">{{
                  $t('m.NavBar_Setting')
                }}</el-dropdown-item>
                <el-dropdown-item v-if="isAdminRole" command="/admin">{{
                  $t('m.NavBar_Management')
                }}</el-dropdown-item>
                <el-dropdown-item divided command="/logout">{{
                  $t('m.NavBar_Logout')
                }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="avatar"
              class="drop-avatar"
            ></avatar>
            <el-dropdown
              :class="['drop-msg', hasUnreadMessage ? 'nav-msg-flash' : '']"
              @command="handleRoute"
              placement="bottom"
            >
              <span class="el-dropdown-link">
                <i class="el-icon-message-solid"></i>
                <svg
                  v-if="hasUnreadMessage"
                  width="10"
                  height="10"
                  style="vertical-align: top;margin-left: -11px;margin-top: 3px;"
                >
                  <circle cx="5" cy="5" r="5" style="fill: red;"></circle>
                </svg>
              </span>

              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="/message/discuss">
                  <span>{{ $t('m.DiscussMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.comment > 0">
                    <MsgSvg :total="unreadMessage.comment"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/reply">
                  <span>{{ $t('m.ReplyMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.reply > 0">
                    <MsgSvg :total="unreadMessage.reply"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/like">
                  <span>{{ $t('m.LikeMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.like > 0">
                    <MsgSvg :total="unreadMessage.like"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/sys">
                  <span>{{ $t('m.SysMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.sys > 0">
                    <MsgSvg :total="unreadMessage.sys"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/mine">
                  <span>{{ $t('m.MineMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.mine > 0">
                    <MsgSvg :total="unreadMessage.mine"></MsgSvg>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <div
              class="nav-chat"
              :title="$t('m.NavBar_Chat')"
              @click="goChat"
            >
              <i class="el-icon-chat-dot-round"></i>
              <span v-if="hasUnreadChat" class="nav-chat-badge">
                <MsgSvg :total="unreadMessage.chat"></MsgSvg>
              </span>
            </div>
          </template>
          <div class="nav-search">
            <el-input
              v-model="problemKeyword"
              size="small"
              :placeholder="$t('m.NavBar_Search_Problem')"
              prefix-icon="el-icon-search"
              clearable
              @keyup.enter.native="searchProblem"
            ></el-input>
          </div>
        </el-menu>
      </div>
      <div id="header-hidden" v-show="isScrolled">
      </div>
    </template>
    <template v-else>
      <div style="top:0px;left:0px;">
      <mu-appbar class="mobile-nav" color="primary">
        <mu-button icon slot="left" @click="opendrawer = !opendrawer">
          <i class="el-icon-s-unfold"></i>
        </mu-button>
        <el-tooltip
            :content="$t('m.Click_To_Change_Web_Language')"
            placement="bottom"
            effect="dark"
          >
          <span @click="changeWebLanguage">
          {{
            websiteConfig.shortName ? websiteConfig.shortName : 'OJ'
          }}
          </span>
        </el-tooltip>
        <mu-button
          flat
          slot="right"
          @click="handleBtnClick('Login')"
          v-show="!isAuthenticated"
          >{{ $t('m.NavBar_Login') }}</mu-button
        >
        <mu-button
          flat
          slot="right"
          @click="handleBtnClick('Register')"
          v-show="!isAuthenticated && websiteConfig.register"
          >{{ $t('m.NavBar_Register') }}</mu-button
        >

        <mu-button
          flat
          slot="right"
          v-show="isAuthenticated"
          @click="goChat"
        >
          <mu-icon value=":el-icon-chat-dot-round" size="24"></mu-icon>
          <svg
            v-if="hasUnreadChat"
            width="10"
            height="10"
            style="margin-left: -11px;margin-top: -13px;"
          >
            <circle cx="5" cy="5" r="5" style="fill: red;"></circle>
          </svg>
        </mu-button>

        <mu-menu slot="right" v-show="isAuthenticated" :open.sync="openmsgmenu">
          <mu-button flat>
            <mu-icon :class="hasUnreadMessage ? 'nav-msg-flash' : ''" value=":el-icon-message-solid" size="24"></mu-icon>
            <svg
              v-if="hasUnreadMessage"
              width="10"
              height="10"
              style="margin-left: -11px;margin-top: -13px;"
            >
              <circle cx="5" cy="5" r="5" style="fill: red;"></circle>
            </svg>
          </mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="/message/discuss">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.DiscussMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.comment > 0">
                    <MsgSvg :total="unreadMessage.comment"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/reply">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.ReplyMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.reply > 0">
                    <MsgSvg :total="unreadMessage.reply"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/like">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.LikeMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.like > 0">
                    <MsgSvg :total="unreadMessage.like"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/sys">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.SysMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.sys > 0">
                    <MsgSvg :total="unreadMessage.sys"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>

            <mu-list-item button value="/message/mine">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.MineMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.mine > 0">
                    <MsgSvg :total="unreadMessage.mine"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>

        <mu-menu
          slot="right"
          v-if="isAuthenticated"
          :open.sync="openusermenu"
        >
          <mu-button flat>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="avatar"
              :title="userInfo.username"
            ></avatar>
            <i class="el-icon-caret-bottom"></i>
          </mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="/user-home">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_UserHome')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/status?onlyMine=true">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Submissions')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/pk-history">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_PK')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/setting">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Setting')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/admin" v-show="isAdminRole">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Management')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>

            <mu-list-item button value="/logout">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Logout')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>
      </mu-appbar>

      <mu-appbar style="width: 100%;">
        <!--占位，刚好占领导航栏的高度-->
      </mu-appbar>

      <mu-drawer :open.sync="opendrawer" :docked="false" :right="false">
        <mu-list toggle-nested>
          <mu-list-item
            button
            to="/home"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-home" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Home') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'practice'"
            @toggle-nested="openSideMenu = arguments[0] ? 'practice' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-claim" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title :class="hasUnfinishedAssignment ? 'nav-assignment-flash' : ''">
              {{ $t('m.NavBar_Practice') }}
              <span v-if="unfinishedBadgeCount > 0" class="nav-assignment-badge">{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span>
            </mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/problem"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.NavBar_Problem') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/training"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.NavBar_Training') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/assignment"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title :class="hasUnfinishedAssignment ? 'nav-assignment-flash' : ''">
                {{ $t('m.NavBar_Assignment') }}
                <span v-if="unfinishedBadgeCount > 0" class="nav-assignment-badge">{{ unfinishedBadgeCount > 99 ? 99 : unfinishedBadgeCount }}</span>
              </mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            button
            to="/contest"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-trophy" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Contest')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/status"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-marketing" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Status') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'rank'"
            @toggle-nested="openSideMenu = arguments[0] ? 'rank' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-data" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Rank') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/acm-rank"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_ACM_Rank')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/oi-rank"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_OI_Rank')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            v-if="websiteConfig.openPublicDiscussion"
            button
            to="/discussion"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-comments" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Discussion')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'more'"
            @toggle-nested="openSideMenu = arguments[0] ? 'more' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-info" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_More') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/introduction"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_Introduction')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/developer"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_Developer')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>
        </mu-list>
      </mu-drawer>
    </div>
    </template>
    
    <el-dialog
      :visible.sync="modalVisible"
      width="370px"
      class="dialog"
      :title="title"
      :close-on-click-modal="false"
    >
      <component :is="modalStatus.mode" v-if="modalVisible"></component>
      <div slot="footer" style="display: none"></div>
    </el-dialog>
  </div>
</template>
<script>
import Login from '@/components/oj/common/Login';
import Register from '@/components/oj/common/Register';
import ResetPwd from '@/components/oj/common/ResetPassword';
import MsgSvg from '@/components/oj/msg/msgSvg';
import { mapGetters, mapActions } from 'vuex';
import Avatar from 'vue-avatar';
import api from '@/common/api';
export default {
  components: {
    Login,
    Register,
    ResetPwd,
    Avatar,
    MsgSvg,
  },
  created(){
    this.page_width();
    window.onresize = () => {
      this.page_width();
      this.setHiddenHeaderHeight();
      this.recomputeNavOverflow();
    };
  },
  mounted() {
    this.switchMode();
    this.setHiddenHeaderHeight();
    this.$nextTick(() => {
      this.recomputeNavOverflow();
    });
    // 字体（尤其图标字体）加载完成后重新测量一次，避免宽度变化导致溢出换行
    if (document.fonts && document.fonts.ready) {
      document.fonts.ready.then(() => {
        this.remeasureNavOverflow();
      });
    }
    this.navFontTimer = setTimeout(() => {
      this.remeasureNavOverflow();
    }, 1500);
    if (this.isAuthenticated) {
      this.getUnreadMsgCount();
      this.msgTimer = setInterval(() => {
        this.getUnreadMsgCount();
      }, 120 * 1000);
      this.startAssignmentPolling();
    }
    // 兜底：5秒后如果页面还没发 contentReady 信号，就启动PK轮询
    this.fallbackTimer = setTimeout(() => {
      if (!this.$store.state.user.contentReady) {
        this.$store.commit('setContentReady', true);
      }
    }, 5000);
  },
  beforeDestroy() {
    clearInterval(this.msgTimer);
    clearInterval(this.pkTimer);
    clearInterval(this.assignmentTimer);
    clearTimeout(this.fallbackTimer);
    clearTimeout(this.navFontTimer);
  },
  data() {
    return {
      mode:'defalut',
      centerDialogVisible: false,
      mobileNar: false,
      opendrawer: false,
      openusermenu: false,
      openmsgmenu: false,
      openSideMenu: '',
      imgUrl: require('@/assets/logo.png'),
      avatarStyle:
        'display: inline-flex;width: 30px;height: 30px;border-radius: 50%;align-items: center;justify-content: center;text-align: center;user-select: none;',
      msgTimer: null,
      pkTimer: null,
      fallbackTimer: null,
      navFontTimer: null,
      assignmentTimer: null,
      correctingNav: false,
      problemKeyword: '',
      notifiedInviteIds: [],
      // 桌面端主导航（从左到右的顺序，越靠右越先被收进「更多」）
      primaryNav: [
        { key: 'home', type: 'item', index: '/home', icon: 'el-icon-s-home', i18n: 'm.NavBar_Home' },
        {
          key: 'practice',
          type: 'submenu',
          index: 'practice',
          icon: 'el-icon-s-claim',
          i18n: 'm.NavBar_Practice',
          children: [
            { index: '/problem', i18n: 'm.NavBar_Problem' },
            { index: '/training', i18n: 'm.NavBar_Training' },
            { index: '/assignment', i18n: 'm.NavBar_Assignment' }
          ]
        },
        { key: 'contest', type: 'item', index: '/contest', icon: 'el-icon-trophy', i18n: 'm.NavBar_Contest' },
        { key: 'status', type: 'item', index: '/status', icon: 'el-icon-s-marketing', i18n: 'm.NavBar_Status' },
        {
          key: 'rank',
          type: 'submenu',
          index: 'rank',
          icon: 'el-icon-s-data',
          i18n: 'm.NavBar_Rank',
          children: [
            { index: '/acm-rank', i18n: 'm.NavBar_ACM_Rank' },
            { index: '/oi-rank', i18n: 'm.NavBar_OI_Rank' }
          ]
        },
        { key: 'discussion', type: 'item', index: '/discussion', icon: 'el-icon-s-comment', i18n: 'm.NavBar_Discussion', condition: 'openPublicDiscussion' },
      ],
      // 「更多」里固定展示的项（始终保留，收在最后）
      moreFixed: [
        { index: '/introduction', i18n: 'm.NavBar_Introduction' },
        { index: '/developer', i18n: 'm.NavBar_Developer' },
      ],
      // 主导航当前展示的数量（初始给个大值表示全部展示）
      visibleCount: 99,
      itemWidths: {},
    };
  },
  methods: {
    ...mapActions(['changeModalStatus']),
    page_width() {
      let screenWidth = window.screen.width;
      if (screenWidth < 992) {
        this.mobileNar = true;
      } else {
        this.mobileNar = false;
      }
    },
    goChat() {
      this.$router.push({ path: '/chat' });
    },
    searchProblem() {
      const keyword = (this.problemKeyword || '').trim();
      if (!keyword) return;
      this.problemKeyword = '';
      this.$router.push({ path: '/problem', query: { keyword } });
    },
    // 右侧登录/注册按钮或用户下拉区域的最左边界（用于计算可用宽度）
    getRightBlockLeft() {
      let left = null;
      const els = this.$el.querySelectorAll('.btn-menu, .drop-menu, .drop-avatar, .drop-msg, .nav-chat, .nav-search');
      for (const el of els) {
        if (el.offsetParent === null) continue;
        const r = el.getBoundingClientRect();
        if (r.width === 0 && r.height === 0) continue;
        if (left === null || r.left < left) left = r.left;
      }
      return left;
    },
    // 取 ref 对应的真实 DOM 元素（v-for 里的动态 ref 是数组，需取第一项）
    getRefEl(ref) {
      if (ref == null) return null;
      const r = Array.isArray(ref) ? ref[0] : ref;
      return r && (r.$el || r);
    },
    measureNavWidths() {
      const widths = {};
      this.filteredPrimary.forEach((item) => {
        const el = this.getRefEl(this.$refs['nav-' + item.key]);
        widths[item.key] = el ? el.getBoundingClientRect().width : 0;
      });
      this.itemWidths = widths;
    },
    applyNavFit() {
      if (this.mobileNar) return;
      if (Object.keys(this.itemWidths).length === 0) return;
      const logoEl = this.getRefEl(this.$refs.navLogo);
      const moreEl = this.getRefEl(this.$refs['nav-more']);
      const rightLeft = this.getRightBlockLeft();
      if (!logoEl || !moreEl || rightLeft == null) return;
      const logoRight = logoEl.getBoundingClientRect().right;
      const moreWidth = moreEl.getBoundingClientRect().width;
      const available = rightLeft - logoRight - moreWidth - 12;
      let count = 0;
      let used = 0;
      for (const item of this.filteredPrimary) {
        const w = this.itemWidths[item.key] || 0;
        if (used + w <= available) {
          used += w;
          count++;
        } else {
          break;
        }
      }
      this.visibleCount = count;
      // 自纠：若右侧区域（铃铛/头像/下拉）仍被挤到下一行，就继续多收起一些 li
      this.$nextTick(() => this.correctNavOverflow());
    },
    // 判断右侧登录/用户区域是否被挤到第二行
    isRightBlockPushedDown() {
      const menuEl = this.getRefEl(this.$refs.navMenu);
      if (!menuEl) return false;
      const menuTop = menuEl.getBoundingClientRect().top;
      const els = this.$el.querySelectorAll('.btn-menu, .drop-menu, .drop-avatar, .drop-msg, .nav-chat, .nav-search');
      for (const el of els) {
        if (el.offsetParent === null) continue;
        const r = el.getBoundingClientRect();
        if (r.width === 0 && r.height === 0) continue;
        if (r.top > menuTop + 40) return true;
      }
      return false;
    },
    correctNavOverflow() {
      if (this.correctingNav) return;
      if (this.mobileNar || this.visibleCount <= 0) return;
      if (!this.isRightBlockPushedDown()) return;
      this.correctingNav = true;
      const step = () => {
        if (this.mobileNar || this.visibleCount <= 0 || !this.isRightBlockPushedDown()) {
          this.correctingNav = false;
          return;
        }
        this.visibleCount--;
        this.$nextTick(step);
      };
      step();
    },
    recomputeNavOverflow() {
      if (this.mobileNar) {
        this.visibleCount = this.filteredPrimary.length;
        return;
      }
      // 首次测量：先把所有项渲染出来
      if (Object.keys(this.itemWidths).length === 0) {
        this.visibleCount = this.filteredPrimary.length;
        this.$nextTick(() => {
          if (this.mobileNar) return;
          this.measureNavWidths();
          this.applyNavFit();
        });
      } else {
        this.applyNavFit();
      }
    },
    remeasureNavOverflow() {
      if (this.mobileNar) return;
      this.visibleCount = this.filteredPrimary.length;
      this.$nextTick(() => {
        if (this.mobileNar) return;
        this.measureNavWidths();
        this.applyNavFit();
      });
    },
    handleBtnClick(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    handleRoute(route) {
      //电脑端导航栏路由跳转事件
      if (route && route.split('/')[1] != 'admin') {
        this.$router.push(route);
      } else {
        window.open('/admin/');
      }
    },
    handleCommand(route) {
      // 移动端导航栏路由跳转事件
      this.openusermenu = false;
      this.openmsgmenu = false;
      if (route && route.split('/')[1] != 'admin') {
        this.$router.push(route);
      } else {
        window.open('/admin/');
      }
    },
    getUnreadMsgCount() {
      api.getUnreadMsgCount().then((res) => {
        let data = res.data.data;
        this.$store.dispatch('updateUnreadMessageCount', data);
        let sumMsg =
          data.comment + data.reply + data.like + data.mine + data.sys + (data.chat || 0);
        if (sumMsg > 0) {
          if (this.webLanguage == 'zh-CN') {
            this.$notify.info({
              title: '未读消息',
              message:
                '亲爱的【' +
                this.userInfo.username +
                '】，您有最新的' +
                sumMsg +
                '条未读消息，请注意查看！',
              position: 'bottom-right',
              duration: 5000,
            });
          } else {
            this.$notify.info({
              title: 'Unread Message',
              message:
                'Dear【' +
                this.userInfo.username +
                '】, you have the latest ' +
                sumMsg +
                ' unread messages. Please check them!',
              position: 'bottom-right',
              duration: 5000,
            });
          }
        }
      });
    },
    refreshChatUnread() {
      if (!this.isAuthenticated) return;
      api.getChatUnreadCount().then((res) => {
        const count = res.data.data || 0;
        this.$store.dispatch('updateUnreadMessageCount', {
          ...this.unreadMessage,
          chat: count,
        });
      }).catch(() => {});
    },
    startPkPolling() {
      if (this.pkTimer) {
        return; // 已经在轮询中
      }
      this.checkPkInvites();
      this.checkActivePkMatch();
      this.pkTimer = setInterval(() => {
        this.checkPkInvites();
        this.checkActivePkMatch();
      }, 10 * 1000);
    },
    startAssignmentPolling() {
      if (this.assignmentTimer) {
        return; // 已经在轮询中
      }
      this.$store.dispatch('getAssignmentUnfinishedCount').catch(() => {});
      this.assignmentTimer = setInterval(() => {
        this.$store.dispatch('getAssignmentUnfinishedCount').catch(() => {});
      }, 60 * 1000);
    },
    checkPkInvites() {
      api.getMyPkInvites().then((res) => {
        let invites = res.data.data;
        // 清理不在当前邀请列表中的已通知ID
        if (invites && invites.length > 0) {
          const currentIds = invites.map(i => i.id);
          this.notifiedInviteIds = this.notifiedInviteIds.filter(id => currentIds.includes(id));
          this.$store.commit('setPkInvites', invites);
          invites.forEach(invite => {
            // 已经弹过通知的邀请不再重复弹
            if (this.notifiedInviteIds.includes(invite.id)) {
              return;
            }
            this.notifiedInviteIds.push(invite.id);
            const from = this.$i18n.t('m.PK_Invite_From') + ' ' + (invite.initiatorNickname || invite.initiatorUsername);
            const problem = this.$i18n.t('m.PK_Invite_Problem') + ': ' + invite.problemTitle;
            const html = '<div class="pk-invite-notify-body">'
              + '<p>' + from + ', ' + problem + '</p>'
              + '<div class="pk-invite-notify-actions">'
              + '<button class="pk-notify-btn pk-notify-accept" data-pk-id="' + invite.id + '">' + this.$i18n.t('m.PK_Accept') + '</button>'
              + '<button class="pk-notify-btn pk-notify-reject" data-pk-id="' + invite.id + '">' + this.$i18n.t('m.PK_Reject') + '</button>'
              + '<button class="pk-notify-btn pk-notify-dismiss" data-pk-id="' + invite.id + '">' + this.$i18n.t('m.PK_Dismiss') + '</button>'
              + '</div></div>';
            const notification = this.$notify({
              title: this.$i18n.t('m.PK_Invite_Received'),
              message: html,
              position: 'bottom-right',
              duration: 0,
              dangerouslyUseHTMLString: true,
              customClass: 'pk-invite-notify',
            });
            // 给按钮绑定事件
            this.$nextTick(() => {
              const el = document.querySelectorAll('.pk-invite-notify');
              const currentEl = el[el.length - 1];
              if (currentEl) {
                const acceptBtn = currentEl.querySelector('.pk-notify-accept');
                const rejectBtn = currentEl.querySelector('.pk-notify-reject');
                const dismissBtn = currentEl.querySelector('.pk-notify-dismiss');
                if (acceptBtn) {
                  acceptBtn.addEventListener('click', () => {
                    this.acceptPkInvite(invite.id);
                    notification.close();
                  });
                }
                if (rejectBtn) {
                  rejectBtn.addEventListener('click', () => {
                    this.rejectPkInvite(invite.id);
                    notification.close();
                  });
                }
                if (dismissBtn) {
                  dismissBtn.addEventListener('click', () => {
                    this.dismissPkInvite(invite.id);
                    notification.close();
                  });
                }
              }
            });
          });
        } else {
          // 没有邀请时清空已通知记录
          this.notifiedInviteIds = [];
          this.$store.commit('setPkInvites', []);
        }
      }).catch(() => {});
    },
    acceptPkInvite(matchId) {
      api.respondPkInvite({ matchId: matchId, accept: true }).then(res => {
        this.$message.success(this.$i18n.t('m.PK_Accept'));
        // 清除该邀请记录
        this.notifiedInviteIds = this.notifiedInviteIds.filter(id => id !== matchId);
        // 刷新store
        let invites = this.$store.state.user.pkInvites.filter(i => i.id !== matchId);
        this.$store.commit('setPkInvites', invites);
        // 接受后跳转到PK对战页面
        this.$router.push({ name: 'PkPage', params: { matchId: matchId } });
      }).catch(() => {
        this.$message.error(this.$i18n.t('m.System_Error'));
      });
    },
    rejectPkInvite(matchId) {
      api.respondPkInvite({ matchId: matchId, accept: false }).then(res => {
        this.$message.success(this.$i18n.t('m.PK_Reject'));
        // 清除该邀请记录
        this.notifiedInviteIds = this.notifiedInviteIds.filter(id => id !== matchId);
        // 刷新store
        let invites = this.$store.state.user.pkInvites.filter(i => i.id !== matchId);
        this.$store.commit('setPkInvites', invites);
      }).catch(() => {
        this.$message.error(this.$i18n.t('m.System_Error'));
      });
    },
    dismissPkInvite(matchId) {
      // 不再提示：ID 已在 checkPkInvites 中添加到 notifiedInviteIds，
      // 无需额外操作，下次轮询自动跳过该邀请
    },
    checkActivePkMatch() {
      // 如果已经在PK对战页面，不需要再检测跳转
      if (this.$route.name === 'PkPage') {
        return;
      }
      api.getMyActivePkMatch().then(res => {
        const match = res.data.data;
        if (match && match.status === 1) {
          // 有进行中的PK对战，跳转过去
          this.$router.push({ name: 'PkPage', params: { matchId: match.id } });
        }
      }).catch(() => {});
    },
    changeWebLanguage() {
      this.$store.commit('changeWebLanguage', { language: this.webLanguage == 'zh-CN' ? 'en-US' : 'zh-CN' });
    },
    setHiddenHeaderHeight(){
      if(!this.mobileNar){
        try {
          let headerHeight = document.getElementById('header').offsetHeight;
          document.getElementById('header-hidden').setAttribute('style','height:'+ headerHeight + 'px')
        } catch (e) {}
      }
    },
    switchMode(){
      if(this.$route.meta.fullScreenSource){
        this.mode = this.$route.meta.fullScreenSource;
      }else{
        this.mode = 'defalut';
      }
    },
    getTrainingHomePath(){
      let tid = this.$route.params.trainingID
      let gid = this.$route.params.groupID
      if(gid){
        return `/group/${gid}/training/${tid}`;
      }else{
        return `/training/${tid}`;
      }
    },
    getTrainingProblemListPath(){
      let tid = this.$route.params.trainingID
      let gid = this.$route.params.groupID
      if(gid){
        return `/group/${gid}/training/${tid}/problems`;
      }else{
        return `/training/${tid}/problems`;
      }
    }
  },
  computed: {
    ...mapGetters([
      'modalStatus',
      'userInfo',
      'isAuthenticated',
      'isAdminRole',
      'token',
      'websiteConfig',
      'unreadMessage',
      'webLanguage',
      'unfinishedBadgeCount',
      'unfinishedFlash',
    ]),
    avatar() {
      return this.$store.getters.userInfo.avatar;
    },
    hasUnreadMessage() {
      const m = this.unreadMessage || {};
      return (
        m.comment > 0 ||
        m.reply > 0 ||
        m.like > 0 ||
        m.sys > 0 ||
        m.mine > 0
      );
    },
    hasUnreadChat() {
      return (this.unreadMessage && this.unreadMessage.chat > 0) || false;
    },
    hasUnfinishedAssignment() {
      return this.unfinishedFlash;
    },
    activeMenuName() {
      if (this.$route.path.split('/')[1] == 'submission-detail') {
        return '/status';
      } else if (this.$route.path.split('/')[1] == 'discussion-detail') {
        return '/discussion';
      } else if (this.$route.path.split('/')[1] == 'chat') {
        return '/chat';
      }
      return '/' + this.$route.path.split('/')[1];
    },
    filteredPrimary() {
      return this.primaryNav.filter((item) => {
        if (item.condition === 'openPublicDiscussion') {
          return this.websiteConfig.openPublicDiscussion;
        }
        return true;
      });
    },
    visiblePrimary() {
      return this.filteredPrimary.slice(0, this.visibleCount);
    },
    overflowPrimary() {
      return this.filteredPrimary.slice(this.visibleCount);
    },
    assignmentParentOverflowed() {
      return this.overflowPrimary.some((item) => item.key === 'practice');
    },
    modalVisible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
    title: {
      get() {
        let ojName = this.websiteConfig.shortName
          ? this.websiteConfig.shortName
          : 'OJ';
        if (this.modalStatus.mode == 'ResetPwd') {
          return this.$i18n.t('m.Dialog_Reset_Password') + ' - ' + ojName;
        } else {
          return (
            this.$i18n.t('m.Dialog_' + this.modalStatus.mode) + ' - ' + ojName
          );
        }
      },
    },
  },
  watch: {
    isAuthenticated() {
      // 登录态变化会切换右侧区域（登录按钮/用户下拉），需要重新计算可用宽度
      this.$nextTick(() => this.recomputeNavOverflow());
      if (this.isAuthenticated) {
        if (this.msgTimer) {
          clearInterval(this.msgTimer);
        }
        this.getUnreadMsgCount();
        this.msgTimer = setInterval(() => {
          this.getUnreadMsgCount();
        }, 120 * 1000);
        // 如果页面内容已经加载完，立即启动PK轮询
        if (this.$store.state.user.contentReady) {
          this.startPkPolling();
        }
        this.startAssignmentPolling();
      } else {
        clearInterval(this.msgTimer);
        clearInterval(this.pkTimer);
        this.pkTimer = null;
        clearInterval(this.assignmentTimer);
        this.assignmentTimer = null;
      }
    },
    '$store.state.user.contentReady'(val) {
      if (val && this.isAuthenticated) {
        clearTimeout(this.fallbackTimer);
        this.startPkPolling();
      }
    },
    webLanguage() {
      // 语言切换会改变菜单文案宽度，需要重新测量
      this.remeasureNavOverflow();
    },
    'websiteConfig.openPublicDiscussion'() {
      // 讨论区开关变化会影响菜单项数量，需要重新测量
      this.remeasureNavOverflow();
    },
    $route(to, from){
      this.switchMode();
      // 离开私聊页面时刷新本地未读计数（后端在读消息时已置为已读），否则未读残留会一直闪动
      if (from && from.path && from.path.split('/')[1] === 'chat' && to.path.split('/')[1] !== 'chat') {
        this.refreshChatUnread();
      }
      // 路由切换时重置 contentReady，停止PK轮询，等新页面加载完再开
      this.$store.commit('setContentReady', false);
      clearInterval(this.pkTimer);
      this.pkTimer = null;
      // 重新设置兜底定时器
      clearTimeout(this.fallbackTimer);
      this.fallbackTimer = setTimeout(() => {
        if (!this.$store.state.user.contentReady) {
          this.$store.commit('setContentReady', true);
        }
      }, 5000);
    }
  },
};
</script>
<style scoped>
#header {
  min-width: 300px;
  position: fixed;
  top: 0;
  left: 0;
  height: 61px;
  overflow: hidden;
  width: 100%;
  z-index: 2000;
  background-color: #fff;
  box-shadow: 0 1px 5px 0 rgba(0, 0, 0, 0.1);
}
.mobile-nav {
  position: fixed;
  left: 0px;
  top: 0px;
  z-index: 2500;
  height: auto;
  width: 100%;
}

#drawer {
  position: fixed;
  left: 0px;
  bottom: 0px;
  z-index: 1000;
  width: 100%;
  box-shadow: 00px 0px 00px rgb(255, 255, 255), 0px 0px 10px rgb(255, 255, 255),
    0px 0px 0px rgb(255, 255, 255), 1px 1px 0px rgb(218, 218, 218);
}

.logo {
  cursor: pointer;
  margin-left: 2%;
  margin-right: 2%;
  float: left;
  width: 139px;
  height: 42px;
  margin-top: 5px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409eff !important;
}
.el-icon-arrow-down {
  font-size: 18px;
}
.drop-menu {
  float: right;
  margin-right: 18px;
  position: relative;
  font-weight: 500;
  right: 10px;
  margin-top: 18px;
  font-size: 18px;
}
.drop-avatar {
  float: right;
  margin-right: 15px;
  position: relative;
  margin-top: 16px;
}
.drop-msg {
  float: right;
  font-size: 25px;
  height: 30px;
  line-height: 30px;
  margin-right: 15px;
  position: relative;
  margin-top: 15px;
}
.drop-msg-count {
  margin-left: 2px;
}
/* 有新消息时，消息图标低频闪烁 */
@keyframes navMsgFlash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.25; }
}
.nav-msg-flash {
  animation: navMsgFlash 2.4s ease-in-out infinite;
}
/* 有必做未完成作业时，导航栏「练习/作业」低频闪烁 */
.nav-assignment-flash {
  animation: navMsgFlash 2.4s ease-in-out infinite;
}
/* 「练习」按钮的未完成角标（模仿私聊角标的红底白字圆角） */
.nav-assignment-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: red;
  color: #fff;
  font-size: 12px;
  text-align: center;
  margin-left: 4px;
  vertical-align: 1px;
  box-sizing: border-box;
}
/* 右侧私聊图标 */
.nav-chat {
  float: right;
  font-size: 25px;
  height: 30px;
  line-height: 30px;
  margin-left: 8px;
  margin-right: 10px;
  position: relative;
  margin-top: 15px;
  cursor: pointer;
  color: #495060;
}
.nav-chat:hover {
  color: #2E95FB;
}
.nav-chat-badge {
  position: absolute;
  top: -6px;
  right: -16px;
}
/* 右侧题号搜索框 */
.nav-search {
  float: right;
  width: 180px;
  margin-top: 16px;
  margin-right: 10px;
}
.btn-menu {
  font-size: 16px;
  float: right;
  margin-right: 6px;
  margin-top: 12px;
}
/deep/ .el-dialog {
  border-radius: 10px !important;
  text-align: center;
}
/deep/ .el-dialog__header .el-dialog__title {
  font-size: 22px;
  font-weight: 600;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1em;
  color: #4e4e4e;
}
.el-submenu__title i {
  color: #495060 !important;
}
.el-menu-item {
  padding: 0 13px;
}
.el-menu-item:hover, .el-menu .el-menu-item:hover{
  border-bottom: 2px solid #2474b5 !important;
}
.el-menu .el-menu-item:hover, 
.el-menu .el-menu-item:hover i,
.el-submenu .el-submenu__title:hover,
.el-submenu .el-submenu__title:hover i{
  outline: 0 !important;
  color: #2E95FB !important;
  background: linear-gradient(270deg, #F2F7FC 0%, #FEFEFE 100%)!important;
  transition: all .2s ease;
}
.el-menu .el-menu-item.is-active, 
.el-menu .el-menu-item.is-active i,
.el-submenu.is-active,
.el-submenu.is-active i
{
  color: #2E95FB !important;
  background: linear-gradient(270deg, #F2F7FC 0%, #FEFEFE 100%)!important;
  transition: all .2s ease;
}
.el-menu--horizontal .el-menu .el-menu-item:hover, 
.el-submenu /deep/.el-submenu__title:hover {
  color: #2E95FB !important;
  background: linear-gradient(270deg, #F2F7FC 0%, #FEFEFE 100%)!important;
}
.el-menu-item i {
  color: #495060;
}
.is-active .el-submenu__title i,
.is-active {
  color: #2196f3 !important;
}
.el-menu-item.is-active i {
  color: #2196f3 !important;
}
.navbar-icon{
  margin-right: 5px !important;
  width: 24px !important;
  text-align: center !important;
}
/* 「团队」弃用项：划线 + 去掉悬停/选中变色反馈 */
.nav-deprecated {
  text-decoration: line-through;
}
.nav-deprecated .mu-list-item-title {
  text-decoration: line-through;
}
.el-menu .el-menu-item.nav-deprecated,
.el-menu .el-menu-item.nav-deprecated:hover,
.el-menu .el-menu-item.nav-deprecated.is-active {
  color: #909399 !important;
  background: transparent !important;
  background-color: transparent !important;
  border-bottom: 2px solid transparent !important;
}
</style>
