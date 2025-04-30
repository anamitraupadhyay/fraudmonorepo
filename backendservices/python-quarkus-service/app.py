import os
import pickle
import pandas as pd
from flask import Flask, request, jsonify

# Paths
MODEL_PATH = "model.pkl"  # Look in the current directory

# Loading the model
if not os.path.exists(MODEL_PATH):
    raise FileNotFoundError(f"Model file not found: {MODEL_PATH}")

print("Loading trained model...")
with open(MODEL_PATH, "rb") as file:
    model = pickle.load(file)

# Defined required fields (9 fields) a other fields are dropped due to being of object data type
REQUIRED_FIELDS = ["cc_num", "amt", "zip", "lat", "long", "city_pop", "unix_time", "merch_lat", "merch_long"]

# Flask app
app = Flask(__name__)

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.get_json()

        # Validate input as test case
        if not all(field in data for field in REQUIRED_FIELDS):
            return jsonify({"error": "Missing required fields", "expected": REQUIRED_FIELDS}), 400

        # Converting input to DataFrame format (ensured correct order)
        df_input = pd.DataFrame([data])[REQUIRED_FIELDS]

        # Making prediction
        prediction = model.predict(df_input)[0]

        return jsonify({
            "prediction": int(prediction),
            "reason": "ML model detected potential fraud" if prediction == 1 else "Transaction looks safe."
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)