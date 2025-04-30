import pandas as pd

# Load the full dataset (replace with your own path if needed)
df = pd.read_csv("balanced_dataset.csv")

# Keep only the selected predictor columns
predictor_columns = [
    'cc_num', 'amt', 'zip', 'lat', 'long',
    'city_pop', 'unix_time', 'merch_lat', 'merch_long'
]
predictors = df[predictor_columns]

# Save to a new CSV
predictors.to_csv("predictors_only.csv", index=False)

print("Filtered CSV saved as 'predictors_only.csv'")
