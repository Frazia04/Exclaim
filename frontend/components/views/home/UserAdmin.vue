<template v-if="users.length">
  <div>
    <div class="container">
      <h1>User Adminstration</h1>

      <!-- Search input for filtering table data -->
      <input v-model="searchTerm" type="text" placeholder="Search..." class="search-input" />
    </div>
    <div>
      <button class="download-button" @click="downloadExcel">Download Excel</button>
      <!--  <button class="action-button" @click="copyTableData">Copy Table Data</button>-->
    </div>

    <div class="entries-controls">
      <label for="entries-per-page">Show Entries:</label>
      <select id="entries-per-page" v-model="entriesPerPage" @change="updatePagination">
        <option v-for="option in entriesOptions" :key="option" :value="option">
          {{ option }}
        </option>
      </select>
      <h6>Click on column name for sorting the table</h6>
    </div>

    <div class="styled-table1">
      <table ref="table" class="table">
        <thead>
          <tr>
            <th :class="getSortClass('User ID')" @click="sortTable('User ID')" v-text="t('user.Id')"></th>
            <th :class="getSortClass('Username')" @click="sortTable('Username')" v-text="t('user.user-name')"></th>
            <th :class="getSortClass('Student ID')" @click="sortTable('Student ID')" v-text="t('user.student-id')"></th>
            <th :class="getSortClass('First Name')" @click="sortTable('First Name')" v-text="t('user.first-name')"></th>
            <th :class="getSortClass('Last Name')" @click="sortTable('Last Name')" v-text="t('user.last-name')"></th>
            <th :class="getSortClass('Email')" @click="sortTable('Email')" v-text="t('user.email')"></th>
            <th :class="getSortClass('Language')" @click="sortTable('Language')" v-text="t('user.language')"></th>
            <th :class="getSortClass('Is Student')" @click="sortTable('Is Student')" v-text="t('user.is-student')"></th>
            <th :class="getSortClass('Is Tutor')" @click="sortTable('Is Tutor')" v-text="t('user.isTutor')"></th>
            <th
              :class="getSortClass('Is Assistant')"
              @click="sortTable('Is Assistant')"
              v-text="t('user.isAssistant')"
            ></th>
            <th :class="getSortClass('Is Admin')" @click="sortTable('Is Admin')" v-text="t('user.isAdmin')"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user1 in sortedUsers" :key="user1.userId">
            <td>
              <RouterLink :to="{ name: userDetailsRouteName, params: { userid: user1.userId } }">
                <td>
                  <span>{{ getHighlightedText(user1.userId) }}</span>
                </td>
                <!-- <span v-html="getHighlightedText(user1.userId)"></span> -->
              </RouterLink>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.username || '') }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.studentId || '') }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.firstname) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.lastname) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.email) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.language || '') }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.isStudent) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.isTutor) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.isAssistant) }}</span>
            </td>
            <td>
              <span>{{ getHighlightedText(user1.isAdmin) }}</span>
            </td>
            <!-- <td v-html="getHighlightedText(user1.username)"></td>
            <td v-html="getHighlightedText(user1.studentId)"></td>
            <td v-html="getHighlightedText(user1.firstname)"></td>
            <td v-html="getHighlightedText(user1.lastname)"></td>
            <td v-html="getHighlightedText(user1.email)"></td>
            <td v-html="getHighlightedText(user1.language)"></td>
            <td v-html="getHighlightedText(user1.isStudent)"></td>
            <td v-html="getHighlightedText(user1.isTutor)"></td>
            <td v-html="getHighlightedText(user1.isAssistant)"></td>
            <td v-html="getHighlightedText(user1.isAdmin)"></td> -->
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination Controls -->
    <div class="pagination-controls">
      <button :disabled="currentPage === 1" @click="previousPage">Previous</button>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
      <button :disabled="currentPage === totalPages" @click="nextPage">Next</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import * as XLSX from 'xlsx';

import { type UserAdminDetails } from '../../../api';
//import { RouterLink } from 'vue-router';
import { getUsers } from '../../../api';
import { t } from '../../../i18n';
import { userDetailsRouteName } from '../../../router/names';

