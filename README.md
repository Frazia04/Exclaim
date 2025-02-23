#  EXCLAIM - Course Management System

A full-stack course management system designed to manage academic activities for students, tutors, and administrators.
The system supports functionalities like lecture management, exercise creation, and exercise group formation, providing role-based access for different users.

---

## **Project Structure**

     Exclaim/ 

      ── backend/ # Core backend logic (Spring Boot) 

      ── frontend/ # Frontend interface (Vue.js + TypeScript) 

          └── components/

              └── views/ # .vue files handling UI for endpoints 
  
          └── api/ # API calls from frontend (TypeScript files)

      ── api/ # API Controllers for lectures, exercises, and groups

---

##  **Key Features & My Contributions**

###  **Lecture Management**
- **API Controllers**: Built endpoints to create, update, and delete lectures.
- **Frontend Integration**:
  - Implemented API calls in **`frontend/api/`** (TypeScript) for lecture CRUD operations.
  - Developed UI in **`frontend/components/views/`** for managing lectures.

###  **Assistant Management**
- **API Controllers**: Developed logic to add or reomve assistants to and from lectures and exercises.
- **Frontend**: Built corresponding forms and views in **`frontend/components/views/`** for assistant management.

###  **Exercise & Exercise Group Management**
- **API Controllers**: Created endpoints for exercises and exercise groups.
- **Frontend Integration**:
  - **`frontend/api/`** handles API calls.
  - **`frontend/components/views/`** renders UI for students to join groups and view exercises.

---

## **Technologies Used**

- **Frontend**: Vue.js, TypeScript
- **Backend**: Spring Boot, Java
- **Other Tools**: GitHub, Postman


