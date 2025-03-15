// Avoid ES6 imports for direct browser use
function getCoordinatesFromGlobal(location) {
    // Check if getCoordinates exists from geocoding.js
    if (typeof getCoordinates === 'function') {
        return getCoordinates(location);
    }

    // Fallback coordinates if geocoding.js isn't loaded
    const mockLocations = {
        'asansol': { lat: 23.68, lng: 86.99 },
        'kolkata': { lat: 22.57, lng: 88.36 },
        'delhi': { lat: 28.70, lng: 77.10 },
        'mumbai': { lat: 19.07, lng: 72.87 },
        'default': { lat: 0, lng: 0 }
    };

    return Promise.resolve(mockLocations[location.toLowerCase()] || mockLocations.default);
}

// Alpine.js style initialization
window.fraudForm = function () {
    return {
        async submitForm(event) {
            event.preventDefault();

            // Add at the beginning of submitForm function
            const debugContent = document.getElementById('debugContent');
            debugContent.innerHTML = '<p>Starting submission...</p>';

            // Get form values
            const ccNum = document.getElementById('ccNum').value;
            const amt = document.getElementById('amt').value;
            const zip = document.getElementById('zip').value;
            const userLocation = document.getElementById('userLocation').value;
            const merchantLocation = document.getElementById('merchantLocation').value;

            // Show loading state
            document.getElementById('result').innerHTML = '<div class="alert alert-info">Processing transaction...</div>';

            try {
                // Get coordinates for user and merchant locations
                const userCoords = await getCoordinatesFromGlobal(userLocation);
                const merchantCoords = await getCoordinatesFromGlobal(merchantLocation);

                if (!userCoords || !merchantCoords) {
                    document.getElementById('result').innerHTML = '<div class="alert alert-danger">Failed to get coordinates for locations.</div>';
                    return;
                }

                // Prepare data for backend
                const data = {
                    cc_num: parseInt(ccNum),
                    amt: parseFloat(amt),
                    zip: zip,
                    lat: userCoords.lat,
                    long: userCoords.lng,
                    city_pop: 500000, // Placeholder value
                    unix_time: Math.floor(Date.now() / 1000),
                    merch_lat: merchantCoords.lat,
                    merch_long: merchantCoords.lng
                };

                // Later, before the fetch call:
                debugContent.innerHTML += `<p>Form data: ${JSON.stringify(data)}</p>`;

                console.log("Sending data to API:", data);

                // Send data to backend - UPDATED ENDPOINT
                const response = await fetch('http://quarkus-service:8080/api/fraud/check', { // Use the IP from your HTTP server logs
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                if (!response.ok) {
                    throw new Error(`Server responded with status: ${response.status}`);
                }

                const result = await response.json();
                console.log("Received response:", result);

                // After the fetch:
                debugContent.innerHTML += `<p>Response: ${JSON.stringify(result)}</p>`;

                // Display formatted result based on fraud prediction
                if (result.prediction === 1) {
                    document.getElementById('result').innerHTML =
                        `<div class="alert alert-danger">
                            <strong>FRAUD DETECTED!</strong><br>
                            Reason: ${result.reason || 'Suspicious transaction pattern'}
                        </div>`;
                } else {
                    document.getElementById('result').innerHTML =
                        `<div class="alert alert-success">
                            <strong>Transaction Approved</strong><br>
                            No fraud detected
                        </div>`;
                }
            } catch (error) {
                console.error("Error:", error);
                document.getElementById('result').innerHTML =
                    `<div class="alert alert-danger">
                        <strong>Error Processing Transaction</strong><br>
                        ${error.message}
                    </div>`;
            }
        }
    };
};

// Add event listener for forms without Alpine.js
document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    if (form && !window.Alpine) {
        console.log("Adding vanilla JS form handler");
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            const fraudFormHandler = window.fraudForm();
            fraudFormHandler.submitForm(event);
        });
    }
});