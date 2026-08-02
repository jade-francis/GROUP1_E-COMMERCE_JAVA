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

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initialiseThemeToggle);
} else {
    initialiseThemeToggle();
}
