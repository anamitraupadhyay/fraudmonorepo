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
        long: userCoords.lng,      
        city_pop: population,
        unix_time: Math.floor(Date.now() / 1000),
        merch_lat: merchantCoords.lat,
        merch_long: merchantCoords.lng  
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
      
      // Build result HTML
      let resultHTML = '';

      // Show transaction result with formatting based on prediction
      if (result.prediction === 1 || result.prediction === "1") {
        resultHTML += `
          <div class="alert alert-danger">
            <h4>Fraud Detected!</h4>
            <p>${result.reason || 'No reason provided'}</p>
          </div>
        `;
      } else {
        resultHTML += `
          <div class="alert alert-success">
            <h4>Transaction Appears Safe</h4>
            <p>${result.reason || 'No reason provided'}</p>
            ${result.warning ? `<p class="text-warning"><strong>Warning:</strong> ${result.warning}</p>` : ''}
          </div>
        `;
        
        // Add merchant analytics if available
        if (result.analytics) {
          resultHTML += this.renderAnalytics(result.analytics);
        }
      }

      resultDiv.innerHTML = resultHTML;

    } catch (error) {
      resultDiv.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    }
  }
  
  /**
   * Renders merchant analytics data
   */
  renderAnalytics(analytics) {
    if (!analytics) return '';
    
    let html = '<div class="card mt-3"><div class="card-header bg-info text-white"><h5 class="mb-0">Merchant Analytics</h5></div><div class="card-body">';
    
    // Add high risk hour information
    if (analytics.is_high_risk_hour !== undefined) {
      html += `
        <div class="alert ${analytics.is_high_risk_hour ? 'alert-warning' : 'alert-info'}">
          <strong>Time Analysis:</strong> 
          ${analytics.is_high_risk_hour ? 'This hour is historically high-risk for this merchant.' : 'This hour is not historically high-risk.'}
        </div>
      `;
    }
    
    // Add merchant risk information if available
    if (analytics.merchant_risk) {
      const risk = analytics.merchant_risk;
      const riskClass = {
        'high': 'danger',
        'medium': 'warning',
        'low': 'success'
      }[risk.risk_level] || 'info';
      
      html += `
        <div class="card mb-3">
          <div class="card-header bg-${riskClass} text-white">
            <h6 class="mb-0">Merchant Risk: ${risk.risk_level.toUpperCase()}</h6>
          </div>
          <div class="card-body">
            <table class="table table-sm table-striped">
              <tbody>
                <tr>
                  <th>Location</th>
                  <td>${risk.merchant_location}</td>
                </tr>
                <tr>
                  <th>Total Transactions</th>
                  <td>${risk.total_transactions}</td>
                </tr>
                <tr>
                  <th>Unique Cards</th>
                  <td>${risk.unique_cards}</td>
                </tr>
                <tr>
                  <th>Fraud Transactions</th>
                  <td>${risk.fraud_transactions}</td>
                </tr>
                <tr>
                  <th>Fraud Rate</th>
                  <td>${risk.fraud_rate_percent.toFixed(2)}%</td>
                </tr>
                <tr>
                  <th>Card Diversity</th>
                  <td>${risk.card_diversity.toFixed(2)}</td>
                </tr>
                <tr>
                  <th>Avg Transaction Amount</th>
                  <td>$${risk.average_transaction_amount.toFixed(2)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      `;
    }
    
    html += '</div></div>';
    return html;
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