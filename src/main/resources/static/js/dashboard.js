document.addEventListener('DOMContentLoaded', function() {

    function animateCounter(element, start, end, duration) {
        let startTime = null;

        function updateCounter(currentTime) {
            if (!startTime) startTime = currentTime;
            const progress = Math.min((currentTime - startTime) / duration, 1);

            const easeOutQuad = progress * (2 - progress);
            element.textContent = Math.floor(easeOutQuad * (end - start) + start);

            if (progress < 1) {
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = end;
            }
        }

        requestAnimationFrame(updateCounter);
    }

    const statValues = document.querySelectorAll('.stat-card-value');

    statValues.forEach((stat, index) => {
        const finalValue = parseInt(stat.textContent);
        stat.textContent = '0';

        setTimeout(() => {
            animateCounter(stat, 0, finalValue, 1500);
        }, index * 100);
    });

});
