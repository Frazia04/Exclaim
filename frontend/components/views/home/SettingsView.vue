<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { changePassword } from '../../../api';
import { t } from '../../../i18n';
import { accountData, authenticated } from '../../../store/account';

// Form data
const oldpassword = ref('');
const password = ref('');
const password2 = ref('');

const submit = async (): Promise<void> => {
  if (!password.value || !password2.value) {
    alert('New password fields cannot be empty.');
    return;
  }
  if (password.value !== password2.value) {
    alert('New passwords do not match. Please try again');
    return;
  }
  await handleChangePassword();
};

const handleChangePassword = async (): Promise<void> => {
  if (authenticated.value) {
    try {
      const responseMessage = await changePassword(oldpassword.value, password.value);
      alert(responseMessage);
    } catch (error) {
      alert('Incorrect Password');
    }
  }
};

onMounted(async () => {});
</script>

<template>
  <main>
    <h1 v-text="t('common.settings')" />
    <template v-if="accountData">
      <div>
        <div class="user-info">
          <table class="user-info">
            <tr>
              <th>{{ t('common.user-id') }}</th>
              <td>{{ accountData.userId }}</td>
            </tr>
            <tr>
              <th>{{ t('common.username') }}</th>
              <td>{{ accountData.username }}</td>
            </tr>
            <tr>
              <th>{{ t('common.student-id') }}</th>
              <td>{{ accountData.studentId }}</td>
            </tr>
            <tr>
              <th>{{ t('common.email') }}</th>
              <td>{{ accountData.email }}</td>
            </tr>
            <tr>
              <th>{{ t('common.first-name') }}</th>
              <td>{{ accountData.firstname }}</td>
            </tr>
            <tr>
              <th>{{ t('common.last-name') }}</th>
              <td>{{ accountData.lastname }}</td>
            </tr>
          </table>
        </div>

        <h2>{{ t('common.change-password') }}</h2>
      </div>
      <form @submit.prevent="submit">
        <div class="form-fields">
          <div class="form-group">
            <label for="oldpassword">{{ t('common.password') }}</label>
            <input id="oldpassword" v-model="oldpassword" type="password" class="form-input" />
          </div>
          <div class="form-group">
            <label for="password">{{ t('common.new-password') }}</label>
            <input id="password" v-model="password" type="password" class="form-input" />
          </div>
          <div class="form-group">
            <label for="password2">{{ t('common.confirm-password') }}</label>
            <input id="password2" v-model="password2" type="password" class="form-input" />
          </div>
        </div>
        <input type="submit" :value="t('common.update-password')" />
      </form>
    </template>
  </main>
</template>

<style scoped>
.settings {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
.user-info {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}
.user-info th,
.user-info td {
  border: 1px solid #ddd;
  padding: 8px;
}
.user-info th {
  background-color: #f2f2f2;
  text-align: left;
}
.form-input {
  width: 200px; /* Adjust the width as needed */
  box-sizing: border-box; /* Ensures padding and border are included in the element's total width */
}
.form-group {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.form-fields {
  display: flex;
  flex-direction: column;
  gap: 10px; /* Add space between the groups if needed */
}

.form-group {
  display: flex;
  align-items: center;
}

.form-group label {
  width: 150px; /* Adjust the label width as needed */
  margin-right: 10px; /* Add space between the label and input */
}

.form-input {
  width: 200px; /* Adjust the input width as needed */
  box-sizing: border-box; /* Ensures padding and border are included in the element's total width */
}
label {
  width: 120px;
  font-weight: bold;
}
span,
input,
select {
  flex: 1;
  padding: 5px;
}
.save-button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  cursor: pointer;
}
.save-button:hover {
  background-color: #0056b3;
}
</style>
