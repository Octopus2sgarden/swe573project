// Shared page layout: header with navigation and footer.
// Each page includes <header id="site-header"></header> and <footer id="site-footer"></footer>.
(function () {
  const links = [
    { href: "/", label: "Home" },
    { href: "/pages/crafts.html", label: "Crafts" },
    { href: "/pages/map.html", label: "Map" },
    { href: "/pages/login.html", label: "Log in" },
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
      <ul>${nav}</ul>
    </nav>`;

  document.getElementById("site-footer").innerHTML = `
    <div class="container"><small>Indigenous Crafts Platform · SWE 573</small></div>`;
})();
