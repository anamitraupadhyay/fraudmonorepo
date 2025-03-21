// Debug.js - Simple non-module script to check browser capabilities
console.log("Debug script loaded");
document.addEventListener('DOMContentLoaded', () => {
  console.log("DOM loaded");
  // Add visible diagnostic info to page
  const debugDiv = document.createElement('div');
  debugDiv.className = 'container alert alert-info mt-3';
  debugDiv.innerHTML = `
    <h3>Diagnostic Information</h3>
    <p>Time: ${new Date().toISOString()}</p>
    <p>Custom element registered: ${!!customElements.get('fraud-detection-form')}</p>
    <p>Browser: ${navigator.userAgent}</p>
    <div id="component-placeholder"></div>
    <button id="test-btn" class="btn btn-primary">Try Manual Render</button>
  `;
  document.body.appendChild(debugDiv);
  
  // Try manual render
  document.getElementById('test-btn').addEventListener('click', () => {
    document.getElementById('component-placeholder').innerHTML = 
      '<div class="alert alert-success">Manual rendering works!</div>';
  });
});