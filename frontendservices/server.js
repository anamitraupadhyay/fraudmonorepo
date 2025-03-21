const express = require('express');
const app = express();
const port = 3000;

// Set proper MIME types for ES modules
app.use((req, res, next) => {
  if (req.path.endsWith('.js')) {
    res.set('Content-Type', 'application/javascript');
  }
  next();
});

// Enable CORS for backend communication
app.use((req, res, next) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');
  next();
});

// Serve static files
app.use(express.static('.'));

// Add health check endpoint
app.get('/health', (req, res) => {
  res.send('OK');
});

// Start server
app.listen(port, '0.0.0.0', () => {
  console.log(`Server running at http://0.0.0.0:${port}`);
});