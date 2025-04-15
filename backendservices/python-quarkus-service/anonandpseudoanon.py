import pandas as pd
import numpy as np
from math import sqrt

# Step 1: Load original data
original = pd.read_csv("predictors_only.csv")

# Step 2: Define which columns are numeric and can be noised
numeric_cols = ['amt', 'zip', 'lat', 'long', 'city_pop', 'unix_time', 'merch_lat', 'merch_long']
id_col = 'cc_num'  # we'll leave this untouched

# Step 3: Create a protected copy with added noise to numeric fields only
protected = original.copy()
np.random.seed(42)
protected[numeric_cols] += protected[numeric_cols] * np.random.normal(0, 0.05, size=protected[numeric_cols].shape)

# Step 4: Save the protected dataset
protected.to_csv("protected_data.csv", index=False)
print("✅ Saved protected_data.csv")

# Step 5: Define column weights for matching (tuneable)
weights = {
    'amt': 1.5,
    'zip': 1.0,
    'lat': 1.2,
    'long': 1.2,
    'city_pop': 1.0,
    'unix_time': 1.0,
    'merch_lat': 1.2,
    'merch_long': 1.2
}

# Step 6: Define the scoring function
def compute_score(protected_row, original_row):
    score = 0
    for col in weights:
        diff = abs(protected_row[col] - original_row[col])
        norm_diff = diff / (abs(original_row[col]) + 1e-5)  # normalize to prevent bias
        score += weights[col] * (1 / (1 + norm_diff))  # inverse score
    return score

# Step 7: Match each protected row to the closest original row
matches = []
for i, p_row in protected.iterrows():
    best_score = -1
    best_index = -1
    for j, o_row in original.iterrows():
        score = compute_score(p_row[numeric_cols], o_row[numeric_cols])
        if score > best_score:
            best_score = score
            best_index = j
    matches.append({
        "Protected_Row_Index": i,
        "Matched_Original_Index": best_index,
        "Match_Score": round(best_score, 4),
        "Original_cc_num": original.loc[best_index, 'cc_num'],
        "Protected_cc_num": protected.loc[i, 'cc_num']
    })

# Step 8: Save the matching results
results_df = pd.DataFrame(matches)
results_df.to_csv("matching_results.csv", index=False)
print("✅ Saved matching_results.csv")

# Optional: print top matches
print("\nTop 5 matches:")
print(results_df.head())
