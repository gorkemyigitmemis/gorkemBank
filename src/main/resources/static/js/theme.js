// theme.js
document.addEventListener('DOMContentLoaded', () => {
    const theme = localStorage.getItem('theme') || 'light';
    if (theme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
    }

    // Refresh icons on load
    updateThemeIcon();
});

// Early apply to prevent flash of unstyled content
const currentTheme = localStorage.getItem('theme') || 'light';
if (currentTheme === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark');
}

function toggleTheme() {
    let current = document.documentElement.getAttribute('data-theme');
    let targetTheme = 'light';
    
    if (current === 'dark') {
        document.documentElement.removeAttribute('data-theme');
        targetTheme = 'light';
    } else {
        document.documentElement.setAttribute('data-theme', 'dark');
        targetTheme = 'dark';
    }
    
    localStorage.setItem('theme', targetTheme);
    updateThemeIcon();
}

function updateThemeIcon() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    // Find all theme toggle buttons and update their icons
    document.querySelectorAll('.theme-toggle').forEach(btn => {
        btn.innerHTML = isDark ? '☀️' : '🌙';
    });
}
