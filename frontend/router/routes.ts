// Define the routes for our single page application.

// For lazily loaded routes, we perform the dynamic import(...) in separate files in the lazy
// directory such that our chunk splitting strategy defined in vite.config.ts can put related view
// components and their dependencies into the same chunk.

import { type RouteRecordRaw } from 'vue-router';

import LoginView from '../components/views/login/LoginView.vue';
import { AddAssistantsView, CreateNewLectureView, ManageCoursesView } from './lazy/admin';
import {
  ExerciseGroupCreate,
  ExerciseGroupEdit,
  ExerciseGroupManagment,
  ExerciseGroupShow,
  ExerciseGroupTutor,
  ExerciseResult,
  ExerciseSheetAdd,
  ExerciseSheetAssessment,
  ExercisesheetAssignmentsCreate,
  ExercisesheetAssignmentsEdit,
  ExerciseSheetEdit,
  ExerciseSheetOverview,
  ExerciseView,
  HomeView,
  JoinCourseView,
  SettingsView,
  UserAdminView,
  UserDetailsView,
} from './lazy/student';
import {
  addAssistantsRouteName,
  createExerciseSheetRouteName,
  createNewLectureRouteName,
  editLectureRouteName,
  exerciseGroupCreateRouteName,
  exerciseGroupEditRouteName,
  exerciseGroupManagmentRouteName,
  exerciseGroupsRouteName,
  exerciseGroupTutorRouteName,
  exerciseResultRouteName,
  exerciseRouteName,
  exerciseSheetAssessmentRouteName,
  exercisesheetAssignmentsCreateRouteName,
  exercisesheetAssignmentsEditRouteName,
  exerciseSheetEditRouteName,
  exerciseSheetOverviewRouteName,
  homeRouteName,
  joinCourseRouteName,
  loginRouteName,
  manageCourseRouteName,
  settingsRouteName,
  userAdminRouteName,
  userDetailsRouteName,
} from './names';

export function buildRoutes(): RouteRecordRaw[] {
  return [
    {
      path: '/login',
      name: loginRouteName,
      component: LoginView,
    },
    {
      path: '/',
      name: homeRouteName,
      component: HomeView,
    },
    {
      path: '/join',
      name: joinCourseRouteName,
      component: JoinCourseView,
    },
    {
      path: '/lectures',
      name: manageCourseRouteName,
      component: ManageCoursesView,
    },
    {
      path: '/lectures/create',
      name: createNewLectureRouteName,
      component: CreateNewLectureView,
    },
    {
      path: '/lectures/edit/:lectureId',
      name: editLectureRouteName,
      component: CreateNewLectureView,
      props: true,
    },
    {
      path: '/settings',
      name: settingsRouteName,
      component: SettingsView,
    },
    {
      path: '/lectures/assistants/:lectureId',
      name: addAssistantsRouteName,
      component: AddAssistantsView,
      props: true,
    },
    {
      path: '/exercise/:exerciseId',
      name: exerciseRouteName,
      component: ExerciseView,
    },
    {
      path: '/exercise/:exerciseId/admin/sheets/create',
      name: createExerciseSheetRouteName,
      component: ExerciseSheetAdd,
    },
    {
      path: '/exercise/:exerciseId/groups',
      name: exerciseGroupsRouteName,
      component: ExerciseGroupShow,
    },
    {
      path: '/exercise/:exerciseId/results',
      name: exerciseResultRouteName,
      component: ExerciseResult,
    },
    {
      path: '/exercise/:exerciseId/admin/groups',
      name: exerciseGroupManagmentRouteName,
      component: ExerciseGroupManagment,
    },
    {
      path: '/exercise/:exerciseId/sheet/:sheetId/overview',
      name: exerciseSheetOverviewRouteName,
      component: ExerciseSheetOverview,
    },
    {
      path: '/exercise/:exerciseId/sheet/:sheetId/assessment',
      name: exerciseSheetAssessmentRouteName,
      component: ExerciseSheetAssessment,
    },
    {
      path: '/exercise/:exerciseId/sheet/:sheetId/edit',
      name: exerciseSheetEditRouteName,
      component: ExerciseSheetEdit,
    },
    {
      path: '/exercise/:exerciseId/admin/groups/create',
      name: exerciseGroupCreateRouteName,
      component: ExerciseGroupCreate,
    },
    {
      path: '/exercise/:exerciseId/admin/groups/:groupId/edit',
      name: exerciseGroupEditRouteName,
      component: ExerciseGroupEdit,
    },
    {
      path: '/exercise/:exerciseId/admin/groups/:groupId/tutors',
      name: exerciseGroupTutorRouteName,
      component: ExerciseGroupTutor,
    },
    {
      path: '/exercise/:exerciseId/admin/sheets/:sheetId/assignments/create',
      name: exercisesheetAssignmentsCreateRouteName,
      component: ExercisesheetAssignmentsCreate,
    },
    {
      path: '/exercise/:exerciseId/admin/sheets/:sheetId/assignments/:assignmentId/edit',
      name: exercisesheetAssignmentsEditRouteName,
      component: ExercisesheetAssignmentsEdit,
    },
    {
      path: '/useradmin',
      name: userAdminRouteName,
      component: UserAdminView,
    },
    {
      path: '/userinfo/:userid',
      name: userDetailsRouteName,
      component: UserDetailsView,
      props: true,
    },
  ];
}
