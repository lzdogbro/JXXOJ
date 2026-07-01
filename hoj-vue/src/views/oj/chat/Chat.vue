<template>
  <div class="chat-container">
    <div v-if="!mobileView || (mobileView && !activeContact)" class="chat-contact-panel">
      <div class="chat-contact-header">
        <span>{{ $t('m.Chat') }}</span>
      </div>
      <div class="chat-contact-list" v-if="contacts.length > 0">
        <div
          v-for="contact in contacts"
          :key="contact.uid"
          :class="['chat-contact-item', { active: activeContact && activeContact.uid === contact.uid }]"
          @click="selectContact(contact)"
        >
          <div class="contact-avatar">
            <avatar :username="contact.nickname || contact.username" :inline="true" :size="40" color="#FFF" :src="contact.avatar"></avatar>
          </div>
          <div class="contact-info">
            <div class="contact-name">
              <span class="contact-username">{{ contact.nickname || contact.username }}</span>
              <span class="contact-time">{{ formatTime(contact.lastTime) }}</span>
            </div>
            <div class="contact-preview">
              <span class="preview-text">{{ contact.lastContent || '' | truncate(30) }}</span>
              <span v-if="contact.unreadCount > 0" class="unread-badge">{{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="chat-empty-contacts">
        <i class="el-icon-chat-round" style="font-size: 40px; color: #c0c4cc;"></i>
        <p>{{ $t('m.Chat_No_Contacts') }}</p>
        <p style="font-size: 12px; color: #999;">{{ $t('m.Chat_Start_Conversation') }}</p>
      </div>
    </div>

    <div v-if="!mobileView || (mobileView && activeContact)" class="chat-message-panel">
      <template v-if="activeContact">
        <div class="chat-message-header">
          <i v-if="mobileView" class="el-icon-arrow-left" style="font-size: 18px; margin-right: 10px; cursor: pointer;" @click="backToContacts"></i>
          <avatar :username="activeContact.nickname || activeContact.username" :inline="true" :size="36" color="#FFF" :src="activeContact.avatar"></avatar>
          <span class="header-username">{{ activeContact.nickname || activeContact.username }}</span>
        </div>
        <div class="chat-message-body" ref="messageBody" @scroll.passive="onScroll">
          <div v-if="messages.length === 0 && !loadingMsg" class="chat-empty-msg">
            <p>{{ $t('m.Chat_No_Messages') }}</p>
          </div>
          <div v-if="hasMore && !loadingMsg" style="text-align: center; padding: 5px;">
            <el-button type="text" @click="loadMore" :loading="loadingMore">{{ $t('m.Load_More') }}</el-button>
          </div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['chat-message-item', { 'chat-message-self': msg.senderId === userInfo.uid }]"
          >
            <div class="msg-avatar" v-if="msg.senderId !== userInfo.uid">
              <avatar :username="msg.senderNickname || msg.senderUsername" :inline="true" :size="30" color="#FFF" :src="msg.senderAvatar"></avatar>
            </div>
            <div :class="['msg-content-wrapper', { 'msg-self': msg.senderId === userInfo.uid }]">
              <div :class="['msg-bubble', { 'bubble-self': msg.senderId === userInfo.uid, 'bubble-other': msg.senderId !== userInfo.uid }]">
                {{ msg.content }}
              </div>
              <div :class="['msg-time', { 'time-self': msg.senderId === userInfo.uid }]">
                {{ formatTime(msg.gmtCreate) }}
              </div>
            </div>
            <div class="msg-avatar" v-if="msg.senderId === userInfo.uid">
              <avatar :username="msg.senderNickname || msg.senderUsername" :inline="true" :size="30" color="#FFF" :src="msg.senderAvatar"></avatar>
            </div>
          </div>
          <div v-if="loadingMsg" style="text-align: center; padding: 20px;">
            <i class="el-icon-loading"></i>
          </div>
        </div>
        <div class="chat-message-footer">
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="2"
            :placeholder="$t('m.Chat_Placeholder')"
            resize="none"
            @keydown.native="handleKeydown"
          ></el-input>
          <el-button type="primary" size="small" @click="sendMessage" :loading="sending" style="margin-left: 10px;">{{ $t('m.Chat_Send') }}</el-button>
        </div>
      </template>
      <div v-else class="chat-no-contact">
        <i class="el-icon-chat-dot-round" style="font-size: 60px; color: #c0c4cc;"></i>
        <p>{{ $t('m.Chat_Select_Contact') }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import Avatar from 'vue-avatar';
import api from '@/common/api';
import { mapGetters } from 'vuex';
import moment from 'moment';

export default {
  components: {
    Avatar
  },
  data() {
    return {
      contacts: [],
      activeContact: null,
      messages: [],
      inputMsg: '',
      sending: false,
      loadingMsg: false,
      loadingMore: false,
      currentPage: 1,
      totalPages: 0,
      hasMore: false,
      mobileView: false,
      chatTimer: null,
      refreshingMsg: false,
    };
  },
  created() {
    this.page_width();
    window.onresize = () => {
      this.page_width();
    };
  },
  mounted() {
    if (this.isAuthenticated) {
      this.loadContacts().then(() => {
        this.checkRouteContact();
      });
      this.chatTimer = setInterval(() => {
        this.loadContacts();
        if (this.activeContact) {
          this.refreshMessages();
        }
      }, 60 * 1000);
    }
  },
  beforeDestroy() {
    clearInterval(this.chatTimer);
  },
  methods: {
    page_width() {
      this.mobileView = window.screen.width < 768;
    },
    loadContacts() {
      return api.getChatContacts().then((res) => {
        this.contacts = res.data.data || [];
      }).catch(() => {});
    },
    selectContact(contact) {
      this.activeContact = contact;
      this.messages = [];
      this.currentPage = 1;
      this.inputMsg = '';
      contact.unreadCount = 0;
      this.loadMessages();
    },
    checkRouteContact() {
      let targetUid = this.$route.query.uid;
      if (!targetUid) return;
      // 检查联系人列表中是否已有该用户
      let existing = this.contacts.find(c => c.uid === targetUid);
      if (existing) {
        this.selectContact(existing);
      } else {
        // 没有历史对话，创建一个虚拟联系人条目以便发送第一条消息
        let newContact = {
          uid: targetUid,
          username: this.$route.query.username || '',
          nickname: this.$route.query.nickname || '',
          avatar: this.$route.query.avatar || '',
          lastContent: '',
          lastTime: null,
          unreadCount: 0
        };
        this.contacts.unshift(newContact);
        this.selectContact(newContact);
      }
    },
    loadMessages() {
      if (!this.activeContact) return;
      this.loadingMsg = true;
      api.getChatMessages(this.activeContact.uid, 20, this.currentPage).then((res) => {
        let data = res.data.data;
        let newMsgs = data.records || [];
        newMsgs.reverse();
        if (this.currentPage === 1) {
          this.messages = newMsgs;
        } else {
          this.messages = newMsgs.concat(this.messages);
        }
        this.totalPages = Math.ceil(data.total / 20);
        this.hasMore = this.currentPage < this.totalPages;
        this.loadingMsg = false;
        this.loadingMore = false;
        if (this.currentPage === 1) {
          this.$nextTick(() => {
            this.scrollToBottom(true);
          });
        }
      }).catch(() => {
        this.loadingMsg = false;
        this.loadingMore = false;
      });
    },
    refreshMessages() {
      if (!this.activeContact || this.refreshingMsg) return;
      this.refreshingMsg = true;
      let lastId = this.messages.length > 0 ? this.messages[this.messages.length - 1].id : 0;
      api.getChatMessages(this.activeContact.uid, 5, 1).then((res) => {
        let data = res.data.data;
        let newMsgs = (data.records || []).reverse();
        let freshMsgs = newMsgs.filter(m => m.id > lastId);
        if (freshMsgs.length > 0) {
          this.messages.push(...freshMsgs);
          this.$nextTick(() => {
            this.scrollToBottom();
          });
        }
        this.refreshingMsg = false;
      }).catch(() => {
        this.refreshingMsg = false;
      });
    },
    loadMore() {
      if (this.currentPage >= this.totalPages) return;
      this.currentPage++;
      this.loadingMore = true;
      this.loadMessages();
    },
    sendMessage() {
      let content = this.inputMsg.trim();
      if (!content || !this.activeContact || this.sending) return;

      this.sending = true;
      let data = {
        recipientId: this.activeContact.uid,
        content: content
      };
      api.sendChatMessage(data).then(() => {
        this.inputMsg = '';
        this.sending = false;
        this.refreshMessages();
        this.loadContacts();
      }).catch(() => {
        this.sending = false;
      });
    },
    handleKeydown(e) {
      if (e.ctrlKey && e.keyCode === 13) {
        this.sendMessage();
      }
    },
    backToContacts() {
      this.activeContact = null;
    },
    scrollToBottom(instant) {
      let el = this.$refs.messageBody;
      if (!el) return;
      if (instant) {
        el.scrollTop = el.scrollHeight;
      } else {
        this.$nextTick(() => {
          el.scrollTop = el.scrollHeight;
        });
      }
    },
    onScroll() {
      let el = this.$refs.messageBody;
      if (!el) return;
      if (el.scrollTop <= 5 && this.hasMore && !this.loadingMore) {
        this.loadMore();
      }
    },
    formatTime(time) {
      if (!time) return '';
      let m = moment(time);
      let now = moment();
      if (m.isSame(now, 'day')) {
        return m.format('HH:mm');
      } else if (m.isSame(now, 'year')) {
        return m.format('MM-DD HH:mm');
      } else {
        return m.format('YYYY-MM-DD HH:mm');
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo', 'isAuthenticated', 'webLanguage']),
  },
  watch: {
    '$route.query.uid'(newUid, oldUid) {
      if (newUid && newUid !== oldUid) {
        this.checkRouteContact();
      }
    }
  },
  filters: {
    truncate(text, len) {
      if (!text) return '';
      if (text.length > len) {
        return text.substring(0, len) + '...';
      }
      return text;
    }
  }
};
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 80px);
  margin: 20px auto;
  max-width: 1000px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* Contact Panel */
.chat-contact-panel {
  width: 300px;
  min-width: 300px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.chat-contact-header {
  padding: 16px 20px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}
.chat-contact-list {
  flex: 1;
  overflow-y: auto;
}
.chat-contact-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.chat-contact-item:hover,
.chat-contact-item.active {
  background-color: #f5f7fa;
}
.contact-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}
.contact-info {
  flex: 1;
  min-width: 0;
}
.contact-name {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}
.contact-username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.contact-time {
  font-size: 11px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 8px;
}
.contact-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.preview-text {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.unread-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
  margin-left: 8px;
  flex-shrink: 0;
}
.chat-empty-contacts {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

/* Message Panel */
.chat-message-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-message-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}
.header-username {
  font-size: 16px;
  font-weight: 500;
  margin-left: 10px;
  color: #303133;
}
.chat-message-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}
.chat-message-item {
  display: flex;
  margin-bottom: 16px;
}
.chat-message-self {
  justify-content: flex-end;
}
.msg-avatar {
  flex-shrink: 0;
  margin: 0 8px;
}
.msg-content-wrapper {
  max-width: 65%;
}
.msg-self {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.bubble-other {
  background: #fff;
  border: 1px solid #ebeef5;
}
.bubble-self {
  background: #409eff;
  color: #fff;
}
.msg-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}
.time-self {
  text-align: right;
}

/* Footer */
.chat-message-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
}
.chat-no-contact {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}
.chat-empty-msg {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
}

/* Mobile */
@media screen and (max-width: 768px) {
  .chat-container {
    margin: 0;
    height: calc(100vh - 56px);
    border-radius: 0;
    box-shadow: none;
  }
  .chat-contact-panel {
    width: 100%;
    min-width: 100%;
  }
}
</style>
