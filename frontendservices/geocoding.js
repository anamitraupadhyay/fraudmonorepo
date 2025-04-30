
const API_KEY = 'YOUR_OPENCAGE_API_KEY';

const GOOGLE_MAPS_API_KEY = 'YOUR_GOOGLE_MAPS_API_KEY';

export async function getCoordinates(location) {
    const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(location)}&key=${GOOGLE_MAPS_API_KEY}`);
    const data = await response.json();

    if (data.results.length > 0) {
        const { lat, lng } = data.results[0].geometry.location;
        return { lat, lng };
    } else {
        return null;
    }
}



export async function getCityPopulation(location) {
    const response = await fetch(`https://api.opencagedata.com/geocode/v1/json?q=${encodeURIComponent(location)}&key=${API_KEY}`);
    const data = await response.json();
    
    if (data.results.length > 0) {
        const category = data.results[0].components._category;
        
        if (category === 'place' || category === 'city') {
            const importance = parseFloat(data.results[0].importance || 0.5);
            return Math.round(importance * 1000000);
        }
        return 10000;
    }
    return 0;
}