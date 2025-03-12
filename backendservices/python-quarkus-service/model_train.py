import numpy as np
import pandas as pd
import pickle
from imblearn.over_sampling import SMOTE
from sklearn.utils import resample
from sklearn.metrics import accuracy_score
from sklearn.preprocessing import StandardScaler
from sklearn.tree import DecisionTreeClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

# Load the dataset
card = pd.read_csv(r'balanced_dataset.csv')
df = pd.DataFrame(card)

# Display basic information
print("Dataset info:")
print(df.info())
print("\nDatatype information:")
print(df.dtypes)

# Create balanced dataset (70/30 ratio)
df_majority = df[df['is_fraud'] == 0]
df_minority = df[df['is_fraud'] == 1]

n_minority = len(df_minority)
n_majority = int(n_minority * (70 / 30))

df_majority_downsampled = resample(df_majority,
                                  replace=False,
                                  n_samples=n_majority,
                                  random_state=42)

# Combine the two classes
df_balanced = pd.concat([df_majority_downsampled, df_minority])

# Shuffle the dataset
df_balanced = df_balanced.sample(frac=1, random_state=42).reset_index(drop=True)

# Drop object type columns
df_balanced = df_balanced.drop(df_balanced.select_dtypes(include=['object']).columns, axis=1)
print("\nBalanced dataset info after dropping object columns:")
print(df_balanced.info())

# Select predictors and target
predictors = df_balanced[['cc_num', 'amt', 'zip', 'lat', 'long', 'city_pop', 'unix_time', 'merch_lat', 'merch_long']]
target = df_balanced["is_fraud"]

# Split data into training and testing sets
X_train, X_test, Y_train, Y_test = train_test_split(predictors, target, test_size=0.20, random_state=42, stratify=target)

# Apply SMOTE to balance classes in training data
smote = SMOTE(sampling_strategy='auto', random_state=42)
X_train_resampled, Y_train_resampled = smote.fit_resample(X_train, Y_train)

# Check class distribution after SMOTE
print("\nBefore SMOTE:")
print(Y_train.value_counts())
print("\nAfter SMOTE:")
print(pd.Series(Y_train_resampled).value_counts())

# Decision Tree Classifier
dt_classifier = DecisionTreeClassifier()
dt_classifier.fit(X_train_resampled, Y_train_resampled)
dt_Y_pred = dt_classifier.predict(X_test)
dt_score = round(accuracy_score(dt_Y_pred, Y_test) * 100, 2)
print(f"\nDecision Tree Accuracy = {dt_score}%")

# Logistic Regression
lr_classifier = LogisticRegression()
lr_classifier.fit(X_train_resampled, Y_train_resampled)
lr_Y_pred = lr_classifier.predict(X_test)
lr_score = round(accuracy_score(lr_Y_pred, Y_test) * 100, 2)
print(f"Logistic Regression Accuracy = {lr_score}%")

# Random Forest
rf_model = RandomForestClassifier()
rf_model.fit(X_train_resampled, Y_train_resampled)
rf_Y_pred = rf_model.predict(X_test)
rf_score = round(accuracy_score(rf_Y_pred, Y_test) * 100, 2)
print(f"Random Forest Accuracy = {rf_score}%")

# Save the Random Forest model (assuming it's the best one)
with open('model.pkl', 'wb') as file:
    pickle.dump(rf_model, file)

print("\nModel saved as model.pkl")