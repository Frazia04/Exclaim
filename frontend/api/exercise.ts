import { expectStatus, extractJson, get, post, postURLSearchParams } from './fetch';
import type {
  Assistant,
  Exercise,
  ExerciseGroupWithDetails,
  ExerciseResultDetails,
  ExerciseSheetAssignments,
  ExerciseSheetWithDetails,
  ExerciseWithDetails,
  ProcessResult,
} from './types';

export function getExercises(): Promise<Exercise[]> {
  return get('api/exercise')
    .then(expectStatus(200, true))
    .then(extractJson<Exercise[]>);
}

export function getExercise(exerciseId: string): Promise<ExerciseWithDetails | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId))
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseWithDetails | null>);
}

export async function deleteLecture(exerciseId: string): Promise<void> {
  await post('api/lectures/' + encodeURIComponent(exerciseId) + '/delete').then(expectStatus(200, true));
}

export function getAssistants(exerciseId: string): Promise<Assistant[]> {
  return get('api/lectures/' + encodeURIComponent(exerciseId) + '/assistants')
    .then(expectStatus(200, true))
    .then(extractJson<Assistant[]>);
}

export function getExerciseSheet(exerciseId: string): Promise<ExerciseSheetWithDetails[] | null> {
  return get('api/exerciseSheet/' + encodeURIComponent(exerciseId))
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseSheetWithDetails[] | null>);
}

export type CreateSheetForm = {
  sheetId: string;
  label: string;
};

export type EditSheetForm = {
  label: string;
};

export type CreateAssignmentForm = {
  assignmentId: string;
  label: string;
  maxPoints: string;
  showStatistics: string;
};

export type EditAssignmentForm = {
  label: string;
  maxPoints: string;
  showStatistics: string;
};

export async function submitExerciseSheet(
  exerciseId: string,
  sheetId: string,
  sheetLable: string,
): Promise<ProcessResult | null> {
  const sheetform: CreateSheetForm = { sheetId: sheetId, label: sheetLable };
  return await postURLSearchParams(
    'api/exerciseSheet/' + encodeURIComponent(exerciseId) + '/admin/sheets/create',
    sheetform,
  )
    .then(expectStatus([200, 401]))
    .then(extractJson<ProcessResult | null>);
}

export async function editExerciseSheet(
  exerciseId: string,
  sheetId: string,
  sheetLable: string,
): Promise<ProcessResult | null> {
  const sheetform: EditSheetForm = { label: sheetLable };
  return await postURLSearchParams(
    'api/exercise/' + encodeURIComponent(exerciseId) + '/sheets/' + encodeURIComponent(sheetId) + '/edit',
    sheetform,
  )
    .then(expectStatus([200, 401]))
    .then(extractJson<ProcessResult | null>);
}

export async function deleteExerciseSheet(exerciseId: string, sheetId: string): Promise<ProcessResult | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/sheets/' + encodeURIComponent(sheetId) + '/delete')
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export async function saveExerciseSheetAssignemnt(
  exerciseId: string,
  sheetId: string,
  assignmentId: string,
  assignmentDesignation: string,
  assignmentMaximumScore: string,
  assignmentShowStatistics: boolean,
): Promise<ProcessResult | null> {
  const assignemntform: CreateAssignmentForm = {
    assignmentId: assignmentId,
    label: assignmentDesignation,
    maxPoints: assignmentMaximumScore,
    showStatistics: assignmentShowStatistics.toString(),
  };

  return await postURLSearchParams(
    '/api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/sheets/' +
      encodeURIComponent(sheetId) +
      '/assignments/create',
    assignemntform,
  )
    .then(expectStatus([200, 401]))
    .then(extractJson<ProcessResult | null>);
}

export async function editExerciseSheetAssignemnt(
  exerciseId: string,
  sheetId: string,
  assignmentId: string,
  assignmentDesignation: string,
  assignmentMaximumScore: string,
  assignmentShowStatistics: boolean,
): Promise<ProcessResult | null> {
  const assignemntform: EditAssignmentForm = {
    label: assignmentDesignation,
    maxPoints: assignmentMaximumScore,
    showStatistics: assignmentShowStatistics.toString(),
  };

  return await postURLSearchParams(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/sheets/' +
      encodeURIComponent(sheetId) +
      '/assignments/' +
      encodeURIComponent(assignmentId) +
      '/edit',
    assignemntform,
  )
    .then(expectStatus([200, 401]))

    .then(extractJson<ProcessResult | null>);
}

export function getAssignmentWithDetails(
  exerciseId: string,
  sheetId: string,
  assignmentId: string,
): Promise<ExerciseSheetAssignments | null> {
  return get(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/sheets/' +
      encodeURIComponent(sheetId) +
      '/assignments/' +
      encodeURIComponent(assignmentId) +
      '/info',
  )
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseSheetAssignments | null>);
}

