import './styles/index.scss';

import { createApp } from 'vue';

import App from './components/layout/App.vue';
import { errorHandler } from './errors';
import { setupMirage } from './mirage';
import setupRouter from './router';
import { initialize as initializeAccountData } from './store/account';
import { initialize as initializeConfiguration } from './store/configuration';

// Mock the backend api (for frontend development)
if (__MIRAGE__) {
  console.log('Development mode: Backend API requests are mocked by Mirage JS');
  setupMirage();
}

if (__BACKEND_SERVER__) {
  console.log(`Development mode: Backend API requests are proxied to ${__BACKEND_SERVER__}`);
}

// Initialize the configuration and account data stores
Promise.all([initializeConfiguration(), initializeAccountData()]).then(
  // then start the application
  () => {
    const app = createApp(App);
    app.use(setupRouter());
    app.config.errorHandler = errorHandler;
    app.mount(document.body);
  },
  // error handler if initialization fails
  console.error,
);
