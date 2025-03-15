// Geocoding API key (replace with your own key)
const API_KEY = '070599de3b634054b32f7d632a8f8220';

// Function to get coordinates from location name
export async function getCoordinates(location) {
    const response = await fetch(`https://api.opencagedata.com/geocode/v1/json?q=${encodeURIComponent(location)}&key=${API_KEY}`);
    const data = await response.json();

    if (data.results.length > 0) {
        const { lat, lng } = data.results[0].geometry;
        return { lat, lng };
    } else {
        return null;
    }
}