export async function deleteExerciseSheetAssignemnt(
  exerciseId: string,
  sheetId: string,
  assignmentId: string,
): Promise<ProcessResult | null> {
  return post(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/sheets/' +
      encodeURIComponent(sheetId) +
      '/assignments/' +
      encodeURIComponent(assignmentId) +
      '/delete',
  )
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export function getExerciseGroupDetailData(exerciseId: string): Promise<ExerciseGroupWithDetails[] | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/groups')
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseGroupWithDetails[] | null>);
}

export type CreateGroupForm = {
  groupId: string;
  day: string;
  time: string;
  location: string;
  maxSize: string;
};

export type EditGroupForm = {
  day: string;
  time: string;
  location: string;
  maxSize: string;
};

export async function submitExerciseGroup(
  exerciseId: string,
  groupId: string,
  day: string,
  time: string,
  location: string,
  maxSize: string,
): Promise<ProcessResult | null> {
  const sheetform: CreateGroupForm = { groupId: groupId, day: day, time: time, location: location, maxSize: maxSize };
  return await postURLSearchParams(
    '/api/exercise/' + encodeURIComponent(exerciseId) + '/admin/groups/create',
    sheetform,
  )
    .then(expectStatus([200, 401]))
    .then(extractJson<ProcessResult | null>);
}

export async function deleteExerciseGroup(exerciseId: string, groupId: string): Promise<ProcessResult | null> {
  return post('api/exercise/' + encodeURIComponent(exerciseId) + '/groups/' + encodeURIComponent(groupId) + '/delete')
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export async function editExerciseGroup(
  exerciseId: string,
  groupId: string,
  day: string,
  time: string,
  location: string,
  maxSize: string,
): Promise<ProcessResult | null> {
  const groupform: EditGroupForm = { day: day, time: time, location: location, maxSize: maxSize };
  return await postURLSearchParams(
    'api/exercise/' + encodeURIComponent(exerciseId) + '/groups/' + encodeURIComponent(groupId) + '/edit',
    groupform,
  )
    .then(expectStatus([200, 401]))
    .then(extractJson<ProcessResult | null>);
}

export function getExerciseGroupData(exerciseId: string, groupId: string): Promise<ExerciseGroupWithDetails | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/groups/' + encodeURIComponent(groupId) + '/info')
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseGroupWithDetails | null>);
}

export function editGroupRegistration(
  exerciseId: string,
  registrationOpen: boolean,
  groupJoin: string,
): Promise<ProcessResult | null> {
  return post(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/groupRegistrationEdit/' +
      encodeURIComponent(registrationOpen) +
      '/' +
      encodeURIComponent(groupJoin),
  )
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export async function getExerciseSheetFiles(exerciseId: string, sheetId: string): Promise<any[] | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/sheet/' + encodeURIComponent(sheetId) + '/zip')
    .then(expectStatus(200, true))
    .then(extractJson<any[] | null>);
}

export async function getExerciseSheetDetailsWithAssignments(
  exerciseId: string,
  sheetId: string,
): Promise<ExerciseSheetWithDetails | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/sheets/' + encodeURIComponent(sheetId) + '/info')
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseSheetWithDetails | null>);
}

export function removeTutor(exerciseId: string, groupId: string, userId: number): Promise<ProcessResult | null> {
  return post(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/groups/' +
      encodeURIComponent(groupId) +
      '/tutors/' +
      encodeURIComponent(userId) +
      '/delete',
  )
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export function addTutor(exerciseId: string, groupId: string, username: string): Promise<ProcessResult | null> {
  return post(
    'api/exercise/' +
      encodeURIComponent(exerciseId) +
      '/groups/' +
      encodeURIComponent(groupId) +
      '/tutors/' +
      encodeURIComponent(username) +
      '/add',
  )
    .then(expectStatus(200, true))
    .then(extractJson<ProcessResult | null>);
}

export function getTutors(exerciseId: string, groupId: string): Promise<Assistant[] | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/groups/' + encodeURIComponent(groupId) + '/tutors')
    .then(expectStatus(200, true))
    .then(extractJson<Assistant[] | null>);
}

export async function getExerciseResult(exerciseId: string): Promise<ExerciseResultDetails[] | null> {
  return get('api/exercise/' + encodeURIComponent(exerciseId) + '/results')
    .then(expectStatus(200, true))
    .then(extractJson<ExerciseResultDetails[] | null>);
}

export function groupJoin(exerciseId: string, groupId: string): Promise<void> {
  return post('api/exercise/' + encodeURIComponent(exerciseId) + '/groups/' + encodeURIComponent(groupId) + '/join')
    .then(expectStatus(200, true))
    .then(extractJson<void>);
}
