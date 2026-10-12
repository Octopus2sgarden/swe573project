// Frontend configuration.
// The API is reached through Nginx, which forwards /api to the backend,
// so the same relative URL works locally and in the cloud.
window.APP_CONFIG = {
  API_BASE_URL: "/api",
};
