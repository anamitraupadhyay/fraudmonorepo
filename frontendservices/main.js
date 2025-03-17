// Import necessary modules
import { getCoordinates, getCityPopulation } from './geocoding.js';

window.fraudForm = function() {
    return {
        async submitForm(event) {
            event.preventDefault();

            // Get form values
            const ccNum = document.getElementById('ccNum').value;
            const amt = document.getElementById('amt').value;
            const zip = document.getElementById('zip').value;
            const userLocation = document.getElementById('userLocation').value;
            const merchantLocation = document.getElementById('merchantLocation').value;

            // Get coordinates for user and merchant locations
            const userCoords = await getCoordinates(userLocation);
            const merchantCoords = await getCoordinates(merchantLocation);
            const population = await getCityPopulation(userLocation);

            if (!userCoords || !merchantCoords) {
                document.getElementById('result').innerHTML = '<div class="alert alert-danger">Failed to get coordinates for locations.</div>';
                return;
            }

            // Prepare data for backend
            const data = {
                cc_num: ccNum,
                amt: parseFloat(amt),
                zip: zip,
                lat: userCoords.lat,
                long: userCoords.lng,
                city_pop: population, // Use the population value from the API
                unix_time: Math.floor(Date.now() / 1000),
                merch_lat: merchantCoords.lat,
                merch_long: merchantCoords.lng
            };

            // Send data to backend
            try {
                // Detect environment - in production, use service name, otherwise use localhost
                const host = window.location.hostname === 'localhost' ? 'localhost' : 'quarkus-service';
                const response = await fetch(`http://${host}:8080/data-handler`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                const result = await response.json();
                document.getElementById('result').innerHTML = `<div class="alert alert-info">${JSON.stringify(result)}</div>`;
            } catch (error) {
                document.getElementById('result').innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
            }
        }
    };
};
