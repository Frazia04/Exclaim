import { faker } from '@faker-js/faker';

import type { Exercise, Exercise_GroupJoin, ExerciseWithDetails, StudentData, Term_SummerWinter } from '../../api';
import type { DB } from '.';
import type { DBUser } from './users';

type DBGroup = {
  groupId: string;
  tutors: Set<number>;
};

type DBStudent = StudentData;

export type DBExercise = Omit<Exercise, 'roles'> & {
  groups: Map<string, DBGroup>;
  students: Map<number, DBStudent>;
  assistants: Set<number>;
};

export function seed(users: Map<number, DBUser>): Map<string, DBExercise> {
  const validUserIds = [...users.keys()];
  faker.seed(0);
  const exercises = new Map<string, DBExercise>();

  // Describe courses that we have every year
  type YearlyCourse = {
    id: string;
    lecture: string;
    term: Term_SummerWinter;
    groupJoin: Exercise_GroupJoin;
    minGroups: number;
    maxGroups: number;
    minStudents: number;
    maxStudents: number;
  };
  const yearlyCourses: YearlyCourse[] = [
    {
      id: 'GdP',
      lecture: 'Grundlagen der Programmierung',
      term: 'winter',
      groupJoin: 'preferences',
      minGroups: 9,
      maxGroups: 14,
      minStudents: 120,
      maxStudents: 300,
    },
    {
      id: 'pp',
      lecture: 'Programmierpraktikum',
      term: 'summer',
      groupJoin: 'group',
      minGroups: 5,
      maxGroups: 5,
      minStudents: 100,
      maxStudents: 150,
    },
    {
      id: 'PinC',
      lecture: 'Programmieren in C',
      term: 'winter',
      groupJoin: 'none',
      minGroups: 1,
      maxGroups: 1,
      minStudents: 40,
      maxStudents: 80,
    },
  ];

  // Years for which to generate those courses
  const yearMax = new Date().getFullYear();
  const yearMin = yearMax - 20;

  // Generate yearly courses
  for (let year = yearMin; year <= yearMax; year++) {
    const yearShort = year % 100;
    const yearSuffix = yearShort < 10 ? `0${yearShort}` : String(yearShort);
    for (const { id, lecture, term, groupJoin, minGroups, maxGroups, minStudents, maxStudents } of yearlyCourses) {
      const exerciseId = id + yearSuffix;

      // Generate groups
      const numGroups = faker.number.int({ min: minGroups, max: maxGroups });
      const groups = new Map<string, DBGroup>();
      for (let i = 1; i <= numGroups; i++) {
        const groupId = `G${i}`;
        groups.set(groupId, {
          groupId,
          // Add 0 to 2 random tutors
          tutors: new Set(faker.helpers.arrayElements(validUserIds, { min: 0, max: 2 })),
        });
      }

      // Generate students
      const students = new Map<number, DBStudent>();
      for (const userId of faker.helpers.arrayElements([...users.keys()], { min: minStudents, max: maxStudents })) {
        // Pick a random group or no group
        const groupId = faker.helpers.arrayElement([null, ...groups.keys()]);
        students.set(userId, {
          groupId,
          // If in a group, pick a random team
          teamId:
            groupId === null ? null : faker.helpers.arrayElement([null, '1', '2', '3', '4', '5', '6', '7', '8', '9']),
        });
      }

      exercises.set(exerciseId, {
        exerciseId,
        lecture,
        term: {
          year,
          term,
          comment: '',
        },
        registrationOpen: year === yearMax,
        groupJoin: year === yearMax ? groupJoin : 'none',
        groups,
        students,
        assistants: new Set([1, ...faker.helpers.arrayElements(validUserIds, { min: 0, max: 5 })]),
      });
    }
  }

  return exercises;
}

function toExercise(db: DB, exercise: DBExercise): Exercise {
  const { exerciseId, lecture, term, registrationOpen, groupJoin, groups, students, assistants } = exercise;
  return {
    exerciseId,
    lecture,
    term,
    registrationOpen,
    groupJoin,
    roles: {
      student: students.get(db.userId!) ?? null,
      tutorGroups: [...groups.values()].filter(({ tutors }) => tutors.has(db.userId!)).map((g) => g.groupId),
      assistant: assistants.has(db.userId!),
    },
  };
}

function toExerciseWithDetails(db: DB, exercise: DBExercise): ExerciseWithDetails {
  return {
    ...toExercise(db, exercise),
    groups: [...exercise.groups.values()].map(({ groupId, tutors }) => ({
      groupId,
      tutors: [...tutors.values()].map((userId) => {
        const { firstname, lastname, email } = db.users.get(userId)!;
        return { userId, firstname, lastname, email };
      }),
    })),
  };
}

export function fetchExercises(db: DB): Exercise[] {
  return [...db.exercises.values()].map((exercise) => toExercise(db, exercise));
}

export function fetchExercise(db: DB, exerciseId: string): ExerciseWithDetails | null {
  const exercise = db.exercises.get(exerciseId);
  return exercise ? toExerciseWithDetails(db, exercise) : null;
}

export function joinExercise(db: DB, exerciseId: string): ExerciseWithDetails {
  const exercise = db.exercises.get(exerciseId);
  if (exercise) {
    if (exercise.students.has(db.userId!)) {
      // already joined
    } else if (exercise.registrationOpen) {
      exercise.students.set(db.userId!, { groupId: null, teamId: null });
    } else {
      throw new Error('Cannot join exercise');
    }
    return toExerciseWithDetails(db, exercise);
  } else {
    throw new Error('Invalid exerciseId');
  }
}
