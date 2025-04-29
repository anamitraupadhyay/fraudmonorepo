import { LitElement, html } from 'lit';
import { getCoordinates, getCityPopulation } from './geocoding.js';

class FraudDetectionForm extends LitElement {
  createRenderRoot() {
    return this;
  }

  async submitForm(event) {
    event.preventDefault();

    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = '<div class="alert alert-info">Processing...</div>';

    const ccNum = document.getElementById('ccNum').value;
    const amt = document.getElementById('amt').value;
    const zip = document.getElementById('zip').value;
    const userLocation = document.getElementById('userLocation').value;
    const merchantLocation = document.getElementById('merchantLocation').value;

    try {
      const userCoords = await getCoordinates(userLocation);
      const merchantCoords = await getCoordinates(merchantLocation);
      const population = await getCityPopulation(userLocation);

      if (!userCoords || !merchantCoords) {
        resultDiv.innerHTML = '<div class="alert alert-danger">Failed to get coordinates for locations.</div>';
        return;
      }

      const data = {
        cc_num: ccNum,
        amt: parseFloat(amt),
        zip: zip,
        lat: userCoords.lat,
        long: userCoords.lng,
        city_pop: population,
        unix_time: Math.floor(Date.now() / 1000),
        merch_lat: merchantCoords.lat,
        merch_long: merchantCoords.lng
      };

      const host = window.location.hostname === 'localhost' ? 'localhost' : 'quarkus-service';
      const response = await fetch(`http://${host}:8080/data-handler`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      const result = await response.json();

      let output = '';

      if (result.prediction === 1 || result.prediction === "1") {
        output += `
          <div class="alert alert-danger">
            <h4>Fraud Detected!</h4>
            <p>${result.reason || 'No reason provided'}</p>
          </div>
        `;
      } else {
        output += `
          <div class="alert alert-success">
            <h4>Transaction Appears Safe</h4>
            <p>${result.reason || 'No reason provided'}</p>
          </div>
        `;
      }

      if (result.analytics) {
        const a = result.analytics;
        output += `
          <div class="card mt-4">
            <div class="card-header"><strong>Merchant Risk Analytics</strong></div>
            <div class="card-body">
              <p><strong>High Risk Hour:</strong> ${a.isHighRiskHour ? 'Yes' : 'No'}</p>
              <p><strong>Merchant Location:</strong> ${a.merchantRisk.merchantLocation}</p>
              <p><strong>Total Transactions:</strong> ${a.merchantRisk.totalTransactions}</p>
              <p><strong>Unique Cards:</strong> ${a.merchantRisk.uniqueCards}</p>
              <p><strong>Fraud Transactions:</strong> ${a.merchantRisk.fraudTransactions}</p>
              <p><strong>Fraud Rate:</strong> ${a.merchantRisk.fraudRatePercent}%</p>
              <p><strong>Card Diversity:</strong> ${a.merchantRisk.cardDiversity}</p>
              <p><strong>Avg Transaction Amount:</strong> $${a.merchantRisk.averageTransactionAmount}</p>
            </div>
          </div>
        `;
      }

      resultDiv.innerHTML = output;

    } catch (error) {
      resultDiv.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    }
  }

  render() {
    return html`
      <form id="fraudForm" @submit=${this.submitForm}>
        <div class="form-group">
          <label for="ccNum">Credit Card Number</label>
          <input type="text" class="form-control" id="ccNum" required>
        </div>
        <div class="form-group">
          <label for="amt">Amount</label>
          <input type="number" class="form-control" id="amt" required>
        </div>
        <div class="form-group">
          <label for="zip">ZIP Code</label>
          <input type="text" class="form-control" id="zip" required>
        </div>
        <div class="form-group">
          <label for="userLocation">User Location</label>
          <input type="text" class="form-control" id="userLocation" required>
        </div>
        <div class="form-group">
          <label for="merchantLocation">Merchant Location</label>
          <input type="text" class="form-control" id="merchantLocation" required>
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>
      </form>
      <div id="result" class="mt-4"></div>
    `;
  }
}

customElements.define('fraud-detection-form', FraudDetectionForm);
