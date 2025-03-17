#!/bin/bash

# Check if model exists, if not train it
if [ ! -f "model.pkl" ]; then
    echo "Training the model..."
    python model_train.py
fi

# Run the Flask application
python app.py