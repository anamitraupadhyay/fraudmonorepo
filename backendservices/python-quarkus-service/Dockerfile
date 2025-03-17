FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir werkzeug==2.0.3
RUN pip install --no-cache-dir -r requirements.txt

COPY model.pkl .
COPY app.py .
COPY balanced_dataset.csv .
COPY model_train.py .

EXPOSE 5000

CMD ["python", "app.py"]
