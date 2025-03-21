// Browser compatibility fix for ES modules
window.addEventListener('DOMContentLoaded', () => {
  // Check if import maps are supported
  if (!HTMLScriptElement.supports || !HTMLScriptElement.supports('importmap')) {
    console.warn('Browser does not support import maps natively');
    document.body.innerHTML += '<div class="alert alert-warning">Your browser does not fully support import maps. Using fallback.</div>';
  }
  
  // Create fallback component if Lit fails
  setTimeout(() => {
    if (!customElements.get('fraud-detection-form')) {
      console.warn('Lit component failed to register, creating fallback');
      
      // Create basic form without Lit
      class FallbackForm extends HTMLElement {
        connectedCallback() {
          this.innerHTML = `
            <form id="fraudForm">
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
          
          this.querySelector('form').addEventListener('submit', this.handleSubmit);
        }
        
        handleSubmit(event) {
          event.preventDefault();
          document.getElementById('result').innerHTML = 
            '<div class="alert alert-info">Processing with fallback component...</div>';
        }
      }
      
      // Register fallback
      customElements.define('fraud-detection-form', FallbackForm);
    }
  }, 1000); // Give the original 1 second to load
});