const users = ref<UserAdminDetails[]>([]);

onMounted(async () => {
  await loadUsers();
});

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
async function loadUsers() {
  users.value = await getUsers();

  // // Process the data to set isStudent based on studentId
  // users.value = users.value.map(user => {
  //   user.isStudent = user.studentId !== null;
  //   return user;
  // });

  console.log('Users111: ', users);
}

const entriesOptions = [5, 10, 25, 50, 100];
const entriesPerPage = ref(entriesOptions[0]);
const currentPage = ref(1);
/////
const sortBy = ref<string | null>(null);
const sortOrder = ref<'asc' | 'desc'>('asc');

// Mapping between column display names and data field names
const columnMapping: { [key: string]: keyof UserAdminDetails } = {
  'User ID': 'userId',
  Username: 'username',
  'Student ID': 'studentId',
  'First Name': 'firstname',
  'Last Name': 'lastname',
  Email: 'email',
  Language: 'language',
  'Is Student': 'isStudent',
  'Is Tutor': 'isTutor',
  'Is Assistant': 'isAssistant',
  'Is Admin': 'isAdmin',
};

const filteredUsers1 = computed(() => {
  const term = searchTerm.value.toLowerCase();
  if (!term) return users.value;
  return users.value.filter((user) => Object.values(user).some((value) => String(value).toLowerCase().includes(term)));
});

const sortedUsers = computed(() => {
  const sorted = [...filteredUsers1.value];
  if (sortBy.value) {
    sorted.sort((a, b) => {
      const aValue = a[sortBy.value as keyof UserAdminDetails];
      const bValue = b[sortBy.value as keyof UserAdminDetails];
      // if (aValue >= bValue) {
      //   if (aValue > bValue) return sortOrder.value === 'asc' ? 1 : -1;
      //   return 0;
      // }

      // Handle null checks
      if (aValue == null && bValue == null) {
        return 0; // Both values are null, considered equal
      } else if (aValue == null) {
        return sortOrder.value === 'asc' ? -1 : 1; // Null is treated as smaller
      } else if (bValue == null) {
        return sortOrder.value === 'asc' ? 1 : -1; // Non-null is treated as greater
      } else {
        // Both values are guaranteed to be non-null
        if (aValue >= bValue) {
          if (aValue > bValue) return sortOrder.value === 'asc' ? 1 : -1;
          return 0; // Values are equal
        }
      }

      return sortOrder.value === 'asc' ? -1 : 1;
    });
  }
  return sorted.slice((currentPage.value - 1) * entriesPerPage.value, currentPage.value * entriesPerPage.value);
});

const totalPages = computed(() => Math.ceil(filteredUsers1.value.length / entriesPerPage.value));

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const sortTable = (displayColumn: string) => {
  const column = columnMapping[displayColumn];
  if (sortBy.value === column) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = column;
    sortOrder.value = 'asc';
  }
};

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const getSortClass = (displayColumn: string) => {
  const column = columnMapping[displayColumn];
  if (sortBy.value === column) {
    return sortOrder.value === 'asc' ? 'active-asc' : 'active-desc';
  }
  return '';
};

/////////////

// Update pagination when entries per page changes
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const updatePagination = () => {
  currentPage.value = 1; // Reset to the first page
};

// Function to go to the previous page
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value -= 1;
  }
};

// Function to go to the next page
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1;
  }
};

//Function to copy table data to clipboard
/// Function to escape and format CSV data
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type,@typescript-eslint/require-await
//const copyTableData = async () => {
// if (!navigator.clipboard) {
//   alert('Clipboard API not supported.');
//   return;
// }

// const table = document.getElementById('data-table');
// if (!table) return;

// let csv = '';

// // Get table headers
// const headers = table.querySelectorAll('thead th');
// csv += Array.from(headers).map(th => escapeCsvValue((th as HTMLElement).innerText)).join(',') + '\n';

