document.addEventListener('DOMContentLoaded', function() {
    initFilters();
});

function initFilters() {
    const filterTabs = document.querySelectorAll('.filter-tab');
    const activityItems = document.querySelectorAll('.activity-item');

    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const filter = this.getAttribute('data-filter');

            // Update active tab
            filterTabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            // Filter activities
            activityItems.forEach(item => {
                const type = item.getAttribute('data-type');

                if (filter === 'all' ||
                    (filter === 'task' && type === 'task') ||
                    (filter === 'project' && type === 'project')) {
                    item.style.display = 'flex';
                } else {
                    item.style.display = 'none';
                }
            });
        });
    });
}