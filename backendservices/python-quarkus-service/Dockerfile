FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .

# Install dependencies with fixed versions
RUN pip install --no-cache-dir werkzeug==2.0.3
RUN pip install --no-cache-dir -r requirements.txt

COPY model_train.py .
COPY app.py .
COPY balanced_dataset.csv .

EXPOSE 5000

# Train model and then run app
CMD python model_train.py && python app.py