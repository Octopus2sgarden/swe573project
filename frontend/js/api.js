// Small helper for calling the backend REST API.
window.api = {
  async get(path) {
    const response = await fetch(window.APP_CONFIG.API_BASE_URL + path, {
      headers: { Accept: "application/json" },
    });
    const body = await response.json().catch(() => null);
    return { ok: response.ok, status: response.status, body };
  },
    async post(path, data) {
    const response = await fetch(window.APP_CONFIG.API_BASE_URL + path, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });
    const body = await response.json().catch(() => null);
    return { ok: response.ok, status: response.status, body };
  },
};
