FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir werkzeug==2.0.3
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install --no-cache-dir scikit-learn==1.0 imbalanced-learn==0.8.1

COPY model_train.py .
COPY app.py .
COPY balanced_dataset.csv .

# Add a startup script
COPY start.sh .
RUN chmod +x start.sh

EXPOSE 5000

# Use the startup script instead of directly running app.py
CMD ["./start.sh"]