// // Get table rows
// const rows = table.querySelectorAll('tbody tr');
// rows.forEach(row => {
//   const cells = row.querySelectorAll('td');
//   csv += Array.from(cells).map(td => escapeCsvValue((td as HTMLElement).innerText)).join(',') + '\n';
// });

// Copy to clipboard using Clipboard API

//alert('Table data copied to clipboard!');

// try {
//   await navigator.clipboard.writeText(csv);
//   alert('Table data copied to clipboard!');
// } catch (error) {
//   console.error('Failed to copy table data: ', error);
//   alert('Failed to copy table data.');
// }
//};

// Searching function

// Reactive variable for search term
const searchTerm = ref('');

// Function to highlight the search term in a given text
function getHighlightedText(text: string | number | boolean): string {
  const textStr = String(text);
  if (!searchTerm.value.trim()) return textStr;
  const lowerCaseTerm = searchTerm.value.toLowerCase();
  const regex = new RegExp(`(${lowerCaseTerm})`, 'gi');
  return textStr.replace(regex, '<mark>$1</mark>');
}
// Reference to the table element
const table = ref<HTMLTableElement | null>(null);
console.log('table1', table);

//////////////////////////////////////

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const downloadExcel = () => {
  const ws = XLSX.utils.json_to_sheet(users.value);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Users');
  XLSX.writeFile(wb, 'Users_data.xlsx');
};
</script>

<style>
/* Container for centering the table */

/* General table styling */
.styled-table1 {
  border-collapse: collapse;
  font-size: 16px;
  min-width: 600px;
  width: 100%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

.entries-controls {
  margin-bottom: 10px;
}

/* Table header styling */
.styled-table1 thead tr {
  background-color: #0b86cd;
  color: #ffffff;
  text-align: left;
  font-weight: bold;
}

th {
  text-align: center; /* Center-aligns the text inside table headers */
  padding: 8px; /* Optional: padding for better spacing */
}

/* Table cell styling */
.styled-table1 th,
.styled-table1 td {
  padding: 15px 20px;
}

/* Uniform row color */
.styled-table1 tbody tr {
  background-color: #f9f9f9; /* Set uniform color for all rows */
  border-bottom: 1px solid #dddddd;
  transition:
    background-color 0.3s ease,
    transform 0.3s ease;
}

.styled-table1 tbody tr:hover {
  background-color: #e0e0e0; /* Change color on hover */
  transform: scale(1.02);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* Responsive table */
@media screen and (max-width: 768px) {
  .styled-table1 thead {
    display: none;
  }

  .styled-table1,
  .styled-table1 tbody,
  .styled-table1 tr,
  .styled-table1 td {
    display: block;
    width: 100%;
  }

  .styled-table1 tr {
    margin-bottom: 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
  }

  .styled-table1 td {
    text-align: right;
    padding-left: 50%;
    position: relative;
  }

  .styled-table1 td::before {
    content: attr(data-label);
    position: absolute;
    left: 0;
    width: 50%;
    padding-left: 15px;
    font-weight: bold;
    text-align: left;
    color: #333;
  }
}

/* Button styling */
.download-button {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 20px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 16px;
  margin-bottom: 10px;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s ease;
  justify-content: flex-end; /* Aligns button to the right */
  margin-top: 20px; /* Adds some space above the button */
}

.download-button:hover {
  background-color: #45a049;
}

/* Pagination Controls */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.pagination-controls button {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 20px;
  margin: 0 5px;
  font-size: 16px;
  cursor: pointer;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}

.pagination-controls button:disabled {
  background-color: #8b8b8b;
  cursor: not-allowed;
}

.pagination-controls button:hover:not(:disabled) {
  background-color: #45a049;
}

/* Action Controls */

.container {
  position: relative;
  width: 100%; /* Ensure the container takes full width */
}

.search-input {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 8px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px; /* Adjust width as needed */
  box-sizing: border-box; /* Ensures padding is included in the width */
}

mark {
  background-color: yellow;
  color: black;
  font-weight: bold;
}

.sort-controls label {
  margin-right: 5px;
}

.sort-controls select {
  margin-right: 15px;
  padding: 5px;
}

.table th {
  cursor: pointer;
}
</style>
