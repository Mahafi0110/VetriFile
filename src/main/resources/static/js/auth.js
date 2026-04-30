const VetriAuth = (() => {

  /* ════════════════════════════════════════════════════
     renderNavbar('vetri-navbar')
     For ALL tool pages — replaces <div id="vetri-navbar">
     with a full Bootstrap navbar. No login state needed.
  ════════════════════════════════════════════════════ */
  function renderNavbar(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    _injectNavbarCSS();

    const path = window.location.pathname;

    container.innerHTML = `
      <nav class="navbar navbar-expand-lg">
        <div class="container">
          <a class="navbar-brand" href="/">
            <div class="brand-icon"><i class="bi bi-file-earmark-text-fill"></i></div>
            <span class="brand-name">Vetri Files</span>
          </a>
          <button class="navbar-toggler"
                  type="button"
                  id="vf-nav-toggler"
                  aria-expanded="false"
                  aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="navbar-collapse" id="vf-main-nav">
            <ul class="navbar-nav ms-auto">
              <li class="nav-item">
                <a class="nav-link ${path === '/all-tools' ? 'active' : ''}"
                   href="/all-tools">Tools</a>
              </li>
              <li class="nav-item">
                <a class="nav-link ${path === '/Feature' || path === '/features' ? 'active' : ''}"
                   href="/Feature">Features</a>
              </li>
            </ul>
          </div>
        </div>
      </nav>`;

    // ── Wire up hamburger manually ──────────────────────
    // Bootstrap data-bs-toggle won't work on dynamically
    // injected HTML. We handle it ourselves instead.
    const toggler = document.getElementById('vf-nav-toggler');
    const navMenu = document.getElementById('vf-main-nav');

    if (toggler && navMenu) {
      // Toggle open/close on hamburger click
      toggler.addEventListener('click', function (e) {
        e.stopPropagation();
        const isOpen = navMenu.classList.contains('open');
        isOpen ? closeNav(navMenu, toggler) : openNav(navMenu, toggler);
      });

      // Close when a nav link is clicked (good UX on mobile)
      navMenu.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => closeNav(navMenu, toggler));
      });

      // Close when clicking anywhere outside the navbar
      document.addEventListener('click', function (e) {
        if (!e.target.closest('.navbar')) {
          closeNav(navMenu, toggler);
        }
      });
    }
  }

  function openNav(navMenu, toggler) {
    navMenu.classList.add('open');
    toggler.setAttribute('aria-expanded', 'true');
  }

  function closeNav(navMenu, toggler) {
    navMenu.classList.remove('open');
    toggler.setAttribute('aria-expanded', 'false');
  }

  /* ════════════════════════════════════════════════════
     renderNavRight('navRight')
     For index.html and all-tools.html which use the
     custom nav-inner structure with id="navRight".
  ════════════════════════════════════════════════════ */
  function renderNavRight(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = ''; // no login buttons needed
  }

  /* ════════════════════════════════════════════════════
     _injectNavbarCSS()
     Injects navbar + mobile collapse styles once.
     Uses our own open/close class — no Bootstrap JS needed.
  ════════════════════════════════════════════════════ */
  function _injectNavbarCSS() {
    if (document.getElementById('vf-navbar-css')) return;
    const style = document.createElement('style');
    style.id = 'vf-navbar-css';
    style.textContent = `

      /* ── Navbar shell ── */
      .navbar {
        background: #ffffff;
        padding: 14px 0;
        border-bottom: 1px solid #E5E7EB;
        position: sticky;
        top: 0;
        z-index: 1050;
        box-shadow: 0 1px 8px rgba(0,0,0,.05);
      }
      .navbar > .container {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: nowrap;
        position: relative;
      }

      /* ── Brand ── */
      .navbar-brand {
        display: flex;
        align-items: center;
        gap: 10px;
        text-decoration: none;
        flex-shrink: 0;
      }
      .brand-icon {
        width: 42px; height: 42px;
        background: linear-gradient(135deg, #1E3A8A, #3B82F6);
        border-radius: 10px;
        display: flex; align-items: center; justify-content: center;
      }
      .brand-icon i { color: #fff; font-size: 1.2rem; }
      .brand-name {
        font-family: 'Poppins', sans-serif;
        font-size: 1.15rem;
        font-weight: 700;
        color: #1F2937;
        white-space: nowrap;
      }

      /* ── Hamburger button ── */
      .navbar-toggler {
        display: none;
        background: none;
        border: 1.5px solid #E5E7EB;
        border-radius: 8px;
        padding: 6px 10px;
        cursor: pointer;
        flex-shrink: 0;
        transition: border-color .2s;
      }
      .navbar-toggler:hover { border-color: #3B82F6; }
      .navbar-toggler:focus { outline: none; box-shadow: none; }
      .navbar-toggler-icon {
        display: block;
        width: 22px;
        height: 18px;
        background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 30 30'%3e%3cpath stroke='%231F2937' stroke-linecap='round' stroke-miterlimit='10' stroke-width='2' d='M4 7h22M4 15h22M4 23h22'/%3e%3c/svg%3e");
        background-repeat: no-repeat;
        background-size: contain;
        background-position: center;
      }

      /* ── Nav collapse (desktop: always visible) ── */
      .navbar-collapse {
        display: flex;
        align-items: center;
        flex-grow: 1;
        justify-content: flex-end;
      }

      /* ── Nav list ── */
      .navbar-nav {
        display: flex;
        flex-direction: row;
        list-style: none;
        margin: 0; padding: 0;
        gap: 2px;
      }

      /* ── Nav links ── */
      .navbar .nav-link {
        font-size: .95rem;
        font-weight: 500;
        color: #1F2937 !important;
        padding: 7px 14px !important;
        text-decoration: none;
        border-radius: 8px;
        display: block;
        transition: color .2s, background .2s;
        white-space: nowrap;
      }
      .navbar .nav-link:hover  { color: #3B82F6 !important; background: #F3F4F6; }
      .navbar .nav-link.active { color: #3B82F6 !important; }

      /* ════════════════════════════════
         MOBILE  (≤ 991px)
      ════════════════════════════════ */
      @media (max-width: 991.98px) {

        /* Show hamburger button */
        .navbar-toggler {
          display: block;
        }

        /* Collapse: hidden by default on mobile */
        .navbar-collapse {
          display: none;
          width: 100%;
          position: absolute;
          top: calc(100% + 1px);
          left: 0; right: 0;
          background: #ffffff;
          border-top: 1px solid #E5E7EB;
          border-bottom: 1px solid #E5E7EB;
          box-shadow: 0 8px 24px rgba(0,0,0,.1);
          z-index: 1049;
          padding: 10px 0 14px;
          flex-direction: column;
          align-items: flex-start;
          justify-content: flex-start;
        }

        /* Shown when hamburger clicked — our JS adds this class */
        .navbar-collapse.open {
          display: flex;
        }

        /* Stack links vertically */
        .navbar-nav {
          flex-direction: column;
          width: 100%;
          padding: 0 8px;
          gap: 2px;
        }

        .navbar .nav-link {
          padding: 10px 14px !important;
          width: 100%;
          border-radius: 8px;
        }
      }
    `;
    document.head.appendChild(style);
  }

  return {
    renderNavbar,
    renderNavRight
  };

})();

window.VetriAuth = VetriAuth;

/* ════════════════════════════════════════════════════
   AUTO-INIT — runs on every page that loads auth.js
   Tool pages:      <div id="vetri-navbar"></div>
   index/all-tools: <div id="navRight"></div>
════════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('vetri-navbar')) {
    VetriAuth.renderNavbar('vetri-navbar');
  }
  if (document.getElementById('navRight')) {
    VetriAuth.renderNavRight('navRight');
  }
});