import { expectStatus, extractJson, postJson } from './fetch';
import type { AddAssisstantRequest, Lecture } from './types';

export function createLecture(data: Lecture): Promise<Lecture> {
  return postJson<Lecture>('api/lectures/create', data)
    .then(expectStatus(200, true))
    .then(extractJson<Lecture>);
}

export function updateLecture(lectureId: string, lecture: Lecture): Promise<void> {
  return postJson(`api/lectures/${encodeURIComponent(lectureId)}/edit`, lecture)
    .then(expectStatus(200, true))
    .then(() => {});
}

export function removeAssistant(lectureId: string, userId: number): Promise<void> {
  return postJson(`api/lectures/${encodeURIComponent(lectureId)}/assistants/${encodeURIComponent(userId)}/remove`, {
    userId,
  })
    .then(expectStatus(200, true))
    .then(() => {});
}

export function addAssisstant(lectureId: string, username: AddAssisstantRequest): Promise<void> {
  return postJson(`api/lectures/${encodeURIComponent(lectureId)}/assistants/add`, username)
    .then(expectStatus(200, true))
    .then(() => {});
}
