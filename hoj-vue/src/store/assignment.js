import api from '@/common/api'

const state = {
  assignmentList: [],
  currentAssignment: {},
  assignmentProblemList: [],
  unfinishedBadgeCount: 0,
  unfinishedFlash: false,
}

const getters = {
  unfinishedBadgeCount: (state) => state.unfinishedBadgeCount,
  unfinishedFlash: (state) => state.unfinishedFlash,
}

const mutations = {
  changeAssignmentList (state, payload) {
    state.assignmentList = payload.assignmentList
  },
  changeCurrentAssignment (state, payload) {
    state.currentAssignment = payload.currentAssignment
  },
  changeAssignmentProblemList (state, payload) {
    state.assignmentProblemList = payload.assignmentProblemList
  },
  changeUnfinished (state, payload) {
    state.unfinishedBadgeCount = payload.badgeCount
    state.unfinishedFlash = payload.flash
  },
  clearAssignment (state) {
    state.assignmentList = []
    state.currentAssignment = {}
    state.assignmentProblemList = []
    state.unfinishedBadgeCount = 0
    state.unfinishedFlash = false
  }
}

const actions = {
  getAssignmentList ({ commit }, { currentPage, limit }) {
    return new Promise((resolve, reject) => {
      api.getAssignmentList(currentPage, limit).then((res) => {
        commit('changeAssignmentList', { assignmentList: res.data.data.records })
        resolve(res)
      }, (err) => {
        commit('changeAssignmentList', { assignmentList: [] })
        reject(err)
      })
    })
  },
  getAssignmentDetail ({ commit }, aid) {
    return new Promise((resolve, reject) => {
      api.getAssignmentDetail(aid).then((res) => {
        let data = res.data.data
        commit('changeCurrentAssignment', { currentAssignment: data.assignment })
        commit('changeAssignmentProblemList', { assignmentProblemList: data.problemList })
        resolve(res)
      }, (err) => {
        reject(err)
      })
    })
  },
  getAssignmentProblemList ({ commit }, aid) {
    return new Promise((resolve, reject) => {
      api.getAssignmentProblemList(aid).then((res) => {
        commit('changeAssignmentProblemList', { assignmentProblemList: res.data.data })
        resolve(res)
      }, (err) => {
        commit('changeAssignmentProblemList', { assignmentProblemList: [] })
        reject(err)
      })
    })
  },
  getAssignmentUnfinishedCount ({ commit }) {
    return new Promise((resolve, reject) => {
      api.getAssignmentUnfinishedCount().then((res) => {
        const data = res.data.data || {}
        commit('changeUnfinished', {
          badgeCount: data.badgeCount || 0,
          flash: !!data.flash
        })
        resolve(res)
      }, (err) => {
        commit('changeUnfinished', { badgeCount: 0, flash: false })
        reject(err)
      })
    })
  }
}

export default {
  state,
  mutations,
  getters,
  actions
}
