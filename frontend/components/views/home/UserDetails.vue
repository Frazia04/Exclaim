<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { deleteUser, getUsers, UpdateUserDetails, type UserAdminDetails } from '../../../api';
import { userAdminRouteName, userDetailsRouteName } from '../../../router/names';

const props = defineProps<{
  userid: number;
}>();

const userid = computed(() => Number(props.userid));
const users = ref<UserAdminDetails[]>([]);
const selectedUser = ref<UserAdminDetails | null>(null);
//const isEditable = ref(true);
const message = ref<string | null>(null);
const router = useRouter();

// Function to fetch users from the API
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const fetchUsers = async () => {
  try {
    users.value = await getUsers();
    console.log('Users: ', users.value);
    findUserById();
  } catch (error) {
    console.error('Error fetching users:', error);
  }
};

// Function to find a specific user based on userid
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const findUserById = () => {
  selectedUser.value = users.value.find((user) => user.userId === userid.value) || null;
  if (selectedUser.value) {
    console.log('selectedUser', selectedUser.value.username);
  }
};

// Watch for changes in userid prop and update the selected user
watch(
  () => props.userid,
  () => {
    findUserById();
  },
);

// Function to handle updating user details
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const updateUserDetails = async () => {
  if (selectedUser.value) {
    try {
      const verified = String(true);
      const admin = String(selectedUser.value.isAdmin);

      await UpdateUserDetails(
        selectedUser.value.userId,
        selectedUser.value.username,
        selectedUser.value.studentId,
        selectedUser.value.firstname,
        selectedUser.value.lastname,
        selectedUser.value.email,
        selectedUser.value.language,
        verified,
        admin,
      );
      message.value = 'User updated successfully!';
      void router.push({ name: userDetailsRouteName });
    } catch (error) {
      console.error('Error updating user details:', error);
      message.value = 'Failed to update user details.';
    }
  }
};

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const goBack = () => {
  router.back();
};

// Function to handle deleting a user
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const handleDelete = async () => {
  const confirmation = confirm(`Are you sure you want to delete the user with ID ${props.userid}?`);
  if (!confirmation) return;

  try {
    await deleteUser(props.userid); // Use the deleteUser function
    message.value = `User with ID ${props.userid} deleted successfully.`;
    void router.push({ name: userAdminRouteName });
  } catch (error) {
    console.error('Error deleting user:', error);
    message.value = `Failed to delete user with ID ${props.userid}.`;
  }
};

// Fetch users once the component is mounted
onMounted(fetchUsers);
</script>

<template>
  <div class="container mt-5">
    <main v-if="selectedUser" class="user-management">
      <h2 class="header">User Management</h2>
      <button @click="goBack">Back</button>

      <div class="form-group">
        <label for="userId">User ID</label>
        <input id="userId" v-model="selectedUser.userId" type="text" readonly />
      </div>

      <div class="form-group">
        <label for="username">Username</label>
        <input id="username" v-model="selectedUser.username" type="text" />
      </div>

      <div class="form-group">
        <label for="registrationId">Registration ID</label>
        <input id="registrationId" v-model="selectedUser.studentId" type="text" />
      </div>

      <div class="form-group">
        <label for="firstName">First Name</label>
        <input id="firstName" v-model="selectedUser.firstname" type="text" />
      </div>

      <div class="form-group">
        <label for="lastName">Last Name</label>
        <input id="lastName" v-model="selectedUser.lastname" type="text" />
      </div>

      <div class="form-group">
        <label for="email">Email</label>
        <input id="email" v-model="selectedUser.email" type="email" />
      </div>

      <div class="form-group">
        <label for="admin">Admin</label>
        <input id="admin" v-model="selectedUser.isAdmin" type="checkbox" />
      </div>

      <button @click="updateUserDetails">Update User</button>
      <button class="delete-button" @click="handleDelete">Delete User</button>

      <p v-if="message">{{ message }}</p>
    </main>
  </div>
</template>

<style scoped>
.header {
  text-align: center;
  margin-bottom: 20px;
}

.user-management {
  max-width: 600px;
  margin: auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

input[type='text'],
input[type='email'] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

input[type='checkbox'] {
  margin-right: 10px;
}

button {
  padding: 10px 20px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #0056b3;
}

.delete-button {
  background-color: #ff4d4f;
}

p {
  margin-top: 10px;
  color: green;
}
</style>
