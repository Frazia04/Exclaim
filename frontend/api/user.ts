import { expectStatus, extractJson, get, post, postURLSearchParams } from './fetch';
import type { UserAdminDetails } from './types';
//import { encodeURIComponent } from './utils';

export type EditUserForm = {
  username: string;
  firstname: string;
  lastname: string;
  studentId: string;
  email: string;
  language: string;
  verified: string;
  admin: string;
};

export function getUsers(): Promise<UserAdminDetails[]> {
  return get('api/users')
    .then(expectStatus(200, true))
    .then(extractJson<UserAdminDetails[]>);
}

export async function UpdateUserDetails(
  userId: number,
  username: string,
  studentId: string,
  firstname: string,
  lastname: string,
  email: string,
  language: string,
  verified: string,
  admin: string,
): Promise<UserAdminDetails | null> {
  //const verified : boolean = stringToBoolean(verified1) ;
  // const admin :boo= stringToBoolean(admin1) ;

  const userform: EditUserForm = {
    username: username,
    studentId: studentId,
    firstname: firstname,
    lastname: lastname,
    email: email,
    language: language,
    verified: verified,
    admin: admin,
  };
  return await postURLSearchParams('api/users/' + encodeURIComponent(userId), userform)
    .then(expectStatus(200, true))
    .then(extractJson<UserAdminDetails>);
}

// export async function deleteUser(userId: number): Promise<UserAdminDetails | null> {
//   return fetch('api/user/' + encodeURIComponent(userId) + '/delete', {
//     method: 'DELETE' // Use DELETE method
//   })
//   .then(expectStatus(200, true))
//   .then(extractJson<UserAdminDetails | null>);
// }

// Function to delete a user
export async function deleteUser(userId: number): Promise<UserAdminDetails | null> {
  return await post(`api/users/${encodeURIComponent(userId)}/delete`)
    .then(expectStatus(200, true))
    .then(extractJson<UserAdminDetails | null>)
    .catch((error) => {
      console.error('Error deleting user:', error);
      throw error; // Propagate the error
    });
}
