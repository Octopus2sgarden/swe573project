// Shared page layout: header with navigation and footer.
// Each page includes <header id="site-header"></header> and <footer id="site-footer"></footer>.
(function () {
  const links = [
    { href: "/", label: "Home" },
    { href: "/pages/crafts.html", label: "Crafts" },
    { href: "/pages/map.html", label: "Map" },
  ];

  const current = window.location.pathname === "/index.html" ? "/" : window.location.pathname;

  const nav = links
    .map((link) => {
      const active = link.href === current ? ' aria-current="page"' : "";
      return `<li><a href="${link.href}"${active}>${link.label}</a></li>`;
    })
    .join("");

  document.getElementById("site-header").innerHTML = `
    <nav class="container">
      <ul><li><a href="/"><strong>Indigenous Crafts</strong></a></li></ul>
      <ul>${nav}<li id="auth-links"></li></ul>
    </nav>`;

  document.getElementById("site-footer").innerHTML = `
    <div class="container"><small>Indigenous Crafts Platform · SWE 573</small></div>`;

  // The JWT is in an HttpOnly cookie, so JavaScript cannot read it.
  // To know who is logged in, we ask the backend: /api/auth/me returns the user or 401.
  // Other pages can reuse the result: `const user = await window.currentUser;` (null = guest).
  window.currentUser = fetch(window.APP_CONFIG.API_BASE_URL + "/auth/me", {
    headers: { Accept: "application/json" },
  })
    .then((res) => (res.ok ? res.json() : null))
    .catch(() => null);

  const authLinks = document.getElementById("auth-links");

  window.currentUser.then((user) => {
    if (user) {
      authLinks.innerHTML = `
        <span>Hi, ${escapeHtml(user.username)}</span>
        <button type="button" class="secondary outline" id="logout-btn">Log out</button>`;
      document.getElementById("logout-btn").addEventListener("click", async () => {
        await fetch(window.APP_CONFIG.API_BASE_URL + "/auth/logout", { method: "POST" });
        window.location.href = "/";
      });
    } else {
      authLinks.innerHTML = `
        <a href="/pages/login.html">Log in</a>
        <a href="/pages/register.html">Register</a>`;
    }
  });

  // Usernames come from users, so never insert them into HTML without escaping (XSS).
  function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
  }
})();