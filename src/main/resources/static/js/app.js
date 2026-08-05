function initialiseThemeToggle() {
    const savedTheme = localStorage.getItem('shopease-theme') || 'light';
    document.documentElement.dataset.theme = savedTheme;
    const toggle = document.querySelector('[data-theme-toggle]');
    if (!toggle || toggle.dataset.ready === 'true') return;
    toggle.dataset.ready = 'true';
    const updateLabel = () => {
        const dark = document.documentElement.dataset.theme === 'dark';
        toggle.setAttribute('aria-label', dark ? 'Switch to light mode' : 'Switch to dark mode');
        toggle.setAttribute('title', dark ? 'Switch to light mode' : 'Switch to dark mode');
    };
    updateLabel();
    toggle.addEventListener('click', () => {
        const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        localStorage.setItem('shopease-theme', next);
        updateLabel();
    });
}

function initialiseMenus() {
    document.querySelectorAll('.navbar-user-btn').forEach(button => {
        if (button.dataset.menuReady) return;
        button.dataset.menuReady = 'true';
        button.addEventListener('click', event => {
            event.stopPropagation();
            const user = button.closest('.navbar-user');
            const open = user.classList.toggle('menu-open');
            button.setAttribute('aria-expanded', String(open));
        });
    });
    document.addEventListener('click', () => document.querySelectorAll('.navbar-user.menu-open').forEach(user => {
        user.classList.remove('menu-open');
        user.querySelector('.navbar-user-btn')?.setAttribute('aria-expanded', 'false');
    }));
    document.querySelectorAll('.sidebar-toggle').forEach(button => {
        button.addEventListener('click', () => {
            const sidebar = button.closest('.sidebar');
            const open = sidebar.classList.toggle('sidebar-open');
            button.setAttribute('aria-expanded', String(open));
        });
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => { initialiseThemeToggle(); initialiseMenus(); });
} else {
    initialiseThemeToggle();
    initialiseMenus();
}
