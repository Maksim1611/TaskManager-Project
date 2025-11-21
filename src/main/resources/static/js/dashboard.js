// Dashboard Counter Animation
document.addEventListener('DOMContentLoaded', function() {

    // Counter animation function
    function animateCounter(element, start, end, duration) {
        let startTime = null;

        function updateCounter(currentTime) {
            if (!startTime) startTime = currentTime;
            const progress = Math.min((currentTime - startTime) / duration, 1);

            // Easing function for smooth animation
            const easeOutQuad = progress * (2 - progress);
            const currentValue = Math.floor(easeOutQuad * (end - start) + start);

            element.textContent = currentValue;

            if (progress < 1) {
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = end; // Ensure final value is exact
            }
        }

        requestAnimationFrame(updateCounter);
    }

    // Get all stat card values
    const statValues = document.querySelectorAll('.stat-card-value');

    // Animate each counter with staggered delays
    statValues.forEach((stat, index) => {
        const finalValue = parseInt(stat.textContent);
        stat.textContent = '0'; // Start from 0

        // Stagger the animation start
        setTimeout(() => {
            animateCounter(stat, 0, finalValue, 1500); // 1.5 second duration
        }, index * 100); // 100ms delay between each
    });

});
