// Import Lit and geocoding functions
import { LitElement, html } from 'lit';
import { getCoordinates, getCityPopulation } from './geocoding.js';

class FraudDetectionForm extends LitElement {
  /**
   * Don't use shadow DOM so Bootstrap styles apply directly
   */
  createRenderRoot() {
    return this;
  }

  async submitForm(event) {
    event.preventDefault();

    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = '<div class="alert alert-info">Processing...</div>';
    
    // Get form values
    const ccNum = document.getElementById('ccNum').value;
    const amt = document.getElementById('amt').value;
    const zip = document.getElementById('zip').value;
    const userLocation = document.getElementById('userLocation').value;
    const merchantLocation = document.getElementById('merchantLocation').value;

    try {
      // Get coordinates for locations
      const userCoords = await getCoordinates(userLocation);
      const merchantCoords = await getCoordinates(merchantLocation);
      const population = await getCityPopulation(userLocation);

      if (!userCoords || !merchantCoords) {
        resultDiv.innerHTML = '<div class="alert alert-danger">Failed to get coordinates for locations.</div>';
        return;
      }

      // Prepare data for backend
      const data = {
        cc_num: ccNum,
        amt: parseFloat(amt),
        zip: zip,
        lat: userCoords.lat,
        lon: userCoords.lng,
        city_pop: population,
        unix_time: Math.floor(Date.now() / 1000),
        merch_lat: merchantCoords.lat,
        merch_lon: merchantCoords.lng
      };

      // Send to backend API
      const host = window.location.hostname === 'localhost' ? 'localhost' : 'quarkus-service';
      const response = await fetch(`http://${host}:8080/data-handler`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      // Process response
      const result = await response.json();

      // Show result with formatting based on prediction
      if (result.prediction === 1 || result.prediction === "1") {
        resultDiv.innerHTML = `
          <div class="alert alert-danger">
            <h4>Fraud Detected!</h4>
            <p>${result.reason || 'No reason provided'}</p>
          </div>
        `;
      } else {
        resultDiv.innerHTML = `
          <div class="alert alert-success">
            <h4>Transaction Appears Safe</h4>
            <p>${result.reason || 'No reason provided'}</p>
          </div>
        `;
      }

    } catch (error) {
      resultDiv.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    }
  }

  /**
   * Render the component
   */
  render() {
    return html`
      <form id="fraudForm" @submit=${this.submitForm}>
        <div class="form-group">
          <label for="ccNum">Credit Card Number</label>
          <input type="text" class="form-control" id="ccNum" name="cc_num" required>
        </div>

        <div class="form-group">
          <label for="amt">Amount</label>
          <input type="number" class="form-control" id="amt" name="amt" required>
        </div>

        <div class="form-group">
          <label for="zip">ZIP Code</label>
          <input type="text" class="form-control" id="zip" name="zip" required>
        </div>

        <div class="form-group">
          <label for="userLocation">User Location</label>
          <input type="text" class="form-control" id="userLocation" name="user_location" required>
        </div>

        <div class="form-group">
          <label for="merchantLocation">Merchant Location</label>
          <input type="text" class="form-control" id="merchantLocation" name="merchant_location" required>
        </div>

        <button type="submit" class="btn btn-primary">Submit</button>
      </form>

      <div id="result" class="mt-4"></div>
    `;
  }
}
// Register the custom element
customElements.define('fraud-detection-form', FraudDetectionForm);