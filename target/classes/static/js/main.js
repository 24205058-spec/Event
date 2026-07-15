// EventFlow – main.js

// Auto-dismiss alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s';
            setTimeout(function () { alert.remove(); }, 500);
        }, 5000);
    });

    // Confirm delete buttons
    const deleteForms = document.querySelectorAll('form[data-confirm]');
    deleteForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!confirm(form.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });
});
