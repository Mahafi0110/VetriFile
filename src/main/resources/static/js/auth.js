const VetriAuth = (() => {

  function getToken() {
    return sessionStorage.getItem('vetri_token') || localStorage.getItem('vetri_token');
  }

  function getUser() {
    try {
      const raw = sessionStorage.getItem('vetri_user') || localStorage.getItem('vetri_user');
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  function isLoggedIn() {
    return !!getToken();
  }

  function logout() {
    fetch('/api/auth/logout', { method: 'POST' }).finally(() => {
      sessionStorage.removeItem('vetri_token');
      sessionStorage.removeItem('vetri_user');
      localStorage.removeItem('vetri_token');
      localStorage.removeItem('vetri_user');
      window.location.href = '/login';
    });
  }

  function requireAuth() {
    if (!isLoggedIn()) {
      window.location.href = '/login';
      return false;
    }
    return true;
  }

  function renderNavbar(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const user      = getUser();
    const loggedIn  = isLoggedIn();
    const initials  = user
      ? (user.name
          ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
          : (user.email ? user.email[0].toUpperCase() : 'U'))
      : '';
    const displayName  = user?.name  || user?.email || 'My Account';
    const displayEmail = user?.email || '';
    const firstName    = displayName.split(' ')[0];

    container.innerHTML = `
      <nav class="navbar navbar-expand-lg">
        <div class="container">
          <a class="navbar-brand" href="/">
            <div class="brand-icon"><i class="bi bi-file-earmark-text-fill"></i></div>
            <span class="brand-name">Vetri Files</span>
          </a>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav mx-auto">
              <li class="nav-item"><a class="nav-link" href="/all-tools">Tools</a></li>
              <li class="nav-item"><a class="nav-link" href="/features">Features</a></li>
            </ul>
            <div class="d-flex align-items-center gap-2 mt-2 mt-lg-0">
              ${loggedIn ? `
                <div class="dropdown">
                  <button class="btn-profile-toggle" id="profileDropdown"
                          data-bs-toggle="dropdown" aria-expanded="false">
                    <div class="profile-avatar">${initials}</div>
                    <span class="profile-name d-none d-lg-inline">${firstName}</span>
                    <i class="bi bi-chevron-down chevron-icon d-none d-lg-inline"></i>
                  </button>
                  <ul class="dropdown-menu dropdown-menu-end profile-menu"
                      aria-labelledby="profileDropdown">
                    <li class="profile-menu-header">
                      <div class="profile-avatar-lg">${initials}</div>
                      <div>
                        <div class="profile-menu-name">${displayName}</div>
                        <div class="profile-menu-email">${displayEmail}</div>
                      </div>
                    </li>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                      <button class="dropdown-item profile-item text-danger"
                              onclick="VetriAuth.logout()">
                        <i class="bi bi-box-arrow-right"></i> Sign Out
                      </button>
                    </li>
                  </ul>
                </div>
              ` : `
                <a href="/login"    class="nav-link btn-login">Login</a>
                <a href="/register" class="nav-link btn-get-started">Get Started</a>
              `}
            </div>
          </div>
        </div>
      </nav>`;
  }

  return { getToken, getUser, isLoggedIn, logout, requireAuth, renderNavbar };
})();

window.VetriAuth = VetriAuth;